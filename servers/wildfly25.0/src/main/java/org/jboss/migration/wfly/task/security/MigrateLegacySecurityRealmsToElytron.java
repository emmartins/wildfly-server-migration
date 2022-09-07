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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResources;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HTTP_UPGRADE_ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author emmartins
 */
public class MigrateLegacySecurityRealmsToElytron<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    private static final String TASK_NAME = "security.migrate-legacy-security-realms-to-elytron";

    public MigrateLegacySecurityRealmsToElytron(LegacySecurityConfigurations legacySecurityConfigurations) {
        name(TASK_NAME);
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Migrating legacy security realms to Elytron..."));
        //subtasks(ManageableServerConfigurationCompositeSubtasks.of(new UpdateElytronSubsystemm<>(legacySecurityConfigurations), new UpdateManagementHttpsSocketBindingPort<>()));
        afterRun(context -> context.getLogger().infof("Legacy security realms migrated to Elytron."));
    }

    public static class UpdateElytronSubsystemm<S> extends UpdateSubsystemResources<S> {
        public UpdateElytronSubsystemm(final LegacySecurityConfigurations legacySecurityConfigurations) {
            super(JBossSubsystemNames.ELYTRON,
                    new UpdateSubsystemResourceSubtaskBuilder<S>() {
                        @Override
                        protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext taskContext, TaskEnvironment taskEnvironment) {
                            LegacySecurityConfiguration legacySecurityConfiguration = legacySecurityConfigurations.getSecurityConfigurations().get(subsystemResource.getServerConfiguration().getConfigurationPath());
                            taskContext.getLogger().warn("Legacy config: "+legacySecurityConfiguration);
                            // update elytron config by adding properties-realm, security-domain, http-factory and sasl
                            return ServerMigrationTaskResult.SUCCESS;
                        }
                    });
        }
    }

    public static class UnsetDefaultHostResponseHeader<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

        public static final String TASK_NAME = "remove-response-header";
        private static final String SERVER_NAME = "default-server";
        private static final String HOST_NAME = "default-host";
        private static final String FILTER_REF = "filter-ref";

        private static final String CONFIGURATION = "configuration";
        private static final String FILTER = "filter";
        private static final String RESPONSE_HEADER = "response-header";
        private static final String HEADER_NAME = "header-name";

        protected final String filterName;
        protected final String headerName;

        public UnsetDefaultHostResponseHeader(String filterName, String headerName) {
            subtaskName(TASK_NAME+"."+filterName);
            this.filterName = filterName;
            this.headerName = headerName;
        }

        @Override
        protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
            final PathAddress configPathAddress = subsystemResource.getResourcePathAddress();
            // check if server is defined
            final PathAddress serverPathAddress = configPathAddress.append(PathElement.pathElement(SERVER, SERVER_NAME));
            if (!config.hasDefined(SERVER, SERVER_NAME)) {
                context.getLogger().debugf("Skipping task, server '%s' not found in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
                return ServerMigrationTaskResult.SKIPPED;
            }
            final ModelNode server = config.get(SERVER, SERVER_NAME);
            // check if host is defined
            final PathAddress defaultHostPathAddress = serverPathAddress.append(PathElement.pathElement(HOST, HOST_NAME));
            if (!server.hasDefined(HOST, HOST_NAME)) {
                context.getLogger().debugf("Skipping task, host '%s' not found in Undertow's config %s", defaultHostPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
                return ServerMigrationTaskResult.SKIPPED;
            }
            final ModelNode filter = config.get(CONFIGURATION, FILTER, RESPONSE_HEADER, filterName);
            if (!filter.isDefined()) {
                context.getLogger().debugf("Skipping task, filter name '%s' not found in Undertow's config %s", filterName, configPathAddress.toCLIStyleString());
                return ServerMigrationTaskResult.SKIPPED;
            }

            // verify the header name
            final ModelNode filterHeaderName = filter.get(HEADER_NAME);
            if (!filterHeaderName.isDefined() || !filterHeaderName.asString().equals(headerName)) {
                context.getLogger().debugf("Skipping task, filter name '%s' found in Undertow's config %s but header name is %s, expected was %s", filterName, configPathAddress.toCLIStyleString(), filterHeaderName.asString(), headerName);
                return ServerMigrationTaskResult.SKIPPED;
            }

            // remove the filter and ref
            final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
            if (server.hasDefined(HOST, HOST_NAME, FILTER_REF, filterName)) {
                final PathAddress filterRefPathAddress = defaultHostPathAddress.append(FILTER_REF, filterName);
                compositeOperationBuilder.addStep(Util.createRemoveOperation(filterRefPathAddress));
            }
            final PathAddress responseHeaderPathAddress = configPathAddress.append(CONFIGURATION, FILTER).append(RESPONSE_HEADER, filterName);
            compositeOperationBuilder.addStep(Util.createRemoveOperation(responseHeaderPathAddress));
            subsystemResource.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());

            context.getLogger().debugf("Filter '%s', with header '%s', removed from Undertow's config %s", filterName, headerName, configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    public static class SetManagementInterfacesHttpUpgradeEnabled<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

        private static final String MANAGEMENT_INTERFACE_NAME = "http-interface";
        private static final String SUBTASK_NAME = TASK_NAME + ".management-interface."+MANAGEMENT_INTERFACE_NAME+".enable-http-upgrade";

        protected SetManagementInterfacesHttpUpgradeEnabled() {
            name(SUBTASK_NAME);
            skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
            final ManageableResourceTaskRunnableBuilder<S, ManagementInterfaceResource> runnableBuilder = params -> context -> {
                // check if attribute is defined
                final ManagementInterfaceResource resource = params.getResource();
                final ModelNode resourceConfig = resource.getResourceConfiguration();
                if (resourceConfig.hasDefined(HTTP_UPGRADE_ENABLED) && resourceConfig.get(HTTP_UPGRADE_ENABLED).asBoolean()) {
                    context.getLogger().debugf("Management interface %s http upgrade already enabled.", MANAGEMENT_INTERFACE_NAME);
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // set attribute value
                final PathAddress pathAddress = resource.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(HTTP_UPGRADE_ENABLED);
                writeAttrOp.get(VALUE).set(true);
                resource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().debugf("Management interface '%s' http upgrade enabled.", MANAGEMENT_INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(ManagementInterfaceResource.class, MANAGEMENT_INTERFACE_NAME, runnableBuilder);
        }
    }

    static class UpdateManagementHttpsSocketBindingPort<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

        public static final String DEFAULT_PORT = "${jboss.management.https.port:9993}";
        private static final String SOCKET_BINDING_NAME = "management-https";
        private static final String SOCKET_BINDING_PORT_ATTR = "port";

        private static final String SUBTASK_NAME = TASK_NAME + ".socket-binding." + SOCKET_BINDING_NAME + ".update-port";

        protected UpdateManagementHttpsSocketBindingPort() {
            name(SUBTASK_NAME);
            skipPolicyBuilders(buildParameters -> TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet(),
                buildParameters -> context -> !(buildParameters.getServerConfiguration() instanceof StandaloneServerConfiguration));
            final ManageableResourceTaskRunnableBuilder<S, SocketBindingResource> runnableBuilder = params -> context -> {
                final SocketBindingResource resource = params.getResource();
                final TaskEnvironment taskEnvironment = new TaskEnvironment(context.getMigrationEnvironment(), context.getTaskName());
                String envPropertyPort = taskEnvironment.getPropertyAsString("port");
                if (envPropertyPort == null || envPropertyPort.isEmpty()) {
                    envPropertyPort = DEFAULT_PORT;
                }
                // management-https binding found, update port
                final PathAddress pathAddress = resource.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(SOCKET_BINDING_PORT_ATTR);
                writeAttrOp.get(VALUE).set(envPropertyPort);
                resource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().debugf("Socket binding '%s' port set to "+envPropertyPort+".", SOCKET_BINDING_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(SocketBindingResource.class, SOCKET_BINDING_NAME, runnableBuilder);
        }
    }
}