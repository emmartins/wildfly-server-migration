/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly8.to.wfly10;

import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.module.ConfigurationModulesMigrationTaskFactory;
import org.jboss.migration.wfly10.config.task.update.AddApplicationRealmSSLServerIdentity;
import org.jboss.migration.wfly10.config.task.update.AddJmxSubsystemToHosts;
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.AddSocketBindingMulticastAddressExpressions;
import org.jboss.migration.wfly10.config.task.update.AddSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensionsAndSubsystems;
import org.jboss.migration.wfly10.config.task.update.CompositeServerUpdate;
import org.jboss.migration.wfly10.config.task.update.UpdateUnsecureInterface;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServerMigrationProvider10_1;
import org.jboss.migration.wfly10.to.wfly10.AddLoadBalancerProfileTaskBuilder;
import org.jboss.migration.wfly8.WildFlyServer8;

/**
 * Server migration, from WildFly 8 to WildFly 10.1.
 * @author emmartins
 */
public class WildFly8ToWildFly10_1ServerMigrationProvider implements WildFlyFullServerMigrationProvider10_1 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final CompositeServerUpdate.Builders<WildFlyServer8> serverUpdateBuilders = new CompositeServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
                        .subtask(WildFly8ToWildFly10_1SubsystemUpdates.INFINISPAN)
                        .subtask(WildFly8ToWildFly10_1SubsystemUpdates.UNDERTOW)
                        .subtask(MigrateSubsystemTasks.JACORB)
                        .subtask(MigrateSubsystemTasks.MESSAGING)
                        .subtask(AddSubsystemTasks.BATCH_JBERET)
                        .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                        .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                        .subtask(AddSubsystemTasks.SINGLETON)
                        .subtask(AddPrivateInterface.INSTANCE)
                        .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                                .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
                                .subtask(WildFly8ToWildFly10_1SubsystemUpdates.INFINISPAN)
                                .subtask(WildFly8ToWildFly10_1SubsystemUpdates.UNDERTOW)
                                .subtask(MigrateSubsystemTasks.JACORB)
                                .subtask(MigrateSubsystemTasks.MESSAGING)
                                .subtask(AddSubsystemTasks.BATCH_JBERET)
                                .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                                .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                                .subtask(AddSubsystemTasks.SINGLETON)
                                .subtask(UpdateUnsecureInterface.INSTANCE)
                                .subtask(AddPrivateInterface.INSTANCE)
                                .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                                .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                .subtask(AddLoadBalancerProfileTaskBuilder.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(AddJmxSubsystemToHosts.INSTANCE)
                                        .subtask(UpdateUnsecureInterface.INSTANCE)
                                        .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                                )
                        )
                )
                .build();
    }

    @Override
    public Class<WildFlyServer8> getSourceType() {
        return WildFlyServer8.class;
    }
}
