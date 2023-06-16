/*
 * Copyright 2022 Red Hat, Inc.
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

import org.jboss.migration.eap.EAPXPServer7_3;
import org.jboss.migration.eap.EAPXPServerMigrationProvider8_0;
import org.jboss.migration.eap.task.hostexclude.EAP8_0AddHostExcludes;
import org.jboss.migration.eap.task.subsystem.metrics.EAP8_0AddMetricsSubsystem;
import org.jboss.migration.wfly.task.paths.WildFly26_0MigrateReferencedPaths;
import org.jboss.migration.wfly.task.security.LegacySecurityConfigurationMigration;
import org.jboss.migration.wfly.task.subsystem.health.WildFly26_0AddHealthSubsystem;
import org.jboss.migration.wfly.task.subsystem.keycloak.MigrateKeycloakSubsystem;
import org.jboss.migration.wfly.task.subsystem.microprofile.WildFly26_0AddMicroprofileJwtSmallryeSubsystem;
import org.jboss.migration.wfly.task.subsystem.picketlink.MigratePicketLinkSubsystem;
import org.jboss.migration.wfly.task.xml.WildFly26_0MigrateVault;
import org.jboss.migration.wfly.task.xml.WildFly27_0MigrateJBossDomainProperties;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.module.MigrateReferencedModules;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensions;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedSubsystems;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;

/**
 * Server migration, from EAP XP 7.3 to EAP XP 8.0.
 * @author emmartins
 */
public class EAPXP7_3ToEAPXP8_0ServerMigrationProvider implements EAPXPServerMigrationProvider8_0 {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final LegacySecurityConfigurationMigration<WildFlyServer10> legacySecurityConfigurationMigration = new LegacySecurityConfigurationMigration<>();
        final ServerUpdate.Builders<WildFlyServer10> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(new WildFly27_0MigrateJBossDomainProperties<>())
                        .subtask(legacySecurityConfigurationMigration.getReadLegacySecurityConfiguration())
                        .subtask(new RemoveUnsupportedExtensions<>())
                        .subtask(new RemoveUnsupportedSubsystems<>())
                        .subtask(new MigrateReferencedModules<>())
                        .subtask(new WildFly26_0MigrateReferencedPaths<>())
                        .subtask(new WildFly26_0MigrateVault<>())
                        .subtask(legacySecurityConfigurationMigration.getRemoveLegacySecurityRealms())
                        .subtask(legacySecurityConfigurationMigration.getEnsureBasicElytronSubsystem())
                        .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityRealmsToElytron())
                        .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityDomainsToElytron())
                        .subtask(new WildFly26_0AddHealthSubsystem<>())
                        .subtask(new EAP8_0AddMetricsSubsystem<>())
                        .subtask(new WildFly26_0AddMicroprofileJwtSmallryeSubsystem<>())
                        .subtask(new MigratePicketLinkSubsystem<>())
                        .subtask(new MigrateKeycloakSubsystem<>())
                )
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(new WildFly27_0MigrateJBossDomainProperties<>())
                                .subtask(legacySecurityConfigurationMigration.getReadLegacySecurityConfiguration())
                                .subtask(new RemoveUnsupportedExtensions<>())
                                .subtask(new RemoveUnsupportedSubsystems<>())
                                .subtask(new MigrateReferencedModules<>())
                                .subtask(new WildFly26_0MigrateReferencedPaths<>())
                                .subtask(new WildFly26_0AddMicroprofileJwtSmallryeSubsystem<>())
                                .subtask(new EAP8_0AddHostExcludes<>())
                                .subtask(legacySecurityConfigurationMigration.getEnsureBasicElytronSubsystem())
                                .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityRealmsToElytron())
                                .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityDomainsToElytron())
                                .subtask(new MigratePicketLinkSubsystem<>())
                                .subtask(new MigrateKeycloakSubsystem<>())
                        )
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(new WildFly27_0MigrateJBossDomainProperties<>())
                                .subtask(legacySecurityConfigurationMigration.getReadLegacySecurityConfiguration())
                                .subtask(new MigrateReferencedModules<>())
                                .subtask(new WildFly26_0MigrateReferencedPaths<>())
                                .subtask(legacySecurityConfigurationMigration.getRemoveLegacySecurityRealms())
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(legacySecurityConfigurationMigration.getEnsureBasicElytronSubsystem())
                                        .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityRealmsToElytron())
                                        .subtask(legacySecurityConfigurationMigration.getMigrateLegacySecurityDomainsToElytron())
                                )
                        )
                ).build();
    }

    @Override
    public Class<EAPXPServer7_3> getSourceType() {
        return EAPXPServer7_3.class;
    }
}
