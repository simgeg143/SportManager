@echo off
REM Sport Manager — release launcher (no bundled JRE; JavaFX sits in .\lib)
REM Requires: JDK or JRE 17+ on PATH. Professor machine policy: runtime only, deps shipped here.
setlocal
title Sport Manager
cd /d "%~dp0"

set "APP_JAR=sport-manager.jar"
set "MODULE_PATH=lib"
set "JAVA_VER="
set "JAVA_MAJOR="

if not exist "%APP_JAR%" (
    echo Missing %APP_JAR%. Extract the full SportManager zip keeping this folder layout.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo Java was not found on PATH.
    echo Please install JDK/JRE 17 or newer, then reopen terminal and run this file again.
    pause
    exit /b 1
)

for /f "tokens=3 delims= " %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%~v"
for /f "tokens=1 delims=." %%m in ("%JAVA_VER%") do set "JAVA_MAJOR=%%m"
if "%JAVA_MAJOR%"=="1" for /f "tokens=2 delims=." %%m in ("%JAVA_VER%") do set "JAVA_MAJOR=%%m"
if not defined JAVA_MAJOR (
    echo Could not detect Java version.
    java -version
    pause
    exit /b 1
)
if %JAVA_MAJOR% LSS 17 (
    echo Detected Java version: %JAVA_VER%
    echo Sport Manager requires Java 17 or newer.
    echo Please install/update Java, then run SportManager.bat again.
    pause
    exit /b 1
)

REM Packaged lib\: normal startup. Missing jars\: classpath bootstrap downloads then restarts.
dir /b "%MODULE_PATH%\javafx-controls-*.jar" >nul 2>&1
if not errorlevel 1 (
    java ^
      --module-path "%MODULE_PATH%" ^
      --add-modules javafx.controls,javafx.fxml ^
      --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
      --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
      --add-opens javafx.base/com.sun.javafx.runtime=ALL-UNNAMED ^
      -cp "%APP_JAR%" ^
      com.sportmanager.JfxLauncher
    goto :launcher_done
)

echo Libraries not found under lib\. Download will run on first launch.
java -cp "%APP_JAR%" com.sportmanager.JfxLauncher

:launcher_done
if errorlevel 1 pause
