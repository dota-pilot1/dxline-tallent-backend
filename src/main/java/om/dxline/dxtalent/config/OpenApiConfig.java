package om.dxline.dxtalent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Talent Admin API")
                        .description("Talent Admin Server API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("DXLine")
                                .email("support@dxline.com")));
    }
}
