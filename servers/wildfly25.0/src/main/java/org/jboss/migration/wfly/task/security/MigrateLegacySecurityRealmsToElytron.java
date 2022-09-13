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

package org.jboss.migration.wfly.task.security;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.HostControllerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ManageableServerConfigurationType;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly11.task.subsystem.elytron.HttpAuthenticationFactoryAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.KeyManagerAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.KeystoreAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.MechanismConfiguration;
import org.jboss.migration.wfly11.task.subsystem.elytron.MechanismRealmConfiguration;
import org.jboss.migration.wfly11.task.subsystem.elytron.PropertiesRealmAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.SaslAuthenticationFactoryAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.SecurityDomainAddOperation;
import org.jboss.migration.wfly11.task.subsystem.elytron.ServerSSLContextAddOperation;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.operations.common.Util.getUndefineAttributeOperation;
import static org.jboss.as.controller.operations.common.Util.getWriteAttributeOperation;

/**
 * @author emmartins
 */
public class MigrateLegacySecurityRealmsToElytron<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    private static final String TASK_NAME = "security.migrate-legacy-security-realms-to-elytron";

    public MigrateLegacySecurityRealmsToElytron(LegacySecurityConfigurations legacySecurityConfigurations) {
        name(TASK_NAME);
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Migrating legacy security realms to Elytron..."));
        subtasks(ManageableServerConfigurationCompositeSubtasks.of(new MigrateToElytron<>(legacySecurityConfigurations), new SetManagementInterfacesHttpUpgradeEnabled<>(legacySecurityConfigurations)));
        afterRun(context -> context.getLogger().infof("Legacy security realms migrated to Elytron."));
    }

    public static class MigrateToElytron<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

        private static final String SUBTASK_NAME = TASK_NAME + ".update-elytron";

        protected MigrateToElytron(final LegacySecurityConfigurations legacySecurityConfigurations) {
            name(SUBTASK_NAME);
            skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
            final ManageableResourceTaskRunnableBuilder<S, SubsystemResource> runnableBuilder = params -> context -> {
                final SubsystemResource subsystemResource = params.getResource();
                final LegacySecurityConfiguration legacySecurityConfiguration = legacySecurityConfigurations.getSecurityConfigurations().get(subsystemResource.getServerConfiguration().getConfigurationPath().getPath().toString());
                for (LegacySecurityRealm securityRealm : legacySecurityConfiguration.getLegacySecurityRealms().values()) {
                    migrateSecurityRealm(securityRealm, legacySecurityConfiguration, subsystemResource, context);
                }
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(SubsystemResource.class, JBossSubsystemNames.ELYTRON, runnableBuilder);
        }

        protected void migrateSecurityRealm(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, TaskContext taskContext) {
            final ManageableServerConfiguration configuration = subsystemResource.getServerConfiguration();
            final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
            addOperationSteps(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
            configuration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
        }

        protected void addOperationSteps(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            addSecurityRealm(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
            addSecurityDomain(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
            addHttp(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
            addSasl(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
            addServerIdentities(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
        }

        protected void addSecurityRealm(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            final ManageableServerConfigurationType configurationType = subsystemResource.getServerConfiguration().getConfigurationType();
            if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
                compositeOperationBuilder.addStep(new PropertiesRealmAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronPropertiesRealmName())
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

        protected void addSecurityDomain(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            final ManageableServerConfigurationType configurationType = subsystemResource.getServerConfiguration().getConfigurationType();
            if (configurationType == StandaloneServerConfiguration.RESOURCE_TYPE || configurationType == HostControllerConfiguration.RESOURCE_TYPE) {
                // TODO assert elytron config has permissionMapper named default-permission-mapper
                // TODO assert elytron config has role decoder groups-to-roles
                final SecurityDomainAddOperation securityDomainAddOperation = new SecurityDomainAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronSecurityDomainName())
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

        protected void addHttp(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            // TODO assert elytron config has httpServerMechanismFactory named global
            compositeOperationBuilder.addStep(new HttpAuthenticationFactoryAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronHttpAuthenticationFactoryName())
                    .securityDomain(securityRealm.getElytronSecurityDomainName())
                    .httpServerMechanismFactory("global")
                    .addMechanismConfiguration(new MechanismConfiguration("DIGEST").addMechanismRealmConfiguration(new MechanismRealmConfiguration(securityRealm.getName())))
                    .toModelNode());
        }

        protected void addSasl(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            // TODO assert elytron config has saslServerFactory named configured
            compositeOperationBuilder.addStep(new SaslAuthenticationFactoryAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronSaslAuthenticationFactoryName())
                    .securityDomain(securityRealm.getElytronSecurityDomainName())
                    .saslServerFactory("configured")
                    .addMechanismConfiguration(new MechanismConfiguration("JBOSS-LOCAL-USER").realmMapper("local"))
                    .addMechanismConfiguration(new MechanismConfiguration("DIGEST-MD5").addMechanismRealmConfiguration(new MechanismRealmConfiguration(securityRealm.getName())))
                    .toModelNode());
            // replace any remoting subsystem usage of the legacy security realm, with the created elytron sasl factory
            final Logger logger = taskContext.getLogger();
            logger.warn("Looking for Remoting subsystem http-connector resources using the legacy security-realm...");
            final SubsystemResource remotingSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.REMOTING);
            if (remotingSubsystemResource != null) {
                logger.warn("Remoting subsystem config found.");
                final ModelNode remotingSubsystemConfig = remotingSubsystemResource.getResourceConfiguration();
                if (remotingSubsystemConfig.hasDefined("http-connector")) {
                    logger.warn("Remoting subsystem config has http-connector resources defined.");
                    for (Property remotingHttpConnectorProperty : remotingSubsystemConfig.get("http-connector").asPropertyList()) {
                        final String remotingHttpConnectorName = remotingHttpConnectorProperty.getName();
                        logger.warnf("Processing Remoting subsystem http-connector named %s.", remotingHttpConnectorName);
                        final ModelNode remotingHttpConnectorConfig = remotingHttpConnectorProperty.getValue();
                        if (securityRealm.getName().equals(remotingHttpConnectorConfig.get(SECURITY_REALM).asStringOrNull())) {
                            // we found a http connector using the legacy security-real, update it to use the created Elytron's sasl factory
                            logger.warn("Remoting subsystem http-invoker uses the legacy security-realm, updating....");
                            final PathAddress remotingConnectorAddress = remotingSubsystemResource.getResourcePathAddress().append("http-connector", remotingHttpConnectorName);
                            compositeOperationBuilder.addStep(getUndefineAttributeOperation(remotingConnectorAddress, SECURITY_REALM));
                            compositeOperationBuilder.addStep(getWriteAttributeOperation(remotingConnectorAddress, "sasl-authentication-factory", securityRealm.getElytronSaslAuthenticationFactoryName()));
                        }
                    }
                }
            }
        }

        protected void addServerIdentities(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            addServerIdentitySSL(securityRealm, legacySecurityConfiguration, subsystemResource, compositeOperationBuilder, taskContext);
        }

        protected void addServerIdentitySSL(LegacySecurityRealm securityRealm, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            final Logger logger = taskContext.getLogger();
            if (securityRealm.getServerIdentities() != null) {
                final LegacySecurityRealmSSLServerIdentity sslServerIdentity = securityRealm.getServerIdentities().getSsl();
                if (sslServerIdentity != null) {
                    logger.warn("Migrating Legacy SSL server identity to Elytron...");
                    // add key-store
                    compositeOperationBuilder.addStep(new KeystoreAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronTLSKeyStoreName())
                            .keystorePassword(sslServerIdentity.getKeystore().getKeystorePassword())
                            .path(sslServerIdentity.getKeystore().getPath())
                            .relativeTo(sslServerIdentity.getKeystore().getRelativeTo())
                            .type(sslServerIdentity.getKeystore().getProvider())
                            .toModelNode());
                    // add key-manager
                    compositeOperationBuilder.addStep(new KeyManagerAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronTLSKeyManagerName())
                            .keystore(securityRealm.getElytronTLSKeyStoreName())
                            .aliasFilter(sslServerIdentity.getKeystore().getAlias())
                            .generateSelfSignedCertificateHost(sslServerIdentity.getKeystore().getGenerateSelfSignedCertificateHost() != null)
                            .keyPassword(sslServerIdentity.getKeystore().getKeyPassword())
                            .toModelNode());
                    // add ssl server context
                    compositeOperationBuilder.addStep(new ServerSSLContextAddOperation(subsystemResource.getResourcePathAddress(), securityRealm.getElytronTLSServerSSLContextName())
                            .keyManager(securityRealm.getElytronTLSKeyManagerName())
                            .toModelNode());
                    // replace any undertow usage of the legacy security realm, with the created elytron ssl context
                    logger.warn("Looking for Undertow subsystem https-listener resources using the legacy security-realm...");
                    final SubsystemResource undertowSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.UNDERTOW);
                    if (undertowSubsystemResource != null) {
                        logger.warn("Undertow subsystem config found.");
                        final ModelNode undertowSubsystemConfig = undertowSubsystemResource.getResourceConfiguration();
                        if (undertowSubsystemConfig.hasDefined("server")) {
                            logger.warn("Undertow subsystem config has server resource defined.");
                            for (Property undertowServerProperty : undertowSubsystemConfig.get("server").asPropertyList()) {
                                final String undertowServerName = undertowServerProperty.getName();
                                logger.warnf("Processing Undertow subsystem server named %s.", undertowServerName);
                                final ModelNode undertowServerConfig = undertowServerProperty.getValue();
                                if (undertowServerConfig.hasDefined("https-listener")) {
                                    logger.warn("Undertow subsystem server has https-listener resource defined.");
                                    for (Property undertowHttpsListenerProperty : undertowServerConfig.get("https-listener").asPropertyList()) {
                                        final String undertowHttpsListenerName = undertowHttpsListenerProperty.getName();
                                        logger.warnf("Processing Undertow https-listener named %s.", undertowServerName);
                                        final ModelNode undertowHttpsListenerConfig = undertowHttpsListenerProperty.getValue();
                                        if (securityRealm.getName().equals(undertowHttpsListenerConfig.get(SECURITY_REALM).asStringOrNull())) {
                                            // we found a http listener using the legacy security-real, update it to use Elytron's server ssl context insteadhttps listener config
                                            logger.warn("Undertow subsystem https-listener uses the legacy security-realm, updating....");
                                            final PathAddress undertowConnectorAddress = undertowSubsystemResource.getResourcePathAddress().append("server", undertowServerName).append("https-listener", undertowHttpsListenerName);
                                            compositeOperationBuilder.addStep(getUndefineAttributeOperation(undertowConnectorAddress, SECURITY_REALM));
                                            compositeOperationBuilder.addStep(getWriteAttributeOperation(undertowConnectorAddress, SSL_CONTEXT, securityRealm.getElytronTLSServerSSLContextName()));
                                        }
                                    }
                                }
                            }
                        }


                    }


                }
            }
        }
    }



    public static class SetManagementInterfacesHttpUpgradeEnabled<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

        private static final String SUBTASK_NAME = TASK_NAME + ".update-management-interfaces";

        protected SetManagementInterfacesHttpUpgradeEnabled(final LegacySecurityConfigurations legacySecurityConfigurations) {
            name(SUBTASK_NAME);
            skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
            final ManageableResourceTaskRunnableBuilder<S, ManagementInterfaceResource.Parent> runnableBuilder = params -> context -> {
                final ManagementInterfaceResource.Parent resource = params.getResource();
                final LegacySecurityConfiguration legacySecurityConfiguration = legacySecurityConfigurations.getSecurityConfiguration(resource.getServerConfiguration().getConfigurationPath().getPath());
                final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
                ServerMigrationTaskResult taskResult = ServerMigrationTaskResult.SKIPPED;
                if (legacySecurityConfiguration != null) {
                    for (LegacySecuredManagementInterface managementInterface : legacySecurityConfiguration.getSecuredManagementInterfaces()) {
                        final ManagementInterfaceResource managementInterfaceResource = resource.getManagementInterfaceResource(managementInterface.getName());
                        final LegacySecurityRealm securityRealm = legacySecurityConfiguration.getLegacySecurityRealm(managementInterface.getSecurityRealm());
                        if (securityRealm == null) {
                            throw new ServerMigrationFailureException("Missing legacy security-realm named " + managementInterface.getSecurityRealm() + ", referred by managed-interface " + managementInterface.getName());
                        }
                        if (managementInterface.getName().equals(HTTP_INTERFACE)) {
                            // set http authentication factory
                            final ModelNode writeHttpAuthFactoryAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, managementInterfaceResource.getResourcePathAddress());
                            writeHttpAuthFactoryAttrOp.get(NAME).set(HTTP_AUTHENTICATION_FACTORY);
                            writeHttpAuthFactoryAttrOp.get(VALUE).set(securityRealm.getElytronHttpAuthenticationFactoryName());
                            compositeOperationBuilder.addStep(writeHttpAuthFactoryAttrOp);
                            if (managementInterfaceResource.getResourceConfiguration().hasDefined(HTTP_UPGRADE)) {
                                // set sasl auth factory
                                final ModelNode writeSaslAuthFactoryAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, managementInterfaceResource.getResourcePathAddress());
                                writeSaslAuthFactoryAttrOp.get(NAME).set(HTTP_UPGRADE + "." + SASL_AUTHENTICATION_FACTORY);
                                writeSaslAuthFactoryAttrOp.get(VALUE).set(securityRealm.getElytronSaslAuthenticationFactoryName());
                                compositeOperationBuilder.addStep(writeSaslAuthFactoryAttrOp);
                            }
                            taskResult = ServerMigrationTaskResult.SUCCESS;
                        } else if (managementInterface.getName().equals(NATIVE_INTERFACE)) {
                            // set sasl auth factory
                            final ModelNode writeSaslAuthFactoryAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, managementInterfaceResource.getResourcePathAddress());
                            writeSaslAuthFactoryAttrOp.get(NAME).set(SASL_AUTHENTICATION_FACTORY);
                            writeSaslAuthFactoryAttrOp.get(VALUE).set(securityRealm.getElytronSaslAuthenticationFactoryName());
                            compositeOperationBuilder.addStep(writeSaslAuthFactoryAttrOp);
                            taskResult = ServerMigrationTaskResult.SUCCESS;
                        } else {
                            context.getLogger().debugf("Skipping Management interface %s.", managementInterface.getName());
                        }
                    }
                }
                if (taskResult == ServerMigrationTaskResult.SUCCESS) {
                    resource.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());
                }
                return taskResult;
            };
            runBuilder(ManagementInterfaceResource.Parent.class, runnableBuilder);
        }
    }
}