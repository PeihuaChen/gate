/*
 * FlexibleGazetteer.java
 *
 * Copyright (c) 2004, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Niraj Aswani 02/2002
 *
 */

package gate.creole.gazetteer;

import java.util.*;
import gate.util.*;
import gate.*;
import gate.creole.*;

/**
 * <p>Title: Flexible Gazetteer </p>
 * <p> The Flexible Gazetteer provides users with the flexibility to choose </p>
 * <p> their own customized input and an external Gazetteer. For example, </p>
 * <p> the user might want to replace words in the text with their base </p>
 * <p> forms (which is an output of the Morphological Analyser) or to segment </p>
 * <p> a Chinese text (using the Chinese Tokeniser) before running the </p>
 * <p> Gazetteer on the Chinese text. </p>
 *
 * <p> The Flexible Gazetteer performs lookup over a document based on the  </p>
 * <p> values of an arbitrary feature of an arbitrary annotation type, by </p>
 * <p> using an externally provided gazetteer. It is important to use an </p>
 * <p> external gazetteer as this allows the use of any type of gazetteer </p>
 * <p> (e.g. an Ontological gazetteer). </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FlexibleGazetteer
    extends AbstractLanguageAnalyser
    implements ProcessingResource {

  /**
   * Constructor
   */
  public FlexibleGazetteer() {
    changedNodes = new ArrayList();
  }

  /** Does the actual loading and parsing of the lists. This method must be
   * called before the gazetteer can be used
   */
  public Resource init() throws ResourceInstantiationException {

    if (listsURL == null) {
      throw new ResourceInstantiationException(
	  "No URL provided for gazetteer creation!");
    }

    if (gazetteerClassName == null) {
      throw new ResourceInstantiationException(
	  "No Gazetter Name provided");
    }

    return this;
  }

  /**
   * This method runs the gazetteer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {
    fireProgressChanged(0);
    fireStatusChanged("Checking Document...");
    if (document == null) {
      throw new ExecutionException(
	  "No document to process!"
	  );
    }

    fireStatusChanged("Creating temporary Document...");
    StringBuffer newdocString = new StringBuffer(document.getContent().toString());
    Document tempDoc = null;
    boolean chineseSplit = false;

    if (inputFeatureNames == null || inputFeatureNames.size() == 0) {
      inputFeatureNames = new ArrayList();
    }

    Iterator tokenIter = getTokenIterator(document, annotationSetName);
    long totalDeductedSpaces = 0;
    fireStatusChanged("Replacing contents with the feature value...");

    outer:while (tokenIter.hasNext()) {
      Annotation currentToken = (Annotation) tokenIter.next();

      // check if it is a chinesesplit
      // if it is, replace no space character with a single space
      if (currentToken.getType().equals(ANNIEConstants.
					SPACE_TOKEN_ANNOTATION_TYPE) &&
	  ( (String) (currentToken.getFeatures().get(ANNIEConstants.
	  TOKEN_KIND_FEATURE_NAME))).equals("ChineseSplit")) {

	// for chinese split startnode and end node are same
	long startOffset = currentToken.getStartNode().getOffset().
	    longValue();

	// because we are adding a space in place of chinesesplit
	// the endoffset will become newStartOffset + 1
	long newStartOffset = startOffset - totalDeductedSpaces;
	long newEndOffset = newStartOffset + 1;
	NodePosition newNode = new NodePosition(startOffset, startOffset,
						newStartOffset, newEndOffset,
						totalDeductedSpaces);
	chineseSplit = true;

	// here is the addition of space in the document
	totalDeductedSpaces--;
	changedNodes.add(newNode);
	newdocString = newdocString.insert( (int) newStartOffset, ' ');
	continue outer;
      }

      // search in the provided inputFeaturesNames
      // if the current token has a feature value that user
      // wants to paste on and replace the original string of the token
      inner:for (int i = 0; i < inputFeatureNames.size(); i++) {
	String[] keyVal = ( (String) (inputFeatureNames.get(i))).split("[.]");

	if (keyVal.length == 2) {
	  // val is the feature name
	  // key is the annotationName
	  if (currentToken.getType().equals(keyVal[0])) {
	    FeatureMap features = currentToken.getFeatures();
	    String newTokenValue = (String) (features.get(keyVal[1]));

	    // what if provided feature doesnot exist
	    if (newTokenValue == null) {
	      continue;

	    }
	    else {
	      // feature value found so we need to replace it
	      // find the start and end offsets for this token
	      long startOffset = currentToken.getStartNode().getOffset().
		  longValue();
	      long endOffset = currentToken.getEndNode().getOffset().
		  longValue();

	      // what is the actual string
	      String actualString = (String) (features.get(ANNIEConstants.
		  TOKEN_STRING_FEATURE_NAME));

	      // if the feature value and the actual string both are same
	      // we don't need to replace it
	      if (actualString.equals(newTokenValue)) {
		// there is no need to change anything for this
		break inner;
	      }

	      // let us find the difference between the lengths of the
	      // actual string and the newTokenValue
	      long lengthDifference = actualString.length() -
		  newTokenValue.length();

	      // so lets find the new startOffset and endOffset
	      long newStartOffset = startOffset - totalDeductedSpaces;
	      long newEndOffset = newStartOffset + newTokenValue.length();

	      // and make the entry for this
	      NodePosition newNode = new NodePosition(startOffset,
		  endOffset,
		  newStartOffset, newEndOffset, totalDeductedSpaces);
	      changedNodes.add(newNode);
	      // how many spaces have been added or removed till the current
	      // position of the token
	      totalDeductedSpaces += lengthDifference;

	      // and finally replace the actual string in the document
	      // with the new document
	      newdocString = newdocString.replace( (int) newStartOffset,
						  (int) newStartOffset +
						  actualString.length(),
						  newTokenValue);
	      break inner;
	    }
	  }
	}
      }
    }

    fireStatusChanged("New Document to be processed with Gazetteer...");
    try {
      FeatureMap params = Factory.newFeatureMap();
      params.put("stringContent", newdocString.toString());
      FeatureMap features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      tempDoc = (Document) Factory.createResource("gate.corpora.DocumentImpl",
						  params, features);
    }
    catch (ResourceInstantiationException rie) {
      throw new ExecutionException("Temporary document cannot be created");
    }

    // lets create the gazetteer based on the provided gazetteer name
    FeatureMap params = Factory.newFeatureMap();
    params.put("listsURL", listsURL);
    params.put("document", tempDoc);
    params.put("annotationSetName", this.annotationSetName);
    params.put("encoding", encoding);
    params.put("caseSensitive", this.caseSensitive);
    if (gazetteerClassName.equals("gate.creole.gazetteer.DefaultGazetteer")) {
      params.put("wholeWordsOnly", this.wholeWordsOnly);
    }
    FeatureMap features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);
    try {
      gazetteerInst = (Gazetteer) (Factory.createResource(gazetteerClassName,
	  params, features, null));
    }
    catch (Exception e) {
      throw new ExecutionException(
	  "gazetteer with name : " + gazetteerClassName + " does not exist");
    }

    fireStatusChanged("Executing Gazetteer...");
    gazetteerInst.execute();

    // now the tempDoc has been looked up, we need to shift the tokens from
    // this temp document to the original document
    fireStatusChanged("Transfering new tages to the original one...");
    Iterator tokensIter = getTokenIterator(tempDoc, annotationSetName);
    AnnotationSet original = document.getAnnotations();
    long totalSpaceAdded = 0;
    long difference = 0;

    int foundNode = -1;
    while (tokensIter.hasNext()) {
      Annotation currentToken = (Annotation) (tokensIter.next());
      long startOffset = currentToken.getStartNode().getOffset().longValue();
      long endOffset = currentToken.getEndNode().getOffset().longValue();

      // search through the changedNodes and if it is found we will have to
      // find the new offsets
      int i = foundNode + 1;
      boolean found = false;
      inner1:for (; i < changedNodes.size(); i++) {

	NodePosition tempNode = (NodePosition) (changedNodes.get(i));

	// all the nodes are in the sorted order based on there offsets
	// so if we reach beyond the position of the current text
	// under consideration, simply terminate the loop
	if (tempNode.getNewStartNode() > startOffset) {
	  // so we lets point to the node whose startOffset
	  // is less than the startOffset of the current node
	  // this will allow us to find out how many
	  // extra spaces were added or removed before the current token
	  i = i - 1;
	  break inner1;
	}

	// how do we know if we want to change the offset
	if (tempNode.getNewStartNode() == startOffset) {
	  // yes it is available

	  // lets find the end node
	  int k = i;
	  for (;
	       k >= 0 && k < changedNodes.size() &&
	       endOffset >
	       ( (NodePosition) changedNodes.get(k)).getNewStartNode(); k++)
	    ;
	  long spacesToAdd = 0;
	  if (k - 1 == i && k - 1 >= 0) {
	    spacesToAdd = (tempNode.getOldEndNode() - tempNode.getNewEndNode());
	  }
	  else if (k - 1 < 0) {
	    spacesToAdd = 0;
	  }
	  else {
	    spacesToAdd = ( (NodePosition) changedNodes.get(k - 1)).
		getOldEndNode() -
		( (NodePosition) changedNodes.get(k - 1)).getNewEndNode();
	  }

	  // and how many to be added before the endnode
	  // as any look up notation can be for the text with one or more tokens
	  FeatureMap newFeatureMap = currentToken.getFeatures();
	  try {

	    original.add(new Long(startOffset +
				  (tempNode.getOldStartNode() -
				   tempNode.getNewStartNode())),
			 new Long(endOffset + spacesToAdd),
			 //new Long(endOffset + (tempNode.getOldEndNode()
			 //          - tempNode.getNewEndNode())),
			 ANNIEConstants.LOOKUP_ANNOTATION_TYPE,
			 newFeatureMap);

	  }
	  catch (InvalidOffsetException ioe) {
	    throw new ExecutionException("Offset Error");
	  }
	  found = true;
	  foundNode = i;
	  break inner1;
	}
      }

      if (!found) {
	long totalStartSpaces = 0;
	long totalEndSpaces = 0;

	// check if we have reached at the end of the changedNodes
	// if yes we need to find the last node
	i = (changedNodes.size() == i) ? i - 1 : i;

	    // lets find the end node
	int k = i;
	for (;
	     k > 0 && k < changedNodes.size() &&
	     endOffset > ( (NodePosition) changedNodes.get(k)).getNewStartNode();
	     k++)
	  ;
	long spacesToAdd = 0;
	if (k - 1 == i && k - 1 >= 0) {
	  spacesToAdd = ( ( (NodePosition) changedNodes.get(i)).getOldEndNode() -
			 ( (NodePosition) changedNodes.get(i)).getNewEndNode());
	}
	else if (k - 1 < 0) {
	  spacesToAdd = 0;
	}
	else {
	  spacesToAdd = ( (NodePosition) changedNodes.get(k - 1)).
	      getOldEndNode() -
	      ( (NodePosition) changedNodes.get(k - 1)).getNewEndNode();
	}

	if (i >= 0) {
	  //totalStartSpaces = ((NodePosition)
	      // changedNodes.get(i)).getOldStartNode()
	      // - ((NodePosition) changedNodes.get(i)).getNewStartNode();
	  totalStartSpaces = ( (NodePosition) changedNodes.get(i)).
	      getOldEndNode() -
	      ( (NodePosition) changedNodes.get(i)).getNewEndNode();
	  //totalEndSpaces = ((NodePosition)
	     // changedNodes.get(i)).getOldEndNode() -
	     // ((NodePosition) changedNodes.get(i)).getNewEndNode();
	  totalEndSpaces = spacesToAdd;
	  foundNode = i;
	}

	// no it is not available
	FeatureMap newFeatureMap = currentToken.getFeatures();
	try {
	  original.add(new Long(startOffset + totalStartSpaces),
		       new Long(endOffset + totalEndSpaces),
		       ANNIEConstants.LOOKUP_ANNOTATION_TYPE,
		       newFeatureMap);
	}
	catch (InvalidOffsetException ioe) {
	  throw new ExecutionException("Offset Error");
	}

      }
    }

    // now remove the newDoc
    Factory.deleteResource(tempDoc);
    fireProcessFinished();
  }

  /**
   * Sets the URL for lists file
   * @param url
   */
  public void setListsURL(java.net.URL url) {
    this.listsURL = url;
  }

  /**
   * Returns the URL for lists file
   * @return
   */
  public java.net.URL getListsURL() {
    return this.listsURL;
  }

  /**
   * Sets the encoding of the document
   * @param enc
   */
  public void setEncoding(String enc) {
    this.encoding = enc;
  }

  /**
   * Returns the encoding
   * @return
   */
  public String getEncoding() {
    return this.encoding;
  }

  /**
   * Sets the value which tells if gazetteer should work in case sensitive mode
   * @param val
   */
  public void setCaseSensitive(Boolean val) {
    this.caseSensitive = val;
  }

  /**
   * Retursn the boolean value which tells if gazetteer is working in the
   * case sensitive mode
   * @return
   */
  public Boolean getCaseSensitive() {
    return this.caseSensitive;
  }

  /**
   * Sets the value which tells if gazetteer should look up for whole words only
   * @param val
   */
  public void setWholeWordsOnly(Boolean val) {
    this.wholeWordsOnly = val;
  }

  /**
   * Returns the boolean value which tells if gazetter is set to look for
   * only whole words entry
   * @return
   */
  public Boolean getWholeWordsOnly() {
    return this.wholeWordsOnly;
  }

  /**
   * Sets the document to work on
   * @param doc
   */
  public void setDocument(gate.Document doc) {
    this.document = doc;
  }

  /**
   * Returns the document set up by user to work on
   * @return
   */
  public gate.Document getDocument() {
    return this.document;
  }

  /**
   * sets the annotationSetName
   * @param annName
   */
  public void setAnnotationSetName(String annName) {
    this.annotationSetName = annName;
  }

  /**
   * Returns the annotationSetName
   * @return
   */
  public String getAnnotationSetName() {
    return this.annotationSetName;
  }


  /**
   * Sets the gazetteer class name that should be used to perform lookup
   * on the provided document
   * @param cname
   */
  public void setGazetteerClassName(String cname) {
    this.gazetteerClassName = cname;
  }

  /**
   * Returns the gazetteer class name that has been set up to perform lookup
   * on the provided document
   * @return
   */
  public String getGazetteerClassName() {
    return this.gazetteerClassName;
  }

  /**
   * Feature names for example: Token.string, Token.root etc... Values of these
   * features should be used to replace the actual string of these features. This
   * method allows a user to set the name of such features
   * @param inputs
   */
  public void setInputFeatureNames(java.util.List inputs) {
    this.inputFeatureNames = inputs;
  }

  /**
   * Returns the feature names that are provided by the user to use their values
   * to replace their actual strings in the document
   * @return
   */
  public java.util.List getInputFeatureNames() {
    return this.inputFeatureNames;
  }

  /**
   * This method takes the document and the annotationSetName and then creates
   * a interator for the annotations available in the document under the
   * provided annotationSetName
   * @param doc
   * @param annotationSetName
   * @return
   */
  public Iterator getTokenIterator(gate.Document doc, String annotationSetName) {
    AnnotationSet inputAs = (annotationSetName == null) ? doc.getAnnotations() :
	doc.getAnnotations(annotationSetName);
    List tokens = new ArrayList(inputAs.get());
    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(tokens, offsetComparator);
    Iterator tokenIter = tokens.iterator();
    return tokenIter;
  }

  // Gazetteer Parameters
  private java.net.URL listsURL;
  private String encoding;
  private Boolean caseSensitive;
  private Boolean wholeWordsOnly;

  // Gazetteer Runtime parameters
  private gate.Document document;
  private java.lang.String annotationSetName;

  // Flexible Gazetteer parameter
  private Gazetteer gazetteerInst;
  private String gazetteerClassName;
  private java.util.List inputFeatureNames;

  // parameters required within the program
  private ArrayList changedNodes;
}
