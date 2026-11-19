# Verificação da versão 1.0.0

Os testes foram executados em Linux x86_64 com JDK Temurin 21.0.13.1 e Gradle 9.6.1. As tarefas `test bootJar` concluíram com **13 testes de integração aprovados**, sem falhas ou testes ignorados. O Gradle Wrapper iniciou com a versão 9.6.1 e o mesmo JDK.

A suíte verificou os CRUDs de curso e aluno, filtros e paginação, matrículas, duplicidade, integridade de exclusão e respostas de validação. Os testes de segurança cobriram a consulta pública ao catálogo, login, armazenamento da senha como hash, bloqueio de operações com token ausente, inválido ou expirado e rejeição de token sem a permissão de operador.

O executável `build/libs/cursolab-1.0.0.jar` também foi verificado por requisições HTTP no servidor local.

- O Swagger abriu e documentou 18 operações de negócio. As 14 operações que usam JWT exibiram o esquema de segurança. `/health` é uma rota de verificação.
- Uma tentativa de exclusão sem token retornou 401. Com o token válido, a mesma exclusão retornou 409 enquanto havia uma matrícula. A remoção da matrícula e as exclusões autorizadas retornaram 204.
- A versão final abriu um banco gerado pela parcial e preservou os cursos, alunos e vínculos existentes.
- Após reiniciar sem informar a senha por variável de ambiente, a conta e os dados permaneceram disponíveis. O token anterior foi rejeitado e um novo login funcionou.
- A troca da senha do mesmo operador foi verificada ao reiniciar com a nova configuração. A senha anterior foi rejeitada.

Os bancos usados nas verificações foram separados dos dados do projeto. A execução dos arquivos `.bat` no Windows deve ser conferida no computador da demonstração.
