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
import junit.framework.Test;
import java.sql.*;

import oracle.sql.*;
//import oracle.jdbc.driver.*;

import gate.*;
import gate.event.DatastoreListener;
import gate.util.*;
import gate.corpora.*;
import gate.security.*;

/** Persistence test class
  */
public class TestPersist extends TestCase
{
  private static String JDBC_URL_1;
  private static String JDBC_URL_2;
  private static String JDBC_URL;

  /** Debug flag */
  private static final boolean DEBUG = false;
  private static Long sampleDoc_lrID = null;
  private static Long sampleCorpus_lrID = null;
  private static Document sampleDoc = null;
  private static Corpus sampleCorpus = null;
  private static int dbType;

//  private static final String UNICODE_STRING = "\u65e5\u672c\u8a9e\u6587\u5b57\u5217";
  private static final String UNICODE_STRING = "\u0915\u0932\u094d\u0907\u0928\u0643\u0637\u0628\u041a\u0430\u043b\u0438\u043d\u0430 Kalina";
  private static final String ASCII_STRING = "Never mistake motion for action (Ernest Hemingway)";

  private final String VERY_LONG_STRING =
  "The memory of Father came back to her. Ever since she had seen him retreat from those "+
  "twelve-year-old boys she often imagined him in this situation: he is on a sinking ship; "+
  "there are only a few lifeboats and there isn't enough room for everyone; there is a "+
  "furious stampede on the deck. At first Father rushes along with the others, but when he "+
  "sees how they push and shove, ready to trample each other under foot, and a wild-eyed "+
  "woman strikes him with her fist because he is in her way, he suddenly stops and steps "+
  "aside. And in the end he merely watches the overloaded lifeboats as they are slowly "+
  "lowered amid shouts and curses, towards the raging waves. "+
  "What name to give to that attitude? Cowardice? No. Cowards are afraid of dying and will "+
  "fight to survive. Nobility? Undoubtedly, if he had acted out of regard for his fellows. "+
  "But Agnes did not believe this was the motive. What was it then? She couldn't say. Only "+
  "one thing seemed certain: on a sinking ship where it was necessary to fight in order to "+
  "boat a lifeboat, Father would have been condemned in advance. "+
  "Yes, that much was certain. The question that arises is htis: had Father hated the people "+
  "on the ship, just as she now hates the motorcyclist and the man who mocked her because "+
  "she covered her ears? No, Agnes cannot imagine that Father was capable of hatred. "+
  "Hate traps us by binding us too tightly to our adversary. This is the obscenity of war: "+
  "the intimacy of mutually shed blood, the lascivious of two soldiers who, eye to eye, "+
  "bayonet each other. Agnes was sure: it is precisely this kind of intimacy that her "+
  "father found repugnant.The melee on the ship filled him with such disgust that he "+
  "preferred to drown. The physical contact with people who struck and trampled and killed "+
  "one another seemed far worse to him than a solitary death in the purity of the waters. "+
  "[p.25-26] "+
  "In our world, where there are more and more faces, more and more alike, it is difficult "+
  "for an individual to reinforce the originality of the self and to become convinced of "+
  "its inimitatable uniqueness. There are two methods for cultivating the uniqueness of the "+
  "self: the method of addition and the method of subtraction. Agnes subtracts from herself "+
  "everything that is exterior and borrowed, in order to come closer to her sheer essence "+
  "(even with the risk that zero lurks at the bottom of subtarction). Laura's method is "+
  "precisely the opposite: in order to make herself even more visible, perceivable, seizable, "+
  "sizeable, she keeps adding to it more and more attributes and she attemptes to identify "+
  "herself with them (with the risk that the essence of the self may be buried by the "+
  "additional attributes). "+
  "Let's take her car as an example. After her divorce, Laura remained alone in a large "+
  "apartment and felt lonely. She longed for a pet to share her solitude. First she thought "+
  "of a dog, but soon realized that a dog needed a kind of care she would be unable to "+
  "provide. And she got a cat. It was a big Siamese cat, beautiful and wicked. As she "+
  "lived with her car and regaled her friends with stories about it, the animal that she "+
  "picked more or less by accident, without any special conviction (after all, her first "+
  "choice was a dog!), took on an evr growing significance: she began to lavish praise "+
  "on her pet and forced everyone to admire it. She saw in the cat a superb independence, "+
  "pride, freedon of action and constancy of charm (so different from human charm, which "+
  "is always spoiled by moments of clumsiness and unattractiveness); in the cat, she saw "+
  "her paradigm; in the cat she saw herself. "+
  " "+
   "The method of addition is quite charming if it involves adding to the self such "+
   "things as a cat, a dog, roast pork, love of the sea or of cold showers. But the matter "+
   "becomes less idyllic if a person decides to add love for communism, for the homeland, "+
   "for Mussolini, for Roman Catolicism or atheism, for facism or antifacism. In both cases "+
   "the method remains exactly the same: a person stubbornly defending the superiority of "+
   "cats over other animals is doing basically the same as the one who maintains that "+
   "Mussolini was the sole saviour of Italy: he is proud of this attribute of the self and "+
   "he tries to make this attribute (a cat or Mussolini) acknowledged or loved by everyone. "+
   "Here is that strange paradox to which all people cultivating the self by way of the "+
   "addition method are subject: they use addition in order to create unique, inimitable "+
   "self, yet because they automatically become prpagandists for the added attributes, they are "+
   "actually doing everything in their power to make as many others as possible similar "+
   "to themselves; as a result, their uniqueness (so painfully gained) quickly begins to disappear. "+
   "We may ask ourselves why a person who loves a cat (or Mussolini) is not satisfied to "+
   "keep his love to himself, and wants to force it on others. Let's seek the answer by "+
   "recalling the young woman in the sauna, who belligerently asserted that she loved "+
   "cold showers. She thereby managed to differentiate herself at once from one half of the "+
   "human race, namely the half that prefers hot showers. Unfortunately, that other half "+
   "resembled her all the more. Alas, how sad! Many people, few ideas, so how are we "+
   "differentiate ourselves from each other? The young woman knew only one way of overcoming "+
   "the disadvantage of her similarity to that enourmous throng devoted to cold showers: "+
   "she had to proclaim her credo \"I adore cold showers!\" as soon as she appeared in the "+
   "door of the sauna and to proclaim it with such fervour as to make the millions of other "+
   "women who also enjoy cold showers seem like pale imitations of herself. Let me put it "+
   "another way: a mere (simple and innocent) love for showers can become an attribute "+
   "of the self only on condition that we let the world we are ready to fight for it. "+
   "The one who chooses as an atrtibyte of the self a love for Mussolini becomes a "+
   "political warrior, while the partisan of cats, music or antique furniture bestows "+
   "gifts on his surroundings. "+
    "[p.111-113]";

