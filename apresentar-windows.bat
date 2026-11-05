@echo off
setlocal
cd /d "%~dp0"
if not exist "executavel\cursolab.jar" (
  echo Executavel nao encontrado. Use iniciar-windows.bat para compilar e executar o codigo.
  pause
  exit /b 1
)
set "CURSOLAB_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "CURSOLAB_JAVA=%JAVA_HOME%\bin\java.exe"
echo CursoLab - Executavel da parcial
echo Requisito: Java 21. O projeto recomenda o JDK 21 usado nas aulas.
echo Aguarde a aplicacao iniciar e abra:
echo http://localhost:8080/api/swagger-ui/index.html
echo Deixe esta janela aberta. Ctrl+C encerra o servidor.
echo.
"%CURSOLAB_JAVA%" -jar "executavel\cursolab.jar"
if errorlevel 1 (
  echo.
  echo Falha ao iniciar. Confira Java 21, porta 8080 e a secao Dificuldades comuns no README.md.
)
pause
endlocal
