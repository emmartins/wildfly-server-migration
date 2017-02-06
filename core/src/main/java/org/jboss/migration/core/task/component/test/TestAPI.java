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

package org.jboss.migration.core.task.component.test;

import org.jboss.migration.core.task.ServerMigrationTask;
import org.jboss.migration.core.task.ServerMigrationTaskName;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.task.component.AfterTaskRun;
import org.jboss.migration.core.task.component.BeforeTaskRun;
import org.jboss.migration.core.task.component.BuildParameters;
import org.jboss.migration.core.task.component.CompositeSubtasks;
import org.jboss.migration.core.task.component.CompositeTask;
import org.jboss.migration.core.task.component.LeafTask;
import org.jboss.migration.core.task.component.TaskNameBuilder;
import org.jboss.migration.core.task.component.TaskRunnable;
import org.jboss.migration.core.task.component.TaskSkipPolicy;

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

        final String n = " ";
        final ServerMigrationTaskName nX = new ServerMigrationTaskName.Builder("X").build();
        final ServerMigrationTaskName nY = new ServerMigrationTaskName.Builder("Y").build();
        final ServerMigrationTaskName nZ = new ServerMigrationTaskName.Builder("Z").build();
        final ServerMigrationTaskName nXY = new ServerMigrationTaskName.Builder("XY").build();
        final ServerMigrationTaskName nXYZ = new ServerMigrationTaskName.Builder("XYZ").build();

        final TaskRunnable r = context -> ServerMigrationTaskResult.SUCCESS;
        final ServerMigrationTask t = new ServerMigrationTask() {
            @Override
            public ServerMigrationTaskName getName() {
                return new ServerMigrationTaskName.Builder(n).build();
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

        final TaskNameBuilder<BuildParameters> nf = parameters -> new ServerMigrationTaskName.Builder(n).build();
        final TaskNameBuilder<XTaskParams> nfX = parameters -> nX;
        final TaskNameBuilder<YTaskParams> nfY = parameters -> nY;
        final TaskNameBuilder<ZTaskParams> nfZ = parameters -> nZ;
        final TaskNameBuilder<XYTaskParams> nfXY = parameters -> nXY;
        final TaskNameBuilder<XYTaskParams> nfXYZ = parameters -> nXYZ;

        final TaskSkipPolicy sp = context -> true;
        final TaskSkipPolicy.Builder<XTaskParams> spX = parameters -> context -> false;
        final TaskSkipPolicy.Builder<YTaskParams> spY = parameters -> context -> false;
        final TaskSkipPolicy.Builder<ZTaskParams> spZ = parameters -> context -> false;
        final TaskSkipPolicy.Builder<XYTaskParams> spXY = parameters -> context -> true;
        final TaskSkipPolicy.Builder<XYZTaskParams> spXYZ = parameters -> context -> true;

        final BeforeTaskRun br = context -> {};
        final BeforeTaskRun.Builder<XTaskParams> brX = parameters -> context -> {};
        final BeforeTaskRun.Builder<YTaskParams> brY = parameters -> context -> {};
        final BeforeTaskRun.Builder<ZTaskParams> brZ = parameters -> context -> {};
        final BeforeTaskRun.Builder<XYTaskParams> brXY = parameters -> context -> {};
        final BeforeTaskRun.Builder<XYZTaskParams> brXYZ = parameters -> context -> {};

        final TaskRunnable.Builder<BuildParameters> rf = parameters -> r;
        final TaskRunnable.Builder<XTaskParams> rfX = parameters -> r;
        final TaskRunnable.Builder<YTaskParams> rfY = parameters -> r;
        final TaskRunnable.Builder<ZTaskParams> rfZ = parameters -> r;
        final TaskRunnable.Builder<XYTaskParams> rfXY = parameters -> r;
        final TaskRunnable.Builder<XYZTaskParams> rfXYZ = parameters -> r;

        final AfterTaskRun ar = context -> {};
        final AfterTaskRun.Builder<XTaskParams> arX = parameters -> context -> {};
        final AfterTaskRun.Builder<YTaskParams> arY = parameters -> context -> {};
        final AfterTaskRun.Builder<ZTaskParams> arZ = parameters -> context -> {};
        final AfterTaskRun.Builder<XYTaskParams> arXY = parameters -> context -> {};
        final AfterTaskRun.Builder<XYZTaskParams> arXYZ = parameters -> context -> {};

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
                .nameBuilder(nf)
                .name(n)
                .nameBuilder(nf)
                .skipPolicy(sp)
                .beforeRun(br)
                .run(r)
                .runBuilder(rf)
                .run(r)
                .runBuilder(rf)
                .afterRun(ar);

        lb
                .nameBuilder(nf)
                .name(n)
                .nameBuilder(nf)
                .name(n)
                .skipPolicy(sp)
                .beforeRun(br)
                .runBuilder(rf)
                .run(r)
                .runBuilder(rf)
                .run(r)
                .afterRun(ar);

        lbX
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .run(r)
                .runBuilder(rf)
                .runBuilder(rfX)
                .runBuilder(pcXX, rfX)
                .runBuilder(pcXY, rfY)
                .runBuilder(pcXZ, rfZ)
                .afterRun(ar)
                .afterRunBuilder(arX);

        lbY
                .nameBuilder(nfY)
                .nameBuilder(nf)
                .name(n)
                .skipPolicyBuilder(spY)
                .skipPolicy(sp)
                .beforeRunBuilder(brY)
                .beforeRun(br)
                .runBuilder(pcYX, rfX)
                .runBuilder(pcYY, rfY)
                .runBuilder(pcYZ, rfZ)
                .runBuilder(rfY)
                .runBuilder(rf)
                .run(r)
                .afterRunBuilder(arY)
                .afterRun(ar);

        lbZ
                .nameBuilder(nfZ)
                .skipPolicyBuilder(spZ)
                .beforeRunBuilder(brZ)
                .runBuilder(pcZX, rfX)
                .runBuilder(pcZY, rfY)
                .runBuilder(pcZZ, rfZ)
                .runBuilder(rfZ)
                .afterRunBuilder(arZ);

        lbXY
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .nameBuilder(nfY)
                .nameBuilder(nfXY)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .skipPolicyBuilder(spY)
                .skipPolicyBuilder(spXY)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .beforeRunBuilder(brY)
                .beforeRunBuilder(brXY)
                .run(r)
                .runBuilder(rf)
                .runBuilder(rfX)
                .runBuilder(rfY)
                .runBuilder(rfXY)
                .afterRun(ar)
                .afterRunBuilder(arX)
                .afterRunBuilder(arY)
                .afterRunBuilder(arXY);

        lbXYZ
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .nameBuilder(nfY)
                .nameBuilder(nfZ)
                .nameBuilder(nfXY)
                .nameBuilder(nfXYZ)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .skipPolicyBuilder(spY)
                .skipPolicyBuilder(spZ)
                .skipPolicyBuilder(spXY)
                .skipPolicyBuilder(spXYZ)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .beforeRunBuilder(brY)
                .beforeRunBuilder(brZ)
                .beforeRunBuilder(brXY)
                .beforeRunBuilder(brXYZ)
                .run(r)
                .runBuilder(rf)
                .runBuilder(rfX)
                .runBuilder(rfY)
                .runBuilder(rfZ)
                .runBuilder(rfXY)
                .runBuilder(rfXYZ)
                .afterRun(ar)
                .afterRunBuilder(arX)
                .afterRunBuilder(arY)
                .afterRunBuilder(arZ)
                .afterRunBuilder(arXY)
                .afterRunBuilder(arXYZ);

        cb
                .name(n)
                .nameBuilder(nf)
                .name(n)
                .nameBuilder(nf)
                .skipPolicy(sp)
                .beforeRun(br)
                .subtasks(new CompositeSubtasks.Builder<>()
                        .subtask(t)
                        .subtask(lb)
                        //.subtask(cb.clone())
                        .subtask(lb)
                        .subtask(t))
                .afterRun(ar);

        cbX
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .subtasks(new CompositeSubtasks.Builder<XTaskParams>()
                        .subtask(t)
                        .subtask(lb)
                        .subtask(lbX)
                        .subtask(cb)
                        //.subtask(cbX.clone())
                        .subtask(pcXX, lbX)
                        .subtask(pcXY, lbY)
                        .subtask(pcXZ, lbZ))
                .afterRun(ar)
                .afterRunBuilder(arX);

        cbY
                .nameBuilder(nfY)
                .nameBuilder(nf)
                .name(n)
                .skipPolicyBuilder(spY)
                .skipPolicy(sp)
                .beforeRunBuilder(brY)
                .beforeRun(br)
                .subtasks(new CompositeSubtasks.Builder<YTaskParams>()
                        .subtask(pcYX, lbX)
                        .subtask(pcYY, lbY)
                        .subtask(pcYZ, lbZ)
                        //.subtask(cbY.clone())
                        .subtask(cb)
                        .subtask(lbY)
                        .subtask(lb)
                        .subtask(t))
                .afterRunBuilder(arY)
                .afterRun(ar);

        cbZ
                .nameBuilder(nfZ)
                .skipPolicyBuilder(spZ)
                .beforeRunBuilder(brZ)
                .subtasks(new CompositeSubtasks.Builder<ZTaskParams>()
                        .subtask(t)
                        .subtask(pcZX, lbX)
                        .subtask(pcZY, lbY)
                        .subtask(pcZZ, lbZ)
                        .subtask(lbZ)
                        .subtask(cb))
                .afterRunBuilder(arZ);

        cbXY
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .nameBuilder(nfY)
                .nameBuilder(nfXY)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .skipPolicyBuilder(spY)
                .skipPolicyBuilder(spXY)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .beforeRunBuilder(brY)
                .beforeRunBuilder(brXY)
                .subtasks(new CompositeSubtasks.Builder<XYTaskParams>()
                        .subtask(t)
                        .subtask(lb)
                        .subtask(lbX)
                        .subtask(lbY)
                        .subtask(lbXY)
                        .subtask(cb)
                        .subtask(cbX)
                        .subtask(cbY)
                        //.subtask(cbXY.clone())
                )
                .afterRun(ar)
                .afterRunBuilder(arX)
                .afterRunBuilder(arY)
                .afterRunBuilder(arXY);

        cbXYZ
                .name(n)
                .nameBuilder(nf)
                .nameBuilder(nfX)
                .nameBuilder(nfY)
                .nameBuilder(nfZ)
                .nameBuilder(nfXY)
                .nameBuilder(nfXYZ)
                .skipPolicy(sp)
                .skipPolicyBuilder(spX)
                .skipPolicyBuilder(spY)
                .skipPolicyBuilder(spZ)
                .skipPolicyBuilder(spXY)
                .skipPolicyBuilder(spXYZ)
                .beforeRun(br)
                .beforeRunBuilder(brX)
                .beforeRunBuilder(brY)
                .beforeRunBuilder(brZ)
                .beforeRunBuilder(brXY)
                .beforeRunBuilder(brXYZ)
                .subtasks(new CompositeSubtasks.Builder<XYZTaskParams>()
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
                        //.subtask(cbXYZ.clone())
                )
                .afterRun(ar)
                .afterRunBuilder(arX)
                .afterRunBuilder(arY)
                .afterRunBuilder(arZ)
                .afterRunBuilder(arXY)
                .afterRunBuilder(arXYZ);

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
