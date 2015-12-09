package org.wildfly.migration.eap;

import java.nio.file.Path;

/**
 * Created by emmartins on 08/12/15.
 */
public class EAP6StandaloneConfig {

    private final Path path;
    private final EAP6Server server;

    protected EAP6StandaloneConfig(Path path, EAP6Server server) {
        this.path = path;
        this.server = server;
    }

    public Path getPath() {
        return path;
    }

    public EAP6Server getServer() {
        return server;
    }
}
