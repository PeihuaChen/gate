/*
 *  TestPersist.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/01
 *
 *  $Id$
 */

package gate.persist;

import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;

/** Persistence test class
  */
public class TestPersist extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestPersist(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
  } // tearDown

  /** Test resource save and restore */
  public void testSaveRestore() throws Exception {
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();
    SerialDataStore sds = new SerialDataStore(storageDir.toURL());
    sds.create();
    sds.open();

    // create a document
    String server = TestDocument.getTestServerName();
    assertNotNull(server);
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    assertNotNull(doc);
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(0), new Long(20), "thingymajig", Factory.newFeatureMap()
    );

    // check that we can't save a resource without adopting it
    boolean cannotSync = false;
    try { sds.sync(doc); } catch(PersistenceException e) { cannotSync=true; }
    if(! cannotSync) assert("doc synced ok before adoption", false);

    // check that we can't adopt a resource that's stored somewhere else
    doc.setDataStore(new SerialDataStore(new File("z:\\").toURL()));
    try { sds.adopt(doc); } catch(PersistenceException e) { cannotSync=true; }
    if(! cannotSync)
      assert("doc adopted but in other datastore already", false);
    doc.setDataStore(null);

    // save the document
    doc = (Document) sds.adopt(doc);
    sds.sync(doc);
    String lrPersistenceId =
      (String) doc.getFeatures().get("DataStoreInstanceId");

    // test the getLrTypes method
    List lrTypes = sds.getLrTypes();
    assert("wrong number of types in SDS", lrTypes.size() == 1);
    assert(
      "wrong type LR in SDS",
      lrTypes.get(0).equals("gate.corpora.DocumentImpl")
    );

    // test the getLrNames method
    Iterator iter = sds.getLrNames("gate.corpora.DocumentImpl").iterator();
    String name = (String) iter.next();
    assertEquals(name, "GATE document");

    // read the document back
    FeatureMap features = Factory.newFeatureMap();
    features.put("DataStoreInstanceId", lrPersistenceId);
    features.put("DataStore", sds);
    Document doc2 =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", features);
    Document doc3 =
      (Document) sds.getLr("gate.corpora.DocumentImpl", lrPersistenceId);
    assert(doc3.equals(doc2));
    assert(doc.equals(doc2));

    // delete the datastore
    sds.delete();
  } // testSaveRestore()

  /** Simple test */
  public void testSimple() throws Exception {
    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();

    // create and open a serial data store
    DataStore sds = Factory.createDataStore(
      "gate.persist.SerialDataStore", storageDir.toURL()
    );

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // save the document
    doc = (Document) sds.adopt(doc);
    sds.sync(doc);

    // remember the persistence ID for reading back
    // (in the normal case these ids are obtained by DataStore.getLrIds(type))
    String lrPersistenceId =
      (String) doc.getFeatures().get("DataStoreInstanceId");

    // read the document back
    FeatureMap features = Factory.newFeatureMap();
    features.put("DataStoreInstanceId", lrPersistenceId);
    features.put("DataStore", sds);
    Document doc2 =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", features);

    // check that the version we read back matches the original
    assert(doc.equals(doc2));

    // delete the datastore
    sds.delete();
  } // testSimple()

  /** Test multiple LRs */
  public void testMultipleLrs() throws Exception {
    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();

    // create and open a serial data store
    SerialDataStore sds = new SerialDataStore(storageDir.toURL());
    sds.create();
    sds.open();

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // create another document with some annotations / features on it
    Document doc2 = Factory.newDocument(new URL(server + "tests/doc1.html"));
    doc.getFeatures().put("hi there again", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "dog poo irritates", Factory.newFeatureMap()
    );

    // save the documents
    doc = (Document) sds.adopt(doc);
    doc2 = (Document) sds.adopt(doc2);
    sds.sync(doc);
    sds.sync(doc2);

    // create a corpus with the documents
    Corpus corp = Factory.newCorpus("Hamish test corpus");
    corp.add(doc);
    corp.add(doc2);
    sds.adopt(corp);
    sds.sync(corp);

    // read the documents back
    ArrayList lrsFromDisk = new ArrayList();
    List types = sds.getLrTypes();
    Iterator typesIter = types.iterator();
    while(typesIter.hasNext()) {
      String typeName = (String) typesIter.next();
      List lrIds = sds.getLrIds(typeName);

      Iterator idsIter = lrIds.iterator();
      while(idsIter.hasNext()) {
        String lrId = (String) idsIter.next();
        FeatureMap features = Factory.newFeatureMap();
        features.put("DataStore", sds);
        features.put("DataStoreInstanceId", lrId);
        Resource lr = Factory.createResource(typeName, features);
        if(lrId.startsWith("GATE cor")) // ensure ordering regardless of OS
          lrsFromDisk.add(0, lr);
        else
          lrsFromDisk.add(lr);
      } // for each LR ID

    } // for each LR type

    // check that the versions we read back match the originals
    Document diskDoc = (Document) lrsFromDisk.get(1);
    Document diskDoc2 = (Document) lrsFromDisk.get(2);
    Corpus diskCorp = (Corpus) lrsFromDisk.get(0);
    assert(doc.equals(diskDoc));
    assert(doc2.equals(diskDoc2));
    assert(corp.equals(diskCorp));
    assert(corp.getName().equals(diskCorp.getName()));

    // delete the datastore
    sds.delete();
  } // testMultipleLrs()

  /** Test LR deletion */
  public void testDelete() throws Exception {
    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();

    // create and open a serial data store
    SerialDataStore sds = new SerialDataStore();
    sds.setStorageUrl(storageDir.toURL());
    sds.create();
    sds.open();

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // save the document
    doc = (Document) sds.adopt(doc);
    sds.sync(doc);

    // remember the persistence ID for reading back
    // (in the normal case these ids are obtained by DataStore.getLrIds(type))
    String lrPersistenceId =
      (String) doc.getFeatures().get("DataStoreInstanceId");

    // delete document back
    sds.delete("gate.corpora.DocumentImpl", lrPersistenceId);

    // check that there are no LRs left in the DS
    assert(sds.getLrIds("gate.corpora.DocumentImpl").size() == 0);

    // delete the datastore
    sds.delete();
  } // testDelete()

  /** Test the DS register. */
  public void testDSR() throws Exception {
    DataStoreRegister dsr = Gate.getDataStoreRegister();
    assert("DSR has wrong number elements (not 0)", dsr.size() == 0);

    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();

    // create and open a serial data store
    DataStore sds = Factory.createDataStore(
      "gate.persist.SerialDataStore", storageDir.toURL()
    );

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // save the document
    doc = (Document) sds.adopt(doc);
    sds.sync(doc);

    // DSR should have one member
    assert("DSR has wrong number elements", dsr.size() == 1);

    // delete the datastore
    sds.delete();
  } // testDSR()


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestPersist.class);
  } // suite

} // class TestPersist
