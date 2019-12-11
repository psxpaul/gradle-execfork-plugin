@echo off

:loop
echo "PANG"
:: Use powershell as this does /not/ require streams attached and can sleep milliseconds. It is present on all supported Windows versions.
powershell -command "sleep -Milliseconds 100"
goto loop