package edu.iu.uits.lms.siterequest.controller;

import canvas.client.generated.api.AccountsApi;
import canvas.client.generated.api.CanvasApi;
import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.api.TermsApi;
import canvas.client.generated.api.UsersApi;
import canvas.client.generated.model.Account;
import canvas.client.generated.model.CanvasTerm;
import canvas.client.generated.model.Course;
import canvas.client.generated.model.CourseCreateWrapper;
import canvas.client.generated.model.Enrollment;
import canvas.client.generated.model.EnrollmentCreateWrapper;
import canvas.client.generated.model.FeatureFlag;
import canvas.client.generated.model.License;
import canvas.client.generated.model.Profile;
import canvas.client.generated.model.Section;
import canvas.client.generated.model.SectionCreateWrapper;
import canvas.client.generated.model.User;
import canvas.helpers.CanvasDateFormatUtil;
import canvas.helpers.CourseHelper;
import edu.iu.uits.lms.common.coursetemplates.CourseTemplateMessage;
import edu.iu.uits.lms.lti.controller.LtiAuthenticationTokenAwareController;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import edu.iu.uits.lms.siterequest.config.CourseTemplateMessageSender;
import edu.iu.uits.lms.siterequest.model.SiteRequestProperty;
import edu.iu.uits.lms.siterequest.repository.SiteRequestPropertyRepository;
import iuonly.client.generated.api.FeatureAccessApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by chmaurer on 8/26/15.
 */
@Controller
@RequestMapping("/app")
@Slf4j
public class SiteRequestController extends LtiAuthenticationTokenAwareController {
    @Autowired
    private AccountsApi accountService = null;
    @Autowired
    private CoursesApi courseService = null;
    @Autowired
    private ResourceBundleMessageSource messageSource = null;
    @Autowired
    private CanvasApi canvasApi = null;
    @Autowired
    private TermsApi termService = null;
    @Autowired
    private UsersApi userService = null;
    @Autowired
    private FeatureAccessApi featureAccessService = null;
    @Autowired
    private CourseTemplateMessageSender courseTemplateMessageSender = null;
    @Autowired
    private SiteRequestPropertyRepository siteRequestPropertyRepository = null;

    private static final String FEATURE_APPLY_TEMPLATE_FOR_MANUAL_COURSES = "coursetemplating.manualCourses";
    private static final String FEATURE_ENABLE_FEATURES_FOR_MANUAL_COURSES = "manualCourses.enableFeatureSetting";

    @RequestMapping("/createsite")
    public String createSite(Model model) {
        LtiAuthenticationToken token = getTokenWithoutContext();
        String username = (String)token.getPrincipal();

        List<Course> instructorCourseList = courseService.getCoursesTaughtBy(username, false, false, false);

        if (instructorCourseList==null || instructorCourseList.isEmpty()) {
            model.addAttribute("notTeacher", true);
            return notTeacher(model);
        }

        // The api call used here seems to sort by name, but not sure if that's always the case or not. - CWM
        List<Account> accounts = accountService.getAccountsForUser(username);

        List<License> licenseList = userService.getLicenses(username);

        User user = userService.getUserBySisLoginId(username);

        model.addAttribute("accounts", accounts);
        model.addAttribute("licenseList", licenseList);
        model.addAttribute("username", username);
        model.addAttribute("displayname", user.getName());

        return "create_site";
    }

