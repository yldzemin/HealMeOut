@echo off
rem Windows: double-click to run ASTRA-TWIN. It uses Git Bash (comes with Git for Windows) to run start.sh,
rem which builds what is needed and starts the app. Then open http://127.0.0.1:8080/  -  close this window to stop.
cd /d "%~dp0"
set "GITBASH=%ProgramFiles%\Git\bin\bash.exe"
if not exist "%GITBASH%" set "GITBASH=%LocalAppData%\Programs\Git\bin\bash.exe"
if not exist "%GITBASH%" (
  echo Git for Windows is needed: https://git-scm.com/downloads  - install it, then double-click start.bat again.
  pause
  exit /b 1
)
"%GITBASH%" start.sh %*
pause
