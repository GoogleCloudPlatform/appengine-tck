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

package com.google.appengine.tck.datastore;

import java.util.List;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static org.junit.Assert.assertEquals;

/**
 * Tests if all Google AppEngine types can be stored as properties of Entity and if queries filtered on those
 * properties behave correctly for all filter operators (EQUAL, NOT_EQUAL, GREATER_THAN, ...)
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QueryFilteringByGAEPropertyTypesTest extends QueryTestBase {

    @Test
    public void testFilterByEntityKey() {
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, "testFilterByEntityKey");
        Key parentKey = parentEntity.getKey();

        Key fooKey = KeyFactory.createKey(parentKey, "foo", 1);
        Entity fooEntity = new Entity(fooKey);
        service.put(fooEntity);

        Query query = new Query("foo")
                .setAncestor(parentKey)
                .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, EQUAL, fooKey));

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        assertEquals(1, results.size());
        assertEquals(fooEntity, results.get(0));
    }

    @Test
    public void testEntityKeyInequalityFilter() {
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, "testFilterByInequalityFilter");
        Key parentKey = parentEntity.getKey();

        Entity entity1 = new Entity("foo", parentKey);
        service.put(entity1);

        Entity entity2 = new Entity("foo", parentKey);
        service.put(entity2);

        Query query = new Query("foo")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, GREATER_THAN, entity1.getKey()));
        List<Entity> list = service.prepare(query).asList(FetchOptions.Builder.withDefaults());
        assertEquals(1, list.size());
        assertEquals(entity2.getKey(), list.get(0).getKey());
    }

    @Test
    public void testPhoneNumberProperty() {
        testEqualityQueries(new PhoneNumber("foo"), new PhoneNumber("bar"));
        testInequalityQueries(new PhoneNumber("111"), new PhoneNumber("222"), new PhoneNumber("333"));
    }

    @Test
    public void testPostalAddressProperty() {
        testEqualityQueries(new PostalAddress("foo"), new PostalAddress("bar"));
        testInequalityQueries(new PostalAddress("aaa"), new PostalAddress("bbb"), new PostalAddress("ccc"));
    }

    @Test
    public void testEmailProperty() {
        testEqualityQueries(new PostalAddress("foo@foo.com"), new PostalAddress("bar@bar.com"));
        testInequalityQueries(new PostalAddress("aaa@foo.com"), new PostalAddress("bbb@foo.com"), new PostalAddress("ccc@foo.com"));
    }

    @Test
    public void testUserProperty() {
        testEqualityQueries(new User("foo@foo.com", "authDomain", "userId", "federatedIdentity"), new User("bar@bar.com", "authDomain", "userId", "federatedIdentity"));
        testInequalityQueries(new User("aaa@foo.com", "authDomain"), new User("bbb@foo.com", "authDomain"), new User("ccc@foo.com", "authDomain"));
    }

    @Test
    public void testLinkProperty() {
        testEqualityQueries(new Link("http://foo.com"), new Link("http://bar.com"));
        testInequalityQueries(new Link("http://aaa.com"), new Link("http://bbb.com"), new Link("http://ccc.com"));
    }

    @Test
    public void testKeyProperty() {
        testEqualityQueries(KeyFactory.createKey("foo", "foo"), KeyFactory.createKey("bar", "bar"));
    }

    @Test
    public void testRatingProperty() {
        testEqualityQueries(new Rating(1), new Rating(2));
        testInequalityQueries(new Rating(1), new Rating(2), new Rating(3));
    }

    @Test
    public void testGeoPtProperty() {
        testEqualityQueries(new GeoPt(45f, 15f), new GeoPt(50f, 20f));
        testInequalityQueries(new GeoPt(20f, 10f), new GeoPt(30f, 10f), new GeoPt(40f, 10f));
        testInequalityQueries(new GeoPt(0f, 10f), new GeoPt(0f, 20f), new GeoPt(0f, 30f));
    }

    @Test
    public void testCategoryProperty() {
        testEqualityQueries(new Category("foo"), new Category("bar"));
        testInequalityQueries(new Category("aaa"), new Category("bbb"), new Category("ccc"));
    }

    @Test
    public void testIMHandleProperty() {
        testEqualityQueries(new IMHandle(IMHandle.Scheme.xmpp, "foo@foo.com"), new IMHandle(IMHandle.Scheme.xmpp, "bar@bar.com"));
    }

    @Test
    public void testShortBlobProperty() {
        testEqualityQueries(new ShortBlob("foo".getBytes()), new ShortBlob("bar".getBytes()));
    }

    @Test
    public void testBlobKeyProperty() {
        testEqualityQueries(new BlobKey("foo"), new BlobKey("bar"));
        testInequalityQueries(new BlobKey("aaa"), new BlobKey("bbb"), new BlobKey("ccc"));
    }

}
