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
  protected static final String KIND_FEATURE = "kind";
  protected static final String STRING_FEATURE = "string";
  protected static final String THE_VALUE = "The";


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

  protected boolean matchingUnknowns = true;

  /** This is an internal variable to indicate whether
   *  we matched using a rule that requires that
   *  the newly matched annotation matches all the others
   *  This is needed, because organizations can share
   *  first/last tokens like News and be different
   *
   * I have not implemented this yet, but might need to
   * Let's see how it tests
   */
  private   boolean allMatchingNeeded = false;

  //** Orthomatching is not case-sensitive by default*/
  protected boolean caseSensitive = false;

  protected FeatureMap queryFM = Factory.newFeatureMap();

//  protected ExecutionException executionException;

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
  //maps annotation ids to array lists of tokens
  protected HashMap tokensMap = new HashMap(150);

  protected Annotation shortAnnot, longAnnot;

  protected ArrayList tokensLongAnnot, tokensShortAnnot;

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
  public void execute() throws ExecutionException{

    //check the input
    if(document == null) {
      throw new ExecutionException(
        "No document for namematch!"
      );
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
      docCleanup();

    // creates the cdg list from the document
    //no need to create otherwise, coz already done in init()
    if (!extLists)
      buildTables(nameAllAnnots);

    //first match all name annotations
    matchNameAnnotations();

    //then match the unknown ones to all name ones
    if (matchingUnknowns)
      matchUnknown();

    // set the matches of the document
//    determineMatchesDocument();
    if (! matchesDocFeature.isEmpty()) {
      document.getFeatures().put(DOC_MATCHES_FEATURE, matchesDocFeature);

      //cannot do clear() as this has already been put on the document
      //so I need a new one for the next run of matcher
      matchesDocFeature = new ArrayList();
    }

//    Out.prln("Processed strings" + processedAnnots.values());
    //clean-up the internal data structures for next run
    nameAllAnnots = null;
    processedAnnots.clear();
    annots2Remove.clear();
    tokensMap.clear();
    matchesDocFeature = new ArrayList();
    longAnnot = null;
    shortAnnot = null;
    tokensLongAnnot = null;
    tokensShortAnnot = null;

  } // run()

  protected void matchNameAnnotations() throws ExecutionException{
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
            throw new ExecutionException
                                   ("Invalid offset of the annotation");
        }
        //convert to lower case if we are not doing a case sensitive match
        if (!caseSensitive)
          annotString = annotString.toLowerCase();

        //get the tokens
        List tokens = new ArrayList((Set)
                        nameAllAnnots.get("Token",
                          nameAnnot.getStartNode().getOffset(),
                          nameAnnot.getEndNode().getOffset()
                        ));
        //if no tokens to match, do nothing
        if (tokens.isEmpty())
          continue;
        Collections.sort(tokens, new gate.util.OffsetComparator());
        //check if these actually do not end after the name
        //needed coz new tokeniser conflates
        //strings with dashes. So British Gas-style is two tokens
        //instead of three. So cannot match properly British Gas
//        tokens = checkTokens(tokens);
        tokensMap.put(nameAnnot.getId(), tokens);

//        Out.prln("Matching annot " + nameAnnot + ": string " + annotString);

        //first check whether we have not matched such a string already
        //if so, just consider it matched, don't bother calling the rules
        if (processedAnnots.containsValue(annotString)) {
//          Out.prln("Contained string found " + annotString);
          updateMatches(nameAnnot, annotString);
          processedAnnots.put(nameAnnot.getId(), annotString);
          continue;
        } else if (processedAnnots.isEmpty()) {
          processedAnnots.put(nameAnnot.getId(), annotString);
          continue;
        }

        //if a person, then remove their title before matching
        if (nameAnnot.getType().equals(personType))
          annotString = containTitle(annotString, nameAnnot);
        else if (nameAnnot.getType().equals(organizationType))
          annotString = stripCDG(annotString, nameAnnot);

        //otherwise try matching with previous annotations
        matchWithPrevious(nameAnnot, annotString);

//        Out.prln("Putting in previous " + nameAnnot + ": string " + annotString);
        //finally add the current annotations to the processed map
        processedAnnots.put(nameAnnot.getId(), annotString);
      }//while through name annotations

    }//while through annotation types

  }

  protected void matchUnknown() throws ExecutionException {
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
          throw new ExecutionException
                                 ("Invalid offset of the annotation");
      }
      //convert to lower case if we are not doing a case sensitive match
      if (!caseSensitive)
        unknownString = unknownString.toLowerCase();

      //get the tokens
      List tokens = new ArrayList((Set)
                      nameAllAnnots.get("Token",
                        unknown.getStartNode().getOffset(),
                        unknown.getEndNode().getOffset()
                      ));
      if (tokens.isEmpty())
        continue;
      Collections.sort(tokens, new gate.util.OffsetComparator());
      tokensMap.put(unknown.getId(), tokens);


      //first check whether we have not matched such a string already
      //if so, just consider it matched, don't bother calling the rules
      if (processedAnnots.containsValue(unknownString)) {
        Annotation matchedAnnot = updateMatches(unknown, unknownString);
//        Out.prln("Matched " + unknown + "with string " + unknownString);
//        Out.prln("That's same as " + matchedAnnot);
        if (matchedAnnot.getType().equals(unknownType)) {
          annots2Remove.put(unknown.getId(),
                            annots2Remove.get(matchedAnnot.getId()));
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
    boolean matchedUnknown = false;

    Iterator prevIter = processedAnnots.keySet().iterator();
    while (prevIter.hasNext()) {
      Integer prevId = (Integer) prevIter.next();
      Annotation prevAnnot = nameAllAnnots.get(prevId);

      //check if the two are from the same type or the new one is unknown
      if (prevAnnot == null || (! prevAnnot.getType().equals(nameAnnot.getType())
          && ! nameAnnot.getType().equals(unknownType))
         )
        continue;
      //do not compare two unknown annotations either
      //they are only matched to those of known types
      if (  nameAnnot.getType().equals(unknownType)
            && prevAnnot.getType().equals(unknownType))
      continue;

      //check if we have already matched this annotation to the new one
      if (matchedAlready(nameAnnot, prevAnnot) )
        continue;

      // determine the title from annotation string
      //now changed to a rule, here we just match by gender
      if (prevAnnot.getType().equals(personType)) {
        String prevGender = (String) prevAnnot.getFeatures().get(GENDER_FEATURE);
        String nameGender = (String) nameAnnot.getFeatures().get(GENDER_FEATURE);
        if (   prevGender != null
            && nameGender != null
            && ( (nameGender.equalsIgnoreCase("female")
                  &&
                  prevGender.equalsIgnoreCase("male")
                  )
               ||
                  (prevGender.equalsIgnoreCase("female")
                   && nameGender.equalsIgnoreCase("male")
                  )
                )
            ) //if condition
          continue; //we don't have a match if the two genders are different

      }//if

      //if the two annotations match
      if (matchAnnotations(nameAnnot, annotString,  prevAnnot)) {
//        Out.prln("Matched " + shortName + "and " + longName);
        updateMatches(nameAnnot, prevAnnot);
        //if unknown annotation, we need to change to the new type
        if (nameAnnot.getType().equals(unknownType)) {
          matchedUnknown = true;
//          annots2Remove.put(nameAnnot.getId(), prevAnnot.getType());
          if (prevAnnot.getType().equals(unknownType))
            annots2Remove.put(nameAnnot.getId(),
                              annots2Remove.get(prevAnnot.getId()));
          else
            annots2Remove.put(nameAnnot.getId(), prevAnnot.getType());
         //also put an attribute to indicate that
          nameAnnot.getFeatures().put("NMRule", unknownType);
        }//if unknown
        break; //no need to match further
      }//if annotations matched

    }//while through previous annotations

    if (matchedUnknown)
      processedAnnots.put(nameAnnot.getId(), annotString);


  }//matchWithPrevious

  protected boolean matchAnnotations(Annotation newAnnot, String annotString,
                                     Annotation prevAnnot) {

    // find which annotation string of the two is longer
    //  this is useful for some of the matching rules
    String prevAnnotString = (String) processedAnnots.get(prevAnnot.getId());

    String longName = prevAnnotString;
    String shortName = annotString;
    longAnnot = prevAnnot;
    shortAnnot = newAnnot;

    if (shortName.length()>=longName.length()) {
      String temp = longName;
      longName = shortName;
      shortName = temp;
      Annotation tempAnn = longAnnot;
      longAnnot = shortAnnot;
      shortAnnot = tempAnn;
    }//if

    tokensLongAnnot = (ArrayList) tokensMap.get(longAnnot.getId());
    tokensShortAnnot = (ArrayList) tokensMap.get(shortAnnot.getId());

    List matchesList = (List) prevAnnot.getFeatures().get(MATCHES_FEATURE);
    if (matchesList == null || matchesList.isEmpty())
      return apply_rules_namematch(prevAnnot.getType(), shortName,longName);

    //if these two match, then let's see if all the other matching one will too
    //that's needed, because sometimes names can share a token (e.g., first or
    //last but not be the same
    if (apply_rules_namematch(prevAnnot.getType(), shortName,longName)) {
      List toMatchList = new ArrayList(matchesList);
//      if (newAnnot.getType().equals(unknownType))
//        Out.prln("Matching new " + annotString + " with annots " + toMatchList);
      toMatchList.remove(prevAnnot.getId());
      return matchOtherAnnots(toMatchList, newAnnot, annotString);
    }
    return false;
  }

  /** This method checkes whether the new annotation matches
   *  all annotations given in the toMatchList (it contains ids)
   *  The idea is that the new annotation needs to match all those,
   *  because assuming transitivity does not always work, when
   *  two different entities share a common token: e.g., BT Cellnet
   *  and BT and British Telecom.
  */
  protected boolean matchOtherAnnots( List toMatchList, Annotation newAnnot,
                                      String annotString) {

    //if the list is empty, then we're matching all right :-)
    if (toMatchList.isEmpty())
      return true;

    boolean matchedAll = true;
    int i = 0;

    while (matchedAll && i < toMatchList.size()) {
      Annotation prevAnnot = nameAllAnnots.get((Integer) toMatchList.get(i));

      // find which annotation string of the two is longer
      //  this is useful for some of the matching rules
      String prevAnnotString = (String) processedAnnots.get(prevAnnot.getId());
      if (prevAnnotString == null)
        try {
          prevAnnotString = document.getContent().getContent(
            prevAnnot.getStartNode().getOffset(),
            prevAnnot.getEndNode().getOffset()
            ).toString();
        } catch (InvalidOffsetException ioe) {
          return false;
        }//try


      String longName = prevAnnotString;
      String shortName = annotString;
      longAnnot = prevAnnot;
      shortAnnot = newAnnot;

      if (shortName.length()>=longName.length()) {
        String temp = longName;
        longName = shortName;
        shortName = temp;
        Annotation tempAnn = longAnnot;
        longAnnot = shortAnnot;
        shortAnnot = tempAnn;
      }//if

      tokensLongAnnot = (ArrayList) tokensMap.get(longAnnot.getId());
      tokensShortAnnot = (ArrayList) tokensMap.get(shortAnnot.getId());

      matchedAll = apply_rules_namematch(prevAnnot.getType(), shortName,longName);
//      if (newAnnot.getType().equals(unknownType))
//        Out.prln("Loop: " + shortName + " and " + longName + ": result: " + matchedAll);

      i++;
    }//while
    return matchedAll;
  }


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
    Integer id;

    //first find a processed annotation with the same string
    Iterator iter = processedAnnots.keySet().iterator();
    while (iter.hasNext()) {
      id = (Integer) iter.next();
      String oldString = (String) processedAnnots.get(id);
      if (annotString.equals(oldString)) {
        matchedAnnot = nameAllAnnots.get(id);
        break;
      }//if
    }//while

    if (matchedAnnot == null) return null;
    //if the two matching annotations are of different type which is not
    //unknown, do not match them
    if (! matchedAnnot.getType().equals(newAnnot.getType())
        && !newAnnot.getType().equals(unknownType) )
      return matchedAnnot;

    List matchesList = (List) matchedAnnot.getFeatures().get(MATCHES_FEATURE);
    if ((matchesList == null) || matchesList.isEmpty()) {
      //no previous matches, so need to add
      if (matchesList == null) {
        matchesList = new ArrayList();
        matchedAnnot.getFeatures().put(MATCHES_FEATURE, matchesList);
        matchesDocFeature.add(matchesList);
      }//if
      matchesList.add(matchedAnnot.getId());
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


  protected void docCleanup() {
    document.getFeatures().remove(DOC_MATCHES_FEATURE);

    //get all annotations that have a matches feature
    HashSet fNames = new HashSet();
    fNames.add(MATCHES_FEATURE);
    AnnotationSet annots =
                  nameAllAnnots.get(null, fNames);

//    Out.prln("Annots to cleanup" + annots);

    if (annots == null || annots.isEmpty())
      return;

    Iterator iter = annots.iterator();
    while (iter.hasNext()) {
      while (iter.hasNext())
        ((Annotation) iter.next()).getFeatures().remove(MATCHES_FEATURE);
    } //while
  }//cleanup

  /** return a person name without title */
  protected String containTitle (String annotString, Annotation annot)
                      throws ExecutionException {
    // get the offsets
    Long startAnnot = annot.getStartNode().getOffset();
    Long endAnnot = annot.getEndNode().getOffset();

    // determine "Lookup" annotation set
    queryFM.clear();
    queryFM.put("majorType", "title");
    AnnotationSet as =
      nameAllAnnots.get(startAnnot,endAnnot).get("Lookup", queryFM);
    if (as !=null && ! as.isEmpty()) {
      List titles = new ArrayList((Set)as);
      Collections.sort(titles, new gate.util.OffsetComparator());

      Iterator iter = titles.iterator();
      while (iter.hasNext()) {
        Annotation titleAnn = (Annotation)(iter.next());

        //we've not found a title at the start offset,
        //there's no point in looking further
        //coz titles come first
        if (titleAnn.getStartNode().getOffset().compareTo(startAnnot) != 0)
          return annotString;

        try {
          // the title from the current annotation
          String annotTitle =
            document.getContent().getContent(
              titleAnn.getStartNode().getOffset(),
              titleAnn.getEndNode().getOffset()
            ).toString();

          // eliminate the title from annotation string and return the result
          if (annotTitle.length()<annotString.length()) {
            //remove from the array of tokens, so then we can compare properly
            //the remaining tokens
//            Out.prln("Removing title from: " + annot + " with string " + annotString);
//            Out.prln("Tokens are" + tokensMap.get(annot.getId()));
//            Out.prln("Title is" + annotTitle);
            ((ArrayList) tokensMap.get(annot.getId())).remove(0);
            return annotString.substring(
                                 annotTitle.length()+1,annotString.length());
          }
        } catch (InvalidOffsetException ioe) {
            throw new ExecutionException
                               ("Invalid offset of the annotation");
        }//try
      }// while
    }//if
    return annotString;

  }

  /** return an organization  without a designator and starting The*/
  protected String stripCDG (String annotString, Annotation annot){

    ArrayList tokens = (ArrayList) tokensMap.get(annot.getId());

    //strip starting The first
    if ( ((String) ((Annotation) tokens.get(0)
          ).getFeatures().get(STRING_FEATURE)).equalsIgnoreCase(THE_VALUE))
      tokens.remove(0);

    if ( cdg.contains( ((Annotation) tokens.get(tokens.size()-1)
          ).getFeatures().get(STRING_FEATURE)) )
      tokens.remove(tokens.size()-1);

    StringBuffer newString = new StringBuffer(50);
    for (int i = 0; i < tokens.size(); i++){
      newString.append((String) ((Annotation) tokens.get(i)
          ).getFeatures().get(STRING_FEATURE) );
      if (i != tokens.size()-1)
        newString.append(" ");
    }

    if (caseSensitive)
      return newString.toString();

    return newString.toString().toLowerCase();
  }

/*
  public void check() throws ExecutionException {
    if (executionException != null) {
      ExecutionException e = executionException;
      executionException = null;
      throw e;
    }
  } // check()
*/

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
            matchRule3(longName, shortName)
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
//                  matchRule8(longName, shortName)
//               ||
                  matchRule9(longName, shortName)
               ||
                  matchRule10(longName, shortName)
               ||
                  matchRule11(longName, shortName)
               ||
                  matchRule12(longName, shortName)
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
                  matchRule14(longName, shortName)
               || //kalina: added this, so it matches names when contain more
                  //than one first and one last name
                  matchRule13(longName, shortName)
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
           boolean matchCase) {
//    Out.prln("Rule1: Matching " + s1 + "and " + s2);

    boolean matched = false;
    if (!matchCase)
        matched = s1.equalsIgnoreCase(s2);
    else matched =  s1.equals(s2) ;
//kalina: do not remove, nice for debug
//    if (matched && (s2.equalsIgnoreCase("news") || s1.equalsIgnoreCase("news")))
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
  public boolean matchRule3(String s1, //long string
                             String s2) { //short string

    if (s2.endsWith("'s") || s2.endsWith("'")
        ||(s1.endsWith("'s")|| s1.endsWith("'"))) {


      String s2_poss = null;

      if (!s2.endsWith("'s")) s2_poss = s2.concat("'s");
      else s2_poss = s2.concat("'");

      if (s2_poss != null && matchRule1(s1, s2_poss,caseSensitive)) return true;

      // now check the second case i.e. "Standard and Poor" == "Standard's"
      String token = (String)
        ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(STRING_FEATURE);

      if (!token.endsWith("'s")) s2_poss = token.concat("'s");
      else s2_poss = token.concat("'");

      if (s2_poss != null && matchRule1(s2_poss,s2,caseSensitive)) return true;

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

    boolean allTokensMatch = true;

    Iterator tokensLongAnnotIter = tokensLongAnnot.iterator();
    Iterator tokensShortAnnotIter = tokensShortAnnot.iterator();
    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
      Annotation token = (Annotation) tokensLongAnnotIter.next();
      if (((String)token.getFeatures().get(KIND_FEATURE)).equals("punctuation"))
        continue;
//      Out.prln("Matching" + tokensLongAnnot + " with " + tokensShortAnnot);
      if (! token.getFeatures().get(STRING_FEATURE).equals(
             ((Annotation) tokensShortAnnotIter.next()).getFeatures().get(STRING_FEATURE))) {
        allTokensMatch = false;
        break;
      } // if (!tokensLongAnnot.nextToken()
    } // while
//    Out.prln("result is: " + allTokensMatch);
    return allTokensMatch;
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

    //do not match numbers by this rule
    if (tokensLongAnnot.size()> 1 &&
        ((Annotation) tokensLongAnnot.get(0)).getFeatures().get("kind").equals("number"))
      return false;

//    if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//      Out.prln("Rule 5: " + s1 + "and " + s2);
    if (tokensLongAnnot.size()>1)
      return matchRule1((String)
                      ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(STRING_FEATURE),
                      s2,
                      caseSensitive);

    return false;

  }//matchRule5

  /**
    * RULE #6: if one name is the acronym of the other
    * e.g. "Imperial Chemical Industries" == "ICI"
    * Applied to: organisation annotations only
    */
  public boolean matchRule6(String s1,
           String s2) {

    int i = 0;

    //don't try it unless the second name is only one token
    if (tokensShortAnnot.size() != 1)
      return false;

    //if the number of tokens without the the is not the same as the length
    //of the second token, there's no point in comparing
    if (tokensLongAnnot.size() != s2.length())
      return false;

//    Out.prln("Acronym: Matching " + s1 + "and " + s2);
    StringBuffer acronym_s1 = new StringBuffer("");
    StringBuffer acronymDot_s1 = new StringBuffer("");

    for ( ;i < tokensLongAnnot.size(); i++ ) {
      String toAppend = ( (String) ((Annotation) tokensLongAnnot.get(i)
                         ).getFeatures().get(STRING_FEATURE)).substring(0,1);
      acronym_s1.append(toAppend);
      acronymDot_s1.append(toAppend);
      acronymDot_s1.append(".");
    }

//    Out.prln("Acronym: To Match " + acronym_s1 + "and " + s2);
//    Out.prln("Result: " + matchRule1(acronym_s1.toString(),s2,caseSensitive));

    if (matchRule1(acronym_s1.toString(),s2,caseSensitive) ||
        matchRule1(acronymDot_s1.toString(),s2,caseSensitive) )
      return true;

    return false;
  }//matchRule6

  /**
    * RULE #7: if one of the tokens in one of the
    * names is in the list of separators eg. "&"
    * then check if the token before the separator
    * matches the other name
    * e.g. "R.H. Macy & Co." == "Macy"
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule7(String s1,
           String s2) {

    //don't try it unless the second string is just one token
    if (tokensShortAnnot.size() != 1)
      return false;

    String previous_token = null;

    for (int i = 0;  i < tokensLongAnnot.size(); i++ ) {
      if (connector.containsKey( ((Annotation) tokensLongAnnot.get(i)
          ).getFeatures().get(STRING_FEATURE) )) {
        previous_token = (String) ((Annotation) tokensLongAnnot.get(i-1)
                                    ).getFeatures().get(STRING_FEATURE);

        break;
      }
    }

    //now match previous_token with other name
    if (previous_token != null) {
//      if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//        Out.prln("Rule7");
      return matchRule1(previous_token,s2,caseSensitive);
    }
    return false;
  }//matchRule7

  /**
   * This rule is now obsolete, as The and the trailing CDG
   * are stripped before matching.
   * DO NOT CALL!!!
   *
    * RULE #8: if the names match, ignoring The and
    * and trailing company designator (which have already been stripped)
    * e.g. "The Magic Tricks Co." == "Magic Tricks"
    * Condition(s): case-sensitive match
    * Applied to: organisation annotations only
    */
  public boolean matchRule8(String s1,
           String s2) {
    Out.prln("OrthoMatcher warning: This rule has been discontinued!");
/*
    if (s1.startsWith("The ")) s1 = s1.substring(4);
    if (s2.startsWith("The ")) s2 = s2.substring(4);

    // check that cdg is not empty
    if (!cdg.isEmpty()) {
      String stringToTokenize1 = s1;
      StringTokenizer tokensLongAnnot = new StringTokenizer(stringToTokenize1," ");

      String stringToTokenize2 = s2;
      StringTokenizer tokensShortAnnot = new StringTokenizer(stringToTokenize2," ");
      String token = null;
      String cdg1 = null;
      String cdg2 = null;

      s1 = "";
      s2 = "";

      //check last token of s1
      while (tokensLongAnnot.hasMoreTokens()) {
        token = tokensLongAnnot.nextToken();
        if (!tokensLongAnnot.hasMoreTokens()
            && cdg.contains(token)) cdg1=token;
        else s1 = s1+token;
      }

      // do the same for s2
      while (tokensShortAnnot.hasMoreTokens()) {
        token = tokensShortAnnot.nextToken();
        if (!tokensShortAnnot.hasMoreTokens()
          && cdg.contains(token)) cdg2=token;
        else s2 = s2+token;
      }

      // if the company designators are different
      // then they are NOT the same organisations
      if ((cdg1!=null && cdg2!=null)
    && !cdg1.equalsIgnoreCase(cdg2)) return false;
    }
    if (!s1.equals("") && !s2.equals("")) return matchRule1(s1,s2,caseSensitive);
*/
    return false;

  }//matchRule8

  /**
    * RULE #9: does one of the names match the token
    * just before a trailing company designator
    * in the other name?
    * The company designator has already been chopped off,
    * so the token before it, is in fact the last token
    * e.g. "R.H. Macy Co." == "Macy"
    * Applied to: organisation annotations only
    */
  public boolean matchRule9(String s1,
           String s2) {

//    if (s1.equalsIgnoreCase("news") || s2.equalsIgnoreCase("news"))
//      Out.prln("Rule 9 " + s1 + " and " + s2);
    String s1_short = (String)
                      ((Annotation) tokensLongAnnot.get(
                          tokensLongAnnot.size()-1)).getFeatures().get(STRING_FEATURE);
//    Out.prln("Converted to " + s1_short);
    if (tokensLongAnnot.size()>1)
      return matchRule1(s1_short,
                      s2,
                      caseSensitive);

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

    String token = null;
    String previous_token = null;
    String next_token = null;
    boolean invoke_rule=false;

    if (tokensLongAnnot.size() >= 3
        && tokensShortAnnot.size() >= 2) {

      // first get the tokens before and after the preposition
      int i = 0;
      for (; i< tokensLongAnnot.size(); i++) {
        token = (String)
                  ((Annotation) tokensLongAnnot.get(i)).getFeatures().get(STRING_FEATURE);
        if (prepos.containsKey(token)) {
          invoke_rule=true;
          break;
        }//if
        previous_token = token;
      }//while

      if (! invoke_rule)
        return false;

      if (i < tokensLongAnnot.size()
          && previous_token != null)
        next_token= (String)
                    ((Annotation) tokensLongAnnot.get(i++)).getFeatures().get(STRING_FEATURE);
      else return false;

      String s21 = (String)
                    ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(STRING_FEATURE);
      String s22 = (String)
                    ((Annotation) tokensShortAnnot.get(1)).getFeatures().get(STRING_FEATURE);
      // then compare (in reverse) with the first two tokens of s2
      if (matchRule1(next_token,(String) s21,caseSensitive)
          && matchRule1(previous_token, s22,caseSensitive))
        return true ;
    }//if (tokensLongAnnot.countTokens() >= 3
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

    String token11 = null;
    String token12 = null;
    String token21 = null;
    String token22 = null;

    if (tokensLongAnnot.size() < 2)
      return false;

    // 1st get the first two tokens of s1
    token11 = (String)
                ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(STRING_FEATURE);
    token12 = (String)
                ((Annotation) tokensLongAnnot.get(1)).getFeatures().get(STRING_FEATURE);

    // now check for the first case i.e. "Pan American" == "Pan Am"
    if (tokensShortAnnot.size() == 2)  {

      token21 = (String)
                  ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(STRING_FEATURE);
      token22 = (String)
                  ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(STRING_FEATURE);

      if (token11.startsWith(token21)
          && token12.startsWith(token22))
        return true;

    } // if (tokensShortAnnot.countTokens() == 2)

    // now the second case e.g.  "Communications Satellite" == "ComSat"
    else if (tokensShortAnnot.size()==1 && s2.length()>=3) {

      // split the token into possible contractions
      // ignore case for matching
      for (int i=2;i<s2.length();i++) {
        token21=s2.substring(0,i+1);
        token22=s2.substring(i+1);

        if (token11.startsWith(token21)
            && token12.startsWith(token22))
          return true;
      }// for
    } // else if

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

    // first do the easy case e.g. "Pan American" == "Pan Am"

    if (tokensLongAnnot.size()>1 && tokensShortAnnot.size()>1) {
//     Out.prln("Rule 12");

      // get first and last tokens of s1 & s2
      String s1_first = (String)
                     ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(STRING_FEATURE);
      String s2_first = (String)
                     ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(STRING_FEATURE);

      if (!matchRule1(s1_first,s2_first,caseSensitive))
        return false;

      String s1_last = (String)
         ((Annotation) tokensLongAnnot.get(tokensLongAnnot.size()-1)).getFeatures().get(STRING_FEATURE);
      String s2_last = (String)
         ((Annotation) tokensShortAnnot.get(tokensShortAnnot.size()-1)).getFeatures().get(STRING_FEATURE);

      return matchRule1(s1_last,s2_last,caseSensitive);
    } // if (tokensLongAnnot.countTokens()>1
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


    String token1 = null;
    String token2 = null;

    int matched_tokens = 0, mismatches = 0;;

    // if names < 2 words then rule is invalid
    if (tokensLongAnnot.size() < 3 || tokensShortAnnot.size() < 2) return false;

//    if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin")) {
//      Out.prln("Rule 13: Matching tokens" + tokensLongAnnot);
//      Out.prln("with tokens " + tokensShortAnnot);
//    }

    // now do the matching
    for (int i=0,j= 0; i < tokensShortAnnot.size() && mismatches < 2; i++) {

//      Out.prln("i = " + i);
//      Out.prln("j = " + j);
      if ( ((Annotation) tokensLongAnnot.get(j)).getFeatures().get(STRING_FEATURE).equals(
           ((Annotation) tokensShortAnnot.get(i)).getFeatures().get(STRING_FEATURE)) ) {
        matched_tokens++;
        j++;
      } else
        mismatches++;
    } // for

    if (matched_tokens >= tokensLongAnnot.size()-1)
      return true;

    return false;
  }//matchRule13

  /**
    * RULE #14: if the last token of one name
    * matches the second name
    * e.g. "Hamish Cunningham" == "Cunningham"
    * Condition(s): case-insensitive match
    * Applied to: all person annotations
    */
  public boolean matchRule14(String s1,
           String s2) {

//    if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//      Out.prln("Rule 14 " + s1 + " and " + s2);
    String s1_short = (String)
                      ((Annotation) tokensLongAnnot.get(
                          tokensLongAnnot.size()-1)).getFeatures().get(STRING_FEATURE);
//    Out.prln("Converted to " + s1_short);
    if (tokensLongAnnot.size()>1)
      return matchRule1(s1_short,
                      s2,
                      caseSensitive);

    return false;

  }//matchRule14


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

