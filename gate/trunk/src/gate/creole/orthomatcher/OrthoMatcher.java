/*
 *  OrthoMatcher.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 24/August/2001
 *
 *  $Id$
 */


package gate.creole.orthomatcher;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.corpora.*;
import gate.annotation.*;
import java.util.*;
import java.io.*;
import java.net.*;
import gnu.regexp.*;

public class OrthoMatcher extends AbstractProcessingResource
                       implements ProcessingResource{

  protected static final String CDGLISTNAME = "cdg";
  protected static final String ALIASLISTNAME = "alias";
  protected static final String ARTLISTNAME = "def_art";
  protected static final String PREPLISTNAME = "prepos";
  protected static final String CONNECTORLISTNAME = "connector";
  protected static final String SPURLISTNAME = "spur_match";

  protected static final String LOOKUPNAME = "Lookup";
  protected static final String MATCHES_FEATURE = "matches";
  protected static final String DOC_MATCHES_FEATURE = "MatchesAnnots";
  protected static final String GENDER_FEATURE = "gender";

  /** the document for namematch */
  protected gate.Document document;

  /**the name of the annotation set*/
  protected String annotationSetName;

  /** the types of the annotation */
  protected List annotationTypes = new ArrayList(10);

  /** the organization type*/
  protected String organizationType = "Organization";

  /** the person type*/
  protected String personType = "Person";

  protected String unknownType = "Unknown";

  /** internal or external list */
  protected boolean extLists = true;

  //** Orthomatching is not case-sensitive by default*/
  protected boolean caseSensitive = false;

  protected ExecutionException executionException;

  // name lookup tables (used for namematch)
  //gave them bigger default size, coz rehash is expensive
  protected HashMap alias = new HashMap(100);
  protected HashSet cdg = new HashSet(50);
  protected HashMap spur_match = new HashMap(100);
  protected HashMap def_art = new HashMap(20);
  protected HashMap connector = new HashMap(20);
  protected HashMap prepos = new HashMap(30);


  protected AnnotationSet nameAllAnnots = null;
  protected HashMap processedAnnots = new HashMap(150);
  protected HashMap annots2Remove = new HashMap(75);
  protected List matchesDocFeature = new ArrayList();

  /** a feature map to be used when retrieving annotations
   *  declared here so can be reused for efficiency
   *  clear() before each use
   */
  protected FeatureMap tempMap = Factory.newFeatureMap();

  /** a buffer in order to read an array of char */
  private char cbuffer[] = null;

  /** the size of the buffer */
  private final static int BUFF_SIZE = 65000;

  public OrthoMatcher () {}

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    cbuffer = new char[BUFF_SIZE];

    //initialise the list of annotations which we will match
    annotationTypes.add(organizationType);
    annotationTypes.add(personType);
    annotationTypes.add("Location");
    annotationTypes.add("Date");
    try {
      createLists();
    } catch (IOException ioe) {ioe.printStackTrace();}
    return this;
  } // init()

  /**  Run the resource. It doesn't make sense not to override
    *  this in subclasses so the default implementation signals an
    *  exception.
    */
  public void run() {

    //check the input
    if(document == null) {
      executionException = new ExecutionException(
        "No document for namematch!"
      );
      return;
    }

    // get the annotations from document
    if ((annotationSetName == null)|| (annotationSetName == ""))
      nameAllAnnots = document.getAnnotations();
    else
      nameAllAnnots = document.getAnnotations(annotationSetName);

    //if none found, print warning and exit
    if ((nameAllAnnots == null) || nameAllAnnots.isEmpty()) {
      Out.prln("OrthoMatcher Warning: No annotations found for processing");
      return;
    }

    //check if we've been run on this document before
    if (document.getFeatures().containsKey(DOC_MATCHES_FEATURE))
      cleanup();

    // creates the cdg list from the document
    //no need to create otherwise, coz already done in init()
    if (!extLists)
      buildTables(nameAllAnnots);

    //first match all name annotations
    matchNameAnnotations();

    //then match the unknown ones to all name ones
    matchUnknown();

    // set the matches of the document
//    determineMatchesDocument();
    if (! matchesDocFeature.isEmpty()) {
      document.getFeatures().put(MATCHES_FEATURE, matchesDocFeature);

      //cannot do clear() as this has already been put on the document
      //so I need a new one for the next run of matcher
      matchesDocFeature = new ArrayList();
    }

    //clean-up the internal data structures for next run
    nameAllAnnots = null;
    processedAnnots.clear();
    annots2Remove.clear();
  } // run()

  protected void matchNameAnnotations() {
    // go through all the annotation types
    Iterator iterAnnotationTypes = annotationTypes.iterator();
    while (iterAnnotationTypes.hasNext()) {
      String annotationType = (String)iterAnnotationTypes.next();

      AnnotationSet nameAnnots = nameAllAnnots.get(annotationType);

      // continue if no such annotations exist
      if ((nameAnnots == null) || nameAnnots.isEmpty())
        continue;

      Iterator iterNames = nameAnnots.iterator();
      while (iterNames.hasNext()) {
        Annotation nameAnnot = (Annotation) iterNames.next();
        Integer id = nameAnnot.getId();

        // get string and value
        String annotString = null;
        try {
            annotString = document.getContent().getContent(
            nameAnnot.getStartNode().getOffset(),
            nameAnnot.getEndNode().getOffset()
            ).toString();
          // now do the reg. exp. substitutions
          annotString = regularExpressions(annotString," ", "\\s+");
        } catch (InvalidOffsetException ioe) {
            executionException = new ExecutionException
                                   ("Invalid offset of the annotation");
        }
        //convert to lower case if we are not doing a case sensitive match
        if (!caseSensitive)
          annotString = annotString.toLowerCase();

        //first check whether we have not matched such a string already
        //if so, just consider it matched, don't bother calling the rules
        if (processedAnnots.containsValue(annotString)) {
          updateMatches(nameAnnot, annotString);
          processedAnnots.put(id, annotString);
          continue;
        } else if (processedAnnots.isEmpty()) {
          processedAnnots.put(id, annotString);
          continue;
        }

        //otherwise try matching with previous annotations
        matchWithPrevious(nameAnnot, annotString);

        //finally add the current annotations to the processed map
        processedAnnots.put(id, annotString);
      }//while through name annotations

    }//while through annotation types

  }

  protected void matchUnknown() {
    //get all Unknown annotations
    AnnotationSet unknownAnnots = nameAllAnnots.get(unknownType);

    if ((unknownAnnots == null) || unknownAnnots.isEmpty())
      return;

    Iterator iter = unknownAnnots.iterator();
    //loop through the unknown annots
    while (iter.hasNext()) {
      Annotation unknown = (Annotation) iter.next();

      // get string and value
      String unknownString = null;
      try {
          unknownString = document.getContent().getContent(
            unknown.getStartNode().getOffset(),
            unknown.getEndNode().getOffset()
            ).toString();
        // now do the reg. exp. substitutions
        unknownString = regularExpressions(unknownString," ", "\\s+");
      } catch (InvalidOffsetException ioe) {
          executionException = new ExecutionException
                                 ("Invalid offset of the annotation");
      }
      //convert to lower case if we are not doing a case sensitive match
      if (!caseSensitive)
        unknownString = unknownString.toLowerCase();

      //first check whether we have not matched such a string already
      //if so, just consider it matched, don't bother calling the rules
      if (processedAnnots.containsValue(unknownString)) {
        Annotation matchedAnnot = updateMatches(unknown, unknownString);
        if (matchedAnnot.getType().equals(unknownType)) {
          annots2Remove.put(unknown.getId(), annots2Remove.get(matchedAnnot));
        }
        else
          annots2Remove.put(unknown.getId(), matchedAnnot.getType());
        processedAnnots.put(unknown.getId(), unknownString);
        unknown.getFeatures().put("NMRule", unknownType);
        continue;
      }

      matchWithPrevious(unknown, unknownString);
    } //while though unknowns

    if (! annots2Remove.isEmpty()) {
      Iterator unknownIter = annots2Remove.keySet().iterator();
      while (unknownIter.hasNext()) {
        Integer unknId = (Integer) unknownIter.next();
        Annotation unknown = nameAllAnnots.get(unknId);
        nameAllAnnots.add(
          unknown.getStartNode(),
          unknown.getEndNode(),
          (String) annots2Remove.get(unknId),
          unknown.getFeatures()
        );
        nameAllAnnots.remove(unknown);

      }//while
    }//if
  }

  protected void matchWithPrevious(Annotation nameAnnot, String annotString) {
    Iterator prevIter = processedAnnots.keySet().iterator();
    while (prevIter.hasNext()) {
      Integer prevId = (Integer) prevIter.next();
      Annotation prevAnnot = (Annotation) nameAllAnnots.get(prevId);

      //check if the two are from the same type or the new one is unknown
      if (prevAnnot == null || (! prevAnnot.getType().equals(nameAnnot.getType())
          && ! nameAnnot.getType().equals(unknownType))
         )
        continue;

      String prevAnnotString = (String) processedAnnots.get(prevId);

      //check if we have already matched this annotation to the new one
      if (matchedAlready(nameAnnot, prevAnnot) )
        continue;

      // find which annotation string of the two is longer
      //  this is useful for some of the matching rules
      String longName = prevAnnotString;
      String shortName = annotString;

      // determine the title from annotation string
      if (prevAnnot.getType().equals(personType)) {
        longName =
          containTitle(nameAllAnnots, longName,prevAnnot);
        shortName = containTitle(nameAllAnnots, shortName,nameAnnot);
      }//if

      if (shortName.length()>=longName.length()) {
        String temp = longName;
        longName = shortName;
        shortName = temp;
      }//if

      //if the two annotations match
      if (apply_rules_namematch(prevAnnot.getType(), shortName,longName)) {
        updateMatches(nameAnnot, prevAnnot);
        //if unknown annotation, we need to change to the new type
        if (nameAnnot.getType().equals(unknownType)) {
          annots2Remove.put(nameAnnot.getId(), prevAnnot.getType());
          //also put an attribute to indicate that
          nameAnnot.getFeatures().put("NMRule", unknownType);
        }
      }

    }//while through previous annotations

  }//matchWithPrevious

  protected boolean matchedAlready(Annotation annot1, Annotation annot2) {
    //the two annotations are already matched if the matches list of the first
    //contains the id of the second
    List matchesList = (List) annot1.getFeatures().get(MATCHES_FEATURE);
    if ((matchesList == null) || matchesList.isEmpty())
      return false;
    else if (matchesList.contains(annot2.getId()))
      return true;
    return false;
  }

  protected Annotation updateMatches(Annotation newAnnot, String annotString) {
    Annotation matchedAnnot = null;
    Integer matchedId = null;

    //first find a processed annotation with the same string
    Iterator iter = processedAnnots.keySet().iterator();
    while (iter.hasNext()) {
      matchedId = (Integer) iter.next();
      String oldString = (String) processedAnnots.get(matchedId);
      if (annotString.equals(oldString)) {
        matchedAnnot = nameAllAnnots.get(matchedId);
        break;
      }//if
    }//while

    if (matchedAnnot == null) return null;

    List matchesList = (List) matchedAnnot.getFeatures().get(MATCHES_FEATURE);
    if ((matchesList == null) || matchesList.isEmpty()) {
      //no previous matches, so need to add
      if (matchesList == null) {
        matchesList = new ArrayList();
        matchedAnnot.getFeatures().put(MATCHES_FEATURE, matchesList);
        matchesDocFeature.add(matchesList);
      }//if
      matchesList.add(matchedId);
      matchesList.add(newAnnot.getId());
    } else {
      //just add the new annotation
      matchesList.add(newAnnot.getId());
    }//if
    //add the matches list to the new annotation
    newAnnot.getFeatures().put(MATCHES_FEATURE, matchesList);
    return matchedAnnot;
  }

  protected void updateMatches(Annotation newAnnot, Annotation prevAnnot) {

    List matchesList = (List) prevAnnot.getFeatures().get(MATCHES_FEATURE);
    if ((matchesList == null) || matchesList.isEmpty()) {
      //no previous matches, so need to add
      if (matchesList == null) {
        matchesList = new ArrayList();
        prevAnnot.getFeatures().put(MATCHES_FEATURE, matchesList);
        matchesDocFeature.add(matchesList);
      }//if
      matchesList.add(prevAnnot.getId());
      matchesList.add(newAnnot.getId());
    } else {
      //just add the new annotation
      matchesList.add(newAnnot.getId());
    }//if
    //add the matches list to the new annotation
    newAnnot.getFeatures().put(MATCHES_FEATURE, matchesList);
    //propagate the gender if two persons are matched
    if (prevAnnot.getType().equals(personType)) {
      String prevGender = (String) prevAnnot.getFeatures().get(GENDER_FEATURE);
      String newGender = (String) newAnnot.getFeatures().get(GENDER_FEATURE);
      boolean unknownPrevGender = isUnknownGender(prevGender);
      boolean unknownNewGender = isUnknownGender(newGender);
      if (unknownPrevGender && !unknownNewGender)
        prevAnnot.getFeatures().put(GENDER_FEATURE, newGender);
      else if (unknownNewGender && !unknownPrevGender)
        newAnnot.getFeatures().put(GENDER_FEATURE, prevGender);
    }//if
  }


  protected void cleanup() {
    document.getFeatures().remove(DOC_MATCHES_FEATURE);

    Iterator iter = annotationTypes.iterator();
    while (iter.hasNext()) {
      String type = (String) iter.next();
      AnnotationSet annots = nameAllAnnots.get(type);
      if (annots == null || annots.isEmpty())
        continue;
      Iterator iter1 = annots.iterator();
      while (iter1.hasNext())
        ((Annotation) iter1.next()).getFeatures().remove(MATCHES_FEATURE);
    } //while
  }//cleanup

  /** return a person name without title */
  protected String containTitle (AnnotationSet annotSet,String annotString,
                              Annotation annot){
    // get the offsets
    Long startAnnot = annot.getStartNode().getOffset();
    Long endAnnot = annot.getEndNode().getOffset();

    // determine "Lookup" annotation set
    AnnotationSet as =
      annotSet.get(startAnnot,endAnnot).get("Lookup");

    if ((as !=null )) {
      Iterator iter = as.iterator();
      while (iter.hasNext()) {
        Annotation currentAnnot = (Annotation)(iter.next());

        // determine the features of the current annotation
        FeatureMap fm = currentAnnot.getFeatures();
        if (fm.containsKey("majorType")&&
          (fm.get("majorType").equals("title"))){

            Long offsetStartAnnot = currentAnnot.getStartNode().getOffset();
            Long offsetEndAnnot = currentAnnot.getEndNode().getOffset();
            try {
              // the title from the current annotation
              String annotTitle =
                document.getContent().getContent(
                  offsetStartAnnot,offsetEndAnnot).toString();

              // eliminate the title from annotation string and return the result
              if (annotTitle.length()<annotString.length())
                return annotString.substring(
                                    annotTitle.length()+1,annotString.length());
            } catch (InvalidOffsetException ioe) {
              executionException = new ExecutionException
                                 ("Invalid offset of the annotation");
            }
        }//if
      }// while
    }//if
    return annotString;

  }

  public void check() throws ExecutionException {
    if (executionException != null) {
      ExecutionException e = executionException;
      executionException = null;
      throw e;
    }
  } // check()

  /** if ( == false) then reads the names of files in order
    *  to create the lookup tables
    */
  protected void createLists() throws IOException {
    InputStream inputStream = Files.getGateResourceAsStream(
                                              "creole/namematcher/listsNM.def");
    InputStreamReader inputStreamReader = new InputStreamReader (
                                                    inputStream);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

    String lineRead = null;
    while ((lineRead = bufferedReader.readLine()) != null){
      int index = lineRead.indexOf(":");
      if (index != -1){
        String nameFile = lineRead.substring(0,index);
        String nameList = lineRead.substring(index+1,lineRead.length());
        createAnnotList(nameFile,nameList);
      }// if
    }//while
    bufferedReader.close();
    inputStreamReader.close();
    inputStream.close();
  }// createLists()

  /** creates the lookup tables */
  protected void createAnnotList(String nameFile,String nameList)
                                                          throws IOException{
    InputStream inputStream = Files.getGateResourceAsStream(
                                              "creole/namematcher/"+nameFile);
    InputStreamReader inputStreamReader = new InputStreamReader (
                                                    inputStream);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

    String lineRead = null;
    while ((lineRead = bufferedReader.readLine()) != null){
      if (nameList.compareTo(CDGLISTNAME)==0){
        if (caseSensitive)
          cdg.add(lineRead);
        else
          cdg.add(lineRead.toLowerCase());
      }// if
      else {
        int index = lineRead.indexOf("£");
        if (index != -1){
          String  expr = lineRead.substring(0,index);
          //if not case-sensitive, we need to downcase all strings
          if (!caseSensitive)
            expr = expr.toLowerCase();
          String code = lineRead.substring(index+1,lineRead.length());
          if (nameList.equals(ALIASLISTNAME))
                            alias.put(expr, code);
          else
          if (nameList.equals(ARTLISTNAME))
                            def_art.put(expr, code);
          else
          if (nameList.equals(PREPLISTNAME))
                            prepos.put(expr, code);
          else
          if (nameList.equals(CONNECTORLISTNAME))
                            connector.put(expr, code);
          else
          if (nameList.equals(SPURLISTNAME))
                            spur_match.put(expr, code);

        }//if
      }// else

    }//while
  }//createAnnotList


  /** apply_rules_namematch: apply rules similarly to lasie1.5's namematch */
  private boolean apply_rules_namematch(String annotationType, String shortName,
                                        String longName) {
    // first apply rule for spurius matches i.e. rule0
    if (matchRule0(longName, shortName))
      return false;
    if (
         (// rules for all annotations
          //no longer use rule1, coz I do the check for same string via the
          //hash table
            matchRule2(longName, shortName)
         ||
            matchRule3(shortName, longName)
         ||
            matchRule5(longName, shortName)
         ) // rules for all annotations
         ||
         (// rules for organisation annotations
             ( annotationType.equals(organizationType))
             &&
             (    matchRule4(longName, shortName)
               ||
                  matchRule6(longName, shortName)
               ||
                  matchRule7(longName, shortName)
               ||
                  matchRule8(longName, shortName)
               ||
                  matchRule9(longName, shortName)
               ||
                  matchRule10(longName, shortName)
               ||
                  matchRule11(longName, shortName)
               ||
                  matchRule13(shortName, longName)
              )
           )// rules for organisation annotations
         ||
         (// rules for person annotations
             (    annotationType.equals(personType))
               &&
             (    matchRule4(longName, shortName)
               ||
                  matchRule7(longName, shortName)
               ||
                  matchRule12(shortName, longName)
               || //kalina: added this, so it matches names when contain more
                  //than one first and one last name
                  matchRule13(shortName, longName)
              )
          )// rules for person annotations
         ) //if
      return true;
    return false;
  }//apply_rules

  /** set the document */
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }//setDocument

  /** set the extLists flag */
  public void setExtLists(Boolean newExtLists) {
    extLists = newExtLists.booleanValue();
  }//setextLists

  /** set the caseSensitive flag */
  public void setCaseSensitive(Boolean newCase) {
    caseSensitive = newCase.booleanValue();
  }//setextLists

  /** set the annotation set name*/
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }//setAnnotationSetName

  /** set the types of the annotations*/
  public void setAnnotationTypes(List newType) {
    annotationTypes = newType;
  }//setAnnotationTypes

  public void setOrganizationType(String newOrganizationType) {
    organizationType = newOrganizationType;
  }//setOrganizationType

  public void setPersonType(String newPersonType) {
    personType = newPersonType;
  }//setPersonType

  /**
   * Gets the document currently set as target for this namematch.
   * @return a {@link gate.Document}
   */
  public gate.Document getDocument() {
    return document;
  }//getDocument

  /**get the name of the annotation set*/
  public String getAnnotationSetName() {
    return annotationSetName;
  }//getAnnotationSetName

  /** get the types of the annotation*/
  public List getAnnotationTypes() {
    return annotationTypes;
  }//getAnnotationTypes

  public String getOrganizationType() {
    return organizationType;
  }

  public String getPersonType() {
    return personType;
  }

  public Boolean getExtList() {
    return new Boolean(extLists);
  }

  public Boolean getCaseSensitive() {
    return new Boolean(caseSensitive);
  }

