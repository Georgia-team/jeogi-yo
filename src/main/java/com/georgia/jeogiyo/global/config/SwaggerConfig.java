package com.georgia.jeogiyo.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Jeogi-yo API", version = "v1")) // Swagger 문서 최상단에 표시되는 제목, 버전 todo 추후 상의 후 변경 가능!!
@SecurityScheme( // 인증 방식
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() { // 모든 OpenAPI에 인증 방식 적용
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
