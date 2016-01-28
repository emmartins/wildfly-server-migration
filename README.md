WildFly Server Migration Tool
=================

The WildFly Server Migration Tool reads the server configuration and properties files for a previous release of WildFly or JBoss EAP, referred to as the source server, and migrates them to the latest release or target server.

Currently the following configuration files, which are located in the `standalone/configuration/` directory, are updated by the migration:

* standalone.xml
* standalone-ha.xml
* standalone-full.xml
* standalone-full-ha.xml
* application-users.properties
* application-roles.properties
* mgmt-users.properties
* mgmt-groups.properties



System Requirements
------------

* [Java 8.0 (Java SDK 1.8)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or later
* [Apache Maven](http://maven.apache.org/download.cgi)


Build the Server Migration Tool
------------

1. Use the following command to build the tool from source:

        mvn clean install

2. Navigate to the `build/target/` directory and unzip the ` wildfly-server-migration-VERSION_NUMBER.zip` file

        unzip  wildfly-server-migration-1.0.0.Alpha1-SNAPSHOT.zip

 
Run the Server Migration Tool
------------

1. Open a terminal and navigate to the `build/target/wildfly-server-migration/` directory.
2. Run the following command.

        For Linux:   ./server-migration.sh --source SOURCE_SERVER_PATH --target TARGET_SERVER_PATH
        For Windows: server-migration.bat --source SOURCE_SERVER_PATH --target SOURCE_SERVER_PATH

    Replace `SOURCE_SERVER_PATH` with the path to previous version of the server installation that you want to migrate from, for example:  `${user.home}/jboss-eap-6.4/`

    Replace `TARGET_SERVER_PATH` with the path to current version of the server installation that you want the old configuration migrated to, for example:  `${user.home}/jboss-eap-7.0/`
3. When you execute the command, the tool displays a list of the files to be migrated and then prompts you whether you want to continue.

        ----------------------------------------------------------
        ----  JBoss Server Migration Tool  -----------------------
        ----------------------------------------------------------

        Retrieving servers...
        INFO  [org.wildfly.migration.core.logger] (main) Processing SOURCE server's base dir /home/username/jboss-eap-6.4
        INFO  [org.wildfly.migration.core.logger] (main) SOURCE server name: EAP, version: 6.4.0.GA
        INFO  [org.wildfly.migration.core.logger] (main) Processing TARGET server's base dir /home/username/jboss-eap-7.0
        INFO  [org.wildfly.migration.core.logger] (main) TARGET server name: JBoss EAP, version: 7.0.0.GA

        ----------------------------------------------------------
        ----------------------------------------------------------

        Server migration starting...
        INFO  [org.wildfly.migration.core.logger] (main) Scanning for standalone server configurations...
        INFO  [org.wildfly.migration.core.logger] (main) /home/username/jboss-eap-6.4/standalone/configuration/standalone.xml
        INFO  [org.wildfly.migration.core.logger] (main) /home/username/jboss-eap-6.4/standalone/configuration/standalone-ha.xml
        INFO  [org.wildfly.migration.core.logger] (main) /home/username/jboss-eap-6.4/standalone/configuration/standalone-full.xml
        INFO  [org.wildfly.migration.core.logger] (main) /home/username/jboss-eap-6.4/standalone/configuration/standalone-full-ha.xml

        Migrate all configurations?
        yes/no? y

    Enter `y` to continue with the migration.
5. Review the output log. The log lists the name of the configuration file being converted followed by informational messages and any error messages.

   * The following message identifies the configuration file that is being converted.
    
            INFO  [org.wildfly.migration.core.logger] (main) Starting server server configuration standalone-ha.xml

   * Informational messages report information about the conversion of the named file, for example:
   
            INFO  [org.jboss.as.remoting] (Controller Boot Thread) WFLYRMT0024: The remoting subsystem is present but no io subsystem was found. An io subsystem was not required when remoting schema 'urn:jboss:domain:remoting:1.2' was current but now is, so a default subsystem is being added.
            INFO  [org.jboss.as.connector] (Controller Boot Thread) WFLYJCA0093: The 'enable' operation is deprecated. Use of the 'add' or 'remove' operations is preferred, or if required the 'write-attribute' operation can used to set the deprecated 'enabled' attribute
            INFO  [org.jboss.as.controller.management-deprecated] (Controller Boot Thread) WFLYCTL0028: Attribute 'default-clustered-sfsb-cache' in the resource at address '/subsystem=ejb3' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.
            INFO  [org.jboss.as.controller.management-deprecated] (Controller Boot Thread) WFLYCTL0028: Attribute 'default-stack' in the resource at address '/subsystem=jgroups' is deprecated, and may be removed in future version. See the attribute description in the output of the read-resource-description operation to learn more about the deprecation.

   * Error messages appear in red and describe the problem encountered in the migration and often how to resolve it. This error requires action.

            ERROR [org.jboss.as.ejb3] (Controller Boot Thread) WFLYEJB0474: Attribute 'default-clustered-sfsb-cache' is not supported on current version servers; it is only allowed if its value matches 'default-sfsb-cache'. This attribute should be removed.

    _NOTE:_ If you have any deployments to your old server configuration, you may see error messages similar to the following. 

        ERROR [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0057: No deployment content with hash 029f689e292c7cbe680ad330edd50440da51d7a3 is available in the deployment content repository for deployment jboss-mail.war. Because this Host Controller is booting in ADMIN-ONLY mode, boot will be allowed to proceed to provide administrators an opportunity to correct this problem. If this Host Controller were not in ADMIN-ONLY mode this would be a fatal boot failure.
6. Review the updated files in target server installation directory. Note the original configuration and properties files are backed up and now have the suffix `.beforeMigration`.



 
