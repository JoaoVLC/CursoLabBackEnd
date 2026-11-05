package br.pucpr.cursolab.matriculas

import jakarta.validation.constraints.Positive
import java.time.Instant

data class MatriculaRequest(@field:Positive val alunoId: Long)

data class MatriculaResponse(
    val id: Long,
    val alunoId: Long,
    val alunoNome: String,
    val cursoId: Long,
    val cursoTitulo: String,
    val criadaEm: Instant
) {
    constructor(matricula: Matricula) : this(matricula.id!!, matricula.aluno.id!!,
        matricula.aluno.nome, matricula.curso.id!!, matricula.curso.titulo, matricula.criadaEm)
}
