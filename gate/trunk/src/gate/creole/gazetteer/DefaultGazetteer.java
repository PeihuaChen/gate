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
import gate.event.*;
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

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  /** Build a gazetter using the default lists from the agte resources
   * {@see init()}
   */
  public DefaultGazetteer(){
  }

  /** Does the actual loading and parsing of the lists. This method must be
   * called before the gazetteer can be used
   */
  public Resource init()throws ResourceInstantiationException{
    fsmStates = new HashSet();
    try{
      initialState = new FSMState(this);
      if(listsURL == null){
        throw new ResourceInstantiationException (
              "No URL provided for gazetteer creation!");
      }

      //find the number of lines
      Reader reader = new InputStreamReader(listsURL.openStream(), encoding);
      int linesCnt = 0;
      BufferedReader bReader = new BufferedReader(reader);
      String line = bReader.readLine();
      while (line != null) {
        linesCnt++;
        line = bReader.readLine();
      }
      bReader.close();

      //parse the file
      reader = new InputStreamReader(listsURL.openStream(), encoding);
      bReader = new BufferedReader(reader);
      line = bReader.readLine();
      String toParse = "";

      int lineIdx = 0;
      while (line != null) {
        if(line.endsWith("\\")) {
          toParse += line.substring(0,line.length()-1);
        } else {
          toParse += line;
          fireStatusChanged("Reading " + toParse);
          fireProgressChanged(lineIdx * 100 / linesCnt);
          lineIdx ++;
          readList(toParse, true);
          toParse = "";
        }
        line = bReader.readLine();
      }
      fireProcessFinished();
    }catch(IOException ioe){
      throw new ResourceInstantiationException(ioe);
    }catch(GazetteerException ge){
      throw new ResourceInstantiationException(ge);
    }
    return this;
  }

  /**
   * Resets this resource preparing it for a new run
   */
  public void reset(){
    document = null;
    annotationSetName = null;
  }

  /** Reads one lists (one file) of phrases
   *
   * @param listDesc the line from the definition file
   * @param add
   * @add if <b>true</b> will add the phrases found in the list to the ones
   *     recognised by this gazetter, if <b>false</b> the phrases found in the
   *     list will be removed from the list of phrases recognised by this
   *     gazetteer.
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
                            (new URL(listsURL, listName)).openStream(), encoding));

    Lookup lookup = new Lookup(majorType, minorType, languages);
    String line = listReader.readLine();
    while(null != line){
      if(add)addLookup(line, lookup);
      else removeLookup(line, lookup);
      line = listReader.readLine();
    }
  } // void readList(String listDesc)

  /** Adds one phrase to the list of phrases recognised by this gazetteer
   *
   * @param text the phrase to be added
   * @param lookup the description of the annotation to be added when this
   *     phrase is recognised
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
      else currentChar = (caseSensitive.booleanValue()) ?
                          new Character(text.charAt(i)) :
                          new Character(Character.toUpperCase(text.charAt(i))) ;
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
   *
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
  /**    */
  public FeatureMap getFeatures(){
    return features;
  } // getFeatures

  /**    */
  public void setFeatures(FeatureMap features){
    this.features = features;
  } // setFeatures



  /**
   * This method runs the gazetteer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void run(){
    AnnotationSet annotationSet;
    //check the input
    if(document == null) {
      executionException = new ExecutionException(
        "No document to process!"
      );
      return;
    }

    if(annotationSetName == null ||
       annotationSetName.equals("")) annotationSet = document.getAnnotations();
    else annotationSet = document.getAnnotations(annotationSetName);

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
      else currentChar = (caseSensitive.booleanValue()) ?
                         new Character(content.charAt(charIdx)) :
                         new Character(Character.toUpperCase(
                                       content.charAt(charIdx)));
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


  /**
   * Sets the URL to be used for reading the Gazetteer lists
   *
   * @param newListsURLStr
   */
  /**
   * Gets the URL used for reading the lists of this Gazetteer
   */
  /**
   * Sets the document to be processed by the next run
   */
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }

  /**
   * Sets the AnnotationSet that will be used at the next run for the newly
   * produced annotations.
   */
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }

  /**    */
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }

  /**    */
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  /** The initial state of the FSM that backs this gazetteer
   */
  FSMState initialState;

  /** A set containing all the states of the FSM backing the gazetteer
   */
  Set fsmStates;

  protected FeatureMap features  = null;

  /** Used to store the document currently being parsed
   */
  protected Document document;

  /** Used to store the annotation set currently being used for the newly
   * generated annotations
   */
  protected String annotationSetName;

  /**    */
  private transient Vector progressListeners;
  /**    */
  private transient Vector statusListeners;
  private String encoding = "UTF-8";

  /**
   * The value of this property is the URL that will be used for reading the
   * lists dtaht define this Gazetteer
   */
  private java.net.URL listsURL;

  /**
   * Should this gazetteer be case sensitive. The default value is true.
   */
  private Boolean caseSensitive = new Boolean(true);

  /**    */
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  /**    */
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  /**    */
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  /**    */
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }
  /**    */
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setListsURL(java.net.URL newListsURL) {
    listsURL = newListsURL;
  }
  public java.net.URL getListsURL() {
    return listsURL;
  }
  public void setCaseSensitive(Boolean newCaseSensitive) {
    caseSensitive = newCaseSensitive;
  }
  public Boolean getCaseSensitive() {
    return caseSensitive;
  }

} // DefaultGazetteer
