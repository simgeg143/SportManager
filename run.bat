@echo off
REM ─────────────────────────────────────────────────────────────────
REM  Sport Manager — quick launcher
REM  Entry: JfxLauncher (sets JavaFX native cache, then starts App).
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
  -cp "target\classes" ^
  com.sportmanager.JfxLauncher

pause
