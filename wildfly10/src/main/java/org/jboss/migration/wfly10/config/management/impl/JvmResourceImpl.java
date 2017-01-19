package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.JvmResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class JvmResourceImpl extends ManageableResourceImpl implements JvmResource {
    protected JvmResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, serverConfiguration);
    }
}
