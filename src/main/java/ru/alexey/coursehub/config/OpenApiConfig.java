package ru.alexey.coursehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI courseHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CourseHub API")
                        .version("1.0.0")
                        .description(
                                "Learning platform API: courses, modules, lessons, assignments, quizzes and reviews")
                        .contact(new Contact().name("CourseHub")));
    }
}
