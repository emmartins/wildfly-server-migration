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
package org.jboss.migration.cli;

/**
 * The CLI environment properties.
 * @author emmartins.
 */
public interface EnvironmentProperties {

    /**
     * The base dir of the tool.
     */
    String BASE_DIR = "baseDir";

    /**
     * The xml migration report file name.
     */
    String REPORT_XML_FILE_NAME = org.jboss.migration.core.report.EnvironmentProperties.XML_REPORT_PROPERTIES_PREFIX + "fileName";

    /**
     * The html migration report file name.
     */
    String REPORT_HTML_FILE_NAME = org.jboss.migration.core.report.EnvironmentProperties.HTML_REPORT_PROPERTIES_PREFIX + "fileName";

    /**
     * The html migration report template file name.
     */
    String REPORT_HTML_TEMPLATE_FILE_NAME = org.jboss.migration.core.report.EnvironmentProperties.HTML_REPORT_PROPERTIES_PREFIX + "templateFileName";

}
