package br.pucpr.cursolab.categorias

import br.pucpr.cursolab.config.BEARER_AUTH
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categorias")
@Tag(name = "1. Categorias", description = "Categorias que agrupam os cursos")
class CategoriaController(private val service: CategoriaService) {
    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    @SecurityRequirement(name = BEARER_AUTH)
    fun criar(@Valid @RequestBody request: CategoriaRequest): ResponseEntity<CategoriaResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request))
}
