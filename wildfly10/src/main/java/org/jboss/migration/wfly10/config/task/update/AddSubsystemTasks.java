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

import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemConfigurationTaskBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.ExtensionNames;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.jberet.AddBatchJBeretSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.securitymanager.AddSecurityManagerSubsystem;
import org.jboss.migration.wfly10.config.task.subsystem.singleton.AddSingletonSubsystem;

/**
 * @author emmartins
 */
public class AddSubsystemTasks {

    public static final AddSubsystemConfigurationTaskBuilder BEAN_VALIDATION = new AddSubsystemConfigurationTaskBuilder(ExtensionNames.BEAN_VALIDATION, SubsystemNames.BEAN_VALIDATION);

    public static final AddSubsystemConfigurationTaskBuilder BATCH_JBERET = new AddSubsystemConfigurationTaskBuilder(ExtensionNames.BATCH_JBERET, AddBatchJBeretSubsystem.INSTANCE);

    public static final AddSubsystemConfigurationTaskBuilder REQUEST_CONTROLLER = new AddSubsystemConfigurationTaskBuilder(ExtensionNames.REQUEST_CONTROLLER, SubsystemNames.REQUEST_CONTROLLER);

    public static final AddSubsystemConfigurationTaskBuilder SECURITY_MANAGER = new AddSubsystemConfigurationTaskBuilder(ExtensionNames.SECURITY_MANAGER, AddSecurityManagerSubsystem.INSTANCE);

    public static final AddSubsystemConfigurationTaskBuilder SINGLETON = new AddSubsystemConfigurationTaskBuilder(ExtensionNames.SINGLETON, AddSingletonSubsystem.INSTANCE);
}