    @RequestMapping("/provisionsite")
    public String createSite(Model model, @RequestParam("course_name") String courseName,
                             @RequestParam("short_name") String shortName, @RequestParam("course_license") String courseLicense,
                             @RequestParam("node_location") String nodeLocation, SecurityContextHolderAwareRequestWrapper request) {
        // make sure they're still logged in!
        LtiAuthenticationToken token = getTokenWithoutContext();
        String username = (String)token.getPrincipal();

        if (courseName.isEmpty() || shortName.isEmpty() || courseLicense.isEmpty() || nodeLocation.isEmpty()) {
            // check specifically to know which error messages to display
            if (courseName.isEmpty()) {
                model.addAttribute("courseNameError", messageSource.getMessage("create.courseName.error", null, Locale.getDefault()));
            }
            if (shortName.isEmpty()) {
                model.addAttribute("shortNameError", messageSource.getMessage("create.shortName.error", null, Locale.getDefault()));
            }
            if (courseLicense.isEmpty()) {
                model.addAttribute("courseLicenseError", messageSource.getMessage("create.license.error", null, Locale.getDefault()));
            }
            if (nodeLocation.isEmpty()) {
                model.addAttribute("nodeLocationError", messageSource.getMessage("create.nodeLocation.error", null, Locale.getDefault()));
            }
            // add all attributes back so the page will still know what was entered previously
            // no problem if any of them are actually empty
            model.addAttribute("coursename", courseName);
            model.addAttribute("shortname", shortName);
            model.addAttribute("courselicense", courseLicense);
            model.addAttribute("nodelocation", nodeLocation);
            return createSite(model);
        }

        // make the rest call to create the site in Canvas
        CourseCreateWrapper ccw = new CourseCreateWrapper();
        ccw.setAccountId(nodeLocation);
        Course course = new Course();
        course.setName(courseName);
        course.setCourseCode(shortName);
        course.setLicense(courseLicense);
        course.setRestrictEnrollmentsToCourseDates(true);

        CanvasTerm currentTerm = termService.getCurrentYearTerm();

        if (currentTerm==null || currentTerm.getId() == null) {
            // Creation of the new term failed, so the course cannot be created
            model.addAttribute("courseCreationError", true);
            return submissionFailure(model);
        }

        course.setEnrollmentTermId(currentTerm.getId());

        OffsetDateTime endDate = CanvasDateFormatUtil.getCalculatedCourseEndDate();

        // the default toString for Instant is ISO-8601
        String endDateString = endDate.toInstant().toString();

        course.setEndAt(endDateString);

        ccw.setCourse(course);
        Course createdCourse = courseService.createCourse(ccw);

        if (createdCourse==null) {
            model.addAttribute("courseCreationError", true);
            return submissionFailure(model);
        }

        List<Account> parentAccounts = accountService.getParentAccounts(createdCourse.getAccountId());
        List<String> parentAccountIds = parentAccounts.stream().map(Account::getId).collect(Collectors.toList());

        // Apply the template to the newly created course
        if (featureAccessService.isFeatureEnabledForAccount(FEATURE_APPLY_TEMPLATE_FOR_MANUAL_COURSES, createdCourse.getAccountId(), parentAccountIds)) {
            //When a course is first created, canvas does not return anything for the term, so need to look it up if it's not provided
            String sisTermId = null;
            if (createdCourse.getTerm() != null) {
                sisTermId = createdCourse.getTerm().getSisTermId();
            } else {
                CanvasTerm term = termService.getTermById(createdCourse.getEnrollmentTermId());
                sisTermId = term.getSisTermId();
            }
            CourseTemplateMessage ctm = new CourseTemplateMessage(createdCourse.getId(), sisTermId, createdCourse.getAccountId(), createdCourse.getSisCourseId(), false);
            courseTemplateMessageSender.send(ctm);
        }

        // Set the features on the newly created course
        if (featureAccessService.isFeatureEnabledForAccount(FEATURE_ENABLE_FEATURES_FOR_MANUAL_COURSES, canvasApi.getRootAccount(), null)) {
            checkAndSetCourseFeatures(createdCourse);
        }

        // create the section
        Section section = new Section();
        section.setName(createdCourse.getName());
        section.setCourseId(createdCourse.getId());
        SectionCreateWrapper scw = new SectionCreateWrapper();
        scw.setCourseSection(section);
        Section newSection = courseService.createCourseSection(scw);

        if (newSection==null) {
            // Course was created, but no section or enrollment
            model.addAttribute("sectionCreationError", true);
            model.addAttribute("courseName", createdCourse.getName());
            model.addAttribute("courseId", createdCourse.getId());
            return submissionFailure(model);
        }

        // course was created successfully, now enroll the user as a teacher!
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(createdCourse.getId());

        User canvasUser = userService.getUserBySisLoginId(username);

        enrollment.setUserId(canvasUser.getId());
        enrollment.setType("TeacherEnrollment");
        enrollment.setEnrollmentState("active");
        enrollment.setCourseSectionId(newSection.getId());
        EnrollmentCreateWrapper ecw = new EnrollmentCreateWrapper();
        ecw.setEnrollment(enrollment);
        Enrollment newEnrollment = courseService.createEnrollment(ecw);

        if (newEnrollment==null) {
            model.addAttribute("userEnrollmentError", true);
            model.addAttribute("courseName", createdCourse.getName());
            model.addAttribute("courseId", createdCourse.getId());
            return submissionFailure(model);
        }

        // if no problems, set the attributes and send to the success page
        // The information from the request are probably the same, but let's grab what's official from the createdCourse
        model.addAttribute("courseName", createdCourse.getName());
        model.addAttribute("shortName", createdCourse.getCourseCode());
        model.addAttribute("license", convertLicenseId(username, createdCourse.getLicense()));

        // We will display the end date in the user's preferred time zone.
        // If the time zone isn't included in the User object (it usually isn't),
        // retrieve the user's profile
        String timeZone = canvasUser.getTimeZone();
        if (timeZone == null) {
            Profile profile = userService.getProfile(canvasUser.getSisUserId());
            if (profile != null) {
                timeZone = profile.getTimeZone();
            }
        }
        String endDateDisplay = CanvasDateFormatUtil.formatISODateForDisplay(createdCourse.getEndAt(), timeZone, CanvasDateFormatUtil.DISPLAY_FORMAT_WITH_TZ);
        model.addAttribute("endDate", endDateDisplay);

        // Example: https://iu.instructure.com/courses/123456
        model.addAttribute("url", canvasApi.getBaseUrl() + "/courses/" + createdCourse.getId());
        model.addAttribute("displayName", canvasUser.getName());

        Account account = accountService.getAccount(createdCourse.getAccountId());

        // If account is null, not really a huge deal, but will not have the account name on the success page
        if (account!=null) {
            model.addAttribute("accountName", account.getName());
        }

        return createSuccess(model);
    }


