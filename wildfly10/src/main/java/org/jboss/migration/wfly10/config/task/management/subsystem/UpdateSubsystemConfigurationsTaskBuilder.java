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

import org.jboss.migration.core.task.AbstractServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesBuildParameters;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourcesCompositeTask;

import java.util.Collection;

/**
 * @author emmartins
 */
public class UpdateSubsystemConfigurationsTaskBuilder<S> extends ManageableResourcesCompositeTask.Builder<S, SubsystemConfiguration> {

    protected
    public UpdateSubsystemConfigurationsTaskBuilder(String subsystem) {
        name(new ServerMigrationTaskName.Builder("update-subsystem").addAttribute("name", subsystem).build());
        beforeRun(context -> context.getLogger().infof("Updating subsystem %s configuration(s)...", subsystem));
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().infof("Subsystem %s configuration(s) updated.", subsystem);
            } else {
                context.getLogger().infof("No subsystem %s configuration(s) updated.", subsystem);
            }
        });
    }

    @Override
    public <P1 extends ManageableResourcesBuildParameters<S, SubsystemConfiguration>> ServerMigrationTask build(P1 params) {
        // params is the "context", we
        return super.build(params);
    }

    public static class Builder<S> extends BaseBuilder<S, Builder<S>> {
        public Builder(final String subsystem) {
            super(subsystem, );
            listener(new AbstractServerMigrationTask.Listener() {
                @Override
                public void started(TaskContext context) {
                    ;
                }
                @Override
                public void done(TaskContext context) {
                    if (context.hasSucessfulSubtasks()) {
                        context.getLogger().infof("Subsystem %s configuration(s) updated.", subsystem);
                    } else {
                        context.getLogger().infof("No subsystem %s configuration(s) updated.", subsystem);
                    }
                }
            });
        }
        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new UpdateSubsystemConfigurationsTaskBuilder<>(this, source, resources);
        }
    }
}
