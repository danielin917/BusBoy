@echo off
set /p port="Enter Port Number: " %=%
@echo on
java -Xmx500m -jar dist\TelemetryGUI2.0.jar port=%port%
pause