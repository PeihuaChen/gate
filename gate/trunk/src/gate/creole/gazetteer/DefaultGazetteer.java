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
 * borislav popov 24/03/2002
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
public class DefaultGazetteer extends AbstractGazetteer {

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  private static final String CLASS = "CLASS";

  private static final String ONTOLOGY = "ONTOLOGY";

  public static final String
    DEF_GAZ_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    DEF_GAZ_ANNOT_SET_PARAMETER_NAME = "annotationSetName";

  public static final String
    DEF_GAZ_LISTS_URL_PARAMETER_NAME = "listsURL";

  public static final String
    DEF_GAZ_ENCODING_PARAMETER_NAME = "encoding";

  public static final String
    DEF_GAZ_CASE_SENSITIVE_PARAMETER_NAME = "caseSensitive";


  /** the linear definition of the gazetteer */
  private LinearDefinition definition;

  /** a map of nodes vs gaz lists */
  private Map listsByNode;

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
    initialState = new FSMState(this);
    if(listsURL == null){
      throw new ResourceInstantiationException (
            "No URL provided for gazetteer creation!");
    }
    definition = new LinearDefinition();
    definition.setURL(listsURL);
    definition.load();
    int linesCnt = definition.size();
    listsByNode = definition.loadLists();
    Iterator inodes = definition.iterator();

    String line;
    int nodeIdx = 0;
    LinearNode node;
    while (inodes.hasNext()) {
      node = (LinearNode) inodes.next();
      fireStatusChanged("Reading " + node.toString());
      fireProgressChanged(++nodeIdx * 100 / linesCnt);
      readList(node,true);
    } // while iline
    fireProcessFinished();
    return this;
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
  void readList(LinearNode node, boolean add) throws ResourceInstantiationException{
    String listName, majorType, minorType, languages;
    if ( null == node ) {
      throw new ResourceInstantiationException(" LinearNode node is null ");
    }

    listName = node.getList();
    majorType = node.getMajorType();
    minorType = node.getMinorType();
    languages = node.getLanguage();
    GazetteerList gazList = (GazetteerList)listsByNode.get(node);
    if (null == gazList) {
      throw new ResourceInstantiationException("gazetteer list not found by node");
    }

    Iterator iline = gazList.iterator();

    Lookup lookup = new Lookup(listName,majorType, minorType, languages);
    lookup.list = node.getList();
    if ( null != mappingDefinition){
      MappingNode mnode = mappingDefinition.getNodeByList(lookup.list);
      lookup.oClass = mnode.getClassID();
      lookup.ontology = mnode.getOntologyID();
    }//if mapping def

    String line;
    while(iline.hasNext()){
      line = iline.next().toString();
      if(add)addLookup(line, lookup);
      else removeLookup(line, lookup);
    }
  } // void readList(String listDesc)

  /** Adds one phrase to the list of phrases recognised by this gazetteer
   *
   * @param text the phrase to be added
   * @param lookup the description of the annotation to be added when this
   *     phrase is recognised
   */
// >>> DAM, was
/*
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
*/
// >>> DAM: TransArray optimization
  public void addLookup(String text, Lookup lookup) {
    char currentChar;
    FSMState currentState = initialState;
    FSMState nextState;
    Lookup oldLookup;
    boolean isSpace;

    for(int i = 0; i< text.length(); i++) {
        currentChar = text.charAt(i);
        isSpace = Character.isWhitespace(currentChar);
        if(isSpace) currentChar = ' ';
        else currentChar = (caseSensitive.booleanValue()) ?
                          currentChar :
                          Character.toUpperCase(currentChar) ;
      nextState = currentState.next(currentChar);
      if(nextState == null){
        nextState = new FSMState(this);
        currentState.put(currentChar, nextState);
        if(isSpace) nextState.put(' ',nextState);
      }
      currentState = nextState;
    } //for(int i = 0; i< text.length(); i++)

    currentState.addLookup(lookup);
    //Out.println(text + "|" + lookup.majorType + "|" + lookup.minorType);

  } // addLookup
// >>> DAM, end

  /** Removes one phrase to the list of phrases recognised by this gazetteer
   *
   * @param text the phrase to be removed
   * @param lookup the description of the annotation associated to this phrase
   */
// >>> DAM, was
/*
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
*/
// >>> DAM: TransArray optimization
  public void removeLookup(String text, Lookup lookup) {
    char currentChar;
    FSMState currentState = initialState;
    FSMState nextState;
    Lookup oldLookup;

    for(int i = 0; i< text.length(); i++) {
        currentChar = text.charAt(i);
        if(Character.isWhitespace(currentChar)) currentChar = ' ';
        nextState = currentState.next(currentChar);
        if(nextState == null) return;//nothing to remove
        currentState = nextState;
    } //for(int i = 0; i< text.length(); i++)
    currentState.removeLookup(lookup);
  } // removeLookup
// >>> DAM, end

