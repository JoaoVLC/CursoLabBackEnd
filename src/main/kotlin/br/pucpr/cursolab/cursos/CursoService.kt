package br.pucpr.cursolab.cursos

import br.pucpr.cursolab.categorias.CategoriaRepository
import br.pucpr.cursolab.common.BadRequestException
import br.pucpr.cursolab.common.ConflictException
import br.pucpr.cursolab.common.NotFoundException
import br.pucpr.cursolab.matriculas.MatriculaRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CursoService(
    private val repository: CursoRepository,
    private val categorias: CategoriaRepository,
    private val matriculas: MatriculaRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun buscarEntidade(id: Long): Curso = repository.findByIdOrNull(id) ?: run {
        log.warn("Curso não encontrado: id={}", id)
        throw NotFoundException("Curso $id não encontrado.")
    }

    private fun categoria(id: Long) = categorias.findByIdOrNull(id)
        ?: throw NotFoundException("Categoria $id não encontrada.")

    @Transactional(readOnly = true)
    fun listar(titulo: String?, categoriaId: Long?, minHoras: Int?, ordenarPor: String,
               direcao: String, pagina: Int, tamanho: Int): List<CursoResponse> {
        if (ordenarPor !in setOf("titulo", "cargaHoraria", "id"))
            throw BadRequestException("ordenarPor deve ser titulo, cargaHoraria ou id.")
        val dir = when (direcao.uppercase()) {
            "ASC" -> Sort.Direction.ASC
            "DESC" -> Sort.Direction.DESC
            else -> throw BadRequestException("direcao deve ser ASC ou DESC.")
        }
        val sort = Sort.by(dir, ordenarPor).and(Sort.by("id"))
        return repository.pesquisar(titulo?.trim()?.takeIf { it.isNotEmpty() }, categoriaId,
            minHoras, PageRequest.of(pagina, tamanho, sort)).map(::CursoResponse)
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long) = CursoResponse(buscarEntidade(id))

    @Transactional
    fun criar(request: CursoRequest): CursoResponse {
        val curso = repository.saveAndFlush(Curso(titulo = request.titulo.trim(),
            descricao = request.descricao.trim(), cargaHoraria = request.cargaHoraria,
            categoria = categoria(request.categoriaId)))
        log.info("Curso criado: id={}, categoriaId={}", curso.id, curso.categoria.id)
        return CursoResponse(curso)
    }

    @Transactional
    fun atualizar(id: Long, request: CursoRequest): CursoResponse {
        val curso = buscarEntidade(id)
        curso.categoria = categoria(request.categoriaId)
        curso.titulo = request.titulo.trim()
        curso.descricao = request.descricao.trim()
        curso.cargaHoraria = request.cargaHoraria
        repository.saveAndFlush(curso)
        log.info("Curso atualizado: id={}", id)
        return CursoResponse(curso)
    }

    @Transactional
    fun excluir(id: Long) {
        val curso = buscarEntidade(id)
        if (matriculas.existsByCursoId(id)) {
            log.warn("Exclusão de curso bloqueada: id={}, motivo=possui_matriculas", id)
            throw ConflictException("Remova as matrículas antes de excluir o curso.")
        }
        repository.delete(curso)
        repository.flush()
        log.info("Curso excluído: id={}", id)
    }
}
