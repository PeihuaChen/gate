@echo off
REM ##############################################
REM $Id$
REM ##############################################

set GATE_HOME=%1
set JAVA_HOME=%2

cd  /d %1\bin
echo set GATE_HOME=%1> tempEnv.txt
echo set JAVA_HOME=%2>> tempEnv.txt
echo if not exist %JAVA_HOME%\lib\tools.jar set CLASSPATH="%CLASSPATH%";%GATE_HOME%\bin\tools14.jar >> tempEnv.txt

move gate.bat gate1.bat
copy tempEnv.txt+gate1.bat gate.bat
del gate1.bat
del tempEnv.txt