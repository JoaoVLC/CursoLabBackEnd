package br.pucpr.cursolab.matriculas

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "4. Matrículas", description = "Associações entre alunos e cursos")
@Validated
class MatriculaController(private val service: MatriculaService) {
    @PostMapping("/cursos/{cursoId}/matriculas")
    fun matricular(@PathVariable @Positive cursoId: Long,
                   @Valid @RequestBody request: MatriculaRequest): ResponseEntity<MatriculaResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.matricular(cursoId, request.alunoId))

    @GetMapping("/cursos/{cursoId}/matriculas")
    fun porCurso(@PathVariable @Positive cursoId: Long,
                 @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
                 @RequestParam(defaultValue = "50") @Min(1) @Max(100) tamanho: Int) =
        service.porCurso(cursoId, pagina, tamanho)

    @GetMapping("/alunos/{alunoId}/matriculas")
    fun porAluno(@PathVariable @Positive alunoId: Long,
                 @RequestParam(defaultValue = "0") @Min(0) pagina: Int,
                 @RequestParam(defaultValue = "50") @Min(1) @Max(100) tamanho: Int) =
        service.porAluno(alunoId, pagina, tamanho)

    @DeleteMapping("/cursos/{cursoId}/matriculas/{alunoId}")
    fun remover(@PathVariable @Positive cursoId: Long, @PathVariable @Positive alunoId: Long): ResponseEntity<Void> {
        service.remover(cursoId, alunoId)
        return ResponseEntity.noContent().build()
    }
}
