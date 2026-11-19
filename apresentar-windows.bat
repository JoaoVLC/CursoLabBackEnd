@echo off
setlocal
cd /d "%~dp0"
if not exist "build\libs\cursolab-1.0.0.jar" (
  echo Executavel nao encontrado. Rode gradlew.bat bootJar ou abra iniciar-windows.bat.
  pause
  exit /b 1
)
set "CURSOLAB_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "CURSOLAB_JAVA=%JAVA_HOME%\bin\java.exe"
echo CursoLab - Executavel da versao final
echo Requisito: Java 21. O projeto recomenda o JDK 21 usado nas aulas.
echo Aguarde a aplicacao iniciar e abra:
echo http://localhost:8080/api/swagger-ui/index.html
echo Deixe esta janela aberta. Ctrl+C encerra o servidor.
echo.
"%CURSOLAB_JAVA%" -jar "build\libs\cursolab-1.0.0.jar"
if errorlevel 1 (
  echo.
  echo Falha ao iniciar. Confira Java 21, porta 8080 e a secao Dificuldades comuns no README.md.
)
pause
endlocal
