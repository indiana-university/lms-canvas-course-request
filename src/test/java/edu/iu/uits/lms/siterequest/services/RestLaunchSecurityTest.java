package edu.iu.uits.lms.siterequest.services;

import edu.iu.uits.lms.canvas.config.CanvasClientTestConfig;
import edu.iu.uits.lms.lti.config.LtiClientTestConfig;
import edu.iu.uits.lms.lti.config.TestUtils;
import edu.iu.uits.lms.siterequest.config.ToolConfig;
import edu.iu.uits.lms.siterequest.controller.SiteRequestPropertiesController;
import edu.iu.uits.lms.siterequest.repository.SiteRequestPropertyRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SiteRequestPropertiesController.class, properties = {"oauth.tokenprovider.url=http://foo"})
@Import({ToolConfig.class, CanvasClientTestConfig.class, LtiClientTestConfig.class})
public class RestLaunchSecurityTest {
   @Autowired
   private MockMvc mvc;

   @MockBean
   private SiteRequestPropertyRepository siteRequestPropertyRepository = null;

   @Test
   public void restNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/props/all")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
   }

   @Test
   public void restAuthnLaunchWithLmsScope() throws Exception {
      Jwt jwt = createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_lms:rest", "ROLE_LMS_REST_ADMINS");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/rest/props/all")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(token)))
            .andExpect(status().isOk());
   }

   @Test
   public void restAuthnLaunchWithWrongScope() throws Exception {
      Jwt jwt = createJwtToken("asdf");

      Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
      JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/rest/props/all")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON)
            .with(authentication(token)))
            .andExpect(status().isForbidden());
   }

   public static Jwt createJwtToken(String username) {
      Jwt jwt = Jwt.withTokenValue("fake-token")
            .header("typ", "JWT")
            .header("alg", SignatureAlgorithm.RS256.getValue())
            .claim("user_name", username)
            .claim("client_id", username)
            .notBefore(Instant.now())
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .subject(username)
            .build();

      return jwt;
   }
}
