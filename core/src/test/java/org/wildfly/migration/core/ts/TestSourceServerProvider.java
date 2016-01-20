package org.wildfly.migration.core.ts;

import org.wildfly.migration.core.ProductInfo;

import java.util.Collections;

/**
 * Created by emmartins on 18/01/16.
 */
public class TestSourceServerProvider extends AbstractTestServerProvider {
    static final TestServer SERVER = new TestServer(new ProductInfo("TestSourceServerName","TestSourceServerVersion"), Collections.<ProductInfo>emptySet());
    public TestSourceServerProvider() {
        super(SERVER);
    }
}
