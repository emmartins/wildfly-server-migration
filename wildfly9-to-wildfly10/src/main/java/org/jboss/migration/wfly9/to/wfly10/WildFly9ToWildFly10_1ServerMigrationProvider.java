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
package org.jboss.migration.wfly9.to.wfly10;

import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.update.AddApplicationRealmSSLServerIdentity;
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.AddSocketBindingMulticastAddressExpressions;
import org.jboss.migration.wfly10.config.task.update.AddSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensionsAndSubsystems;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;
import org.jboss.migration.wfly10.config.task.update.UpdateUnsecureInterface;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServerMigrationProvider10_1;
import org.jboss.migration.wfly9.WildFlyServer9;

/**
 * Server migration, from WildFly 9 to WildFly 10.1
 * @author emmartins
 */
public class WildFly9ToWildFly10_1ServerMigrationProvider implements WildFlyFullServerMigrationProvider10_1 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final ServerUpdate.Builders<WildFlyServer9> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(WildFly9ToWildFly10_1SubsystemUpdates.INFINISPAN)
                        .subtask(WildFly9ToWildFly10_1SubsystemUpdates.UNDERTOW)
                        .subtask(MigrateSubsystemTasks.MESSAGING)
                        .subtask(AddSubsystemTasks.BATCH_JBERET)
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
                                .subtask(WildFly9ToWildFly10_1SubsystemUpdates.INFINISPAN)
                                .subtask(WildFly9ToWildFly10_1SubsystemUpdates.UNDERTOW)
                                .subtask(MigrateSubsystemTasks.MESSAGING)
                                .subtask(AddSubsystemTasks.BATCH_JBERET)
                                .subtask(AddSubsystemTasks.SINGLETON)
                                .subtask(UpdateUnsecureInterface.INSTANCE)
                                .subtask(AddPrivateInterface.INSTANCE)
                                .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                                .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(serverUpdateBuilders.hostBuilder()
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
    public Class<WildFlyServer9> getSourceType() {
        return WildFlyServer9.class;
    }
}
