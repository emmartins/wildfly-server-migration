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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ManagementInterfaceResource;
import org.jboss.migration.wfly10.config.management.SocketBindingResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Setup EAP 7 http upgrade management.
 * @author emmartins
 */
public class SetupHttpUpgradeManagement<S> extends ServerConfigurationCompositeTask.Builder<S> {

    private static final String TASK_NAME = "setup-http-upgrade-management";

    public SetupHttpUpgradeManagement() {
        name(TASK_NAME);
        skipPolicy(TaskSkipPolicy.Builders.skipIfDefaultSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().infof("HTTP upgrade management setup starting..."));
        subtasks(ServerConfigurationCompositeSubtasks.of(new SetManagementInterfacesHttpUpgradeEnabled<>(), new UpdateManagementHttpsSocketBindingPort<>()));
        afterRun(context -> context.getLogger().infof("HTTP upgrade management setup completed."));
    }

    public static class SetManagementInterfacesHttpUpgradeEnabled<S> extends ServerConfigurationLeafTask.Builder<S> {

        private static final String SUBTASK_NAME = "set-management-interfaces-http-upgrade-enabled";
        private static final String MANAGEMENT_INTERFACE_NAME = "http-interface";

        protected SetManagementInterfacesHttpUpgradeEnabled() {
            name(SUBTASK_NAME);
            skipPolicy(TaskSkipPolicy.skipIfAnyPropertyIsSet(TASK_NAME+"."+SUBTASK_NAME+".skip"));
            final ResourceTaskRunnableBuilder<S, ManagementInterfaceResource> runnableBuilder = (params, taskName) -> context -> {
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
                context.getLogger().infof("Management interface '%s' http upgrade enabled.", MANAGEMENT_INTERFACE_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(ManagementInterfaceResource.class, MANAGEMENT_INTERFACE_NAME, runnableBuilder);
        }
    }

    static class UpdateManagementHttpsSocketBindingPort<S> extends ServerConfigurationLeafTask.Builder<S> {

        private static final String SUBTASK_NAME = "update-management-https-socket-binding-port";

        public interface EnvironmentProperties {
            /**
             * the prefix for the name of the management-https socket binding related properties
             */
            String PROPERTIES_PREFIX = TASK_NAME + "." + SUBTASK_NAME + ".";

            String PORT = PROPERTIES_PREFIX + "port";
        }

        public static final String DEFAULT_PORT = "${jboss.management.https.port:9993}";
        private final String SOCKET_BINDING_NAME = "management-https";
        private final String SOCKET_BINDING_PORT_ATTR = "port";

        protected UpdateManagementHttpsSocketBindingPort() {
            name("set-management-interfaces-http-upgrade-enabled");
            skipPolicy((buildParameters, taskName) -> context -> {
                if(TaskSkipPolicy.skipIfAnyPropertyIsSet(TASK_NAME+"."+SUBTASK_NAME+".skip").isSkipped(context)) {
                    return true;
                }
                // only run on standalone configs
                if (!(buildParameters.getServerConfiguration() instanceof StandaloneServerConfiguration)) {
                    return true;
                }
                return false;
            });
            final ResourceTaskRunnableBuilder<S, SocketBindingResource> runnableBuilder = (params, taskName) -> context -> {
                final SocketBindingResource resource = params.getResource();
                final MigrationEnvironment env = context.getServerMigrationContext().getMigrationEnvironment();
                String envPropertyPort = env.getPropertyAsString(UpdateManagementHttpsSocketBindingPort.EnvironmentProperties.PORT);
                if (envPropertyPort == null || envPropertyPort.isEmpty()) {
                    envPropertyPort = DEFAULT_PORT;
                }
                // management-https binding found, update port
                final PathAddress pathAddress = resource.getResourcePathAddress();
                final ModelNode writeAttrOp = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress);
                writeAttrOp.get(NAME).set(SOCKET_BINDING_PORT_ATTR);
                writeAttrOp.get(VALUE).set(envPropertyPort);
                resource.getServerConfiguration().executeManagementOperation(writeAttrOp);
                context.getLogger().infof("Socket binding '%s' port set to "+envPropertyPort+".", SOCKET_BINDING_NAME);
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(SocketBindingResource.class, SOCKET_BINDING_NAME, runnableBuilder);
        }
    }
}