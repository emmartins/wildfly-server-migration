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
package org.jboss.migration.wfly10.config.task.subsystem.remoting;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;

/**
 * A task which adds the default http connector, if missing from the remote subsystem config.
 * @author emmartins
 */
public class AddHttpConnectorIfMissing<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public interface EnvironmentProperties {
        String HTTP_CONNECTOR_NAME = "httpConnectorName";
        String CONNECTOR_REF_NAME = "connectorRefName";
        String SECURITY_REALM_NAME = "securityRealmName";
    }

    public static final String DEFAULT_HTTP_CONNECTOR_NAME = "http-remoting-connector";
    public static final String DEFAULT_CONNECTOR_REF_NAME = "http";
    public static final String DEFAULT_SECURITY_REALM_NAME = "ApplicationRealm";

    public static final String TASK_NAME = "add-remoting-http-connector";

    public AddHttpConnectorIfMissing() {
        subtaskName(TASK_NAME);
    }

    private static final String HTTP_CONNECTOR = "http-connector";
    private static final String CONNECTOR_REF = "connector-ref";
    private static final String SECURITY_REALM = "security-realm";

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext taskContext, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration serverConfiguration = subsystemResource.getServerConfiguration();
        // read env properties
        final String httpConnectorName = taskEnvironment.getPropertyAsString(EnvironmentProperties.HTTP_CONNECTOR_NAME, DEFAULT_HTTP_CONNECTOR_NAME);
        final String connectorRefName = taskEnvironment.getPropertyAsString(EnvironmentProperties.CONNECTOR_REF_NAME, DEFAULT_CONNECTOR_REF_NAME);
        final String securityRealmName = taskEnvironment.getPropertyAsString(EnvironmentProperties.SECURITY_REALM_NAME, DEFAULT_SECURITY_REALM_NAME);
        // if not defined add http connector
        if (!config.hasDefined(HTTP_CONNECTOR, httpConnectorName)) {
            final PathAddress httpRemotingConnectorPathAddress = subsystemPathAddress.append(PathElement.pathElement(HTTP_CONNECTOR, httpConnectorName));
            final ModelNode httpRemotingConnectorAddOp = Util.createEmptyOperation(ADD, httpRemotingConnectorPathAddress);
            httpRemotingConnectorAddOp.get(CONNECTOR_REF).set(connectorRefName);
            httpRemotingConnectorAddOp.get(SECURITY_REALM).set(securityRealmName);
            serverConfiguration.executeManagementOperation(httpRemotingConnectorAddOp);
            taskContext.getLogger().debugf("Http connector %s added to Remoting subsystem configuration.", httpConnectorName);
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
