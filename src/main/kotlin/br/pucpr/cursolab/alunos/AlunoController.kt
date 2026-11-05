package br.pucpr.cursolab.alunos

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/alunos")
@Tag(name = "3. Alunos", description = "CRUD completo de alunos")
@Validated
class AlunoController(private val service: AlunoService) {
    @GetMapping
    fun listar(@RequestParam(defaultValue = "0") @Min(0) pagina: Int,
               @RequestParam(defaultValue = "50") @Min(1) @Max(100) tamanho: Int) = service.listar(pagina, tamanho)

    @GetMapping("/{id}")
    fun buscar(@PathVariable @Positive id: Long) = service.buscar(id)

    @PostMapping
    fun criar(@Valid @RequestBody request: AlunoRequest): ResponseEntity<AlunoResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request))

    @PutMapping("/{id}")
    fun atualizar(@PathVariable @Positive id: Long, @Valid @RequestBody request: AlunoRequest) =
        service.atualizar(id, request)

    @DeleteMapping("/{id}")
    fun excluir(@PathVariable @Positive id: Long): ResponseEntity<Void> {
        service.excluir(id)
        return ResponseEntity.noContent().build()
    }
}
