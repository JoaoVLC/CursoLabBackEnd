package br.pucpr.cursolab.categorias

import br.pucpr.cursolab.common.ConflictException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoriaService(private val repository: CategoriaRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun listar() = repository.findAll(Sort.by("nome")).map(::CategoriaResponse)

    @Transactional
    fun criar(request: CategoriaRequest): CategoriaResponse {
        val nome = request.nome.trim()
        if (repository.existsByNomeIgnoreCase(nome)) throw ConflictException("Categoria já cadastrada.")
        val categoria = repository.saveAndFlush(Categoria(nome = nome))
        log.info("Categoria criada: id={}", categoria.id)
        return CategoriaResponse(categoria)
    }
}
