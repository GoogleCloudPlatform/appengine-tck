// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appengine.testing.e2e.memcache;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;

/**
 * Tests Cache.
 *
 * @author kjin@google.com (Kevin Jin)
 * @author hchen@google.com (Hannah Chen)
 */
public class AbstractCacheTest extends BaseTest {
    protected static final String VALUE1 = "value1";
    protected static final String KEY1 = "key1";

    // data of various types for testing -- primitive types, String, Date, Set,
    // array, custom type
    protected static final ComboType COMBO1 = new ComboType(101, 123456789L, KEY1, new Date());
    protected static final ComboType COMBO2 = new ComboType(0, 0, null, new Date(0));
    protected static final ComboType COMBO3 =
            new ComboType(-1, -123456789L, VALUE1, new Date(-123456789L));
    protected static final Set<?> EMPTY_SET;
    protected static final Set<Object> SET1;

    static {
        EMPTY_SET = new HashSet<Object>();
        SET1 = new HashSet<Object>();
        Collections.addAll(SET1, COMBO1, COMBO2, COMBO3, null, VALUE1);
    }

    protected static final int[] ARRAY1 = {1, -1, 0};
    protected static final Object[] ARRAY2 = {1, VALUE1, COMBO2};

    protected static final Object[] TEST_DATA =
            {KEY1, 101, -1, 0, 123456789L, -987654321L, new Date(0), COMBO1, EMPTY_SET, SET1};

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getCapedwarfDeployment();
        war.addClass(AbstractCacheTest.class);
        war.addClass(ComboType.class);
        return war;
    }
}
