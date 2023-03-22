package edu.iu.uits.lms.siterequest.services.swagger;

import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class SwaggerNotifierTestConfig {
    @MockBean
    private BufferingApplicationStartup bufferingApplicationStartup;
}
