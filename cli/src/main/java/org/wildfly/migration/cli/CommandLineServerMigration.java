/*
 * Copyright 2015 Red Hat, Inc.
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
package org.wildfly.migration.cli;

import org.wildfly.migration.core.ServerMigration;
import org.wildfly.security.manager.WildFlySecurityManager;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * The command line tool to migrate a WildFly server.
 *
 * @author Eduardo Martins
 */
public class CommandLineServerMigration {
    // Capture System.out and System.err before they are redirected by STDIO
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    private static void usage() {
        CommandLineArgumentUsageImpl.printUsage(STDOUT);
    }

    private CommandLineServerMigration() {
    }

    /**
     * The main method.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length != 4) {
                usage();
                abort(null);
            }
            Path source = null;
            Path target = null;
            for(int i = 0; i < args.length; ++i) {
                String arg = args[i];
                switch (arg) {
                    case CommandLineConstants.SOURCE: {
                        ++i;
                        if(i == args.length || source != null) {
                            usage();
                            abort(null);
                        }
                        source = resolvePath(args[i]);
                        break;
                    }
                    case CommandLineConstants.TARGET: {
                        ++i;
                        if(i == args.length || target != null) {
                            usage();
                            abort(null);
                        }
                        target = resolvePath(args[i]);
                        break;
                    }
                }
            }
            WildFlySecurityManager.setPropertyPrivileged("java.util.logging.manager", "org.jboss.logmanager.LogManager");
            new ServerMigration()
                    .from(source)
                    .to(target)
                    .run();
        } catch (Throwable t) {
            abort(t);
        }
    }

    private static void abort(Throwable t) {
        try {
            if (t != null) {
                t.printStackTrace(STDERR);
            }
        } finally {
            System.exit(1);
        }
    }

    private static Path resolvePath(String s) {
        final FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(s).normalize();
        return path.isAbsolute() ? path : fileSystem.getPath(System.getProperty("user.dir")).resolve(path);
    }
}
