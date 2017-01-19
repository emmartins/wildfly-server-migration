package org.jboss.migration.wfly10.config.management;

import java.util.stream.Stream;

/**
 * @author emmartins
 */
public class BasicManageableResourceType<T extends ManageableResource> implements ManageableResource.Type<T> {

    private final Class<T> type;
    private final ManageableResource.Type[] childTypes;
    private final ManageableResource.Type[] childTypesRecursive;

    public BasicManageableResourceType(Class<T> type, ManageableResource.Type... childTypes) {
        this.type = type;
        this.childTypes = childTypes;
        this.childTypesRecursive = Stream.of(childTypes)
                .flatMap(childType -> Stream.of(childType.getChildTypes(true)))
                .toArray(ManageableResource.Type[]::new);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public ManageableResource.Type[] getChildTypes(boolean recursive) {
        return recursive ? childTypesRecursive : childTypes;
    }
}
