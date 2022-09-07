package org.jboss.migration.wfly.task.security;

import java.util.HashMap;
import java.util.Map;

public class LegacySecurityConfigurations {
    private final Map<String, LegacySecurityConfiguration> securityConfigurations = new HashMap<>();

    public Map<String, LegacySecurityConfiguration> getSecurityConfigurations() {
        return securityConfigurations;
    }
}
