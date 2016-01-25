package org.wildfly.migration.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * The migration files.
 */
public class MigrationFiles {

    private final Map<Path, Path> copiedFiles;

    public MigrationFiles() {
        this.copiedFiles = new HashMap<>();
    }

    /**
     * Copy a file.
     * @param source the file's path
     * @param target the file copy's path
     * @throws IllegalArgumentException if the source's file does not exists
     * @throws IllegalStateException if the target's path was used in a previous file copy, with a different source
     * @throws IOException if the file copy failed
     */
    public synchronized void copy(Path source, Path target) throws IllegalArgumentException, IOException {
        // check if already copied
        final Path existentCopySource = copiedFiles.get(target);
        if (existentCopySource != null) {
            if (!existentCopySource.equals(source)) {
                throw ROOT_LOGGER.targetPreviouslyCopiedFromDifferentSource(target);
            } else {
                // no need to re-copy same file
                ROOT_LOGGER.debugf("Skipping previously copied file %s", source);
                return;
            }
        }
        // check source file exists
        if (!Files.exists(source)) {
            throw ROOT_LOGGER.sourceFileDoesNotExists(source);
        }
        // if target file exists make a backup copy
        if (Files.exists(target)) {
            ROOT_LOGGER.targetFileRenamed(target, target.getFileName().toString());
            Files.copy(target, target.getParent().resolve(target.getFileName().toString()+".beforeMigration"), StandardCopyOption.REPLACE_EXISTING);
        }
        // copy file
        ROOT_LOGGER.tracef("Copying file %s", target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        // keep track of the file copy to prevent more copies for same target
        copiedFiles.put(target, source);
        ROOT_LOGGER.fileCopied(source, target);
    }
}
