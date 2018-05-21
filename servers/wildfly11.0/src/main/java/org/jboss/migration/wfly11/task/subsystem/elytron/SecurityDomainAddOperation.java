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
public class SecurityDomainAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String securityDomain;
    private String defaultRealm;
    private String permissionMapper;
    private String securityEventListener;
    private List<Realm> realms;

    public SecurityDomainAddOperation(PathAddress subsystemPathAddress, String securityDomain) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.securityDomain = securityDomain;
        this.realms = new ArrayList<>();
    }

    public SecurityDomainAddOperation permissionMapper(String permissionMapper) {
        this.permissionMapper = permissionMapper;
        return this;
    }

    public SecurityDomainAddOperation defaultRealm(String defaultRealm) {
        this.defaultRealm = defaultRealm;
        return this;
    }

    public SecurityDomainAddOperation securityEventListener(String securityEventListener) {
        this.securityEventListener = securityEventListener;
        return this;
    }

    public SecurityDomainAddOperation addRealm(Realm realm) {
        this.realms.add(realm);
        return this;
    }

    public ModelNode toModelNode() {
        final PathAddress pathAddress = subsystemPathAddress.append("security-domain", securityDomain);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (permissionMapper != null) {
            operation.get("permission-mapper").set(permissionMapper);
        }
        if (defaultRealm != null) {
            operation.get("default-realm").set(defaultRealm);
        }
        if (securityEventListener != null) {
            operation.get("security-event-listener").set(securityEventListener);
        }
        if (realms != null && !realms.isEmpty()) {
            final ModelNode operationRealms = operation.get("realms").setEmptyList();
            for (Realm realm : realms) {
                operationRealms.add(realm.toModelNode());
            }
        }
        return operation;
    }

    public static class Realm {
        private final String realm;
        private String roleDecoder;
        private String roleMapper;

        public Realm(String realm) {
            this.realm = realm;
        }

        public Realm roleDecoder(String roleDecoder) {
            this.roleDecoder = roleDecoder;
            return this;
        }

        public Realm roleMapper(String roleMapper) {
            this.roleMapper = roleMapper;
            return this;
        }

        ModelNode toModelNode() {
            final ModelNode modelNode = new ModelNode();
            modelNode.get("realm").set(realm);
            if (roleDecoder != null) {
                modelNode.get("role-decoder").set(roleDecoder);
            }
            if (roleMapper != null) {
                modelNode.get("role-mapper").set(roleMapper);
            }
            return modelNode;
        }
    }
}
