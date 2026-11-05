package br.pucpr.cursolab.config

import br.pucpr.cursolab.alunos.Aluno
import br.pucpr.cursolab.alunos.AlunoRepository
import br.pucpr.cursolab.categorias.Categoria
import br.pucpr.cursolab.categorias.CategoriaRepository
import br.pucpr.cursolab.cursos.Curso
import br.pucpr.cursolab.cursos.CursoRepository
import br.pucpr.cursolab.matriculas.Matricula
import br.pucpr.cursolab.matriculas.MatriculaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(name = ["app.seed-enabled"], havingValue = "true", matchIfMissing = true)
class SeedData(private val categorias: CategoriaRepository, private val cursos: CursoRepository,
               private val alunos: AlunoRepository, private val matriculas: MatriculaRepository) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        // A carga ocorre apenas em banco vazio e não substitui registros do usuário.
        if (categorias.count() > 0 || cursos.count() > 0 || alunos.count() > 0 || matriculas.count() > 0) return
        val programacao = categorias.save(Categoria(nome = "Programação"))
        val dados = categorias.save(Categoria(nome = "Banco de Dados"))
        val kotlin = cursos.save(Curso(titulo = "Kotlin para iniciantes", descricao = "Fundamentos da linguagem Kotlin.",
            cargaHoraria = 20, categoria = programacao))
        cursos.save(Curso(titulo = "API REST com Spring Boot", descricao = "Endpoints, validação e persistência.",
            cargaHoraria = 40, categoria = programacao))
        cursos.save(Curso(titulo = "SQL essencial", descricao = "Consultas e modelagem relacional.",
            cargaHoraria = 30, categoria = dados))
        val ana = alunos.save(Aluno(nome = "Ana Exemplo", email = "ana@example.com"))
        alunos.save(Aluno(nome = "Bruno Exemplo", email = "bruno@example.com"))
        matriculas.save(Matricula(aluno = ana, curso = kotlin))
        log.info("Dados fictícios criados: 2 categorias, 3 cursos, 2 alunos e 1 matrícula.")
    }
}
