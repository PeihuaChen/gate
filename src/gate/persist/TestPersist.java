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
import gate.security.*;

/** Persistence test class
  */
public class TestPersist extends TestCase
{
  private static final String JDBC_URL =
//           "jdbc:oracle:thin:GATEUSER/gate@192.168.128.7:1521:GATE04";
//           "jdbc:oracle:oci8:GATEUSER/gate@GATE04.SIRMA.BG";
"jdbc:oracle:thin:GATEUSER/gate2@hope.dcs.shef.ac.uk:1521:GateDB";


  /** Debug flag */
  private static final boolean DEBUG = false;
  private static Long uc01_lrID = null;
  private static LanguageResource uc01_LR = null;

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
    storageDir.delete(); // get rid of the temp file
    storageDir.mkdir(); // create an empty dir of same name

    SerialDataStore sds = new SerialDataStore(storageDir.toURL().toString());
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
    doc.setDataStore(new SerialDataStore(new File("z:\\").toURL().toString()));
    try { sds.adopt(doc,null); } catch(PersistenceException e) { cannotSync=true; }
    if(! cannotSync)
      assert("doc adopted but in other datastore already", false);
    doc.setDataStore(null);
    doc.setName("Alicia Tonbridge, a Document");

    // save the document
    Document persDoc = (Document) sds.adopt(doc,null);
    sds.sync(persDoc);
    Object lrPersistenceId = persDoc.getLRPersistenceId();

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
    assertEquals(name, "Alicia Tonbridge, a Document");

    // read the document back
    FeatureMap features = Factory.newFeatureMap();
    features.put(DataStore.LR_ID_FEATURE_NAME, lrPersistenceId);
    features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
    Document doc2 =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", features);
    Document doc3 =
      (Document) sds.getLr("gate.corpora.DocumentImpl", lrPersistenceId);

    //clear the parameters value from features as they will be different

    assert(doc3.equals(doc2));
    assert(persDoc.equals(doc2));

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
      "gate.persist.SerialDataStore", storageDir.toURL().toString()
    );

    // check we can get empty lists from empty data stores
    List lrTypes = sds.getLrTypes();

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // save the document
    Document persDoc = (Document) sds.adopt(doc,null);
    sds.sync(persDoc);

    // remember the persistence ID for reading back
    // (in the normal case these ids are obtained by DataStore.getLrIds(type))
    Object lrPersistenceId = persDoc.getLRPersistenceId();

    // read the document back
    FeatureMap features = Factory.newFeatureMap();
    features.put(DataStore.LR_ID_FEATURE_NAME, lrPersistenceId);
    features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
    Document doc2 =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", features);

    //parameters should be different
    // check that the version we read back matches the original
    assert(persDoc.equals(doc2));

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
    SerialDataStore sds = new SerialDataStore(storageDir.toURL().toString());
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
    Document doc2 =
      Factory.newDocument(new URL(server + "tests/html/test1.htm"));
    doc.getFeatures().put("hi there again", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "dog poo irritates", Factory.newFeatureMap()
    );

    // create a corpus with the documents
    Corpus corp = Factory.newCorpus("Hamish test corpus");
    corp.add(doc);
    corp.add(doc2);
    LanguageResource persCorpus = sds.adopt(corp,null);
    sds.sync(persCorpus);


    // read the documents back
    ArrayList lrsFromDisk = new ArrayList();
    List lrIds = sds.getLrIds("gate.corpora.SerialCorpusImpl");

    Iterator idsIter = lrIds.iterator();
    while(idsIter.hasNext()) {
      String lrId = (String) idsIter.next();
      FeatureMap features = Factory.newFeatureMap();
      features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
      features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
      Resource lr = Factory.createResource( "gate.corpora.SerialCorpusImpl",
                                            features);
      lrsFromDisk.add(lr);
    } // for each LR ID

    if (DEBUG) System.out.println("LRs on disk" + lrsFromDisk);

    // check that the versions we read back match the originals
    Corpus diskCorp = (Corpus) lrsFromDisk.get(0);

    Document diskDoc = (Document) diskCorp.get(0);

    if (DEBUG) Out.prln("Documents in corpus: " + corp.getDocumentNames());
    assert("corp name != mem name", corp.getName().equals(diskCorp.getName()));
    if (DEBUG) Out.prln("Memory features " + corp.getFeatures());
    if (DEBUG) Out.prln("Disk features " + diskCorp.getFeatures());
    assert("corp feat != mem feat",
           corp.getFeatures().equals(diskCorp.getFeatures()));
    if (DEBUG)
      Out.prln("Annotations in doc: " + diskDoc.getAnnotations());
    assert("doc annotations from disk not equal to memory version",
          doc.getAnnotations().equals(diskDoc.getAnnotations()));
    assert("doc from disk not equal to memory version",
          doc.equals(diskDoc));

    Iterator corpusIter = diskCorp.iterator();
    while(corpusIter.hasNext()){
      if (DEBUG)
        Out.prln(((Document) corpusIter.next()).getName());
      else
        corpusIter.next();
    }


