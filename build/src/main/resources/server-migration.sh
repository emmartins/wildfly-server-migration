#!/bin/sh

# JBoss Server Migration Tool
#
# A simple tool for migrating servers.
#

DIRNAME=`dirname "$0"`
BASE_DIR=`cd "$DIRNAME"; pwd`

# OS specific support (must be 'true' or 'false').
cygwin=false;
if  [ `uname|grep -i CYGWIN` ]; then
    cygwin=true;
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    BASE_DIR=`cygpath --path --windows "$BASE_DIR"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y"

# determine the default output dir, if not set
if [ "x$JBOSS_SERVER_MIGRATION_OUTPUT_DIR" = "x" ]; then
   JBOSS_SERVER_MIGRATION_OUTPUT_DIR="$BASE_DIR"/output
   JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.output.dir="$JBOSS_SERVER_MIGRATION_OUTPUT_DIR"\""
fi

LOG_CONF=`echo $JAVA_OPTS | grep "logging.configuration"`
if [ "x$LOG_CONF" = "x" ]; then
    JAVA_OPTS=""$JAVA_OPTS" \"-Dlogging.configuration=file:"$BASE_DIR"/config/logging.properties\""
    JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.logfile="$JBOSS_SERVER_MIGRATION_OUTPUT_DIR"/migration.log\""
else
    echo "logging.configuration already set in JAVA_OPTS"
    JAVA_OPTS="$JAVA_OPTS"
fi

eval \"$JAVA\" $JAVA_OPTS \
    -cp \""$BASE_DIR"/lib/*\" \
    org.jboss.migration.cli.CommandLineServerMigration \
    '"$@"'
