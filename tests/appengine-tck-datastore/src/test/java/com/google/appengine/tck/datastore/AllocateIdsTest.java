/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class AllocateIdsTest extends SimpleTestBase {

    private static final String ALLOCATE_IDS_ENTITY = "AllocateIdsEntity";

    @Test
    public void testAllocateId() throws Exception {
        KeyRange firstBlock = service.allocateIds(ALLOCATE_IDS_ENTITY, 10L);

        Assert.assertNotNull(firstBlock);
        Assert.assertEquals(10, firstBlock.getEnd().getId() - firstBlock.getStart().getId() + 1);
        Assert.assertEquals(10, firstBlock.getSize());

        KeyRange secondBlock = service.allocateIds(ALLOCATE_IDS_ENTITY, 10L);
        Assert.assertNotNull(secondBlock);
        Assert.assertFalse("Allocated key ranges should not overlap.", rangeOverlap(firstBlock, secondBlock));
    }

    @Test
    public void testCheckKeyRange() throws Exception {
        long initialValue = getInitialValue("OtherKind");

        KeyRange kr1 = new KeyRange(null, "OtherKind", 1 + initialValue, 5 + initialValue);
        DatastoreService.KeyRangeState state1 = service.allocateIdRange(kr1);
        Assert.assertNotNull(state1);
        // imo, it could be either -- depending on the impl
        Assert.assertTrue(DatastoreService.KeyRangeState.CONTENTION == state1 || DatastoreService.KeyRangeState.EMPTY == state1);

        KeyRange kr2 = service.allocateIds("OtherKind", 6);
        Assert.assertNotNull(kr2);

        KeyRange kr3 = new KeyRange(null, "OtherKind", 2 + initialValue, 5 + initialValue);
        DatastoreService.KeyRangeState state2 = service.allocateIdRange(kr3);
        Assert.assertNotNull(state2);
        // can it be both, depending on the impl?
        Assert.assertTrue(DatastoreService.KeyRangeState.COLLISION == state2 || DatastoreService.KeyRangeState.CONTENTION == state2);
    }

    private long getInitialValue(String kind) {
        return service.allocateIds(kind, 1L).getStart().getId();
    }

    @Test
    public void testAllocateChild() {
        Entity parent = new Entity(ALLOCATE_IDS_ENTITY);
        parent.setProperty("name", "parent-" + System.currentTimeMillis());
        Key parentKey = service.put(parent);

        int allocateSize = 10;
        KeyRange range = service.allocateIds(parentKey, ALLOCATE_IDS_ENTITY, allocateSize);

        Entity child = new Entity(range.getStart());
        Key key = service.put(child);

        // child with allocated key should have correct parent.
        Assert.assertEquals(parentKey, key.getParent());
    }

    private boolean rangeOverlap(KeyRange kr1, KeyRange kr2) {
        long firstStart = kr1.getStart().getId();
        long firstEnd = kr1.getEnd().getId();
        long secondStart = kr2.getStart().getId();
        long secondEnd = kr2.getStart().getId();

        if ((firstStart == secondStart) || (firstEnd == secondEnd)) {
            return true;
        }
        if ((firstStart == secondEnd) || (firstEnd == secondStart)) {
            return true;
        }
        if ((firstStart < secondStart) && (firstEnd > secondStart)) {
            return true;
        }
        if ((firstStart > secondStart) && (secondEnd > firstStart)) {
            return true;
        }
        return false;
    }
}
