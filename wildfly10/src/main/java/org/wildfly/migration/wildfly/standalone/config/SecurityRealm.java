package org.wildfly.migration.wildfly.standalone.config;

import org.jboss.dmr.ModelNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by emmartins on 06/12/15.
 */
public class SecurityRealm {
    private final ModelNode modelNode;
    private final List<Path> filesToCopy;

    public SecurityRealm(ModelNode modelNode) {
        this.modelNode = modelNode;
        this.filesToCopy = new ArrayList<>();
    }

    public List<Path> getFilesToCopy() {
        return Collections.unmodifiableList(filesToCopy);
    }
}
