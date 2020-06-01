/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.core.ts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.hamcrest.CoreMatchers;
import org.jboss.migration.core.MigrationFiles;
import org.jboss.migration.core.ServerMigration;
import org.jboss.migration.core.ServerMigrationFailureException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Simple test class for MigrationFiles copy method.
 *
 * @author rmartinc
 */
public class MigrationFilesTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final Random random = new Random();

    private final MigrationFiles migrationFiles = new ServerMigration()
            .from(TestSourceServerProvider.SERVER.getBaseDir())
            .to(TestTargetServerProvider.SERVER.getBaseDir())
            .run()
            .getRootTask()
            .getServerMigrationContext()
            .getMigrationFiles();

    private Path createNewFile(String name, int size) throws IOException {
        Path file = tmp.newFile(name).toPath();
        return createNewFile(file, size);
    }

    private Path createNewFile(Path file, int size) throws IOException {
        try (OutputStream os = Files.newOutputStream(file)) {
            byte[] bytes = new byte[size];
            random.nextBytes(bytes);
            os.write(bytes);
        }
        return file;
    }

    @Test
    public void copyFile() throws IOException {
        Path source = createNewFile("source.bin", 32);
        Path target = tmp.getRoot().toPath().resolve("target.bin");

        migrationFiles.copy(source, target);

        Assert.assertTrue("Target file is created", Files.exists(target));
        Assert.assertArrayEquals("Contents of the files are OK", Files.readAllBytes(source), Files.readAllBytes(target));
    }

    @Test
    public void copyFileSeveralLevels() throws IOException {
        Path source = createNewFile("source.bin", 32);
        Path target = tmp.getRoot().toPath().resolve("level1").resolve("level2").resolve("target.bin");

        migrationFiles.copy(source, target);

        Assert.assertTrue("Target file is created", Files.exists(target));
        Assert.assertArrayEquals("Contents of the files are OK", Files.readAllBytes(source), Files.readAllBytes(target));
    }

    @Test
    public void copyFileBackup() throws IOException {
        Path source = createNewFile("source.bin", 32);
        Path target = createNewFile("target.bin", 32);

        migrationFiles.copy(source, target);

        Assert.assertTrue("Target file is created", Files.exists(target));
        Assert.assertArrayEquals("Contents of the files are OK", Files.readAllBytes(source), Files.readAllBytes(target));
        Assert.assertTrue("Backup file is created", Files.exists(tmp.getRoot().toPath().resolve("target.bin.beforeMigration")));
    }

    @Test
    public void copyFileWithALink() throws IOException {
        Path source = createNewFile("source.bin", 32);
        Path realTargetDir = tmp.newFolder("realLevel1").toPath();
        Path linkTargetDir = tmp.getRoot().toPath().resolve("linkLevel1");
        Files.createSymbolicLink(linkTargetDir, realTargetDir);
        Path target = linkTargetDir.resolve("target.bin");

        migrationFiles.copy(source, target);

        Assert.assertTrue("Target file is created", Files.exists(target));
        Assert.assertArrayEquals("Contents of the files are OK", Files.readAllBytes(source), Files.readAllBytes(target));
    }

    @Test
    public void copyFileWithALinkSeveralLevels() throws IOException {
        Path source = createNewFile("source.bin", 32);
        Path realTargetDir = tmp.newFolder("realLevel1").toPath();
        Path linkTargetDir = tmp.getRoot().toPath().resolve("linkLevel1");
        Files.createSymbolicLink(linkTargetDir, realTargetDir);
        Path target = linkTargetDir.resolve("level2").resolve("level3").resolve("target.bin");

        migrationFiles.copy(source, target);

        Assert.assertTrue("Target file is created", Files.exists(target));
        Assert.assertArrayEquals("Contents of the files are OK", Files.readAllBytes(source), Files.readAllBytes(target));
    }

    @Test
    public void copyDirectory() throws IOException {
        Path sourceDir = tmp.newFolder("source").toPath();
        Path source1 = createNewFile(sourceDir.resolve("source1.bin"), 32);
        Path source2 = createNewFile(sourceDir.resolve("source2.bin"), 32);
        Path levels = sourceDir.resolve("level1").resolve("level2");
        Files.createDirectories(levels);
        Path source3 = createNewFile(levels.resolve("source3.bin"), 32);
        Path targetDir = tmp.newFolder("target").toPath();

        migrationFiles.copy(sourceDir, targetDir);

        Assert.assertTrue("Target source1.bin file is created", Files.exists(targetDir.resolve("source1.bin")));
        Assert.assertArrayEquals("Contents of source1.bin are OK", Files.readAllBytes(source1), Files.readAllBytes(targetDir.resolve("source1.bin")));
        Assert.assertTrue("Target source2.bin file is created", Files.exists(targetDir.resolve("source2.bin")));
        Assert.assertArrayEquals("Contents of source2.bin are OK", Files.readAllBytes(source2), Files.readAllBytes(targetDir.resolve("source2.bin")));
        Assert.assertTrue("Target source3.bin file is created", Files.exists(targetDir.resolve("level1").resolve("level2").resolve("source3.bin")));
        Assert.assertArrayEquals("Contents of source3.bin are OK", Files.readAllBytes(source3),
                Files.readAllBytes(targetDir.resolve("level1").resolve("level2").resolve("source3.bin")));
    }

    @Test
    public void copyDirectoryAlreadyExists() throws IOException {
        Path sourceDir = tmp.newFolder("source").toPath();
        createNewFile(sourceDir.resolve("source1.bin"), 32);
        Path target = tmp.newFile("target").toPath();

        exception.expect(ServerMigrationFailureException.class);
        exception.expectCause(CoreMatchers.isA(FileAlreadyExistsException.class));

        migrationFiles.copy(sourceDir, target);
    }
}
