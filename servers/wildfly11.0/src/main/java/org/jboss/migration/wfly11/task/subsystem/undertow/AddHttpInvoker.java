/*
 * Copyright 2017 Red Hat, Inc.
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
package org.jboss.migration.wfly11.task.subsystem.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which adds the http invoker.
 * @author emmartins
 */
public class AddHttpInvoker<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public static final String TASK_NAME = "add-http-invoker";
    private static final String SERVER_NAME = "default-server";
    private static final String HOST_NAME = "default-host";
    private static final String SETTING = "setting";
    private static final String HTTP_INVOKER = "http-invoker";
    private static final String SECURITY_REALM_NAME = "ApplicationRealm";

    public AddHttpInvoker() {
        subtaskName(TASK_NAME);
    }

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        /*
        "path" => undefined,
            "http-authentication-factory" => undefined,
            "security-realm" => "ApplicationRealm",
            "operation" => "add",
            "address" => [
                ("subsystem" => "undertow"),
                ("server" => "default-server"),
                ("host" => "default-host"),
                ("setting" => "http-invoker")
            ]
         */
        if (!config.hasDefined(SERVER, SERVER_NAME, HOST, HOST_NAME)) {
            context.getLogger().debugf("Undertow's host %s not defined, skipping task to configure the host's http-invoker.");
            return ServerMigrationTaskResult.SKIPPED;
        }
        if (!config.hasDefined(SERVER, SERVER_NAME, HOST, HOST_NAME, SETTING, HTTP_INVOKER)) {
            final PathAddress pathAddress = subsystemResource.getResourcePathAddress().append(SERVER, SERVER_NAME).append(HOST, HOST_NAME).append(SETTING, HTTP_INVOKER);
            final ModelNode addOp = Util.createEmptyOperation(ADD, pathAddress);
            addOp.get(SECURITY_REALM).set(SECURITY_REALM_NAME);
            subsystemResource.getServerConfiguration().executeManagementOperation(addOp);
            context.getLogger().debugf("Undertow's default host http-invoker configured.");
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            context.getLogger().debugf("Undertow's host %s http-invoker is defined, skipping task to configure it.");
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
