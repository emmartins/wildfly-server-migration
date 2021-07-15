/*
 * Copyright 2021 Red Hat, Inc.
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

package org.jboss.migration.wfly;

import org.jboss.migration.wfly.task.hostexclude.WildFly24_0AddHostExcludes;
import org.jboss.migration.wfly.task.subsystem.health.WildFly24_0AddHealthSubsystem;
import org.jboss.migration.wfly.task.subsystem.metrics.WildFly24_0AddMetricsSubsystem;
import org.jboss.migration.wfly.task.subsystem.microprofile.WildFly24_0AddMicroprofileConfigSmallryeSubsystem;
import org.jboss.migration.wfly.task.subsystem.microprofile.WildFly24_0AddMicroprofileJwtSmallryeSubsystem;
import org.jboss.migration.wfly.task.subsystem.microprofile.WildFly24_0AddMicroprofileOpentracingSmallryeSubsystem;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.WildFlyServerMigration10;
import org.jboss.migration.wfly10.config.task.module.MigrateReferencedModules;
import org.jboss.migration.wfly10.config.task.paths.MigrateReferencedPaths;
import org.jboss.migration.wfly10.config.task.update.AddApplicationRealmSSLServerIdentity;
import org.jboss.migration.wfly10.config.task.update.AddLoadBalancerProfile;
import org.jboss.migration.wfly10.config.task.update.AddSocketBindingMulticastAddressExpressions;
import org.jboss.migration.wfly10.config.task.update.MigrateCompatibleSecurityRealms;
import org.jboss.migration.wfly10.config.task.update.MigrateDeployments;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedExtensions;
import org.jboss.migration.wfly10.config.task.update.RemoveUnsupportedSubsystems;
import org.jboss.migration.wfly10.config.task.update.ServerUpdate;
import org.jboss.migration.wfly10.dist.full.WildFlyFullServer10_0;
import org.jboss.migration.wfly11.task.subsystem.coremanagement.AddCoreManagementSubsystem;
import org.jboss.migration.wfly11.task.subsystem.logging.RemoveConsoleHandlerFromLoggingSubsystem;
import org.jboss.migration.wfly13.task.subsystem.discovery.AddDiscoverySubsystem;
import org.jboss.migration.wfly13.task.subsystem.eesecurity.AddEESecuritySubsystem;
import org.jboss.migration.wfly13.task.subsystem.elytron.WildFly13_0AddElytronSubsystem;

/**
 * Server migration to WFLY 24.0, from WFLY 10.0.
 * @author emmartins
 */
public class WildFly10_0ToWildFly24_0ServerMigrationProvider implements WildFly24_0ServerMigrationProvider {

    @Override
    public WildFlyServerMigration10 getServerMigration() {
        final ServerUpdate.Builders<WildFlyServer10> serverUpdateBuilders = new ServerUpdate.Builders<>();
        return serverUpdateBuilders.serverUpdateBuilder()
                .standaloneServer(serverUpdateBuilders.standaloneConfigurationBuilder()
                        .subtask(new RemoveUnsupportedExtensions<>())
                        .subtask(new RemoveUnsupportedSubsystems<>())
                        .subtask(new MigrateReferencedModules<>())
                        .subtask(new MigrateReferencedPaths<>())
                        .subtask(new WildFly10_0ToWildFly24_0UpdateInfinispanSubsystem<>())
                        .subtask(new WildFly10_0ToWildFly24_0UpdateUndertowSubsystem<>())
                        .subtask(new WildFly10_0ToWildFly24_0UpdateJGroupsSubsystem<>())
                        .subtask(new AddCoreManagementSubsystem<>())
                        .subtask(new WildFly13_0AddElytronSubsystem<>())
                        .subtask(new AddDiscoverySubsystem<>())
                        .subtask(new AddEESecuritySubsystem<>())
                        .subtask(new WildFly24_0AddHealthSubsystem<>())
                        .subtask(new WildFly24_0AddMetricsSubsystem<>())
                        .subtask(new WildFly24_0AddMicroprofileConfigSmallryeSubsystem<>())
                        .subtask(new WildFly24_0AddMicroprofileJwtSmallryeSubsystem<>())
                        .subtask(new WildFly24_0AddMicroprofileOpentracingSmallryeSubsystem<>())
                        .subtask(new AddSocketBindingMulticastAddressExpressions<>())
                        .subtask(new MigrateCompatibleSecurityRealms<>())
                        .subtask(new AddApplicationRealmSSLServerIdentity<>())
                        .subtask(new MigrateDeployments<>()))
                .domain(serverUpdateBuilders.domainBuilder()
                        .domainConfigurations(serverUpdateBuilders.domainConfigurationBuilder()
                                .subtask(new RemoveUnsupportedExtensions<>())
                                .subtask(new RemoveUnsupportedSubsystems<>())
                                .subtask(new MigrateReferencedModules<>())
                                .subtask(new MigrateReferencedPaths<>())
                                .subtask(new WildFly10_0ToWildFly24_0UpdateInfinispanSubsystem<>())
                                .subtask(new WildFly10_0ToWildFly24_0UpdateUndertowSubsystem<>())
                                .subtask(new WildFly10_0ToWildFly24_0UpdateJGroupsSubsystem<>())
                                .subtask(new AddCoreManagementSubsystem<>())
                                .subtask(new WildFly13_0AddElytronSubsystem<>())
                                .subtask(new AddDiscoverySubsystem<>())
                                .subtask(new AddEESecuritySubsystem<>())
                                .subtask(new WildFly24_0AddMicroprofileConfigSmallryeSubsystem<>())
                                .subtask(new WildFly24_0AddMicroprofileJwtSmallryeSubsystem<>())
                                .subtask(new WildFly24_0AddMicroprofileOpentracingSmallryeSubsystem<>())
                                .subtask(new AddSocketBindingMulticastAddressExpressions<>())
                                .subtask(new AddLoadBalancerProfile<>())
                                .subtask(new WildFly24_0AddHostExcludes<>())
                                .subtask(new RemoveConsoleHandlerFromLoggingSubsystem<>())
                                .subtask(new MigrateDeployments<>()))
                        .hostConfigurations(serverUpdateBuilders.hostConfigurationBuilder()
                                .subtask(new MigrateReferencedModules<>())
                                .subtask(new MigrateReferencedPaths<>())
                                .subtask(serverUpdateBuilders.hostBuilder()
                                        .subtask(new AddCoreManagementSubsystem<>())
                                        .subtask(new WildFly13_0AddElytronSubsystem<>())
                                        .subtask(new MigrateCompatibleSecurityRealms<>())
                                        .subtask(new AddApplicationRealmSSLServerIdentity<>()))))
                .build();
    }

    @Override
    public Class<WildFlyFullServer10_0> getSourceType() {
        return WildFlyFullServer10_0.class;
    }
}
