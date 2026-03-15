@echo off
REM ─────────────────────────────────────────────────────────────────
REM  Sport Manager — quick launcher
REM  Workaround: redirects JavaFX native-DLL cache to D:/javafx-cache
REM  so it avoids the non-ASCII character in %USERPROFILE%.
REM ─────────────────────────────────────────────────────────────────
title Sport Manager

echo [1/2] Building...
call mvn process-resources compile -q
if errorlevel 1 (
    echo Build failed. Check compiler output.
    pause
    exit /b 1
)

echo [2/2] Launching...
java ^
  --module-path "target\javafx-libs" ^
  --add-modules javafx.controls,javafx.fxml ^
  --enable-native-access=javafx.graphics ^
  -Duser.home=D:/javafx-cache ^
  -cp "target\classes" ^
  com.sportmanager.App

pause
