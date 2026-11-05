package br.pucpr.cursolab.alunos

import br.pucpr.cursolab.common.ConflictException
import br.pucpr.cursolab.common.NotFoundException
import br.pucpr.cursolab.matriculas.MatriculaRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
class AlunoService(private val repository: AlunoRepository, private val matriculas: MatriculaRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun buscarEntidade(id: Long): Aluno = repository.findByIdOrNull(id) ?: run {
        log.warn("Aluno não encontrado: id={}", id)
        throw NotFoundException("Aluno $id não encontrado.")
    }

    private fun validarEmail(email: String, id: Long? = null) {
        val existente = repository.findByEmailIgnoreCase(email)
        if (existente != null && existente.id != id) {
            log.warn("Cadastro de aluno bloqueado: motivo=email_duplicado")
            throw ConflictException("Já existe um aluno com este e-mail.")
        }
    }

    @Transactional(readOnly = true)
    fun listar(pagina: Int, tamanho: Int) = repository
        .findAll(PageRequest.of(pagina, tamanho, Sort.by("nome", "id"))).content.map(::AlunoResponse)

    @Transactional(readOnly = true)
    fun buscar(id: Long) = AlunoResponse(buscarEntidade(id))

    @Transactional
    fun criar(request: AlunoRequest): AlunoResponse {
        val email = request.email.trim().lowercase(Locale.ROOT)
        validarEmail(email)
        val aluno = repository.saveAndFlush(Aluno(nome = request.nome.trim(), email = email))
        log.info("Aluno criado: id={}", aluno.id)
        return AlunoResponse(aluno)
    }

    @Transactional
    fun atualizar(id: Long, request: AlunoRequest): AlunoResponse {
        val aluno = buscarEntidade(id)
        val email = request.email.trim().lowercase(Locale.ROOT)
        validarEmail(email, id)
        aluno.nome = request.nome.trim()
        aluno.email = email
        repository.saveAndFlush(aluno)
        log.info("Aluno atualizado: id={}", id)
        return AlunoResponse(aluno)
    }

    @Transactional
    fun excluir(id: Long) {
        val aluno = buscarEntidade(id)
        if (matriculas.existsByAlunoId(id)) {
            log.warn("Exclusão de aluno bloqueada: id={}, motivo=possui_matriculas", id)
            throw ConflictException("Remova as matrículas antes de excluir o aluno.")
        }
        repository.delete(aluno)
        repository.flush()
        log.info("Aluno excluído: id={}", id)
    }
}
