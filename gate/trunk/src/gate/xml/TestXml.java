/*
 *  TestXml.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  8/May/2000
 *
 *  $Id$
 */

package gate.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.*;
import java.text.NumberFormat;

import junit.framework.*;

import gate.*;
import gate.creole.SerialAnalyserController;
import gate.util.Files;
import gate.util.Err;
import gate.creole.ANNIEConstants;

//import org.w3c.www.mime.*;


/** Test class for XML facilities
  *
  */
public class TestXml extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestXml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  public void testGateDocumentToAndFromXmlWithDifferentKindOfFormats()
                                                               throws Exception{
    List urlList = new LinkedList();
    List urlDescription = new LinkedList();
    URL url = null;

    url = Gate.getUrl("tests/xml/xces.xml");
    assertTrue("Coudn't create a URL object for tests/xml/xces.xml ", url != null);
    urlList.add(url);
    urlDescription.add(" an XML document ");

    url = Gate.getUrl("tests/xml/Sentence.xml");
    assertTrue("Coudn't create a URL object for tests/xml/Sentence.xml",
                                                         url != null);
    urlList.add(url);
    urlDescription.add(" an XML document ");

    url = Gate.getUrl("tests/html/test1.htm");
    assertTrue("Coudn't create a URL object for tests/html/test.htm",url != null);
    urlList.add(url);
    urlDescription.add(" an HTML document ");

    url = Gate.getUrl("tests/rtf/Sample.rtf");
    assertTrue("Coudn't create a URL object for defg ",url != null);
    urlList.add(url);
    urlDescription.add(" a RTF document ");


    url = Gate.getUrl("tests/email/test2.eml");
    assertTrue("Coudn't create a URL object for defg ",url != null);
    urlList.add(url);
    urlDescription.add(" an EMAIL document ");

    Iterator iter = urlList.iterator();
    Iterator descrIter = urlDescription.iterator();
    while(iter.hasNext()){
      runCompleteTestWithAFormat((URL) iter.next(),(String)descrIter.next());
    }// End While


  }// testGateDocumentToAndFromXmlWithDifferentKindOfFormats

  private void runCompleteTestWithAFormat(URL url, String urlDescription)
                                                             throws Exception{
    // Load the xml Key Document and unpack it
    gate.Document keyDocument = null;

    FeatureMap params = Factory.newFeatureMap();
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME, url);
    params.put(Document.DOCUMENT_MARKUP_AWARE_PARAMETER_NAME, "false");
    keyDocument = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    assertTrue("Coudn't create a GATE document instance for " +
            url.toString() +
            " Can't continue." , keyDocument != null);

    gate.DocumentFormat keyDocFormat = null;
    keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      keyDocument, keyDocument.getSourceUrl()
    );

    assertTrue("Fail to recognize " +
            url.toString() +
            " as being " + urlDescription + " !", keyDocFormat != null);

    // Unpack the markup
    keyDocFormat.unpackMarkup(keyDocument);
    // Verfy if all annotations from the default annotation set are consistent
    gate.corpora.TestDocument.verifyNodeIdConsistency(keyDocument);

    // Save the size of the document and the number of annotations
    long keyDocumentSize = keyDocument.getContent().size().longValue();
    int keyDocumentAnnotationSetSize = keyDocument.getAnnotations().size();


    // Export the Gate document called keyDocument as  XML, into a temp file,
    // using UTF-8 encoding
    File xmlFile = null;
    xmlFile = Files.writeTempFile(keyDocument.toXml(),"UTF-8");
    assertTrue("The temp GATE XML file is null. Can't continue.",xmlFile != null);
