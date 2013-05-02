/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.memcache;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.memcache.support.ComboType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Tests Cache.
 *
 * @author kjin@google.com (Kevin Jin)
 * @author hchen@google.com (Hannah Chen)
 */
public abstract class CacheTestBase extends TestBase {
    protected static final String STR_VALUE = "str_value";
    protected static final String KEY1 = "key1";
    protected static final int overhead = 1024;   // space for key value
    protected static final String str1mb = getBigString(1024 * 1024 - overhead);
    protected static final String str1K = getBigString(1024);

    // data of various types for testing -- primitive types, String, Date, Set,
    // array, custom type
    protected static final ComboType COMBO1 = new ComboType(101, 123456789L, KEY1, new Date());
    protected static final ComboType COMBO2 = new ComboType(0, 0, null, new Date(0));
    protected static final ComboType COMBO3 = new ComboType(-1, -123456789L, STR_VALUE, new Date(-123456789L));
    protected static final Set<?> EMPTY_SET;
    protected static final Set<Object> SET1;

    protected String createTimeStampKey(String name) {
        return name + "-" + System.currentTimeMillis();
    }

    static {
        EMPTY_SET = new HashSet<Object>();
        SET1 = new HashSet<Object>();
        Collections.addAll(SET1, COMBO1, COMBO2, COMBO3, null, STR_VALUE);
    }

    protected static final int[] ARRAY1 = {1, -1, 0};
    protected static final Object[] ARRAY2 = {1, STR_VALUE, COMBO2};
    protected static final Object[] TEST_DATA = {KEY1, 101, -1, 0, 123456789L, -987654321L, new Date(0),
        COMBO1, EMPTY_SET, SET1};

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(CacheTestBase.class);
        war.addClass(ComboType.class);
        return war;
    }

    protected static String getBigString(int len) {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = 'x';
        }
        return new String(chars);
    }

    protected Map<Object, Object> createSmallBatchData() {
        String tsKey = createTimeStampKey("SmallBatchData");
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (int i = 0; i < 3; i++) {
            map.put(tsKey + "-" + i, str1K);
        }
        return map;
    }

    protected Map<Object, Long> createLongBatchData() {
        String tsKey = createTimeStampKey("LongBatchData");
        Map<Object, Long> map = new HashMap<Object, Long>();
        for (long num = 0; num < 3; num++) {
            map.put(tsKey + "-" + num, num);
        }
        return map;
    }

    protected Map<Object, Long> copyMapIncrementLongValue(Map<Object, Long> map, long delta) {
        Map<Object, Long> copiedMap = new HashMap<Object, Long>();
        for (Map.Entry<Object, Long> entry : map.entrySet()) {
            copiedMap.put(entry.getKey(), entry.getValue() + delta);
        }
        return copiedMap;
    }

    protected Map<Object, Long> createRandomIncrementMap(Map<Object, Long> map) {
        Map<Object, Long> incMap = new HashMap<Object, Long>();
        Random random = new Random();

        int max = 1000;
        for (Map.Entry<Object, Long> entry : map.entrySet()) {
            incMap.put(entry.getKey(), new Long(random.nextInt(max)));
        }
        return incMap;
    }

    protected Map<Object, Long> createMapFromIncrementMap(Map<Object, Long> originalMap,
                                                          Map<Object, Long> incMap) {
        Map<Object, Long> incrementedMap = new HashMap<Object, Long>();

        for (Map.Entry<Object, Long> entry : originalMap.entrySet()) {
            incrementedMap.put(entry.getKey(), entry.getValue() + incMap.get(entry.getKey()));
        }

        return incrementedMap;
    }
}
