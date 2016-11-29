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

package org.jboss.migration.eap6.to.eap7.tasks;

import org.jboss.migration.wfly10.config.task.subsystem.UpdateSubsystemTaskFactory;

/**
 * @author emmartins
 */
public class EAP71SubsystemUpdates {

    public static final UpdateSubsystemTaskFactory INFINISPAN = EAP7SubsystemUpdates.INFINISPAN;

    public static final UpdateSubsystemTaskFactory EE = EAP7SubsystemUpdates.EE;

    public static final UpdateSubsystemTaskFactory EJB3 = EAP7SubsystemUpdates.EJB3;

    public static final UpdateSubsystemTaskFactory REMOTING = EAP7SubsystemUpdates.REMOTING;

    public static final UpdateSubsystemTaskFactory UNDERTOW = EAP7SubsystemUpdates.UNDERTOW;

    public static final UpdateSubsystemTaskFactory MESSAGING_ACTIVEMQ = EAP7SubsystemUpdates.MESSAGING_ACTIVEMQ;

}
