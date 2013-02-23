/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package com.google.appengine.tck.memcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.memcache.ErrorHandler;
import com.google.appengine.api.memcache.ConsistentErrorHandler;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.Stats;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.infinispan.CacheName;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;
import org.jboss.capedwarf.common.infinispan.WrapperTxCallable;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class TckMemcacheService implements MemcacheService {

    protected static final Logger log = Logger.getLogger(TckMemcacheService.class.getName());
    private static final SetPolicy DEFAULT_SET_POLICY = SetPolicy.SET_ALWAYS;

    protected final Cache<com.google.appengine.tck.memcache.NamespacedKey, Object> cache;
    private String namespace;
    private ErrorHandler errorHandler;

    public TckMemcacheService() {
        this(null);
    }

    public TckMemcacheService(String namespace) {
        setNamespace(namespace);
        this.cache = InfinispanUtils.getCache(Application.getAppId(), CacheName.MEMCACHE);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        if (namespace != null) {
            NamespaceManager.validateNamespace(namespace);
        }
        this.namespace = namespace;
    }

    public Object get(Object key) {
        return cache.get(namespacedKey(key));
    }

    public IdentifiableValue getIdentifiable(final Object key) {
        return new MyIdentifiableValue(get(key));
    }

    public <T> Map<T, IdentifiableValue> getIdentifiables(Collection<T> keys) {
        Map<T, IdentifiableValue> map = new HashMap<T, IdentifiableValue>();
        for (T key : keys) {
            map.put(key, getIdentifiable(key));
        }
        return map;
    }

    public boolean contains(Object key) {
        return cache.containsKey(namespacedKey(key));
    }

    public <T> Map<T, Object> getAll(Collection<T> keys) {
        // TODO: batching
        Map<T, Object> map = new HashMap<T, Object>();
        for (T key : keys) {
            map.put(key, get(key));
        }
        return map;
    }

    public void put(Object key, Object value) {
        put(key, value, null);
    }

    public void put(Object key, Object value, Expiration expiration) {
        put(key, value, expiration, DEFAULT_SET_POLICY);
    }

    public boolean put(Object key, Object value, Expiration expiration, SetPolicy policy) {
        NamespacedKey namespacedKey = namespacedKey(key);
        switch (policy) {
            case SET_ALWAYS: {
                cache.getAdvancedCache()
                        .withFlags(Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP)
                        .put(namespacedKey, value, toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                return true;
            }
            case ADD_ONLY_IF_NOT_PRESENT: {
                Object previousValue = cache.putIfAbsent(namespacedKey, value, toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                return previousValue == null;
            }
            case REPLACE_ONLY_IF_PRESENT: {
                Object previousValue = cache.replace(namespacedKey, value, toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                return previousValue != null;
            }
            default:
                throw new IllegalArgumentException("Unsupported policy " + policy);
        }
    }

    public <T> Set<T> putIfUntouched(Map<T, CasValues> values) {
        return putIfUntouched(values, null);
    }

    public <T> Set<T> putIfUntouched(Map<T, CasValues> values, Expiration expiration) {
        //        TODO: cache.startBatch();
        Set<T> set = new HashSet<T>();
        for (Map.Entry<T, CasValues> entry : values.entrySet()) {
            T key = entry.getKey();
            CasValues casValues = entry.getValue();
            Expiration actualExpiration = casValues.getExipration() == null ? expiration : casValues.getExipration();
            boolean stored = putIfUntouched(key, casValues.getOldValue(), casValues.getNewValue(), actualExpiration);
            if (stored) {
                set.add(key);
            }
        }
        return set;
    }

    public boolean putIfUntouched(Object key, IdentifiableValue oldValue, Object newValue) {
        return putIfUntouched(key, oldValue, newValue, null);
    }

    public boolean putIfUntouched(Object key, IdentifiableValue oldValue, Object newValue, Expiration expiration) {
        return cache.replace(namespacedKey(key), oldValue.getValue(), newValue, toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
    }

    public void putAll(Map<?, ?> map) {
        putAll(map, null);
    }

    public void putAll(Map<?, ?> map, Expiration expiration) {
        putAll(map, expiration, DEFAULT_SET_POLICY);
    }

    public <T> Set<T> putAll(Map<T, ?> map, Expiration expiration, SetPolicy policy) {
        //        TODO: cache.startBatch();
        switch (policy) {
            case SET_ALWAYS:
                cache.getAdvancedCache()
                        .withFlags(Flag.SKIP_CACHE_LOAD, Flag.SKIP_REMOTE_LOOKUP)
                        .putAll(toNamespacedMap(map), toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                return map.keySet();
            case ADD_ONLY_IF_NOT_PRESENT:
                Set<T> addedKeys = new HashSet<T>();
                for (Map.Entry<T, ?> entry : map.entrySet()) {
                    Object previousValue = cache.putIfAbsent(namespacedKey(entry.getKey()), entry.getValue(), toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                    if (previousValue == null) {
                        addedKeys.add(entry.getKey());
                    }
                }
                return addedKeys;
            case REPLACE_ONLY_IF_PRESENT:
                Set<T> replacedKeys = new HashSet<T>();
                for (Map.Entry<T, ?> entry : map.entrySet()) {
                    Object previousValue = cache.replace(namespacedKey(entry.getKey()), entry.getValue(), toLifespanMillis(expiration), TimeUnit.MILLISECONDS);
                    if (previousValue != null) {
                        replacedKeys.add(entry.getKey());
                    }
                }
                return replacedKeys;
            default:
                throw new IllegalArgumentException("Unsupported policy " + policy);
        }
    }

    public boolean delete(Object key) {
        return delete(key, 0L);
    }

    public boolean delete(Object key, long millisNoReAdd) {
        Object removedObject = cache.remove(namespacedKey(key));
        return removedObject != null; // TODO -- use millisNoReAdd
    }

    public <T> Set<T> deleteAll(Collection<T> keys) {
        return deleteAll(keys, 0L);
    }

    public <T> Set<T> deleteAll(Collection<T> keys, long millisNoReAdd) {
        Set<T> deletedKeys = new HashSet<T>();
        for (T key : keys) {
            Object previousValue = cache.remove(namespacedKey(key));
            if (previousValue != null) {
                deletedKeys.add(key);
            }
        }
        return deletedKeys; // TODO -- use millisNoReAdd
    }

    protected void lock(Object key) {
        final AdvancedCache<NamespacedKey, Object> ac = cache.getAdvancedCache();
        if (ac.lock(namespacedKey(key)) == false)
            throw new IllegalArgumentException("Cannot lock key: " + key);
    }

    protected long castToLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            String string = (String) value;
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                throw new InvalidValueException("Cannot increment. Value was " + value);
            }
        }
        throw new InvalidValueException("Cannot increment. Value was " + value);
    }

    private Long incrementInternal(final Object key, final long delta, final Long initialValue) {
        lock(key);
        Object value = get(key);
        if (value == null) {
            if (initialValue == null) {
                return null;
            }
            value = initialValue;
        }

        long newValue = castToLong(value) + delta;
        if (newValue < 0) {
            newValue = 0;
        }

        if (value instanceof String) {
            put(key, String.valueOf(newValue));
        } else if (value instanceof Byte) {
            put(key, ((Number)newValue).byteValue());
        } else if (value instanceof Short) {
            put(key, ((Number)newValue).shortValue());
        } else if (value instanceof Integer) {
            put(key, ((Number)newValue).intValue());
        } else if (value instanceof Long) {
            put(key, ((Number)newValue).longValue());
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
        }
        return newValue;
    }

    public Long increment(Object key, long delta) {
        return increment(key, delta, null);
    }

    public Long increment(final Object key, final long delta, final Long initialValue) {
        final Callable<Long> callable = new Callable<Long>() {
            public Long call() throws Exception {
                return incrementInternal(key, delta, initialValue);
            }
        };
        return new WrapperTxCallable<NamespacedKey, Object, Long>(cache, callable).call();
    }

    public <T> Map<T, Long> incrementAll(Collection<T> keys, long delta) {
        return incrementAll(keys, delta, null);
    }

    public <T> Map<T, Long> incrementAll(final Collection<T> keys, final long delta, final Long initialValue) {
        final Callable<Map<T, Long>> callable = new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                Map<T, Long> map = new HashMap<T, Long>();
                for (T key : keys) {
                    Long newValue = incrementInternal(key, delta, initialValue);
                    map.put(key, newValue);
                }
                return map;
            }
        };
        return new WrapperTxCallable<NamespacedKey, Object, Map<T, Long>>(cache, callable).call();
    }

    public <T> Map<T, Long> incrementAll(Map<T, Long> offsets) {
        return incrementAll(offsets, null);
    }

    public <T> Map<T, Long> incrementAll(final Map<T, Long> offsets, final Long initialValue) {
        final Callable<Map<T, Long>> callable = new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                Map<T, Long> map = new HashMap<T, Long>();
                for (Map.Entry<T, Long> entry : offsets.entrySet()) {
                    T key = entry.getKey();
                    Long delta = entry.getValue();
                    Long newValue = incrementInternal(key, delta, initialValue);
                    map.put(key, newValue);
                }
                return map;
            }
        };
        return new WrapperTxCallable<NamespacedKey, Object, Map<T, Long>>(cache, callable).call();
    }

    public void clearAll() {
        cache.clear();
    }


    public Stats getStatistics() {
        return new InfinispanStatistics(cache.getAdvancedCache());
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    private long toLifespanMillis(Expiration expiration) {
        if (expiration == null) {
            return -1;
        } else {
            return expiration.getMillisecondsValue() - System.currentTimeMillis();
        }
    }

    private static class MyIdentifiableValue implements IdentifiableValue {
        private final Object value;

        public MyIdentifiableValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    private NamespacedKey namespacedKey(Object key) {
        String namespace = getNamespace() == null ? NamespaceManager.get() : getNamespace();
        return new NamespacedKey(namespace, key);
    }

    private <T> Map<? extends NamespacedKey, ?> toNamespacedMap(Map<T, ?> map) {
        HashMap<NamespacedKey, Object> namespacedKeyMap = new HashMap<NamespacedKey, Object>();
        for (Map.Entry<T, ?> entry : map.entrySet()) {
            namespacedKeyMap.put(namespacedKey(entry.getKey()), entry.getValue());
        }
        return namespacedKeyMap;
    }
}
