# Roteiro da apresentação final do CursoLab

Reserve de 7 a 9 minutos e ajuste ao tempo indicado pelo professor. João pode apresentar a proposta, o modelo e a consulta. Barbara pode demonstrar o login, o CRUD e as matrículas. Dividam a explicação dos arquivos e participem do vídeo.

## Antes de começar

- Configure a conta de operador conforme o README. Execute a aplicação e abra `http://localhost:8080/api/swagger-ui/index.html`.
- Deixe o terminal dos logs e o projeto abertos.
- Consulte `GET /categorias` e anote o ID da categoria que será usada no cadastro.
- Abra `README.md`, `Curso.kt`, `Matricula.kt`, `CursoService.kt`, `AlunoService.kt` e `auth/SecurityConfig.kt`.
- Use dados fictícios e uma senha exclusiva para o ambiente local de demonstração.

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

## 4. Demonstrar o login — 1 minuto

Execute `POST /auth/login` com o usuário e a senha configurados no terminal. Copie o campo `accessToken`, clique em **Authorize** e cole o token. Execute `GET /auth/me` para mostrar o usuário conectado.

Explique que a senha fica no banco como hash BCrypt. Depois de conferir as credenciais, o servidor assina um token que vale 30 minutos. As próximas requisições enviam o token no cabeçalho `Authorization`.

## 5. Demonstrar um CRUD — 1 a 2 minutos

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

## 6. Demonstrar matrículas e proteção de exclusão — 2 minutos

Crie um aluno em `POST /alunos`:

```json
{"nome": "Carla Exemplo", "email": "carla@example.com"}
```

Se esse e-mail já existe por causa do ensaio, reutilize o ID existente ou escolha outro e-mail fictício. Anote o ID retornado.

No `POST /cursos/{cursoId}/matriculas`, use o ID do curso criado. No corpo, coloque o ID do aluno:

```json
{"alunoId": 3}
```

Consulte `GET /cursos/{cursoId}/matriculas`. O retorno mostra nomes, IDs e data da matrícula. Repita o POST para mostrar o erro **409** de matrícula duplicada.

1. Remova a autorização do Swagger em **Authorize → Logout**. Tente `DELETE /cursos/{id}` com o ID do curso criado. A API retorna **401** porque a requisição chegou sem token.
2. Autorize novamente com o token e repita o DELETE. A API retorna **409** porque o curso ainda tem matrículas.
3. Remova a associação em `DELETE /cursos/{cursoId}/matriculas/{alunoId}`. Exclua o curso e o aluno criados para a demonstração. A remoção da matrícula e as exclusões retornam **204**.

## 7. Mostrar organização, logs e erros — 1 minuto

Abra `CursoController`: ele recebe a requisição, valida os dados e chama o serviço.

Abra `CursoService`: ele concentra as regras e chama os repositórios. Aponte `@Transactional`, o bloqueio de exclusão e os logs. Mostre também a validação de e-mail duplicado em `AlunoService`.

No terminal, aponte os logs de criação, alteração e exclusão. Em `GlobalExceptionHandler`, mostre a conversão das exceções em respostas HTTP estruturadas.

Abra `SecurityConfig.kt` para mostrar as rotas públicas e onde a aplicação exige o JWT de operador. Em `AuthService.kt`, mostre como o login define a validade e assina o token.

Se houver tempo, envie um curso com `cargaHoraria: 0` para mostrar **422** ou consulte um ID inexistente para mostrar **404**.

Mostre no README os 18 endpoints e onde ficam as instruções de execução e os resultados dos testes.

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
| O banco some ao reiniciar? | O banco da aplicação é H2 em arquivo e mantém os registros. Os testes usam outro banco, em memória. |
| O que é BCrypt? | É a função usada para gerar o hash da senha. No login, o Spring Security confere a senha informada contra esse hash. |
| Qual é a diferença entre 401 e 409 na exclusão? | 401 indica que faltou um token válido. Com o token correto, 409 indica que o curso tem matrículas vinculadas. |
| O que muda quando a aplicação reinicia? | A conta permanece no banco. A chave do JWT é gerada de novo, então é preciso obter outro token. |
