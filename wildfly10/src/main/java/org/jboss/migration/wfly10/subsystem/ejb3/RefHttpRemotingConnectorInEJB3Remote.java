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
package org.jboss.migration.wfly10.subsystem.ejb3;

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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which changes EJB3 remote service, if presence in config, to ref the HTTP Remoting Conector.
 * @author emmartins
 */
public class RefHttpRemotingConnectorInEJB3Remote implements WildFly10SubsystemMigrationTaskFactory {

    public static final RefHttpRemotingConnectorInEJB3Remote INSTANCE = new RefHttpRemotingConnectorInEJB3Remote();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("activate-ejb3-remoting-http-connector").build();

    private RefHttpRemotingConnectorInEJB3Remote() {
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
                if (config == null || !config.hasDefined(SERVICE,"remote")) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // /subsystem=ejb3/service=remote:write-attribute(name=connector-ref,value=http-remoting-connector)
                final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(SERVICE,"remote")));
                op.get(NAME).set("connector-ref");
                op.get(VALUE).set("http-remoting-connector");
                server.executeManagementOperation(op);
                context.getLogger().infof("EJB3 subsystem's remote service configured to use HTTP Remoting connector.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
