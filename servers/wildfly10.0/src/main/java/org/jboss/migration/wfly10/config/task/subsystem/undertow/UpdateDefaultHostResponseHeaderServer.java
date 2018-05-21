/*
 * Copyright 2016 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task.subsystem.undertow;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;

/**
 * @author emmartins
 */
public class UpdateDefaultHostResponseHeaderServer<S> extends SetDefaultHostResponseHeaderServer<S> {
    @Override
    protected String getHeaderValue(boolean defined, ModelNode config, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        if (!defined) {
            return null;
        }
        return super.getHeaderValue(defined, config, subsystemResource, context, taskEnvironment);
    }
}
