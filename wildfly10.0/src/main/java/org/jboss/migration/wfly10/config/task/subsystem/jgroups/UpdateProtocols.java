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
package org.jboss.migration.wfly10.config.task.subsystem.jgroups;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A task which updates jgroup subsystem configuration's protocols.
 * @author emmartins
 */
public class UpdateProtocols<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    private final List<Operation> operations;

    public UpdateProtocols(Operations operations) {
        subtaskName("update-protocols");
        this.operations = Collections.unmodifiableList(operations.operations);
    }

    private static final String STACK = "stack";
    private static final String PROTOCOL = "protocol";

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration serverConfiguration = subsystemResource.getServerConfiguration();

        final ModelNode stacks = config.get(STACK);
        if (!stacks.isDefined()) {
            context.getLogger().debugf("No stacks defined.");
            return ServerMigrationTaskResult.SKIPPED;
        }
        final Set<String> protocolsRemoved = new HashSet<>();
        final Set<String> protocolsAdded = new HashSet<>();
        final org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder compositeOperationBuilder = org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder.create();
        for (Operation operation : operations) {
            for (String stackName : stacks.keys()) {
                final ModelNode stack = config.get(STACK, stackName);
                if (operation.removeProtocol != null) {
                    if (stack.hasDefined(PROTOCOL, operation.removeProtocol)) {
                        compositeOperationBuilder.addStep(Util.createRemoveOperation(subsystemPathAddress.append(STACK, stackName).append(PROTOCOL, operation.removeProtocol)));
                        protocolsRemoved.add(operation.removeProtocol);
                    } else {
                        continue;
                    }
                }
                if (operation.addProtocol != null) {
                    if (!stack.hasDefined(PROTOCOL, operation.addProtocol)) {
                        compositeOperationBuilder.addStep(Util.createAddOperation(subsystemPathAddress.append(STACK, stackName).append(PROTOCOL, operation.addProtocol)));
                        if (!protocolsRemoved.remove(operation.addProtocol)) {
                            protocolsAdded.add(operation.addProtocol);
                        }
                    }
                }
            }
        }
        if (protocolsRemoved.isEmpty() && protocolsAdded.isEmpty()) {
            context.getLogger().debugf("No protocols removed or added.");
            return ServerMigrationTaskResult.SKIPPED;
        }
        serverConfiguration.executeManagementOperation(compositeOperationBuilder.build().getOperation());
        return new ServerMigrationTaskResult.Builder()
                .success()
                .addAttribute("protocols-removed", protocolsRemoved)
                .addAttribute("protocols-added", protocolsAdded)
                .build();
    }

    public static class Operations {

        private final List<Operation> operations = new ArrayList<>();

        protected Operations operation(String removeProtocol, String addProtocol) {
            final Operation operation = new Operation();
            operation.removeProtocol = removeProtocol;
            operation.addProtocol = addProtocol;
            operations.add(operation);
            return this;
        }

        public Operations add(String protocol) {
            return operation(null, protocol);
        }

        public Operations readd(String protocol) {
            return operation(protocol, protocol);
        }

        public Operations replace(String oldProtocol, String newProtocol) {
            return operation(oldProtocol, newProtocol);
        }

        public Operations remove(String protocol) {
            return operation(protocol, null);
        }
    }

    protected static class Operation {
        String addProtocol;
        String removeProtocol;
    }
}