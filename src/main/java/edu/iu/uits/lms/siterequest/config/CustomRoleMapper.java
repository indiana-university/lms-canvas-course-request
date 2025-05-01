package edu.iu.uits.lms.siterequest.config;

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static edu.iu.uits.lms.siterequest.service.Constants.AUTH_ADMIN_TOOL_PERMISSION;

@Slf4j
public class CustomRoleMapper extends LmsDefaultGrantedAuthoritiesMapper {

    private AuthorizedUserService authorizedUserService;

    public CustomRoleMapper(DefaultInstructorRoleRepository defaultInstructorRoleRepository, AuthorizedUserService authorizedUserService) {
      super(defaultInstructorRoleRepository);
      this.authorizedUserService = authorizedUserService;
   }

   @Override
   public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
       List<GrantedAuthority> remappedAuthorities = new ArrayList<>();
       remappedAuthorities.addAll(authorities);
       for (GrantedAuthority authority : authorities) {
           OidcUserAuthority userAuth = (OidcUserAuthority) authority;
           OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(userAuth.getAttributes());
           log.debug("LTI Claims: {}", userAuth.getAttributes());

           if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue("is_admin_mode"))) {
               log.debug("Admin mode");
               String userId = oidcTokenUtils.getUserLoginId();

               String rolesString = "NotAuthorized";

               AuthorizedUser user = authorizedUserService.findByActiveUsernameAndToolPermission(userId, AUTH_ADMIN_TOOL_PERMISSION);

               if (user != null) {
                   rolesString = LTIConstants.CANVAS_INSTRUCTOR_ROLE;
               }

               String[] userRoles = rolesString.split(",");

               String newAuthString = returnEquivalentAuthority(userRoles, getDefaultInstructorRoles());
               OidcUserAuthority newUserAuth = new OidcUserAuthority(newAuthString, userAuth.getIdToken(), userAuth.getUserInfo());

               remappedAuthorities.add(newUserAuth);
           } else {
               log.debug("User mode");
               // Use the legit roles
               return super.mapAuthorities(authorities);
           }
       }

       return remappedAuthorities;
   }

    @Override
    protected String returnEquivalentAuthority(String[] userRoles, List<String> instructorRoles) {
        List<String> userRoleList = Arrays.asList(userRoles);

        for (String instructorRole : instructorRoles) {
            if (userRoleList.contains(instructorRole)) {
                return LTIConstants.INSTRUCTOR_AUTHORITY;
            }
        }

        if (userRoleList.contains(LTIConstants.CANVAS_DESIGNER_ROLE)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
        }

        return LTIConstants.STUDENT_AUTHORITY;
    }
}
