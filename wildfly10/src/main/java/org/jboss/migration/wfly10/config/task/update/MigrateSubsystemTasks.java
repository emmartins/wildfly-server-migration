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

package org.jboss.migration.wfly10.config.task.update;

import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.MigrateSubsystemTaskFactory;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

/**
 * @author emmartins
 */
public class MigrateSubsystemTasks {

    public static final MigrateSubsystemTaskFactory JACORB = new MigrateSubsystemTaskFactory(SubsystemNames.JACORB, ExtensionNames.JACORB);

    public static final MigrateSubsystemTaskFactory WEB = new MigrateSubsystemTaskFactory(SubsystemNames.WEB, ExtensionNames.WEB);

    public static final MigrateSubsystemTaskFactory MESSAGING = new MigrateSubsystemTaskFactory(SubsystemNames.MESSAGING, ExtensionNames.MESSAGING);
}
