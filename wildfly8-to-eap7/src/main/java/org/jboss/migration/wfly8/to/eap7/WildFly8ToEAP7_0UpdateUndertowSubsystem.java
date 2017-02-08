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

package org.jboss.migration.wfly8.to.eap7;

import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurations;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHostResponseHeaderServer;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHostResponseHeaderXPoweredBy;
import org.jboss.migration.wfly10.config.task.subsystem.undertow.SetDefaultHttpListenerRedirectSocket;

/**
 * @author emmartins
 */
public class WildFly8ToEAP7_0UpdateUndertowSubsystem<S> extends UpdateSubsystemConfigurations<S> {
    public WildFly8ToEAP7_0UpdateUndertowSubsystem() {
        super(SubsystemNames.UNDERTOW,
                new SetDefaultHttpListenerRedirectSocket<>(),
                new SetDefaultHostResponseHeaderServer<>(),
                new SetDefaultHostResponseHeaderXPoweredBy<>());
    }
}
