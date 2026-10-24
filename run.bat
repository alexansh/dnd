@echo off
title Mythos 5E - Solo AI D&D Tabletop
echo ====================================================
echo         MYTHOS 5E - Solo AI Virtual Tabletop
echo ====================================================
echo.

cd /d "%~dp0"

IF NOT EXIST "venv\Scripts\activate.bat" (
    echo [!] Virtual environment not detected. Creating venv...
    python -m venv venv
    call venv\Scripts\activate.bat
    echo [*] Installing dependencies from requirements.txt...
    python -m pip install --upgrade pip
    python -m pip install -r requirements.txt
) ELSE (
    call venv\Scripts\activate.bat
)

echo [*] Starting server on http://127.0.0.1:8000 ...
echo [*] Opening browser in 2 seconds...
start "" timeout /t 2 /nobreak >nul & start http://127.0.0.1:8000

uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
pause
