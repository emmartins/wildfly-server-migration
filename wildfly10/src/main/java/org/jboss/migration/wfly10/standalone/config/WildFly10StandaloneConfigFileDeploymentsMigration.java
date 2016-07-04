/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.standalone.config;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.ServerPath;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;

import java.io.IOException;
import java.util.List;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Migration logic with respect to the deployments found in a standalone config file being to WildFly 10.
 * @author emmartins
 */
public class WildFly10StandaloneConfigFileDeploymentsMigration<S extends Server> {

    public interface EnvironmentProperties {
        /**
         * the prefix for the name of deployments related properties
         */
        String PROPERTIES_PREFIX = "deployments.";
        /**
         * Boolean property which if true skips migration of deployments
         */
        String SKIP = PROPERTIES_PREFIX + "skip";
    }

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("deployments").build();
    public static final String SERVER_MIGRATION_TASK_DEPLOYMENT_REMOVAL_NAME = "remove-deployment";

    public ServerMigrationTask getServerMigrationTask(final ServerPath<S> source, final WildFly10StandaloneServer target) {
        return new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }

            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                if (!context.getServerMigrationContext().getMigrationEnvironment().getPropertyAsBoolean(EnvironmentProperties.SKIP, Boolean.FALSE)) {
                    context.getServerMigrationContext().getConsoleWrapper().printf("%n%n");
                    // remove all deployments, TODO add (user optional) functionality that copies deployment files.
                    context.getLogger().info("Deployments migration starting...");
                    final boolean targetStarted = target.isStarted();
                    if (!targetStarted) {
                        target.start();
                    }
                    try {
                        for (ModelNode deployment : getDeployments(target, context)) {
                            migrateDeployment(deployment, source, target, context);
                        }
                        context.getLogger().info("Deployments migration done.");
                    } finally {
                        if (!targetStarted) {
                            target.stop();
                        }
                    }
                }
                return context.hasSucessfulSubtasks() ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
            }
        };
    }

    private List<ModelNode> getDeployments(WildFly10StandaloneServer target, ServerMigrationTaskContext taskContext) throws IOException {
        final ModelNode op = Util.createEmptyOperation(READ_CHILDREN_RESOURCES_OPERATION, pathAddress());
        op.get(CHILD_TYPE).set(DEPLOYMENT);
        op.get(RECURSIVE).set(true);
        final ModelNode result = target.getModelControllerClient().execute(op);
        taskContext.getLogger().debugf("Deployments config: %s", result.asString());
        if(!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new RuntimeException("Failed to retrieve deployments from server.");
        }
        return result.get(RESULT).asList();
    }

    protected void migrateDeployment(final ModelNode deployment, final ServerPath<S> source, final WildFly10StandaloneServer target, final ServerMigrationTaskContext context) throws IOException {
        final Property deploymentAsProperty = deployment.asProperty();
        final String deploymentName = deploymentAsProperty.getName();
        final ServerMigrationTaskName taskName = new ServerMigrationTaskName.Builder().setName(SERVER_MIGRATION_TASK_DEPLOYMENT_REMOVAL_NAME).addAttribute("name", deploymentName).build();
        final ServerMigrationTask task = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return taskName;
            }
            @Override
            public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
                final ModelNode op = Util.createRemoveOperation( pathAddress(pathElement(DEPLOYMENT,deploymentName)));
                target.executeManagementOperation(op);
                context.getLogger().infof("Removed deployment %s", deploymentName);
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
        context.execute(task);
    }
}