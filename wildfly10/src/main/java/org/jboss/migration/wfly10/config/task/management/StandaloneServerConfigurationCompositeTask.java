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

package org.jboss.migration.wfly10.config.task.management;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.wfly10.config.management.ManageableResource;
import org.jboss.migration.wfly10.config.management.StandaloneServerConfiguration;

import java.util.Collection;

/**
 * @author emmartins
 */
public class StandaloneServerConfigurationCompositeTask<S> extends ManageableServerConfigurationCompositeTask<S, StandaloneServerConfiguration> {

    private StandaloneServerConfigurationCompositeTask(BaseBuilder<S, StandaloneServerConfiguration, ?> builder, S source, Collection<? extends ManageableResource> resources) {
        super(builder, source, resources);
    }

    public interface SubtaskExecutor<S> extends ManageableServerConfigurationCompositeTask.SubtaskExecutor<S, StandaloneServerConfiguration> {
    }

    public static class Builder<S> extends BaseBuilder<S, StandaloneServerConfiguration, Builder<S>> {

        public Builder(ServerMigrationTaskName taskName) {
            super(taskName);
        }

        @Override
        public ServerMigrationTask build(S source, Collection<? extends ManageableResource> resources) {
            return new StandaloneServerConfigurationCompositeTask<>(this, source, resources);
        }
    }
}
