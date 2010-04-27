@REM
@REM  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
@REM  http://fusesource.com
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM
@echo off

REM ------------------------------------------------------------------------
if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_SCALATE_HOME=%~dp0..

if "%SCALATE_HOME%"=="" set SCALATE_HOME=%DEFAULT_SCALATE_HOME%
set DEFAULT_SCALATE_HOME=

:doneStart
rem find SCALATE_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%SCALATE_HOME%\README.txt" goto checkJava

rem check for scalate in Program Files on system drive
if not exist "%SystemDrive%\Program Files\scalate" goto checkSystemDrive
set SCALATE_HOME=%SystemDrive%\Program Files\scalate
goto checkJava

:checkSystemDrive
rem check for scalate in root directory of system drive
if not exist %SystemDrive%\scalate\README.txt goto checkCDrive
set SCALATE_HOME=%SystemDrive%\scalate
goto checkJava

:checkCDrive
rem check for scalate in C:\scalate for Win9X users
if not exist C:\scalate\README.txt goto noMopHome
set SCALATE_HOME=C:\scalate
goto checkJava

:noMopHome
echo SCALATE_HOME is set incorrectly or scalate could not be located. Please set SCALATE_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto runAnt

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo.

:runAnt

if "%SCALATE_BASE%" == "" set SCALATE_BASE=%SCALATE_HOME%

if "%SCALATE_OPTS%" == "" set SCALATE_OPTS=-Xmx512M 

if "%SUNJMX%" == "" set SUNJMX=-Dcom.sun.management.jmxremote
REM set SUNJMX=-Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

REM Uncomment to enable YourKit profiling
REM SET SCALATE_DEBUG_OPTS="-agentlib:yjpagent"

REM Uncomment to enable remote debugging
REM SET SCALATE_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

"%_JAVACMD%" %SCALATE_DEBUG_OPTS% %SUNJMX% %SCALATE_OPTS% -Dscalate.classpath="%SCALATE_CLASSPATH%" -Dscalate.home="%SCALATE_HOME%" -Dscalate.base="%SCALATE_BASE%"  -classpath "%SCALATE_HOME%\lib\scalate-tool-${project.version}.jar;%SCALATE_HOME%\lib\scala-library-${scala-version}.jar" org.fusesource.scalate.tool.Scalate  %*

goto end

:end
set _JAVACMD=
if "%OS%"=="Windows_NT" @endlocal

