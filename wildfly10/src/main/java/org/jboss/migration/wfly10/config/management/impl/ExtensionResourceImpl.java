package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.ExtensionResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class ExtensionResourceImpl extends ManageableResourceImpl implements ExtensionResource {
    protected ExtensionResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, serverConfiguration);
    }
}
