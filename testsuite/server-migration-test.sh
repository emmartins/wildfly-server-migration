#!/bin/sh
set -e
set -o pipefail

DIRNAME=`dirname "$0"`
TEST_DIR=`cd "$DIRNAME"; pwd`
TOOL_DIR="$TEST_DIR/jboss-server-migration"

SOURCE_DIST_DIR="$1"
TARGET_SRC_DIR="$2"

if [ "x$SOURCE_DIST_DIR" != "x" ]; then
    if [[ $SOURCE_DIST_DIR != /* ]]; then
        SOURCE_DIST_DIR="$TEST_DIR/$SOURCE_DIST_DIR"
    fi
else
    echo "### Usage: ./server-migration-test.sh SOURCE_DIST_DIR TARGET_SRC_DIR"
    exit
fi

echo "### Source Server base directory: $SOURCE_DIST_DIR"

if [ "x$TARGET_SRC_DIR" != "x" ]; then
    if [[ $TARGET_SRC_DIR != /* ]]; then
        TARGET_SRC_DIR="$TEST_DIR/$TARGET_SRC_DIR"
    fi
else
    echo "### Usage: ./server-migration-test.sh SOURCE_DIST_DIR TARGET_SRC_DIR"
    exit
fi

echo "### Target Server base directory: $TARGET_SRC_DIR"

for i in "$TARGET_SRC_DIR/dist/target"/*
do
  if [[ $i == *.jar ]]; then
    TARGET_DIST_DIR="${i%.jar}"
    break
  fi
done

echo "### Target Server dist directory: $TARGET_DIST_DIR"

echo "### Preparing JBoss Server Migration Tool binary..."
rm -Rf $TOOL_DIR
unzip $TEST_DIR/../build/target/jboss-server-migration-*.zip -d $TEST_DIR

echo "### Executing the migration..."
$TOOL_DIR/server-migration.sh --source $SOURCE_DIST_DIR --target $TARGET_DIST_DIR --interactive false -Djboss.server.migration.domain.skip=false

echo "### Patching target server's migrated config files..."
TARGET_STANDALONE_CONFIG_DIR="$TARGET_DIST_DIR/standalone/configuration"
sed -f $TEST_DIR/migration.patch -i '' $TARGET_STANDALONE_CONFIG_DIR/standalone.xml $TARGET_STANDALONE_CONFIG_DIR/standalone-full.xml

echo "### Running target server's testsuite..."
mvn -f $TARGET_SRC_DIR/testsuite/pom.xml clean install -Dts.basic