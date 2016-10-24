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
package org.jboss.migration.eap6.to.eap7;

import org.jboss.migration.core.ServerPath;
import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.eap.EAP7ServerMigrationProvider;
import org.jboss.migration.eap6.to.eap7.tasks.SetSocketBindingPortExpressions;
import org.jboss.migration.eap6.to.eap7.tasks.SetUnsecureInterfaceInetAddress;
import org.jboss.migration.eap6.to.eap7.tasks.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.eap6.to.eap7.tasks.EnableHttpInterfaceSupportForHttpUpgrade;
import org.jboss.migration.eap6.to.eap7.tasks.UpdateManagementHttpsSocketBinding;
import org.jboss.migration.eap6.to.eap7.tasks.AddJmxSubsystemToHosts;
import org.jboss.migration.wfly10.WildFly10ServerMigration;
import org.jboss.migration.wfly10.config.task.JVMsMigration;
import org.jboss.migration.wfly10.config.task.ManagementInterfacesMigration;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;
import org.jboss.migration.wfly10.config.task.subsystem.SupportedExtensions;
import org.jboss.migration.wfly10.config.task.subsystem.ee.AddConcurrencyUtilitiesDefaultConfig;
import org.jboss.migration.wfly10.config.task.subsystem.ee.AddDefaultBindingsConfig;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.AddInfinispanPassivationStoreAndDistributableCache;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.DefinePassivationDisabledCacheRef;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.RefHttpRemotingConnectorInEJB3Remote;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.AddEjbCache;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.AddServerCache;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.FixHibernateCacheModuleName;
import org.jboss.migration.wfly10.config.task.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.messaging.AddHttpAcceptorsAndConnectors;
import org.jboss.migration.wfly10.config.task.subsystem.remoting.AddHttpConnectorIfMissing;
import org.jboss.migration.wfly10.config.task.subsystem.securitymanager.AddSecurityManagerSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.singleton.AddSingletonSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.AddBufferCache;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.AddWebsockets;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.MigrateHttpListener;
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;

/**
 * Server migration, from EAP 6 to EAP 7.
 * @author emmartins
 */
public class EAP6ToEAP7ServerMigrationProvider implements EAP7ServerMigrationProvider {

