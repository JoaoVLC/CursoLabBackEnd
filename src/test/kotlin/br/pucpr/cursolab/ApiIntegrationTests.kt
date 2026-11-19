package br.pucpr.cursolab

import br.pucpr.cursolab.alunos.AlunoRepository
import br.pucpr.cursolab.auth.EMISSOR_JWT
import br.pucpr.cursolab.auth.ESCOPO_OPERADOR
import br.pucpr.cursolab.auth.OperadorRepository
import br.pucpr.cursolab.categorias.CategoriaRepository
import br.pucpr.cursolab.cursos.CursoRepository
import br.pucpr.cursolab.matriculas.MatriculaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private const val USUARIO_TESTE = "operador-teste"
private const val SENHA_TESTE = "senha-da-suite-local"

@SpringBootTest(properties = ["app.auth.usuario=$USUARIO_TESTE", "app.auth.senha=$SENHA_TESTE"])
class ApiIntegrationTests {
    @Autowired lateinit var context: WebApplicationContext
    @Autowired lateinit var mapper: ObjectMapper
    @Autowired lateinit var matriculas: MatriculaRepository
    @Autowired lateinit var cursos: CursoRepository
    @Autowired lateinit var alunos: AlunoRepository
    @Autowired lateinit var categorias: CategoriaRepository
    @Autowired lateinit var operadores: OperadorRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder
    @Autowired lateinit var jwtEncoder: JwtEncoder
    lateinit var mvc: MockMvc
    private lateinit var token: String

