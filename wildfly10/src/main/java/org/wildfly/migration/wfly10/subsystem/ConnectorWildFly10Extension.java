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
public class ConnectorWildFly10Extension extends WildFly10Extension {

    public static final ConnectorWildFly10Extension INSTANCE = new ConnectorWildFly10Extension();

    private final WildFly10Subsystem datasourceSubsystem;
    private final WildFly10Subsystem jcaSubsystem;
    private final WildFly10Subsystem resourceAdaptersSubsystem;

    private ConnectorWildFly10Extension() {
        super("org.jboss.as.connector");
        datasourceSubsystem = new BasicWildFly10Subsystem("datasources", this);
        subsystems.add(datasourceSubsystem);
        jcaSubsystem = new BasicWildFly10Subsystem("jca", this);
        subsystems.add(jcaSubsystem);
        resourceAdaptersSubsystem = new BasicWildFly10Subsystem("resource-adapters", this);
        subsystems.add(resourceAdaptersSubsystem);
    }

    public WildFly10Subsystem getDatasourceSubsystem() {
        return datasourceSubsystem;
    }

    public WildFly10Subsystem getJcaSubsystem() {
        return jcaSubsystem;
    }

    public WildFly10Subsystem getResourceAdaptersSubsystem() {
        return resourceAdaptersSubsystem;
    }
}