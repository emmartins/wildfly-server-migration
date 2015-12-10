package org.wildfly.migration.wfly10;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;

import java.io.IOException;

/**
 * Created by emmartins on 08/12/15.
 */
public interface WildFly10ServerMigration {
    void run(Server source, WildFly10Server target, ServerMigrationContext context) throws IOException;
}
