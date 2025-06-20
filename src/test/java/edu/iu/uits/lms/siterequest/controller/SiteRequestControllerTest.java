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

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.CanvasService;
import edu.iu.uits.lms.canvas.services.ContentMigrationService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.canvas.services.TermService;
import edu.iu.uits.lms.canvas.services.UserService;
import edu.iu.uits.lms.common.server.ServerInfo;
import edu.iu.uits.lms.iuonly.repository.HierarchyResourceRepository;
import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.iuonly.services.TemplateAuditService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import edu.iu.uits.lms.siterequest.config.ApplicationConfig;
import edu.iu.uits.lms.siterequest.config.SecurityConfig;
import edu.iu.uits.lms.siterequest.config.ToolConfig;
import edu.iu.uits.lms.siterequest.model.SiteRequestHiddenAccount;
import edu.iu.uits.lms.siterequest.repository.SiteRequestHiddenAccountRepository;
import edu.iu.uits.lms.siterequest.repository.SiteRequestPropertyRepository;
import edu.iu.uits.lms.siterequest.service.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(value = SiteRequestController.class, properties = {"oauth.tokenprovider.url=http://foo"})
@ContextConfiguration(classes = {ApplicationConfig.class, ToolConfig.class, SiteRequestController.class, SecurityConfig.class})

public class SiteRequestControllerTest {

   @Autowired
   private MockMvc mvc;

   @MockitoBean
   private AccountService accountService;
   @MockitoBean
   private CourseService courseService;
   @MockitoBean
   private ResourceBundleMessageSource messageSource;
   @MockitoBean
   private CanvasService canvasService;
   @MockitoBean
   private TermService termService;
   @MockitoBean
   private UserService userService;
   @MockitoBean
   private FeatureAccessServiceImpl featureAccessService;
   @MockitoBean
   private SiteRequestPropertyRepository siteRequestPropertyRepository;
   @MockitoBean
   private HierarchyResourceRepository hierarchyResourceRepository;
   @MockitoBean
   private SiteRequestHiddenAccountRepository siteRequestHiddenAccountRepository;
   @MockitoBean
   private TemplateAuditService templateAuditService;
   @MockitoBean
   private ContentMigrationService contentMigrationService;
   @MockitoBean
   private DefaultInstructorRoleRepository defaultInstructorRoleRepository;
   @MockitoBean
   private AuthorizedUserService authorizedUserService;
   @MockitoBean
   private LmsDefaultGrantedAuthoritiesMapper defaultGrantedAuthoritiesMapper;
   @MockitoBean
   private ClientRegistrationRepository clientRegistrationRepository;
   @MockitoBean(name = ServerInfo.BEAN_NAME)
   private ServerInfo serverInfo;

   private static String userLoginId = "userLoginId1";

   @BeforeEach
   public void mockCommonStuff(TestInfo testInfo) {
      if (! testInfo.getTestMethod().get().getName().equals("userDoesNotTeachAnyCourses")) {
         Mockito.when(courseService.getCoursesTaughtBy(any(), anyBoolean(), anyBoolean(), anyBoolean()))
                 .thenReturn(List.of(new Course()));
      }

      Account account1 = new Account();
      account1.setId("1");

      Account account2 = new Account();
      account2.setId("2");

      Account account3 = new Account();
      account3.setId("3");

      Mockito.when(accountService.getAccountsForUser(userLoginId))
              .thenReturn(List.of(account1, account2, account3));

      SiteRequestHiddenAccount siteRequestHiddenAccount1 = new SiteRequestHiddenAccount();
      siteRequestHiddenAccount1.setAccountIdToHide(1L);

      SiteRequestHiddenAccount siteRequestHiddenAccount2 = new SiteRequestHiddenAccount();
      siteRequestHiddenAccount2.setAccountIdToHide(2L);

      Mockito.when(siteRequestHiddenAccountRepository.findAll()).thenReturn(List.of(siteRequestHiddenAccount1, siteRequestHiddenAccount2));

      User user1 = new User();
      user1.setName("name1");

      Mockito.when(userService.getUserBySisLoginId(userLoginId)).thenReturn(user1);

   }

   @Test
   public void frontendModeNotSuppliedForCreateSite() throws Exception {
      Map<String, Object> extraAttributesMap = new HashMap<>();
      extraAttributesMap.put(LTIConstants.CLAIMS_KEY_ROLES, List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

      Map<String, Object> customAttributesMap = new HashMap<>();
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "1234");
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, userLoginId);

      OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.INSTRUCTOR_AUTHORITY, extraAttributesMap, customAttributesMap);

      SecurityContextHolder.getContext().setAuthentication(token);

      ResultActions resultActions = mvc.perform(get("/app/createsite")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      Assertions.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
      Assertions.assertEquals("siterequest_error", resultActions.andReturn().getModelAndView().getViewName());
   }

   @Test
   public void frontendModeIsSuppliedForCreateSite() throws Exception {
      Map<String, Object> extraAttributesMap = new HashMap<>();
      extraAttributesMap.put(LTIConstants.CLAIMS_KEY_ROLES, List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

      Map<String, Object> customAttributesMap = new HashMap<>();
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "1234");
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, userLoginId);
      customAttributesMap.put(Constants.IS_FRONTEND_MODE, "true");

      OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.INSTRUCTOR_AUTHORITY, extraAttributesMap, customAttributesMap);

      SecurityContextHolder.getContext().setAuthentication(token);

      ResultActions resultActions = mvc.perform(get("/app/createsite")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      Assertions.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
      Assertions.assertEquals("create_site", resultActions.andReturn().getModelAndView().getViewName());
   }

   @Test
   public void userDoesNotTeachAnyCourses() throws Exception {
      Map<String, Object> extraAttributesMap = new HashMap<>();
      extraAttributesMap.put(LTIConstants.CLAIMS_KEY_ROLES, List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

      Map<String, Object> customAttributesMap = new HashMap<>();
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "1234");
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, userLoginId);
      customAttributesMap.put(Constants.IS_FRONTEND_MODE, "true");

      OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.INSTRUCTOR_AUTHORITY, extraAttributesMap, customAttributesMap);

      SecurityContextHolder.getContext().setAuthentication(token);

      ResultActions resultActions = mvc.perform(get("/app/createsite")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      Assertions.assertEquals(200, resultActions.andReturn().getResponse().getStatus());
      Assertions.assertEquals("siterequest_error", resultActions.andReturn().getModelAndView().getViewName());

      Boolean notTeacher = (Boolean) resultActions.andReturn().getModelAndView().getModel().get("notTeacher");
      Assertions.assertNotNull(notTeacher);
      Assertions.assertTrue(notTeacher);
   }

   @Test
   public void omitAccount_UserIsNotAdminSomeAccountsRemoved() throws Exception {
      Map<String, Object> extraAttributesMap = new HashMap<>();
      extraAttributesMap.put(LTIConstants.CLAIMS_KEY_ROLES, List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

      Map<String, Object> customAttributesMap = new HashMap<>();
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "1234");
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, userLoginId);
      customAttributesMap.put(Constants.IS_FRONTEND_MODE, "true");

      OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.INSTRUCTOR_AUTHORITY, extraAttributesMap, customAttributesMap);

      SecurityContextHolder.getContext().setAuthentication(token);

      ResultActions resultActions = mvc.perform(get("/app/createsite")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON));

      Assertions.assertEquals(200, resultActions.andReturn().getResponse().getStatus());

      List<Account> accounts = (List<Account>) resultActions.andReturn().getModelAndView().getModel().get("accounts");

      Assertions.assertNotNull(accounts);
      Assertions.assertEquals(1, accounts.size());
      Assertions.assertEquals("3", accounts.getFirst().getId());
   }

   @Test
   public void omitAccount_UserIsAdminAccountsNotRemoved() throws Exception {
      Map<String, Object> extraAttributesMap = new HashMap<>();
      extraAttributesMap.put(LTIConstants.CLAIMS_KEY_ROLES, List.of(LTIConstants.CANVAS_ADMIN_ROLE));

      Map<String, Object> customAttributesMap = new HashMap<>();
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "1234");
      customAttributesMap.put(LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY, userLoginId);
      customAttributesMap.put(Constants.IS_FRONTEND_MODE, "true");

      OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.ADMIN_ROLE, extraAttributesMap, customAttributesMap);

      SecurityContextHolder.getContext().setAuthentication(token);

      ResultActions resultActions = mvc.perform(get("/app/createsite")
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON));

      Assertions.assertEquals(200, resultActions.andReturn().getResponse().getStatus());

      List<Account> accounts = (List<Account>) resultActions.andReturn().getModelAndView().getModel().get("accounts");

      Assertions.assertNotNull(accounts);
      Assertions.assertEquals(3, accounts.size());
   }
}