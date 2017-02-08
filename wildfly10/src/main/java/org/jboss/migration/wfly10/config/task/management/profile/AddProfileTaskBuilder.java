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
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceTaskRunnableBuilder;

/**
 * @author emmartins
 */
public class AddProfileTaskBuilder<S> extends ManageableServerConfigurationCompositeTask.BaseBuilder<S, AddProfileTaskBuilder<S>> {

    protected final ManageableResourceCompositeSubtasks.Builder<S, SubsystemConfiguration.Parent> subsystemSubtasks;
    protected final ManageableServerConfigurationCompositeSubtasks.Builder<S> subtasks;

    public AddProfileTaskBuilder(String profileName) {
        name("add-profile-"+profileName);
        skipPolicyBuilder(buildParameters -> TaskSkipPolicy.skipIfAnySkips(
                TaskSkipPolicy.skipIfDefaultSkipPropertyIsSet(),
                context -> {
                    if (!buildParameters.getServerConfiguration().findResources(ProfileResource.class, profileName).isEmpty()) {
                        context.getLogger().infof("Profile %s already exists.", profileName);
                        return true;
                    } else {
                        return false;
                    }
                }));
        beforeRun(context -> context.getLogger().infof("Configuring profile %s...", profileName));
        this.subsystemSubtasks = new ManageableResourceCompositeSubtasks.Builder<>();
        this.subtasks = new ManageableServerConfigurationCompositeSubtasks.Builder<S>()
                .subtask(ProfileResource.Parent.class, new CreateProfileTask<>(profileName))
                .subtask(ProfileResource.class, profileName, this.subsystemSubtasks);
        subtasks(subtasks);
        afterRun(context -> context.getLogger().infof("Profile %s configured.", profileName));
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
                context.getLogger().infof("Profile %s created.", profileName);
                return ServerMigrationTaskResult.SUCCESS;
            };
            runBuilder(runnableBuilder);
        }
    }
}
