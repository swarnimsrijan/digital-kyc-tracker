package in.zeta.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Digital KYC Tracker}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API Documentation")
                        .description("API documentation for the Digital KYC Tracker System")
                        .version("1.0.0")
                        .contact(new Contact()));
    }
}