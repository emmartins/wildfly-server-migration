package org.jboss.migration.wfly10.config.task.subsystem.infinispan;

/**
 * @author emmartins
 */
public class WildFly10_0FixHibernateCacheModuleName<S> extends FixHibernateCacheModuleName<S> {
    public WildFly10_0FixHibernateCacheModuleName() {
        super("org.hibernate.infinispan");
    }
}
