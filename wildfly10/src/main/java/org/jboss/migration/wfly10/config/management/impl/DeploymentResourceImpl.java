package org.jboss.migration.wfly10.config.management.impl;

import org.jboss.as.controller.PathAddress;
import org.jboss.migration.wfly10.config.management.DeploymentResource;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;

/**
 * @author emmartins
 */
public class DeploymentResourceImpl extends ManageableResourceImpl implements DeploymentResource {
    protected DeploymentResourceImpl(String resourceName, PathAddress pathAddress, ManageableServerConfiguration serverConfiguration) {
        super(resourceName, pathAddress, serverConfiguration);
    }
}
