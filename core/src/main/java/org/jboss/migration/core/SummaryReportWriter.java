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
package org.jboss.migration.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The summary report builder.
 * @author emmartins
 */
public class SummaryReportWriter {

    private static final String TASK_ID_LEVEL_INDENT = " ";
    private static final char SEPARATOR_CHAR = '.';
    private static final int MIN_SEPARATOR_LENGTH = 3;

    public String toString(MigrationData migrationData) {
        final List<SummaryTaskEntry> summaryTaskEntries = getSummaryTaskEntries(migrationData);
        final int taskIdAndSeparatorLength = getTaskIdAndSeparatorLength(summaryTaskEntries);
        final String lineSeparator = getLineSeparator(taskIdAndSeparatorLength);
        final StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(lineSeparator);
        sb.append(" Migration Summary\n");
        sb.append(lineSeparator);
        sb.append('\n');
        appendTasks(sb, summaryTaskEntries, taskIdAndSeparatorLength);
        sb.append('\n');
        sb.append(lineSeparator);
        appendRootTaskResult(sb, migrationData);
        sb.append(lineSeparator);
        return sb.toString();
    }

    protected List<SummaryTaskEntry> getSummaryTaskEntries(MigrationData migrationData) {
        final List<SummaryTaskEntry> summaryTaskEntries = new ArrayList<>();
        getSummaryTaskEntry(migrationData.getRootTask(), " ", summaryTaskEntries);
        return  summaryTaskEntries;
    }

    protected void getSummaryTaskEntry(ServerMigrationTaskExecution task, String prefix, List<SummaryTaskEntry> summaryTaskEntries) {
        if (task.getResult().getStatus() == ServerMigrationTaskResult.Status.SKIPPED) {
            return;
        }
        String taskIdWithPrefix = prefix+task.getTaskId().toString();
        summaryTaskEntries.add(new SummaryTaskEntry(taskIdWithPrefix, task.getResult()));
        String subtaskPrefix = TASK_ID_LEVEL_INDENT + prefix;
        for (ServerMigrationTaskExecution subtask : task.getSubtasks()) {
            getSummaryTaskEntry(subtask, subtaskPrefix, summaryTaskEntries);
        }
    }

    protected int getTaskIdAndSeparatorLength(List<SummaryTaskEntry> summaryTaskEntries) {
        int maxTaskIdLength = 0;
        for (SummaryTaskEntry summaryTaskEntry : summaryTaskEntries) {
            if (summaryTaskEntry.taskId.length() > maxTaskIdLength) {
                maxTaskIdLength = summaryTaskEntry.taskId.length();
            }
        }
        return maxTaskIdLength + MIN_SEPARATOR_LENGTH;
    }

    protected String getLineSeparator(int taskIdAndSeparatorLength) {
        // append header
        final StringBuilder sb = new StringBuilder();
        //sb.append(' ');
        int headerLineLength = taskIdAndSeparatorLength + 8;
        for (int i=0; i < headerLineLength; i++) {
            sb.append('-');
        }
        sb.append('\n');
        return sb.toString();
    }

    protected void appendRootTaskResult(StringBuilder sb, MigrationData migrationData) {
        sb.append(" Migration Result: ").append(migrationData.getRootTask().getResult().getStatus()).append('\n');
    }

    protected void appendTasks(StringBuilder sb, List<SummaryTaskEntry> summaryTaskEntries, int taskIdAndSeparatorLength) {
        //sb.append('\n');
        for (SummaryTaskEntry summaryTaskEntry : summaryTaskEntries) {
            // append task id
            sb.append(summaryTaskEntry.taskId);
            // append separator between task id and result status
            int suffixLength =  taskIdAndSeparatorLength - summaryTaskEntry.taskId.length();
            sb.append(' ');
            for (int i=1; i < suffixLength; i++) {
                sb.append(SEPARATOR_CHAR);
            }
            sb.append(' ');
            // append task result status
            sb.append(summaryTaskEntry.taskResult.getStatus()).append('\n');
        }
    }

    private static class SummaryTaskEntry {
        private final String taskId;
        private final ServerMigrationTaskResult taskResult;
        private SummaryTaskEntry(String taskId, ServerMigrationTaskResult taskResult) {
            this.taskId = taskId;
            this.taskResult = taskResult;
        }
    }
}