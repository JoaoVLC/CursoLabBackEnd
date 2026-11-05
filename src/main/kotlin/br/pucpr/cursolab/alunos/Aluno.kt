package br.pucpr.cursolab.alunos

import br.pucpr.cursolab.matriculas.Matricula
import jakarta.persistence.*

@Entity
@Table(name = "alunos")
class Aluno(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var nome: String,

    @Column(nullable = false, unique = true, length = 254)
    var email: String,

    @OneToMany(mappedBy = "aluno")
    var matriculas: MutableList<Matricula> = mutableListOf()
)