  /** Construction */
  public TestPersist(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    if (! Gate.getDataStoreRegister().getConfigData().containsKey("url-test"))
      throw new GateRuntimeException("DB URL not configured in gate.xml");
    else
      JDBC_URL_1 =
        (String) Gate.getDataStoreRegister().getConfigData().get("url-test");
      JDBC_URL_2 =
        (String) Gate.getDataStoreRegister().getConfigData().get("url-test1");
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
    if(! cannotSync) assertTrue("doc synced ok before adoption", false);

    // check that we can't adopt a resource that's stored somewhere else
    doc.setDataStore(new SerialDataStore(new File("z:\\").toURL().toString()));
    try { sds.adopt(doc,null); } catch(PersistenceException e) { cannotSync=true; }
    if(! cannotSync)
      assertTrue("doc adopted but in other datastore already", false);
    doc.setDataStore(null);
    doc.setName("Alicia Tonbridge, a Document");

    // save the document
    Document persDoc = (Document) sds.adopt(doc,null);
    sds.sync(persDoc);
    Object lrPersistenceId = persDoc.getLRPersistenceId();

    // test the getLrTypes method
    List lrTypes = sds.getLrTypes();
    assertTrue("wrong number of types in SDS", lrTypes.size() == 1);
    assertTrue(
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

    try{
      boolean value = TestEqual.documentsEqual(doc3, doc2);
      assertTrue(TestEqual.message, value);
      value = TestEqual.documentsEqual(persDoc, doc2);
      assertTrue(TestEqual.message, value);
    }finally{
      // delete the datastore
      sds.delete();
    }
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
    assertTrue(TestEqual.documentsEqual(persDoc, doc2));

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
    assertTrue("corp name != mem name", corp.getName().equals(diskCorp.getName()));
    if (DEBUG) Out.prln("Memory features " + corp.getFeatures());
    if (DEBUG) Out.prln("Disk features " + diskCorp.getFeatures());
    assertTrue("corp feat != mem feat",
           corp.getFeatures().equals(diskCorp.getFeatures()));
    if (DEBUG)
      Out.prln("Annotations in doc: " + diskDoc.getAnnotations());
    assertTrue("doc annotations from disk not equal to memory version",
          TestEqual.annotationSetsEqual(doc.getAnnotations(),
                                        diskDoc.getAnnotations()));

    assertTrue("doc from disk not equal to memory version",
          TestEqual.documentsEqual(doc, diskDoc));

    Iterator corpusIter = diskCorp.iterator();
    while(corpusIter.hasNext()){
      if (DEBUG)
        Out.prln(((Document) corpusIter.next()).getName());
      else
        corpusIter.next();
    }


//    assertTrue("doc2 from disk not equal to memory version", doc2.equals(diskDoc2));

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
    assertTrue(sds.getLrIds("gate.corpora.DocumentImpl").size() == 0);

    // delete the datastore
    sds.delete();
  } // testDelete()




  /** Test the DS register. */
  public void testDSR() throws Exception {
    DataStoreRegister dsr = Gate.getDataStoreRegister();
    assertTrue("DSR has wrong number elements (not 0): " + dsr.size(),
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
    assertTrue("DSR has wrong number elements (expected 1): " + dsr.size(),
               dsr.size() == 1);

    // create and open another serial data store
    storageDir = File.createTempFile("TestPersist__", "__StorageDir");
    storageDir.delete();
    DataStore sds2 = Factory.createDataStore(
      "gate.persist.SerialDataStore", storageDir.toURL().toString()
    );

    // DSR should have two members
    assertTrue("DSR has wrong number elements: " + dsr.size(), dsr.size() == 2);

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
    assertTrue("DSR has wrong number elements (expected 1): " + dsr.size(),
               dsr.size() == 1);
    sds.delete();
    assertTrue("DSR has wrong number elements (expected 1): " + dsr.size(),
               dsr.size() == 1);
    sds2.delete();
    assertTrue("DSR has wrong number elements (expected 0): " + dsr.size(),
               dsr.size() == 0);

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
    doc.getFeatures().put("LONG STRING feature", this.VERY_LONG_STRING);
    doc.getFeatures().put("NULL feature",null);
    doc.getFeatures().put("BINARY feature",new Dummy(101,"101",true,101.101f));
    doc.getFeatures().put("LONG feature",new Long(101));
//    doc.getFeatures().put("FLOAT feature",new Double(101.102d));
    doc.getFeatures().put("ASCII feature",ASCII_STRING);
    doc.getFeatures().put("UNICODE feature",UNICODE_STRING);

    //create a complex feature - array of strings
    Vector complexFeature = new Vector();
    complexFeature.add("string 1");
    complexFeature.add("string 2");
    complexFeature.add("string 3");
    complexFeature.add("string 4");
    complexFeature.add("string 5");
    doc.getFeatures().put("complex feature",complexFeature);
    FeatureMap fm  = Factory.newFeatureMap();
//    fm.put("FLOAT feature ZZZ",new Double(101.102d));
//    fm.put("ASCII feature",ASCII_STRING);
//      fm.put("INT feature",new Integer(1212));
//    fm.put("UNICODE feature",UNICODE_STRING);
    doc.getAnnotations().add(
      new Long(0), new Long(20), "thingymajig", fm);
    doc.setName("DB test Document---");

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
    corp.getFeatures().put("my LONG STRING feature", this.VERY_LONG_STRING);
    corp.getFeatures().put("my NULL feature", null);
    corp.getFeatures().put("my BINARY feature",new Dummy(101,"101",true,101.101f));
    return corp;
  }

  private DatabaseDataStore _createDS() {

    DatabaseDataStore ds = null;
    if (this.dbType == DBHelper.ORACLE_DB) {
      ds = new OracleDataStore();
    }
    else if (this.dbType == DBHelper.POSTGRES_DB) {
      ds = new PostgresDataStore();
    }
    else {
      throw new IllegalArgumentException();
    }

    Assert.assertNotNull(ds);
    return ds;
  }

  private void prepareDB(String db) {

    if (this.JDBC_URL_1.indexOf(db) > 0 ) {
      this.JDBC_URL = this.JDBC_URL_1;
    }
    else {
      this.JDBC_URL = this.JDBC_URL_2;
    }

    Assert.assertNotNull("jdbc url not set for Oracle or Postgres",this.JDBC_URL);

    this.dbType = DBHelper.getDatabaseType(JDBC_URL);
  }


  /** Test the DS register. */
  private void _testDB_UseCase01() throws Exception {
///Err.prln("Use case 01 started...");
    //descr: create a document in the DB


    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //2. get test document
    Document doc = createTestDocument();
    Assert.assertNotNull(doc);

    //3. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    ac.open();
    Assert.assertNotNull(ac);

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //4. create security settings for doc
    SecurityInfo si = new SecurityInfo(SecurityInfo.ACCESS_WR_GW,usr,grp);

    //4.5 set DS session
    ds.setSession(usrSession);

    //5. try adding doc to data store
    LanguageResource lr = ds.adopt(doc,si);

    Assert.assertTrue(lr instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(lr.getDataStore());
    Assert.assertTrue(lr.getDataStore() instanceof DatabaseDataStore);
    Assert.assertEquals(doc.getAnnotations(), ((DatabaseDocumentImpl)lr).getAnnotations());

    sampleDoc_lrID = (Long)lr.getLRPersistenceId();
    if (DEBUG) Out.prln("lr id: " + this.sampleDoc_lrID);
//    this.sampleDoc = lr;
    sampleDoc = doc;
//System.out.println("adopted doc:name=["+((Document)lr).getName()+"], lr_id=["+((Document)lr).getLRPersistenceId()+"]");
    //6.close
    ac.close();
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 01 passed...");
    }
  }


  private void _testDB_UseCase02() throws Exception {
///Err.prln("Use case 02 started...");
    //read a document
    //use the one created in UC01
    LanguageResource lr = null;

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //3. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //4. create security settings for doc
    SecurityInfo si = new SecurityInfo(SecurityInfo.ACCESS_WR_GW,usr,grp);

    //4.5 set DS session
    ds.setSession(usrSession);

    //2. read LR
///Err.println(">>>");
    FeatureMap params = Factory.newFeatureMap();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    lr = (LanguageResource) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);
///Err.println("<<<");
    //3. check name
    String name = lr.getName();
    Assert.assertNotNull(name);
    Assert.assertEquals(name,sampleDoc.getName());

    //4. check features
    FeatureMap fm = lr.getFeatures();
    FeatureMap fmOrig = sampleDoc.getFeatures();

    Assert.assertNotNull(fm);
    Assert.assertNotNull(fmOrig);
    Assert.assertTrue(fm.size() == fmOrig.size());

    Iterator keys = fm.keySet().iterator();

    while (keys.hasNext()) {
      String currKey = (String)keys.next();
      Assert.assertTrue(fmOrig.containsKey(currKey));
      Assert.assertEquals(fm.get(currKey),fmOrig.get(currKey));
    }

    //6. URL
    DatabaseDocumentImpl dbDoc = (DatabaseDocumentImpl)lr;
    Assert.assertEquals(dbDoc.getSourceUrl(),this.sampleDoc.getSourceUrl());

    //5.start/end
    Assert.assertEquals(dbDoc.getSourceUrlStartOffset(),this.sampleDoc.getSourceUrlStartOffset());
    Assert.assertEquals(dbDoc.getSourceUrlEndOffset(),this.sampleDoc.getSourceUrlEndOffset());

    //6.markupAware
    Assert.assertEquals(dbDoc.getMarkupAware(),this.sampleDoc.getMarkupAware());

    //7. content
    DocumentContent cont = dbDoc.getContent();
    Assert.assertEquals(cont,this.sampleDoc.getContent());

    //8. access the content again and assure it's not read from the DB twice
    Assert.assertEquals(cont,((Document)this.sampleDoc).getContent());

    //9. encoding
    String encNew = (String)dbDoc.
      getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME);
    String encOld = (String)((DocumentImpl)this.sampleDoc).
      getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME);
    Assert.assertEquals(encNew,encOld);

