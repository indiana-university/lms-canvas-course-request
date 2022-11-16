package edu.iu.uits.lms.siterequest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "siterequest")
@Getter
@Setter
public class ToolConfig {

   private String version;
   private String env;
   private String courseTemplateQueueName;
   private String templateHostingUrl;
}
