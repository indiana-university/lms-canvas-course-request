package edu.iu.uits.lms.siterequest.services.swagger;

import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MockitoBean(types = {AuthorizedUserService.class, BufferingApplicationStartup.class, ClientRegistrationRepository.class,
        DefaultInstructorRoleRepository.class, LmsDefaultGrantedAuthoritiesMapper.class, OAuth2AuthorizedClientService.class})
public @interface SharedSwaggerMocks {

}