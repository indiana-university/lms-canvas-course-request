package edu.iu.uits.lms.siterequest.services.swagger;/*-
 * #%L
 * siterequest
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
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

import edu.iu.uits.lms.iuonly.config.IuCustomRestConfiguration;
import edu.iu.uits.lms.lti.config.LtiClientTestConfig;
import edu.iu.uits.lms.lti.config.LtiRestConfiguration;
import edu.iu.uits.lms.lti.swagger.SwaggerTestingBean;
import edu.iu.uits.lms.siterequest.config.SecurityConfig;
import edu.iu.uits.lms.siterequest.config.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOM_GROUP_CODE_PATH;

@Import({
        SecurityConfig.class,
        SwaggerConfig.class,
        edu.iu.uits.lms.lti.config.SwaggerConfig.class,
        LtiRestConfiguration.class,
        edu.iu.uits.lms.iuonly.config.SwaggerConfig.class,
        IuCustomRestConfiguration.class,
        LtiClientTestConfig.class
})
public class SwaggerTestConfig {
    @Bean
    public SwaggerTestingBean swaggerTestingBean() {
        SwaggerTestingBean stb = new SwaggerTestingBean();

        List<String> expandedList = new ArrayList<>();
        expandedList.add(IUCUSTOM_GROUP_CODE_PATH);

        stb.setEmbeddedSwaggerToolPaths(expandedList);
        return stb;
    }
}