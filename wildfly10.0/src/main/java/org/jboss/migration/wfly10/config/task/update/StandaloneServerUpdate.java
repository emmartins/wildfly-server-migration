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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.WildFlyServer10;
import org.jboss.migration.wfly10.config.task.StandaloneServerConfigurationsMigration;
import org.jboss.migration.wfly10.config.task.StandaloneServerMigration;

/**
 * @author emmartins
 */
public class StandaloneServerUpdate<S extends JBossServer<S>> extends StandaloneServerMigration<S> {
    public StandaloneServerUpdate(StandaloneServerConfigurationsMigration<S, ?> configFilesMigration) {
        super(configFilesMigration);
    }

    @Override
    protected void beforeConfigurationsMigration(S source, WildFlyServer10 target, TaskContext context) {
        context.getConsoleWrapper().println();
        context.execute(new MigrateContentDir<>("standalone", source.getStandaloneContentDir(), target.getStandaloneContentDir()).build());
        super.beforeConfigurationsMigration(source, target, context);
    }
}
