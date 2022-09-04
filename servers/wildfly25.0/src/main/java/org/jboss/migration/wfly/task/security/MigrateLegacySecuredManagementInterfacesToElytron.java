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
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HTTP_UPGRADE_ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author emmartins
 */
public class MigrateLegacySecuredManagementInterfacesToElytron<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    private static final String TASK_NAME = "management.migrate-legacy-secured-management-interfaces";

    public MigrateLegacySecuredManagementInterfacesToElytron() {
        name(TASK_NAME);
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Configuring HTTP Upgrade Management..."));
        subtasks(ManageableServerConfigurationCompositeSubtasks.of(new SetManagementInterfacesHttpUpgradeEnabled<>(), new UpdateManagementHttpsSocketBindingPort<>()));
        afterRun(context -> context.getLogger().infof("HTTP Upgrade Management configured."));
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