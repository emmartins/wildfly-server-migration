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

package org.jboss.migration.wfly10.to.wfly10;

import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.subsystem.messaging.JMSBridgesModulesFinder;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateModules;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServer10_1;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServerMigrationProvider10_1;

/**
 * Server migration, from WFLY 10.1 to WFLY 10.1.
 * @author emmartins
 */
public class WildFly10_1ToWildFly10_1ServerMigrationProvider implements WildFlyFullServerMigrationProvider10_1 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final ServerUpdate.Builders<WildFlyServer10> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(new MigrateModules.Builder<WildFlyServer10>().modulesFinder(new JMSBridgesModulesFinder()).build())
                        .subtask(WildFly10_1ToWildFly10_1SubsystemUpdates.UNDERTOW)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(new MigrateModules.Builder<WildFlyServer10>().modulesFinder(new JMSBridgesModulesFinder()).build())
                                .subtask(WildFly10_1ToWildFly10_1SubsystemUpdates.UNDERTOW)
                                .subtask(RemoveDeployments.INSTANCE)
                                .build()
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                                        .build()
                                )
                        )
                )
                .build();
    }

    @Override
    public Class<WildFlyFullServer10_1> getSourceType() {
        return WildFlyFullServer10_1.class;
    }
}
