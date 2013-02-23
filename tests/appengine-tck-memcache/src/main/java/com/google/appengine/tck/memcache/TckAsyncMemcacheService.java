/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.memcache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.ErrorHandler;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.Stats;
import org.jboss.capedwarf.common.async.Wrappers;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TckAsyncMemcacheService implements AsyncMemcacheService {
    private final MemcacheService delegate;

    public TckAsyncMemcacheService() {
        delegate = new TckMemcacheService();
    }

    public TckAsyncMemcacheService(String namespace) {
        delegate = new TckMemcacheService(namespace);
    }

    protected <T> Future<T> wrap(Callable<T> callable) {
        return Wrappers.future(callable);
    }
    
    public Future<Object> get(final Object o) {
        return wrap(new Callable<Object>() {
            public Object call() throws Exception {
                return delegate.get(o);
            }
        });
    }

    public Future<MemcacheService.IdentifiableValue> getIdentifiable(final Object o) {
        return wrap(new Callable<MemcacheService.IdentifiableValue>() {
            public MemcacheService.IdentifiableValue call() throws Exception {
                return delegate.getIdentifiable(o);
            }
        });
    }

    public <T> Future<Map<T, MemcacheService.IdentifiableValue>> getIdentifiables(final Collection<T> ts) {
        return wrap(new Callable<Map<T, MemcacheService.IdentifiableValue>>() {
            public Map<T, MemcacheService.IdentifiableValue> call() throws Exception {
                return delegate.getIdentifiables(ts);
            }
        });
    }

    public Future<Boolean> contains(final Object o) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.contains(o);
            }
        });
    }

    public <T> Future<Map<T, Object>> getAll(final Collection<T> ts) {
        return wrap(new Callable<Map<T, Object>>() {
            public Map<T, Object> call() throws Exception {
                return delegate.getAll(ts);
            }
        });
    }

    public Future<Boolean> put(final Object o, final Object o2, final Expiration expiration, final MemcacheService.SetPolicy setPolicy) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.put(o, o2, expiration, setPolicy);
            }
        });
    }

    public Future<Void> put(final Object o, final Object o2, final Expiration expiration) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delegate.put(o, o2, expiration);
                return null;
            }
        });
    }

    public Future<Void> put(final Object o, final Object o2) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delegate.put(o, o2);
                return null;
            }
        });
    }

    public <T> Future<Set<T>> putAll(final Map<T, ?> tMap, final Expiration expiration, final MemcacheService.SetPolicy setPolicy) {
        return wrap(new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return delegate.putAll(tMap, expiration, setPolicy);
            }
        });
    }

    public Future<Void> putAll(final Map<?, ?> map, final Expiration expiration) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delegate.putAll(map, expiration);
                return null;
            }
        });
    }

    public Future<Void> putAll(final Map<?, ?> map) {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delegate.putAll(map);
                return null;
            }
        });
    }

    public Future<Boolean> putIfUntouched(final Object o, final MemcacheService.IdentifiableValue identifiableValue, final Object o2, final Expiration expiration) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.putIfUntouched(o, identifiableValue, o2, expiration);
            }
        });
    }

    public Future<Boolean> putIfUntouched(final Object o, final MemcacheService.IdentifiableValue identifiableValue, final Object o2) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.putIfUntouched(o, identifiableValue, o2);
            }
        });
    }

    public <T> Future<Set<T>> putIfUntouched(final Map<T, MemcacheService.CasValues> tCasValuesMap) {
        return wrap(new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return delegate.putIfUntouched(tCasValuesMap);
            }
        });
    }

    public <T> Future<Set<T>> putIfUntouched(final Map<T, MemcacheService.CasValues> tCasValuesMap, final Expiration expiration) {
        return wrap(new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return delegate.putIfUntouched(tCasValuesMap, expiration);
            }
        });
    }

    public Future<Boolean> delete(final Object o) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.delete(o);
            }
        });
    }

    public Future<Boolean> delete(final Object o, final long l) {
        return wrap(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.delete(o, l);
            }
        });
    }

    public <T> Future<Set<T>> deleteAll(final Collection<T> ts) {
        return wrap(new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return delegate.deleteAll(ts);
            }
        });
    }

    public <T> Future<Set<T>> deleteAll(final Collection<T> ts, final long l) {
        return wrap(new Callable<Set<T>>() {
            public Set<T> call() throws Exception {
                return delegate.deleteAll(ts, l);
            }
        });
    }

    public Future<Long> increment(final Object o, final long l) {
        return wrap(new Callable<Long>() {
            public Long call() throws Exception {
                return delegate.increment(o, l);
            }
        });
    }

    public Future<Long> increment(final Object o, final long l, final Long aLong) {
        return wrap(new Callable<Long>() {
            public Long call() throws Exception {
                return delegate.increment(o, l, aLong);
            }
        });
    }

    public <T> Future<Map<T, Long>> incrementAll(final Collection<T> ts, final long l) {
        return wrap(new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                return delegate.incrementAll(ts, l);
            }
        });
    }

    public <T> Future<Map<T, Long>> incrementAll(final Collection<T> ts, final long l, final Long aLong) {
        return wrap(new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                return delegate.incrementAll(ts, l, aLong);
            }
        });
    }

    public <T> Future<Map<T, Long>> incrementAll(final Map<T, Long> tLongMap) {
        return wrap(new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                return delegate.incrementAll(tLongMap);
            }
        });
    }

    public <T> Future<Map<T, Long>> incrementAll(final Map<T, Long> tLongMap, final Long aLong) {
        return wrap(new Callable<Map<T, Long>>() {
            public Map<T, Long> call() throws Exception {
                return delegate.incrementAll(tLongMap, aLong);
            }
        });
    }

    public Future<Void> clearAll() {
        return wrap(new Callable<Void>() {
            public Void call() throws Exception {
                delegate.clearAll();
                return null;
            }
        });
    }

    public Future<Stats> getStatistics() {
        return wrap(new Callable<Stats>() {
            public Stats call() throws Exception {
                return delegate.getStatistics();
            }
        });
    }

    public String getNamespace() {
        return delegate.getNamespace();
    }

    @SuppressWarnings("deprecation")
    public ErrorHandler getErrorHandler() {
      return delegate.getErrorHandler();
    }

    @SuppressWarnings("deprecation")
    public void setErrorHandler(final ErrorHandler errorHandler) {
        delegate.setErrorHandler(errorHandler);
    }
}
