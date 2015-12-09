package org.wildfly.migration.wildfly.full.from.eap6.standalone.config;

import org.wildfly.migration.wildfly.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileMigration;
import org.wildfly.migration.wildfly.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration;
import org.wildfly.migration.wildfly.from.eap6.standalone.config.WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration;
import org.wildfly.migration.wildfly.standalone.config.Extensions;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FullFromEAP6StandaloneConfigFileMigration extends WildFly10FromEAP6StandaloneConfigFileMigration {

    public WildFly10FullFromEAP6StandaloneConfigFileMigration() {
        super(new WildFly10FromEAP6StandaloneConfigFileSubsystemsMigration(Extensions.SUPPORTED), new WildFly10FromEAP6StandaloneConfigFileSecurityRealmsMigration());
    }
}
