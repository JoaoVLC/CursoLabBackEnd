package br.pucpr.cursolab.matriculas

import br.pucpr.cursolab.alunos.AlunoRepository
import br.pucpr.cursolab.common.ConflictException
import br.pucpr.cursolab.common.NotFoundException
import br.pucpr.cursolab.cursos.CursoRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatriculaService(
    private val repository: MatriculaRepository,
    private val alunos: AlunoRepository,
    private val cursos: CursoRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun matricular(cursoId: Long, alunoId: Long): MatriculaResponse {
        val curso = cursos.findByIdOrNull(cursoId) ?: throw NotFoundException("Curso não encontrado.")
        val aluno = alunos.findByIdOrNull(alunoId) ?: throw NotFoundException("Aluno não encontrado.")
        if (repository.existsByAlunoIdAndCursoId(alunoId, cursoId))
            throw ConflictException("O aluno já está matriculado neste curso.")
        val matricula = repository.saveAndFlush(Matricula(aluno = aluno, curso = curso))
        log.info("Matrícula criada: id={}, alunoId={}, cursoId={}", matricula.id, alunoId, cursoId)
        return MatriculaResponse(matricula)
    }

    @Transactional(readOnly = true)
    fun porCurso(cursoId: Long, pagina: Int, tamanho: Int): List<MatriculaResponse> {
        if (!cursos.existsById(cursoId)) throw NotFoundException("Curso não encontrado.")
        return repository.findAllByCursoId(cursoId, PageRequest.of(pagina, tamanho, Sort.by("id")))
            .map(::MatriculaResponse)
    }

    @Transactional(readOnly = true)
    fun porAluno(alunoId: Long, pagina: Int, tamanho: Int): List<MatriculaResponse> {
        if (!alunos.existsById(alunoId)) throw NotFoundException("Aluno não encontrado.")
        return repository.findAllByAlunoId(alunoId, PageRequest.of(pagina, tamanho, Sort.by("id")))
            .map(::MatriculaResponse)
    }

    @Transactional
    fun remover(cursoId: Long, alunoId: Long) {
        val matricula = repository.findByAlunoIdAndCursoId(alunoId, cursoId)
            ?: throw NotFoundException("Matrícula não encontrada.")
        repository.delete(matricula)
        repository.flush()
        log.info("Matrícula removida: alunoId={}, cursoId={}", alunoId, cursoId)
    }
}
