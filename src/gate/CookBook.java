/*
 *  CookBook.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 16/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.creole.tokeniser.*;
import gate.creole.splitter.*;
import gate.creole.gazetteer.*;
import gate.creole.orthomatcher.*;


/**
  * <P><B>NOTE: this class has been REPLACED by the GateExamples package;
  * see
  * <A HREF=http://gate.ac.uk/GateExamples/doc/>http://gate.ac.uk/GateExamples/doc/</A>.</B>
  *
  * <P>
  * This class provides examples of using the GATE APIs.
  * Read this documentation along with a copy of the
  * <A HREF=http://gate.ac.uk/gate/doc/java2html/gate/CookBook.java.html>source
  * code</A>.
  *
  * <P>
  * The CookBook is set up as
  * part of the GATE test suite (using the
  * <A HREF="http://www.junit.org/>JUnit testing framework</A>), so there's
  * an easy way to run the examples (viz.,
  * <A HREF=../gate/TestGate.html>gate.TestGate</A>'s <TT>main</TT> method,
  * which will invoke the
  * JUnit test runner). Also, we can use JUnit's assert methods: e.g.
  * <TT>assertTrue(corpus.isEmpty());</TT>
  * tests that a corpus object is empty, and creates a test failure report if
  * this is not the case. (To add a new test class to the suite, see the
  * <A HREF=../gate/util/TestTemplate.html>gate.util.TestTemplate</A> class.)
  *
  * <P>
  * Programming to the GATE Java API involves manipulating the classes and
  * interfaces in the <A HREF=package-summary.html>gate package</A>
  * (and to a lesser extent other packages). These are
  * often interfaces; classes there are often to do with getting
  * access to objects that implement the interfaces (without exposing those
  * implementations). In other words, there's a lot of interface-based design
  * around.
  *
  * <P>
  * For more details and for a conceptual view, see
  * <A HREF=http://gate.ac.uk/sale/tao/>Developing Language Processing
  * Components with GATE</A> (for which this class provides some of the
  * examples).
  *
  * <P>
  * The rest of this documentation refers to methods in the code that
  * provide examples of using the GATE API.
  *
  * <P>
  * The <A HREF=#testResourceCreation()>testResourceCreation</A> method gives
  * an example of creating a resource via
  * <A HREF=../gate/Factory.html>gate.Factory</A>.
  *
  * <P>
  * The <A HREF=Corpus.html>Corpus interface</A> represents collections of
  * <A HREF=Document.html>Documents</A> (and takes the place of the old TIPSTER
  * <TT>Collection</TT> class).
  *
  * <P>
  * The <A HREF=#testCorpusConstruction()>testCorpusConstruction</A> method
  * gives an example of how to create a new transient Corpus object.
  *
  * <P>
  * The <A HREF=#testAddingDocuments()>testAddingDocuments</A> method gives
  * examples of adding documents to corpora.
  *
  * <P>
  * The <A HREF=#testAddingAnnotations()>testAddingAnnotations</A> method gives
  * examples of adding annotations to documents.
  *
  *
  * <P>
  * The <A HREF=#testUsingFeatures()>testUsingFeatures</A> method gives
  * examples of using features. <A HREF=FeatureMap.html>The FeatureMap
  * interface</A> is a mechanism for associating arbitrary data with GATE
  * entities. Corpora, documents and annotations all share this
  * mechanism. Simple feature maps use Java's Map interface.
  *
  *
  * <H3>Other sources of examples</H3>
  *
  * <P>
  * See also the other test classes, although note that they also use methods
  * that are not part of the public API. Test classes include:
  * <A HREF=corpora/TestCreole.html>TestCreole</A>;
  * <A HREF=corpora/TestCorpus.html>TestCorpus</A>;
  * <A HREF=corpora/TestDocument.html>TestDocument</A>;
  * <A HREF=corpora/TestAnnotation.html>TestAnnotation</A>; anything
  * else starting "Test" - about 30 of them at the last count.
  */
