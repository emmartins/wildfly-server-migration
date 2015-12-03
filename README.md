Server Migration Tool
=================

A tool to migrate WildFly/EAP servers.

Build
======

To build the tool from source do:

mvn install

A zip archive of the tool will be in build/target.
 
Run
======

Unpack the build archive and execute the server-migration.sh or .bat script, e.g.

server-migration.sh --source testsuite/src/main/resources/jboss-eap-6.4 --target ../wildfly/dist/target/wildfly-10.0.0.CR5-SNAPSHOT