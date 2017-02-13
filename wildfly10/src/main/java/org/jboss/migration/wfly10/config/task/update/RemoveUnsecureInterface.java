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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.config.management.InterfaceResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationLeafTask;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * Removes Unsecure interface.
 * TODO lock for specific config type?
 * @author emmartins
 */
public class RemoveUnsecureInterface<S> extends ManageableServerConfigurationLeafTask.Builder<S> {

    public RemoveUnsecureInterface() {
        name("interface.unsecure.remove");
        skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Unsecure interface removal task starting..."));
        runBuilder(params -> context -> {
            InterfaceResource resource = params.getServerConfiguration().getInterfaceResource("unsecure");
            if (resource == null) {
                context.getLogger().debugf("Unsecure interface not found, skipping task.");
                return ServerMigrationTaskResult.SKIPPED;
            }
            resource.removeResource();
            context.getLogger().infof("Interface %s removed.", resource.getResourceAbsoluteName());
            return ServerMigrationTaskResult.SUCCESS;
        });
        afterRun(context -> context.getLogger().debugf("Unsecure interface removal task done."));
    }
}