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
package org.jboss.migration.wfly10.config.task.subsystem.ejb3;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResources;
import org.jboss.migration.wfly10.config.task.subsystem.UpdateSubsystemTaskFactory;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which changes EJB3 remote service, if presence in config, to ref the HTTP Remoting Conector.
 * @author emmartins
 */
public class RefHttpRemotingConnectorInEJB3Remote implements UpdateSubsystemTaskFactory.SubtaskFactory {

    public static final RefHttpRemotingConnectorInEJB3Remote INSTANCE = new RefHttpRemotingConnectorInEJB3Remote();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder("activate-ejb3-remoting-http-connector").build();

    private RefHttpRemotingConnectorInEJB3Remote() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemResources subsystemResources) {
        return new UpdateSubsystemTaskFactory.Subtask(config, subsystem, subsystemResources) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, UpdateSubsystemTaskFactory subsystem, SubsystemResources subsystemResources, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
                if (config == null || !config.hasDefined(SERVICE,"remote")) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                final PathAddress subsystemPathAddress = subsystemResources.getResourcePathAddress(subsystem.getName());
                final ManageableServerConfiguration configurationManagement = subsystemResources.getServerConfiguration();
                // /subsystem=ejb3/service=remote:write-attribute(name=connector-ref,value=http-remoting-connector)
                final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION,  subsystemPathAddress.append(pathElement(SERVICE,"remote")));
                op.get(NAME).set("connector-ref");
                op.get(VALUE).set("http-remoting-connector");
                configurationManagement.executeManagementOperation(op);
                context.getLogger().infof("EJB3 subsystem's remote service configured to use HTTP Remoting connector.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
