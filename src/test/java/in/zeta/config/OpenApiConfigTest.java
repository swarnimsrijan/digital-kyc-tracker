package in.zeta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigTest {

    @Test
    void customOpenAPIBean_isPresentAndNotNull() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(OpenApiConfig.class);
            ctx.refresh();

            OpenAPI openAPI = ctx.getBean(OpenAPI.class);
            assertNotNull(openAPI);

            Info info = openAPI.getInfo();
            assertNotNull(info);
            assertTrue(info.getTitle().contains("API Documentation"));
            assertEquals("API documentation for the Digital KYC Tracker System", info.getDescription());
            assertEquals("1.0.0", info.getVersion());
        }
    }

    @Test
    void customOpenAPIBean_isSingleton() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(OpenApiConfig.class);
            ctx.refresh();

            OpenAPI first = ctx.getBean(OpenAPI.class);
            OpenAPI second = ctx.getBean(OpenAPI.class);
            assertSame(first, second);
        }
    }
}