/*
  public List getMatchesDocument() {
    return matchesDocument;
  }
*/

  protected boolean isUnknownGender(String gender) {
    if (gender == null)
      return true;
    if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))
      return false;
    return true;

  } //isUnknownGender

  /** RULE #0: If the two names are listed in table of
    * spurius matches then they do NOT match
    * Condition(s): -
    * Applied to: all name annotations
    */
  public boolean matchRule0(String s1,
           String s2) {
    if (spur_match.containsKey(s1)
        && spur_match.containsKey(s2) )
      return
        spur_match.get(s1).toString().equals(spur_match.get(s2).toString());

    return false;
  }//matchRule0

  /** RULE #1: If the two names are identical then they are the same
    * no longer used, because I do the check for same string via the
    * hash table of previous annotations
    * Condition(s): depend on case
    * Applied to: all name annotations
    */
  public boolean matchRule1(String s1,
           String s2,
           boolean MatchCase) {
    boolean matched = false;
    if (MatchCase == true)
        matched = s1.equalsIgnoreCase(s2);
    else matched =  s1.equals(s2) ;
//kalina: do not remove, nice for debug
//    if (matched)
//        Out.prln("Rule1: Matched " + s1 + "and " + s2);
    return matched;
  }//matchRule1


  /**
    * RULE #2: if the two names are listed as equivalent in the
    * lookup table (alias) then they match
    * Condition(s): -
    * Applied to: all name annotations
    */
  public boolean matchRule2(String s1,
           String s2) {

    if (alias.containsKey(s1) && alias.containsKey(s2))
      return (alias.get(s1).toString().equals(alias.get(s2).toString()));

    return false;
  }//matchRule2

  /**
    * RULE #3: adding a possessive at the end
    * of one name causes a match
    * e.g. "Standard and Poor" == "Standard and Poor's"
    * and also "Standard and Poor" == "Standard's"
    * Condition(s): case-insensitive match
    * Applied to: all name annotations
    */
  public boolean matchRule3(String s1,
                             String s2) {

    if (s2.endsWith("'s") || s2.endsWith("'")
        ||(s1.endsWith("'s")|| s1.endsWith("'"))) {

      String s1_poss = null;

      if (!s1.endsWith("s")) s1_poss = s1.concat("'s");
      else s1_poss = s1.concat("'");

      if (s1_poss != null && matchRule1(s1_poss,s2,true)) return true;

      // now check the second case i.e. "Standard and Poor" == "Standard's"
      // changed s1-->s2
      String stringToTokenize1 = s2;
      StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
      String token = tokens1.nextToken();

      if (!token.endsWith("s")) s1_poss = token.concat("'s");
      else s1_poss = token.concat("'");

      // changed s2-->s1
      if (s1_poss != null && matchRule1(s1_poss,s1,true)) return true;
    } // if (s2.endsWith("'s")
    return false;
  }//matchRule3

  /**
    * RULE #4: Do all tokens other than the punctuation marks
    * , and . match?
    * e.g. "Smith, Jones" == "Smith Jones"
    * Condition(s): case-insensitive match
    * Applied to: organisation and person annotations
    */
  public boolean matchRule4(String s1,
           String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1,"., ");
    String stringToTokenize2 = s2;
    StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2,"., ");
    boolean allTokensMatch = true;

    if (tokens1.countTokens() == tokens2.countTokens()) {
//      Out.prln("Rule 4");
      while (tokens1.hasMoreTokens()) {
  if (!tokens1.nextToken().equalsIgnoreCase(tokens2.nextToken())) {
    allTokensMatch = false;
    break;
  } // if (!tokens1.nextToken()
      } // while
      return allTokensMatch;
    } // if (tokens1.countTokens() ==
    return false;
  }//matchRule4

  /**
    * RULE #5: if the 1st token of one name
    * matches the second name
    * e.g. "Pepsi Cola" == "Pepsi"
    * Condition(s): case-insensitive match
    * Applied to: all name annotations
    */
  public boolean matchRule5(String s1,
           String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");

//    Out.prln("Rule 5");
    if (tokens1.countTokens()>1)
      return matchRule1(tokens1.nextToken(),s2,true);

    return false;

  }//matchRule5

  /**
    * RULE #6: if one name is the acronym of the other
    * e.g. "Imperial Chemical Industries" == "ICI"
    * Condition(s): case-sensitive match, remove initial "The"
    * Applied to: organisation annotations only
    */
  public boolean matchRule6(String s1,
           String s2) {

    if (s1.startsWith("The ")) s1 = s1.substring(4);
    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");

    String token = null;
    StringBuffer acronym_s1 = new StringBuffer("");
    StringBuffer acronymDot_s1 = new StringBuffer("");

    while (tokens1.hasMoreTokens()) {
      token = tokens1.nextToken();
      acronym_s1.append(token.substring(0,1));
    }

    s1 = acronym_s1.toString();

    //now check if last token is cdg
    //if (cdg.containsKey(token)) s1 = s1.substring(0,s1.length()-1);
    s2 = regularExpressions(s2,""," ");
    s2 = regularExpressions(s2,"","\\.");

    return matchRule1(s1,s2,false);

  }//matchRule6

  /**
    * RULE #7: if one of the tokens in one of the
    * names is in the list of separators eg. "&"
    * then check if the token before the separator
    * matches the other name
    * e.g. "R.H. Macy & Co." == "Macy"
    * Condition(s): case-sensitive match
    * Applied to: organisation and person annotations only
    *
    * kalina: That rule is responsible for matching Hamish Cunningham and
    * Cunningham for Person-s, because it's been changed, so that the connector
    * actually being there is unnecessary. Took me absolutely f***ing ages
    * to find out that this particular rule is responsible for that.
    */
  public boolean matchRule7(String s1,
           String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
    String token = null;
    String previous_token = null;

    while (tokens1.hasMoreTokens()) {
      token = tokens1.nextToken();
      if (connector.containsKey(token)) break;
      previous_token = token;
    }

    //now match previous_token with other name
    if (previous_token != null) {
//      Out.prln("Rule7");
      return matchRule1(previous_token,s2,false);
    }
    return false;
  }//matchRule7

  /**
    * RULE #8: if the names match after stripping off "The"
    * and trailing company designator
    * e.g. "The Magic Tricks Co." == "Magic Tricks"
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule8(String s1,
           String s2) {

    if (s1.startsWith("The ")) s1 = s1.substring(4);
    if (s2.startsWith("The ")) s2 = s2.substring(4);

    // check that cdg is not empty
    if (!cdg.isEmpty()) {
      String stringToTokenize1 = s1;
      StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");

      String stringToTokenize2 = s2;
      StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2," ");
      String token = null;
      String cdg1 = null;
      String cdg2 = null;

      s1 = "";
      s2 = "";

      //check last token of s1
      while (tokens1.hasMoreTokens()) {
        token = tokens1.nextToken();
        if (!tokens1.hasMoreTokens()
            && cdg.contains(token)) cdg1=token;
        else s1 = s1+token;
      }

      // do the same for s2
      while (tokens2.hasMoreTokens()) {
        token = tokens2.nextToken();
        if (!tokens2.hasMoreTokens()
          && cdg.contains(token)) cdg2=token;
        else s2 = s2+token;
      }

      // if the company designators are different
      // then they are NOT the same organisations
      if ((cdg1!=null && cdg2!=null)
    && !cdg1.equalsIgnoreCase(cdg2)) return false;
    }
    if (!s1.equals("") && !s2.equals("")) return matchRule1(s1,s2,false);

    return false;
  }//matchRule8
  /**
    * RULE #9: does one of the names match the token
    * just before a trailing company designator
    * in the other name?
    * e.g. "R.H. Macy Co." == "Macy"
    * Condition(s): case-sensitive match
    * Applied to: organisation and person annotations only
    */
  public boolean matchRule9(String s1,
           String s2) {

    if (!cdg.isEmpty()) {
      String stringToTokenize1 = s1;
      StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
      String token = null;
      String previous_token = null;

      if (tokens1.countTokens()>1) {
        while  (tokens1.hasMoreTokens()) {
          token = tokens1.nextToken();
          if (tokens1.hasMoreTokens()) previous_token = token;
        } //while
        if (cdg.contains(token)) {
          //now match previous_token with other name
          if (previous_token != null)
            return matchRule1(previous_token,s2,false);
          } //if (cdg.containsKey(token))
        } // if (tokens1.countTokens()>1)
    } // if (!cdg.isEmpty()) {
    return false;
  }//matchRule9

  /**
    * RULE #10: is one name the reverse of the other
    * reversing around prepositions only?
    * e.g. "Department of Defence" == "Defence Department"
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule10(String s1,
            String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
    String stringToTokenize2 = s2;
    StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2," ");
    String token = null;
    String previous_token = null;
    String next_token = null;
    boolean invoke_rule=false;

    if (tokens1.countTokens() >= 3
  && tokens2.countTokens() >= 2) {

    // first get the tokens before and after the preposition
    while (tokens1.hasMoreTokens()) {
      token = tokens1.nextToken();
      if (prepos.containsKey(token)) {
  invoke_rule=true;
  break;
      }
      previous_token = token;
    }

    if (invoke_rule) {
    if (tokens1.hasMoreTokens()
  && previous_token != null) next_token=tokens1.nextToken();
    else return false;

    // then compare (in reverse) with the first two tokens of s2
      if (matchRule1(next_token,tokens2.nextToken(),false)
    && matchRule1(previous_token,tokens2.nextToken(),false)) return true ;
    } // if (invoke_rule)
    }//(tokens1.countTokens() >= 3
    return false;
  }//matchRule10

  /**
    * RULE #11: does one name consist of contractions
    * of the first two tokens of the other name?
    * e.g. "Communications Satellite" == "ComSat"
    * and "Pan American" == "Pan Am"
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule11(String s1,
            String s2) {


    // first do the easy case e.g. "Pan American" == "Pan Am"
    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
    String stringToTokenize2 = s2;
    StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2," ");
    String token11 = null;
    String token12 = null;
    String token21 = null;
    String token22 = null;

    if (tokens1.countTokens() >= 2) {
      // 1st get the first two tokens of s1
      token11 = tokens1.nextToken();
      token12 = tokens1.nextToken();

    // now check for the first case i.e. "Pan American" == "Pan Am"
    if (tokens2.countTokens() == 2)  {

      token21 = tokens2.nextToken();
      token22 = tokens2.nextToken();

      if (token11.startsWith(token21)
    && token12.startsWith(token22)) return true;

    } // if (tokens2.countTokens() == 2)

    // now the second case e.g.  "Communications Satellite" == "ComSat"
    else if (tokens2.countTokens()==1 && s2.length()>=3) {

      // split the token into possible contractions
      // ignore case for matching
      for (int i=2;i<s2.length();i++) {
  token21=s2.substring(0,i+1);
  token22=s2.substring(i+1);

  if (token11.startsWith(token21)
      && token12.startsWith(token22)) return true;
      }// for
    } // else if
    } //if (tokens1.countTokens() >= 2)
    return false;
  }//matchRule11

  /**
    * RULE #12: do the first and last tokens of one name
    * match the first and last tokens of the other?
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule12(String s1,
            String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
    String stringToTokenize2 = s2;
    StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2," ");
    String token1 = null;
    String token2 = null;
    String s1_FirstAndLastTokens = null;
    String s2_FirstAndLastTokens = null;

    if (tokens1.countTokens()>1 && tokens2.countTokens()>1) {
//     Out.prln("Rule 12");

      // get first and last tokens of s1
      s1_FirstAndLastTokens = tokens1.nextToken();

      while (tokens1.hasMoreTokens())  token1 = tokens1.nextToken();
      s1_FirstAndLastTokens = s1_FirstAndLastTokens + token1;

      // get first and last tokens of s2
      s2_FirstAndLastTokens = tokens2.nextToken();

      while (tokens2.hasMoreTokens()) token2 = tokens2.nextToken();
      s2_FirstAndLastTokens = s2_FirstAndLastTokens + token2;

      return matchRule1(s1_FirstAndLastTokens,s2_FirstAndLastTokens,false);
    } // if (tokens1.countTokens()>1
    return false;
  }//matchRule12

  /**
    * RULE #13: do multi-word names match except for
    * one token e.g.
    * "Second Force Recon Company" == "Force Recon Company"
    * Note that this rule has NOT been used in LaSIE's 1.5
    * namematcher
    * Restrictions: - remove cdg first
    *               - shortest name should be 2 words or more
    *               - if N is the number of tokens of the longest
    *                 name, then N-1 tokens should be matched
    * Condition(s): case-sensitive match
    * Applied to: organisation or person annotations only
    */
  public boolean matchRule13(String s1,
            String s2) {

    String stringToTokenize1 = s1;
    StringTokenizer tokens1 = new StringTokenizer(stringToTokenize1," ");
    String stringToTokenize2 = s2;
    StringTokenizer tokens2 = new StringTokenizer(stringToTokenize2," ");
    String token1 = null;
    String token2 = null;
    String cdg1 = null;
    String cdg2 = null;

    List v1 = new ArrayList();
    List v2 = new ArrayList();
    List largerVector = new ArrayList();
    List smallerVector = new ArrayList();

    int matched_tokens = 0;

    while (tokens1.hasMoreTokens()) {
        token1 = tokens1.nextToken();
        if(!tokens1.hasMoreTokens()
          &&  cdg.contains(token1)) cdg1=token1;
        else {
        //kalina: move it to lower case, so we match irrespective of case
        token1 = token1.toLowerCase();
        v1.add(token1);
        }
    }

    while (tokens2.hasMoreTokens()) {
      token2 = tokens2.nextToken();
      if(!tokens2.hasMoreTokens()
          &&  cdg.contains(token2)) cdg2=token2;
          else {
            token2 = token2.toLowerCase();
            v2.add(token2);
          }
    }


    // if names < 2 words then rule is invalid
    if (v1.size() < 2 || v2.size() < 2) return false;

    // if the company designators are different
    // then they are NOT the same organisations
    if ((cdg1!=null && cdg2!=null)
    && !cdg1.equalsIgnoreCase(cdg2)) return false;

    if (v2.size() <= v1.size()) {
    largerVector = v1;
    smallerVector = v2;
    } else {
    largerVector = v2;
    smallerVector = v1;
    }

    // now do the matching
    if (largerVector.size()>=3) {
      for (Iterator iter = smallerVector.iterator();
                              iter.hasNext() ;) {
        token1 = (String) iter.next();
        if (largerVector.contains(token1))  matched_tokens++;
      } // for (Enumeration enum
    }
    if (matched_tokens >= largerVector.size()-1) return true;
    return false;
  }//matchRule13


  /** Tables for namematch info
    * (used by the namematch rules)
    */
  private void buildTables(AnnotationSet nameAllAnnots) {

    //reset the tables first
    cdg.clear();

    if (! extLists) {
    // i.e. get cdg from Lookup annotations
      // get all Lookup annotations
      tempMap.clear();
      tempMap.put("majorType", "cdg");
      //now get all lookup annotations which are cdg
      AnnotationSet nameAnnots =
        nameAllAnnots.get(LOOKUPNAME, tempMap);

      if ((nameAnnots ==null) || nameAnnots.isEmpty())
        return;

      Iterator iter = nameAnnots.iterator();
      while (iter.hasNext()) {
         Annotation annot = (Annotation)iter.next();
         // get the actual string
         Long offsetStartAnnot = annot.getStartNode().getOffset();
         Long offsetEndAnnot = annot.getEndNode().getOffset();
         try {
           gate.Document doc = nameAllAnnots.getDocument();
           String annotString =
                            doc.getContent().getContent(
                            offsetStartAnnot,offsetEndAnnot
                            ).toString();
                cdg.add(annotString);
         } catch (InvalidOffsetException ioe) {
             ioe.printStackTrace(Err.getPrintWriter());
         }
      }// while
    }//if
  }//buildTables

  /** substitute all multiple spaces, tabes and newlines
    * with a single space
    */
  public String regularExpressions ( String text, String replacement,
                                      String regEx) {
    String result = text;
    try {
      RE re = new RE(regEx);
      result = re.substituteAll( text,replacement);
    } catch (REException ree) {ree.printStackTrace();}
    return result;
  }//regularExpressions

} // public class OrthoMatcher

