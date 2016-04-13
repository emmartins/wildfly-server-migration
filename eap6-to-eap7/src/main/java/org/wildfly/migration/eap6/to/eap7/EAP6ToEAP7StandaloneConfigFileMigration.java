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
package org.wildfly.migration.eap6.to.eap7;

import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.ServerPath;
import org.wildfly.migration.eap.EAP6Server;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.wildfly.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileDeploymentsMigration;
import org.wildfly.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileMigration;
import org.wildfly.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSecurityRealmsMigration;
import org.wildfly.migration.wfly10.standalone.config.WildFly10StandaloneConfigFileSubsystemsMigration;
import org.wildfly.migration.wfly10.subsystem.AddExtension;
import org.wildfly.migration.wfly10.subsystem.AddSubsystemWithoutConfig;
import org.wildfly.migration.wfly10.subsystem.WildFly10Extension;
import org.wildfly.migration.wfly10.subsystem.WildFly10ExtensionBuilder;
import org.wildfly.migration.wfly10.subsystem.WildFly10ExtensionNames;
import org.wildfly.migration.wfly10.subsystem.WildFly10LegacyExtensionBuilder;
import org.wildfly.migration.wfly10.subsystem.WildFly10SubsystemNames;
import org.wildfly.migration.wfly10.subsystem.ee.AddConcurrencyUtilitiesDefaultConfig;
import org.wildfly.migration.wfly10.subsystem.ee.AddDefaultBindingsConfig;
import org.wildfly.migration.wfly10.subsystem.ejb3.DefinePassivationDisabledCacheRef;
import org.wildfly.migration.wfly10.subsystem.ejb3.RefHttpRemotingConnectorInEJB3Remote;
import org.wildfly.migration.wfly10.subsystem.infinispan.AddEjbCache;
import org.wildfly.migration.wfly10.subsystem.infinispan.AddServerCache;
import org.wildfly.migration.wfly10.subsystem.infinispan.FixHibernateCacheModuleName;
import org.wildfly.migration.wfly10.subsystem.jberet.AddBatchJBeretSubsystem;
import org.wildfly.migration.wfly10.subsystem.messaging.AddHttpAcceptorsAndConnectors;
import org.wildfly.migration.wfly10.subsystem.remoting.AddHttpConnectorIfMissing;
import org.wildfly.migration.wfly10.subsystem.securitymanager.AddSecurityManagerSubsystem;
import org.wildfly.migration.wfly10.subsystem.undertow.AddBufferCache;
import org.wildfly.migration.wfly10.subsystem.undertow.AddWebsockets;
import org.wildfly.migration.wfly10.subsystem.undertow.MigrateHttpListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standalone config file migration, from EAP 6 to EAP 7
 * @author emmartins
 */
public class EAP6ToEAP7StandaloneConfigFileMigration extends WildFly10StandaloneConfigFileMigration<EAP6Server> {

    private static final List<WildFly10Extension> SUPPORTED_EXTENSIONS = initSupportedExtensions();

    private static List<WildFly10Extension> initSupportedExtensions() {

        List<WildFly10Extension> supportedExtensions = new ArrayList<>();

        // note: order may be relevant since extension/subsystem migration process order follows it

        // jacorb is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.JACORB)
                .addSubsystem(WildFly10SubsystemNames.JACORB)
                .build()
        );

        // web is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.WEB)
                .addSubsystem(WildFly10SubsystemNames.WEB)
                .build()
        );
        // messaging is legacy
        supportedExtensions.add(new WildFly10LegacyExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING)
                .addSubsystem(WildFly10SubsystemNames.MESSAGING)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.INFINISPAN)
                .addSubsystem(WildFly10SubsystemNames.INFINISPAN, AddServerCache.INSTANCE, AddEjbCache.INSTANCE, FixHibernateCacheModuleName.INSTANCE)
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

        // On EAP 6, the EE subsystem config has no Concurrency Utilities and Default Bindings, thus the need for a customized  migration EE Extension, which adds these
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EE)
                .addSubsystem(WildFly10SubsystemNames.EE, AddConcurrencyUtilitiesDefaultConfig.INSTANCE, AddDefaultBindingsConfig.INSTANCE)
                .build()
        );

        // set ejb3 remote to http remoting
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.EJB3)
                .addSubsystem(WildFly10SubsystemNames.EJB3, RefHttpRemotingConnectorInEJB3Remote.INSTANCE, DefinePassivationDisabledCacheRef.INSTANCE)
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

        // http connector must be added to remoting, if missing
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REMOTING)
                .addSubsystem(WildFly10SubsystemNames.REMOTING, AddHttpConnectorIfMissing.INSTANCE)
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

        // bean-validation did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.BEAN_VALIDATION)
                .addSubsystem(WildFly10SubsystemNames.BEAN_VALIDATION, AddExtension.INSTANCE, AddSubsystemWithoutConfig.INSTANCE)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SINGLETON)
                .addSubsystem(WildFly10SubsystemNames.SINGLETON)
                .build()
        );

        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.IO)
                .addSubsystem(WildFly10SubsystemNames.IO)
                .build()
        );

        // request-controller did not exist in source server, need tasks to create extension and subsystem default config
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.REQUEST_CONTROLLER)
                .addSubsystem(WildFly10SubsystemNames.REQUEST_CONTROLLER, AddExtension.INSTANCE, AddSubsystemWithoutConfig.INSTANCE)
                .build()
        );

        // add new subsystem security manager
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.SECURITY_MANAGER)
                .addSubsystem(WildFly10SubsystemNames.SECURITY_MANAGER, AddExtension.INSTANCE, AddSecurityManagerSubsystem.INSTANCE)
                .build()
        );

        // wrt undertow add buffer cache and web sockets, and migrate http listener
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.UNDERTOW)
                .addSubsystem(WildFly10SubsystemNames.UNDERTOW, AddBufferCache.INSTANCE, MigrateHttpListener.INSTANCE, AddWebsockets.INSTANCE)
                .build()
        );

        // add default http-acceptors and http-connectors to messaging
        supportedExtensions.add(new WildFly10ExtensionBuilder()
                .setName(WildFly10ExtensionNames.MESSAGING_ACTIVEMQ)
                .addSubsystem(WildFly10SubsystemNames.MESSAGING_ACTIVEMQ, AddHttpAcceptorsAndConnectors.INSTANCE)
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
    protected void run(ServerPath<EAP6Server> sourceConfig, WildFly10StandaloneServer standaloneServer, ServerMigrationContext context) throws IOException {
        new WildFly10StandaloneConfigFileSubsystemsMigration(SUPPORTED_EXTENSIONS).run(sourceConfig, standaloneServer, context);
        new WildFly10StandaloneConfigFileSecurityRealmsMigration().run(sourceConfig, standaloneServer, context);
        new EAP6ToEAP7StandaloneConfigFileManagementInterfacesMigration().run(standaloneServer, context);
        new EAP6ToEAP7StandaloneConfigFileSocketBindingsMigration().run(standaloneServer, context);
        new WildFly10StandaloneConfigFileDeploymentsMigration().run(sourceConfig, standaloneServer, context);
    }
}
