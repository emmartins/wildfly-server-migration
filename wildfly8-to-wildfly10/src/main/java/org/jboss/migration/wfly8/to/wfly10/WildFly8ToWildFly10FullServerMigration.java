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
package org.jboss.migration.wfly8.to.wfly10;

import org.jboss.migration.wfly10.WildFly10ServerMigration;
import org.jboss.migration.wfly10.config.task.InterfacesMigration;
import org.jboss.migration.wfly10.config.task.JVMsMigration;
import org.jboss.migration.wfly10.config.task.SocketBindingGroupMigration;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemsMigration;
import org.jboss.migration.wfly10.config.task.subsystem.SupportedExtensions;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.AddServerCache;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.FixHibernateCacheModuleName;
import org.jboss.migration.wfly10.config.task.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.securitymanager.AddSecurityManagerSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.singleton.AddSingletonSubsystem;
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;
import org.jboss.migration.wfly10.dist.full.WildFly10FullServerMigrationProvider;
import org.jboss.migration.wfly8.WildFly8Server;

/**
 * Server migration, from WildFly 8 to WildFly 10.
 * @author emmartins
 */
public class WildFly8ToWildFly10FullServerMigration implements WildFly10FullServerMigrationProvider {

    @Override
    public WildFly10ServerMigration getServerMigration() {
        final ServerUpdate.Builders<WildFly8Server> serverUpdateBuilders = new ServerUpdate.Builders<>();
        final SubsystemsMigration subsystemsMigration = serverUpdateBuilders.subsystemsMigrationBuilder()
                .addExtensions(SupportedExtensions.allExcept(ExtensionNames.BATCH_JBERET, ExtensionNames.BEAN_VALIDATION, ExtensionNames.INFINISPAN, ExtensionNames.REQUEST_CONTROLLER, ExtensionNames.SECURITY_MANAGER, ExtensionNames.SINGLETON))
                .addExtension(new ExtensionBuilder(ExtensionNames.INFINISPAN).addUpdatedSubsystem(SubsystemNames.INFINISPAN, AddServerCache.INSTANCE, FixHibernateCacheModuleName.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.BEAN_VALIDATION).addNewSubsystem(SubsystemNames.BEAN_VALIDATION, AddSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.BATCH_JBERET).addNewSubsystem(SubsystemNames.BATCH_JBERET, AddBatchJBeretSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.REQUEST_CONTROLLER).addNewSubsystem(SubsystemNames.REQUEST_CONTROLLER))
                .addExtension(new ExtensionBuilder(ExtensionNames.SECURITY_MANAGER).addNewSubsystem(SubsystemNames.SECURITY_MANAGER, AddSecurityManagerSubsystem.INSTANCE))
                .addExtension(new ExtensionBuilder(ExtensionNames.SINGLETON).addNewSubsystem(SubsystemNames.SINGLETON, AddSingletonSubsystem.INSTANCE))
                .build();
        final JVMsMigration jvMsMigration = serverUpdateBuilders.jvmsMigrationBuilder()
                .subtask(new RemovePermgenAttributesFromJVMs())
                .build();
        final InterfacesMigration interfacesMigration = serverUpdateBuilders.interfacesMigrationBuilder()
                .subtask(new AddPrivateInterface.InterfacesSubtaskFactory())
                .build();
        final SocketBindingGroupMigration socketBindingGroupMigration = serverUpdateBuilders.socketBindingGroupMigrationBuilder()
                .socketBindingsMigration(serverUpdateBuilders.socketBindingsMigrationBuilder()
                        .subtask(new AddPrivateInterface.SocketBindingsSubtaskFactory()))
                .build();
        return new ServerUpdate.Builder<WildFly8Server>()
                .standaloneMigration(serverUpdateBuilders.standaloneConfigurationMigrationBuilder()
                        .subsystemsMigration(subsystemsMigration)
                        .interfacesMigration(interfacesMigration)
                        .socketBindingGroupsMigration(socketBindingGroupMigration)
                )
                .domainMigration(serverUpdateBuilders.domainBuilder()
                        .domainConfigurationsMigration(serverUpdateBuilders.domainConfigurationMigrationBuilder()
                                .subsystemsMigration(subsystemsMigration)
                                .profilesMigration(subsystemsMigration)
                                .interfacesMigration(interfacesMigration)
                                .socketBindingGroupsMigration(socketBindingGroupMigration)
                                .serverGroupsMigration(serverUpdateBuilders.serverGroupMigrationBuilder()
                                        .jvmsMigration(jvMsMigration)
                                )
                        )
                        .hostConfigurationsMigration(serverUpdateBuilders.hostConfigurationMigrationBuilder()
                                .hostUpdate(serverUpdateBuilders.hostUpdateBuilder()
                                        .jvmsMigration(jvMsMigration)
                                )
                        )
                )
                .build();
    }

    @Override
    public Class<WildFly8Server> getSourceType() {
        return WildFly8Server.class;
    }
}
