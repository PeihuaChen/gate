@echo off
rem A batch file to run GATE
rem Kali and Valy, 9/3/1
rem This batch file was written and tested under Windows NT
rem $Id$

rem if the correct version of java isn't in your path, or in the
rem JAVA_HOME variable, then set JAVA_HOME to the correct location here, e.g.
rem SET JAVA_HOME=w:\jdk\jdk1.3

rem Guess if JAVA_HOME is defined
if not "%JAVA_HOME%" == "" goto gothome

SET JAVA=java

goto execute

:gothome

SET JAVA=%JAVA_HOME%\bin\java.exe 

:execute

%JAVA% -Djava.ext.dirs=..\lib\ext -classpath gate.jar;..\lib\ext\guk.jar -Xmx200m gate.Main %1 %2 %3 %4 %5 %6 %7 %8 %9