    //10. default annotations
///System.out.println("GETTING default ANNOTATIONS...");
    AnnotationSet defaultNew = dbDoc.getAnnotations();
    AnnotationSet defaultOld = this.sampleDoc.getAnnotations();

    Assert.assertNotNull(defaultNew);
    Assert.assertTrue(defaultNew.size() == defaultOld.size());
    Assert.assertEquals(defaultNew,defaultOld);


    //10. iterate named annotations
    Map namedOld = this.sampleDoc.getNamedAnnotationSets();
    Iterator itOld = namedOld.keySet().iterator();
///System.out.println("GETTING named ANNOTATIONS...");
    while (itOld.hasNext()) {
      String asetName = (String)itOld.next();
      AnnotationSet asetOld = (AnnotationSet)namedOld.get(asetName);
      AnnotationSet asetNew = (AnnotationSet)dbDoc.getAnnotations(asetName);
      Assert.assertNotNull(asetNew);
///System.out.println("aset_old, size=["+asetOld.size()+"]");
///System.out.println("aset_new, size=["+asetNew.size()+"]");
///System.out.println("old = >>" + asetOld +"<<");
///System.out.println("new = >>" + asetNew +"<<");
      Assert.assertTrue(asetNew.size() == asetOld.size());
      Assert.assertEquals(asetNew,asetOld);
    }


