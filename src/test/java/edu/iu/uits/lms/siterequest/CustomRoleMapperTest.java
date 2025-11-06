package edu.iu.uits.lms.siterequest;

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

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.siterequest.config.CustomRoleMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.iu.uits.lms.siterequest.service.Constants.AUTH_ADMIN_TOOL_PERMISSION;

public class CustomRoleMapperTest {
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository = Mockito.mock();

    private AuthorizedUserService authorizedUserService = Mockito.mock();

    private static final String userLoginId = "userLoginId1";

    @Test
    public void testFrontEndUser() throws Exception {
        CustomRoleMapper customRoleMapper = new CustomRoleMapper(defaultInstructorRoleRepository, authorizedUserService);

        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("is_frontend_mode", "true");
        customClaims.put("canvas_user_login_id", userLoginId);
        customClaims.put("instructure_membership_roles", LTIConstants.CANVAS_INSTRUCTOR_ROLE);

        claims.put(LTIConstants.CLAIMS_KEY_CUSTOM, customClaims);

        OidcIdToken oidcIdToken = new OidcIdToken("value", Instant.now(), Instant.MAX, claims);
        OidcUserAuthority oidcUserAuthority = new OidcUserAuthority(oidcIdToken);

        Mockito.when(defaultInstructorRoleRepository.findInstructorRoles()).thenReturn(List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

        List<GrantedAuthority> mappedAuthorities = (List<GrantedAuthority>) customRoleMapper.mapAuthorities(List.of(oidcUserAuthority));

        long numberOfOidcUsers = mappedAuthorities.stream()
                .filter(grantedAuthority ->  "OIDC_USER".equals(grantedAuthority.getAuthority()))
                .count();

        long numberOfInstructorAuthorites = mappedAuthorities.stream()
                .filter(grantedAuthority -> LTIConstants.INSTRUCTOR_AUTHORITY.equals(grantedAuthority.getAuthority()))
                .count();

        Assertions.assertNotNull(mappedAuthorities);
        Assertions.assertEquals(2, mappedAuthorities.size());
        Assertions.assertEquals(1, numberOfOidcUsers);
        Assertions.assertEquals(1, numberOfInstructorAuthorites);
    }

    @Test
    public void testAdminNotAuthUser() throws Exception {
        CustomRoleMapper customRoleMapper = new CustomRoleMapper(defaultInstructorRoleRepository, authorizedUserService);

        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("canvas_user_login_id", userLoginId);
        customClaims.put("instructure_membership_roles", LTIConstants.CANVAS_INSTRUCTOR_ROLE);

        claims.put(LTIConstants.CLAIMS_KEY_CUSTOM, customClaims);

        OidcIdToken oidcIdToken = new OidcIdToken("value", Instant.now(), Instant.MAX, claims);
        OidcUserAuthority oidcUserAuthority = new OidcUserAuthority(oidcIdToken);

        Mockito.when(defaultInstructorRoleRepository.findInstructorRoles()).thenReturn(List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

        List<GrantedAuthority> mappedAuthorities = (List<GrantedAuthority>) customRoleMapper.mapAuthorities(List.of(oidcUserAuthority));

        long numberOfOidcUsers = mappedAuthorities.stream()
                .filter(grantedAuthority ->  "OIDC_USER".equals(grantedAuthority.getAuthority()))
                .count();

        Assertions.assertNotNull(mappedAuthorities);
        Assertions.assertEquals(1, mappedAuthorities.size());
        Assertions.assertEquals(1, numberOfOidcUsers);
    }

    @Test
    public void testAdminIsAuthUser() throws Exception {
        CustomRoleMapper customRoleMapper = new CustomRoleMapper(defaultInstructorRoleRepository, authorizedUserService);

        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("canvas_user_login_id", userLoginId);
        customClaims.put("instructure_membership_roles", LTIConstants.CANVAS_INSTRUCTOR_ROLE);

        claims.put(LTIConstants.CLAIMS_KEY_CUSTOM, customClaims);

        OidcIdToken oidcIdToken = new OidcIdToken("value", Instant.now(), Instant.MAX, claims);
        OidcUserAuthority oidcUserAuthority = new OidcUserAuthority(oidcIdToken);

        Mockito.when(defaultInstructorRoleRepository.findInstructorRoles()).thenReturn(List.of(LTIConstants.CANVAS_INSTRUCTOR_ROLE));

        AuthorizedUser authorizedUser = new AuthorizedUser();
        authorizedUser.setId(1L);
        authorizedUser.setCanvasUserId(userLoginId);

        Mockito.when(authorizedUserService.findByActiveUsernameAndToolPermission(userLoginId, AUTH_ADMIN_TOOL_PERMISSION))
                .thenReturn(authorizedUser);

        List<GrantedAuthority> mappedAuthorities = (List<GrantedAuthority>) customRoleMapper.mapAuthorities(List.of(oidcUserAuthority));

        long numberOfOidcUsers = mappedAuthorities.stream()
                        .filter(grantedAuthority ->  "OIDC_USER".equals(grantedAuthority.getAuthority()))
                        .count();

        long numberOfInstructorAuthorites = mappedAuthorities.stream()
                .filter(grantedAuthority -> LTIConstants.INSTRUCTOR_AUTHORITY.equals(grantedAuthority.getAuthority()))
                .count();

        Assertions.assertNotNull(mappedAuthorities);
        Assertions.assertEquals(2, mappedAuthorities.size());
        Assertions.assertEquals(1, numberOfOidcUsers);
        Assertions.assertEquals(1, numberOfInstructorAuthorites);
    }
}
