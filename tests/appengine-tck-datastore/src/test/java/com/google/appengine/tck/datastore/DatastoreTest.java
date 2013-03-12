

package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.utils.SystemProperty;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author lyan@google.com (Li Yan)
 */
@RunWith(Arquillian.class)
public class DatastoreTest extends DatastoreTestBase {

  /**
   * test ancestor query in transaction.
   */
  @Test
  public void testAncestorQueryInTxn() throws Exception {
    Entity parentA = new Entity("parentA");
    Entity parentB = new Entity("parentB");
    Entity childA = new Entity("child", parentA.getKey());
    Entity childB = new Entity("child", parentB.getKey());

    datastoreService.beginTransaction();
    datastoreService.put(parentA);
    datastoreService.getCurrentTransaction().commit();

    datastoreService.beginTransaction();
    datastoreService.put(parentB);
    datastoreService.getCurrentTransaction().commit();

    datastoreService.beginTransaction();
    datastoreService.put(childA);
    datastoreService.getCurrentTransaction().commit();

    datastoreService.beginTransaction();
    datastoreService.put(childB);
    datastoreService.getCurrentTransaction().commit();
    // query on ancestor, only childA should be returned
    datastoreService.beginTransaction();
    Query query = new Query("child", parentA.getKey());
    Transaction tx = datastoreService.getCurrentTransaction();
    int numRows = datastoreService.prepare(tx, query)
                                  .countEntities(FetchOptions.Builder.withDefaults());
    tx.commit();
    assertEquals(1, numRows);
    datastoreService.beginTransaction();
    tx = datastoreService.getCurrentTransaction();
    Entity result = datastoreService.prepare(tx, query).asSingleEntity();
    assertEquals(childA.getKey(), result.getKey());
    tx.commit();
  }
  
  /**
   * test datastore type.
   * In corp., 'High Replication' option is not available. So no appid starts with "s~" there now.
   * - Noted at 2/15/2011
   */
  @Test
  public void testDatastoreType() {
    String appId = SystemProperty.applicationId.get();
    DatastoreAttributes.DatastoreType dsTypte = datastoreService.getDatastoreAttributes()
                                                                .getDatastoreType();
    if (appId.startsWith("s~")) {
      assertEquals(DatastoreAttributes.DatastoreType.HIGH_REPLICATION, dsTypte);
    } else {
      assertEquals(DatastoreAttributes.DatastoreType.MASTER_SLAVE, dsTypte);
    }

  }
}
