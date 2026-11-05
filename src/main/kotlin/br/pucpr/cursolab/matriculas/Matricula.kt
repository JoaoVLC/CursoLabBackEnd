package br.pucpr.cursolab.matriculas

import br.pucpr.cursolab.alunos.Aluno
import br.pucpr.cursolab.cursos.Curso
import jakarta.persistence.*
import java.time.Instant

// Aluno N:N Curso, usando uma entidade intermediária com data própria.
@Entity
@Table(name = "matriculas", uniqueConstraints = [UniqueConstraint(columnNames = ["aluno_id", "curso_id"])])
class Matricula(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    var aluno: Aluno,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    var curso: Curso,

    @Column(nullable = false, updatable = false)
    var criadaEm: Instant = Instant.now()
)
