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
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.AddSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensionsAndSubsystems;
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
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(MigrateSubsystemTasks.JACORB)
                        .subtask(MigrateSubsystemTasks.MESSAGING)
                        .subtask(SubsystemUpdates.INFINISPAN)
                        .subtask(AddSubsystemTasks.BATCH_JBERET)
                        .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                        .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                        .subtask(AddSubsystemTasks.SINGLETON)
                        .subtask(AddPrivateInterface.INSTANCE)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                                .subtask(MigrateSubsystemTasks.JACORB)
                                .subtask(MigrateSubsystemTasks.MESSAGING)
                                .subtask(SubsystemUpdates.INFINISPAN)
                                .subtask(AddSubsystemTasks.BATCH_JBERET)
                                .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                                .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                                .subtask(AddSubsystemTasks.SINGLETON)
                                .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                .subtask(AddPrivateInterface.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
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