//    assert("doc2 from disk not equal to memory version", doc2.equals(diskDoc2));

    // delete the datastore
    sds.delete();
  } // testMultipleLrs()

  /** Test LR deletion */
  public void testDelete() throws Exception {
    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    if (DEBUG) Out.prln("Corpus stored to: " + storageDir.getAbsolutePath());
    storageDir.delete();

    // create and open a serial data store
    SerialDataStore sds = new SerialDataStore();
    sds.setStorageUrl(storageDir.toURL().toString());
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
    Document persDoc = (Document) sds.adopt(doc,null);
    sds.sync(persDoc);

    // remember the persistence ID for reading back
    // (in the normal case these ids are obtained by DataStore.getLrIds(type))
    Object lrPersistenceId = persDoc.getLRPersistenceId();

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
    assert("DSR has wrong number elements (not 0): " + dsr.size(),
           dsr.size() == 0);

    // create a temporary directory; because File.createTempFile actually
    // writes the bloody thing, we need to delete it from disk before calling
    // DataStore.create
    File storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();

    // create and open a serial data store
    DataStore sds = Factory.createDataStore(
      "gate.persist.SerialDataStore", storageDir.toURL().toString()
    );

    // create a document with some annotations / features on it
    String server = TestDocument.getTestServerName();
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(5), new Long(25), "ThingyMaJig", Factory.newFeatureMap()
    );

    // save the document
    Document persDoc = (Document) sds.adopt(doc,null);
    sds.sync(persDoc);

    // DSR should have one member
    assert("DSR has wrong number elements", dsr.size() == 1);

    // create and open another serial data store
    storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();
    DataStore sds2 = Factory.createDataStore(
      "gate.persist.SerialDataStore", storageDir.toURL().toString()
    );

    // DSR should have two members
    assert("DSR has wrong number elements: " + dsr.size(), dsr.size() == 2);

    // peek at the DSR members
    Iterator dsrIter = dsr.iterator();
    while(dsrIter.hasNext()) {
      DataStore ds = (DataStore) dsrIter.next();
      assertNotNull("null ds in ds reg", ds);
      if(DEBUG)
        Out.prln(ds);
    }

    // delete the datastores
    sds.close();
    assert("DSR has wrong number elements: " + dsr.size(), dsr.size() == 1);
    sds.delete();
    assert("DSR has wrong number elements: " + dsr.size(), dsr.size() == 1);
    sds2.delete();
    assert("DSR has wrong number elements: " + dsr.size(), dsr.size() == 0);

  } // testDSR()



  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestPersist.class);
  } // suite


  private Document createTestDocument()
    throws Exception {

    String server = TestDocument.getTestServerName();
    assertNotNull(server);
    Document doc = Factory.newDocument(new URL(server + "tests/doc0.html"));
    assertNotNull(doc);

    doc.getFeatures().put("hi there", new Integer(23232));
    doc.getAnnotations().add(
      new Long(0), new Long(20), "thingymajig", Factory.newFeatureMap()
    );
    doc.setName("DB test Document");

    return doc;
  }


  private Corpus createTestCorpus()
    throws Exception {

    String server = TestDocument.getTestServerName();
    assertNotNull(server);
    Document doc1 = Factory.newDocument(new URL(server + "tests/doc0.html"));
    assertNotNull(doc1);

    doc1.getFeatures().put("hi there", new Integer(23232));
    doc1.getAnnotations().add(
      new Long(0), new Long(20), "thingymajig", Factory.newFeatureMap()
    );
    doc1.setName("DB test Document1");

    // create another document with some annotations / features on it
    Document doc2 =
      Factory.newDocument(new URL(server + "tests/html/test1.htm"));
    doc2.getFeatures().put("hi there again", new Integer(23232));
    doc2.getAnnotations().add(
      new Long(5), new Long(25), "dog poo irritates", Factory.newFeatureMap()
    );
    doc2.setName("DB test Document2");

    //create corpus
    Corpus corp = Factory.newCorpus("My test corpus");
    //add docs
    corp.add(doc1);
    corp.add(doc2);
    //add features
    corp.getFeatures().put("my STRING feature ", new String("string string"));
    corp.getFeatures().put("my BOOL feature ", new Boolean("false"));
    corp.getFeatures().put("my INT feature ", new Integer("1234"));
    corp.getFeatures().put("my LONG feature ", new Long("123456789"));

    return corp;
  }

  /** Test the DS register. */
  public void testDB_UseCase01() throws Exception {

    //descr: create a document in the DB


    //1. open data storage
    DatabaseDataStore ds = new OracleDataStore();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //2. get test document
    Document doc = createTestDocument();
    Assert.assertNotNull(doc);

    //3. get security factory & login
    AccessController ac = new AccessControllerImpl();
    Assert.assertNotNull(ac);
    ac.open(this.JDBC_URL);

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assert(ac.isValidSession(usrSession));

    //4. create security settings for doc
    SecurityInfo si = new SecurityInfo(SecurityInfo.ACCESS_WR_GW,usr,grp);

    //5. try adding doc to data store
    LanguageResource lr = ds.adopt(doc,si);

    Assert.assert(lr instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(lr.getDataStore());
    Assert.assert(lr.getDataStore() instanceof DatabaseDataStore);

    this.uc01_lrID = (Long)lr.getLRPersistenceId();
//    this.uc01_LR = lr;
    this.uc01_LR = doc;
//System.out.println("adopted doc:name=["+((Document)lr).getName()+"], lr_id=["+((Document)lr).getLRPersistenceId()+"]");
    //6.close
    ac.close();
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 01 passed...");
    }
  }


  /** Test the DS register. */
  public void testDB_UseCase02() throws Exception {

    //descr : create a corpus

    //1. open data storage
    DatabaseDataStore ds = new OracleDataStore();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //2. get test document
    Corpus corp = createTestCorpus();
    Assert.assertNotNull(corp);

    //3. get security factory & login
    AccessController ac = new AccessControllerImpl();
    Assert.assertNotNull(ac);
    ac.open(this.JDBC_URL);

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assert(ac.isValidSession(usrSession));

    //4. create security settings for doc
    SecurityInfo si = new SecurityInfo(SecurityInfo.ACCESS_WR_GW,usr,grp);

    //5. try adding doc to data store
    ds.adopt(corp,si);

    //6.close
    ac.close();
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 02 passed...");
    }

  }


  public void testDB_UseCase03() throws Exception {

    //read a document
    //use the one created in UC01
//this.uc01_lrID = new Long(1041);
//System.out.println("docid = " +this.uc01_lrID);
    LanguageResource lr = null;

    //1. open data storage
    DatabaseDataStore ds = new OracleDataStore();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //2. read LR
    lr = ds.getLr(DBHelper.DOCUMENT_CLASS,this.uc01_lrID);

    //3. check name
    String name = lr.getName();
    Assert.assertNotNull(name);
    Assert.assertEquals(name,this.uc01_LR.getName());

    //4. check features
    FeatureMap fm = lr.getFeatures();
    FeatureMap fmOrig = this.uc01_LR.getFeatures();

    Assert.assertNotNull(fm);
    Assert.assertNotNull(fmOrig);
    Assert.assert(fm.size() == fmOrig.size());

    Iterator keys = fm.keySet().iterator();

    while (keys.hasNext()) {
      String currKey = (String)keys.next();
      Assert.assert(fmOrig.containsKey(currKey));
      Assert.assertEquals(fm.get(currKey),fmOrig.get(currKey));
    }

    //6. URL
    Document dbDoc = (Document)lr;
    Assert.assertEquals(dbDoc.getSourceUrl(),((Document)this.uc01_LR).getSourceUrl());

    //5.start/end
    Assert.assertEquals(dbDoc.getSourceUrlStartOffset(),((Document)this.uc01_LR).getSourceUrlStartOffset());
    Assert.assertEquals(dbDoc.getSourceUrlEndOffset(),((Document)this.uc01_LR).getSourceUrlEndOffset());

    //6.markupAware
    Assert.assertEquals(dbDoc.getMarkupAware(),((Document)this.uc01_LR).getMarkupAware());

    //7. content
    DocumentContent cont = dbDoc.getContent();
    Assert.assertEquals(cont,((Document)this.uc01_LR).getContent());

    //7. access the contect again and assure it's not read from the DB twice
    Assert.assertEquals(cont,((Document)this.uc01_LR).getContent());

    //8. encoding
    String encNew = (String)dbDoc.getParameterValue("encoding");
    String encOld = (String)((DocumentImpl)this.uc01_LR).getParameterValue("encoding");
    Assert.assertEquals(encNew,encOld);

    //9. default annotations
    AnnotationSet defaultNew = dbDoc.getAnnotations();
    AnnotationSet defaultOld = ((DocumentImpl)this.uc01_LR).getAnnotations();

    Assert.assertNotNull(defaultNew);
    Assert.assert(defaultNew.size() == defaultOld.size());
    Iterator itDefault = defaultNew.iterator();

    while (itDefault.hasNext()) {
      Annotation currAnn = (Annotation)itDefault.next();
      Assert.assert(defaultOld.contains(currAnn));
    }

    //10. iterate named annotations
    Map namedOld = ((DocumentImpl)this.uc01_LR).getNamedAnnotationSets();
    Iterator itOld = namedOld.keySet().iterator();
    while (itOld.hasNext()) {
      String asetName = (String)itOld.next();
      AnnotationSet asetOld = (AnnotationSet)namedOld.get(asetName);
      AnnotationSet asetNew = (AnnotationSet)dbDoc.getAnnotations(asetName);
      Assert.assertNotNull(asetNew);
      Assert.assertEquals(asetNew,asetOld);
//      Features fmNew = asetNew.getFea
    }


    //11. ALL named annotation (ensure nothing is read from DB twice)
    Map namedNew = dbDoc.getNamedAnnotationSets();

    Assert.assertNotNull(namedNew);
    Assert.assert(namedNew.size() == namedOld.size());

    Iterator itNames = namedNew.keySet().iterator();
    while (itNames.hasNext()) {
      String asetName = (String)itNames.next();
      AnnotationSet asetNew = (AnnotationSet)namedNew.get(asetName);
      AnnotationSet asetOld = (AnnotationSet)namedOld.get(asetName);
      Assert.assertNotNull(asetNew);
      Assert.assertNotNull(asetOld);
      Assert.assertEquals(asetNew,asetOld);
    }

    if(DEBUG) {
      Err.prln("Use case 03 passed...");
    }

    //9.

  }

  public void testDB_UseCase04() throws Exception {

    //sync a document

    if(DEBUG) {
      Err.prln("Use case 04 passed...");
    }
  }

  public static void main(String[] args){
    try{
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      TestPersist test = new TestPersist("");
      test.setUp();
      test.testDelete();
      test.tearDown();

      test.setUp();
      test.testDSR();
      test.tearDown();

      test.setUp();
      test.testMultipleLrs();
      test.tearDown();

      test.setUp();
      test.testSaveRestore();
      test.tearDown();

      test.setUp();
      test.testSimple();
      test.tearDown();

      //I put this last because its failure is dependent on the gc() and
      //there's nothing I can do about it. Maybe I'll remove this from the
      //test
      test.setUp();
      test.testMultipleLrs();
      test.tearDown();

      test.setUp();
      test.testDB_UseCase01();
      test.tearDown();

      test.setUp();
      test.testDB_UseCase02();
      test.tearDown();

      test.setUp();
      test.testDB_UseCase03();
      test.tearDown();

      test.setUp();
      test.testDB_UseCase04();
      test.tearDown();

    }catch(Exception e){
      e.printStackTrace();
    }
  }
} // class TestPersist
