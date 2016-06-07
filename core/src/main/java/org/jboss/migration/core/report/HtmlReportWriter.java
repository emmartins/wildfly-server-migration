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
import org.jboss.migration.core.ServerMigrationTaskExecution;
import org.jboss.migration.core.ServerMigrationTaskResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * The HTML report writer.
 * @author emmartins
 */
public class HtmlReportWriter {

    public static HtmlReportWriter INSTANCE = new HtmlReportWriter();

    private static final int MAX_TASK_MAP_DEPTH = 4;

    private HtmlReportWriter() {

    }

    public void toPath(Path path, MigrationData migrationData, ReportTemplate template) throws IOException {
        final String s = toString(migrationData,template);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(s, 0, s.length());
        }
    }

    public String toString(MigrationData migrationData, ReportTemplate template) {
        final StringBuilder sb = new StringBuilder();
        sb.append(template.header);
        appendSummary(migrationData, sb);
        sb.append(template.summaryToTaskSummary);
        appendTaskSummary(migrationData, sb);
        sb.append(template.taskSummaryToTaskMap);
        appendTaskMap(migrationData, sb);
        sb.append(template.footer);
        return sb.toString();
    }

    private void appendSummary(MigrationData migrationData, StringBuilder sb) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date(migrationData.getRootTask().getStartTime()));
        appendProperty("Start Time", utcTime, sb);
        appendProperty("Source Server", migrationData.getSource().getProductInfo().getName() + ' ' + migrationData.getSource().getProductInfo().getVersion(), sb);
        appendProperty("Target Server", migrationData.getTarget().getProductInfo().getName() + ' ' + migrationData.getTarget().getProductInfo().getVersion(), sb);
        appendProperty("Result", getTaskStatus(migrationData.getRootTask().getResult(), migrationData.getRootTask().getResult().getStatus()), sb);
    }

    private void appendTaskSummary(MigrationData migrationData, StringBuilder sb) {
        appendProperty("Executed", migrationData.getTasks().size(), sb);
        appendProperty("Successful", migrationData.getTaskCount(ServerMigrationTaskResult.Status.SUCCESS), sb);
        appendProperty("Skipped", migrationData.getTaskCount(ServerMigrationTaskResult.Status.SKIPPED), sb);
        appendProperty("Failed", migrationData.getTaskCount(ServerMigrationTaskResult.Status.FAIL), sb);
    }

    private void appendTaskMap(MigrationData migrationData, StringBuilder sb) {
        appendTask(migrationData.getRootTask(), sb);
    }

    private void appendTask(ServerMigrationTaskExecution task, StringBuilder sb) {
        final String tableClass = (task.getTaskPath().size() % 2) == 0 ? "task-map-even" : "task-map-odd";
        sb.append("<table class=\"").append(tableClass).append("\">");
        appendTaskHeader(task, sb);
        appendTaskDetails(task, sb);
        appendTaskSubtasks(task, sb);
        sb.append("</table>");
    }

    private void appendTaskHeader(ServerMigrationTaskExecution task, StringBuilder sb) {
        sb.append("<tr>");
        sb.append("<td class=\"task-map-header\">");
        sb.append("<table class=\"task-header\">");
        sb.append("<tr>");
        // name
        sb.append("<td class=\"task-header-name\" id=\"task").append(task.getTaskNumber()).append("\">");
        sb.append("<a class=\"task-display-toggle\" href=\"#task").append(task.getTaskNumber()).append("\" onclick=\"toggleDisplayTaskDetails('task").append(task.getTaskNumber()).append("'); return false\">").append(getTaskStatus(task.getResult(), task.getTaskName())).append("</a>");
        sb.append("</td>");
        // subtasks toggles
        if (!task.getSubtasks().isEmpty()) {
            sb.append("<td class=\"task-header-toggles\">");
            sb.append("<table>");
            sb.append("<tr>");
            if (task.getTaskPath().size() > MAX_TASK_MAP_DEPTH) {
                sb.append("<td class=\"task-display-toggle\" style=\"display: none\" id=\"task").append(task.getTaskNumber()).append("-subtasks-hide\">");
            } else {
                sb.append("<td class=\"task-display-toggle\" id=\"task").append(task.getTaskNumber()).append("-subtasks-hide\">");
            }
            sb.append("<a class=\"task-display-toggle\" href=\"#task").append(task.getTaskNumber()).append("\" onclick=\"toggleDisplayTaskSubtasks('task").append(task.getTaskNumber()).append("'); return false\">-</a>");
            sb.append("</td>");
            if (task.getTaskPath().size() > MAX_TASK_MAP_DEPTH) {
                sb.append("<td class=\"task-display-toggle\" id=\"task").append(task.getTaskNumber()).append("-subtasks-show\">");
            } else {
                sb.append("<td class=\"task-display-toggle\" style=\"display: none\" id=\"task").append(task.getTaskNumber()).append("-subtasks-show\">");
            }
            sb.append("<a class=\"task-display-toggle\" href=\"#task").append(task.getTaskNumber()).append("\" onclick=\"toggleDisplayTaskSubtasks('task").append(task.getTaskNumber()).append("'); return false\">+</a>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</td>");
        }
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</td>");
        sb.append("</tr>");
    }

    private void appendTaskDetails(ServerMigrationTaskExecution task, StringBuilder sb) {
        sb.append("<tr>");
        sb.append("<td id=\"task").append(task.getTaskNumber()).append("-details\" style=\"display: none\" class=\"task-map-details\"><table class=\"task-details\">");

        appendTaskDetailsProperty("Number", task.getTaskNumber(), sb);
        appendTaskDetailsProperty("Name", task.getTaskName(), sb);
        appendTaskDetailsProperty("Path", task.getTaskPath(), sb);
        appendTaskDetailsProperty("Logger", task.getLogger().getName(), sb);

        // result
        final ServerMigrationTaskResult result = task.getResult();
        appendTaskDetailsProperty("Result Status", getTaskStatus(result, result.getStatus()), sb);
        if (result.getStatus() == ServerMigrationTaskResult.Status.FAIL) {
            appendTaskDetailsProperty("Fail Reason", result.getFailReason(), sb);
        }
        final Map<String, String> attributes = result.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            final StringBuilder temp = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    temp.append("<br/>");
                }
                temp.append(attribute.getKey()).append('=').append(attribute.getValue());
            }
            appendTaskDetailsProperty("Result Attributes", temp, sb);
        }

        // subtasks
        final StringBuilder temp = new StringBuilder();
        boolean first = true;
        for (ServerMigrationTaskExecution subtask : task.getSubtasks()) {
            if (first) {
                first = false;
            } else {
                temp.append("<br/>");
            }
            temp.append("<a href=\"#task").append(subtask.getTaskNumber()).append("\">").append(subtask.getTaskName()).append("</a>");
        }
        appendTaskDetailsProperty("Subtasks", temp, sb);

        sb.append("</table></td></tr>");
    }

    private void appendTaskSubtasks(ServerMigrationTaskExecution task, StringBuilder sb) {
        final List<ServerMigrationTaskExecution> subtasks = task.getSubtasks();
        if (!subtasks.isEmpty()) {
            sb.append("<tr>");
            if (task.getTaskPath().size() > MAX_TASK_MAP_DEPTH) {
                sb.append("<td class=\"task-map-subtasks\" style=\"display: none\" id=\"task").append(task.getTaskNumber()).append("-subtasks\">");
            } else {
                sb.append("<td class=\"task-map-subtasks\" id=\"task").append(task.getTaskNumber()).append("-subtasks\">");
            }
            sb.append("<table class=\"task-subtasks\">");
            for (ServerMigrationTaskExecution subtask : subtasks) {
                sb.append("<tr>");
                sb.append("<td>");
                appendTask(subtask, sb);
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</td>");
            sb.append("</tr>");
        }
    }

    private void appendTaskDetailsProperty(String propertyName, Object propertyValue, StringBuilder sb) {
        sb.append("<tr><td class=\"task-details-property-name\">")
                .append(propertyName)
                .append(":</td><td class=\"task-details-property-value\">")
                .append(propertyValue)
                .append("</td></tr>");
    }

    private void appendProperty(String propertyName, Object propertyValue, StringBuilder sb) {
        sb.append("<tr><td>")
                .append(propertyName)
                .append(":</td><td>")
                .append(propertyValue)
                .append("</td></tr>");
    }

    private String getTaskStatus(ServerMigrationTaskResult result, Object text) {
        StringBuilder sb = new StringBuilder("<span class=\"task-result-");
        sb.append(result.getStatus());
        sb.append("\">").append(text).append("</span>");
        return sb.toString();
    }

    public static class ReportTemplate {
        private final String header;
        private final String summaryToTaskSummary;
        private final String taskSummaryToTaskMap;
        private final String footer;

        private ReportTemplate(String header, String summaryToTaskSummary, String taskSummaryToTaskMap, String footer) {
            this.header = header;
            this.summaryToTaskSummary = summaryToTaskSummary;
            this.taskSummaryToTaskMap = taskSummaryToTaskMap;
            this.footer = footer;
        }

        public static ReportTemplate from(Path path) throws IOException {
            return from(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        }

        public static ReportTemplate from(String string) {
            final String summaryMarker = "$SUMMARY";
            final String taskSummaryMarker = "$TASK_SUMMARY";
            final String taskMapMarker = "$TASK_MAP";

            final int summaryIndex = string.indexOf(summaryMarker);
            final String header = string.substring(0, summaryIndex);

            final int taskSummaryIndex = string.indexOf(taskSummaryMarker);
            final String summaryToTaskSummary = string.substring(summaryIndex+summaryMarker.length(), taskSummaryIndex);

            final int taskMapIndex = string.indexOf(taskMapMarker);
            final String taskSummaryToTaskMap = string.substring(taskSummaryIndex+taskSummaryMarker.length(), taskMapIndex);

            final String footer = string.substring(taskMapIndex+taskMapMarker.length());

            return new ReportTemplate(header, summaryToTaskSummary, taskSummaryToTaskMap, footer);
        }
    }
}
