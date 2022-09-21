/*
 * Copyright 2022 Red Hat, Inc.
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
package org.jboss.migration.wfly.task.security;

import org.jboss.migration.core.jboss.JBossServerConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LegacySecurityConfiguration {

    private final JBossServerConfiguration targetConfiguration;
    private final Map<String, LegacySecurityRealm> legacySecurityRealms = new HashMap<>();
    private final Set<LegacySecuredManagementInterface> securedManagementInterfaces = new HashSet<>();

    public LegacySecurityConfiguration(JBossServerConfiguration targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    public JBossServerConfiguration getTargetConfiguration() {
        return targetConfiguration;
    }

    public Map<String, LegacySecurityRealm> getLegacySecurityRealms() {
        return legacySecurityRealms;
    }

    public LegacySecurityRealm getLegacySecurityRealm(String name) {
        return legacySecurityRealms.get(name);
    }

    public Set<LegacySecuredManagementInterface> getSecuredManagementInterfaces() {
        return securedManagementInterfaces;
    }

    @Override
    public String toString() {
        return "LegacySecurityConfiguration{" +
                "targetConfiguration=" + targetConfiguration +
                ", legacySecurityRealms=" + legacySecurityRealms.values() +
                ", securedManagementInterfaces=" + securedManagementInterfaces +
                '}';
    }
}
