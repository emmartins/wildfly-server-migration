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

package org.jboss.migration.wfly11.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author emmartins
 */
public class FileAuditLogAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String fileAuditLog;
    private String path;
    private String relativeTo;
    private String format;

    public FileAuditLogAddOperation(PathAddress subsystemPathAddress, String fileAuditLog) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.fileAuditLog = fileAuditLog;
    }

    public FileAuditLogAddOperation path(String path) {
        this.path = path;
        return this;
    }

    public FileAuditLogAddOperation relativeTo(String relativeTo) {
        this.relativeTo = relativeTo;
        return this;
    }

    public FileAuditLogAddOperation format(String format) {
        this.format = format;
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "path" => "audit.log",
            "relative-to" => "jboss.server.log.dir",
            "format" => "JSON",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("file-audit-log" => "local-audit")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("file-audit-log", fileAuditLog);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (path != null) {
            operation.get("path").set(path);
        }
        if (relativeTo != null) {
            operation.get("relative-to").set(relativeTo);
        }
        if (format != null) {
            operation.get("format").set(format);
        }
        return operation;
    }
}
