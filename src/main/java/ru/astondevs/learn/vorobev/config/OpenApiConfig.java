package ru.astondevs.learn.vorobev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addJavaTypeToIgnore(org.springframework.hateoas.Link.class);
        SpringDocUtils.getConfig().addJavaTypeToIgnore(org.springframework.hateoas.Links.class);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Learn App API")
                        .version("1.0")
                        .description("Сервис для управления пользователями с PostgreSQL и Kafka")
                        .contact(new Contact()
                                .name("Vorobev Vladimir")
                                .email("worobev2001@gmail.com")));
    }
}
