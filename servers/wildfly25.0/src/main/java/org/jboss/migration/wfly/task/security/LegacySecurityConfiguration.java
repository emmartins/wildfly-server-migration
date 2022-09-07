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
