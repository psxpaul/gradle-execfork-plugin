@echo off

set DIR=%~dp0

echo "starting subprocess from %DIR%"
start /b cmd /c call "%DIR%/SubProcess.bat"

:loop
echo "PING"
echo "PONG" 1>&2
:: Use powershell as this does /not/ require streams attached and can sleep milliseconds. It is present on all supported Windows versions.
powershell -command "sleep -Milliseconds 100"
goto loop