    @BeforeEach
    fun prepararBanco() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
        matriculas.deleteAllInBatch()
        cursos.deleteAllInBatch()
        alunos.deleteAllInBatch()
        categorias.deleteAllInBatch()
        val resposta = login(USUARIO_TESTE, SENHA_TESTE)
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        token = mapper.readTree(resposta).get("accessToken").asText()
    }

    private fun autenticada(request: MockHttpServletRequestBuilder) =
        mvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer $token"))

    private fun login(usuario: String, senha: String) =
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(mapOf("usuario" to usuario, "senha" to senha))))

    private fun tokenDeTeste(escopo: String, emitidoEm: Instant, expiraEm: Instant): String {
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val claims = JwtClaimsSet.builder()
            .issuer(EMISSOR_JWT)
            .subject(USUARIO_TESTE)
            .issuedAt(emitidoEm)
            .expiresAt(expiraEm)
            .claim("scope", escopo)
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    private fun criar(path: String, body: String): Long {
        val response = autenticada(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
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
        autenticada(get("/cursos/$id")).andExpect(status().isOk)
            .andExpect(jsonPath("$.categoriaId").value(cat1)).andExpect(jsonPath("$.categoriaNome").value("Programacao"))
        autenticada(put("/cursos/$id").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"SQL essencial","cargaHoraria":30,"categoriaId":$cat2}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.categoriaNome").value("Dados"))
        autenticada(get("/cursos")).andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
        autenticada(delete("/cursos/$id")).andExpect(status().isNoContent)
        autenticada(get("/cursos/$id")).andExpect(status().isNotFound)
        autenticada(delete("/cursos/$id")).andExpect(status().isNotFound)
    }

    @Test
    fun `CRUD de aluno normaliza email e impede duplicidade`() {
        val id = aluno("ANA@example.com")
        autenticada(get("/alunos/$id")).andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("ana@example.com"))
        autenticada(post("/alunos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"Outra pessoa","email":"ana@example.com"}"""))
            .andExpect(status().isConflict)
        autenticada(put("/alunos/$id").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"Ana Atualizada","email":"ana.nova@example.com"}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.nome").value("Ana Atualizada"))
        autenticada(get("/alunos")).andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
        autenticada(delete("/alunos/$id")).andExpect(status().isNoContent)
        autenticada(get("/alunos/$id")).andExpect(status().isNotFound)
    }

    @Test
    fun `filtro ordenacao e paginacao funcionam juntos`() {
        val cat = categoria()
        val outra = categoria("Dados")
        curso(cat, "Kotlin basico", 20)
        curso(cat, "Kotlin avancado", 40)
        curso(cat, "Java avancado", 50)
        curso(outra, "Kotlin e SQL", 60)
        autenticada(get("/cursos").param("titulo", "kotlin").param("categoriaId", "$cat")
            .param("minHoras", "25").param("ordenarPor", "cargaHoraria").param("direcao", "DESC"))
            .andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].titulo").value("Kotlin avancado"))
        autenticada(get("/cursos").param("ordenarPor", "cargaHoraria").param("direcao", "DESC")
            .param("pagina", "1").param("tamanho", "2"))
            .andExpect(status().isOk).andExpect(jsonPath("$[0].cargaHoraria").value(40))
            .andExpect(jsonPath("$[1].cargaHoraria").value(20))
        autenticada(get("/cursos").param("ordenarPor", "campoInexistente")).andExpect(status().isBadRequest)
        autenticada(get("/cursos").param("direcao", "ERRADO")).andExpect(status().isBadRequest)
        autenticada(get("/cursos").param("tamanho", "0")).andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `matricula tem vinculos e protege integridade na exclusao`() {
        val alunoId = aluno()
        val cursoId = curso(categoria())
        criar("/cursos/$cursoId/matriculas", """{"alunoId":$alunoId}""")
        autenticada(post("/cursos/$cursoId/matriculas").contentType(MediaType.APPLICATION_JSON)
            .content("""{"alunoId":$alunoId}""")).andExpect(status().isConflict)
        autenticada(get("/cursos/$cursoId/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].alunoNome").value("Ana Exemplo"))
            .andExpect(jsonPath("$[0].criadaEm").exists())
        autenticada(get("/alunos/$alunoId/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].cursoTitulo").value("Kotlin"))
        autenticada(delete("/cursos/$cursoId")).andExpect(status().isConflict)
        autenticada(delete("/alunos/$alunoId")).andExpect(status().isConflict)
        autenticada(delete("/cursos/$cursoId/matriculas/$alunoId")).andExpect(status().isNoContent)
        autenticada(delete("/cursos/$cursoId/matriculas/$alunoId")).andExpect(status().isNotFound)
        autenticada(delete("/cursos/$cursoId")).andExpect(status().isNoContent)
        autenticada(delete("/alunos/$alunoId")).andExpect(status().isNoContent)
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
        autenticada(get("/cursos/$c1/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
        autenticada(get("/alunos/$a1/matriculas")).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `validacao rejeita campos vazios dados invalidos e JSON quebrado`() {
        autenticada(post("/alunos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":" ","email":"email-errado"}"""))
            .andExpect(status().isUnprocessableEntity).andExpect(jsonPath("$.errors.nome").exists())
        autenticada(post("/cursos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"Teste","cargaHoraria":0,"categoriaId":1}"""))
            .andExpect(status().isUnprocessableEntity)
        autenticada(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("{quebrado"))
            .andExpect(status().isBadRequest)
        autenticada(get("/alunos/0")).andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `referencias inexistentes respondem 404 e nao criam matricula`() {
        val cursoId = curso(categoria())
        autenticada(post("/cursos/$cursoId/matriculas").contentType(MediaType.APPLICATION_JSON)
            .content("""{"alunoId":99999}""")).andExpect(status().isNotFound)
        autenticada(get("/alunos/99999/matriculas")).andExpect(status().isNotFound)
        autenticada(get("/cursos/99999/matriculas")).andExpect(status().isNotFound)
        autenticada(post("/cursos").contentType(MediaType.APPLICATION_JSON)
            .content("""{"titulo":"Teste","cargaHoraria":10,"categoriaId":99999}"""))
            .andExpect(status().isNotFound)
        autenticada(get("/cursos/$cursoId/matriculas")).andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `categorias rejeitam duplicidade e DTO evita colecoes recursivas`() {
        categoria("Programacao")
        autenticada(post("/categorias").contentType(MediaType.APPLICATION_JSON)
            .content("""{"nome":"PROGRAMACAO"}""")).andExpect(status().isConflict)
        autenticada(get("/categorias")).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].nome").value("Programacao"))
            .andExpect(jsonPath("$[0].cursos").doesNotExist())
        autenticada(get("/health")).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OK"))
    }

    @Test
    fun `catalogo publico e login com senha armazenada como hash`() {
        val categoriaId = categoria()
        val cursoId = curso(categoriaId)
        for (rota in listOf("/health", "/categorias", "/cursos", "/cursos/$cursoId")) {
            mvc.perform(get(rota)).andExpect(status().isOk)
        }
        login(USUARIO_TESTE.uppercase(), SENHA_TESTE)
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(1800))
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
        autenticada(get("/auth/me")).andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario").value(USUARIO_TESTE))
            .andExpect(jsonPath("$.senhaHash").doesNotExist())

        val hash = operadores.findById(USUARIO_TESTE).orElseThrow().senhaHash
        assertNotEquals(SENHA_TESTE, hash)
        assertTrue(passwordEncoder.matches(SENHA_TESTE, hash))
    }

    @Test
    fun `login incorreto nao indica se o operador existe`() {
        for (usuario in listOf(USUARIO_TESTE, "operador-inexistente")) {
            login(usuario, "senha-incorreta")
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.detail").value("Usuário ou senha inválidos."))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
        }
    }

    @Test
    fun `escritas e consultas privadas exigem token`() {
        val alunoId = aluno()
        val cursoId = curso(categoria())
        criar("/cursos/$cursoId/matriculas", """{"alunoId":$alunoId}""")

        val protegidas = listOf(
            post("/categorias"), post("/cursos"), put("/cursos/$cursoId"), delete("/cursos/$cursoId"),
            post("/alunos"), put("/alunos/$alunoId"), delete("/alunos/$alunoId"),
            post("/cursos/$cursoId/matriculas"), delete("/cursos/$cursoId/matriculas/$alunoId"),
            get("/alunos"), get("/alunos/$alunoId"), get("/alunos/$alunoId/matriculas"),
            get("/cursos/$cursoId/matriculas"), get("/auth/me")
        )
        for (request in protegidas) {
            mvc.perform(request).andExpect(status().isUnauthorized)
        }
        assertEquals(1L, cursos.count())
        assertEquals(1L, alunos.count())
        assertEquals(1L, matriculas.count())
    }

    @Test
    fun `token invalido ou expirado bloqueia exclusao`() {
        val id = curso(categoria())
        val agora = Instant.now()
        val expirado = tokenDeTeste(ESCOPO_OPERADOR, agora.minusSeconds(1800), agora.minusSeconds(1))
        for (invalido in listOf("token-invalido", expirado)) {
            mvc.perform(delete("/cursos/$id").header(HttpHeaders.AUTHORIZATION, "Bearer $invalido"))
                .andExpect(status().isUnauthorized)
        }
        assertTrue(cursos.existsById(id))
        autenticada(delete("/cursos/$id")).andExpect(status().isNoContent)
    }

    @Test
    fun `token sem permissao de operador recebe 403`() {
        val agora = Instant.now()
        val semPermissao = tokenDeTeste("leitura", agora, agora.plusSeconds(60))
        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer $semPermissao"))
            .andExpect(status().isForbidden)
    }
}
