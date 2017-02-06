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

package org.jboss.migration.wfly10.config.task.management.resources;

import org.jboss.migration.core.task.component.CompositeSubtasks;
import org.jboss.migration.wfly10.config.management.ManageableResource;

/**
 * @author emmartins
 */
public class ResourcesCompositeSubtasks<S, R extends ManageableResource> extends CompositeSubtasks<ResourcesBuildParameters<S, R>>  {

    protected ResourcesCompositeSubtasks(BaseBuilder<S, R, ?> baseBuilder, ResourcesBuildParameters<S, R> params) {
        super(baseBuilder, params);
    }

    public static abstract class BaseBuilder<S, R extends ManageableResource, T extends BaseBuilder<S, R, T>> extends CompositeSubtasks.BaseBuilder<ResourcesBuildParameters<S, R>, T> implements ResourcesCompositeSubtasksBuilder<S, R, T> {
        @Override
        public ResourcesCompositeSubtasks build(ResourcesBuildParameters<S, R> params) {
            return new ResourcesCompositeSubtasks(this, params);
        }
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<S, R, Builder<S, R>> {
        @Override
        protected Builder<S, R> getThis() {
            return this;
        }
    }

    public static <S, R extends ManageableResource> Builder<S, R> of(ResourcesComponentTaskBuilder<S, R, ?>... subtasks) {
        final Builder<S, R> builder = new Builder<>();
        for (ResourcesComponentTaskBuilder<S, R, ?> subtask : subtasks) {
            builder.subtask(subtask);
        };
        return builder;
    }
}
