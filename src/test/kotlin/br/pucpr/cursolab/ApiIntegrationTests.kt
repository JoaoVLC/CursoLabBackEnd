package br.pucpr.cursolab

import br.pucpr.cursolab.alunos.AlunoRepository
import br.pucpr.cursolab.categorias.CategoriaRepository
import br.pucpr.cursolab.cursos.CursoRepository
import br.pucpr.cursolab.matriculas.MatriculaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper

@SpringBootTest
class ApiIntegrationTests {
    @Autowired lateinit var context: WebApplicationContext
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var matriculas: MatriculaRepository
    @Autowired lateinit var cursos: CursoRepository
    @Autowired lateinit var alunos: AlunoRepository
    @Autowired lateinit var categorias: CategoriaRepository
    lateinit var mvc: MockMvc

    @BeforeEach
    fun prepararBanco() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build()
        matriculas.deleteAllInBatch()
        cursos.deleteAllInBatch()
        alunos.deleteAllInBatch()
        categorias.deleteAllInBatch()
    }

    private fun criar(path: String, body: String): Long {
        val response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return mapper.readTree(response).get("id").asLong()
    }

    private fun categoria(nome: String = "Programacao") = criar("/categorias", """{"nome":"$nome"}""")
    private fun aluno(email: String = "ana@example.com") =
        criar("/alunos", """{"nome":"Ana Exemplo","email":"$email"}""")
    private fun curso(categoriaId: Long, titulo: String = "Kotlin", horas: Int = 20) =
        criar("/cursos", """{"titulo":"$titulo","descricao":"Curso de teste","cargaHoraria":$horas,"categoriaId":$categoriaId}""")

    @Test
    fun `CRUD completo de curso e troca de categoria`() {
        val cat1 = categoria()
        val cat2 = categoria("Dados")
        val id = curso(cat1)
        mvc.perform(get("/cursos/$id")).andExpect(status().isOk)
            .andExpect(jsonPath("$.categoriaId").value(cat1)).andExpect(jsonPath("$.categoriaNome").value("Programacao"))
        mvc.perform(put("/cursos/$id").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"SQL essencial","cargaHoraria":30,"categoriaId":$cat2}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.categoriaNome").value("Dados"))
        mvc.perform(get("/cursos")).andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
        mvc.perform(delete("/cursos/$id")).andExpect(status().isNoContent)
        mvc.perform(get("/cursos/$id")).andExpect(status().isNotFound)
        mvc.perform(delete("/cursos/$id")).andExpect(status().isNotFound)
    }

    @Test
    fun `CRUD de aluno normaliza email e impede duplicidade`() {
        val id = aluno("ANA@example.com")
        mvc.perform(get("/alunos/$id")).andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("ana@example.com"))
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"Outra pessoa","email":"ana@example.com"}"""))
            .andExpect(status().isConflict)
        mvc.perform(put("/alunos/$id").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"Ana Atualizada","email":"ana.nova@example.com"}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.nome").value("Ana Atualizada"))
        mvc.perform(get("/alunos")).andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
        mvc.perform(delete("/alunos/$id")).andExpect(status().isNoContent)
        mvc.perform(get("/alunos/$id")).andExpect(status().isNotFound)
    }

    @Test
    fun `filtro ordenacao e paginacao funcionam juntos`() {
        val cat = categoria()
        val outra = categoria("Dados")
        curso(cat, "Kotlin basico", 20)
        curso(cat, "Kotlin avancado", 40)
        curso(cat, "Java avancado", 50)
        curso(outra, "Kotlin e SQL", 60)
        mvc.perform(get("/cursos").param("titulo", "kotlin").param("categoriaId", "$cat")
            .param("minHoras", "25").param("ordenarPor", "cargaHoraria").param("direcao", "DESC"))
            .andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titulo").value("Kotlin avancado"))
        mvc.perform(get("/cursos").param("ordenarPor", "cargaHoraria").param("direcao", "DESC")
            .param("pagina", "1").param("tamanho", "2"))
            .andExpect(status().isOk).andExpect(jsonPath("$[0].cargaHoraria").value(40))
            .andExpect(jsonPath("$[1].cargaHoraria").value(20))
        mvc.perform(get("/cursos").param("ordenarPor", "campoInexistente")).andExpect(status().isBadRequest)
        mvc.perform(get("/cursos").param("direcao", "ERRADO")).andExpect(status().isBadRequest)
        mvc.perform(get("/cursos").param("tamanho", "0")).andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `matricula tem vinculos e protege integridade na exclusao`() {
        val alunoId = aluno()
        val cursoId = curso(categoria())
        criar("/cursos/$cursoId/matriculas", """{"alunoId":$alunoId}""")
        mvc.perform(post("/cursos/$cursoId/matriculas").contentType(MediaType.APPLICATION_JSON)
            .content("""{"alunoId":$alunoId}""")).andExpect(status().isConflict)
        mvc.perform(get("/cursos/$cursoId/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].alunoNome").value("Ana Exemplo"))
            .andExpect(jsonPath("$[0].criadaEm").exists())
        mvc.perform(get("/alunos/$alunoId/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].cursoTitulo").value("Kotlin"))
        mvc.perform(delete("/cursos/$cursoId")).andExpect(status().isConflict)
        mvc.perform(delete("/alunos/$alunoId")).andExpect(status().isConflict)
        mvc.perform(delete("/cursos/$cursoId/matriculas/$alunoId")).andExpect(status().isNoContent)
        mvc.perform(delete("/cursos/$cursoId/matriculas/$alunoId")).andExpect(status().isNotFound)
        mvc.perform(delete("/cursos/$cursoId")).andExpect(status().isNoContent)
        mvc.perform(delete("/alunos/$alunoId")).andExpect(status().isNoContent)
    }

    @Test
    fun `relacao permite varios alunos por curso e varios cursos por aluno`() {
        val cat = categoria()
        val a1 = aluno("um@example.com")
        val a2 = aluno("dois@example.com")
        val c1 = curso(cat, "Kotlin")
        val c2 = curso(cat, "Spring Boot")
        criar("/cursos/$c1/matriculas", """{"alunoId":$a1}""")
        criar("/cursos/$c1/matriculas", """{"alunoId":$a2}""")
        criar("/cursos/$c2/matriculas", """{"alunoId":$a1}""")
        mvc.perform(get("/cursos/$c1/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
        mvc.perform(get("/alunos/$a1/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `validacao rejeita campos vazios dados invalidos e JSON quebrado`() {
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":" ","email":"email-errado"}"""))
            .andExpect(status().isUnprocessableEntity).andExpect(jsonPath("$.errors.nome").exists())
        mvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"Teste","cargaHoraria":0,"categoriaId":1}"""))
            .andExpect(status().isUnprocessableEntity)
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("{quebrado"))
            .andExpect(status().isBadRequest)
        mvc.perform(get("/alunos/0")).andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `referencias inexistentes respondem 404 e nao criam matricula`() {
        val cursoId = curso(categoria())
        mvc.perform(post("/cursos/$cursoId/matriculas").contentType(MediaType.APPLICATION_JSON)
            .content("""{"alunoId":99999}""")).andExpect(status().isNotFound)
        mvc.perform(get("/alunos/99999/matriculas")).andExpect(status().isNotFound)
        mvc.perform(get("/cursos/99999/matriculas")).andExpect(status().isNotFound)
        mvc.perform(post("/cursos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"Teste","cargaHoraria":10,"categoriaId":99999}"""))
            .andExpect(status().isNotFound)
        mvc.perform(get("/cursos/$cursoId/matriculas")).andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `categorias rejeitam duplicidade e DTO evita colecoes recursivas`() {
        categoria("Programacao")
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"PROGRAMACAO"}""")).andExpect(status().isConflict)
        mvc.perform(get("/categorias")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].nome").value("Programacao"))
            .andExpect(jsonPath("$[0].cursos").doesNotExist())
        mvc.perform(get("/health")).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OK"))
    }
}
