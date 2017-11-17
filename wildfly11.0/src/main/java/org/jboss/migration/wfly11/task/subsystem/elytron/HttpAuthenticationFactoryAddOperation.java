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
public class HttpAuthenticationFactoryAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String httpAuthenticationFactory;
    private String securityDomain;
    private String httpServerMechanismFactory;
    private List<MechanismConfiguration> mechanismConfigurations;

    public HttpAuthenticationFactoryAddOperation(PathAddress subsystemPathAddress, String httpAuthenticationFactory) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.httpAuthenticationFactory = httpAuthenticationFactory;
        this.mechanismConfigurations = new ArrayList<>();
    }

    public HttpAuthenticationFactoryAddOperation securityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
        return this;
    }

    public HttpAuthenticationFactoryAddOperation httpServerMechanismFactory(String httpServerMechanismFactory) {
        this.httpServerMechanismFactory = httpServerMechanismFactory;
        return this;
    }

    public HttpAuthenticationFactoryAddOperation addMechanismConfiguration(MechanismConfiguration mechanismConfiguration) {
        this.mechanismConfigurations.add(mechanismConfiguration);
        return this;
    }

    public ModelNode toModelNode() {
        /*
              "security-domain" => "ManagementDomain",
            "http-server-mechanism-factory" => "global",
            "mechanism-configurations" => [{
                "mechanism-name" => "DIGEST",
                "mechanism-realm-configurations" => [{"realm-name" => "ManagementRealm"}]
            }],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("http-authentication-factory" => "management-http-authentication")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("http-authentication-factory", httpAuthenticationFactory);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (securityDomain != null) {
            operation.get("security-domain").set(securityDomain);
        }
        if (httpServerMechanismFactory != null) {
            operation.get("http-server-mechanism-factory").set(httpServerMechanismFactory);
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
