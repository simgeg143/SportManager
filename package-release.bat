@echo off
REM Builds target\sport-manager-release.zip (thin app jar + lib with JavaFX — no bundled JRE).
cd /d "%~dp0"
REM Skip Surefire when JavaFX headless/GPU isn't available (use `mvn test` separately).

echo Trying clean + package...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo CLEAN FAILED — Windows often locks target\ when Sport Manager, tests, or the IDE still run.
    echo Close the game, stop any Java processes for this project, then retry.
    echo Packaging WITHOUT clean so you can still get the zip...
    echo.
    call mvn package -DskipTests
    if errorlevel 1 exit /b 1
)
echo.
echo Distribution: %CD%\target\sport-manager-release.zip
pause
