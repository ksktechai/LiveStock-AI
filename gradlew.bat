@echo off
REM Minimal Gradle wrapper bootstrap script.
REM If gradle-wrapper.jar is missing, install Gradle locally and run: gradle wrapper
set DIR=%~dp0
if not exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  echo gradle-wrapper.jar is missing.
  echo Install Gradle locally and run: gradle wrapper
  exit /b 1
)
java -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
