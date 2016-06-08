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
package org.jboss.migration.wfly10.subsystem.ee;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds the default EE Concurrency Utilities config to the subsystem.
 * @author emmartins
 */
public class AddConcurrencyUtilitiesDefaultConfig implements WildFly10SubsystemMigrationTaskFactory {

    public static final String DEFAULT_CONTEXT_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/context/default";
    public static final String DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME = "java:jboss/ee/concurrency/factory/default";
    public static final String DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/executor/default";
    public static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME = "java:jboss/ee/concurrency/scheduler/default";

    public static final String TASK_RESULT_ATTR_CONTEXT_SERVICE = "context-service";
    public static final String TASK_RESULT_ATTR_MANAGED_THREAD_FACTORY = "managed-thread-factory";
    public static final String TASK_RESULT_ATTR_MANAGED_EXECUTOR_SERVICE = "managed-executor-service";
    public static final String TASK_RESULT_ATTR_MANAGED_SCHEDULED_EXECUTOR_SERVICE = "managed-scheduled-executor-service";

    public static final AddConcurrencyUtilitiesDefaultConfig INSTANCE = new AddConcurrencyUtilitiesDefaultConfig();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("setup-ee-concurrency-utilities").build();

    private AddConcurrencyUtilitiesDefaultConfig() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathElement subsystemPathElement = pathElement(SUBSYSTEM, subsystem.getName());
                // add default context service
            /*
            "context-service" => {"default" => {
                    "jndi-name" => "java:jboss/ee/concurrency/context/default",
                    "use-transaction-setup-provider" => true
                }},
             */
                final ServerMigrationTaskResult.Builder taskResultBuilder = new ServerMigrationTaskResult.Builder();
                final PathAddress defaultContextServicePathAddress = pathAddress(subsystemPathElement, pathElement("context-service", "default"));
                final ModelNode defaultContextServiceAddOp = Util.createEmptyOperation(ADD, defaultContextServicePathAddress);
                defaultContextServiceAddOp.get("jndi-name").set(DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
                defaultContextServiceAddOp.get("use-transaction-setup-provider").set(true);
                server.executeManagementOperation(defaultContextServiceAddOp);
                context.getLogger().debugf("Default ContextService added to subsystem EE configuration.");
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_CONTEXT_SERVICE, DEFAULT_CONTEXT_SERVICE_JNDI_NAME);
                // add default managed thread factory
            /*
            "managed-thread-factory" => {"default" => {
                    "context-service" => "default",
                    "jndi-name" => "java:jboss/ee/concurrency/factory/default",
                    "priority" => 5
                }}
             */
                final PathAddress defaultManagedThreadFactoryPathAddress = pathAddress(subsystemPathElement, pathElement("managed-thread-factory", "default"));
                final ModelNode defaultManagedThreadFactoryAddOp = Util.createEmptyOperation(ADD, defaultManagedThreadFactoryPathAddress);
                defaultManagedThreadFactoryAddOp.get("jndi-name").set(DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
                defaultManagedThreadFactoryAddOp.get("context-service").set("default");
                defaultManagedThreadFactoryAddOp.get("priority").set(5);
                server.executeManagementOperation(defaultManagedThreadFactoryAddOp);
                context.getLogger().debugf("Default ManagedThreadFactory added to subsystem EE configuration.");
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_THREAD_FACTORY, DEFAULT_MANAGED_THREAD_FACTORY_JNDI_NAME);
                // add default managed executor service
            /*
            "managed-executor-service" => {"default" => {
                    "context-service" => "default",
                    "core-threads" => undefined,
                    "hung-task-threshold" => 60000L,
                    "jndi-name" => "java:jboss/ee/concurrency/executor/default",
                    "keepalive-time" => 5000L,
                    "long-running-tasks" => false,
                    "max-threads" => undefined,
                    "queue-length" => undefined,
                    "reject-policy" => "ABORT",
                    "thread-factory" => undefined
                }}
             */
                final PathAddress defaultManagedExecutorServicePathAddress = pathAddress(subsystemPathElement, pathElement("managed-executor-service", "default"));
                final ModelNode defaultManagedExecutorServiceAddOp = Util.createEmptyOperation(ADD, defaultManagedExecutorServicePathAddress);
                defaultManagedExecutorServiceAddOp.get("jndi-name").set(DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);
                defaultManagedExecutorServiceAddOp.get("context-service").set("default");
                defaultManagedExecutorServiceAddOp.get("hung-task-threshold").set(60000L);
                defaultManagedExecutorServiceAddOp.get("keepalive-time").set(5000L);
                defaultManagedExecutorServiceAddOp.get("long-running-tasks").set(false);
                defaultManagedExecutorServiceAddOp.get("reject-policy").set("ABORT");
                server.executeManagementOperation(defaultManagedExecutorServiceAddOp);
                context.getLogger().debugf("Default ManagedExecutorService added to subsystem EE configuration.");
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_EXECUTOR_SERVICE, DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI_NAME);

                // add default managed scheduled executor service
            /*
            "managed-scheduled-executor-service" => {"default" => {
                    "context-service" => "default",
                    "core-threads" => undefined,
                    "hung-task-threshold" => 60000L,
                    "jndi-name" => "java:jboss/ee/concurrency/scheduler/default",
                    "keepalive-time" => 3000L,
                    "long-running-tasks" => false,
                    "reject-policy" => "ABORT",
                    "thread-factory" => undefined
                }}
             */
                final PathAddress defaultManagedScheduledExecutorServicePathAddress = pathAddress(subsystemPathElement, pathElement("managed-scheduled-executor-service", "default"));
                final ModelNode defaultManagedScheduledExecutorServiceAddOp = Util.createEmptyOperation(ADD, defaultManagedScheduledExecutorServicePathAddress);
                defaultManagedScheduledExecutorServiceAddOp.get("jndi-name").set(DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);
                defaultManagedScheduledExecutorServiceAddOp.get("context-service").set("default");
                defaultManagedScheduledExecutorServiceAddOp.get("hung-task-threshold").set(60000L);
                defaultManagedScheduledExecutorServiceAddOp.get("keepalive-time").set(3000L);
                defaultManagedScheduledExecutorServiceAddOp.get("long-running-tasks").set(false);
                defaultManagedScheduledExecutorServiceAddOp.get("reject-policy").set("ABORT");
                server.executeManagementOperation(defaultManagedScheduledExecutorServiceAddOp);
                context.getLogger().debugf("Default ManagedScheduledExecutorService added to subsystem EE configuration.");
                taskResultBuilder.addAttribute(TASK_RESULT_ATTR_MANAGED_SCHEDULED_EXECUTOR_SERVICE, DEFAULT_MANAGED_SCHEDULED_EXECUTOR_SERVICE_JNDI_NAME);

                context.getLogger().infof("EE Concurrency Utilities added.");
                return taskResultBuilder.sucess().build();
            }
        };
    }
}
