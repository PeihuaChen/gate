@echo off
REM ######################################################################
REM Gate run script by Valentin Tablan 01 Jun 2001
REM This script uses some paths that it finds using some informed guesses.
REM If you have problems starting gate please check the values below.
REM
REM
REM GATE_HOME should point to the top level directory of your Gate
REM installation.
REM
REM JAVA_HOME should point to the top level installation of the java VM you
REM want to use for gate.
REM
REM GATE_CONFIG points to the gate.xml file containing the gate
REM configuration options that are specific to your site (e.g. URL for the
REM Oracle database). This configuration file is not required so don't
REM worry if you don't have it.
REM
REM	TOOLSJAR should point to the tools.jar file to be used by gate. If the
REM VM specified by JAVA_HOME contains a tools.jar file in the /lib
REM directory it will be used otherwise the file pointed by TOOLSJAR will
REM used.
REM IMPORTANT: the tools.jar needs to have the same version as the java VM
REM used. The /bin directory of the gate installation contains the
REM tools.jar files for java2 versions 1.3 and 1.4.
REM
REM
REM   ALL THE VALUES DESCRIBED ABOVESHOULD BE VALID PATHS
REM   DO NOT INCLUDE ANY QUOTE SIGNS EVEN IF YOU PATH CONTAINS SPACES
REM   TAKE CARE NOT TO INCLUDE ANY TRAILING SPACES AT THE END OF THE LINES
REM
REM
REM ######################################################################
REM $Id$
REM ######################################################################



REM ######################################################################
REM set NLS_LANG for Oracle
set NLS_LANG=AMERICAN_AMERICA.UTF8
REM ######################################################################

REM ######################################################################
REM !!!!!    You shouldn't need to change anything below this line   !!!!!
REM ######################################################################




REM ######################################################################
REM Attempt to find GATE_HOME if not already set. This procedure will fail
REM on Windows 95/98 so this value should be already set.
REM Hopefully the installer did that.
REM ######################################################################


if not "x%GATE_HOME%"=="x" goto doneGH

set GATE_HOME=%~d0%~p0\..
goto doneGH

:doneGH
echo GATE_HOME: %GATE_HOME%


REM ######################################################################
REM set TOOLSJAR to where we hope tools.jar is are located
REM ######################################################################

if not "x%TOOLSJAR%"=="x" goto doneTJ

if EXIST "%JAVA_HOME%\lib\tools.jar" goto jdk


set TOOLSJAR=%GATE_HOME%\bin\tools14.jar
goto doneTJ

:jdk
set TOOLSJAR=%JAVA_HOME%\lib\tools.jar
goto doneTJ

:doneTJ
echo TOOLSJAR=%TOOLSJAR%


REM ######################################################################
REM set GATE_CONFIG to where we thing gate.xml is (or "" if not around)
REM ######################################################################

if not "x%GATE_CONFIG%"=="x" goto doneGC

if exist "%GATE_HOME%\bin\gate.xml" set GATE_CONFIG=%GATE_HOME%\bin\gate.xml
goto doneGC

:doneGC
echo GATE_CONFIG: %GATE_CONFIG%

REM ######################################################################
REM set GATEJAR
REM ######################################################################

set GATEJAR=%GATE_HOME%\bin\gate.jar
if not exist "%GATEJAR%" set GATEJAR=%GATE_HOME%\build\gate.jar

echo GATEJAR: %GATEJAR%

REM ######################################################################
REM set EXTDIR
REM ######################################################################

set EXTDIR=%GATE_HOME%\bin\ext
if not exist "%EXTDIR%\guk.jar" set EXTDIR=%GATE_HOME%\lib\ext

echo EXTDIR: %EXTDIR%

REM ######################################################################
REM set JAVA
REM ######################################################################

set JAVA=%JAVA_HOME%\bin\javaw.exe
if not exist "%JAVA%" set JAVA=%GATE_HOME%\jre1.4\bin\javaw.exe
if not exist "%JAVA%" set JAVA=javaw.exe

echo JAVA: %JAVA%

REM ######################################################################
REM set CLASSPATH
REM ######################################################################

set OLDCP=%CLASSPATH%
set CLASSPATH="%GATEJAR%";"%TOOLSJAR%";"%OLDCP%"

echo CLASSPATH: %CLASSPATH%

REM ######################################################################
REM if we have a site gate.xml set a var including the -i
REM ######################################################################

if not "x%GATE_CONFIG%"=="x" set FLAGS=-i "%GATE_CONFIG%"

echo FLAGS: %FLAGS%

REM ######################################################################
REM run the beast
REM ######################################################################

if "%OS%"=="Windows_NT" goto NT

echo RUN:"%JAVA%" -Xmx200m -Djava.ext.dirs="%EXTDIR%" -classpath %CLASSPATH% gate.Main %FLAGS% %1 %2 %3 %4 %5 %6 %7 %8 %9
start "%JAVA%" -Xmx200m -Djava.ext.dirs="%EXTDIR%" -classpath %CLASSPATH% gate.Main %FLAGS% %1 %2 %3 %4 %5 %6 %7 %8 %9
exit

:NT
echo RUN:"%JAVA%" -Xmx200m -Djava.ext.dirs="%EXTDIR%" -classpath %CLASSPATH% gate.Main %FLAGS% %1 %2 %3 %4 %5 %6 %7 %8 %9
start "GATE" "%JAVA%" -Xmx200m -Djava.ext.dirs="%EXTDIR%" -classpath %CLASSPATH% gate.Main %FLAGS% %1 %2 %3 %4 %5 %6 %7 %8 %9
