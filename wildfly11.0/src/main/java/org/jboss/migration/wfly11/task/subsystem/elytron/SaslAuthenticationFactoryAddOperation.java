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

package org.jboss.migration.wfly11.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class SaslAuthenticationFactoryAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String saslAuthenticationFactory;
    private String securityDomain;
    private String saslServerFactory;
    private List<MechanismConfiguration> mechanismConfigurations;

    public SaslAuthenticationFactoryAddOperation(PathAddress subsystemPathAddress, String saslAuthenticationFactory) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.saslAuthenticationFactory = saslAuthenticationFactory;
        this.mechanismConfigurations = new ArrayList<>();
    }

    public SaslAuthenticationFactoryAddOperation securityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
        return this;
    }

    public SaslAuthenticationFactoryAddOperation saslServerFactory(String saslServerFactory) {
        this.saslServerFactory = saslServerFactory;
        return this;
    }

    public SaslAuthenticationFactoryAddOperation addMechanismConfiguration(MechanismConfiguration mechanismConfiguration) {
        this.mechanismConfigurations.add(mechanismConfiguration);
        return this;
    }

    public ModelNode toModelNode() {
        /*
              "security-domain" => "ManagementDomain",
            "sasl-server-factory" => "configured",
            "mechanism-configurations" => [
                {
                    "mechanism-name" => "JBOSS-LOCAL-USER",
                    "realm-mapper" => "local"
                },
                {
                    "mechanism-name" => "DIGEST-MD5",
                    "mechanism-realm-configurations" => [{"realm-name" => "ManagementRealm"}]
                }
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("sasl-authentication-factory" => "management-sasl-authentication")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("sasl-authentication-factory", saslAuthenticationFactory);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (securityDomain != null) {
            operation.get("security-domain").set(securityDomain);
        }
        if (saslServerFactory != null) {
            operation.get("sasl-server-factory").set(saslServerFactory);
        }
        if (mechanismConfigurations != null && !mechanismConfigurations.isEmpty()) {
            final ModelNode mechanismConfigurationsNode = operation.get("mechanism-configurations").setEmptyList();
            for (MechanismConfiguration mechanismConfiguration : mechanismConfigurations) {
                mechanismConfigurationsNode.add(mechanismConfiguration.toModelNode());
            }
        }
        return operation;
    }
}
