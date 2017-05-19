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

package org.jboss.migration.wfly10.config.task.management.resource;

import org.jboss.migration.core.task.component.CompositeSubtasks;
import org.jboss.migration.wfly10.config.management.ManageableResource;

/**
 * @author emmartins
 */
public class ManageableResourceCompositeSubtasks<S, R extends ManageableResource> extends CompositeSubtasks<ManageableResourceBuildParameters<S, R>> {

    protected ManageableResourceCompositeSubtasks(BaseBuilder<S, R, ?> baseBuilder, ManageableResourceBuildParameters<S, R> params) {
        super(baseBuilder, params);
    }

    public abstract static class BaseBuilder<S, R extends ManageableResource, T extends BaseBuilder<S, R, T>> extends CompositeSubtasks.BaseBuilder<ManageableResourceBuildParameters<S, R>, T> implements ManageableResourceCompositeSubtasksBuilder<S, R, T> {
        @Override
        public ManageableResourceCompositeSubtasks build(ManageableResourceBuildParameters<S, R> params) {
            return new ManageableResourceCompositeSubtasks(this, params);
        }
    }

    public static class Builder<S, R extends ManageableResource> extends BaseBuilder<S, R, Builder<S, R>> {
        @Override
        protected Builder<S, R> getThis() {
            return this;
        }
    }

    public static <S, R extends ManageableResource> Builder<S, R> of(ManageableResourceComponentTaskBuilder<S, R, ?>... subtasks) {
        final Builder<S, R> builder = new Builder<>();
        for (ManageableResourceComponentTaskBuilder<S, R, ?> subtask : subtasks) {
            builder.subtask(subtask);
        }
        return builder;
    }
}
