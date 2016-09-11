@echo off
REM FOR LATER:
REM set JAVA_HOME=jre/win_64
SET mypath=%~dp0
SET DIR=%mypath:~0,-1%
REM $DIR\bin\server.bat %CMD_LINE_ARGS%
$DIR\bin\server.bat port=8000 basedir="$DIR\gamedata"
