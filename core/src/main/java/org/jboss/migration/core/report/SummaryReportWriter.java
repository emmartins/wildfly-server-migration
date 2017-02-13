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
package org.jboss.migration.core.report;

import org.jboss.migration.core.MigrationData;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * The summary report writer.
 * @author emmartins
 */
public class SummaryReportWriter {

    public static SummaryReportWriter INSTANCE = new SummaryReportWriter();

    private SummaryReportWriter() {

    }

    private static final String TASK_NAME_LEVEL_INDENT = " ";
    private static final char SEPARATOR_CHAR = '.';
    private static final int MIN_SEPARATOR_LENGTH = 3;

    public String toString(MigrationData migrationData) {
        final List<SummaryTaskEntry> summaryTaskEntries = getSummaryTaskEntries(migrationData);
        final int taskNameAndSeparatorLength = getTaskNameAndSeparatorLength(summaryTaskEntries);
        final String lineSeparator = getLineSeparator(taskNameAndSeparatorLength);
        final StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(lineSeparator);
        sb.append(" Task Summary\n");
        sb.append(lineSeparator);
        sb.append('\n');
        appendTasks(sb, summaryTaskEntries, taskNameAndSeparatorLength);
        sb.append('\n');
        sb.append(lineSeparator);
        appendRootTaskResult(sb, migrationData);
        sb.append(lineSeparator);
        return sb.toString();
    }

    protected List<SummaryTaskEntry> getSummaryTaskEntries(MigrationData migrationData) {
        final int maxTaskPathSizeToDisplaySubtasks = Integer.valueOf(migrationData.getServerMigrationEnvironment().getPropertyAsString(EnvironmentProperties.SUMMARY_REPORT_PROPERTY_MAX_TASK_PATH_SIZE_TO_DISPLAY_SUBTASKS, "5"));
        final List<SummaryTaskEntry> summaryTaskEntries = new ArrayList<>();
        int taskDepth = 0;
        getSummaryTaskEntry(migrationData.getRootTask(), " ", summaryTaskEntries, taskDepth, maxTaskPathSizeToDisplaySubtasks);
        return  summaryTaskEntries;
    }

    protected void getSummaryTaskEntry(TaskExecution task, String prefix, List<SummaryTaskEntry> summaryTaskEntries, int taskDepth, int maxTaskPathSizeToDisplaySubtasks) {
        if (task.getResult().getStatus() == ServerMigrationTaskResult.Status.SKIPPED) {
            return;
        }
        String taskNameWithPrefix = prefix+task.getTaskName().toString();
        summaryTaskEntries.add(new SummaryTaskEntry(taskNameWithPrefix, task.getResult()));
        String subtaskPrefix = TASK_NAME_LEVEL_INDENT + prefix;
        taskDepth++;
        if (taskDepth > maxTaskPathSizeToDisplaySubtasks) {
            return;
        }
        for (TaskExecution subtask : task.getSubtasks()) {
            getSummaryTaskEntry(subtask, subtaskPrefix, summaryTaskEntries, taskDepth, maxTaskPathSizeToDisplaySubtasks);
        }
    }

    protected int getTaskNameAndSeparatorLength(List<SummaryTaskEntry> summaryTaskEntries) {
        int maxTaskNameLength = 0;
        for (SummaryTaskEntry summaryTaskEntry : summaryTaskEntries) {
            if (summaryTaskEntry.taskName.length() > maxTaskNameLength) {
                maxTaskNameLength = summaryTaskEntry.taskName.length();
            }
        }
        return maxTaskNameLength + MIN_SEPARATOR_LENGTH;
    }

    protected String getLineSeparator(int taskNameAndSeparatorLength) {
        // append header
        final StringBuilder sb = new StringBuilder();
        //sb.append(' ');
        int headerLineLength = taskNameAndSeparatorLength + 8;
        for (int i=0; i < headerLineLength; i++) {
            sb.append('-');
        }
        sb.append('\n');
        return sb.toString();
    }

    protected void appendRootTaskResult(StringBuilder sb, MigrationData migrationData) {
        sb.append(" Migration Result: ").append(migrationData.getRootTask().getResult().getStatus()).append('\n');
    }

    protected void appendTasks(StringBuilder sb, List<SummaryTaskEntry> summaryTaskEntries, int taskNameAndSeparatorLength) {
        //sb.append('\n');
        for (SummaryTaskEntry summaryTaskEntry : summaryTaskEntries) {
            // append task id
            sb.append(summaryTaskEntry.taskName);
            // append separator between task id and result status
            int suffixLength =  taskNameAndSeparatorLength - summaryTaskEntry.taskName.length();
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
        private final String taskName;
        private final ServerMigrationTaskResult taskResult;
        private SummaryTaskEntry(String taskName, ServerMigrationTaskResult taskResult) {
            this.taskName = taskName;
            this.taskResult = taskResult;
        }
    }
}