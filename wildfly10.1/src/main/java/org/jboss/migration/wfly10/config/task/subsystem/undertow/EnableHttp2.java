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
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which updates the config of the default http listener.
 * @author emmartins
 */
public class EnableHttp2<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "enable-http2";

    private static final String SERVER_NAME = "default-server";
    private static final String HTTP_LISTENER = "http-listener";
    private static final String HTTPS_LISTENER = "https-listener";
    private static final String ENABLE_HTTP2 = "enable-http2";

    public EnableHttp2() {
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress configPathAddress = subsystemResource.getResourcePathAddress();
        final String configName = subsystemResource.getResourceAbsoluteName();
        final PathAddress serverPathAddress = configPathAddress.append(SERVER, SERVER_NAME);
        if (!config.hasDefined(SERVER, SERVER_NAME)) {
            context.getLogger().debugf("Skipping task, server '%s' not found in Undertow's config %s", serverPathAddress.toCLIStyleString(), configName);
            return ServerMigrationTaskResult.SKIPPED;
        }
        final ModelNode server = config.get(SERVER, SERVER_NAME);
        final Set<String> updatedHttpListeners = new HashSet<>();
        if (server.hasDefined(HTTP_LISTENER)) {
            for (String listenerName : server.get(HTTP_LISTENER).keys()) {
                final ModelNode listener = server.get(HTTP_LISTENER, listenerName);
                if (!listener.hasDefined(ENABLE_HTTP2) || !listener.get(ENABLE_HTTP2).asBoolean()) {
                    final PathAddress listenerPathAddress = serverPathAddress.append(HTTP_LISTENER, listenerName);
                    final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, listenerPathAddress);
                    op.get(NAME).set(ENABLE_HTTP2);
                    op.get(VALUE).set(true);
                    subsystemResource.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().debugf("HTTP2 enabled for Undertow's HTTP Listener %s.", listenerPathAddress.toCLIStyleString());
                    updatedHttpListeners.add(listenerName);
                }
            }
        }
        final Set<String> updatedHttpsListeners = new HashSet<>();
        if (server.hasDefined(HTTPS_LISTENER)) {
            for (String listenerName : server.get(HTTPS_LISTENER).keys()) {
                final ModelNode listener = server.get(HTTPS_LISTENER, listenerName);
                if (!listener.hasDefined(ENABLE_HTTP2) || !listener.get(ENABLE_HTTP2).asBoolean()) {
                    final PathAddress listenerPathAddress = serverPathAddress.append(HTTPS_LISTENER, listenerName);
                    final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, listenerPathAddress);
                    op.get(NAME).set(ENABLE_HTTP2);
                    op.get(VALUE).set(true);
                    subsystemResource.getServerConfiguration().executeManagementOperation(op);
                    context.getLogger().debugf("HTTP2 enabled for Undertow's HTTPS Listener %s.", listenerPathAddress.toCLIStyleString());
                    updatedHttpsListeners.add(listenerName);
                }
            }
        }
        return new ServerMigrationTaskResult.Builder()
                .success()
                .addAttribute("http-listeners-updated", updatedHttpListeners)
                .addAttribute("https-listeners-updated", updatedHttpsListeners)
                .build();
    }
}
