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

package org.jboss.migration.wfly9.to.wfly10;

import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationTaskBuilder;
import org.jboss.migration.wfly10.to.wfly10.WildFly10_0ToWildFly10_1SubsystemUpdates;

/**
 * @author emmartins
 */
public class WildFly9ToWildFly10_1SubsystemUpdates {

    public static final UpdateSubsystemConfigurationTaskBuilder INFINISPAN = WildFly10_0ToWildFly10_1SubsystemUpdates.INFINISPAN;

    public static final UpdateSubsystemConfigurationTaskBuilder UNDERTOW = WildFly10_0ToWildFly10_1SubsystemUpdates.UNDERTOW;

}
