package org.jboss.migration.wfly12.task.subsystem.infinispan;

import org.jboss.migration.wfly10.config.task.subsystem.infinispan.FixHibernateCacheModuleName;

/**
 * @author emmartins
 */
public class WildFly12_0FixHibernateCacheModuleName<S> extends FixHibernateCacheModuleName<S> {
    public WildFly12_0FixHibernateCacheModuleName() {
        super("org.infinispan.hibernate-cache");
    }
}
