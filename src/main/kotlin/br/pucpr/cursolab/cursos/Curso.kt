package br.pucpr.cursolab.cursos

import br.pucpr.cursolab.categorias.Categoria
import br.pucpr.cursolab.matriculas.Matricula
import jakarta.persistence.*

@Entity
@Table(name = "cursos")
class Curso(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 120)
    var titulo: String,

    @Column(nullable = false, length = 2000)
    var descricao: String = "",

    @Column(nullable = false)
    var cargaHoraria: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    var categoria: Categoria,

    @OneToMany(mappedBy = "curso")
    var matriculas: MutableList<Matricula> = mutableListOf()
)
