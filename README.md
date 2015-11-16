WildFly Server Migration
=================
TODO

Build
======

To build the tool from source do:

mvn install

A zip archive of the tool will be in build/target.
 
Run
======

Unpack the build archive and execute the server-migration.sh or .bat script, e.g.

server-migration.sh --source testsuite/src/main/resources/jboss-eap-6.4/standalone-full.xml --target ../wildfly/dist/target/wildfly-10.0.0.CR5-SNAPSHOT