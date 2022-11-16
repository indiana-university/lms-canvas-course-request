package edu.iu.uits.lms.siterequest.services.swagger;

import edu.iu.uits.lms.lti.swagger.AbstractSwaggerEmbeddedToolTest;
import edu.iu.uits.lms.siterequest.WebApplication;
import edu.iu.uits.lms.siterequest.config.SecurityConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;

@SpringBootTest(classes = {WebApplication.class, SecurityConfig.class})
@ActiveProfiles({IUCUSTOMREST_PROFILE})
public class SwaggerEmbeddedToolTest extends AbstractSwaggerEmbeddedToolTest {

   @Override
   protected List<String> getEmbeddedSwaggerToolPaths() {
      return SwaggerTestUtil.getEmbeddedSwaggerToolPaths(super.getEmbeddedSwaggerToolPaths());
   }
}
