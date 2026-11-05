package br.pucpr.cursolab.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(Info()
        .title("CursoLab - API de cursos e matrículas")
        .version("0.1.0-parcial")
        .description("Parcial de Backend: 16 endpoints de negócio implementados. " +
            "CRUDs de Curso e Aluno, relacionamentos, filtros, logs e exceções. " +
            "Autenticação JWT ainda pendente para a entrega final. Demonstração local com dados fictícios."))
}
