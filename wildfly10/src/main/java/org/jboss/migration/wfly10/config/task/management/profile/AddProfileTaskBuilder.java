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

package org.jboss.migration.wfly10.config.task.management.profile;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ResourceTaskRunnableBuilder;

/**
 * @author emmartins
 */
public class AddProfileTaskBuilder<S> extends ServerConfigurationCompositeTask.BaseBuilder<S, AddProfileTaskBuilder<S>> {

    protected final ResourceCompositeSubtasks.Builder<S, SubsystemConfiguration.Parent> subsystemSubtasks;
    protected final ServerConfigurationCompositeSubtasks.Builder<S> subtasks;

    public AddProfileTaskBuilder(String profileName) {
        name("add-profile-"+profileName);
        skipPolicy((buildParameters, taskName) -> context -> {
            if (TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(taskName).isSkipped(context)) {
                return true;
            } else if (!buildParameters.getServerConfiguration().findResources(ProfileResource.class, profileName).isEmpty()) {
                context.getLogger().infof("Profile %s already exists.", profileName);
                return true;
            };
            return false;
        });
        beforeRun(context -> context.getLogger().infof("Configuring profile %s...", profileName));
        this.subsystemSubtasks = new ResourceCompositeSubtasks.Builder<>();
        this.subtasks = new ServerConfigurationCompositeSubtasks.Builder<S>()
                .subtask(ProfileResource.Parent.class, new CreateProfileTask<>(profileName))
                .subtask(ProfileResource.class, profileName, this.subsystemSubtasks);
        subtasks(subtasks);
        afterRun(context -> context.getLogger().infof("Profile %s configured.", profileName));
    }

    @Override
    protected AddProfileTaskBuilder<S> getThis() {
        return this;
    }

    public static class CreateProfileTask<S> extends ResourceLeafTask.Builder<S, ProfileResource.Parent> {
        protected CreateProfileTask(String profileName) {
            name(new ServerMigrationTaskName.Builder("create-profile").addAttribute("name", profileName).build());
            final ResourceTaskRunnableBuilder<S, ProfileResource.Parent> runnableBuilder = (params, taskName) -> context -> {
                final ProfileResource.Parent resource = params.getResource();
                final PathAddress pathAddress = resource.getProfileResourcePathAddress(profileName);
                final ModelNode op = Util.createAddOperation(pathAddress);
                resource.getServerConfiguration().executeManagementOperation(op);
                context.getLogger().infof("Profile %s created.", profileName);
                return ServerMigrationTaskResult.SUCCESS;
            };
            run(runnableBuilder);
        }
    }
}
