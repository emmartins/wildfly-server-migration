package org.jboss.migration.wfly.task.security;

import org.jboss.migration.core.jboss.JBossServer;
import org.jboss.migration.core.jboss.JBossServerConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LegacySecurityConfigurations<S extends JBossServer<S>> {
    final Map<JBossServerConfiguration<S>, LegacySecurityConfiguration> securityConfigurations = new HashMap<>();

}
