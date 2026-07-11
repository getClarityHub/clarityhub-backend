package com.clarityhub.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clarityhub.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "prod"})
@Import(TestcontainersConfiguration.class)
class OpenApiDisabledTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ApplicationContext context;

    @Test
    void apiDocsEndpointReturns404InProdProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    /**
     * Direct check of the cause, not the symptom. If the springdoc OpenAPI bean is absent from the
     * context, the feature is truly off — a 404 alone could be caused by unrelated routing
     * failures.
     */
    @Test
    void springdocOpenApiBeanIsNotRegisteredInProdProfile() {
        String[] openApiBeans = context.getBeanNamesForType(io.swagger.v3.oas.models.OpenAPI.class);
        assertThat(openApiBeans)
                .as("springdoc OpenAPI bean must not be registered when api-docs.enabled=false")
                .isEmpty();
    }
}
