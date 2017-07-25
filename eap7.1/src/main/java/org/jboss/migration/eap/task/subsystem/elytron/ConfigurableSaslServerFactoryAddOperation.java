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

package org.jboss.migration.eap.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

import java.util.Properties;

/**
 *
 * @author emmartins
 */
public class ConfigurableSaslServerFactoryAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String configurableSaslServerFactory;
    private String saslServerFactory;
    private Properties properties;

    public ConfigurableSaslServerFactoryAddOperation(PathAddress subsystemPathAddress, String configurableSaslServerFactory) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.configurableSaslServerFactory = configurableSaslServerFactory;
        this.properties = new Properties();
    }

    public ConfigurableSaslServerFactoryAddOperation saslServerFactory(String saslServerFactory) {
        this.saslServerFactory = saslServerFactory;
        return this;
    }

    public ConfigurableSaslServerFactoryAddOperation addProperty(String propertyName, String propertyValue) {
        this.properties.setProperty(propertyName, propertyValue);
        return this;
    }

    public ModelNode toModelNode() {
            /*
               "sasl-server-factory" => "elytron",
            "properties" => {"wildfly.sasl.local-user.default-user" => "$local"},
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("configurable-sasl-server-factory" => "configured")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("configurable-sasl-server-factory", configurableSaslServerFactory);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (saslServerFactory != null) {
            operation.get("sasl-server-factory").set(saslServerFactory);
        }
        if (properties != null && !properties.isEmpty()) {
            final ModelNode propertiesNode = operation.get("properties");
            for (String propertyName : properties.stringPropertyNames()) {
                propertiesNode.get(propertyName).set(properties.getProperty(propertyName));
            }
        }
        return operation;
    }
}
