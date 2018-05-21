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

package org.jboss.migration.wfly13.task.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;

/**
 * A task which removes a response header filter from Undertow's default host config.
 * @author emmartins
 */
public abstract class UnsetDefaultHostResponseHeader<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "remove-response-header";

    private static final String SERVER_NAME = "default-server";
    private static final String HOST_NAME = "default-host";
    private static final String FILTER_REF = "filter-ref";

    private static final String CONFIGURATION = "configuration";
    private static final String FILTER = "filter";
    private static final String RESPONSE_HEADER = "response-header";
    private static final String HEADER_NAME = "header-name";

    protected final String filterName;
    protected final String headerName;

    public UnsetDefaultHostResponseHeader(String filterName, String headerName) {
        subtaskName(TASK_NAME+"."+filterName);
        this.filterName = filterName;
        this.headerName = headerName;
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress configPathAddress = subsystemResource.getResourcePathAddress();
        // check if server is defined
        final PathAddress serverPathAddress = configPathAddress.append(PathElement.pathElement(SERVER, SERVER_NAME));
        if (!config.hasDefined(SERVER, SERVER_NAME)) {
            context.getLogger().debugf("Skipping task, server '%s' not found in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SKIPPED;
        }
        final ModelNode server = config.get(SERVER, SERVER_NAME);
        // check if host is defined
        final PathAddress defaultHostPathAddress = serverPathAddress.append(PathElement.pathElement(HOST, HOST_NAME));
        if (!server.hasDefined(HOST, HOST_NAME)) {
            context.getLogger().debugf("Skipping task, host '%s' not found in Undertow's config %s", defaultHostPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SKIPPED;
        }
        final ModelNode filter = config.get(CONFIGURATION, FILTER, RESPONSE_HEADER, filterName);
        if (!filter.isDefined()) {
            context.getLogger().debugf("Skipping task, filter name '%s' not found in Undertow's config %s", filterName, configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SKIPPED;
        }

        // verify the header name
        final ModelNode filterHeaderName = filter.get(HEADER_NAME);
        if (!filterHeaderName.isDefined() || !filterHeaderName.asString().equals(headerName)) {
            context.getLogger().debugf("Skipping task, filter name '%s' found in Undertow's config %s but header name is %s, expected was %s", filterName, configPathAddress.toCLIStyleString(), filterHeaderName.asString(), headerName);
            return ServerMigrationTaskResult.SKIPPED;
        }

        // remove the filter and ref
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        if (server.hasDefined(HOST, HOST_NAME, FILTER_REF, filterName)) {
            final PathAddress filterRefPathAddress = defaultHostPathAddress.append(FILTER_REF, filterName);
            compositeOperationBuilder.addStep(Util.createRemoveOperation(filterRefPathAddress));
        }
        final PathAddress responseHeaderPathAddress = configPathAddress.append(CONFIGURATION, FILTER).append(RESPONSE_HEADER, filterName);
        compositeOperationBuilder.addStep(Util.createRemoveOperation(responseHeaderPathAddress));
        subsystemResource.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());

        context.getLogger().debugf("Filter '%s', with header '%s', removed from Undertow's config %s", filterName, headerName, configPathAddress.toCLIStyleString());
        return ServerMigrationTaskResult.SUCCESS;
    }
}