  /** Returns a string representation of the deterministic FSM graph using
   * GML.
   */
  public String getFSMgml() {
    String res = "graph[ \ndirected 1\n";
    ///String nodes = "", edges = "";
    StringBuffer nodes = new StringBuffer(gate.Gate.STRINGBUFFER_SIZE),
                edges = new StringBuffer(gate.Gate.STRINGBUFFER_SIZE);
    Iterator fsmStatesIter = fsmStates.iterator();
    while (fsmStatesIter.hasNext()){
      FSMState currentState = (FSMState)fsmStatesIter.next();
      int stateIndex = currentState.getIndex();
      /*nodes += "node[ id " + stateIndex +
               " label \"" + stateIndex;
      */
      nodes.append("node[ id ");
      nodes.append(stateIndex);
      nodes.append(" label \"");
      nodes.append(stateIndex);

             if(currentState.isFinal()){
              ///nodes += ",F\\n" + currentState.getLookupSet();
              nodes.append(",F\\n");
              nodes.append(currentState.getLookupSet());
             }
             ///nodes +=  "\"  ]\n";
             nodes.append("\"  ]\n");
      //edges += currentState.getEdgesGML();
      edges.append(currentState.getEdgesGML());
    }
    res += nodes.toString() + edges.toString() + "]\n";
    return res;
  } // getFSMgml


  /**
   * This method runs the gazetteer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{
    interrupted = false;
    AnnotationSet annotationSet;
    //check the input
    if(document == null) {
      throw new ExecutionException(
        "No document to process!"
      );
    }

    if(annotationSetName == null ||
       annotationSetName.equals("")) annotationSet = document.getAnnotations();
    else annotationSet = document.getAnnotations(annotationSetName);

    fireStatusChanged("Doing lookup in " +
                           document.getName() + "...");
    String content = document.getContent().toString();
    int length = content.length();
// >>> DAM, was
/*
    Character currentChar;
*/
// >>> DAM: TransArray optimization
    char currentChar;
// >>> DAM, end
    FSMState currentState = initialState;
    FSMState nextState;
    FSMState lastMatchingState = null;
    int matchedRegionEnd = 0;
    int matchedRegionStart = 0;
    int charIdx = 0;
    int oldCharIdx = 0;
    FeatureMap fm;
    Lookup currentLookup;

// >>> DAM, was
/*
    while(charIdx < length) {
      if(Character.isWhitespace(content.charAt(charIdx)))
        currentChar = new Character(' ');
      else currentChar = (caseSensitive.booleanValue()) ?
                         new Character(content.charAt(charIdx)) :
                         new Character(Character.toUpperCase(
                                       content.charAt(charIdx)));
*/
// >>> DAM: TransArray optimization
    while(charIdx < length) {
      currentChar = content.charAt(charIdx);
      if(Character.isWhitespace(currentChar)) currentChar = ' ';
      else currentChar = caseSensitive.booleanValue() ?
                          currentChar :
                          Character.toUpperCase(currentChar);
// >>> DAM, end
      nextState = currentState.next(currentChar);
      if(nextState == null) {
        //the matching stopped

        //if we had a successful match then act on it;
        if(lastMatchingState != null){
          //let's add the new annotation(s)
          Iterator lookupIter = lastMatchingState.getLookupSet().iterator();

          while(lookupIter.hasNext()) {
            currentLookup = (Lookup)lookupIter.next();
            fm = Factory.newFeatureMap();
            fm.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME, currentLookup.majorType);
            if (null!= currentLookup.oClass && null!=currentLookup.ontology){
              fm.put(CLASS,currentLookup.oClass);
              fm.put(ONTOLOGY,currentLookup.ontology);
            }
            if(null != currentLookup.minorType) {
              fm.put(LOOKUP_MINOR_TYPE_FEATURE_NAME, currentLookup.minorType);
              if(null != currentLookup.languages)
                fm.put("language", currentLookup.languages);
            }
            try {
              annotationSet.add(new Long(matchedRegionStart),
                              new Long(matchedRegionEnd + 1),
                              LOOKUP_ANNOTATION_TYPE,
                              fm);
            } catch(InvalidOffsetException ioe) {
              throw new LuckyException(ioe.toString());
            }
          }//while(lookupIter.hasNext())
          lastMatchingState = null;
        }

        //reset the FSM
        charIdx = matchedRegionStart + 1;
        matchedRegionStart = charIdx;
        currentState = initialState;

      } else{//go on with the matching
        currentState = nextState;
        //if we have a successful state then store it
        if(currentState.isFinal() &&
           (matchedRegionStart == 0 ||
            !Character.isLetter(content.charAt(matchedRegionStart - 1))) &&
           (charIdx + 1 >= content.length()   ||
            !Character.isLetter(content.charAt(charIdx + 1)))
          ){
          matchedRegionEnd = charIdx;
          lastMatchingState = currentState;
        }
        charIdx ++;
        if(charIdx == content.length()){
          //we can't go on, use the last matching state and restart matching
          //from the next char
          if(lastMatchingState != null){
            //let's add the new annotation(s)
            Iterator lookupIter = lastMatchingState.getLookupSet().iterator();

            while(lookupIter.hasNext()) {
              currentLookup = (Lookup)lookupIter.next();
              fm = Factory.newFeatureMap();
              fm.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME, currentLookup.majorType);
              if (null!= currentLookup.oClass && null!=currentLookup.ontology){
                fm.put(CLASS,currentLookup.oClass);
                fm.put(ONTOLOGY,currentLookup.ontology);
              }
              if(null != currentLookup.minorType) {
                fm.put(LOOKUP_MINOR_TYPE_FEATURE_NAME, currentLookup.minorType);
                if(null != currentLookup.languages)
                  fm.put("language", currentLookup.languages);
              }
              try {
                annotationSet.add(new Long(matchedRegionStart),
                                new Long(matchedRegionEnd + 1),
                                LOOKUP_ANNOTATION_TYPE,
                                fm);
              } catch(InvalidOffsetException ioe) {
                throw new LuckyException(ioe.toString());
              }
            }//while(lookupIter.hasNext())
            lastMatchingState = null;
          }

          //reset the FSM
          charIdx = matchedRegionStart + 1;
          matchedRegionStart = charIdx;
          currentState = initialState;
        }
      }
      if(charIdx - oldCharIdx > 256) {
        fireProgressChanged((100 * charIdx )/ length );
        oldCharIdx = charIdx;
        if(isInterrupted()) throw new ExecutionInterruptedException(
            "The execution of the " + getName() +
            " gazetteer has been abruptly interrupted!");
      }
    } // while(charIdx < length)

    if(lastMatchingState != null) {
      Iterator lookupIter = lastMatchingState.getLookupSet().iterator();
      while(lookupIter.hasNext()) {
        currentLookup = (Lookup)lookupIter.next();
        fm = Factory.newFeatureMap();
        fm.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME, currentLookup.majorType);
        if (null!= currentLookup.oClass && null!=currentLookup.ontology){
          fm.put(CLASS,currentLookup.oClass);
          fm.put(ONTOLOGY,currentLookup.ontology);
        }

        if(null != currentLookup.minorType)
          fm.put(LOOKUP_MINOR_TYPE_FEATURE_NAME, currentLookup.minorType);
        try{
          annotationSet.add(new Long(matchedRegionStart),
                          new Long(matchedRegionEnd + 1),
                          LOOKUP_ANNOTATION_TYPE,
                          fm);
        } catch(InvalidOffsetException ioe) {
          throw new GateRuntimeException(ioe.toString());
        }
      }//while(lookupIter.hasNext())
    }
    fireProcessFinished();
    fireStatusChanged("Lookup complete!");
  } // execute


  /** The initial state of the FSM that backs this gazetteer
   */
  FSMState initialState;

  /** A set containing all the states of the FSM backing the gazetteer
   */
  Set fsmStates;

  /**lookup <br>
   * @param singleItem a single string to be looked up by the gazetteer
   * @return the referring Lookup object*/
  public Lookup lookup(String singleItem) {
    return null;
  }

} // DefaultGazetteer

