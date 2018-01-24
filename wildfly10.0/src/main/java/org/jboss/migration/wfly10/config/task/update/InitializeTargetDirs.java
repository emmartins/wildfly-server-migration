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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.core.ServerMigrationFailureException;
import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.SimpleComponentTask;
import org.jboss.migration.core.task.component.SimpleComponentTaskBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class InitializeTargetDirs {

    protected final SimpleComponentTask.Builder builder;
    protected final List<SimpleComponentTaskBuilder> subtasks;

    public InitializeTargetDirs(String targetDirsName) {
        builder = new SimpleComponentTask.Builder()
                .name("initialize-target-"+targetDirsName+"-dirs")
                .beforeRun(context -> context.getLogger().debugf("Initializing target's %s dirs....", targetDirsName))
                .afterRun(context -> {
                    if (context.hasSucessfulSubtasks()) {
                        context.getLogger().infof("Target's %s dirs initialized.", targetDirsName);
                    }
                });
        subtasks = new ArrayList<>();
    }

    public InitializeTargetDirs targetDir(String name, Path targetDir, Path defaultTargetDir) {
        final SimpleComponentTaskBuilder subtask = new SimpleComponentTask.Builder()
                .name(new ServerMigrationTaskName.Builder("initialize-target-dir").addAttribute("name", name).build())
                .skipPolicy(context -> Files.isDirectory(targetDir))
                .beforeRun(context -> context.getLogger().debugf("Initializing target dir %s...", targetDir))
                .runnable(context -> {
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
        subtasks.add(subtask);
        return this;
    }

    public ServerMigrationTask build() {
        return builder.subtasks(subtasks.toArray(new SimpleComponentTaskBuilder[subtasks.size()])).build();
    }
}
