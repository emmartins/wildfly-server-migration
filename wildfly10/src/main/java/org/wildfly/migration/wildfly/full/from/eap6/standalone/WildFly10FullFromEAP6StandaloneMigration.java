package org.wildfly.migration.wildfly.full.from.eap6.standalone;

import org.wildfly.migration.wildfly.from.eap6.standalone.WildFly10FromEAP6StandaloneMigration;
import org.wildfly.migration.wildfly.full.from.eap6.standalone.config.WildFly10FullFromEAP6StandaloneConfigFileMigration;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FullFromEAP6StandaloneMigration extends WildFly10FromEAP6StandaloneMigration {

    public WildFly10FullFromEAP6StandaloneMigration() {
        super(new WildFly10FullFromEAP6StandaloneConfigFileMigration());
    }
}
