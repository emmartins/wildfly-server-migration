#!/bin/sh

targetArg=false;
ARGS=""
while [ "$#" -gt 0 ]
do
    case "$1" in
      -D*)
          JAVA_OPTS=""$JAVA_OPTS" \"$1\""
          ;;
      *)
          ARGS="$ARGS '$1'"
          ;;
    esac
    shift
done

DIRNAME=`dirname "$0"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup JBOSS_HOME
RESOLVED_JBOSS_HOME=`cd "$DIRNAME/.."; pwd`
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=$RESOLVED_JBOSS_HOME
else
 SANITIZED_JBOSS_HOME=`cd "$JBOSS_HOME"; pwd`
 if [ "$RESOLVED_JBOSS_HOME" != "$SANITIZED_JBOSS_HOME" ]; then
   echo "WARNING JBOSS_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
 fi
fi
export JBOSS_HOME

if [ "x$JBOSS_MODULEPATH" = "x" ]; then
    JBOSS_MODULEPATH="$JBOSS_HOME/modules"
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
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    JBOSS_MODULEPATH=`cygpath --path --windows "$JBOSS_MODULEPATH"`
fi

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"

TOOL_BASEDIR="$JBOSS_HOME/migration"
JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.baseDir=$TOOL_BASEDIR\" \"-Djava.util.logging.manager=org.jboss.logmanager.LogManager\""

TARGET_BASEDIR_SET=`echo $ARGS | grep "\-\-target"`
if [ "x$TARGET_BASEDIR_SET" = "x" ]; then
    ARGS="$ARGS '--target' '$JBOSS_HOME'"
fi

LOG_CONF=`echo $JAVA_OPTS | grep "logging.configuration"`
if [ "x$LOG_CONF" = "x" ]; then
    JAVA_OPTS=""$JAVA_OPTS" \"-Dlogging.configuration=file:"$TOOL_BASEDIR"/configuration/logging.properties\""
    JAVA_OPTS=""$JAVA_OPTS" \"-Djboss.server.migration.logfile="$TOOL_BASEDIR"/logs/migration.log\""
else
    echo "logging.configuration already set in JAVA_OPTS"
    JAVA_OPTS="$JAVA_OPTS"
fi

eval \"$JAVA\" $JAVA_OPTS -jar \""$JBOSS_HOME"/jboss-modules.jar\" -mp \""${JBOSS_MODULEPATH}"\" org.jboss.migration.cli "$ARGS"