package com.google.appengine.tck.datastore;

import java.util.Arrays;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryFilteringByStringPropertyTypeTest extends QueryTestBase {

    @Test
    public void queryByEqualReturnsEntityWithEqualPropertyValue() throws Exception {
        testEqualityQueries("foo", "bar");
        testInequalityQueries("aaa", "bbb", "ccc");
    }

    @Test
    public void queryDoesNotReturnResultIfFilterIsSubstringOfProperty() throws Exception {
        storeTestEntityWithSingleProperty("John Doe");
        Query query = createQuery(EQUAL, "John");
        assertNoResults(query);
    }

    @Test
    public void testQueryByIn() throws Exception {
        Entity john = storeTestEntityWithSingleProperty("John");
        Entity kate = storeTestEntityWithSingleProperty("Kate");
        Entity ashley = storeTestEntityWithSingleProperty("Ashley");

        assertSet(whenFilteringBy(IN, Arrays.asList("Kate", "Ashley")), queryReturns(kate, ashley));
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatement() throws Exception {
        Entity a = storeTestEntityWithSingleProperty("a");
        Entity b = storeTestEntityWithSingleProperty("b");
        Entity c = storeTestEntityWithSingleProperty("c");
        Entity n = storeTestEntityWithSingleProperty(null);

        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("a", "b")), containsResultsInOrder(a, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", "a")), containsResultsInOrder(b, a));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("c", "a", "b")), containsResultsInOrder(c, a, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", "c", "c", "b")), containsResultsInOrder(b, c));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList(null, "b")), containsResultsInOrder(n, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", null)), containsResultsInOrder(b, n));
    }

}
