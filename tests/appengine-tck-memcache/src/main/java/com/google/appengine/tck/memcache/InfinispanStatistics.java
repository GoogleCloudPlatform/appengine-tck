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

import com.google.appengine.api.memcache.Stats;
import org.infinispan.AdvancedCache;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class InfinispanStatistics implements Stats {
    private AdvancedCache<?, ?> advancedCache;

    public InfinispanStatistics(AdvancedCache<?, ?> cache) {
        this.advancedCache = cache;
    }

    public long getHitCount() {
        return advancedCache.getStats().getHits();
    }

    public long getMissCount() {
        return advancedCache.getStats().getMisses();
    }

    public long getBytesReturnedForHits() {
        return 0; // TODO
    }

    public long getItemCount() {
        return advancedCache.getStats().getTotalNumberOfEntries();
    }

    public long getTotalItemBytes() {
        return 0; // TODO
    }

    public int getMaxTimeWithoutAccess() {
        return 0; // TODO
    }
}
