package com.google.appengine.tck.datastore;

import com.google.appengine.tck.base.TestBase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 */
public abstract class DatastoreTestBase extends TestBase {

  @Deployment
  public static WebArchive getDeployment() {
    WebArchive war = getTckDeployment();
    war.addClasses(DatastoreTestBase.class, AbstractDatastoreTest.class);

//    war.addClasses(QueryTest.class, AsyncServiceTest.class, AncestorTest.class, DatastoreTest.class, SchemaTest.class, DistinctTest.class,  KeyTest.class,  EntityTest.class,  ListTest.class,  StringDataTest.class,  EncodedEntityTest.class,  GeoPtDataTest.class,  NamespaceTest.class,  TransactionTest.class,  IndexTest.class,  BatchTest.class,  IndexQueryTest.class,  DatastoreTestBase.class,  CursorTest.class,  AllocateIdTest.class,  DatetimeDataTest.class,  AbstractDatastoreTest.class,  NumberDataTest.class,  StatsTest.class);
    war.addAsWebInfResource("datastore-indexes.xml");

    return war;
  }
}

