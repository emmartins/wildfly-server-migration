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

import org.jboss.migration.eap.EAPServer6_4;
import org.jboss.migration.eap.EAPServerMigrationProvider7_1;
import org.jboss.migration.wfly10.config.task.module.ConfigurationModulesMigrationTaskFactory;
import org.jboss.migration.wfly10.to.wfly10.AddLoadBalancerProfile;
import org.jboss.migration.wfly10.config.task.update.AddJmxSubsystemToHosts;
import org.jboss.migration.eap6.to.eap7.tasks.AddSocketBindingPortExpressions;
import org.jboss.migration.eap6.to.eap7.tasks.EAPSubsystemUpdates7_1;
import org.jboss.migration.eap6.to.eap7.tasks.SetupHttpUpgradeManagement;
import org.jboss.migration.wfly10.config.task.update.UpdateUnsecureInterface;
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

/**
 * Server migration, from EAP 6.4 to EAP 7.1.
 * @author emmartins
 */
public class EAP6_4ToEAP7_1ServerMigrationProvider implements EAPServerMigrationProvider7_1 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final ServerUpdate.Builders<EAPServer6_4> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
                        .subtask(MigrateSubsystemTasks.JACORB)
                        .subtask(MigrateSubsystemTasks.WEB)
                        .subtask(EAPSubsystemUpdates7_1.UNDERTOW)
                        .subtask(MigrateSubsystemTasks.MESSAGING)
                        .subtask(EAPSubsystemUpdates7_1.MESSAGING_ACTIVEMQ)
                        .subtask(EAPSubsystemUpdates7_1.INFINISPAN)
                        .subtask(EAPSubsystemUpdates7_1.EE)
                        .subtask(EAPSubsystemUpdates7_1.EJB3)
                        .subtask(EAPSubsystemUpdates7_1.REMOTING)
                        .subtask(AddSubsystemTasks.BATCH_JBERET)
                        .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                        .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                        .subtask(AddSubsystemTasks.SINGLETON)
                        .subtask(SetupHttpUpgradeManagement.INSTANCE)
                        .subtask(AddPrivateInterface.INSTANCE)
                        .subtask(AddSocketBindingPortExpressions.INSTANCE)
                        .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                                .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
                                .subtask(MigrateSubsystemTasks.JACORB)
                                .subtask(MigrateSubsystemTasks.WEB)
                                .subtask(EAPSubsystemUpdates7_1.UNDERTOW)
                                .subtask(MigrateSubsystemTasks.MESSAGING)
                                .subtask(EAPSubsystemUpdates7_1.MESSAGING_ACTIVEMQ)
                                .subtask(EAPSubsystemUpdates7_1.INFINISPAN)
                                .subtask(EAPSubsystemUpdates7_1.EE)
                                .subtask(EAPSubsystemUpdates7_1.EJB3)
                                .subtask(EAPSubsystemUpdates7_1.REMOTING)
                                .subtask(AddSubsystemTasks.BATCH_JBERET)
                                .subtask(AddSubsystemTasks.REQUEST_CONTROLLER)
                                .subtask(AddSubsystemTasks.SECURITY_MANAGER)
                                .subtask(AddSubsystemTasks.SINGLETON)
                                .subtask(UpdateUnsecureInterface.INSTANCE)
                                .subtask(AddPrivateInterface.INSTANCE)
                                .subtask(AddSocketBindingPortExpressions.INSTANCE)
                                .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                                .subtask(AddLoadBalancerProfile.INSTANCE)
                                .subtask(RemovePermgenAttributesFromJVMs.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                                .build()
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(ConfigurationModulesMigrationTaskFactory.TASK_WITH_ALL_DEFAULT_MODULE_FINDERS)
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
    public Class<EAPServer6_4> getSourceType() {
        return EAPServer6_4.class;
    }
}