/*
    // Prepare to write into the xmlFile using UTF-8 encoding
    OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(xmlFile),"UTF-8");
    // Write (test the toXml() method)
    writer.write(keyDocument.toXml());
    writer.flush();
    writer.close();
*/
    // Load the XML Gate document form the tmp file into memory
    gate.Document gateDoc = null;
    gateDoc = gate.Factory.newDocument(xmlFile.toURL());

    assertTrue("Coudn't create a GATE document instance for " +
                xmlFile.toURL().toString() +
                " Can't continue." , gateDoc != null);

    gate.DocumentFormat gateDocFormat = null;
    gateDocFormat =
            DocumentFormat.getDocumentFormat(gateDoc,gateDoc.getSourceUrl());

    assertTrue("Fail to recognize " +
      xmlFile.toURL().toString() +
      " as being a GATE XML document !", gateDocFormat != null);

    gateDocFormat.unpackMarkup(gateDoc);
    // Verfy if all annotations from the default annotation set are consistent
    gate.corpora.TestDocument.verifyNodeIdConsistency(gateDoc);

    // Save the size of the document snd the number of annotations
    long gateDocSize = keyDocument.getContent().size().longValue();
    int gateDocAnnotationSetSize = keyDocument.getAnnotations().size();

    assertTrue("Exporting as GATE XML resulted in document content size lost." +
      " Something went wrong.", keyDocumentSize == gateDocSize);

    assertTrue("Exporting as GATE XML resulted in annotation lost." +
      " No. of annotations missing =  " +
      Math.abs(keyDocumentAnnotationSetSize - gateDocAnnotationSetSize),
      keyDocumentAnnotationSetSize == gateDocAnnotationSetSize);

    //Don't need tmp Gate XML file.
    xmlFile.delete();
  }//runCompleteTestWithAFormat

  /** A test */
  public void testUnpackMarkup() throws Exception{
    // create the markupElementsMap map
    Map markupElementsMap = null;
    gate.Document doc = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("S","Sentence");
    markupElementsMap.put ("s","Sentence");
    */
    // Create the element2String map
    Map anElement2StringMap = null;
    anElement2StringMap = new HashMap();
    // Populate it
    anElement2StringMap.put("S","\n");
    anElement2StringMap.put("s","\n");

    doc = gate.Factory.newDocument(Gate.getUrl("tests/xml/xces.xml"));
 //doc = gate.Factory.newDocument(new URL("file:///z:/gu.xml"));

    AnnotationSet annotSet = doc.getAnnotations(
                        GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    assertEquals("For "+doc.getSourceUrl()+" the number of annotations"+
    " should be:758",758,annotSet.size());

    gate.corpora.TestDocument.verifyNodeIdConsistency(doc);
  } // testUnpackMarkup()

  /*
   * This method runs ANNIE with defaults on a document, then saves
   * it as a GATE XML document and loads it back. All the annotations on the
   * loaded document should be the same as the original ones.
   *
   * It also verifies if the matches feature still holds after an export/import to XML
   */
  public void testAnnotationConsistencyForSaveAsXml()throws Exception{
    // Load a document from the test repository
    //Document origDoc = gate.Factory.newDocument(Gate.getUrl("tests/xml/gateTestSaveAsXML.xml"));
    String testDoc = gate.util.Files.getGateResourceAsString("gate.ac.uk/tests/xml/gateTestSaveAsXML.xml");
    Document origDoc = gate.Factory.newDocument(testDoc);


    // Load ANNIE with defaults and run it on the document
    SerialAnalyserController annie = loadANNIEWithDefaults();
    assertTrue("ANNIE not loaded!", annie != null);
    Corpus c = Factory.newCorpus("test");
    c.add(origDoc);
    annie.setCorpus(c);
    annie.execute();

    // SaveAS XML and reload the document into another GATE doc
    // Export the Gate document called origDoc as XML, into a temp file,
    // using UTF-8 encoding
    File xmlFile = Files.writeTempFile(origDoc.toXml(),"UTF-8");

    Document reloadedDoc = gate.Factory.newDocument(xmlFile.toURL());
    System.out.println("Saving to temp file :" + xmlFile.toURL());

    // Verify if the annotations are identical in the two docs.
    Map origAnnotMap = buildID2AnnotMap(origDoc);
    Map reloadedAnnMap = buildID2AnnotMap(reloadedDoc);

    //Verifies if the reloaded annotations are the same as the original ones
    verifyIDConsistency(origAnnotMap, reloadedAnnMap);

    // Build the original Matches map
    // ID  -> List of IDs
    Map origMatchesMap = buildMatchesMap(origDoc);
    // Verify the consistency of matches
    // Compare every orig annotation pointed by the MatchesMap with the reloadedAnnot
    // extracted from the reloadedMAp
    for(Iterator it = origMatchesMap.keySet().iterator(); it.hasNext();){
      Integer id = (Integer)it.next();
      Annotation origAnnot = (Annotation) origAnnotMap.get(id);
      assertTrue("Couldn't find an original annot with ID=" + id, origAnnot != null);
      Annotation reloadedAnnot = (Annotation) reloadedAnnMap.get(id);
      assertTrue("Couldn't find a reloaded annot with ID=" + id, reloadedAnnot != null);
      compareAnnot(origAnnot,reloadedAnnot);
      // Iterate through the matches list and repeat the comparison
      List matchesList = (List) origMatchesMap.get(id);
      for (Iterator itList = matchesList.iterator(); itList.hasNext();){
        Integer matchId = (Integer) itList.next();
        Annotation origA = (Annotation) origAnnotMap.get(matchId);
        assertTrue("Couldn't find an original annot with ID=" + matchId, origA != null);
        Annotation reloadedA = (Annotation) reloadedAnnMap.get(matchId);
        assertTrue("Couldn't find a reloaded annot with ID=" + matchId, reloadedA != null);
        compareAnnot(origA, reloadedA);
      }// End for
    }// End for

  }// End testAnnotationIDConsistencyForSaveAsXml

  /**
   * Builds a Map based on the matches feature of some annotations. The goal is to
   * use this map to validate the annotations from the reloaded document.
   * In case no Annot has the matches feat, will return an Empty MAP
   * @param doc The document of which annotations will be used to construct the map
   * @return A Map from Annot ID -> Lists of Annot IDs
   */
  private Map buildMatchesMap(Document doc){
    Map matchesMap = new HashMap();
    // Scan the default annotation set
    AnnotationSet annotSet = doc.getAnnotations();

    helperBuildMatchesMap(annotSet, matchesMap);
    // Scan all named annotation sets
    if (doc.getNamedAnnotationSets() != null){
      for ( Iterator namedAnnotSetsIter = doc.getNamedAnnotationSets().values().iterator();
                                                                namedAnnotSetsIter.hasNext(); ){
        helperBuildMatchesMap((gate.AnnotationSet) namedAnnotSetsIter.next(), matchesMap);
      }// End while
    }// End if
    return matchesMap;
  }// End of buildMatchesMap()

  /**
   * This is a helper metod. It scans an annotation set and adds the ID of the annotations
   * which have the matches feature to the map.
   * @param sourceAnnotSet  The annotation set investigated
   * @param aMap
   */
  private void helperBuildMatchesMap(AnnotationSet sourceAnnotSet, Map aMap ){

    for (Iterator it = sourceAnnotSet.iterator(); it.hasNext();){
      Annotation a = (Annotation) it.next();
      FeatureMap aFeatMap = a.getFeatures();
      // Skip those annotations who don't have features
      if (aFeatMap == null) continue;
      // Extract the matches feat
      List matchesVal = (List) aFeatMap.get("matches");
      if (matchesVal == null) continue;
      Integer id = a.getId();
      aMap.put(id,matchesVal);
    }//End for

  }// End of helperBuildMatchesMap()


  /**
   * Verifies if the two maps hold annotations with the same ID. The only thing not checked
   * are the features, as some of them could be lost in the serialization/deserialization process
   * @param origAnnotMap A map by ID, containing the original annotations
   * @param reloadedAnnMap A map by ID, containing the recreated annotations
   */
  private void verifyIDConsistency(Map origAnnotMap, Map reloadedAnnMap) {
    assertEquals("Found a different number of annot in both documents.",
            origAnnotMap.keySet().size(), reloadedAnnMap.keySet().size());

//    List orig = new ArrayList(origAnnotMap.keySet());
//    Collections.sort(orig);
//    System.out.println("ORIG SET =" + orig);
//
//    List rel = new ArrayList(reloadedAnnMap.keySet());
//    Collections.sort(rel);
//    System.out.println("REL  SET =" + rel);
//

    for (Iterator it = origAnnotMap.keySet().iterator(); it.hasNext();){
      Integer id = (Integer) it.next();
      Annotation origAnn = (Annotation) origAnnotMap.get(id);
      Annotation reloadedAnnot = (Annotation) reloadedAnnMap.get(id);

      assertTrue("Annotation with ID="+ id +" was not found in the reloaded document.", reloadedAnnot != null);
      compareAnnot(origAnn, reloadedAnnot);

    }// End for
  }// End of verifyIDConsistency()

  /**
   * Thes if two annotatiosn are the same, except their features.
   * @param origAnn
   * @param reloadedAnnot
   */
  private void compareAnnot(Annotation origAnn, Annotation reloadedAnnot) {
    assertTrue("Found original and reloaded annot without the same ID!",
            origAnn.getId().equals(reloadedAnnot.getId()));
    assertTrue("Found original and reloaded annot without the same TYPE!\n"+
               "Original was ["+origAnn.getType()+"] and reloaded was ["+reloadedAnnot.getType()+"].",
            origAnn.getType().equals(reloadedAnnot.getType()));
    assertTrue("Found original and reloaded annot without the same START offset!",
            origAnn.getStartNode().getOffset().equals(reloadedAnnot.getStartNode().getOffset()));
    assertTrue("Found original and reloaded annot without the same END offset!",
            origAnn.getEndNode().getOffset().equals(reloadedAnnot.getEndNode().getOffset()));
  }// End of compareAnnot()


  private Map addAnnotSet2Map(AnnotationSet annotSet, Map id2AnnMap){
    for (Iterator it = annotSet.iterator(); it.hasNext();){
      Annotation a = (Annotation) it.next();
      Integer id = a.getId();

      assertTrue("Found two annotations(one with type = " + a.getType() +
              ")with the same ID=" + id, !id2AnnMap.keySet().contains(id));

      id2AnnMap.put(id, a);
    }// End for
    return id2AnnMap;
  }

  /**
   * Scans a target Doc for all Annotations and builds a map (from anot ID to annot) in the process
   * I also checks to see if there are two annotations with the same ID.
   * @param aDoc The GATE doc to be scaned
   * @return a Map ID2Annot
   */
  private Map buildID2AnnotMap(Document aDoc){
    Map id2AnnMap = new HashMap();
    // Scan the default annotation set
    AnnotationSet annotSet = aDoc.getAnnotations();
    addAnnotSet2Map(annotSet, id2AnnMap);
    // Scan all named annotation sets
    if (aDoc.getNamedAnnotationSets() != null){
      for ( Iterator namedAnnotSetsIter = aDoc.getNamedAnnotationSets().values().iterator();
                                                                namedAnnotSetsIter.hasNext(); ){

        addAnnotSet2Map((gate.AnnotationSet) namedAnnotSetsIter.next(), id2AnnMap);
      }// End while
    }// End if
    return id2AnnMap;
  }// End of buildID2AnnotMap()

  /**
   * Load ANNIE with defaults
   * @return
   */
  private SerialAnalyserController loadANNIEWithDefaults(){
    FeatureMap params = Factory.newFeatureMap();
    SerialAnalyserController sac = null;
    try{
      // Create a serial analyser
      sac = (SerialAnalyserController)
          Factory.createResource("gate.creole.SerialAnalyserController",
                                 Factory.newFeatureMap(),
                                 Factory.newFeatureMap(),
                                 "ANNIE_" + Gate.genSym());
      // Load each PR as defined in gate.creole.ANNIEConstants.PR_NAMES
      for(int i = 0; i < ANNIEConstants.PR_NAMES.length; i++){
      ProcessingResource pr = (ProcessingResource)
          Factory.createResource(ANNIEConstants.PR_NAMES[i], params);
        // Add the PR to the sac
        sac.add(pr);
      }// End for

    }catch(gate.creole.ResourceInstantiationException ex){
      ex.printStackTrace(Err.getPrintWriter());
    }
    return sac;
  }// End of LoadANNIEWithDefaults()


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
