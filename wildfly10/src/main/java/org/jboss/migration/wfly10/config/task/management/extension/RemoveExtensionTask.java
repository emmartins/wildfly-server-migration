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

import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.TaskContext;
import org.jboss.migration.wfly10.config.management.ExtensionConfiguration;

/**
 * A task which creates an extension if its missing from the server's config.
 * @author emmartins
 */
public class RemoveExtensionTask<S> extends AbstractExtensionTask<S> {

    public RemoveExtensionTask(String extensionModule) {
        super(extensionModule);
    }

    @Override
    protected ServerMigrationTaskName getName(S source, ExtensionConfiguration.Parent extensionResourceParent, TaskContext parentContext) {
        return new ServerMigrationTaskName.Builder("remove-extension").addAttribute("name", extensionModule).build();
    }

    @Override
    protected ServerMigrationTaskResult runTask(S source, ExtensionConfiguration.Parent extensionResourceParent, TaskContext context) throws Exception {
        if (extensionResourceParent.getExtensionConfigurationNames().contains(extensionModule)) {
            context.getLogger().debugf("Removing Extension %s...", extensionModule);
            extensionResourceParent.removeExtensionConfiguration(extensionModule);
            context.getLogger().infof("Extension %s removed.",extensionModule);
            return ServerMigrationTaskResult.SUCCESS;
        } else {
            context.getLogger().debugf("Skipped extension %s removal, doesn't exists in config.", extensionModule);
            return ServerMigrationTaskResult.SKIPPED;
        }
    }
}
