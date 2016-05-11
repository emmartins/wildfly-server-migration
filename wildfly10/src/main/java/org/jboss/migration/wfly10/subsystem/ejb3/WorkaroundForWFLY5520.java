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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskId;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which applies WFLY-5520 workaround when EAP 7.0.0.Beta1 is the target server.
 * @author emmartins
 */
public class WorkaroundForWFLY5520 implements WildFly10SubsystemMigrationTaskFactory {

    public static final WorkaroundForWFLY5520 INSTANCE = new WorkaroundForWFLY5520();

    public static final ServerMigrationTaskId SERVER_MIGRATION_TASK_ID = new ServerMigrationTaskId.Builder().setName("apply-wfly-5520-fix").build();

    private WorkaroundForWFLY5520() {
    }

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskId getId() {
                return SERVER_MIGRATION_TASK_ID;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationTaskContext context) throws Exception {
                // this tmp workaround only needed when migrating into EAP 7.0.0.Beta1
                if (!server.getServer().getProductInfo().getName().equals("EAP") || !server.getServer().getProductInfo().getVersion().equals("7.0.0.Beta1")) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                if (config == null || !config.hasDefined("default-clustered-sfsb-cache")) {
                    return ServerMigrationTaskResult.SKIPPED;
                }
                // /subsystem=ejb3:undefine-attribute(name=default-clustered-sfsb-cache)
                final PathAddress address = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()));
                ModelNode op = Util.createEmptyOperation(UNDEFINE_ATTRIBUTE_OPERATION, address);
                op.get(NAME).set("default-clustered-sfsb-cache");
                server.executeManagementOperation(op);

                // /subsystem=ejb3:write-attribute(name=default-sfsb-cache,value=clustered)
                op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, address);
                op.get(NAME).set("default-sfsb-cache");
                op.get(VALUE).set("clustered");
                server.executeManagementOperation(op);

                // /subsystem=ejb3:write-attribute(name=default-sfsb-passivation-disabled-cache,value=simple)
                op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, address);
                op.get(NAME).set("default-sfsb-passivation-disabled-cache");
                op.get(VALUE).set("simple");
                server.executeManagementOperation(op);
                context.getLogger().infof("Target server does not includes fix for WFLY-5520, workaround applied into EJB3 subsystem configuration.");
                return ServerMigrationTaskResult.SUCCESS;
            }
        };
    }
}
