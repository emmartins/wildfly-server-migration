package org.jboss.migration.wfly10.config.task.hostexclude;

import org.jboss.migration.core.jboss.HostExclude;
import org.jboss.migration.core.jboss.HostExcludes;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.component.TaskSkipPolicy;
import org.jboss.migration.wfly10.config.management.HostExcludeResource;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeSubtasks;
import org.jboss.migration.wfly10.config.task.management.configuration.ManageableServerConfigurationCompositeTask;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceLeafTask;

import java.util.Set;

/**
 * A task which adds a specific host-excludes to a host controller configuration.
 * @author emmartins
 */
public class AddHostExcludes<S> extends ManageableServerConfigurationCompositeTask.Builder<S> {

    public AddHostExcludes(HostExcludes hostExcludes) {
        name("host-excludes.add");
        skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
        beforeRun(context -> context.getLogger().debug("Adding host-excludes configuration..."));
        final ManageableServerConfigurationCompositeSubtasks.Builder<S> subtasksBuilder = new ManageableServerConfigurationCompositeSubtasks.Builder<>();
        // first we remove all existent
        subtasksBuilder.subtask(HostExcludeResource.Parent.class, new RemoveAllHostExclude<>());
        // then add each host-exclude config
        for (HostExclude hostExclude : hostExcludes.getHostExcludes()) {
            subtasksBuilder.subtask(HostExcludeResource.Parent.class, new AddHostExclude<>(hostExclude));
        }
        subtasks(subtasksBuilder);
        afterRun(context -> {
            if (context.hasSucessfulSubtasks()) {
                context.getLogger().info("Host-excludes configuration added.");
            } else {
                context.getLogger().debug("Host-excludes configuration not added.");
            }
        });
    }

    private static class RemoveAllHostExclude<S> extends ManageableResourceLeafTask.Builder<S, HostExcludeResource.Parent> {
        public RemoveAllHostExclude() {
            name("host-exclude.removeAll");
            skipPolicy(TaskSkipPolicy.skipIfDefaultTaskSkipPropertyIsSet());
            beforeRun(context -> context.getLogger().debugf("Removing all legacy host-exclude configs..."));
            runBuilder(params -> taskContext -> {
                final HostExcludeResource.Parent parent = params.getResource();
                final Set<String> legacyHostExcludeNames = parent.getHostExcludeResourceNames();
                if (legacyHostExcludeNames.isEmpty()) {
                    taskContext.getLogger().debugf("No legacy host-exclude configs found.");
                    return ServerMigrationTaskResult.SKIPPED;
                }
                for (String hostExcludeName : legacyHostExcludeNames) {
                    parent.removeHostExcludeResource(hostExcludeName);
                    taskContext.getLogger().debugf("Legacy host-exclude %s found and removed.", hostExcludeName);
                }
                taskContext.getLogger().debugf("All legacy host-exclude configs removed.");
                return ServerMigrationTaskResult.SUCCESS;
            });
        }
    }
}
