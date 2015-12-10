package org.wildfly.migration.wfly10.full.from.eap6.standalone;

import org.wildfly.migration.wfly10.from.eap6.standalone.WildFly10FromEAP6StandaloneMigration;
import org.wildfly.migration.wfly10.full.from.eap6.standalone.config.WildFly10FullFromEAP6StandaloneConfigFileMigration;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FullFromEAP6StandaloneMigration extends WildFly10FromEAP6StandaloneMigration {

    public WildFly10FullFromEAP6StandaloneMigration() {
        super(new WildFly10FullFromEAP6StandaloneConfigFileMigration());
    }
}
