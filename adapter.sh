#!/bin/bash
#
# This script runs the example application.
# Usage: adapter.sh <command>
# <command> is one of: start, stop, now

# The name to be displayed when starting/stopping the application
SERVICE_NAME=taxii-log-adapter

# Directory where the application jar file is located
HOME_DIR=/data/taxii-log-adapter

# Directory where the log files will be created
LOG_DIR=$HOME_DIR/log
mkdir -p $LOG_DIR

PATH_TO_JAR=$HOME_DIR/taxii-log-adapter.jar
PID_PATH_NAME=$HOME_DIR/application.pid
PATH_TO_CONFIG=$HOME_DIR/config

JAVA_OPTS="-Djava.io.tmpdir=$HOME_DIR"

function now {
    echo "Triggering $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        cd $HOME_DIR
        java $JAVA_OPTS -Dspring.profiles.active=now -jar $PATH_TO_JAR 2> $LOG_DIR/err.out > $LOG_DIR/std.out
        echo "$SERVICE_NAME finished"
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

function smoketest {
    echo "Starting $SERVICE_NAME smoke test ..."
    if [ ! -f $PID_PATH_NAME ]; then
        cd $HOME_DIR
        java $JAVA_OPTS -Dspring.profiles.active=smoketest -jar $PATH_TO_JAR
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

function start {
    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        cd $HOME_DIR
        nohup java $JAVA_OPTS -Dspring.profiles.active=schedule -jar $PATH_TO_JAR 2> $LOG_DIR/err.out > $LOG_DIR/std.out &
        echo "$SERVICE_NAME started ..."
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

function stop {
    if [ -f $PID_PATH_NAME ]; then
        PID=$(cat $PID_PATH_NAME);
        echo "$SERVICE_NAME stoping ..."
        kill $PID;
        echo "$SERVICE_NAME stopped ..."
        rm -f $PID_PATH_NAME
    else
        echo "$SERVICE_NAME is not running ..."
    fi
}

function config {
    java $JAVA_OPTS -Dspring.profiles.active=config -Dlogging.config=classpath:config/template/logback.xml -jar $PATH_TO_JAR
    echo "No configuration directory found - created new"
    echo "YOU MUST CONFIGURE FILES config/application.yml config/logback.xml MANUALLY"

}

if [ -d $PATH_TO_CONFIG ]
then
  case $1 in
    now)
        now
    ;;
    smoketest)
        smoketest
    ;;
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        sleep 5
        start
    ;;
  esac
else
  config
fi
