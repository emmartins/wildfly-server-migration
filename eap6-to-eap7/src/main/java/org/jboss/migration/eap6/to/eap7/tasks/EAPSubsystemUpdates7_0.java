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

import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationTaskBuilder;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.ee.AddConcurrencyUtilitiesDefaultConfig;
import org.jboss.migration.wfly10.config.task.subsystem.ee.AddDefaultBindingsConfig;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.AddInfinispanPassivationStoreAndDistributableCache;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.DefinePassivationDisabledCacheRef;
import org.jboss.migration.wfly10.config.task.subsystem.ejb3.RefHttpRemotingConnectorInEJB3Remote;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.AddEjbCache;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.AddServerCache;
import org.jboss.migration.wfly10.config.task.subsystem.infinispan.FixHibernateCacheModuleName;
import org.jboss.migration.wfly10.config.task.subsystem.messaging.AddHttpAcceptorsAndConnectors;
import org.jboss.migration.wfly10.config.task.subsystem.remoting.AddHttpConnectorIfMissing;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.AddBufferCache;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.AddWebsockets;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHostResponseHeaderServer;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHostResponseHeaderXPoweredBy;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHttpListenerRedirectSocket;

/**
 * @author emmartins
 */
public class EAPSubsystemUpdates7_0 {

    public static final UpdateSubsystemConfigurationTaskBuilder INFINISPAN = new UpdateSubsystemConfigurationTaskBuilder(SubsystemNames.INFINISPAN,
            new AddServerCache<>(),
            new AddEjbCache<>(),
            new FixHibernateCacheModuleName<>());

    public static final UpdateSubsystemConfigurationTaskBuilder EE = new UpdateSubsystemConfigurationTaskBuilder(SubsystemNames.EE,
            new AddConcurrencyUtilitiesDefaultConfig<>(),
            new AddDefaultBindingsConfig<>());

    public static final UpdateSubsystemConfigurationTaskBuilder EJB3 = new UpdateSubsystemConfigurationTaskBuilder(SubsystemNames.EJB3,
            new RefHttpRemotingConnectorInEJB3Remote<>(),
            new DefinePassivationDisabledCacheRef<>(),
            new AddInfinispanPassivationStoreAndDistributableCache<>());

    public static final UpdateSubsystemConfigurationTaskBuilder REMOTING = new UpdateSubsystemConfigurationTaskBuilder(SubsystemNames.REMOTING,
            new AddHttpConnectorIfMissing<>());

    public static final UpdateSubsystemConfigurationTaskBuilder UNDERTOW = new UpdateSubsystemConfigurationTaskBuilder<>(SubsystemNames.UNDERTOW,
            new AddBufferCache<>(),
            new SetDefaultHttpListenerRedirectSocket<>(),
            new AddWebsockets<>(),
            new SetDefaultHostResponseHeaderServer<>(),
            new SetDefaultHostResponseHeaderXPoweredBy<>());

    public static final UpdateSubsystemConfigurationTaskBuilder MESSAGING_ACTIVEMQ = new UpdateSubsystemConfigurationTaskBuilder(SubsystemNames.MESSAGING_ACTIVEMQ,
            new AddHttpAcceptorsAndConnectors<>());

}
