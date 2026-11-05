package br.pucpr.cursolab.alunos

import org.springframework.data.jpa.repository.JpaRepository

interface AlunoRepository : JpaRepository<Aluno, Long> {
    fun findByEmailIgnoreCase(email: String): Aluno?
}
