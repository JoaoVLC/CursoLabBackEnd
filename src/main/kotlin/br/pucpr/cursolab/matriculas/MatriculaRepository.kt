package br.pucpr.cursolab.matriculas

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface MatriculaRepository : JpaRepository<Matricula, Long> {
    fun existsByAlunoIdAndCursoId(alunoId: Long, cursoId: Long): Boolean
    fun existsByAlunoId(alunoId: Long): Boolean
    fun existsByCursoId(cursoId: Long): Boolean
    fun findByAlunoIdAndCursoId(alunoId: Long, cursoId: Long): Matricula?

    @EntityGraph(attributePaths = ["aluno", "curso"])
    fun findAllByCursoId(cursoId: Long, pageable: Pageable): List<Matricula>

    @EntityGraph(attributePaths = ["aluno", "curso"])
    fun findAllByAlunoId(alunoId: Long, pageable: Pageable): List<Matricula>
}
