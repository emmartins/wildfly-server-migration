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

package org.jboss.migration.wfly.task.security;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.operations.common.Util.createAddOperation;
import static org.jboss.as.controller.operations.common.Util.getUndefineAttributeOperation;
import static org.jboss.as.controller.operations.common.Util.getWriteAttributeOperation;

/**
 * @author emmartins
 */
public class MigrateLegacySecurityDomainsToElytron<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    private static final String TASK_NAME = "security.migrate-legacy-security-domains-to-elytron";

    public MigrateLegacySecurityDomainsToElytron(LegacySecurityConfigurations legacySecurityConfigurations) {
        name(TASK_NAME);
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Migrating legacy security domains to Elytron..."));
        subtasks(ManageableServerConfigurationCompositeSubtasks.of(new UpdateSubsystems<>(legacySecurityConfigurations)));
        afterRun(context -> context.getLogger().debugf("Legacy security domains migrated to Elytron."));
    }

    public static class UpdateSubsystems<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

        private static final String SUBTASK_NAME = TASK_NAME + ".update-subsystems";

        public static final String SECURITY_DOMAIN = "security-domain";
        public static final String APPLICATION_SECURITY_DOMAIN = "application-security-domain";
        public static final String DEFAULT_SECURITY_DOMAIN = "default-security-domain";
        public static final String REALM = "realm";
        public static final String SECURITY = "security";
        public static final String CLIENT = "client";
        public static final String IDENTITY = "identity";
        public static final String ELYTRON = "elytron";
        public static final String ELYTRON_DOMAIN = "elytron-domain";

        protected UpdateSubsystems(final LegacySecurityConfigurations legacySecurityConfigurations) {
            name(SUBTASK_NAME);
            skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
            final ManageableResourceTaskRunnableBuilder<S, SubsystemResource> runnableBuilder = params -> context -> {
                ServerMigrationTaskResult taskResult = ServerMigrationTaskResult.SKIPPED;
                final SubsystemResource subsystemResource = params.getResource();
                final LegacySecurityConfiguration legacySecurityConfiguration = legacySecurityConfigurations.getSecurityConfigurations().get(subsystemResource.getServerConfiguration().getConfigurationPath().getPath().toString());
                if (legacySecurityConfiguration != null) {
                    /*
                    final String subsystemProfileName = subsystemResource.getParentResource().getResourceType() == ProfileResource.RESOURCE_TYPE ? subsystemResource.getParentResource().getResourceName() : null;
                    for (LegacySecurityDomain securityDomain : legacySecurityConfiguration.getLegacySecurityDomains()) {
                        if (subsystemProfileName != null && !subsystemProfileName.equals(securityDomain.getProfile())) {
                            // security domains bound to a profile (host controller config) should be migrated only by the task for the subsystem resource with same profile
                            continue;
                        }
                        if (migrateSecurityDomain(securityDomain, legacySecurityConfiguration, subsystemResource, context)) {
                            taskResult = ServerMigrationTaskResult.SUCCESS;
                        }
                    }
                    */
                    if (migrateSubsystemIIOP(legacySecurityConfiguration, subsystemResource, context)) {
                        taskResult = ServerMigrationTaskResult.SUCCESS;
                    }
                    if (migrateSubsystemIIOP(legacySecurityConfiguration, subsystemResource, context)) {
                        taskResult = ServerMigrationTaskResult.SUCCESS;
                    }
                    if (migrateSubsystemIIOP(legacySecurityConfiguration, subsystemResource, context)) {
                        taskResult = ServerMigrationTaskResult.SUCCESS;
                    }
                }
                return taskResult;
            };
            runBuilder(SubsystemResource.class, JBossSubsystemNames.ELYTRON, runnableBuilder);
        }

        protected boolean migrateSubsystemIIOP(LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, TaskContext taskContext) {
            taskContext.getLogger().debugf("Looking for iiop-openjdk subsystem with security configuration requiring legacy security domains.");
            final SubsystemResource iiopSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.IIOP_OPENJDK);
            if (iiopSubsystemResource != null) {
                final String iiopSecurity = iiopSubsystemResource.getResourceConfiguration().get(SECURITY).asStringOrNull();
                if (IDENTITY.equals(iiopSecurity) || CLIENT.equals(iiopSecurity)) {
                    subsystemResource.getServerConfiguration().executeManagementOperation(getWriteAttributeOperation(iiopSubsystemResource.getResourcePathAddress(), SECURITY, ELYTRON));
                    taskContext.getLogger().debugf("Migrated iiop-openjdk subsystem resource to Elytron.");
                    return true;
                }
            }
            return false;
        }

        protected boolean migrateSubsystemEJB3(LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, TaskContext taskContext) {
            taskContext.getLogger().debugf("Looking for ejb3 subsystem resources using a legacy security-domain...");
            final SubsystemResource ejbSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.EJB3);
            if (ejbSubsystemResource != null) {
                final ModelNode subsystemConfig = ejbSubsystemResource.getResourceConfiguration();
                final String defaultSecurityDomain = subsystemConfig.get(DEFAULT_SECURITY_DOMAIN).asStringOrNull();
                if (defaultSecurityDomain != null) {
                    if (!subsystemConfig.hasDefined(APPLICATION_SECURITY_DOMAIN, legacySecurityConfiguration.getDefaultElytronApplicationDomainName())) {
                        taskContext.getLogger().debugf("Found ejb3 subsystem resource using a Legacy security-domain.");
                        final PathAddress pathAddress = ejbSubsystemResource.getResourcePathAddress().append(APPLICATION_SECURITY_DOMAIN, legacySecurityConfiguration.getDefaultElytronApplicationDomainName());
                        final ModelNode op = createAddOperation(pathAddress);
                        op.get(SECURITY_DOMAIN).set(defaultSecurityDomain);
                        subsystemResource.getServerConfiguration().executeManagementOperation(op);
                        taskContext.getLogger().debugf("Migrated ejb3 subsystem resource using legacy security domain %s, to Elytron's default application Security Domain %s. Please note that further manual Elytron configuration may be needed if the legacy security domain being used was not the source server's default Application Domain configuration!", defaultSecurityDomain, legacySecurityConfiguration.getDefaultElytronApplicationDomainName());
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean migrateSubsystemUndertow(LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, TaskContext taskContext) {
            taskContext.getLogger().debugf("Looking for undertow subsystem resources using a legacy security-domain...");
            final SubsystemResource undertowSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.UNDERTOW);
            if (undertowSubsystemResource != null) {
                final ModelNode subsystemConfig = undertowSubsystemResource.getResourceConfiguration();
                final String defaultSecurityDomain = subsystemConfig.get(DEFAULT_SECURITY_DOMAIN).asStringOrNull();
                if (defaultSecurityDomain != null) {
                    if (!subsystemConfig.hasDefined(APPLICATION_SECURITY_DOMAIN, legacySecurityConfiguration.getDefaultElytronApplicationDomainName())) {
                        final PathAddress pathAddress = undertowSubsystemResource.getResourcePathAddress().append(APPLICATION_SECURITY_DOMAIN, legacySecurityConfiguration.getDefaultElytronApplicationDomainName());
                        final ModelNode op = createAddOperation(pathAddress);
                        op.get(SECURITY_DOMAIN).set(defaultSecurityDomain);
                        subsystemResource.getServerConfiguration().executeManagementOperation(op);
                        taskContext.getLogger().debugf("Migrated undertow subsystem resource using legacy security domain %s, to Elytron's default application Security Domain %s. Please note that further manual Elytron configuration may be needed if the legacy security domain being used was not the source server's default Application Domain configuration!", defaultSecurityDomain, legacySecurityConfiguration.getDefaultElytronApplicationDomainName());
                        return true;
                    }
                }
            }
            return false;
        }

        protected boolean migrateSubsystemMessaging(LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, TaskContext taskContext) {
            taskContext.getLogger().debugf("Looking for messaging-activemq subsystem resources using a legacy security-domain...");
            final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
            boolean updated = false;
            final SubsystemResource messagingSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.MESSAGING_ACTIVEMQ);
            if (messagingSubsystemResource != null) {
                final ModelNode subsystemConfig = messagingSubsystemResource.getResourceConfiguration();
                for (Property serverProperty : subsystemConfig.get(SERVER).asPropertyList()) {
                    final String serverName = serverProperty.getName();
                    final ModelNode serverConfig = serverProperty.getValue();
                    final ModelNode serverSecurityDomainNode = serverConfig.get(SECURITY_DOMAIN);
                    final String messagingSubsystemSecurityDomain = serverSecurityDomainNode.isDefined() ? serverSecurityDomainNode.asString() : "other";
                    if (messagingSubsystemSecurityDomain != null) {
                        taskContext.getLogger().debugf("Found messaging-activemq subsystem server %s using the legacy security-domain %s.", serverName, legacySecurityDomainName);
                        final PathAddress pathAddress = messagingSubsystemResource.getResourcePathAddress().append(SERVER, serverName);
                        compositeOperationBuilder.addStep(getUndefineAttributeOperation(pathAddress, SECURITY_DOMAIN));
                        compositeOperationBuilder.addStep(getWriteAttributeOperation(pathAddress, ELYTRON_DOMAIN, elytronSecurityDomainName));
                        taskContext.getLogger().debugf("Migrated messaging-activemq subsystem server %s to Elytron security-domain %s.", serverName, elytronSecurityDomainName);
                        updated = true;
                    }
                }
            }
            if (updated) {
                
            }
            return updated;
        }

        protected boolean addSubsystemDependenciesMigrationOperationSteps(String elytronSecurityDomainName, LegacySecurityDomain legacySecurityDomain, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            boolean updated = false;
            final Logger logger = taskContext.getLogger();

            final String legacySecurityDomainName = legacySecurityDomain.getName();
            final SubsystemResource messagingSubsystemResource = subsystemResource.getParentResource().getSubsystemResource(JBossSubsystemNames.MESSAGING_ACTIVEMQ);
            if (messagingSubsystemResource != null) {
                final ModelNode subsystemConfig = messagingSubsystemResource.getResourceConfiguration();
                for (Property serverProperty : subsystemConfig.get(SERVER).asPropertyList()) {
                    final String serverName = serverProperty.getName();
                    final ModelNode serverConfig = serverProperty.getValue();
                    final ModelNode serverSecurityDomainNode = serverConfig.get(SECURITY_DOMAIN);
                    final String messagingSubsystemSecurityDomain = serverSecurityDomainNode.isDefined() ? serverSecurityDomainNode.asString() : "other";
                    if (legacySecurityDomain.getName().equals(messagingSubsystemSecurityDomain)) {
                        logger.debugf("Found messaging-activemq subsystem server %s using the legacy security-domain %s.", serverName, legacySecurityDomainName);
                        final PathAddress pathAddress = messagingSubsystemResource.getResourcePathAddress().append(SERVER, serverName);
                        compositeOperationBuilder.addStep(getUndefineAttributeOperation(pathAddress, SECURITY_DOMAIN));
                        compositeOperationBuilder.addStep(getWriteAttributeOperation(pathAddress, ELYTRON_DOMAIN, elytronSecurityDomainName));
                        logger.debugf("Migrated messaging-activemq subsystem server %s to Elytron security-domain %s.", serverName, elytronSecurityDomainName);
                        updated = true;
                    }
                }
            }

            return updated;
        }

        protected boolean addRemotingLoginModuleOperationSteps(LegacySecurityDomain.LoginModule loginModule, LegacySecurityDomain.Authentication authentication, LegacySecurityDomain securityDomain, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            // nothing todo
            return false;
        }

        protected boolean addUsersRolesLoginModuleOperationSteps(LegacySecurityDomain.LoginModule loginModule, LegacySecurityDomain.Authentication authentication, LegacySecurityDomain securityDomain, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            // TODO similar steps to migrating a legacy security realm with properties authentication (but legacy may point to deployment files, which elytron does not supports
            taskContext.getLogger().warnf("Migration of legacy security domain %s's UsersRoles login module is not supported and will be ignored.", securityDomain.getName());
            return false;
        }

        protected boolean addAuthenticationJaspiOperationSteps(LegacySecurityDomain securityDomain, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            final LegacySecurityDomain.AuthenticationJaspi authentication = securityDomain.getAuthenticationJaspi();
            if (authentication != null) {
                taskContext.getLogger().warnf("Migration of legacy security domain %s's authentication-jaspi is not supported and will be ignored.", securityDomain.getName());
            }
            return false;
        }

        protected boolean addAuthorizationOperationSteps(LegacySecurityDomain securityDomain, LegacySecurityConfiguration legacySecurityConfiguration, SubsystemResource subsystemResource, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext taskContext) {
            final LegacySecurityDomain.Authorization authorization = securityDomain.getAuthorization();
            if (authorization != null) {
                taskContext.getLogger().warnf("Migration of legacy security domain %s's authorization is not supported and will be ignored.", securityDomain.getName());
            }
            return false;
        }
    }
}