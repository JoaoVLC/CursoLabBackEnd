package br.pucpr.cursolab.cursos

import jakarta.validation.constraints.*

data class CursoRequest(
    @field:NotBlank(message = "Informe o título.")
    @field:Size(max = 120)
    val titulo: String,

    @field:Size(max = 2000)
    val descricao: String = "",

    @field:Min(1, message = "A carga horária deve ser maior que zero.")
    @field:Max(1000)
    val cargaHoraria: Int,

    @field:Positive(message = "Informe um ID de categoria positivo.")
    val categoriaId: Long
)

data class CursoResponse(
    val id: Long,
    val titulo: String,
    val descricao: String,
    val cargaHoraria: Int,
    val categoriaId: Long,
    val categoriaNome: String
) {
    constructor(curso: Curso) : this(curso.id!!, curso.titulo, curso.descricao,
        curso.cargaHoraria, curso.categoria.id!!, curso.categoria.nome)
}
