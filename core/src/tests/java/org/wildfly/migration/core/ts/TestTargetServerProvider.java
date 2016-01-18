package org.wildfly.migration.core.ts;

import org.wildfly.migration.core.ProductInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by emmartins on 18/01/16.
 */
public class TestTargetServerProvider extends AbstractTestServerProvider {

    private static final Set<ProductInfo> SOURCE_SERVERS = initSourceServers();

    private static Set<ProductInfo> initSourceServers() {
        final Set<ProductInfo> set = new HashSet<>();
        set.add(TestSourceServerProvider.SERVER.getProductInfo());
        return set;
    }

    static final TestServer SERVER = new TestServer(new ProductInfo("TestTargetServerName","TestTargetServerVersion"), SOURCE_SERVERS);

    public TestTargetServerProvider() {
        super(SERVER);
    }
}
