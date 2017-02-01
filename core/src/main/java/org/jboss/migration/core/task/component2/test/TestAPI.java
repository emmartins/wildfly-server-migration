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
import org.jboss.migration.core.task.component2.AfterTaskRun;
import org.jboss.migration.core.task.component2.BeforeTaskRun;
import org.jboss.migration.core.task.component2.BuildParameters;
import org.jboss.migration.core.task.component2.CompositeTask;
import org.jboss.migration.core.task.component2.CompositeTask.Subtasks;
import org.jboss.migration.core.task.component2.LeafTask;
import org.jboss.migration.core.task.component2.TaskNameBuilder;
import org.jboss.migration.core.task.component2.TaskRunnable;
import org.jboss.migration.core.task.component2.TaskSkipPolicy;

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

        final TaskRunnable r = context -> ServerMigrationTaskResult.SUCCESS;
        final ServerMigrationTask t = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return n;
            }

            @Override
            public ServerMigrationTaskResult run(TaskContext context) {
                return r.run(context);
            }
        };

        // components

        final BuildParameters.Mapper<XTaskParams, XTaskParams> pcXX = xTaskParams -> null;
        final BuildParameters.Mapper<XTaskParams, YTaskParams> pcXY = xTaskParams -> null;
        final BuildParameters.Mapper<XTaskParams, ZTaskParams> pcXZ = xTaskParams -> null;
        final BuildParameters.Mapper<YTaskParams, XTaskParams> pcYX = yTaskParams -> null;
        final BuildParameters.Mapper<YTaskParams, YTaskParams> pcYY = yTaskParams -> null;
        final BuildParameters.Mapper<YTaskParams, ZTaskParams> pcYZ = yTaskParams -> null;
        final BuildParameters.Mapper<ZTaskParams, XTaskParams> pcZX = zTaskParams -> null;
        final BuildParameters.Mapper<ZTaskParams, YTaskParams> pcZY = zTaskParams -> null;
        final BuildParameters.Mapper<ZTaskParams, ZTaskParams> pcZZ = zTaskParams -> null;

        final TaskNameBuilder<BuildParameters> nf = parameters -> n;
        final TaskNameBuilder<XTaskParams> nfX = parameters -> nX;
        final TaskNameBuilder<YTaskParams> nfY = parameters -> nY;
        final TaskNameBuilder<ZTaskParams> nfZ = parameters -> nZ;
        final TaskNameBuilder<XYTaskParams> nfXY = parameters -> nXY;
        final TaskNameBuilder<XYTaskParams> nfXYZ = parameters -> nXYZ;

        final TaskSkipPolicy.Builder<BuildParameters> sp = (parameters, taskName) -> context -> true;
        final TaskSkipPolicy.Builder<XTaskParams> spX = (parameters, taskName) -> context -> false;
        final TaskSkipPolicy.Builder<YTaskParams> spY = (parameters, taskName) -> context -> false;
        final TaskSkipPolicy.Builder<ZTaskParams> spZ = (parameters, taskName) -> context -> false;
        final TaskSkipPolicy.Builder<XYTaskParams> spXY = (parameters, taskName) -> context -> true;
        final TaskSkipPolicy.Builder<XYZTaskParams> spXYZ = (parameters, taskName) -> context -> true;

        final BeforeTaskRun.Builder<BuildParameters> br = (parameters, taskName) -> context -> {};
        final BeforeTaskRun.Builder<XTaskParams> brX = (parameters, taskName) -> context -> {};
        final BeforeTaskRun.Builder<YTaskParams> brY = (parameters, taskName) -> context -> {};
        final BeforeTaskRun.Builder<ZTaskParams> brZ = (parameters, taskName) -> context -> {};
        final BeforeTaskRun.Builder<XYTaskParams> brXY = (parameters, taskName) -> context -> {};
        final BeforeTaskRun.Builder<XYZTaskParams> brXYZ = (parameters, taskName) -> context -> {};

        final TaskRunnable.Builder<BuildParameters> rf = (parameters, name) -> r;
        final TaskRunnable.Builder<XTaskParams> rfX = (parameters, name) -> r;
        final TaskRunnable.Builder<YTaskParams> rfY = (parameters, name) -> r;
        final TaskRunnable.Builder<ZTaskParams> rfZ = (parameters, name) -> r;
        final TaskRunnable.Builder<XYTaskParams> rfXY = (parameters, name) -> r;
        final TaskRunnable.Builder<XYZTaskParams> rfXYZ = (parameters, name) -> r;

        final AfterTaskRun.Builder<BuildParameters> ar = (parameters, taskName) -> context -> {};
        final AfterTaskRun.Builder<XTaskParams> arX = (parameters, taskName) -> context -> {};
        final AfterTaskRun.Builder<YTaskParams> arY = (parameters, taskName) -> context -> {};
        final AfterTaskRun.Builder<ZTaskParams> arZ = (parameters, taskName) -> context -> {};
        final AfterTaskRun.Builder<XYTaskParams> arXY = (parameters, taskName) -> context -> {};
        final AfterTaskRun.Builder<XYZTaskParams> arXYZ = (parameters, taskName) -> context -> {};

        // builders

        final LeafTask.Builder<BuildParameters> lb = new LeafTask.Builder<>();
        final LeafTask.Builder<XTaskParams> lbX = new LeafTask.Builder<>();
        final LeafTask.Builder<YTaskParams> lbY = new LeafTask.Builder<>();
        final LeafTask.Builder<ZTaskParams> lbZ = new LeafTask.Builder<>();
        final LeafTask.Builder<XYTaskParams> lbXY = new LeafTask.Builder<>();
        final LeafTask.Builder<XYZTaskParams> lbXYZ = new LeafTask.Builder<>();

        final CompositeTask.Builder<BuildParameters> cb = new CompositeTask.Builder<>();
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
                .subtasks(new Subtasks<>()
                        .run(t)
                        .run(lb)
                        .run(cb.clone())
                        .run(lb)
                        .run(t))
                .afterRun(ar);

        cbX
                .name(n)
                .name(nf)
                .name(nfX)
                .skipPolicy(sp)
                .skipPolicy(spX)
                .beforeRun(br)
                .beforeRun(brX)
                .subtasks(new Subtasks<XTaskParams>().run(t))
                .subtasks(new Subtasks<XTaskParams>().run(t).run(lb).run(lbX).run(cb).run(cbX.clone()).run(pcXX, lbX).run(pcXY, lbY).run(pcXZ, lbZ))
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
                .subtasks(new Subtasks<YTaskParams>()
                        .run(pcYX, lbX)
                        .run(pcYY, lbY)
                        .run(pcYZ, lbZ)
                        .run(cbY.clone())
                        .run(cb)
                        .run(lbY)
                        .run(lb)
                        .run(t))
                .afterRun(arY)
                .afterRun(ar);

        cbZ
                .name(nfZ)
                .skipPolicy(spZ)
                .beforeRun(brZ)
                .subtask(t)
                .subtask(pcZX, lbX)
                .subtask(pcZY, lbY)
                .subtask(pcZZ, lbZ)
                .subtask(lbZ)
                .subtask(cb)
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
                .subtask(t)
                .subtask(lb)
                .subtask(lbX)
                .subtask(lbY)
                .subtask(lbXY)
                .subtask(cb)
                .subtask(cbX)
                .subtask(cbY)
                .subtask(cbXY.clone())
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
                .subtask(t)
                .subtask(lb)
                .subtask(lbX)
                .subtask(lbY)
                .subtask(lbZ)
                .subtask(lbXY)
                .subtask(lbXYZ)
                .subtask(cb)
                .subtask(cbX)
                .subtask(cbY)
                .subtask(cbZ)
                .subtask(cbXY)
                .subtask(cbXYZ.clone())
                .afterRun(ar)
                .afterRun(arX)
                .afterRun(arY)
                .afterRun(arZ)
                .afterRun(arXY)
                .afterRun(arXYZ);

        // tasks
        lb.build(BuildParameters.NONE);
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

        cb.build(BuildParameters.NONE);
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