    //11. ALL named annotation (ensure nothing is read from DB twice)
    Map namedNew = dbDoc.getNamedAnnotationSets();

    Assert.assertNotNull(namedNew);
    Assert.assertTrue(namedNew.size() == namedOld.size());

    Iterator itNames = namedNew.keySet().iterator();
    while (itNames.hasNext()) {
      String asetName = (String)itNames.next();
      AnnotationSet asetNew = (AnnotationSet)namedNew.get(asetName);
      AnnotationSet asetOld = (AnnotationSet)namedOld.get(asetName);
      Assert.assertNotNull(asetNew);
      Assert.assertNotNull(asetOld);
      Assert.assertEquals(asetNew,asetOld);
    }

    //close
    ds.removeDatastoreListener((DatastoreListener)lr);
    lr = null;

    ds.close();
    ac.close();

    if(DEBUG) {
      Err.prln("Use case 02 passed...");
    }

  }


  private void _testDB_UseCase03() throws Exception {
///Err.prln("Use case 03 started...");
    //sync a document
    LanguageResource lr = null;

    //0. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();

    //1.5 set DS session
    ds.setSession(usrSession);

    if (DEBUG) Out.prln("ID " + sampleDoc_lrID);
    //2. read LR
    FeatureMap params = Factory.newFeatureMap();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    lr = (LanguageResource) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);
    Document dbDoc = (Document)lr;
    Document doc2 = null;

    //2.5 get exclusive lock
    if (false == ds.lockLr(lr)) {
      throw new PersistenceException("document is locked by another user");
    }

    //3. change name
    String oldName = dbDoc.getName();
    String newName = oldName + "__UPD";
    dbDoc.setName(newName);
    dbDoc.sync();
