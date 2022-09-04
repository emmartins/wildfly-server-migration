package org.jboss.migration.wfly.task.security;

import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LegacySecurityConfiguration<S extends JBossServer<S>> {
    JBossServerConfiguration<S> sourceConfiguration;
    JBossServerConfiguration targetConfiguration;
    final Map<String, LegacySecurityRealm> legacySecurityRealms = new HashMap<>();
    final Set<LegacySecuredManagementInterface> securedManagementInterfaces = new HashSet<>();

    @Override
    public String toString() {
        return "LegacySecurityConfiguration{" +
                "sourceConfiguration=" + sourceConfiguration +
                ", targetConfiguration=" + targetConfiguration +
                ", legacySecurityRealms=" + legacySecurityRealms +
                ", securedManagementInterfaces=" + securedManagementInterfaces +
                '}';
    }
}
