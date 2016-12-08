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

package org.jboss.migration.eap7.to.eap7;

import org.jboss.migration.eap.EAPServer7_0;
import org.jboss.migration.eap.EAPServerMigrationProvider7_1;
import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.update.AddApplicationRealmSSLServerIdentity;
import org.jboss.migration.wfly10.config.task.update.AddSocketBindingMulticastAddressExpressions;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.RemoveDeployments;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensionsAndSubsystems;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;

/**
 * Server migration, from EAP 7.0 to EAP 7.1.
 * @author emmartins
 */
public class EAP7_0ToEAP7_1ServerMigrationProvider implements EAPServerMigrationProvider7_1 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final ServerUpdate.Builders serverUpdateBuilders = new ServerUpdate.Builders();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                        .subtask(EAP7_0ToEAP7_1SubsystemUpdates.UNDERTOW)
                        .subtask(EAP7_0ToEAP7_1SubsystemUpdates.INFINISPAN)
                        .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                        .subtask(RemoveDeployments.INSTANCE)
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(RemoveUnsupportedExtensionsAndSubsystems.INSTANCE)
                                .subtask(EAP7_0ToEAP7_1SubsystemUpdates.UNDERTOW)
                                .subtask(EAP7_0ToEAP7_1SubsystemUpdates.INFINISPAN)
                                .subtask(AddSocketBindingMulticastAddressExpressions.INSTANCE)
                                .subtask(RemoveDeployments.INSTANCE)
                                .build()
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(MigrateCompatibleSecurityRealms.INSTANCE)
                                        .subtask(AddApplicationRealmSSLServerIdentity.INSTANCE)
                                        .build()
                                )
                        )
                )
                .build();
    }

    @Override
    public Class<EAPServer7_0> getSourceType() {
        return EAPServer7_0.class;
    }
}
