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

import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileDeploymentsMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSecurityRealmsMigration;
import org.jboss.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSubsystemsMigration;
import org.jboss.migration.wfly10.subsystem.AddExtension;
import org.jboss.migration.wfly10.subsystem.WildFly10Extension;
import org.jboss.migration.wfly10.subsystem.WildFly10ExtensionBuilder;
import org.jboss.migration.wfly10.subsystem.WildFly10ExtensionNames;
import org.jboss.migration.wfly10.subsystem.WildFly10LegacyExtensionBuilder;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemNames;
import org.jboss.migration.wfly10.subsystem.ejb3.WorkaroundForWFLY5520;
import org.jboss.migration.wfly10.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.subsystem.singleton.AddSingletonSubsystem;
import org.jboss.migration.wfly9.WildFly9Server;

import java.io.IOException;
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
                .addSubsystem(WildFly10SubsystemNames.MESSAGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.INFINISPAN)
                .addSubsystem(WildFly10SubsystemNames.INFINISPAN)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JGROUPS)
                .addSubsystem(WildFly10SubsystemNames.JGROUPS)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.CONNECTOR)
                .addSubsystem(WildFly10SubsystemNames.DATASOURCES)
                .addSubsystem(WildFly10SubsystemNames.JCA)
                .addSubsystem(WildFly10SubsystemNames.RESOURCE_ADAPTERS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.DEPLOYMENT_SCANNER)
                .addSubsystem(WildFly10SubsystemNames.DEPLOYMENT_SCANNER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EE)
                .addSubsystem(WildFly10SubsystemNames.EE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EJB3)
                .addSubsystem(WildFly10SubsystemNames.EJB3, WorkaroundForWFLY5520.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JAXRS)
                .addSubsystem(WildFly10SubsystemNames.JAXRS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JDR)
                .addSubsystem(WildFly10SubsystemNames.JDR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JMX)
                .addSubsystem(WildFly10SubsystemNames.JMX)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JPA)
                .addSubsystem(WildFly10SubsystemNames.JPA)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSF)
                .addSubsystem(WildFly10SubsystemNames.JSF)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.JSR77)
                .addSubsystem(WildFly10SubsystemNames.JSR77)
                .build()
        );
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.LOGGING)
                .addSubsystem(WildFly10SubsystemNames.LOGGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MAIL)
                .addSubsystem(WildFly10SubsystemNames.MAIL)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MODCLUSTER)
                .addSubsystem(WildFly10SubsystemNames.MODCLUSTER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.NAMING)
                .addSubsystem(WildFly10SubsystemNames.NAMING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.POJO)
                .addSubsystem(WildFly10SubsystemNames.POJO)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REMOTING)
                .addSubsystem(WildFly10SubsystemNames.REMOTING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SAR)
                .addSubsystem(WildFly10SubsystemNames.SAR)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY)
                .addSubsystem(WildFly10SubsystemNames.SECURITY)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.TRANSACTIONS)
                .addSubsystem(WildFly10SubsystemNames.TRANSACTIONS)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WEBSERVICES)
                .addSubsystem(WildFly10SubsystemNames.WEBSERVICES)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.WELD)
                .addSubsystem(WildFly10SubsystemNames.WELD)
                .build()
        );

        // batch jberet did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BATCH_JBERET)
                .addSubsystem(WildFly10SubsystemNames.BATCH_JBERET, AddExtension.INSTANCE, AddBatchJBeretSubsystem.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BEAN_VALIDATION)
                .addSubsystem(WildFly10SubsystemNames.BEAN_VALIDATION)
                .build()
        );

        // singleton did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SINGLETON)
                .addSubsystem(WildFly10SubsystemNames.SINGLETON, AddExtension.INSTANCE, AddSingletonSubsystem.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IO)
                .addSubsystem(WildFly10SubsystemNames.IO)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING_ACTIVEMQ)
                .addSubsystem(WildFly10SubsystemNames.MESSAGING_ACTIVEMQ)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REQUEST_CONTROLLER)
                .addSubsystem(WildFly10SubsystemNames.REQUEST_CONTROLLER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY_MANAGER)
                .addSubsystem(WildFly10SubsystemNames.SECURITY_MANAGER)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.UNDERTOW)
                .addSubsystem(WildFly10SubsystemNames.UNDERTOW)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IIOP_OPENJDK)
                .addSubsystem(WildFly10SubsystemNames.IIOP_OPENJDK)
                .build()
        );

        return Collections.unmodifiableList(supportedExtensions);
    }

    @Override
    protected void run(ServerPath<WildFly9Server> sourceConfig, WildFly10StandaloneServer standaloneServer, ServerMigrationContext context) throws IOException {
        new WildFly10StandaloneConfigFileSubsystemsMigration(SUPPORTED_EXTENSIONS).run(sourceConfig, standaloneServer, context);
        new WildFly10StandaloneConfigFileSecurityRealmsMigration().run(sourceConfig, standaloneServer, context);
        new WildFly10StandaloneConfigFileDeploymentsMigration().run(sourceConfig, standaloneServer, context);
    }
}
