/*
 * DefaultGazeteer.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 03/07/2000
 *
 * $Id$
 */

package gate.creole.gazetteer;

import java.io.*;
import java.util.*;
import java.net.*;

import gate.util.*;
import gate.creole.*;
import gate.gui.*;
import gate.*;

/** This component is responsible for doing lists lookup. The implementaion is
  * based on finite state machines.
  * The phrases to be recognised should be listed in a set of files, one for
  * each type of occurences.
  * The gazeteer is build with the information from a file that contains the set
  * of lists (which are files as well) and the associated type for each list.
  * The file defining the set of lists should have the following syntax:
  * each list definition should be written on its own line and should contain:
  * <ol>
  * <li>the file name (required) </li>
  * <li>the major type (required) </li>
  * <li>the minor type (optional)</li>
  * <li>the language(s) (optional) </li>
  * </ol>
  * The elements of each definition are separated by &quot;:&quot;.
  * The following is an example of a valid definition: <br>
  * <code>personmale.lst:person:male:english</code>
  * Each list file named in the lists definition file is just a list containing
  * one entry per line.
  * When this gazetter will be run over some input text (a Gate document) it
  * will generate annotations of type Lookup having the attributes specified in
  * the definition file.
  */
public class DefaultGazetteer extends AbstractProcessingResource
implements ProcessingResource {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Build a gazetter using the default lists from the agte resources
    * {@see init()}
    */
  public DefaultGazetteer(){
//    this("gate/resources/creole/gazeteer/default/", "lists.def");
  }

  /** Builds a gazetter reading the definitions of the lists from the specified
    * file.
    * @param fileName a string representing the name of the file
    * {@see init()}
    */
/*
  public DefaultGazetteer(String fileName) throws IOException,
                                                 FileNotFoundException,
                                                 GazetteerException
  {
    File defsFile = new File(fileName);
    resPath = defsFile.getParent();
    if(!(resPath.endsWith("/") ||
       resPath.endsWith("\\"))) resPath += "/";
    initialState = new FSMState(this);

    reader = new FileReader(defsFile);

  }//public DefaultGazeteer(String fileName)
*/
  /** Builds a gazetteer reading the lists from the classpath.
    * @param resourcePath the path to the file containing the definitions of the
    * lists.
    * @param resourceName the name of the file containing the definitions of the
    * lists.
    * {@see init()}
    */
/*
  public DefaultGazetteer(String resourcePath, String resourceName)
         throws IOException, GazetteerException
  {
    initialState = new FSMState(this);
    fromResource = true;
    if(resourcePath.endsWith("/") ||
       resourcePath.endsWith("\\")) resPath = resourcePath;
    else resPath = resourcePath + "/";
    reader = new InputStreamReader(Files.getResourceAsStream(
                                                      resPath + resourceName));
  } // public DefaultGazeteer(String resourcePath, String resourceName)

*/
  /** Does the actual loading and prsing of the lists. This method must be
    * called before the gazetteer can be used
    */
  public Resource init()throws ResourceInstantiationException{
    try{
      initialState = new FSMState(this);

      if(listsURLStr == null){
        String defaultListsURLStr = this.getClass().getResource(
                          Files.getResourcePath() +
                          "/creole/gazeteer/default/lists.def").toExternalForm();
        mainURL = new URL(defaultListsURLStr);
      }else mainURL = new URL(listsURLStr);
      Reader reader = new InputStreamReader(mainURL.openStream());

      BufferedReader bReader = new BufferedReader(reader);
      String line = bReader.readLine();
      String toParse = "";

      while (line != null) {
        if(line.endsWith("\\")) {
          toParse += line.substring(0,line.length()-1);
        } else {
          toParse += line;
          fireStatusChanged("Reading " + toParse);
          readList(toParse, true);
          toParse = "";
        }
        line = bReader.readLine();
      }
    }catch(IOException ioe){
      throw new ResourceInstantiationException(ioe);
    }catch(GazetteerException ge){
      throw new ResourceInstantiationException(ge);
    }
    return this;
  }

  public void reset(){
    document = null;
    annotationSet = null;
  }

  /** Reads one lists (one file) of phrases
    * @param listDesc the line from the definition file
    * @add if <b>true</b> will add the phrases found in the list to the ones
    * recognised by this gazetter, if <b>false</b> the phrases found in the list
    * will be removed from the list of phrases recognised by this gazetteer.
    */
  void readList(String listDesc, boolean add) throws FileNotFoundException,
                                        IOException,
                                        GazetteerException{
    String listName, majorType, minorType, languages;
    int firstColon = listDesc.indexOf(':');
    int secondColon = listDesc.indexOf(':', firstColon + 1);
    int thirdColon = listDesc.indexOf(':', secondColon + 1);
    if(firstColon == -1){
      throw new GazetteerException("Invalid list definition: " + listDesc);
    }
    listName = listDesc.substring(0, firstColon);

    if(secondColon == -1){
      majorType = listDesc.substring(firstColon + 1);
      minorType = null;
      languages = null;
    } else {
      majorType = listDesc.substring(firstColon + 1, secondColon);
      if(thirdColon == -1) {
        minorType = listDesc.substring(secondColon + 1);
        languages = null;
      } else {
        minorType = listDesc.substring(secondColon + 1, thirdColon);
        languages = listDesc.substring(thirdColon + 1);
      }
    }
    BufferedReader listReader;

    listReader = new BufferedReader(new InputStreamReader(
                            (new URL(mainURL, listName)).openStream()));

    Lookup lookup = new Lookup(majorType, minorType, languages);
    String line = listReader.readLine();
    while(null != line){
      if(add)addLookup(line, lookup);
      else removeLookup(line, lookup);
      line = listReader.readLine();
    }
  } // void readList(String listDesc)

  /** Adds one phrase to the list of phrases recognised by this gazetteer
    * @param text the phrase to be added
    * @param lookup the description of the annotation to be added when this
    * phrase is recognised
    */
  public void addLookup(String text, Lookup lookup) {
    Character currentChar;
    FSMState currentState = initialState;
    FSMState nextState;
    Lookup oldLookup;
    boolean isSpace;

    for(int i = 0; i< text.length(); i++) {
      isSpace = Character.isWhitespace(text.charAt(i));
      if(isSpace) currentChar = new Character(' ');
      else currentChar = new Character(text.charAt(i));
      nextState = currentState.next(currentChar);
      if(nextState == null){
        nextState = new FSMState(this);
        currentState.put(currentChar, nextState);
        if(isSpace) nextState.put(new Character(' '),nextState);
      }
      currentState = nextState;
    } //for(int i = 0; i< text.length(); i++)

    currentState.addLookup(lookup);
    //Out.println(text + "|" + lookup.majorType + "|" + lookup.minorType);

  } // addLookup

  /** Removes one phrase to the list of phrases recognised by this gazetteer
    * @param text the phrase to be removed
    * @param lookup the description of the annotation associated to this phrase
    */
  public void removeLookup(String text, Lookup lookup) {
    Character currentChar;
    FSMState currentState = initialState;
    FSMState nextState;
    Lookup oldLookup;
    boolean isSpace;

    for(int i = 0; i< text.length(); i++) {
      isSpace = Character.isWhitespace(text.charAt(i));
      if(isSpace) currentChar = new Character(' ');
      else currentChar = new Character(text.charAt(i));
      nextState = currentState.next(currentChar);
      if(nextState == null) return;//nothing to remove
      currentState = nextState;
    } //for(int i = 0; i< text.length(); i++)
    currentState.removeLookup(lookup);
  } // removeLookup


  /** Returns a string representation of the deterministic FSM graph using
    * GML.
    */
  public String getFSMgml() {
    String res = "graph[ \ndirected 1\n";
    String nodes = "", edges = "";
    Iterator fsmStatesIter = fsmStates.iterator();
    while (fsmStatesIter.hasNext()){
      FSMState currentState = (FSMState)fsmStatesIter.next();
      int stateIndex = currentState.getIndex();
      nodes += "node[ id " + stateIndex +
               " label \"" + stateIndex;
             if(currentState.isFinal()){
              nodes += ",F\\n" + currentState.getLookupSet();
             }
             nodes +=  "\"  ]\n";
      edges += currentState.getEdgesGML();
    }
    res += nodes + edges + "]\n";
    return res;
  } // getFSMgml

  //no doc required: javadoc will copy it from the interface
  public FeatureMap getFeatures(){
    return features;
  } // getFeatures

  public void setFeatures(FeatureMap features){
    this.features = features;
  } // setFeatures



  /** The method that does the actual input. This method should never be called
    * by the user; the {@link doLookup()} methodshould be used instead.
    */
  public void run(){
    //check the input
    if(document == null) {
      executionException = new ExecutionException(
        "No document to parse!"
      );
      return;
    }

    if(annotationSet == null) annotationSet = document.getAnnotations();
    else if(annotationSet.getDocument() != document) {
      executionException = new ExecutionException(
        "The annotation set provided does not belong to the current document!"
      );
      return;
    }

    fireStatusChanged("Doing lookup in " +
                           document.getSourceUrl().getFile() + "...");
    String content = document.getContent().toString();
    int length = content.length();
    Character currentChar;
    FSMState currentState = initialState;
    FSMState nextState;
    FSMState lastMatchingState = null;
    int matchedRegionEnd = 0;
    int matchedRegionStart = 0;
    int charIdx = 0;
    int oldCharIdx = 0;
    FeatureMap fm;
    Lookup currentLookup;

    while(charIdx < length) {
      if(Character.isWhitespace(content.charAt(charIdx)))
        currentChar = new Character(' ');
      else currentChar = new Character(content.charAt(charIdx));
      nextState = currentState.next(currentChar);

      if(null == nextState) {
        //the matching stopped
        if(null != lastMatchingState &&
           !Character.isLetter(content.charAt(matchedRegionEnd + 1)) &&
           (matchedRegionStart == 0 ||
           !Character.isLetter(content.charAt(matchedRegionStart - 1))
           )){
          //let's add the new annotation(s)
          Iterator lookupIter = lastMatchingState.getLookupSet().iterator();

          while(lookupIter.hasNext()) {
            currentLookup = (Lookup)lookupIter.next();
            fm = Factory.newFeatureMap();
            fm.put("majorType", currentLookup.majorType);
            if(null != currentLookup.minorType) {
              fm.put("minorType", currentLookup.minorType);
              if(null != currentLookup.languages)
                fm.put("language", currentLookup.languages);
            }
            try {
              annotationSet.add(new Long(matchedRegionStart),
                              new Long(matchedRegionEnd + 1),
                              "Lookup",
                              fm);
            } catch(InvalidOffsetException ioe) {
              throw new LuckyException(ioe.toString());
            }

          }//while(lookupIter.hasNext())

        }

        lastMatchingState = null;
        charIdx = matchedRegionStart + 1;
        matchedRegionStart = charIdx;
        currentState = initialState;

      } else{//go on with the matching
        currentState = nextState;
        if(currentState.isFinal()) {
          matchedRegionEnd = charIdx;
          lastMatchingState = currentState;
        }
        charIdx ++;
      }
      if(charIdx - oldCharIdx > 256) {
        fireProgressChanged((100 * charIdx )/ length );
        oldCharIdx = charIdx;
      }
    } // while(charIdx < length)

    if(lastMatchingState != null) {

      Iterator lookupIter = lastMatchingState.getLookupSet().iterator();
      while(lookupIter.hasNext()) {
        currentLookup = (Lookup)lookupIter.next();
        fm = Factory.newFeatureMap();
        fm.put("majorType", currentLookup.majorType);
        if(null != currentLookup.minorType)
          fm.put("minorType", currentLookup.minorType);
        try{
          annotationSet.add(new Long(matchedRegionStart),
                          new Long(matchedRegionEnd + 1),
                          "Lookup",
                          fm);
        } catch(InvalidOffsetException ioe) {
          throw new GateRuntimeException(ioe.toString());
        }
      }//while(lookupIter.hasNext())
    }
    reset();
    fireProcessFinished();
    fireStatusChanged("Tokenisation complete!");
  } // run

  /*
  public static void main(String[] args){
    try{
      DefaultGazeteer dg = new
                          DefaultGazeteer("d:/tmp/gaztest/extension/lists.def");
      gate.fsm.TestFSM.showGraph(
                            "Tokeniser graph (deterministic)", dg.getFSMgml());

      Document doc = Factory.newDocument(
                          new URL("file:///d:/tmp/gaztest/extension/long.lst"));
      dg.doLookup(doc, false);
      Out.println(doc.getAnnotations());
    }catch(Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }
  }//public static void main(String[] args)
  */


  public void setListsURLStr(String newListsURLStr) {
    listsURLStr = newListsURLStr;
  }
  public String getListsURLStr() {
    return listsURLStr;
  }
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }
  public gate.Document getDocument() {
    return document;
  }
  public void setAnnotationSet(gate.AnnotationSet newAnnotationSet) {
    annotationSet = newAnnotationSet;
  }
  public gate.AnnotationSet getAnnotationSet() {
    return annotationSet;
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  /** The initial state of the FSM that backs this gazetteer */
  FSMState initialState;

  /** A set containing all the states of the FSM backing the gazetteer */
  Set fsmStates = new HashSet();


  protected FeatureMap features  = null;

  protected String listsURLStr = null;

  protected URL mainURL = null;

  /** Used to store the document currently being parsed */
  protected Document document;

  /** Used to store the annotation set currently being used for the newly
    * generated annotations
    */
  protected AnnotationSet annotationSet;

  private transient Vector progressListeners;
  private transient Vector statusListeners;
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

} // DefaultGazetteer
