package edu.iu.uits.lms.siterequest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class ApplicationConfig implements WebMvcConfigurer {

   @Autowired
   private ToolConfig toolConfig = null;

   public ApplicationConfig() {
      log.debug("ApplicationConfig()");
   }

   @Override
   // used to read in various directories to add resources for the templates to use
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/app/css/**").addResourceLocations("classpath:/static/css/");
      registry.addResourceHandler("/app/js/**").addResourceLocations("classpath:/static/js/");
      registry.addResourceHandler("/app/webjars/**").addResourceLocations("/webjars/").resourceChain(true);
      registry.addResourceHandler("/app/jsreact/**").addResourceLocations("classpath:/META-INF/resources/jsreact/").resourceChain(true);
      registry.addResourceHandler("/app/jsrivet/**").addResourceLocations("classpath:/META-INF/resources/jsrivet/").resourceChain(true);
   }

   @Bean
   public ResourceBundleMessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("siterequest");
      return messageSource;
   }
}
