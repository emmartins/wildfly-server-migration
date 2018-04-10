/*
 * Copyright 2018 Red Hat, Inc.
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
package org.jboss.migration.wfly10.config.task.hostexclude;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.jboss.HostExclude;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.HostExcludeResource;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author emmartins
 */
public class AddHostExclude<S> extends ManageableResourceLeafTask.Builder<S, HostExcludeResource.Parent> {

    public AddHostExclude(HostExclude hostExclude) {
        final String hostExcludeName = hostExclude.getName();
        name("host-exclude."+hostExcludeName+".add");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Adding host-exclude %s...", hostExcludeName));
        runBuilder(params -> taskContext -> {
            final HostExcludeResource.Parent parent = params.getResource();
            // if migrated config has an host-exclude with same name, remove it
            if (parent.hasHostExcludeResource(hostExcludeName)) {
                parent.removeHostExcludeResource(hostExcludeName);
                taskContext.getLogger().debugf("Legacy host-exclude %s found and removed...", hostExclude.getName());
            }
            // add the host-exclude
            final PathAddress hostExcludePathAddress = parent.getHostExcludeResourcePathAddress(hostExclude.getName());
            final ModelNode addOp = Util.createAddOperation(hostExcludePathAddress);
            final HostExclude.ApiVersion apiVersion = hostExclude.getApiVersion();
            if (apiVersion != null) {
                addOp.get(MANAGEMENT_MAJOR_VERSION).set(apiVersion.getMajorVersion());
                addOp.get(MANAGEMENT_MINOR_VERSION).set(apiVersion.getMinorVersion());
                if (apiVersion.getMicroVersion() != null) {
                    addOp.get(MANAGEMENT_MICRO_VERSION).set(apiVersion.getMicroVersion());
                }
            }
            final HostExclude.Release hostRelease = hostExclude.getRelease();
            if (hostRelease != null) {
                addOp.get(HOST_RELEASE).set(hostRelease.getId());
            }
            final List<HostExclude.ExcludedExtension> excludedExtensions = hostExclude.getExcludedExtensions();
            if (excludedExtensions != null && !excludedExtensions.isEmpty()) {
                final ModelNode modelNode = addOp.get(EXCLUDED_EXTENSIONS).setEmptyList();
                for (HostExclude.ExcludedExtension excludedExtension : excludedExtensions) {
                    modelNode.add(excludedExtension.getModule());
                }
            }
            params.getServerConfiguration().executeManagementOperation(addOp);
            taskContext.getLogger().debugf("Host-exclude %s added.", hostExcludeName);
            return ServerMigrationTaskResult.SUCCESS;
        });
    }
}
