package br.pucpr.cursolab.cursos

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cursos")
@Tag(name = "2. Cursos", description = "CRUD completo e busca com filtros e ordenação")
@Validated
class CursoController(private val service: CursoService) {
    @GetMapping
    @Operation(summary = "Lista cursos", description = "Combina filtros; ordenarPor aceita titulo, cargaHoraria ou id. Paginação começa em zero.")
    fun listar(
        @RequestParam(required = false) titulo: String?,
        @RequestParam(required = false) @Positive categoriaId: Long?,
        @RequestParam(required = false) @Min(1) @Max(1000) minHoras: Int?,
        @RequestParam(defaultValue = "titulo") ordenarPor: String,
        @RequestParam(defaultValue = "ASC") direcao: String,
        @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) tamanho: Int
    ) = service.listar(titulo, categoriaId, minHoras, ordenarPor, direcao, pagina, tamanho)

    @GetMapping("/{id}")
    fun buscar(@PathVariable @Positive id: Long) = service.buscar(id)

    @PostMapping
    fun criar(@Valid @RequestBody request: CursoRequest): ResponseEntity<CursoResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request))

    @PutMapping("/{id}")
    fun atualizar(@PathVariable @Positive id: Long, @Valid @RequestBody request: CursoRequest) =
        service.atualizar(id, request)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable @Positive id: Long): ResponseEntity<Void> {
        service.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