//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(newName,dbDoc.getName());
    Assert.assertEquals(newName,doc2.getName());

    Factory.deleteResource(doc2);
    doc2 = null;

    //4. change features
    FeatureMap fm = dbDoc.getFeatures();
    Iterator keys = fm.keySet().iterator();

    //4.1 change the value of the first feature
    while(keys.hasNext()) {
      String currKey = (String)keys.next();
      Object val = fm.get(currKey);
      Object newVal = null;
      if (val instanceof Long) {
        newVal = new Long(101010101);
      }
      else if (val instanceof Integer) {
        newVal = new Integer(2121212);
      }
      else if (val instanceof String) {
        newVal = new String("UPD__").concat( (String)val).concat("__UPD");
      }
      if (newVal != null)
        fm.put(currKey,newVal);
    }
    dbDoc.sync();
//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(fm,dbDoc.getFeatures());
    Assert.assertEquals(fm,doc2.getFeatures());
    Factory.deleteResource(doc2);
    doc2 = null;

    //6. URL
    URL docURL = dbDoc.getSourceUrl();
    URL newURL = null;
    newURL = new URL(docURL.toString()+".UPDATED");
    dbDoc.setSourceUrl(newURL);
    dbDoc.sync();
//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(newURL,dbDoc.getSourceUrl());
    Assert.assertEquals(newURL,doc2.getSourceUrl());
    Factory.deleteResource(doc2);
    doc2 = null;

    //5.start/end
    Long newStart = new Long(123);
    Long newEnd = new Long(789);
    dbDoc.setSourceUrlStartOffset(newStart);
    dbDoc.setSourceUrlEndOffset(newEnd);
    dbDoc.sync();

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(newStart,dbDoc.getSourceUrlStartOffset());
    Assert.assertEquals(newStart,doc2.getSourceUrlStartOffset());
    Assert.assertEquals(newEnd,dbDoc.getSourceUrlEndOffset());
    Assert.assertEquals(newEnd,doc2.getSourceUrlEndOffset());

    Factory.deleteResource(doc2);
    doc2 = null;


    //6.markupAware
    Boolean oldMA = dbDoc.getMarkupAware();
    Boolean newMA = oldMA.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    dbDoc.setMarkupAware(newMA);
    dbDoc.sync();

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(newMA,doc2.getMarkupAware());
    Assert.assertEquals(newMA,dbDoc.getMarkupAware());

    Factory.deleteResource(doc2);
    doc2 = null;


    //7. content
    DocumentContent contOld = dbDoc.getContent();
    DocumentContent contNew = new DocumentContentImpl(new String("UPDATED__").concat(contOld.toString().concat("__UPDATED")));
    dbDoc.setContent(contNew);
    dbDoc.sync();

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertEquals(contNew,dbDoc.getContent());
    Assert.assertEquals(contNew,doc2.getContent());

    Factory.deleteResource(doc2);
    doc2 = null;

    //8. encoding
    String encOld = (String)dbDoc.
      getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME);
    dbDoc.setParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME,"XXX");
    dbDoc.sync();
//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    String encNew = (String)doc2.
      getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME);
    Assert.assertEquals(encNew,encOld);

    Factory.deleteResource(doc2);
    doc2 = null;


    //9. add annotations
    AnnotationSet dbDocSet = dbDoc.getAnnotations("TEST SET");
    Assert.assertNotNull(dbDocSet);

    FeatureMap fm1 = new SimpleFeatureMapImpl();
    fm1.put("string key","string value");

    Integer annInd = dbDocSet.add(new Long(0), new Long(10),
                                "TEST TYPE",
                                fm1);

    dbDoc.sync();
//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    AnnotationSet doc2Set = doc2.getAnnotations("TEST SET");
    Assert.assertTrue(dbDocSet.size() == doc2Set.size());
//--    Assert.assertEquals(doc2Set,dbDocSet);

    Factory.deleteResource(doc2);
    doc2 = null;


    //9.1. change+add annotations
    Annotation dbDocAnn = dbDocSet.get(annInd);

    FeatureMap fm2 = new SimpleFeatureMapImpl();
    fm2.put("string2","uuuuuu");
    fm2.put("int2",new Integer(98989898));
    Integer newInd = dbDocSet.add(dbDocAnn.getStartNode().getOffset(),
                                    dbDocAnn.getEndNode().getOffset(),
                                    dbDocAnn.getType() + "__XX",
                                    fm2);
    Annotation dbDocAnnNew = dbDocSet.get(newInd);
    dbDoc.sync();

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    doc2Set = doc2.getAnnotations("TEST SET");
    Assert.assertTrue(dbDocSet.size() == doc2Set.size());
    Assert.assertTrue(TestEqual.annotationSetsEqual(doc2Set, dbDocSet));
    Assert.assertTrue(doc2Set.contains(dbDocAnnNew));

    Factory.deleteResource(doc2);
    doc2 = null;
