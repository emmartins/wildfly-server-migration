#!/bin/sh

# JBoss Server Migration Tool
#
# A simple tool for migrating servers.
#

setModularJdk() {
  $JAVA --add-modules=java.se -version > /dev/null 2>&1 && MODULAR_JDK=true || MODULAR_JDK=false
}

setDefaultModularJvmOptions() {
  setModularJdk
  if [ "$MODULAR_JDK" = "true" ]; then
    DEFAULT_MODULAR_JVM_OPTIONS=`echo $* | $GREP "\-\-add\-modules"`
    if [ "x$DEFAULT_MODULAR_JVM_OPTIONS" = "x" ]; then
      # Set default modular jdk options
      DEFAULT_MODULAR_JVM_OPTIONS="$DEFAULT_MODULAR_JVM_OPTIONS --add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
      DEFAULT_MODULAR_JVM_OPTIONS="$DEFAULT_MODULAR_JVM_OPTIONS --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
      DEFAULT_MODULAR_JVM_OPTIONS="$DEFAULT_MODULAR_JVM_OPTIONS --add-exports=jdk.unsupported/sun.reflect=ALL-UNNAMED"
      DEFAULT_MODULAR_JVM_OPTIONS="$DEFAULT_MODULAR_JVM_OPTIONS --add-modules=java.se"
    else
      DEFAULT_MODULAR_JVM_OPTIONS=""
    fi
  fi
}

TOOL_OPTS=""
while [ "$#" -gt 0 ]
do
    case "$1" in
      -D*)
          JAVA_OPTS=""$JAVA_OPTS" \"$1\""
                ;;
      *)
          TOOL_OPTS="$TOOL_OPTS $1"
          ;;
    esac
    shift
done

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

# Set default modular JVM options
setDefaultModularJvmOptions $JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS $DEFAULT_MODULAR_JVM_OPTIONS"

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y"

JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.baseDir="$BASE_DIR"\""

LOG_CONF=`echo $JAVA_OPTS | grep "logging.configuration"`
if [ "x$LOG_CONF" = "x" ]; then
    JAVA_OPTS=""$JAVA_OPTS" \"-Djava.util.logging.manager=org.jboss.logmanager.LogManager\" \"-Dlogging.configuration=file:"$BASE_DIR"/configuration/logging.properties\""
    JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.logfile="$BASE_DIR"/logs/migration.log\""
else
    echo "logging.configuration already set in JAVA_OPTS"
    JAVA_OPTS="$JAVA_OPTS"
fi

eval \"$JAVA\" $JAVA_OPTS \
    -cp \""$BASE_DIR"/lib/*\" \
    org.jboss.migration.cli.CommandLineServerMigration \
    "$TOOL_OPTS"