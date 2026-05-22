@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

if defined JAVA_HOME goto useJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if not "%ERRORLEVEL%"=="0" (
  echo ERROR: JAVA_HOME is not set and no java.exe was found in PATH.
  exit /b 1
)
goto run

:useJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_EXE%" (
  echo ERROR: JAVA_HOME is invalid: %JAVA_HOME%
  exit /b 1
)

:run
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
  echo Missing gradle\wrapper\gradle-wrapper.jar
  echo Run fetch-gradle-wrapper.cmd once, or install Gradle and run: gradle wrapper --gradle-version 7.6.4
  exit /b 1
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%~n0" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
