package org.jboss.migration.core.util;

import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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
                throw ServerMigrationLogger.ROOT_LOGGER.targetPreviouslyCopiedFromDifferentSource(target);
            } else {
                // no need to re-copy same file
                ServerMigrationLogger.ROOT_LOGGER.debugf("Skipping previously copied file %s", source);
                return;
            }
        }
        // check source file exists
        if (!Files.exists(source)) {
            throw ServerMigrationLogger.ROOT_LOGGER.sourceFileDoesNotExists(source);
        }
        // if target file exists make a backup copy
        if (Files.exists(target)) {
            ServerMigrationLogger.ROOT_LOGGER.targetFileRenamed(target, target.getFileName().toString());
            Files.copy(target, target.getParent().resolve(target.getFileName().toString()+".beforeMigration"), StandardCopyOption.REPLACE_EXISTING);
        }
        // copy file
        ServerMigrationLogger.ROOT_LOGGER.tracef("Copying file %s", target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        // keep track of the file copy to prevent more copies for same target
        copiedFiles.put(target, source);
        ServerMigrationLogger.ROOT_LOGGER.fileCopied(source, target);
    }
}
