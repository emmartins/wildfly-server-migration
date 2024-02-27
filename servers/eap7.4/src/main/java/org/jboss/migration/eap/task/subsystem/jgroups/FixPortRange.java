/*
 * Copyright 2023 Red Hat, Inc.
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
package org.jboss.migration.eap.task.subsystem.jgroups;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROPERTIES;

/**
 * A task which fixes jgroup subsystem configuration's wrt port_range properties.
 * @author emmartins
 */
public class FixPortRange<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    private static final String FD_SOCK = "FD_SOCK";
    private static final String PORT_RANGE = "port_range";
    private static final String PROTOCOL = "protocol";
    private static final String STACK = "stack";
    private static final String tcp = "tcp";
    private static final String TCP = "TCP";
    private static final String TRANSPORT = "transport";
    private static final String udp = "udp";
    private static final String UDP = "UDP";

    public FixPortRange() {
        subtaskName("fix-port_range");
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration serverConfiguration = subsystemResource.getServerConfiguration();
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        boolean update = false;
        if (updateStack(udp, UDP, config, subsystemPathAddress, compositeOperationBuilder, context)) {
            update = true;
        }
        if (updateStack(tcp, TCP, config, subsystemPathAddress, compositeOperationBuilder, context)) {
            update = true;
        }
        if (update) {
            serverConfiguration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }

    private boolean updateStack(String stack, String transport, ModelNode config, PathAddress subsystemPathAddress, Operations.CompositeOperationBuilder compositeOperationBuilder, TaskContext context) {
        final ModelNode stackNode = config.get(STACK,stack);
        if (!stackNode.isDefined()) {
            context.getLogger().debugf("JGroups stack %s is not defined, skipping config update.", stack);
            return false;
        }
        final ModelNode transportNode = stackNode.get(TRANSPORT,transport);
        if (!transportNode.isDefined()) {
            context.getLogger().debugf("JGroups transport %s is not defined, skipping config update.", transport);
            return false;
        }
        final ModelNode portRangeTransportNode = transportNode.get(PROPERTIES, PORT_RANGE);
        if (!portRangeTransportNode.isDefined()) {
            context.getLogger().debugf("JGroups transport %s property %s is not defined, skipping config update.", transport, PORT_RANGE);
            return false;
        }
        final ModelNode protocolNode = stackNode.get(PROTOCOL, FD_SOCK);
        if (!protocolNode.isDefined()) {
            context.getLogger().debugf("JGroups protocol %s is not defined, skipping config update.", FD_SOCK);
            return false;
        }
        ModelNode protocolNodeProperties = protocolNode.get(PROPERTIES);
        if (!protocolNodeProperties.isDefined()) {
            protocolNodeProperties = new ModelNode();
        }
        if (protocolNodeProperties.get(PORT_RANGE).isDefined()) {
            context.getLogger().debugf("JGroups protocol %s property %s is defined, skipping config update.", FD_SOCK, PORT_RANGE);
            return false;
        }

        // we found the faulty config use case, let's copy the transport's port_range property to the protocol
        protocolNodeProperties.get(PORT_RANGE).set(portRangeTransportNode.asString());
        final ModelNode addOp = Util.getWriteAttributeOperation(subsystemPathAddress.append(STACK, stack).append(PROTOCOL, FD_SOCK), PROPERTIES, protocolNodeProperties);
        compositeOperationBuilder.addStep(addOp);
        context.getLogger().debugf("JGroups protocol %s property %s configuration updated.", FD_SOCK, PORT_RANGE);
        return true;
    }
}