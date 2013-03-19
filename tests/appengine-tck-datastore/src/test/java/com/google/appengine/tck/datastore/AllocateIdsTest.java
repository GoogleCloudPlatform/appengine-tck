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

    @Test
    public void testAllocateId() throws Exception {
        long initialValue = getInitialValue("SomeKind");

        KeyRange keys = service.allocateIds("SomeKind", 10L);
        Assert.assertNotNull(keys);

        Key start = keys.getStart();
        Assert.assertNotNull(start);
        Assert.assertEquals(1 + initialValue, start.getId());

        Key end = keys.getEnd();
        Assert.assertNotNull(end);
        Assert.assertEquals(10 + initialValue, end.getId());
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
}
