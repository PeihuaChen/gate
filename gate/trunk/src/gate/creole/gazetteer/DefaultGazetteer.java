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
public class DefaultGazetteer implements Runnable {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Build a gazetter using the default lists from the agte resources
    * {@see init()}
    */
  public DefaultGazetteer()
         throws IOException, GazetteerException
  {
    this("gate/resources/creole/gazeteer/default/", "lists.def");
  }

  /** Builds a gazetter reading the definitions of the lists from the specified
    * file.
    * @param fileName a string representing the name of the file
    * {@see init()}
    */
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

  /** Builds a gazetteer reading the lists from the classpath.
    * @param resourcePath the path to the file containing the definitions of the
    * lists.
    * @param resourceName the name of the file containing the definitions of the
    * lists.
    * {@see init()}
    */
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


  /** Does the actual loading and prsing of the lists. This method must be
    * called before the gazetteer can be used
    */
  public void init()throws IOException, GazetteerException {
    BufferedReader bReader = new BufferedReader(reader);
    String line = bReader.readLine();
    String toParse = "";

    while (line != null) {
      if(line.endsWith("\\")) {
        toParse += line.substring(0,line.length()-1);
      } else {
        toParse += line;
        fireStatusChangedEvent("Reading " + toParse);
        readList(toParse, true);
        toParse = "";
      }
      line = bReader.readLine();
    }

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

    if(fromResource){
      listReader = new BufferedReader(new InputStreamReader(
                   ClassLoader.getSystemResourceAsStream(resPath + listName)));
    } else {
      listReader = new BufferedReader(new FileReader(resPath + listName));
    }

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

  /** This method runs this gazetteer over a provided input.
    * @param document the document this gazetteer should run on
    * @param runInNewThread whether the gazetteer should start a new thread for
    * its processing needs or it should run in the thread of the calling method
    */
  public void doLookup(Document document, boolean runInNewThread){
    doLookup(document, document.getAnnotations(), runInNewThread);
  } // doLookup

  /** This method runs this gazetteer over a provided input.
    * @param document the document this gazetteer should run on
    * @param runInNewThread whether the gazetteer should start a new thread for
    * @param annotations the annotation set which should ben used for adding the
    * new annotations generated by this gazetteer.
    * its processing needs or it should run in the thread of the calling method
    */
  public void doLookup(Document document,
                       AnnotationSet annotations,
                       boolean runInNewThread){
    this.doc =  document;
    this.annotations = annotations;
    if(runInNewThread){
      Thread thread = new Thread(this);
      thread.start();
    } else run();
  } // doLookup

  /** The method that does the actual input. This method should never be called
    * by the user; the {@link doLookup()} methodshould be used instead.
    */
  public void run(){
    fireStatusChangedEvent("Doing lookup in " +
                                          doc.getSourceUrl().getFile() + "...");
    String content = doc.getContent().toString();
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
              annotations.add(new Long(matchedRegionStart),
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
        fireProgressChangedEvent((100 * charIdx )/ length );
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
          annotations.add(new Long(matchedRegionStart),
                          new Long(matchedRegionEnd + 1),
                          "Lookup",
                          fm);
        } catch(InvalidOffsetException ioe) {
          throw new LuckyException(ioe.toString());
        }
      }//while(lookupIter.hasNext())
    }
    fireProcessFinishedEvent();
    fireStatusChangedEvent("Tokenisation complete!");
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

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener) {
    myStatusListeners.add(listener);
  }

  public void removeStatusListener(StatusListener listener) {
    myStatusListeners.remove(listener);
  }

  protected void fireStatusChangedEvent(String text) {
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  //ProcessProgressReporter implementation
  public void addProcessProgressListener(ProgressListener listener) {
    myProgressListeners.add(listener);
  }

  public void removeProcessProgressListener(ProgressListener listener) {
    myProgressListeners.remove(listener);
  }

  protected void fireProgressChangedEvent(int i) {
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).progressChanged(i);
  }

  protected void fireProcessFinishedEvent() {
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).processFinished();
  }
  //ProcessProgressReporter implementation ends here

  /** The path to the resource directory containing the files used to define this
    * gazetteer.
    */
  String resPath = null;

  /** If this gazetteer is loaded from the classpath instead of files */
  boolean fromResource = false;

  /** The initial state of the FSM that backs this gazetteer */
  FSMState initialState;

  /** A set containing all the states of the FSM backing the gazetteer */
  Set fsmStates = new HashSet();

  /** Used to store the document currently being parsed */
  Document doc;

  /** Used to store the annotation set currently being usede for the newly
    * generated annotations
    */
  AnnotationSet annotations;

  protected List myProgressListeners = new LinkedList();

  protected List myStatusListeners = new LinkedList();

  /** The reader used to build this gazetteer */
  protected Reader reader;

} // DefaultGazetteer
