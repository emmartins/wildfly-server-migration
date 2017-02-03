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

package org.jboss.migration.wfly10.config.task.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;

/**
 * A task which adds a response header filter to Undertow's default host config.
 * @author emmartins
 */
public class SetDefaultHostResponseHeader<S> extends UpdateSubsystemConfigurationSubtaskBuilder<S> {

    public static final String TASK_NAME_NAME = "add-default-host-response-header";

    private static final String SERVER_NAME = "default-server";
    private static final String HOST_NAME = "default-host";
    private static final String FILTER_REF = "filter-ref";

    private static final String CONFIGURATION = "configuration";
    private static final String FILTER = "filter";
    private static final String RESPONSE_HEADER = "response-header";
    private static final String HEADER_NAME = "header-name";
    private static final String HEADER_VALUE = "header-value";

    protected final String filterName;
    protected final String headerName;
    protected final String headerValue;

    public SetDefaultHostResponseHeader(String filterName, String headerName) {
        this(filterName, headerName, null);
    }

    public SetDefaultHostResponseHeader(String filterName, String headerName, String headerValue) {
        this.filterName = filterName;
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    protected String getHeaderValue(ModelNode config, SubsystemConfiguration subsystemConfiguration, TaskContext context, TaskEnvironment taskEnvironment) {
        return headerValue;
    }

    @Override
    public ServerMigrationTaskName getName(S source, SubsystemConfiguration subsystemConfiguration, TaskContext parentContext) {
        return new ServerMigrationTaskName.Builder(TASK_NAME_NAME).addAttribute(RESPONSE_HEADER, filterName).build();
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemConfiguration subsystemConfiguration, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
        final PathAddress configPathAddress = subsystemConfiguration.getResourcePathAddress();
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
        final ModelNode defaultHost = server.get(HOST, HOST_NAME);
        // add/update the response header
        final String headerValue = getHeaderValue(config, subsystemConfiguration, context, taskEnvironment);
        if (headerValue == null) {
            context.getLogger().debugf("Skipping task, null header-value");
            return ServerMigrationTaskResult.SKIPPED;
        }
        final PathAddress responseHeaderPathAddress = configPathAddress.append(CONFIGURATION, FILTER).append(RESPONSE_HEADER, filterName);
        if (!config.hasDefined(CONFIGURATION, FILTER, RESPONSE_HEADER, filterName)) {
            // response header not defined, add it
            if (!config.hasDefined(CONFIGURATION, FILTER)) {
                final ModelNode op = Util.createAddOperation(configPathAddress.append(CONFIGURATION, FILTER));
                subsystemConfiguration.getServerConfiguration().executeManagementOperation(op);
            }
            final ModelNode op = Util.createAddOperation(responseHeaderPathAddress);
            op.get(HEADER_NAME).set(headerName);
            op.get(HEADER_VALUE).set(headerValue);
            subsystemConfiguration.getServerConfiguration().executeManagementOperation(op);
        } else {
            // response header exists, update its header-value attr
            final ModelNode op = Util.getWriteAttributeOperation(responseHeaderPathAddress, HEADER_VALUE, headerValue);
            subsystemConfiguration.getServerConfiguration().executeManagementOperation(op);
        }
        // add filter-ref to default host, if missing
        if (!defaultHost.hasDefined(FILTER_REF, filterName)) {
            final PathAddress filterRefPathAddress = defaultHostPathAddress.append(FILTER_REF, filterName);
            final ModelNode op = Util.createAddOperation(filterRefPathAddress);
            subsystemConfiguration.getServerConfiguration().executeManagementOperation(op);
        }
        context.getLogger().infof("Response header '%s' set as '%s: %s' in Undertow's config %s", filterName, headerName, headerValue, configPathAddress.toCLIStyleString());
        return new ServerMigrationTaskResult.Builder()
                .success()
                .addAttribute(HEADER_NAME, headerName)
                .addAttribute(HEADER_VALUE, headerValue)
                .build();
    }
}
