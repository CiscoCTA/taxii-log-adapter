@echo off

REM Usage: adapter.cmd <command>

REM <command> is one of: start, stop, now, smoketest

REM The name to be displayed when starting/stopping the application

set SERVICE_NAME=taxii-log-adapter
REM Directory where the application jar file is located

set HOME_DIR=C:\taxii\taxii-log-adapter



REM Directory where the log files will be created

set LOG_DIR=%HOME_DIR%\log
mkdir %LOG_DIR%

set PATH_TO_JAR=%HOME_DIR%\taxii-log-adapter.jar

set PID_PATH_NAME=%HOME_DIR%\application.pid

set CONFIG_PATH_NAME=%HOME_DIR%\config

set JAVA_OPTS=-Djava.io.tmpdir=%HOME_DIR%

cd %HOME_DIR%

IF EXIST %CONFIG_PATH_NAME% (
    GOTO %1
) ELSE (
    GOTO config
)

:config

        java %JAVA_OPTS% -Dspring.profiles.active=config -Dlogging.config=classpath:config/template/logback.xml -jar %PATH_TO_JAR%
        echo No configuration directory found - created new
        echo YOU MUST CONFIGURE FILES config\application.yml config\logback.xml MANUALLY


    GOTO end


:now
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo Triggering %SERVICE_NAME% ...
        java %JAVA_OPTS% -Dspring.profiles.active=now -jar %PATH_TO_JAR% 2>%LOG_DIR%/err.out 1>%LOG_DIR%/std.out
        echo %SERVICE_NAME% finished
    )

    GOTO end

:smoketest
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo Starting %SERVICE_NAME% smoke test ...
        java %JAVA_OPTS% -Dspring.profiles.active=smoketest -jar %PATH_TO_JAR%
    )

    GOTO end

:smoketestssl
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo Starting %SERVICE_NAME% smoke test ...
        java %JAVA_OPTS% -Dspring.profiles.active=smoketest -Djavax.net.debug=ssl -jar %PATH_TO_JAR%
    )

    GOTO end

:start
    IF EXIST %PID_PATH_NAME% (
        echo %SERVICE_NAME% is already running ...
    ) ELSE (
        echo %SERVICE_NAME% starting...
        java %JAVA_OPTS% -Dspring.profiles.active=schedule -jar %PATH_TO_JAR% 2>%LOG_DIR%/err.out 1>%LOG_DIR%/std.out
        echo %SERVICE_NAME% finished
        )
    GOTO end

:stop
    set /p PID=<%PID_PATH_NAME%
    taskkill /PID %PID% /F
    del %PID_PATH_NAME%
    echo stopped

:end
