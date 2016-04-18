JBoss Server Migration Tool
=================

A tool to migrate JBoss Application Servers.

Build
======

To build the tool from source code do:

mvn install

A zip archive of the tool will be in build/target.
 
Run
======

Unpack the build archive and execute the server-migration.sh or .bat script, e.g.

./jboss-server-migration/server-migration.sh --source ../../dist/jboss-eap-6.4 --target ../../dist/jboss-eap-7.0

where --source points to the base dir of the migration source server, and --target points to the base dir of the target server.