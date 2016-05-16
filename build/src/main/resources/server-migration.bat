@echo off
rem -------------------------------------------------------------------------
rem JBoss Server Migration Tool for Windows
rem -------------------------------------------------------------------------
rem
rem A simple tool for migrating servers.

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

set "BASE_DIR=%CD%"

rem Setup JBoss specific properties
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

# determine the default output dir, if not set
if "x%JBOSS_SERVER_MIGRATION_OUTPUT_DIR%" == "x" (
  set "JBOSS_SERVER_MIGRATION_OUTPUT_DIR=%BASE_DIR%\output"
  set "JAVA_OPTS=%JAVA_OPTS% -Djboss.server.migration.output.dir=%JBOSS_SERVER_MIGRATION_OUTPUT_DIR%"
)

set LOGGING_CONFIG=
echo "%JAVA_OPTS%" | findstr /I "logging.configuration" > nul
if errorlevel == 1 (
  set "LOGGING_CONFIG=-Dlogging.configuration=file:%BASE_DIR%\config\logging.properties -Djboss.server.migration.logfile=%JBOSS_SERVER_MIGRATION_OUTPUT_DIR%\migration.log"
) else (
  echo logging.configuration already set in JAVA_OPTS
)
if "x%LOGGING_CONFIG%" == "x" (
  "%JAVA%" %JAVA_OPTS% ^
      -cp "%BASE_DIR%\lib\*" ^
       org.jboss.migration.cli.CommandLineServerMigration ^
         %*
) else (
  "%JAVA%" %JAVA_OPTS% "%LOGGING_CONFIG%" ^
      -cp "%BASE_DIR%\lib\*" ^
       org.jboss.migration.cli.CommandLineServerMigration ^
       %*
)

set /A RC=%errorlevel%
:END
if "x%NOPAUSE%" == "x" pause

if "x%RC%" == "x" (
  set /A RC=0
)
exit /B %RC%
