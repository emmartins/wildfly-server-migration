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

package org.jboss.migration.wfly10.config.task.management.extension;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ExtensionsManagement;

/**
 * A task which creates an extension if its missing from the server's config.
 * @author emmartins
 */
public class RemoveExtensionSubtask<S> extends AbstractExtensionSubtask<S> {

    public RemoveExtensionSubtask(String extensionModule) {
        super(extensionModule);
    }

    @Override
    protected ServerMigrationTaskName getName(S source, ExtensionsManagement extensionsManagement, TaskContext parentContext) {
        return new ServerMigrationTaskName.Builder("remove-extension").addAttribute("name", extensionModule).build();
    }

    @Override
    protected ServerMigrationTaskResult runTask(S source, ExtensionsManagement extensionsManagement, TaskContext context) throws Exception {
        if (extensionsManagement.getResourceNames().contains(extensionModule)) {
            context.getLogger().debugf("Removing Extension %s...", extensionModule);
            final ModelNode op = Util.createRemoveOperation(extensionsManagement.getResourcePathAddress(extensionModule));
            extensionsManagement.getServerConfiguration().executeManagementOperation(op);
            context.getLogger().infof("Extension %s removed.",extensionModule);
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            context.getLogger().debugf("Skipped extension %s removal, doesn't exists in config.", extensionModule);
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