public class CookBook extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** A corpus */
  Corpus corpus = null;

  /** A document */
  Document doc1 = null;

  /** Another document */
  Document doc2 = null;

  /** Constructing a resource */
  public void testResourceCreation() throws GateException {

    // before creating a resource we need a feature map to store
    // parameter values
    FeatureMap params = Factory.newFeatureMap();

    // to create a document we need a sourceUrlName parameter giving
    // the location of the source for the document content
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME,
      Gate.getUrl("tests/doc0.html"));
    params.put(Document.DOCUMENT_MARKUP_AWARE_PARAMETER_NAME,
      new Boolean(true));
    Resource res = Factory.createResource("gate.corpora.DocumentImpl", params);

    // now we have a document
    assertTrue(
      "should be document but the class is: " + res.getClass().getName(),
      res instanceof gate.Document
    );
    Document doc = (Document) res;
    AnnotationSet markupAnnotations = doc.getAnnotations(
                        GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    //this is useless as doc.getAnnotations() will never return null!
    assertNotNull("no markup annotations on doc " + doc, markupAnnotations);
    int numMarkupAnnotations = markupAnnotations.size();
    if(DEBUG)
      Out.prln("annotations on doc after unpack= " + numMarkupAnnotations);
    assertTrue(
      "wrong number annots on doc: " + doc + numMarkupAnnotations,
      numMarkupAnnotations == 27
    );

  } // testResourceCreation

  /** Constructing a corpus */
  public void testCorpusConstruction() throws GateException {

    // corpus constructors require a name
    corpus = Factory.newCorpus("My example corpus");

    // the corpus interface inherits all the sorted set methods
    assertTrue(corpus.isEmpty());

  } // testCorpusConstruction

  /** Adding documents to a corpus */
  public void testAddingDocuments() throws GateException {

    corpus = Factory.newCorpus("My example corpus");

    // add a document or two....
    corpus.add(doc1);
    corpus.add(doc2);

    // iterate the corpus members and do some random tests
    Iterator iter = corpus.iterator();
    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
      assertTrue(
        "document url not as expected",
        doc.getSourceUrl().toExternalForm().endsWith("doc0.html") ||
          doc.getSourceUrl().toExternalForm().endsWith("test1.htm")
      );
    } // while

  } // testAddingDocuments

  /** Adding annotations to documents */
  public void testAddingAnnotations() {
    AnnotationSet as = doc1.getAnnotations();
    FeatureMap fm = doc1.getFeatures();
    Integer id;

    // during creation of annotations offsets are checked and an invalid
    // offset exception thrown if they are invalid
    try {
      id = as.add(new Long(10), new Long(20), "T1", fm);
    } catch (InvalidOffsetException e) {
      fail(e.toString());
    }
  } // testAddingAnnotations

  /** Using the FeatureMap interface */
  public void testUsingFeatures() {
    AnnotationSet as = doc1.getAnnotations();
    Integer id; // the id of new annotations

    // putting features on documents
    FeatureMap fm = Factory.newFeatureMap();
    doc1.setFeatures(fm);
    assertTrue(fm.size() == 0);
    fm.put("author", "segovia");
    assertTrue(fm.get("author").equals("segovia"));
    fm.put("author", "brendl"); // map puts overwrite existing values
    assertTrue(fm.get("author").equals("brendl"));
    assertTrue(fm.size() == 1);

  } // testUsingFeatures

  /** String to print when wrong command-line args */
  private static String usage =
    "usage: CookBook [-dir directory-name | file(s)]";

  /**
   * Main function: an example of embedding GATE-based
   * batch processing. The method:
   * <UL>
   * <LI>
   * initialises the GATE library, and creates PRs for
   * tokenisation, sentence splitting and part of speech tagging
   * <LI>
   * takes a directory name as argument (-dir option) or just a list
   * of files
   * <LI>
   * creates a directory called "out" and an index.html file there
   * <LI>
   * for each .html file in that directory:
   * <BR> create a GATE document from the file
   * <BR> run the PRs on the document
   * <BR> dump some output for the file to "out/gate__[file name].txt",
   * and add a line to the index
   * </UL>
   */
  public static void main(String[] args) throws Exception {
    // say "hi"
    Out.prln("CookBook.main");
    Out.prln("processing command line arguments");

    // check we have a directory name or list of files
    List inputFiles = null;
    if(args.length < 1) throw new GateException(usage);

    // set up a list of all the files to process
    if(args[0].equals("-dir")) { // list all the files in the dir
      if(args.length < 2) throw new GateException(usage);
      File dir = new File(args[1]);
      File[] filesArray = dir.listFiles();
      if(filesArray == null)
        throw new GateException(
          dir.getPath() + " is not a directory; " + usage
        );
      inputFiles = Arrays.asList(filesArray);

    } else { // all args should be file names
      inputFiles = new ArrayList();
      for(int i = 0; i < args.length; i++)
        inputFiles.add(new File(args[i]));
    }

    // did we get some file names?
    if(inputFiles.isEmpty()) {
      throw new GateException("No files to process!");
    }

    // initialise GATE
    Out.prln("initialising GATE");
    Gate.init();

    // create some processing resources
    Out.prln("creating PRs");
    //create a tokeniser
    DefaultTokeniser tokeniser = (DefaultTokeniser)Factory.createResource(
                                      "gate.creole.tokeniser.DefaultTokeniser");
    //create a sentence splitter
    SentenceSplitter splitter = (SentenceSplitter)Factory.createResource(
                                      "gate.creole.splitter.SentenceSplitter");
    //create a POS tagger
    POSTagger tagger = (POSTagger)Factory.createResource(
                                      "gate.creole.POSTagger");

    //create  a gazetteer
    DefaultGazetteer gazetteer = (DefaultGazetteer)Factory.createResource(
                                      "gate.creole.gazetteer.DefaultGazetteer");

    //create a grammar
    ANNIETransducer transducer = (ANNIETransducer)Factory.createResource(
                                      "gate.creole.ANNIETransducer");

    //create an orthomatcher
    OrthoMatcher orthomatcher = (OrthoMatcher) Factory.createResource(
                                "gate.creole.orthomatcher.OrthoMatcher");

    // make the "out" directory that will contain the results.
    String outDirName =
      ((File) inputFiles.get(0)).getParent() + Strings.getFileSep() + "out";
    if(! new File(outDirName).mkdir()){
      throw new GateException("Could not create the output directory");
    }

    // construct a name for the output index file; open; dump header
    String nl = Strings.getNl(); // shorthand for platform's newline
    String fsep =
      Strings.getFileSep(); // shorthand for platform's file separator
    String indexName =
      ( (File) inputFiles.get(0) ).getParent() + fsep + "index.html";
    FileWriter indexWriter = new FileWriter(new File(indexName));
    indexWriter.write("<HTML><HEAD><TITLE>Documents list</TITLE></HEAD>");
    indexWriter.write(nl + "<BODY>" + nl + "<UL>" + nl);

    // main loop:
    // for each document
    //   create a gate doc
    //   set as the document for the PRs
    //   run the PRs
    //   dump output from the doc to out/gate__.....txt
    //   delete the doc

    // loop on files list
    Iterator filesIter = inputFiles.iterator();
    Out.prln("looping on input files list");
    while(filesIter.hasNext()) {
      File inFile = (File) filesIter.next(); // the current file
      Out.prln("processing file " + inFile.getPath());
      FeatureMap params = Factory.newFeatureMap(); // params list for new doc

      // set the source URL parameter to a "file:..." URL string
      params.put(Document.DOCUMENT_URL_PARAMETER_NAME,
        inFile.toURL().toExternalForm());

      // use the platform's default encoding rather than GATE's
      params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "");

      // create the document
      Document doc = (Document) Factory.createResource(
        "gate.corpora.DocumentImpl", params
      );

      // set the document param on the PRs
       tokeniser.setDocument(doc);
       splitter.setDocument(doc);
       tagger.setDocument(doc);
       gazetteer.setDocument(doc);
       transducer.setDocument(doc);
       orthomatcher.setDocument(doc);

      // run each PR
      tokeniser.execute();
      splitter.execute();
      tagger.execute();
      gazetteer.execute();
      transducer.execute();
      orthomatcher.execute();

      // dump out results

      // construct a name for the output file and open a stream
      StringBuffer outFileName = new StringBuffer(inFile.getParent());
      outFileName.append(fsep);
      outFileName.append("out");
      outFileName.append(fsep);
      outFileName.append("gate__");
      outFileName.append(inFile.getName());
      outFileName.append(".txt");
      File outFile = new File(outFileName.toString());
      FileWriter outFileWriter = new FileWriter(outFile);
      Out.prln("dumping " + outFile.getPath());

      // iterate round the token annotations writing to the out file
      // NOTE: to dump all to XML: outFileWriter.write(doc.toXml(tokens));
      AnnotationSet tokens = doc.getAnnotations("nercAS").
        get(ANNIEConstants.TOKEN_ANNOTATION_TYPE);
      Iterator iter = tokens.iterator();
      while(iter.hasNext()) {
        Annotation token = (Annotation) iter.next();
        FeatureMap tokFeats = token.getFeatures();
        String tokStr = (String) tokFeats.
          get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME);
        String tokPos = (String) tokFeats.
          get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
        outFileWriter.write(tokStr + "\t" + tokPos + nl);
      }
      outFileWriter.write(doc.getFeatures().get("entitySet").toString());

      // close the out file stream; add an index line
      outFileWriter.close();
      indexWriter.write(
        "<LI><A href=\"" + inFile.getName() + "\">" + inFile.getName() +
        "</a>" + " -> " + "<a href=\"" + "out" + fsep + outFile.getName() +
        "\">" + "out" + fsep + outFile.getName() + "</a></LI>\n"
      );

      // make the doc a candidate for garbage collection
      Out.prln("deleting gate doc");

      Factory.deleteResource(doc);
    } // input files loop

    // finish the index file
    indexWriter.write(nl + "</UL>" + nl + "</BODY></HTML>" + nl);
    indexWriter.close();

    Out.prln("The End (roll credits)");
  } // main

  /** Fixture set up: initialise members before each test method */
  public void setUp() throws GateException, IOException {
    corpus = Factory.newCorpus("My example corpus");

    doc1 = Factory.newDocument(Gate.getUrl("tests/doc0.html"));
    doc2 = Factory.newDocument(Gate.getUrl("tests/html/test1.htm"));
  } // setUp

  /** Construction */
  public CookBook(String name) { super(name); }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(CookBook.class);
  } // suite

} // class CookBook
