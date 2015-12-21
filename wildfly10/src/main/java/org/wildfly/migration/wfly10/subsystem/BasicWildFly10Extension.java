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
public class BasicWildFly10Extension extends WildFly10Extension {

    public BasicWildFly10Extension(String extensionName) {
        super(extensionName);
    }

    private BasicWildFly10Extension addSubsystem(WildFly10Subsystem subsystem) {
        subsystems.add(subsystem);
        return this;
    }

    public BasicWildFly10Extension addBasicSubsystem(String subsystemName) {
        return addSubsystem(new BasicWildFly10Subsystem(subsystemName, this));
    }

    public BasicWildFly10Extension addLegacySubsystem(String subsystemName) {
        return addSubsystem(new LegacyWildFly10Subsystem(subsystemName, this));
    }
}
