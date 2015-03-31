@echo off
REM This script runs the example application.

REM Usage: adapter.sh <command>

REM <command> is one of: start, stop, now

REM The name to be displayed when starting/stopping the application

set SERVICE_NAME=taxii-log-adapter
REM Directory where the application jar file is located

set HOME_DIR=C:\taxii\taxii-log-adapter



REM Directory where the log files will be created

set LOG_DIR=%HOME_DIR%\log


set PATH_TO_JAR=%HOME_DIR%\taxii-log-adapter.jar

set PID_PATH_NAME=%HOME_DIR%\application.pid


cd %HOME_DIR%

GOTO %1

:now
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo Triggering %SERVICE_NAME% ...

        java -Djsse.enableSNIExtension=false -Dspring.profiles.active=now -jar %PATH_TO_JAR% 2>%LOG_DIR%/err.out 1>%LOG_DIR%/std.out

        echo %SERVICE_NAME% finished
    )

    GOTO end

:start
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo %SERVICE_NAME% starting...
        java -Djsse.enableSNIExtension=false -Dspring.profiles.active=schedule -jar %PATH_TO_JAR% 2>%LOG_DIR%/err.out 1>%LOG_DIR%/std.out

        echo %SERVICE_NAME% finished
        )
    GOTO end

:stop
    set /p PID=<%PID_PATH_NAME%
    taskkill /PID %PID% /F
    del %PID_PATH_NAME%
    echo stopped

:end
