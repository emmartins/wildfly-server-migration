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

package org.jboss.migration.wfly10.config.task.management.subsystem;

import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resources.ManageableResourcesCompositeTask;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * @author emmartins
 */
public class UpdateSubsystemResources<S> extends ManageableResourcesCompositeTask.Builder<S, ManageableResource> {

    public UpdateSubsystemResources(String subsystemName, UpdateSubsystemResourceSubtaskBuilder<S>... subtasks) {
        name("subsystem."+subsystemName+".update");
        skipPolicy(skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debugf("Updating subsystem %s configuration(s)...", subsystemName));
        final ManageableResourceCompositeSubtasks.Builder<S, SubsystemResource> subtasksBuilder = new ManageableResourceCompositeSubtasks.Builder<>();
        for (UpdateSubsystemResourceSubtaskBuilder<S> subtask : subtasks) {
            subtasksBuilder.subtask(subtask);
        }
        subtasks(SubsystemResource.class, subsystemName, subtasksBuilder);
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().debugf("Subsystem %s configuration(s) update complete.", subsystemName);
            } else {
                context.getLogger().debugf("No subsystem %s configuration(s) updated.", subsystemName);
            }
        });
    }
}