    @Override
    public WildFly10ServerMigration getServerMigration() {
        final ServerUpdate.Builders<EAP6Server> serverUpdateBuilders = new ServerUpdate.Builders<>();
        final SubsystemsMigration<ServerPath<EAP6Server>> subsystemsMigration = serverUpdateBuilders.subsystemsMigrationBuilder()
                .addExtensions(SupportedExtensions.allExcept(ExtensionNames.BATCH_JBERET, ExtensionNames.BEAN_VALIDATION, ExtensionNames.EE, ExtensionNames.EJB3, ExtensionNames.INFINISPAN, ExtensionNames.MESSAGING_ACTIVEMQ, ExtensionNames.REMOTING, ExtensionNames.REQUEST_CONTROLLER, ExtensionNames.SECURITY_MANAGER, ExtensionNames.SINGLETON, ExtensionNames.UNDERTOW))
                .addExtension(new ExtensionBuilder(ExtensionNames.INFINISPAN).addUpdatedSubsystem(SubsystemNames.INFINISPAN, AddServerCache.INSTANCE, AddEjbCache.INSTANCE, FixHibernateCacheModuleName.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.BEAN_VALIDATION).addNewSubsystem(SubsystemNames.BEAN_VALIDATION, AddSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.BATCH_JBERET).addNewSubsystem(SubsystemNames.BATCH_JBERET, AddBatchJBeretSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.EE).addUpdatedSubsystem(SubsystemNames.EE, AddConcurrencyUtilitiesDefaultConfig.INSTANCE, AddDefaultBindingsConfig.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.EJB3).addUpdatedSubsystem(SubsystemNames.EJB3, RefHttpRemotingConnectorInEJB3Remote.INSTANCE, DefinePassivationDisabledCacheRef.INSTANCE, AddInfinispanPassivationStoreAndDistributableCache.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.REMOTING).addUpdatedSubsystem(SubsystemNames.REMOTING, AddHttpConnectorIfMissing.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.REQUEST_CONTROLLER).addNewSubsystem(SubsystemNames.REQUEST_CONTROLLER))
                .addExtension(new ExtensionBuilder(ExtensionNames.SECURITY_MANAGER).addNewSubsystem(SubsystemNames.SECURITY_MANAGER, AddSecurityManagerSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.SINGLETON).addNewSubsystem(SubsystemNames.SINGLETON, AddSingletonSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.UNDERTOW).addUpdatedSubsystem(SubsystemNames.UNDERTOW, AddBufferCache.INSTANCE, MigrateHttpListener.INSTANCE, AddWebsockets.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.MESSAGING_ACTIVEMQ).addUpdatedSubsystem(SubsystemNames.MESSAGING_ACTIVEMQ, AddHttpAcceptorsAndConnectors.INSTANCE))
                .build();
        final JVMsMigration<ServerPath<EAP6Server>> jvMsMigration = serverUpdateBuilders.jvmsMigrationBuilder()
                .addSubtaskFactory(new RemovePermgenAttributesFromJVMs())
                .build();
        final ManagementInterfacesMigration<ServerPath<EAP6Server>> managementInterfacesMigration = serverUpdateBuilders.managementInterfacesMigrationBuilder()
                .addSubtaskFactory(new EnableHttpInterfaceSupportForHttpUpgrade())
                .build();
        return new ServerUpdate.Builder<EAP6Server>()
                .standaloneMigration(serverUpdateBuilders.standaloneConfigurationMigrationBuilder()
                        .subsystemsMigration(subsystemsMigration)
                        .managementInterfacesMigration(managementInterfacesMigration)
                        .interfacesMigration(serverUpdateBuilders.interfacesMigrationBuilder()
                                .addSubtaskFactory(new AddPrivateInterface.InterfacesSubtaskFactory<EAP6Server>())
                        )
                        .socketBindingGroupsMigration(serverUpdateBuilders.socketBindingGroupMigrationBuilder()
                                .addSocketBindingsMigration(serverUpdateBuilders.socketBindingsMigrationBuilder()
                                        .addSubtaskFactory(new SetSocketBindingPortExpressions("ajp", "http", "https"))
                                        .addSubtaskFactory(new UpdateManagementHttpsSocketBinding())
                                        .addSubtaskFactory(new AddPrivateInterface.SocketBindingsSubtaskFactory<EAP6Server>())
                                )
                        )
                )
                .domainMigration(serverUpdateBuilders.domainBuilder()
                        .domainConfigurationsMigration(serverUpdateBuilders.domainConfigurationMigrationBuilder()
                                .subsystemsMigration(subsystemsMigration)
                                .profilesMigration(subsystemsMigration)
                                .interfacesMigration(serverUpdateBuilders.interfacesMigrationBuilder()
                                        .addSubtaskFactory(new SetUnsecureInterfaceInetAddress())
                                        .addSubtaskFactory(new AddPrivateInterface.InterfacesSubtaskFactory<EAP6Server>())
                                )
                                .serverGroupsMigration(serverUpdateBuilders.serverGroupMigrationBuilder()
                                        .addJVMsMigration(jvMsMigration)
                                )
                                .socketBindingGroupsMigration(serverUpdateBuilders.socketBindingGroupMigrationBuilder()
                                        .addSocketBindingsMigration(serverUpdateBuilders.socketBindingsMigrationBuilder()
                                                .addSubtaskFactory(new SetSocketBindingPortExpressions("ajp", "http", "https"))
                                                .addSubtaskFactory(new AddPrivateInterface.SocketBindingsSubtaskFactory<EAP6Server>())
                                        )
                                )
                        )
                        .hostConfigurationsMigration(serverUpdateBuilders.hostConfigurationMigrationBuilder()
                                .hostUpdate(serverUpdateBuilders.hostUpdateBuilder()
                                        .subsystemsMigration(serverUpdateBuilders.subsystemsMigrationBuilder()
                                                .addExtension(new ExtensionBuilder(ExtensionNames.JMX).addNewSubsystem(SubsystemNames.JMX, AddJmxSubsystemToHosts.INSTANCE)))
                                        .managementInterfacesMigration(managementInterfacesMigration)
                                        .jvMsMigration(jvMsMigration)
                                )
                        )
                )
                .build();
    }

    @Override
    public Class<EAP6Server> getSourceType() {
        return EAP6Server.class;
    }
}
