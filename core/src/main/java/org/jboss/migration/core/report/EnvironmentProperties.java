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

/**
 * Environment properties related Migration Reports.
 * @author emmartins
 */
public interface EnvironmentProperties {

    /**
     * the prefix of all properties related with migration reports
     */
    String REPORT_PROPERTIES_PREFIX = "report.";

    /**
     * the prefix of all properties related with the XML report
     */
    String XML_REPORT_PROPERTIES_PREFIX = REPORT_PROPERTIES_PREFIX + "XML.";

    /**
     * the prefix of all properties related with the HTML report
     */
    String HTML_REPORT_PROPERTIES_PREFIX = REPORT_PROPERTIES_PREFIX + "HTML.";

    /**
     * The HTML report will show subtasks, if the task has a path size smaller or equal, than this property value.
     */
    String PROPERTY_MAX_TASK_PATH_SIZE_TO_DISPLAY_SUBTASKS = HTML_REPORT_PROPERTIES_PREFIX + "maxTaskPathSizeToDisplaySubtasks";
}
