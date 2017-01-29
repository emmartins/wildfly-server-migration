/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.core.task.component2.test;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component2.TaskBuilder;
import org.jboss.migration.core.task.component2.TaskBuilder.AfterRun;
import org.jboss.migration.core.task.component2.TaskBuilder.BeforeRun;
import org.jboss.migration.core.task.component2.CompositeTask;
import org.jboss.migration.core.task.component2.LeafTask;
import org.jboss.migration.core.task.component2.TaskBuilder.NameFactory;
import org.jboss.migration.core.task.component2.TaskBuilder.Params;
import org.jboss.migration.core.task.component2.TaskRunnable;
import org.jboss.migration.core.task.component2.TaskBuilder.RunnableFactory;
import org.jboss.migration.core.task.component2.TaskBuilder.SkipPolicy;

/**
 * @author emmartins
 */
public class TestAPI {

    static void test() throws Exception {

        // non components

        final XTaskParams pX = () -> 1;
        final YTaskParams pY = () -> 2;
        final ZTaskParams pZ = () -> 3;
        final XYTaskParams pXY = new XYTaskParams() {
            @Override
            public int getX() {
                return pX.getX();
            }
            @Override
            public int getY() {
                return pY.getY();
            }
        };
        final XYZTaskParams pXYZ = new XYZTaskParams() {
            @Override
            public int getX() {
                return pXY.getX();
            }

            @Override
            public int getY() {
                return pXY.getY();
            }

            @Override
            public int getZ() {
                return pZ.getZ();
            }
        };

        final ServerMigrationTaskName n = new ServerMigrationTaskName.Builder(" ").build();
        final ServerMigrationTaskName nX = new ServerMigrationTaskName.Builder("X").build();
        final ServerMigrationTaskName nY = new ServerMigrationTaskName.Builder("Y").build();
        final ServerMigrationTaskName nZ = new ServerMigrationTaskName.Builder("Z").build();
        final ServerMigrationTaskName nXY = new ServerMigrationTaskName.Builder("XY").build();
        final ServerMigrationTaskName nXYZ = new ServerMigrationTaskName.Builder("XYZ").build();

        final TaskRunnable r = (taskName, context) -> ServerMigrationTaskResult.SUCCESS;
        final ServerMigrationTask t = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return n;
            }

