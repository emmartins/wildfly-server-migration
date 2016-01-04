/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.migration.wfly10.subsystem;

/**
 * @author emmartins
 */
public class MessagingActiveMQWildFly10Extension extends WildFly10Extension {

    public static final MessagingActiveMQWildFly10Extension INSTANCE = new MessagingActiveMQWildFly10Extension();

    private final WildFly10Subsystem messagingActiveMQSubsystem;

    private MessagingActiveMQWildFly10Extension() {
        super("org.wildfly.extension.messaging-activemq");
        messagingActiveMQSubsystem = new BasicWildFly10Subsystem("messaging-activemq", this);
        subsystems.add(messagingActiveMQSubsystem);
    }

    public WildFly10Subsystem getMessagingActiveMQSubsystem() {
        return messagingActiveMQSubsystem;
    }
}