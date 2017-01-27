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
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationSubtask;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which adds Undertow's default https listener.
 * @author emmartins
 */
public class AddHttpsListener<S> extends UpdateSubsystemConfigurationSubtask<S> {

    public static final String TASK_NAME_NAME = "add-undertow-https-listener";
    private static final String SERVER_NAME = "default-server";
    private static final String HTTPS_LISTENER = "https-listener";
    private static final String HTTPS_LISTENER_NAME = "https";

    @Override
    public ServerMigrationTaskName getName(S source, SubsystemConfiguration subsystemConfiguration, TaskContext parentContext) {
        return new ServerMigrationTaskName.Builder(TASK_NAME_NAME).build();
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemConfiguration subsystemConfiguration, TaskContext context, TaskEnvironment taskEnvironment) throws Exception {
        final PathAddress configPathAddress = subsystemConfiguration.getResourcePathAddress();
        final PathAddress serverPathAddress = configPathAddress.append(SERVER, SERVER_NAME);
        if (!config.hasDefined(SERVER, SERVER_NAME)) {
            context.getLogger().debugf("Skipping task, server '%s' not found in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SKIPPED;
        }
        final ModelNode server = config.get(SERVER, SERVER_NAME);
        if (server.hasDefined(HTTPS_LISTENER, HTTPS_LISTENER_NAME)) {
            context.getLogger().debugf("Skipping task, https listener already defined by server '%s', in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
            return ServerMigrationTaskResult.SKIPPED;
        }
                /*
                add to server's config
                "https-listener" => {
                    "https" => {
                        "security-realm" => "ApplicationRealm",
                        "socket-binding" => "https"
                    }
                }
                */
        final PathAddress httpsListenerPathAddress = serverPathAddress.append(HTTPS_LISTENER, HTTPS_LISTENER_NAME);
        final ModelNode op = Util.createAddOperation(httpsListenerPathAddress);
        op.get(SOCKET_BINDING).set("https");
        op.get(SECURITY_REALM).set("ApplicationRealm");
        subsystemConfiguration.getServerConfiguration().executeManagementOperation(op);
        context.getLogger().infof("Default HTTPS listener added to server '%s', in Undertow's config %s", serverPathAddress.toCLIStyleString(), configPathAddress.toCLIStyleString());
        return ServerMigrationTaskResult.SUCCESS;
    }
}
