/*
 * Copyright 2018 Red Hat, Inc.
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

package org.jboss.migration.wfly10.config.task;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.SimpleComponentTask;
import org.jboss.migration.core.task.component.TaskSkipPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author emmartins
 */
public class InitializeTargetDir<S extends JBossServer<S>> extends SimpleComponentTask.Builder {
    public InitializeTargetDir(String name, Path targetDir, Path defaultTargetDir) {
        name(new ServerMigrationTaskName.Builder("initialize-target-dir").addAttribute("name", name).build());
        skipPolicies(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet(), context -> Files.isDirectory(targetDir));
        runnable(context -> {
            context.getLogger().debugf("Initializing target dir %s...", targetDir);
            if (defaultTargetDir != null) {
                context.getMigrationFiles().copy(defaultTargetDir, targetDir);
            } else {
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException e) {
                    throw new ServerMigrationFailureException(e);
                }
            }
            context.getLogger().debugf("Target's dir %s initialized.", targetDir);
            return ServerMigrationTaskResult.SUCCESS;
        });
    }
}