    @RequestMapping("/createsuccess")
    public String createSuccess(Model model) {

        return "create_success";
    }

    @RequestMapping("/notteacher")
    public String notTeacher(Model model) {
        return "error";
    }

    @RequestMapping("/submissionfailure")
    public String submissionFailure(Model model) {
        return "error";
    }

    /**
     * Use this to translate the Canvas code for license into the user friendly, readable version
     * @param canvasLicenseId
     * @return
     */
    private String convertLicenseId(String sis_login_id, String canvasLicenseId) {
        String licenseText = "";

        List<License> licenseList = userService.getLicenses(sis_login_id);

        for (License license : licenseList) {
            if (canvasLicenseId.equals(license.getId())) {
                licenseText = license.getName();
                break;
            }
        }

        return licenseText;
    }

    /**
     * Check to see if certain features are enabled for the course and enable them if not.
     * @param course
     */
    private void checkAndSetCourseFeatures(Course course) {
        final String COURSE_FEATURES_KEY = "course.provisioning.features";

        SiteRequestProperty siteRequestProperty = siteRequestPropertyRepository.findByKey(COURSE_FEATURES_KEY);

        if (siteRequestProperty != null) {
            List<String> features = siteRequestProperty.getPropValueAsList();

            if (features != null) {
                for (String feature : features) {
                    FeatureFlag ff = courseService.getCourseFeature(course.getId(), feature);
                    if (!CourseHelper.FEATURE_FLAG_STATE.on.name().equals(ff.getState())) {
                        courseService.setCourseFeature(course.getId(), feature, CourseHelper.FEATURE_FLAG_STATE.on.name());
                    }
                }
            }
        }
    }
}
