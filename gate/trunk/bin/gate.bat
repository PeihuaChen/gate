@echo off
REM ##############################################
REM Gate run script by Valentin Tablan 01 Jun 2001
REM $Id$
REM ##############################################

REM ##############################################
REM set LOCAT to where we hope gate.jar etc. are located
REM ##############################################

if not x%GATE_HOME% == x goto gotGateHome

set LOCAT=%~d0%~p0
goto doneGateHome

:gotGateHome
set LOCAT=%GATE_HOME%\bin

:doneGateHome
echo LOCAT: %LOCAT%


REM ##############################################
REM set TOOLSJAR to where we hope tools.jar is are located
REM ##############################################

set TOOLSJAR=%LOCAT%\tools14.jar
if not exist %TOOLSJAR% set TOOLSJAR=%LOCAT%\..\lib\tools14.jar

echo TOOLSJAR=%TOOLSJAR%


REM ##############################################
REM set SITEGATEXML to where we thing gate.xml is (or "" if not around)
REM ##############################################

set SITEGATEXML=
if not x%GATE_CONFIG% == x goto gotGateConfig

if exist %LOCAT%\gate.xml set SITEGATEXML=%LOCAT%\gate.xml
goto doneGateConfig

:gotGateConfig
set SITEGATEXML=%GATE_CONFIG%

:doneGateConfig

if not exist %SITEGATEXML% set SITEGATEXML=

echo SITEGATEXML: %SITEGATEXML%


REM ##############################################
REM set GATEJAR and GUK to gate.jar and ext locations
REM ##############################################

set GATEJAR=%LOCAT%\gate.jar
set GUK=%LOCAT%\ext
if not exist %GATEJAR% set GATEJAR=%LOCAT%\..\build\gate.jar
if not exist %GUK%\guk.jar set GUK=%LOCAT%\..\lib\ext

echo GATEJAR: %GATEJAR%
echo GUK: %GUK%


REM ##############################################
REM set JAVA
REM ##############################################

set JAVA=%LOCAT%\..\jre1.4\bin\javaw.exe
if not exist %JAVA% set JAVA=%JAVA_HOME%\bin\javaw.exe
if not exist %JAVA% set JAVA=javaw.exe

echo JAVA: %JAVA%


REM ##############################################
REM set CLASSPATH
REM ##############################################

set "CLASSPATH=%GATEJAR%;%TOOLSJAR%;%CLASSPATH%"

echo CLASSPATH: %CLASSPATH%


REM ##############################################
REM if we have a site gate.xml set a var including the -i
REM ##############################################

if not x%SITEGATEXML% == x set FLAGS=-i %SITEGATEXML%

echo FLAGS: %FLAGS%


REM ##############################################
REM run the beast
REM ##############################################

set RUN=%JAVA% -Xmx200m -Djava.ext.dirs=%GUK% -classpath "%CLASSPATH%" gate.Main %FLAGS% %1 %2 %3 %4 %5 %6 %7 %8 %9

echo RUN: %RUN%
start %RUN%
