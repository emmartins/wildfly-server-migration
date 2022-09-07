/*
 * Copyright 2018 Red Hat, Inc.
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

package org.jboss.migration.wfly.task.security;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.HostConfiguration;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfigurationType;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResources;
import org.jboss.migration.wfly11.task.subsystem.elytron.HttpAuthenticationFactoryAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.IdentityRealmAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.MechanismConfiguration;
import org.jboss.migration.wfly11.task.subsystem.elytron.MechanismRealmConfiguration;
import org.jboss.migration.wfly11.task.subsystem.elytron.PropertiesRealmAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.ProviderHttpServerMechanismFactoryAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.SecurityDomainAddOperation;

/**
 * @author emmartins
 */
public class MigrateLegacySecurityUpdateElytronSubsystem<S> extends UpdateSubsystemResources<S> {

    private static final String TASK_NAME = "security.migrate-legacy-security-to-elytron";

    public MigrateLegacySecurityUpdateElytronSubsystem(final LegacySecurityConfigurations legacySecurityConfigurations) {
        super(JBossSubsystemNames.ELYTRON,
                new UpdateSubsystemResourceSubtaskBuilder<S>() {
                    {
                        subtaskName("migrate-legacy-security-realms");
                    }
                    @Override
                    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext taskContext, TaskEnvironment taskEnvironment) {
                        LegacySecurityConfiguration legacySecurityConfiguration = legacySecurityConfigurations.getSecurityConfigurations().get(subsystemResource.getServerConfiguration().getConfigurationPath().getPath().toString());
                        for (LegacySecurityRealm securityRealm : legacySecurityConfiguration.getLegacySecurityRealms().values()) {
                            migrateSecurityRealm(securityRealm, config, subsystemResource, taskContext, taskEnvironment);
                        }
                        return ServerMigrationTaskResult.SUCCESS;
                    }

                    private void migrateSecurityRealm(LegacySecurityRealm securityRealm, ModelNode config, SubsystemResource subsystemResource, TaskContext taskContext, TaskEnvironment taskEnvironment) {
                        final ManageableServerConfiguration configuration = subsystemResource.getServerConfiguration();
                        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
                        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
                        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
                        addOperationSteps(securityRealm, configuration, subsystemPathAddress, compositeOperationBuilder);
                        configuration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
                    }

                    private void addOperationSteps(LegacySecurityRealm securityRealm, ManageableServerConfiguration configuration, PathAddress subsystemPathAddress, Operations.CompositeOperationBuilder compositeOperationBuilder) {
                        addSecurityRealm(securityRealm, configuration, subsystemPathAddress, compositeOperationBuilder);
                        addSecurityDomain(securityRealm, configuration, subsystemPathAddress, compositeOperationBuilder);
                        addHttp(securityRealm, configuration, subsystemPathAddress, compositeOperationBuilder);
                        addSasl(securityRealm, configuration, subsystemPathAddress, compositeOperationBuilder);
                    }

                    private void addHttp(LegacySecurityRealm securityRealm, ManageableServerConfiguration configuration, PathAddress subsystemPathAddress, Operations.CompositeOperationBuilder compositeOperationBuilder) {
                        compositeOperationBuilder.addStep(new HttpAuthenticationFactoryAddOperation(subsystemPathAddress, securityRealm.getElytronHttpAuthenticationFactoryName())
                                .securityDomain(securityRealm.getElytronSecurityDomainName())
                                .httpServerMechanismFactory("global")
                                .addMechanismConfiguration(new MechanismConfiguration("BASIC").addMechanismRealmConfiguration(new MechanismRealmConfiguration("ManagementRealm")))
                                .toModelNode());
                        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
                        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE) {
                            compositeOperationBuilder.addStep(new HttpAuthenticationFactoryAddOperation(subsystemPathAddress, "management-http-authentication")
                                    .securityDomain("ManagementDomain")
                                    .httpServerMechanismFactory("global")
                                    .addMechanismConfiguration(new MechanismConfiguration("DIGEST").addMechanismRealmConfiguration(new MechanismRealmConfiguration("ManagementRealm")))
                                    .toModelNode());
                        } else if (configurationType == HostConfiguration.RESOURCE_TYPE) {
                            compositeOperationBuilder.addStep(new HttpAuthenticationFactoryAddOperation(subsystemPathAddress, "management-http-authentication")
                                    .securityDomain("ManagementDomain")
                                    .httpServerMechanismFactory("global")
                                    .addMechanismConfiguration(new MechanismConfiguration("BASIC").addMechanismRealmConfiguration(new MechanismRealmConfiguration("ManagementRealm")))
                                    .toModelNode());
                        }
                        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
                            compositeOperationBuilder.addStep(new HttpAuthenticationFactoryAddOperation(subsystemPathAddress, "application-http-authentication")
                                    .securityDomain("ApplicationDomain")
                                    .httpServerMechanismFactory("global")
                                    .addMechanismConfiguration(new MechanismConfiguration("BASIC").addMechanismRealmConfiguration(new MechanismRealmConfiguration("ApplicationRealm")))
                                    .addMechanismConfiguration(new MechanismConfiguration("FORM"))
                                    .toModelNode());
                        }
                        compositeOperationBuilder.addStep(new ProviderHttpServerMechanismFactoryAddOperation(subsystemPathAddress, "global")
                                .toModelNode());
                    }

