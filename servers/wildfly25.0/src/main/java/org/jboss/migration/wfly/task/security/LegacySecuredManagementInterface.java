package org.jboss.migration.wfly.task.security;

import org.jboss.migration.core.jboss.JBossServer;

import java.util.Objects;

public class LegacySecuredManagementInterface<S extends JBossServer<S>> {

    private final String name;
    private final String securityRealm;

    public LegacySecuredManagementInterface(String name, String securityRealm) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(securityRealm);
        this.name = name;
        this.securityRealm = securityRealm;
    }

    public String getName() {
        return name;
    }

    public String getSecurityRealm() {
        return securityRealm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegacySecuredManagementInterface<?> that = (LegacySecuredManagementInterface<?>) o;
        return name.equals(that.name) && securityRealm.equals(that.securityRealm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, securityRealm);
    }
}
