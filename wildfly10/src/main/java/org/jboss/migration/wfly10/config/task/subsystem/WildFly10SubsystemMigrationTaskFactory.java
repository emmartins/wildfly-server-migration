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
package org.jboss.migration.wfly10.config.task.subsystem;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.wfly10.config.management.SubsystemResources;

/**
 * The factory of a task which is part of a subsystem's migration logic.
 * @author emmartins
 */
public interface WildFly10SubsystemMigrationTaskFactory {
    /**
     * Retrieves the server migration task's runnable.
     * @param config the subsystem configuration
     * @param subsystem the subsystem
     * @param subsystemResources the target configuration subsystem management
     * @return
     */
    ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, SubsystemResources subsystemResources);
}
