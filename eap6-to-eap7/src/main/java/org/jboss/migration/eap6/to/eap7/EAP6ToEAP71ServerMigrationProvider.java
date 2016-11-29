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
package org.jboss.migration.eap6.to.eap7;

import org.jboss.migration.eap.EAP6Server;
import org.jboss.migration.eap.EAP71ServerMigrationProvider;
import org.jboss.migration.eap6.to.eap7.tasks.AddJmxSubsystemToHosts;
import org.jboss.migration.eap6.to.eap7.tasks.AddSocketBindingPortExpressions;
import org.jboss.migration.eap6.to.eap7.tasks.EAP71SubsystemUpdates;
import org.jboss.migration.eap6.to.eap7.tasks.SetupHttpUpgradeManagement;
import org.jboss.migration.eap6.to.eap7.tasks.UpdateUnsecureInterface;
import org.jboss.migration.wfly10.WildFly10ServerMigration;
import org.jboss.migration.wfly10.config.task.update.AddApplicationRealmSSLServerIdentity;
import org.jboss.migration.wfly10.config.task.update.AddPrivateInterface;
import org.jboss.migration.wfly10.config.task.update.AddSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateSubsystemTasks;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.RemovePermgenAttributesFromJVMs;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensionsAndSubsystems;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;

/**
 * Server migration, from EAP 6.4 to EAP 7.1.
 * @author emmartins
 */
public class EAP6ToEAP71ServerMigrationProvider implements EAP71ServerMigrationProvider {

    @Override
    public WildFly10ServerMigration getServerMigration() {
        final ServerUpdate.Builders<EAP6Server> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(MigrateSubsystemTasks.JACORB)
                        .subtask(MigrateSubsystemTasks.WEB)
                        .subtask(EAP71SubsystemUpdates.UNDERTOW)
                        .subtask(MigrateSubsystemTasks.MESSAGING)
                        .subtask(EAP71SubsystemUpdates.MESSAGING_ACTIVEMQ)
                        .subtask(EAP71SubsystemUpdates.INFINISPAN)
                        .subtask(EAP71SubsystemUpdates.EE)
                        .subtask(EAP71SubsystemUpdates.EJB3)
                        .subtask(EAP71SubsystemUpdates.REMOTING)
                        .subtask(AddSubsystemTasks.BATCH_JBERET)
                        .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                        .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                        .subtask(AddSubsystemTasks.SINGLETON)
                        .subtask(SetupHttpUpgradeManagement.INSTANCE)
                        .subtask(AddPrivateInterface.INSTANCE)
                        .subtask(AddSocketBindingPortExpressions.INSTANCE)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                                .subtask(MigrateSubsystemTasks.JACORB)
                                .subtask(MigrateSubsystemTasks.WEB)
                                .subtask(EAP71SubsystemUpdates.UNDERTOW)
                                .subtask(MigrateSubsystemTasks.MESSAGING)
                                .subtask(EAP71SubsystemUpdates.MESSAGING_ACTIVEMQ)
                                .subtask(EAP71SubsystemUpdates.INFINISPAN)
                                .subtask(EAP71SubsystemUpdates.EE)
                                .subtask(EAP71SubsystemUpdates.EJB3)
                                .subtask(EAP71SubsystemUpdates.REMOTING)
                                .subtask(AddSubsystemTasks.BATCH_JBERET)
                                .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                                .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                                .subtask(AddSubsystemTasks.SINGLETON)
                                .subtask(UpdateUnsecureInterface.INSTANCE)
                                .subtask(AddPrivateInterface.INSTANCE)
                                .subtask(AddSocketBindingPortExpressions.INSTANCE)
                                .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                                .build()
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(AddJmxSubsystemToHosts.INSTANCE)
                                        .subtask(UpdateUnsecureInterface.INSTANCE)
                                        .subtask(SetupHttpUpgradeManagement.INSTANCE)
                                        .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                                        .build()
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
