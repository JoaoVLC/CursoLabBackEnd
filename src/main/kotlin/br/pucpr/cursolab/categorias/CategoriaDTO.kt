package br.pucpr.cursolab.categorias

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoriaRequest(
    @field:NotBlank(message = "Informe o nome da categoria.")
    @field:Size(max = 80, message = "Use no máximo 80 caracteres.")
    val nome: String
)

data class CategoriaResponse(val id: Long, val nome: String) {
    constructor(categoria: Categoria) : this(categoria.id!!, categoria.nome)
}
