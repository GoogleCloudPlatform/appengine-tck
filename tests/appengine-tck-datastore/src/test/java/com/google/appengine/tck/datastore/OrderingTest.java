package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(Arquillian.class)
public class OrderingTest extends QueryTestBase {

    private List<Set<?>> values;
    private List<Set<Entity>> entities;

    @Before
    public void setUp() {
        super.setUp();

        values = asList(
            asSet((Object)null),
            asSet((short)-10, -10, -10L),
            asSet((short)10, 10, 10L, new Rating(10)),
            asSet((short)20, 20, 20L, new Rating(20)),
            asSet(createDate(2013, 1, 1)),
            asSet(createDate(2013, 5, 5)),
            asSet(1381363199999999L),   // 1 microsecond before 2013-10-10
            asSet(new Date(1381363200000L), 1381363200000000L), // 2013-10-10
            asSet(1381363200000001L),   // 1 microsecond after 2013-10-10
            asSet(false),
            asSet(true),
            asSet(
                "sip sip",
                new ShortBlob("sip sip".getBytes()),
                new PostalAddress("sip sip"),
                new PhoneNumber("sip sip"),
                new Email("sip sip"),
                new IMHandle(IMHandle.Scheme.sip, "sip"),   // this is stored as "sip sip"
                new Link("sip sip"),
                new Category("sip sip"),
                new BlobKey("sip sip")
            ),
            asSet(
                "xmpp xmpp",
                new ShortBlob("xmpp xmpp".getBytes()),
                new PostalAddress("xmpp xmpp"),
                new PhoneNumber("xmpp xmpp"),
                new Email("xmpp xmpp"),
                new IMHandle(IMHandle.Scheme.xmpp, "xmpp"), // this is stored as "xmpp xmpp"
                new Link("xmpp xmpp"),
                new Category("xmpp xmpp"),
                new BlobKey("xmpp xmpp")
            ),
            asSet(-10f, -10d),
            asSet(10f, 10d),
            asSet(20f, 20d),
            asSet(new GeoPt(10f, 10f)),
            asSet(new GeoPt(20f, 20f)),
            asSet(new User("aaa", "aaa"), new User("aaa", "otherAuthDomain")),  // ordering must depend only on the email
            asSet(new User("bbb", "bbb")),
            asSet(KeyFactory.createKey("kind", "aaa")),
            asSet(KeyFactory.createKey("kind", "bbb"))
        );

        entities = new ArrayList<Set<Entity>>();
        for (Set<?> values2 : values) {
            Set<Entity> entities2 = new HashSet<Entity>();
            entities.add(entities2);
            for (Object value : values2) {
                entities2.add(storeTestEntityWithSingleProperty(value));
            }
        }
    }

    private Set<?> asSet(Object... values) {
        return new HashSet<Object>(Arrays.asList(values));
    }

    @After
    @Override
    public void tearDown() {
        for (Set<Entity> entities2 : entities) {
            for (Entity entity : entities2) {
                service.delete(entity.getKey());
            }
        }
        super.tearDown();
    }

    @Test
    public void testFiltering() {
        for (int i = 0; i < values.size(); i++) {
            Set<?> values2 = values.get(i);
            for (Object value : values2) {
                assertThat("when filtering with = " + value, whenFilteringBy(EQUAL, value), queryReturns(entities.get(i).toArray(new Entity[0])));
                assertThat("when filtering with <= " + value, whenFilteringBy(LESS_THAN_OR_EQUAL, value), queryReturns(flatten(entities.subList(0, i + 1))));
                assertThat("when filtering with < " + value, whenFilteringBy(LESS_THAN, value), queryReturns(flatten(entities.subList(0, i))));
                assertThat("when filtering with >= " + value, whenFilteringBy(GREATER_THAN_OR_EQUAL, value), queryReturns(flatten(entities.subList(i, entities.size()))));
                assertThat("when filtering with > " + value, whenFilteringBy(GREATER_THAN, value), queryReturns(flatten(entities.subList(i + 1, entities.size()))));
            }
        }
    }

    @Test
    public void testSorting() {
        Query query = createQuery().addSort(SINGLE_PROPERTY_NAME);
        List<Entity> results = service.prepare(query).asList(withDefaults());

        Iterator<Entity> resultIterator = results.iterator();
        Set<Entity> currentResults = new HashSet<Entity>();
        for (Set<Entity> entities2 : entities) {
            for (int i=0; i<entities2.size(); i++) {
                assertTrue(resultIterator.hasNext());
                currentResults.add(resultIterator.next());
            }

            assertEquals(entities2, currentResults);
            currentResults.clear();
        }

        assertFalse(resultIterator.hasNext());
    }

    private Entity[] flatten(List<Set<Entity>> entities) {
        List<Entity> list = new ArrayList<Entity>();
        for (Set<Entity> entities2 : entities) {
            list.addAll(entities2);
        }
        return list.toArray(new Entity[list.size()]);
    }

}
