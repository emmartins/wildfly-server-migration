package org.wildfly.migration.wfly10.full.from.eap6.standalone.config;

import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileManagementInterfacesMigration;
import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileMigration;
import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration;
import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileSocketBindingsMigration;
import org.wildfly.migration.wfly10.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration;
import org.wildfly.migration.wfly10.subsystem.WildFly10Extensions;

/**
 * @author emmartins
 */
public class WildFly10FullFromEAP6StandaloneConfigFileMigration extends WildFly10FromEAP6StandaloneConfigFileMigration {

    public WildFly10FullFromEAP6StandaloneConfigFileMigration() {
        super(new WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration(WildFly10Extensions.SUPPORTED),
                new WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration(),
                new WildFly10FromEAP6StandaloneConfigFileManagementInterfacesMigration(),
                new WildFly10FromEAP6StandaloneConfigFileSocketBindingsMigration());
    }
}
