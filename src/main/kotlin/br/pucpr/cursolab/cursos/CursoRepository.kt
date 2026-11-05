package br.pucpr.cursolab.cursos

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CursoRepository : JpaRepository<Curso, Long> {
    @Query("""
        select c from Curso c join fetch c.categoria
        where (:titulo is null or lower(c.titulo) like lower(concat('%', :titulo, '%')))
        and (:categoriaId is null or c.categoria.id = :categoriaId)
        and (:minHoras is null or c.cargaHoraria >= :minHoras)
    """)
    fun pesquisar(
        @Param("titulo") titulo: String?,
        @Param("categoriaId") categoriaId: Long?,
        @Param("minHoras") minHoras: Int?,
        pageable: Pageable
    ): List<Curso>
}
