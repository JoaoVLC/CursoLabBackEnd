@echo off
setlocal
cd /d "%~dp0"
echo CursoLab - Apresentacao parcial
echo Requisito: JDK 21. A primeira execucao precisa de internet.
echo Swagger: http://localhost:8080/api/swagger-ui/index.html
echo.
call gradlew.bat bootRun
if errorlevel 1 (
  echo.
  echo A aplicacao nao iniciou. Leia o erro acima e a secao Dificuldades comuns no README.md.
)
pause
endlocal
