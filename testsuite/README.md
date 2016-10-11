JBoss Server Migration Tool Testsuite
==========================================

The resources to test the server migration tool distribution.

server-migration-test.sh
------------------------
A shell script which runs a server migration, and if it succeeds then runs the target server's integration testsuite.

Usage: ./server-migration-test.sh SOURCE_DIST_DIR TARGET_SRC_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_SRC_DIR is path to base dir of a supported target server "source repository", if not an absolute path it's assumed to be relative to the script directory

Please note that both the migration tool, and the target server must have been built from source code, prior to execute the script.
