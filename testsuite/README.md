JBoss Server Migration Tool Testsuite
==========================================

The resources to test the server migration tool distribution.

server-migration-test.sh
------------------------
A shell script which runs a non-interactive server migration using the tool's standalone build, and if it succeeds then runs the target server's integration testsuite.

Usage: ./server-migration-test.sh SOURCE_DIST_DIR TARGET_SRC_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_SRC_DIR is path to base dir of a supported target server "source repository", if not an absolute path it's assumed to be relative to the script directory

Please note that both the migration tool, and the target server must have been built from source code, prior to execute the script.

server-migration-simple-test.sh
------------------------
A shell script which runs a non-interactive server migration using the tool's standalone build.

Usage: ./server-migration-simple-test.sh SOURCE_DIST_DIR TARGET_DIST_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_DIST_DIR is path to base dir of a supported target server "distribution", if not an absolute path it's assumed to be relative to the script directory

server-migration-simple-interactive-test.sh
------------------------
A shell script which runs an interactive server migration using the tool's standalone build

Usage: ./server-migration-simple-interactive-test.sh SOURCE_DIST_DIR TARGET_DIST_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_DIST_DIR is path to base dir of a supported target server "distribution", if not an absolute path it's assumed to be relative to the script directory

integrated-server-migration-test.sh
------------------------
A shell script which runs a non-interactive server migration using the tool's integrated in the target server distribution, and if it succeeds then runs the target server's integration testsuite.

Usage: ./integrated-server-migration-test.sh SOURCE_DIST_DIR TARGET_SRC_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_SRC_DIR is path to base dir of a supported target server "source repository", if not an absolute path it's assumed to be relative to the script directory

Please note that the target server must have been built from source code, prior to execute the script.
Please note also that to integrate a custom built tool in the target server it's required to change the root pom.xml of the target server sources,
to point to the built tool version, and then rebuild the target server.

integrated-server-migration-simple-test.sh
------------------------
A shell script which runs a non-interactive server migration using the tool's integrated in the target server distribution.

Usage: ./integrated-server-migration-simple-test.sh SOURCE_DIST_DIR TARGET_DIST_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_DIST_DIR is path to base dir of a supported target server "distribution", if not an absolute path it's assumed to be relative to the script directory

integrated-server-migration-simple-interactive-test.sh
------------------------
A shell script which runs an interactive server migration using the tool's integrated in the target server distribution.

Usage: ./integrated-server-migration-simple-interactive-test.sh SOURCE_DIST_DIR TARGET_DIST_DIR

SOURCE_DIST_DIR is path to base dir of a supported source server "distribution", if not an absolute path it's assumed to be relative to the script directory

TARGET_DIST_DIR is path to base dir of a supported target server "distribution", if not an absolute path it's assumed to be relative to the script directory
