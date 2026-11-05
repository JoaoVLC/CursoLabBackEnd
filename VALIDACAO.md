# Verificação desta entrega

Validação executada em 28/10/2026 23:15, horário de São Paulo.

- Ambiente: Linux x86_64, JDK Temurin 21.0.13, Gradle 9.6.1.
- Mesmas versões de Kotlin (2.2.21) e Spring Boot (4.0.7) dos arquivos fornecidos.
- Tarefas Gradle `test bootJar`: concluídas.
- JUnit: 8 testes, 0 falhas, 0 erros, 0 ignorados.
- Testes HTTP usando o JAR compilado:
  - Swagger: HTTP 200.
  - OpenAPI: 16 operações de negócio + health.
  - Carga inicial: 2 categorias, 3 cursos, 2 alunos.
  - Persistência: aluno recuperado após reiniciar o processo.
  - Carga inicial não duplicada após reiniciar.

O teste de reinício utilizou um banco separado; o pacote não contém o banco usado nos testes.

Os scripts .bat foram preparados para Windows. A execução validada aqui foi no ambiente Linux indicado; a execução no computador da apresentação ainda precisa ser ensaiada.

A autenticação JWT continua pendente. Os testes desta parcial não certificam uma proteção que ainda não foi implementada.

## Executável

Arquivo: `executavel/cursolab.jar`. Inclui as dependências da aplicação, sem precisar baixá-las na apresentação.

SHA-256:

```text
1cd05cd34a4269d09944f361e7504edc0d6a791732345b2bc0243dbe7c176ff9
```
