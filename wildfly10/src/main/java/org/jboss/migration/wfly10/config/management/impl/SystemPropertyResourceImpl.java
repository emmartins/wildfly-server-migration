package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SystemPropertyResource;

/**
 * @author emmartins
 */
public class SystemPropertyResourceImpl extends ManageableResourceImpl implements SystemPropertyResource {
    protected SystemPropertyResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, serverConfiguration);
    }
}
