package edu.iu.uits.lms.siterequest.controller;

/*-
 * #%L
 * siterequest
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.canvas.helpers.CanvasDateFormatUtil;
import edu.iu.uits.lms.canvas.helpers.ContentMigrationHelper;
import edu.iu.uits.lms.canvas.helpers.CourseHelper;
import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.canvas.model.ContentMigration;
import edu.iu.uits.lms.canvas.model.ContentMigrationCreateWrapper;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.CourseCreateWrapper;
import edu.iu.uits.lms.canvas.model.Enrollment;
import edu.iu.uits.lms.canvas.model.EnrollmentCreateWrapper;
import edu.iu.uits.lms.canvas.model.FeatureFlag;
import edu.iu.uits.lms.canvas.model.License;
import edu.iu.uits.lms.canvas.model.Profile;
import edu.iu.uits.lms.canvas.model.Section;
import edu.iu.uits.lms.canvas.model.SectionCreateWrapper;
import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.TermService;
import edu.iu.uits.lms.canvas.services.UserService;
import edu.iu.uits.lms.iuonly.model.HierarchyResource;
import edu.iu.uits.lms.iuonly.model.StoredFile;
import edu.iu.uits.lms.iuonly.repository.HierarchyResourceRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceException;
import edu.iu.uits.lms.iuonly.services.TemplateAuditService;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import edu.iu.uits.lms.siterequest.config.ToolConfig;
import edu.iu.uits.lms.siterequest.model.SiteRequestHiddenAccount;
import edu.iu.uits.lms.siterequest.model.SiteRequestProperty;
import edu.iu.uits.lms.siterequest.repository.SiteRequestHiddenAccountRepository;
import edu.iu.uits.lms.siterequest.repository.SiteRequestPropertyRepository;
import edu.iu.uits.lms.siterequest.service.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.ac.ox.ctl.lti13.lti.Role;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by chmaurer on 8/26/15.
 */
@Controller
@RequestMapping("/app")
@Slf4j
public class SiteRequestController extends OidcTokenAwareController {
    @Autowired
    private AccountService accountService = null;
    @Autowired
    private CourseService courseService = null;
    @Autowired
    private ResourceBundleMessageSource messageSource = null;
    @Autowired
    private CanvasService canvasService = null;
    @Autowired
    private TermService termService = null;
    @Autowired
    private UserService userService = null;
    @Autowired
    private FeatureAccessServiceImpl featureAccessService = null;
    @Autowired
    private SiteRequestPropertyRepository siteRequestPropertyRepository = null;
    @Autowired
    private HierarchyResourceRepository hierarchyResourceRepository = null;
    @Autowired
    private SiteRequestHiddenAccountRepository siteRequestHiddenAccountRepository = null;
    @Autowired
    private ToolConfig toolConfig = null;
    @Autowired
    private TemplateAuditService templateAuditService = null;
    @Autowired
    private ContentMigrationService contentMigrationService = null;

    private static final String FEATURE_APPLY_TEMPLATE_FOR_MANUAL_COURSES = "coursetemplating.manualCourses";
    private static final String FEATURE_ENABLE_FEATURES_FOR_MANUAL_COURSES = "manualCourses.enableFeatureSetting";

