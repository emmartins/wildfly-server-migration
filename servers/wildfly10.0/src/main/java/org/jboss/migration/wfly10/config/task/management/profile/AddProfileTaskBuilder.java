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
import org.jboss.migration.wfly10.config.management.ProfileResource;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemResources;

import static org.jboss.migration.core.task.component.TaskSkipPolicy.Builders.skipIfDefaultTaskSkipPropertyIsSet;

/**
 * @author emmartins
 */
public class AddProfileTaskBuilder<S> extends ManageableServerConfigurationCompositeTask.BaseBuilder<S, AddProfileTaskBuilder<S>> {

    protected final ManageableServerConfigurationCompositeSubtasks.Builder<S> subtasks;
    protected final String profileName;

    public AddProfileTaskBuilder(String profileName) {
        this.profileName = profileName;
        name("profile."+profileName+".add");
        skipPolicyBuilders(skipIfDefaultTaskSkipPropertyIsSet(),
                buildParameters -> context -> {
                    if (!buildParameters.getServerConfiguration().findResources(ProfileResource.class, profileName).isEmpty()) {
                        context.getLogger().debugf("Profile %s already exists.", profileName);
                        return true;
                    } else {
                        return false;
                    }
                });
        beforeRun(context -> context.getLogger().debugf("Adding profile %s...", profileName));
        this.subtasks = new ManageableServerConfigurationCompositeSubtasks.Builder<S>()
                .subtask(ProfileResource.Parent.class, new CreateProfileTask<>(profileName));
        subtasks(subtasks);
        afterRun(context -> context.getLogger().infof("Profile %s added.", profileName));
    }

    protected void addSubsystemSubtasks(AddSubsystemResources<S>... addSubsystemSubtasks) {
        final ManageableResourceCompositeSubtasks.Builder<S, SubsystemResource.Parent> compositeSubtasks = new ManageableResourceCompositeSubtasks.Builder<>();
        for (AddSubsystemResources<S> subtask : addSubsystemSubtasks) {
            subtask.afterRun(null);
            compositeSubtasks.subtask(subtask);
        }
        this.subtasks.subtask(ProfileResource.class, profileName, compositeSubtasks);
    }

    @Override
    protected AddProfileTaskBuilder<S> getThis() {
        return this;
    }

    public static class CreateProfileTask<S> extends ManageableResourceLeafTask.Builder<S, ProfileResource.Parent> {
        protected CreateProfileTask(String profileName) {
            name(new ServerMigrationTaskName.Builder("create-profile").addAttribute("name", profileName).build());
            final ManageableResourceTaskRunnableBuilder<S, ProfileResource.Parent> runnableBuilder = params -> context -> {
                final ProfileResource.Parent resource = params.getResource();
                final PathAddress pathAddress = resource.getProfileResourcePathAddress(profileName);
                final ModelNode op = Util.createAddOperation(pathAddress);
                resource.getServerConfiguration().executeManagementOperation(op);
                context.getLogger().debugf("Profile %s created.", profileName);
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}