// >>> DAM: TransArray optimization, new charMap implementation
interface Iter
{
    public boolean hasNext();
    public char next();
} // iter class

/**
 * class implementing the map using binary serach by char as key
 * to retrive the coresponding object.
 */
class charMap
{
    char[] itemsKeys = null;
    Object[] itemsObjs = null;

    /**
     * resize the containers by one leavaing empty elemant at position 'index'
     */
    void resize(int index)
    {
        int newsz = itemsKeys.length + 1;
        char[] tempKeys = new char[newsz];
        Object[] tempObjs = new Object[newsz];
        int i;
        for (i= 0; i < index; i++)
        {
            tempKeys[i] = itemsKeys[i];
            tempObjs[i] = itemsObjs[i];
        }
        for (i= index+1; i < newsz; i++)
        {
            tempKeys[i] = itemsKeys[i-1];
            tempObjs[i] = itemsObjs[i-1];
        }

        itemsKeys = tempKeys;
        itemsObjs = tempObjs;
    } // resize

/**
 * get the object from the map using the char key
 */
    Object get(char key)
    {
        if (itemsKeys == null) return null;
        int index = Arrays.binarySearch(itemsKeys, key);
        if (index<0)
            return null;
        return itemsObjs[index];
    }
/**
 * put the object into the char map using the chat as the key
 */
    Object put(char key, Object value)
    {
        if (itemsKeys == null)
        {
            itemsKeys = new char[1];
            itemsKeys[0] = key;
            itemsObjs = new Object[1];
            itemsObjs[0] = value;
            return value;
        }// if first time
        int index = Arrays.binarySearch(itemsKeys, key);
        if (index<0)
        {
            index = ~index;
            resize(index);
            itemsKeys[index] = key;
            itemsObjs[index] = value;
        }
        return itemsObjs[index];
    } // put
/**
 * the keys itereator
 * /
    public Iter iter()
    {
        return new Iter()
        {
            int counter = 0;
            public boolean hasNext() {return counter < itemsKeys.length;}
            public char next() { return itemsKeys[counter];}
        };
    } // iter()
 */

} // class charMap
// >>> DAM, end, new charMap instead MAP for transition function in the FSMState