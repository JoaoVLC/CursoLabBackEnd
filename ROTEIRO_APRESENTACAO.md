# Roteiro da apresentação parcial — 29/10/2026

Tempo sugerido: 6 a 8 minutos. Adapte ao tempo determinado pelo professor. Ensaie no mesmo computador antes da aula.

## Antes de começar

- Execute a aplicação e abra `http://localhost:8080/api/swagger-ui/index.html`.
- Deixe o terminal dos logs e o projeto abertos.
- Consulte `GET /categorias`, `GET /cursos` e `GET /alunos`; anote os IDs atuais.
- Abra `README.md`, `Curso.kt`, `Matricula.kt`, `CursoService.kt` e `AlunoService.kt`.
- A documentação mostra 16 endpoints de negócio. `/health` é infraestrutura, fora da contagem.

## 1. Apresentar a proposta — 30 segundos

Explique com suas palavras: o CursoLab é um backend para uma escola cadastrar cursos e alunos e gerenciar matrículas. A opção escolhida é a primeira do enunciado. A base técnica acompanha o projeto da aula, com Kotlin, Spring Boot, JPA e H2.

## 2. Explicar as classes e relações — 1 minuto

Mostre as quatro entidades:

- Categoria agrupa cursos: uma categoria pode ter muitos cursos.
- Curso pertence a uma categoria.
- Aluno pode fazer vários cursos.
- Matricula liga um aluno a um curso e guarda a data desse vínculo.

Abra `Categoria.kt` e `Curso.kt` e aponte `@OneToMany`, `@ManyToOne` e `@JoinColumn`. Abra `Matricula.kt` e explique que dois vínculos muitos-para-um formam o relacionamento muitos-para-muitos com entidade no meio.

## 3. Demonstrar consulta — 45 segundos

No Swagger, abra `GET /cursos`, clique em **Try it out** e depois **Execute**.

Em seguida, preencha:

- `minHoras`: 25.
- `ordenarPor`: cargaHoraria.
- `direcao`: DESC.

Na carga inicial, aparecem primeiro Spring Boot (40h), depois SQL (30h). Kotlin (20h) fica fora pelo filtro. Explique que os parâmetros vêm pela URL, usando `@RequestParam`.

## 4. Demonstrar um CRUD — 1 a 2 minutos

Em `POST /cursos`, use uma categoria existente:

```json
{
  "titulo": "Kotlin aplicado",
  "descricao": "Curso criado durante a apresentação.",
  "cargaHoraria": 24,
  "categoriaId": 1
}
```

Troque o 1 se a categoria tiver outro ID. Anote o ID retornado pelo curso. Mostre o status **201**.

Consulte `GET /cursos/{id}` e depois use `PUT /cursos/{id}` com os mesmos campos, alterando a carga horária para **32**. Mostre a atualização no GET. Deixe a exclusão para depois da demonstração da matrícula.

## 5. Demonstrar a associação e uma regra — 1 a 2 minutos

Crie um aluno em `POST /alunos`:

```json
{"nome": "Carla Exemplo", "email": "carla@example.com"}
```

Se esse e-mail já existe por causa do ensaio, reutilize o ID existente ou escolha outro e-mail fictício. Anote o ID retornado.

No `POST /cursos/{cursoId}/matriculas`, use o ID do curso criado. No corpo, coloque o ID do aluno:

```json
{"alunoId": 3}
```

Consulte `GET /cursos/{cursoId}/matriculas`. O retorno mostra nomes, IDs e data da matrícula. Repita o POST para mostrar **409: matrícula duplicada**.

Tente `DELETE /cursos/{id}` no curso matriculado: deve retornar **409**, pois o curso tem vínculos. Remova a matrícula em `DELETE /cursos/{cursoId}/matriculas/{alunoId}`. Agora exclua o curso e o aluno: ambos retornam **204**.

## 6. Mostrar organização, logs e erros — 1 minuto

Abra `CursoController`: ele recebe a requisição, valida os dados e chama o serviço.

Abra `CursoService`: ele concentra as regras e chama os repositórios. Aponte `@Transactional`, o bloqueio de exclusão e os logs. Mostre também a validação de e-mail duplicado em `AlunoService`.

No terminal, aponte os logs de criação, alteração e exclusão. Em `GlobalExceptionHandler`, mostre a conversão das exceções em respostas HTTP estruturadas.

Se houver tempo, envie um curso com `cargaHoraria: 0` para mostrar **422** ou consulte um ID inexistente para mostrar **404**.

## 7. Encerrar com o estado real — 30 segundos

Mostre a tabela do README: 16 de 18 endpoints implementados, acima do mínimo de 9 nesse escopo. Explique que a próxima etapa é adicionar autenticação JWT, proteger as rotas e finalizar o vídeo/documentação.

**Não apresente JWT como concluído:** nesta versão ainda não existe login nem exigência de token nos endpoints.

## Perguntas que você deve saber responder

| Pergunta | Ideia principal |
|---|---|
| O que é um endpoint? | Uma operação HTTP em uma rota; GET e POST na mesma rota são operações diferentes. |
| Por que criar Matricula como entidade? | O vínculo tem dados próprios, como data, e precisa de regras de duplicidade. |
| Por que DTO? | Define o contrato da API e evita enviar a entidade inteira e suas relações. |
| O que faz o Repository? | Acessa o banco; Spring Data implementa operações comuns a partir da interface. |
| O que faz o Service? | Aplica regras, verifica existência/duplicidade e coordena operações. |
| Para que serve @Transactional? | Executa a operação dentro de uma transação, com rollback em falhas apropriadas. |
| Como funciona a ordenação? | Um campo permitido e ASC/DESC geram um Sort aplicado à consulta no banco. |
| O banco some ao reiniciar? | O banco da aplicação é H2 em arquivo; o dos testes é em memória. |
| Já existe autenticação? | Ainda não; faz parte do plano para a final e protegerá operações como PUT e DELETE. |

Se a equipe tiver três pessoas, uma pode explicar o domínio/relações, outra demonstrar CRUD/filtros e a terceira apresentar matrículas/erros/próximos passos. Todos precisam entender o fluxo completo.