    @RequestMapping({"/createsite", "/launch"})
    public String createSite(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (! Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }
        
        String username = oidcTokenUtils.getUserLoginId();

        List<Course> instructorCourseList = courseService.getCoursesTaughtBy(username, false, false, false);

        if (instructorCourseList==null || instructorCourseList.isEmpty()) {
            model.addAttribute("notTeacher", true);
            return notTeacher(model);
        }

        // The api call used here seems to sort by name, but not sure if that's always the case or not. - CWM
        List<Account> accounts = accountService.getAccountsForUser(username);

        List<String> roles = null;

        try {
            roles = Arrays.asList(oidcTokenUtils.getAllRoles());
        } catch (Exception e) {
            roles = new ArrayList<>();
        }

        final boolean isAdmin = roles.contains(Role.Institution.ADMINISTRATOR);

        if (! isAdmin) {
            List<SiteRequestHiddenAccount> siteRequestHiddenAccounts = siteRequestHiddenAccountRepository.findAll();

            if (! siteRequestHiddenAccounts.isEmpty()) {
                List<String> siteRequestHiddenAccountIds = siteRequestHiddenAccounts.stream()
                        .map(siteRequestAccountOmit -> siteRequestAccountOmit.getAccountIdToHide().toString())
                        .toList();

                accounts = accounts.stream()
                        .filter(account -> ! siteRequestHiddenAccountIds.contains(account.getId()))
                        .toList();
            }
        }

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
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String username = oidcTokenUtils.getUserLoginId();

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
            HierarchyResource templateForCourse = null;
            try {
                templateForCourse = getClosestDefaultTemplateForCourse(createdCourse);
            } catch (HierarchyResourceException e) {
                model.addAttribute("courseCreationError", true);
                return submissionFailure(model);
            }

            StoredFile storedFile = templateForCourse.getStoredFile();
            String baseUrl = toolConfig.getTemplateHostingUrl();
            // Use the current application as the template host if no other has been configured.
            if (baseUrl == null) {
                baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            }

            String url = MessageFormat.format("{0}/rest/iu/file/download/{1}/{2}", baseUrl, storedFile.getId(), storedFile.getDisplayName());
            log.debug("Course template (" + templateForCourse.getId() + ") url: " + url);

            ContentMigrationCreateWrapper wrapper = new ContentMigrationCreateWrapper();
            ContentMigrationCreateWrapper.Settings settings = new ContentMigrationCreateWrapper.Settings();
            wrapper.setMigrationType(ContentMigrationHelper.MIGRATION_TYPE_CC);
            wrapper.setSettings(settings);

            settings.setFileUrl(url);

            ContentMigration cm = contentMigrationService.initiateContentMigration(createdCourse.getId(), null, wrapper);
            log.info("{}", cm);
            templateAuditService.audit(createdCourse.getId(), templateForCourse, "SITE_REQUEST", username);
        }

        // Set the features on the newly created course
        if (featureAccessService.isFeatureEnabledForAccount(FEATURE_ENABLE_FEATURES_FOR_MANUAL_COURSES, canvasService.getRootAccount(), null)) {
            checkAndSetCourseFeatures(createdCourse);
        }

        // create the section
        Section section = new Section();
        section.setName(createdCourse.getName());
        section.setCourse_id(createdCourse.getId());
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
            Profile profile = null;

            if (canvasUser.getSisUserId() != null) {
                profile = userService.getProfile(canvasUser.getSisUserId());
            }

            if (profile != null) {
                timeZone = profile.getTimeZone();
            }
        }
        String endDateDisplay = CanvasDateFormatUtil.formatISODateForDisplay(createdCourse.getEndAt(), timeZone, CanvasDateFormatUtil.DISPLAY_FORMAT_WITH_TZ);
        model.addAttribute("endDate", endDateDisplay);

        // Example: https://iu.instructure.com/courses/123456
        model.addAttribute("url", canvasService.getBaseUrl() + "/courses/" + createdCourse.getId());
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
        return "siterequest_error";
    }

    @RequestMapping("/submissionfailure")
    public String submissionFailure(Model model) {
        return "siterequest_error";
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

    // Maybe centralize this?
    private HierarchyResource getClosestDefaultTemplateForCourse(Course course) throws HierarchyResourceException {
        String bodyText = "";
        if (course!=null) {
            Account account = accountService.getAccount(course.getAccountId());
            if (account!=null) {
                List<HierarchyResource> hierarchyResources = hierarchyResourceRepository.findByNodeAndDefaultTemplateTrue(account.getName());
                if (hierarchyResources != null && hierarchyResources.size() == 1) {
                    return hierarchyResources.get(0);
                } else {
                    // specific account doesn't exist in our table, let's see if there's a parent
                    List<String> relatedAccountNames = new ArrayList<>();
                    accountService.getParentAccounts(account.getId()).forEach(parentAccount -> relatedAccountNames.add(parentAccount.getName()));

                    for (String accountName : relatedAccountNames) {
                        List<HierarchyResource> parentHierarchyResources = hierarchyResourceRepository.findByNodeAndDefaultTemplateTrue(accountName);
                        if (parentHierarchyResources != null && parentHierarchyResources.size() == 1) {
                            return parentHierarchyResources.get(0);
                        }
                    }
                }

                // if we're here, could not find a record in our table
                bodyText = "No node found for " + course.getId() + " (" + course.getSisCourseId() + ")";
            } else {
                bodyText = "Could not find account!";
            }
        } else {
            bodyText = "Course does not exist!";
        }

        // if we made it here, it did not find something along the way
        throw new HierarchyResourceException(bodyText);
    }
}