/*
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
*/

    //11. add a new ann-set
    String dummySetName = "--NO--SUCH--SET--";
    AnnotationSet aset = dbDoc.getAnnotations(dummySetName);
    aset.addAll(dbDoc.getAnnotations());
    dbDoc.sync();

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertTrue(dbDoc.getNamedAnnotationSets().containsKey(dummySetName));
    Assert.assertTrue(doc2.getNamedAnnotationSets().containsKey(dummySetName));
    AnnotationSet copy1 = (AnnotationSet)
                          dbDoc.getNamedAnnotationSets().get(dummySetName);
    AnnotationSet copy2 = (AnnotationSet)
                          doc2.getNamedAnnotationSets().get(dummySetName);
    Assert.assertTrue(dbDoc.getNamedAnnotationSets().containsValue(aset));
    Assert.assertTrue(TestEqual.annotationSetsEqual(copy1, copy2));
    Assert.assertTrue(dbDoc.getNamedAnnotationSets().size() == doc2.getNamedAnnotationSets().size());
//maps aren't equal since removing the equals impementations
//    Assert.assertEquals(doc2.getNamedAnnotationSets(),dbDoc.getNamedAnnotationSets());

    Factory.deleteResource(doc2);
    doc2 = null;

    //12. remove aset
    dbDoc.removeAnnotationSet(dummySetName);
    dbDoc.sync();
    Assert.assertTrue(false == ((EventAwareDocument)dbDoc).getLoadedAnnotationSets().contains(dummySetName));
    Assert.assertTrue(false == dbDoc.getNamedAnnotationSets().containsKey(dummySetName));

