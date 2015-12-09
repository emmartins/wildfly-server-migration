package org.wildfly.migration.wildfly;

import org.wildfly.migration.core.AbstractServer;
import org.wildfly.migration.core.ProductInfo;
import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by emmartins on 08/12/15.
 */
public abstract class WildFly10Server extends AbstractServer {

    public WildFly10Server(ProductInfo productInfo, Path baseDir) {
        super(productInfo, baseDir);
    }

    @Override
    public void migrate(Server source, ServerMigrationContext context) throws IOException {
        final WildFly10ServerMigration migration = getMigration(source);
        if (migration != null) {
            migration.run(source, this, context);
        } else {
            throw new IllegalArgumentException("Source server migration to WildFly 10 not supported: "+source.getProductInfo());
        }
    }

    protected abstract WildFly10ServerMigration getMigration(Server source);

    public Path getStandaloneDir() {
        return getBaseDir().resolve("standalone");
    }

    public Path getStandaloneConfigurationDir() {
        return getStandaloneDir().resolve("configuration");
    }

    public Path getModulesDir() {
        return getModulesDir(getBaseDir());
    }

    public Path getModulesSystemLayersBaseDir() {
        return getModulesSystemLayersBaseDir(getBaseDir());
    }

    public static Path getModulesDir(Path baseDir) {
        return baseDir.resolve("modules");
    }

    public static Path getModulesSystemLayersBaseDir(Path baseDir) {
        return getModulesDir(baseDir).resolve("system").resolve("layers").resolve("base");
    }
}
