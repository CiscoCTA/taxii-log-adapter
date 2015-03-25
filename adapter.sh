#!/bin/sh
#
# This script runs the example application.
# Usage: adapter.sh <command>
# <command> is one of: start, stop, restart, now

# The name to be displayed when starting/stopping the application
SERVICE_NAME=taxii-log-adapter

# Directory where the application jar file is located
HOME_DIR=/data/taxii-log-adapter

# Directory where the log files will be created
LOG_DIR=$HOME_DIR/log

PATH_TO_JAR=$HOME_DIR/taxii-log-adapter.jar
PID_PATH_NAME=$HOME_DIR/application.pid


function now {
    echo "Triggering $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        cd $HOME_DIR
        java -Djsse.enableSNIExtension=false -Dspring.profiles.active=now -jar $PATH_TO_JAR 2> $LOG_DIR/err.out > $LOG_DIR/std.out
        echo "$SERVICE_NAME finished"
    else
        echo "$SERVICE_NAME is already running ..."
    fi
}

function start {
    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_PATH_NAME ]; then
        cd $HOME_DIR
        nohup java -Djsse.enableSNIExtension=false -Dspring.profiles.active=schedule -jar $PATH_TO_JAR 2> $LOG_DIR/err.out > $LOG_DIR/std.out &
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

case $1 in
    now)
        now
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