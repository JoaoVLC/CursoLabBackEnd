package br.pucpr.cursolab.alunos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AlunoRequest(
    @field:NotBlank(message = "Informe o nome.")
    @field:Size(max = 120)
    val nome: String,

    @field:NotBlank(message = "Informe o e-mail.")
    @field:Email(message = "Informe um e-mail válido.")
    @field:Size(max = 254)
    val email: String
)

data class AlunoResponse(val id: Long, val nome: String, val email: String) {
    constructor(aluno: Aluno) : this(aluno.id!!, aluno.nome, aluno.email)
}
