/*
 * Copyright 2016 Red Hat, Inc.
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

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author emmartins
 */
public class TestServer extends AbstractServer {

    static Path getBaseDir(ProductInfo productInfo) {
        return Paths.get(productInfo.getName(), productInfo.getVersion());
    }

    /**
     * the product infos of source servers which migration from is supported
     */
    private final Set<ProductInfo> supportedMigrations;

    /**
     *
     * @param productInfo
     * @param supportedMigrations
     */
    public TestServer(ProductInfo productInfo, Set<ProductInfo> supportedMigrations) {
        super(productInfo.getName(), productInfo, getBaseDir(productInfo), null);
        this.supportedMigrations = supportedMigrations;
    }

    @Override
    public ServerMigrationTaskResult migrate(Server source, ServerMigrationTaskContext context) throws IllegalArgumentException {
        if (!supportedMigrations.contains(source.getProductInfo())) {
            return super.migrate(source, context);
        }

        MigrationEnvironment env = context.getServerMigrationContext().getMigrationEnvironment();

        env.getPropertyAsString("test.property.key");

        context.execute(new SubTask1());
        context.execute(new SubTask2());
        if (env.getPropertyAsBoolean("test.should.fail", Boolean.FALSE)) {
            context.execute(new SubTask3());
        }
        return ServerMigrationTaskResult.SUCCESS;
    }

    // ---

    private static final class SubTask1 implements ServerMigrationTask {
        @Override
        public ServerMigrationTaskName getName() {
            return new ServerMigrationTaskName.Builder()
                    .setName("subtask 1")
                    .build();
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            context.execute(new SubTask11());
            context.execute(new SubTask12());
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    private static final class SubTask11 implements ServerMigrationTask {
        @Override
        public ServerMigrationTaskName getName() {
            return new ServerMigrationTaskName.Builder()
                    .setName("subtask 1.1")
                    .addAttribute("config", "foobar")
                    .build();
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    private static final class SubTask12 implements ServerMigrationTask {
        @Override
        public ServerMigrationTaskName getName() {
            return new ServerMigrationTaskName.Builder()
                    .setName("subtask 1.2")
                    .addAttribute("config", "quux")
                    .build();
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            return ServerMigrationTaskResult.SUCCESS;
        }
    }

    private static final class SubTask2 implements ServerMigrationTask {
        @Override
        public ServerMigrationTaskName getName() {
            return new ServerMigrationTaskName.Builder()
                    .setName("subtask 2")
                    .addAttribute("source", "abcd xyzw")
                    .build();
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            return ServerMigrationTaskResult.SKIPPED;
        }
    }

    private static final class SubTask3 implements ServerMigrationTask {
        @Override
        public ServerMigrationTaskName getName() {
            return new ServerMigrationTaskName.Builder()
                    .setName("subtask 3")
                    .addAttribute("always", "fails")
                    .build();
        }

        @Override
        public ServerMigrationTaskResult run(ServerMigrationTaskContext context) throws Exception {
            throw new Exception("this task always fails");
        }
    }
}
