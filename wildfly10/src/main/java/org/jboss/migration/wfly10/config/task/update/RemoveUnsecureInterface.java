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
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

/**
 * Removes Unsecure interface.
 * TODO lock for specific config type
 * @author emmartins
 */
public class RemoveUnsecureInterface<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    public RemoveUnsecureInterface() {
        name("remove-unsecure-interface");
        beforeRun(context -> context.getLogger().debugf("Removing unsecure interface..."));
        subtasks(InterfaceResource.class, "unsecure", ManageableResourceCompositeSubtasks.of(new Subtask<>()));
        afterRun(context -> {
          if (context.hasSucessfulSubtasks()) {
              context.getLogger().debugf("Unsecure interface configuration removed.");
          }
        });
    }

    public static class Subtask<S> extends ManageableResourceLeafTask.Builder<S, InterfaceResource> {
        protected Subtask() {
            name("remove-unsecure-interface-config");
            final ManageableResourceTaskRunnableBuilder<S, InterfaceResource> runnableBuilder = params-> context -> {
                params.getResource().remove();
                context.getLogger().info("Unsecure interface configuration removed.");
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}