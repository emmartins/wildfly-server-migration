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

import org.jboss.migration.core.MigrationData;
import org.jboss.migration.core.ServerMigration;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author emmartins
 */
public class BasicMigrationTestCase {

    @Test
    public void testSupportedMigration() {
        final MigrationData migrationData = new ServerMigration()
                .from(TestSourceServerProvider.SERVER.getBaseDir())
                .to(TestTargetServerProvider.SERVER.getBaseDir())
                .run();
        Assert.assertTrue(migrationData.getRootTask().getResult().getStatus() == ServerMigrationTaskResult.Status.SUCCESS);
    }

    @Test
    public void testUnsupportedMigration() {
        final MigrationData migrationData = new ServerMigration()
                .to(TestSourceServerProvider.SERVER.getBaseDir())
                .from(TestTargetServerProvider.SERVER.getBaseDir())
                .run();
        Assert.assertNotNull(migrationData.getRootTask().getResult().getFailReason());
    }
}
