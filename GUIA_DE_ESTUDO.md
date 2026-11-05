# Como entender o CursoLab

Não tente decorar todas as linhas. Acompanhe uma requisição de criação de curso do início ao fim.

## 1. O cliente envia um POST

No Swagger, você envia JSON para `POST /api/cursos`. A URL chega ao método `criar` de `cursos/CursoController.kt` porque a classe tem `@RequestMapping("/cursos")` e o método tem `@PostMapping`.

`@RequestBody` transforma JSON em `CursoRequest`. `@Valid` executa as validações do DTO: título não vazio, carga horária entre 1 e 1000 e categoriaId positivo.

## 2. O Controller chama o Service

`CursoService.criar` procura a categoria no banco. Se o ID não existe, lança `NotFoundException`, que será convertido em HTTP 404.

Se existe, o serviço cria a entidade `Curso` e a salva. Depois monta um `CursoResponse`, registra um log e devolve o resultado. O controller retorna HTTP 201.

## 3. O Repository conversa com o banco

`CursoRepository` estende `JpaRepository<Curso, Long>`. Isso fornece `save`, `findById`, `delete` e outras operações sem implementar cada SQL manualmente. O método `pesquisar` usa JPQL para os filtros.

## 4. A entidade descreve a tabela

Em `Curso.kt`:

- `@Entity`: classe persistida pelo JPA.
- `@Table`: nome da tabela.
- `@Id`: chave primária.
- `@GeneratedValue`: ID gerado pelo banco.
- `@Column`: regras de coluna.
- `@ManyToOne`: vários cursos podem usar a mesma categoria.
- `@JoinColumn`: nome da coluna que guarda a chave estrangeira.

O `kotlin("plugin.jpa")` e o bloco `allOpen` do Gradle permitem a integração das classes Kotlin com os mecanismos do JPA/Hibernate.

## 5. A associação também tem uma entidade

`Matricula` guarda o aluno, o curso e a data. A tabela possui `aluno_id` e `curso_id`. O banco impede repetir esse par por meio de uma restrição de unicidade.

Em vez de enviar objetos Aluno e Curso inteiros no JSON, a API recebe os IDs e busca as entidades existentes. Isso evita criar alunos ou cursos acidentalmente ao matricular.

## 6. Tratamento de erro é centralizado

`GlobalExceptionHandler` converte exceções em respostas `ProblemDetail`. Cada serviço pode lançar uma exceção de negócio com uma mensagem clara, sem repetir toda a montagem da resposta HTTP.

Exemplos reais:

- `CursoService.buscarEntidade`: ID inexistente → 404.
- `CursoService.excluir`: curso matriculado → 409.
- `AlunoService.validarEmail`: e-mail já cadastrado → 409.
- Bean Validation: dados fora das regras → 422.

## 7. Transações e DTOs

As operações dos serviços usam `@Transactional`. As leituras usam `readOnly = true`. Os DTOs são montados dentro da transação, onde as relações podem ser acessadas com segurança.

`spring.jpa.open-in-view` está desativado: o controller não precisa manter uma sessão de banco aberta para percorrer entidades. Listas de matrículas usam `@EntityGraph` para carregar aluno e curso, e a consulta de cursos usa `join fetch` para a categoria.

## Arquivos, em ordem de leitura

1. `src/main/kotlin/br/pucpr/cursolab/CursoLabApplication.kt`
2. `src/main/resources/application.yaml`
3. `src/main/kotlin/br/pucpr/cursolab/cursos/CursoController.kt`
4. `src/main/kotlin/br/pucpr/cursolab/cursos/CursoDTO.kt`
5. `src/main/kotlin/br/pucpr/cursolab/cursos/CursoService.kt`
6. `src/main/kotlin/br/pucpr/cursolab/cursos/CursoRepository.kt`
7. `src/main/kotlin/br/pucpr/cursolab/cursos/Curso.kt`
8. Classes dos pacotes `alunos`, `categorias` e `matriculas`.
9. `src/main/kotlin/br/pucpr/cursolab/common/GlobalExceptionHandler.kt`
10. `src/test/kotlin/br/pucpr/cursolab/ApiIntegrationTests.kt`

## O que foi adaptado em relação à base da aula

| Base enviada | CursoLab |
|---|---|
| CRUD de usuários | Domínio de cursos, alunos, categorias e matrículas |
| Controller, Service, Repository e DTOs | Mesmo padrão, agrupado por entidade |
| H2 em memória | H2 em arquivo para preservar a demonstração |
| Validação e tratamento global | Mantidos e aplicados às regras do novo domínio |
| Springdoc/Swagger | Mantido e organizado por grupos de endpoints |
| Exemplo inicial sem relacionamentos de negócio | Categoria 1:N Curso e Aluno N:N Curso por Matricula |

A configuração JWT será adicionada em uma etapa posterior. O material de segurança ainda não faz parte desta implementação parcial.
