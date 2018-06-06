@echo off
for /f "tokens=1,2 delims==" %%a in (config.properties) do (
 if "%%a"=="jrePath" set jrePath=%%b
 )
echo %jrePath%
%jrePath%\java  -jar xiaov.jar
pause