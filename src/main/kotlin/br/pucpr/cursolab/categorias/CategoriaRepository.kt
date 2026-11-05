package br.pucpr.cursolab.categorias

import org.springframework.data.jpa.repository.JpaRepository

interface CategoriaRepository : JpaRepository<Categoria, Long> {
    fun existsByNomeIgnoreCase(nome: String): Boolean
}
