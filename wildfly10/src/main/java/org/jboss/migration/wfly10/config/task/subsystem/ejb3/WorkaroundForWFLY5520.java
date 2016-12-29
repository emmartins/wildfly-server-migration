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
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which applies WFLY-5520 workaround when EAP 7.0.0.Beta1 is the target server.
 * @author emmartins
 */
public class WorkaroundForWFLY5520<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "apply-wfly-5520-fix";

    public WorkaroundForWFLY5520() {
        super(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final ManageableServerConfiguration configurationManagement = subsystemResource.getServerConfiguration();
        // this tmp workaround only needed when migrating into EAP 7.0.0.Beta1
        if (!configurationManagement.getServer().getProductInfo().getName().equals("EAP") || !configurationManagement.getServer().getProductInfo().getVersion().equals("7.0.0.Beta1")) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        if (config == null || !config.hasDefined("default-clustered-sfsb-cache")) {
            return ServerMigrationTaskResult.SKIPPED;
        }
        // /subsystem=ejb3:undefine-attribute(name=default-clustered-sfsb-cache)
        final PathAddress address = subsystemResource.getResourcePathAddress();
        ModelNode op = Util.createEmptyOperation(UNDEFINE_ATTRIBUTE_OPERATION, address);
        op.get(NAME).set("default-clustered-sfsb-cache");
        configurationManagement.executeManagementOperation(op);

        // /subsystem=ejb3:write-attribute(name=default-sfsb-cache,value=clustered)
        op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, address);
        op.get(NAME).set("default-sfsb-cache");
        op.get(VALUE).set("clustered");
        configurationManagement.executeManagementOperation(op);

        // /subsystem=ejb3:write-attribute(name=default-sfsb-passivation-disabled-cache,value=simple)
        op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, address);
        op.get(NAME).set("default-sfsb-passivation-disabled-cache");
        op.get(VALUE).set("simple");
        configurationManagement.executeManagementOperation(op);
        context.getLogger().infof("Target server does not includes fix for WFLY-5520, workaround applied into EJB3 subsystem configuration.");
        return ServerMigrationTaskResult.SUCCESS;
    }
}
