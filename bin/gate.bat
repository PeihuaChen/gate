@echo off
REM ##############################################
REM Gate run script by Valentin Tablan 01 Jun 2001
REM $id$
REM ##############################################


REM ##############################################
REM let's find JAVA
REM ##############################################

set RUNJAVA="java"
if not "%JAVA_HOME%" == "" set RUNJAVA="%JAVA_HOME%\bin\java"
echo Using java in %RUNJAVA%


REM ##############################################
REM let's find GATE
REM ##############################################
SET GATE_HOME=%~d0%~p0
if exist "%GATE_HOME%gate.jar" goto okGate

%~d0
cd %~p0
SET GATE_HOME=..\build\
if exist "%GATE_HOME%gate.jar" goto okGate

echo Could not find gate.jar
goto finish


:okGate
echo using gate.jar in "%GATE_HOME%gate.jar"


REM ##############################################
REM let's find GUK
REM ##############################################
REM we have gate.jar; let's find guk.jar
SET GUK_HOME=%~d0%~p0ext
if exist "%GUK_HOME%\guk*.jar" goto allGo
%~d0
cd %~p0

SET GUK_HOME=..\lib\ext\
if exist "%GUK_HOME%\guk*.jar" goto allGo


echo Could not find guk.jar
goto finish


REM ##############################################
REM Run the thing
REM ##############################################
:allGo
echo using guk.jar in "%GUK_HOME%guk.jar"
%RUNJAVA% -Xmx200m -cp "%GATE_HOME%gate.jar" -Djava.ext.dirs="%GUK_HOME%" gate.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:finish