                    private void addSecurityDomain(LegacySecurityRealm securityRealm, ManageableServerConfiguration configuration, PathAddress subsystemPathAddress, Operations.CompositeOperationBuilder compositeOperationBuilder) {
                        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
                        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
                            final SecurityDomainAddOperation securityDomainAddOperation = new SecurityDomainAddOperation(subsystemPathAddress, securityRealm.getElytronSecurityDomainName())
                                    .permissionMapper("default-permission-mapper")
                                    .defaultRealm(securityRealm.getElytronPropertiesRealmName())
                                    .addRealm(new SecurityDomainAddOperation.Realm(securityRealm.getElytronPropertiesRealmName())
                                            .roleDecoder(securityRealm.getAuthorization().isMapGroupsToRoles() ? "groups-to-roles" : null));
                            if (securityRealm.getAuthentication().getLocal() != null) {
                                // TODO assert elytron config has realm local and role mapper super-user-mapper
                                securityDomainAddOperation.addRealm(new SecurityDomainAddOperation.Realm("local").roleMapper(securityRealm.getAuthentication().getLocal().getAllowedUsers() == null ? "super-user-mapper" : null));
                            }
                            compositeOperationBuilder.addStep(securityDomainAddOperation.toModelNode());
                        }
                    }

                    private void addSecurityRealm(LegacySecurityRealm securityRealm, ManageableServerConfiguration configuration, PathAddress subsystemPathAddress, Operations.CompositeOperationBuilder compositeOperationBuilder) {
                        final ManageableServerConfigurationType configurationType = configuration.getConfigurationType();
                        if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
                            compositeOperationBuilder.addStep(new PropertiesRealmAddOperation(subsystemPathAddress, securityRealm.getElytronPropertiesRealmName())
                                    .usersProperties(new PropertiesRealmAddOperation.Properties(securityRealm.getAuthentication().getProperties().getPath())
                                            .relativeTo(securityRealm.getAuthentication().getProperties().getRelativeTo())
                                            .plainText(securityRealm.getAuthentication().getProperties().isPlainText())
                                            .digestRealmName(securityRealm.getName())
                                    )
                                    .groupsProperties(new PropertiesRealmAddOperation.Properties(securityRealm.getAuthorization().getProperties().getPath())
                                            .relativeTo(securityRealm.getAuthorization().getProperties().getRelativeTo())
                                            .plainText(securityRealm.getAuthorization().getProperties().isPlainText())
                                    )
                                    .toModelNode());
                        }
                    }
                });
        name(TASK_NAME);
    }
}