            @Override
            public ServerMigrationTaskResult run(TaskContext context) throws Exception {
                return r.run(n, context);
            }
        };

        // components

        final TaskBuilder.ParamsConverter<XTaskParams, XTaskParams> pcXX = xTaskParams -> null;
        final TaskBuilder.ParamsConverter<XTaskParams, YTaskParams> pcXY = xTaskParams -> null;
        final TaskBuilder.ParamsConverter<XTaskParams, ZTaskParams> pcXZ = xTaskParams -> null;
        final TaskBuilder.ParamsConverter<YTaskParams, XTaskParams> pcYX = yTaskParams -> null;
        final TaskBuilder.ParamsConverter<YTaskParams, YTaskParams> pcYY = yTaskParams -> null;
        final TaskBuilder.ParamsConverter<YTaskParams, ZTaskParams> pcYZ = yTaskParams -> null;
        final TaskBuilder.ParamsConverter<ZTaskParams, XTaskParams> pcZX = zTaskParams -> null;
        final TaskBuilder.ParamsConverter<ZTaskParams, YTaskParams> pcZY = zTaskParams -> null;
        final TaskBuilder.ParamsConverter<ZTaskParams, ZTaskParams> pcZZ = zTaskParams -> null;

        final NameFactory<Params> nf = parameters -> n;
        final NameFactory<XTaskParams> nfX = parameters -> nX;
        final NameFactory<YTaskParams> nfY = parameters -> nY;
        final NameFactory<ZTaskParams> nfZ = parameters -> nZ;
        final NameFactory<XYTaskParams> nfXY = parameters -> nXY;
        final NameFactory<XYTaskParams> nfXYZ = parameters -> nXYZ;

        final SkipPolicy<Params> sp = (parameters, taskName, context) -> true;
        final SkipPolicy<XTaskParams> spX = (parameters, taskName, context) -> false;
        final SkipPolicy<YTaskParams> spY = (parameters, taskName, context) -> false;
        final SkipPolicy<ZTaskParams> spZ = (parameters, taskName, context) -> false;
        final SkipPolicy<XYTaskParams> spXY = (parameters, taskName, context) -> true;
        final SkipPolicy<XYZTaskParams> spXYZ = (parameters, taskName, context) -> true;

        final BeforeRun<Params> br = (parameters, taskName, context) -> {};
        final BeforeRun<XTaskParams> brX = (parameters, taskName, context) -> {};
        final BeforeRun<YTaskParams> brY = (parameters, taskName, context) -> {};
        final BeforeRun<ZTaskParams> brZ = (parameters, taskName, context) -> {};
        final BeforeRun<XYTaskParams> brXY = (parameters, taskName, context) -> {};
        final BeforeRun<XYZTaskParams> brXYZ = (parameters, taskName, context) -> {};

        final RunnableFactory<Params> rf = parameters -> r;
        final RunnableFactory<XTaskParams> rfX = parameters -> r;
        final RunnableFactory<YTaskParams> rfY = parameters -> r;
        final RunnableFactory<ZTaskParams> rfZ = parameters -> r;
        final RunnableFactory<XYTaskParams> rfXY = parameters -> r;
        final RunnableFactory<XYZTaskParams> rfXYZ = parameters -> r;

        final AfterRun<Params> ar = (parameters, taskName, context) -> {};
        final AfterRun<XTaskParams> arX = (parameters, taskName, context) -> {};
        final AfterRun<YTaskParams> arY = (parameters, taskName, context) -> {};
        final AfterRun<ZTaskParams> arZ = (parameters, taskName, context) -> {};
        final AfterRun<XYTaskParams> arXY = (parameters, taskName, context) -> {};
        final AfterRun<XYZTaskParams> arXYZ = (parameters, taskName, context) -> {};

        // builders

        final LeafTask.Builder<Params> lb = new LeafTask.Builder<>();
        final LeafTask.Builder<XTaskParams> lbX = new LeafTask.Builder<>();
        final LeafTask.Builder<YTaskParams> lbY = new LeafTask.Builder<>();
        final LeafTask.Builder<ZTaskParams> lbZ = new LeafTask.Builder<>();
        final LeafTask.Builder<XYTaskParams> lbXY = new LeafTask.Builder<>();
        final LeafTask.Builder<XYZTaskParams> lbXYZ = new LeafTask.Builder<>();

        final CompositeTask.Builder<Params> cb = new CompositeTask.Builder<>();
        final CompositeTask.Builder<XTaskParams> cbX = new CompositeTask.Builder<>();
        final CompositeTask.Builder<YTaskParams> cbY = new CompositeTask.Builder<>();
        final CompositeTask.Builder<ZTaskParams> cbZ = new CompositeTask.Builder<>();
        final CompositeTask.Builder<XYTaskParams> cbXY = new CompositeTask.Builder<>();
        final CompositeTask.Builder<XYZTaskParams> cbXYZ = new CompositeTask.Builder<>();


        // builder ops

        lb
                .name(n)
                .name(nf)
                .name(n)
                .name(nf)
                .skipPolicy(sp)
                .beforeRun(br)
                .run(r)
                .run(rf)
                .run(r)
                .run(rf)
                .afterRun(ar);

        lb
                .name(nf)
                .name(n)
                .name(nf)
                .name(n)
                .skipPolicy(sp)
                .beforeRun(br)
                .run(rf)
                .run(r)
                .run(rf)
                .run(r)
                .afterRun(ar);

        lbX
                .name(n)
                .name(nf)
                .name(nfX)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .beforeRun(br)
                .beforeRun(brX)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(pcXX, rfX)
                .run(pcXY, rfY)
                .run(pcXZ, rfZ)
                .afterRun(ar)
                .afterRun(arX);

        lbY
                .name(nfY)
                .name(nf)
                .name(n)
                .skipPolicy(spY)
                .skipPolicy(sp)
                .beforeRun(brY)
                .beforeRun(br)
                .run(pcYX, rfX)
                .run(pcYY, rfY)
                .run(pcYZ, rfZ)
                .run(rfY)
                .run(rf)
                .run(r)
                .afterRun(arY)
                .afterRun(ar);

        lbZ
                .name(nfZ)
                .skipPolicy(spZ)
                .beforeRun(brZ)
                .run(pcZX, rfX)
                .run(pcZY, rfY)
                .run(pcZZ, rfZ)
                .run(rfZ)
                .afterRun(arZ);

        lbXY
                .name(n)
                .name(nf)
                .name(nfX)
                .name(nfY)
                .name(nfXY)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .skipPolicy(spY)
                .skipPolicy(spXY)
                .beforeRun(br)
                .beforeRun(brX)
                .beforeRun(brY)
                .beforeRun(brXY)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(rfY)
                .run(rfXY)
                .afterRun(ar)
                .afterRun(arX)
                .afterRun(arY)
                .afterRun(arXY);

        lbXYZ
                .name(n)
                .name(nf)
                .name(nfX)
                .name(nfY)
                .name(nfZ)
                .name(nfXY)
                .name(nfXYZ)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .skipPolicy(spY)
                .skipPolicy(spZ)
                .skipPolicy(spXY)
                .skipPolicy(spXYZ)
                .beforeRun(br)
                .beforeRun(brX)
                .beforeRun(brY)
                .beforeRun(brZ)
                .beforeRun(brXY)
                .beforeRun(brXYZ)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(rfY)
                .run(rfZ)
                .run(rfXY)
                .run(rfXYZ)
                .afterRun(ar)
                .afterRun(arX)
                .afterRun(arY)
                .afterRun(arZ)
                .afterRun(arXY)
                .afterRun(arXYZ);

        cb
                .name(n)
                .name(nf)
                .name(n)
                .name(nf)
                .skipPolicy(sp)
                .beforeRun(br)
                .run(r)
                .run(rf)
                .run(t)
                .run(lb)
                .run(cb.clone())
                .run(lb)
                .run(t)
                .run(rf)
                .run(r)
                .afterRun(ar);

        cbX
                .name(n)
                .name(nf)
                .name(nfX)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .beforeRun(br)
                .beforeRun(brX)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(t)
                .run(lb)
                .run(lbX)
                .run(cb)
                .run(cbX.clone())
                .run(pcXX, rfX)
                .run(pcXY, rfY)
                .run(pcXZ, rfZ)
                .run(pcXX, lbX)
                .run(pcXY, lbY)
                .run(pcXZ, lbZ)
                .afterRun(ar)
                .afterRun(arX);

        cbY
                .name(nfY)
                .name(nf)
                .name(n)
                .skipPolicy(spY)
                .skipPolicy(sp)
                .beforeRun(brY)
                .beforeRun(br)
                .run(pcYX, lbX)
                .run(pcYY, lbY)
                .run(pcYZ, lbZ)
                .run(pcYX, rfX)
                .run(pcYY, rfY)
                .run(pcYZ, rfZ)
                .run(cbY.clone())
                .run(cb)
                .run(lbY)
                .run(lb)
                .run(t)
                .run(rfY)
                .run(rf)
                .run(r)
                .afterRun(arY)
                .afterRun(ar);

        cbZ
                .name(nfZ)
                .skipPolicy(spZ)
                .beforeRun(brZ)
                .run(t)
                .run(pcZX, lbX)
                .run(pcZY, lbY)
                .run(pcZZ, lbZ)
                .run(pcZX, rfX)
                .run(pcZY, rfY)
                .run(pcZZ, rfZ)
                .run(lbZ)
                .run(cb)
                .afterRun(arZ);

        cbXY
                .name(n)
                .name(nf)
                .name(nfX)
                .name(nfY)
                .name(nfXY)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .skipPolicy(spY)
                .skipPolicy(spXY)
                .beforeRun(br)
                .beforeRun(brX)
                .beforeRun(brY)
                .beforeRun(brXY)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(rfY)
                .run(rfXY)
                .run(t)
                .run(lb)
                .run(lbX)
                .run(lbY)
                .run(lbXY)
                .run(cb)
                .run(cbX)
                .run(cbY)
                .run(cbXY.clone())
                .afterRun(ar)
                .afterRun(arX)
                .afterRun(arY)
                .afterRun(arXY);

        cbXYZ
                .name(n)
                .name(nf)
                .name(nfX)
                .name(nfY)
                .name(nfZ)
                .name(nfXY)
                .name(nfXYZ)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .skipPolicy(spY)
                .skipPolicy(spZ)
                .skipPolicy(spXY)
                .skipPolicy(spXYZ)
                .beforeRun(br)
                .beforeRun(brX)
                .beforeRun(brY)
                .beforeRun(brZ)
                .beforeRun(brXY)
                .beforeRun(brXYZ)
                .run(r)
                .run(rf)
                .run(rfX)
                .run(rfY)
                .run(rfZ)
                .run(rfXY)
                .run(rfXYZ)
                .run(t)
                .run(lb)
                .run(lbX)
                .run(lbY)
                .run(lbZ)
                .run(lbXY)
                .run(lbXYZ)
                .run(cb)
                .run(cbX)
                .run(cbY)
                .run(cbZ)
                .run(cbXY)
                .run(cbXYZ.clone())
                .afterRun(ar)
                .afterRun(arX)
                .afterRun(arY)
                .afterRun(arZ)
                .afterRun(arXY)
                .afterRun(arXYZ);

        // tasks
        lb.build(Params.NONE);
        lb.build(pX);
        lb.build(pY);
        lb.build(pZ);
        lb.build(pXY);
        lb.build(pXYZ);

        lbX.build(pX);
        lbX.build(pXY);
        lbX.build(pXYZ);

        lbY.build(pY);
        lbY.build(pXY);
        lbY.build(pXYZ);

        lbZ.build(pZ);
        lbZ.build(pXYZ);

        lbXY.build(pXY);
        lbXY.build(pXYZ);

        lbXYZ.build(pXYZ);

        cb.build(Params.NONE);
        cb.build(pX);
        cb.build(pY);
        cb.build(pZ);
        cb.build(pXY);
        cb.build(pXYZ);

        cbX.build(pX);
        cbX.build(pXY);
        cbX.build(pXYZ);

        cbY.build(pY);
        cbY.build(pXY);
        cbY.build(pXYZ);

        cbZ.build(pZ);
        cbZ.build(pXYZ);

        cbXY.build(pXY);
        cbXY.build(pXYZ);

        cbXYZ.build(pXYZ);
    }
}
