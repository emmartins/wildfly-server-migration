/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly9.to.wfly10;

import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileDeploymentsMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSecurityRealmsMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10ExtensionBuilder;
import org.jboss.migration.wfly10.subsystem.WildFly10ExtensionNames;
import org.jboss.migration.wfly10.subsystem.WildFly10LegacyExtensionBuilder;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemNames;
import org.jboss.migration.wfly10.subsystem.ejb3.WorkaroundForWFLY5520;
import org.jboss.migration.wfly10.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.subsystem.singleton.AddSingletonSubsystem;
import org.jboss.migration.wfly9.WildFly9Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standalone config file migration, from WildFly 9 to WildFly 10
 * @author emmartins
 */
public class WildFly9ToWildFly10FullStandaloneConfigFileMigration extends WildFly10StandaloneConfigFileMigration<WildFly9Server> {

    private static final List<WildFly10Extension> SUPPORTED_EXTENSIONS = initSupportedExtensions();

    private static List<WildFly10Extension> initSupportedExtensions() {

        List<WildFly10Extension> supportedExtensions = new ArrayList<>();

        // note: order may be relevant since extension/subsystem migration process order follows it

        // messaging is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING)
                .addMigratedSubsystem(WildFly10SubsystemNames.MESSAGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.INFINISPAN)
                .addSupportedSubsystem(WildFly10SubsystemNames.INFINISPAN)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JGROUPS)
                .addSupportedSubsystem(WildFly10SubsystemNames.JGROUPS)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.CONNECTOR)
                .addSupportedSubsystem(WildFly10SubsystemNames.DATASOURCES)
                .addSupportedSubsystem(WildFly10SubsystemNames.JCA)
                .addSupportedSubsystem(WildFly10SubsystemNames.RESOURCE_ADAPTERS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.DEPLOYMENT_SCANNER)
                .addSupportedSubsystem(WildFly10SubsystemNames.DEPLOYMENT_SCANNER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EE)
                .addSupportedSubsystem(WildFly10SubsystemNames.EE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EJB3)
                .addUpdatedSubsystem(WildFly10SubsystemNames.EJB3, WorkaroundForWFLY5520.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JAXRS)
                .addSupportedSubsystem(WildFly10SubsystemNames.JAXRS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JDR)
                .addSupportedSubsystem(WildFly10SubsystemNames.JDR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JMX)
                .addSupportedSubsystem(WildFly10SubsystemNames.JMX)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JPA)
                .addSupportedSubsystem(WildFly10SubsystemNames.JPA)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSF)
                .addSupportedSubsystem(WildFly10SubsystemNames.JSF)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSR77)
                .addSupportedSubsystem(WildFly10SubsystemNames.JSR77)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.LOGGING)
                .addSupportedSubsystem(WildFly10SubsystemNames.LOGGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MAIL)
                .addSupportedSubsystem(WildFly10SubsystemNames.MAIL)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MODCLUSTER)
                .addSupportedSubsystem(WildFly10SubsystemNames.MODCLUSTER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.NAMING)
                .addSupportedSubsystem(WildFly10SubsystemNames.NAMING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.POJO)
                .addSupportedSubsystem(WildFly10SubsystemNames.POJO)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REMOTING)
                .addSupportedSubsystem(WildFly10SubsystemNames.REMOTING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SAR)
                .addSupportedSubsystem(WildFly10SubsystemNames.SAR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY)
                .addSupportedSubsystem(WildFly10SubsystemNames.SECURITY)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.TRANSACTIONS)
                .addSupportedSubsystem(WildFly10SubsystemNames.TRANSACTIONS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WEBSERVICES)
                .addSupportedSubsystem(WildFly10SubsystemNames.WEBSERVICES)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WELD)
                .addSupportedSubsystem(WildFly10SubsystemNames.WELD)
                .build()
        );

        // batch jberet did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BATCH_JBERET)
                .addNewSubsystem(WildFly10SubsystemNames.BATCH_JBERET, AddBatchJBeretSubsystem.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BEAN_VALIDATION)
                .addSupportedSubsystem(WildFly10SubsystemNames.BEAN_VALIDATION)
                .build()
        );

        // singleton did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SINGLETON)
                .addNewSubsystem(WildFly10SubsystemNames.SINGLETON, AddSingletonSubsystem.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IO)
                .addSupportedSubsystem(WildFly10SubsystemNames.IO)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING_ACTIVEMQ)
                .addSupportedSubsystem(WildFly10SubsystemNames.MESSAGING_ACTIVEMQ)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REQUEST_CONTROLLER)
                .addSupportedSubsystem(WildFly10SubsystemNames.REQUEST_CONTROLLER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY_MANAGER)
                .addSupportedSubsystem(WildFly10SubsystemNames.SECURITY_MANAGER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.UNDERTOW)
                .addSupportedSubsystem(WildFly10SubsystemNames.UNDERTOW)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IIOP_OPENJDK)
                .addSupportedSubsystem(WildFly10SubsystemNames.IIOP_OPENJDK)
                .build()
        );

        return Collections.unmodifiableList(supportedExtensions);
    }

    @Override
    protected void run(ServerPath<WildFly9Server> sourceConfig, WildFly10StandaloneServer standaloneServer, ServerMigrationTaskContext context) {
        context.execute(new WildFly10StandaloneConfigFileSubsystemsMigration(SUPPORTED_EXTENSIONS).getServerMigrationTask(sourceConfig, standaloneServer));
        context.execute(new WildFly10StandaloneConfigFileSecurityRealmsMigration().getServerMigrationTask(sourceConfig, standaloneServer));
        context.execute(new WildFly10StandaloneConfigFileDeploymentsMigration().getServerMigrationTask(sourceConfig, standaloneServer));
    }
}
