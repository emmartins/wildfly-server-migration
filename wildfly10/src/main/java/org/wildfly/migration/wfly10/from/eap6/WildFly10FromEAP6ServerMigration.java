package org.wildfly.migration.wfly10.from.eap6;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.eap.EAP6Server;
import org.wildfly.migration.wfly10.WildFly10Server;
import org.wildfly.migration.wfly10.WildFly10ServerMigration;
import org.wildfly.migration.wfly10.from.eap6.domain.WildFly10FromEAP6DomainMigration;
import org.wildfly.migration.wfly10.from.eap6.standalone.WildFly10FromEAP6StandaloneMigration;

import java.io.IOException;

/**
 * Created by emmartins on 08/12/15.
 */
public class WildFly10FromEAP6ServerMigration implements WildFly10ServerMigration {

    private final WildFly10FromEAP6StandaloneMigration standaloneMigration;
    private final WildFly10FromEAP6DomainMigration domainMigration;

    public WildFly10FromEAP6ServerMigration(WildFly10FromEAP6StandaloneMigration standaloneMigration, WildFly10FromEAP6DomainMigration domainMigration) {
        this.standaloneMigration = standaloneMigration;
        this.domainMigration = domainMigration;
    }

    @Override
    public void run(Server source, WildFly10Server target, ServerMigrationContext context) throws IOException {
        final EAP6Server eap6Server = (EAP6Server) source;
        standaloneMigration.run(eap6Server, target, context);
        domainMigration.run(eap6Server, target, context);
    }
}
