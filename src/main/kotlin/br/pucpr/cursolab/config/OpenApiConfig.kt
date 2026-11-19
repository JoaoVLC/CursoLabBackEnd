package br.pucpr.cursolab.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val BEARER_AUTH = "bearerAuth"

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(Info()
            .title("CursoLab - API de cursos e matrículas")
            .version("1.0.0")
            .description("Consulte o catálogo de cursos e categorias ou faça login para operar a API. " +
                "Copie o accessToken de POST /auth/login e use o botão Authorize nas operações com cadeado."))
        .components(Components().addSecuritySchemes(BEARER_AUTH, SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
}
