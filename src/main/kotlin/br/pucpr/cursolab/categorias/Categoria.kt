package br.pucpr.cursolab.categorias

import br.pucpr.cursolab.cursos.Curso
import jakarta.persistence.*

@Entity
@Table(name = "categorias")
class Categoria(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 80)
    var nome: String,

    // Uma categoria pode ter vários cursos. O FK está em Curso.categoria.
    @OneToMany(mappedBy = "categoria")
    var cursos: MutableList<Curso> = mutableListOf()
)