//--    doc2 = (Document)ds.getLr(DBHelper.DOCUMENT_CLASS,sampleDoc_lrID);
    params.clear();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    doc2= (Document) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    Assert.assertTrue(false == doc2.getNamedAnnotationSets().containsKey(dummySetName));

    Factory.deleteResource(doc2);
    doc2 = null;

    //13. unlock
    ds.unlockLr(lr);

    //close
    Factory.deleteResource(dbDoc);
    dbDoc = null;

    ac.close();
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 03 passed...");
    }
  }


  private void _testDB_UseCase04() throws Exception {
///Err.prln("Use case 04 started...");
    //delete a document
    LanguageResource lr = null;

    //0. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();
    ds.setSession(usrSession);

    //2. read LR
    FeatureMap params = Factory.newFeatureMap();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, this.sampleDoc_lrID);
    lr = (LanguageResource) Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

    //2.5 get exclusive lock
    if (false == ds.lockLr(lr)) {
      throw new PersistenceException("document is locked by another user");
    }

    //3. try to delete it
    ds.delete(DBHelper.DOCUMENT_CLASS,lr.getLRPersistenceId());

    //no need to unlock

    //close
    ds.close();
    ac.close();

    if(DEBUG) {
      Err.prln("Use case 04 passed...");
    }

  }


  /** Test the DS register. */
  private void _testDB_UseCase101() throws Exception {
///Err.prln("Use case 101 started...");
    //descr : create a corpus

    //0. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();
    ds.setSession(usrSession);

    //2. get test document
    Corpus corp = createTestCorpus();
    Assert.assertNotNull(corp);

    //4. create security settings for doc
    SecurityInfo si = new SecurityInfo(SecurityInfo.ACCESS_WR_GW,usr,grp);

    //5. try adding corpus to data store
    Corpus result = (Corpus)ds.adopt(corp,si);
    Assert.assertNotNull(result);
    Assert.assertTrue(result instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(result.getLRPersistenceId());

    this.sampleCorpus =  result;
    this.sampleCorpus_lrID = (Long)result.getLRPersistenceId();

    //6.close
    ac.close();
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 101 passed...");
    }

  }



  /** Test the DS register. */
  private void _testDB_UseCase102() throws Exception {
    //read a corpus
///Err.prln("Use case 102 started...");
    LanguageResource lr = null;

    //0. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();
    ds.setSession(usrSession);

    //2. read LR
    FeatureMap params = Factory.newFeatureMap();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, sampleCorpus_lrID);
    lr = (LanguageResource) Factory.createResource(DBHelper.CORPUS_CLASS, params);

    //3. check name
    String name = lr.getName();
    Assert.assertNotNull(name);
    Assert.assertEquals(name,sampleCorpus.getName());

    //4. check features
    FeatureMap fm = lr.getFeatures();
    FeatureMap fmOrig = sampleCorpus.getFeatures();

    Assert.assertNotNull(fm);
    Assert.assertNotNull(fmOrig);
    Assert.assertTrue(fm.size() == fmOrig.size());

    Iterator keys = fm.keySet().iterator();

    while (keys.hasNext()) {
      String currKey = (String)keys.next();
      Assert.assertTrue(fmOrig.containsKey(currKey));
      Assert.assertEquals(fm.get(currKey),fmOrig.get(currKey));
    }

    //close
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 102 passed...");
    }

  }


  private void _testDB_UseCase103() throws Exception {
///Err.prln("Use case 103 started...");
    //sync a corpus
    LanguageResource lr = null;

    //0. get security factory & login
    AccessController ac = Factory.createAccessController(this.JDBC_URL);
    Assert.assertNotNull(ac);
    ac.open();

    User usr = ac.findUser("kalina");
    Assert.assertNotNull(usr);

    Group grp = (Group)usr.getGroups().get(0);
    Assert.assertNotNull(grp);

    Session usrSession = ac.login("kalina","sesame",grp.getID());
    Assert.assertNotNull(usrSession);
    Assert.assertTrue(ac.isValidSession(usrSession));

    //1. open data storage
    DatabaseDataStore ds = this._createDS();
    Assert.assertNotNull(ds);
    ds.setStorageUrl(this.JDBC_URL);
    ds.open();
    ds.setSession(usrSession);

    if (DEBUG) Out.prln("ID " + sampleCorpus_lrID);

    //2. read LR
    FeatureMap params = Factory.newFeatureMap();
    params.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    params.put(DataStore.LR_ID_FEATURE_NAME, sampleCorpus_lrID);
    lr = (LanguageResource) Factory.createResource(DBHelper.CORPUS_CLASS, params);

    Corpus dbCorp = (Corpus)lr;
    Corpus corp2 = null;

    //3. change name
    String oldName = dbCorp.getName();
    String newName = oldName + "__UPD";
    dbCorp.setName(newName);
    dbCorp.sync();
    corp2 = (Corpus)ds.getLr(DBHelper.CORPUS_CLASS,sampleCorpus_lrID);
    Assert.assertEquals(newName,dbCorp.getName());
    Assert.assertEquals(newName,corp2.getName());

    //4. change features
    FeatureMap fm = dbCorp.getFeatures();
    Iterator keys = fm.keySet().iterator();

    //4.1 change the value of the first feature
    while(keys.hasNext()) {
      String currKey = (String)keys.next();
      Object val = fm.get(currKey);
      Object newVal = null;
      if (val instanceof Long) {
        newVal = new Long(101010101);
      }
      else if (val instanceof Integer) {
        newVal = new Integer(2121212);
      }
      else if (val instanceof String) {
        newVal = new String("UPD__").concat( (String)val).concat("__UPD");
      }
      if (newVal != null)
        fm.put(currKey,newVal);
    }
    dbCorp.sync();
    corp2 = (Corpus)ds.getLr(DBHelper.CORPUS_CLASS,sampleCorpus_lrID);
    Assert.assertEquals(fm,dbCorp.getFeatures());
    Assert.assertEquals(fm,corp2.getFeatures());

    //close
    ds.close();

    if(DEBUG) {
      Err.prln("Use case 103 passed...");
    }

}

  public void testOracle_01() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase01();
  }

  public void testOracle_02() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase02();
  }

  public void testOracle_03() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase03();
  }

  public void testOracle_04() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase04();
  }

  public void testOracle_101() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase101();
  }

  public void testOracle_102() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase102();
  }

  public void testOracle_103() throws Exception {

    prepareDB("oracle");
    _testDB_UseCase103();
  }

  public void testPostgres_01() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase01();
  }

  public void testPostgres_02() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase02();
  }

  public void testPostgres_03() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase03();
  }

  public void testPostgres_04() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase04();
  }

  public void testPostgres_101() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase101();
  }

  public void testPostgres_102() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase102();
  }

  public void testPostgres_103() throws Exception {

    prepareDB("postgres");
    _testDB_UseCase103();
  }



  public static void main(String[] args){
    try{

System.setProperty(Gate.GATE_CONFIG_PROPERTY,"y:/gate.xml")    ;
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();


      TestPersist test = new TestPersist("");

/*
      long timeStart = 0;
      timeStart = System.currentTimeMillis();
      int size = 512*1024;
//      test.testOracleLOB(size,3);
      test.testPostgresLOB(size,3);
      System.out.println("time: ["+ (System.currentTimeMillis()-timeStart) +"]");

      if (true) {
        throw new RuntimeException();
      }
*/

      /* oracle */

      test.setUp();
      test.testOracle_01();
      test.tearDown();

      test.setUp();
      test.testOracle_02();
      test.tearDown();

      test.setUp();
      test.testOracle_03();
      test.tearDown();

      test.setUp();
      test.testOracle_04();
      test.tearDown();

      test.setUp();
      test.testOracle_101();
      test.tearDown();

      test.setUp();
      test.testOracle_102();
      test.tearDown();

      test.setUp();
      test.testOracle_103();
      test.tearDown();


      /* postgres */

      test.setUp();
      test.testPostgres_01();
      test.tearDown();

      test.setUp();
      test.testPostgres_02();
      test.tearDown();

      test.setUp();
      test.testPostgres_03();
      test.tearDown();

      test.setUp();
      test.testPostgres_04();
      test.tearDown();

      test.setUp();
      test.testPostgres_101();
      test.tearDown();

      test.setUp();
      test.testPostgres_102();
      test.tearDown();

      test.setUp();
      test.testPostgres_103();
      test.tearDown();

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
//      test.testSaveRestore();
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

      if (DEBUG) {
        Err.println("done.");
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

/*
  public void testPostgresLOB(int size, int count) throws Exception {

    byte[] buffer = new byte[size];
    String url = "jdbc:postgresql://192.168.128.208:5432/gate09?user=gateuser&password=gate";
//    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

    try {
      Connection conn = DBHelper.connect(url);
      conn.setAutoCommit(false);
      PreparedStatement pstmt = conn.prepareStatement("insert into lob_test values(?)");

      for (int i =0; i< count; i++) {
//        bais.reset();
//        pstmt.setBinaryStream(1,bais,buffer.length);
        pstmt.setBytes(1,buffer);
        pstmt.executeUpdate();
        conn.commit();
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }


  }

  public void testOracleLOB(int size,int count) throws Exception {
    byte[] buffer = new byte[size];
    String url = "jdbc:oracle:thin:GATEUSER/gate@192.168.128.208:1521:gate07";
    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

    CallableStatement cstmt = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Blob blobValue = null;

    try {
      Connection conn = DBHelper.connect(url);
      conn.setAutoCommit(false);
      cstmt = conn.prepareCall("{ call gateadmin.create_lob(?) }");


      for (int i =0; i< count; i++) {

        cstmt.registerOutParameter(1,java.sql.Types.BIGINT);
        cstmt.execute();
        long blobID = cstmt.getLong(1);

        pstmt = conn.prepareStatement("select blob_value from gateadmin.lob_test where id=?");
        pstmt.setLong(1,blobID);
        pstmt.execute();
        rs = pstmt.getResultSet();
        rs.next();

        blobValue = rs.getBlob(1);
        BLOB oraBlob = (BLOB)blobValue;
        OutputStream output = oraBlob.getBinaryOutputStream();
        output.write(buffer,0,buffer.length);
        output.close();

        conn.commit();
      }

    }
    catch(Exception e) {
      e.printStackTrace();
    }

  }

*/


/*
  public void testPostgres01() throws Exception {

    String url = "jdbc:postgresql://192.168.128.208:5432/gate09";
    try {

      Connection c = DBHelper.connect(url,"gateuser","gate");
      c.setAutoCommit(false);

      Object src = new Long(1234);

      PreparedStatement pstmt = c.prepareStatement("insert into test3 values (nextval('seq3'), ?)");
      Object o = new Object();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(src);
      oos.flush();
      oos.close();
      baos.close();

      byte[] buff = baos.toByteArray();
System.out.println(buff.length);
      ByteArrayInputStream bais = new ByteArrayInputStream(buff);

      pstmt.setBinaryStream(1,bais,buff.length);
      pstmt.execute();
bais.close();
      c.commit();
      bais.close();

      PreparedStatement pstmt2 = c.prepareStatement("select blob from test3 where id = (select max(id) from test3)");
      pstmt2.execute();
      ResultSet rs = pstmt2.getResultSet();
      if (false == rs.next()) {
        throw new Exception("empty result set");
      }

      InputStream is = rs.getBinaryStream("blob");
      ObjectInputStream ois = new ObjectInputStream(is);
      Object result = ois.readObject();
System.out.println(result);
      ois.close();
      is.close();

      rs.close();
      pstmt2.close();

      c.commit();

    }
    catch(SQLException e) {
System.out.println(e.getErrorCode());
      e.printStackTrace();
    }

  }
*/

} // class TestPersist


class Dummy implements Serializable {

  static final long serialVersionUID = 3632609241787241900L;

  public int     intValue;
  public String  stringValue;
  public boolean boolValue;
  public float   floatValue;


  public Dummy(int _int, String _string, boolean _bool, float _float) {

    this.intValue = _int;
    this.stringValue= _string;
    this.boolValue = _bool;
    this.floatValue = _float;
  }

  public boolean equals(Object obj) {
    Dummy d2 = (Dummy)obj;

    return  this.intValue == d2.intValue &&
            this.stringValue.equals(d2.stringValue)  &&
            this.boolValue == d2.boolValue &&
            this.floatValue == d2.floatValue;
  }

  public String toString() {
    return "Dummy: intV=["+this.intValue+"], stringV=["+this.stringValue+"], boolV=["+this.boolValue+"], floatV = ["+this.floatValue+"]";
  }

}
