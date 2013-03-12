package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * embedded entity property test
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class EncodedEntityTest extends AbstractDatastoreTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  private String kindName = "entityproperty";

  
  @Test
  public void testEmbeddedEntity() throws InterruptedException {
    clearData(kindName);

    List<Entity> elist = new ArrayList<Entity>();
    Entity newRec = new Entity(kindName, rootKey);
    newRec.setProperty("entityDat", null);
    newRec.setProperty("stringData", "no entity property");
    newRec.setProperty("timestamp", new Date());
    elist.add(newRec);

    // Notice, no kind, or key is required
    EmbeddedEntity newRec0 = new EmbeddedEntity();
    newRec0.setProperty("stringData", "check6009464");
    newRec0.setProperty("timestamp", new Date());

    newRec = new Entity(kindName, rootKey);
    newRec.setProperty("entityDat", newRec0);
    newRec.setProperty("stringData", "have entity property");
    newRec.setProperty("timestamp", new Date());
    elist.add(newRec);
    datastoreService.put(elist);
    Thread.sleep(waitTime);

    Query q = new Query(kindName, rootKey);
    int count = datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
    assertEquals(2, count);
    for (Entity readRec : datastoreService.prepare(q).asIterable()) {
      EmbeddedEntity ee2 = (EmbeddedEntity) readRec.getProperty("entityDat");
      if (ee2 != null) {
        assertEquals("have entity property", readRec.getProperty("stringData"));
        assertEquals(readRec.getProperty("entityDat").getClass(), EmbeddedEntity.class);
        Entity e = new Entity(ee2.getKey());
        e.setPropertiesFrom(ee2);
        assertEquals("check6009464", e.getProperty("stringData"));
      } else {
        assertEquals("no entity property", readRec.getProperty("stringData"));
      }
    }
  }
}
