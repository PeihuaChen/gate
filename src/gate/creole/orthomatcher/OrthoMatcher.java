/*
 *  OrthoMatcher.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 24/August/2001
 *
 *  Major update by Andrew Borthwick of Spock Networks, 11/13/2007 - 8/3/2008:
 *    1.  matchWithPrevious now searches for matching annotations in order, starting from current and working backwards
 *    until it finds a match.  This compares with the previous behavior, which searched randomly among previous annotations
 *    for a match (because it used an iterator across an AnnotationSet, whereas now we iterate across an ArrayList<Annotation>)
 *    2.  We no longer require that identical strings always refer to the same entity.  We can correctly match
 *    the sequence "David Jones ... David ... David Smith ... David" as referring to two people, tying the first
 *    David to "David Jones" and the second David to "David Smith".  Ditto with David Jones .. Mr. Jones .. 
 *    Richard Jones .. Mr. Jones
 *    3.  We now allow for nickname matches for Persons (David = Dave) via the "fuzzyMatch" method which is referenced
 *    in some of the matching rules.
 *    4.  Optional parameter highPrecisionOrgs only allows high precision matches for organizations and
 *    turns off the riskier rules.  Under this option, need to match on something like IBM = IBM Corp.
 *    5.  Various fixes to a number of rules
 *
 *  $Id: OrthoMatcher.java 8929 2007-07-12 16:49:55Z ian_roberts $
 */

package gate.creole.orthomatcher;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import gate.*;
import gate.creole.*;
import gate.util.*;

public class OrthoMatcher extends AbstractLanguageAnalyser
implements ANNIEConstants{
  protected static final Logger log = Logger.getLogger(OrthoMatcher.class);

  public static final boolean DEBUG = false;

  public static final String
  OM_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
  OM_ANN_SET_PARAMETER_NAME = "annotationSetName";

  public static final String
  OM_CASE_SENSITIVE_PARAMETER_NAME = "caseSensitive";

  public static final String
  OM_ANN_TYPES_PARAMETER_NAME = "annotationTypes";

  public static final String
  OM_ORG_TYPE_PARAMETER_NAME = "organizationType";

  public static final String
  OM_PERSON_TYPE_PARAMETER_NAME = "personType";

  public static final String
  OM_EXT_LISTS_PARAMETER_NAME = "extLists";

  protected static final String CDGLISTNAME = "cdg";
  protected static final String ALIASLISTNAME = "alias";
  protected static final String ARTLISTNAME = "def_art";
  protected static final String PREPLISTNAME = "prepos";
  protected static final String CONNECTORLISTNAME = "connector";
  protected static final String SPURLISTNAME = "spur_match";

  protected static final String PUNCTUATION_VALUE = "punctuation";
  protected static final String THE_VALUE = "The";


  /**the name of the annotation set*/
  protected String annotationSetName;

  /** the types of the annotation */
  protected List annotationTypes = new ArrayList(10);

  /** the organization type*/
  protected String organizationType = ORGANIZATION_ANNOTATION_TYPE;

  /** the person type*/
  protected String personType = PERSON_ANNOTATION_TYPE;

  protected String unknownType = "Unknown";

  /** internal or external list */
  protected boolean extLists = true;

  /** Use only high precision rules for Organizations */
  protected Boolean highPrecisionOrgs = false;

  /** matching unknowns or not*/
  protected boolean matchingUnknowns = true;

  /** This is an internal variable to indicate whether
   *  we matched using a rule that requires that
   *  the newly matched annotation matches all the others
   *  This is needed, because organizations can share
   *  first/last tokens like News and be different
   */
  private   boolean allMatchingNeeded = false;

  //** Orthomatching is not case-sensitive by default*/
  protected boolean caseSensitive = false;

  protected FeatureMap queryFM = Factory.newFeatureMap();

//protected ExecutionException executionException;

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

  protected ArrayList<Annotation> tokensLongAnnot, tokensShortAnnot;

  /** a feature map to be used when retrieving annotations
   *  declared here so can be reused for efficiency
   *  clear() before each use
   */
  protected FeatureMap tempMap = Factory.newFeatureMap();

  /** the size of the buffer */
  private final static int BUFF_SIZE = 65000;

  /**
   * URL to the file containing the definition for this orthomatcher
   */
  private java.net.URL definitionFileURL;

  private Double minimumNicknameLikelihood;

  /** The encoding used for the definition file and associated lists.*/
  private String encoding;

  private Map<String,HashSet<String>> nicknameMap;

  /** @link dependency */
  /*#OrthoMatcher lnkOrthoMatcher;*/

  public OrthoMatcher () {
    annotationTypes.add(organizationType);
    annotationTypes.add(personType);
    annotationTypes.add("Location");
    annotationTypes.add("Date");
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    //initialise the list of annotations which we will match
    if(definitionFileURL == null){
      throw new ResourceInstantiationException(
      "No URL provided for the definition file!");
    }

    //at this point we have the definition file
    try{
      BufferedReader reader = new BufferedReader(
              new InputStreamReader(definitionFileURL.openStream(),
                      encoding));
      String lineRead = null;
      boolean foundANickname = false;
      while ((lineRead = reader.readLine()) != null){
        int index = lineRead.indexOf(":");
        if (index != -1){
          String nameFile = lineRead.substring(0,index);
          String nameList = lineRead.substring(index+1,lineRead.length());
          if (nameList.equals("nickname")) {
            initNicknames(nameFile,encoding, minimumNicknameLikelihood);
            foundANickname = true;
          }
          else {
            createAnnotList(nameFile,nameList);
          }
        }// if
      }//while
      reader.close();
      if (!foundANickname) {
        throw new ResourceInstantiationException(
                "No entry for nickname provided in definition file!");
      }

    }catch(IOException ioe){
      throw new ResourceInstantiationException(ioe);
    }

    return this;
  } // init()

  protected void initNicknames(String nicknameRelativeUrl,
          String nicknameFileEncoding, Double minimumNicknameLikelihood) throws IOException {
    Pattern spacePat = Pattern.compile("(\\s+)");
    nicknameMap = new HashMap<String,HashSet<String>>();

    //create the relative URL
    URL fileURL = new URL(definitionFileURL, nicknameRelativeUrl);
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(fileURL.openStream(),
                    nicknameFileEncoding));
    String lineRead = null;
    int ctr = 0;
    while ((lineRead = reader.readLine()) != null){
      if (lineRead.length() == 0 || lineRead.charAt(0) == '#') {
        continue;
      }

      ArrayList<String> nickNameLine =
        new ArrayList<String>(Arrays.asList(spacePat.split(lineRead.toLowerCase().trim())));
      if (nickNameLine.size() != 3 &&
              (nickNameLine.size() != 4 && ((nickNameLine.get(3) != "M") || nickNameLine.get(3) != "F"))) {
        continue;
      }
      if (round2Places(Double.valueOf(nickNameLine.get(2))) < round2Places(minimumNicknameLikelihood)) {
        continue;
      }
      if (nicknameMap.containsKey(nickNameLine.get(0))) {
        /*        System.out.println("Adding to existing nickname of " + nickNameLine.get(0) + " "
            + nickNameLine.get(1));*/
        nicknameMap.get(nickNameLine.get(0)).add(nickNameLine.get(1));
      }
      else {
        /*          System.out.println("Adding new nickname of " + nickNameLine.get(0) + " "
              + nickNameLine.get(1));*/
        nicknameMap.put(nickNameLine.get(0),
                new HashSet<String>(Collections.singleton(nickNameLine.get(1))));
      }
    }
  }

  public Double round2Places(Double input) {
    return Math.round(input*100.0)/100.0;
  }


  /**  Run the resource. It doesn't make sense not to override
   *  this in subclasses so the default implementation signals an
   *  exception.
   */
  public void execute() throws ExecutionException{
    try{
      //check the input
      if(document == null) {
        throw new ExecutionException(
                "No document for namematch!"
        );
      }
      if (log.isInfoEnabled()) {
        log.info("OrthoMatcher started processing: " + document.getName() + "---------------");
      }
      fireStatusChanged("OrthoMatcher processing: " +  document.getName());

      // get the annotations from document
      if ((annotationSetName == null)|| (annotationSetName.equals("")))
        nameAllAnnots = document.getAnnotations();
      else
        nameAllAnnots = document.getAnnotations(annotationSetName);

      //if none found, print warning and exit
      if ((nameAllAnnots == null) || nameAllAnnots.isEmpty()) {
        Out.prln("OrthoMatcher Warning: No annotations found for processing");
        return;
      }

      //check if we've been run on this document before
      //and clean the doc if needed
      docCleanup();
      Map matchesMap = (Map)document.getFeatures().
      get(DOCUMENT_COREF_FEATURE_NAME);

      // creates the cdg list from the document
      //no need to create otherwise, coz already done in init()
      if (!extLists)
        buildTables(nameAllAnnots);

      //Match all name annotations and unknown annotations
      matchNameAnnotations();

      // set the matches of the document
      //    determineMatchesDocument();
      if (! matchesDocFeature.isEmpty()) {
        if(matchesMap == null){
          matchesMap = new HashMap();
        }
        matchesMap.put(nameAllAnnots.getName(), matchesDocFeature);
        // System.out.println("matchesMap is: " + matchesMap);
        //we need to put it even if it was already present in order to triger
        //the update events
        document.getFeatures().put(DOCUMENT_COREF_FEATURE_NAME, matchesMap);

        //cannot do clear() as this has already been put on the document
        //so I need a new one for the next run of matcher
        matchesDocFeature = new ArrayList();
        fireStatusChanged("OrthoMatcher completed");
        log.info("OrthoMatcher completed-----------------------------------------------");
      }
    }finally{
      //make sure the cleanup happens even if there are errors.
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
    }
  } // run()

  protected void matchNameAnnotations() throws ExecutionException{
    // go through all the annotation types
    Iterator iterAnnotationTypes = annotationTypes.iterator();
    while (iterAnnotationTypes.hasNext()) {
      String annotationType = (String)iterAnnotationTypes.next();

      AnnotationSet nameAnnots = nameAllAnnots.get(annotationType);

      // continue if no such annotations exist
      if (nameAnnots.isEmpty()) continue;

      AnnotationSet tokensNameAS = nameAllAnnots.get(TOKEN_ANNOTATION_TYPE);
      if (tokensNameAS.isEmpty()) continue;

      ArrayList<Annotation> sortedNameAnnots = new ArrayList<Annotation>(nameAnnots);
      Collections.<Annotation>sort(sortedNameAnnots,new OffsetComparator());
      for (int snaIndex = 0;snaIndex < sortedNameAnnots.size();snaIndex++) {
        Annotation tempAnnot = sortedNameAnnots.get(snaIndex);
        Annotation nameAnnot = nameAllAnnots.get(tempAnnot.getId()); // Not sure if this matters
        Integer id = nameAnnot.getId();

        // get string and value
        String annotString = getStringForAnnotation(nameAnnot, document);

        //convert to lower case if we are not doing a case sensitive match
        if (!caseSensitive)
          annotString = annotString.toLowerCase();

        // System.out.println("Now processing the annotation: " + annotString + "|" + nameAnnot);

        // get the tokens
        List tokens = new ArrayList(tokensNameAS.getContained(nameAnnot.getStartNode().getOffset(),
                nameAnnot.getEndNode().getOffset()));

        //if no tokens to match, do nothing
        if (tokens.isEmpty()) {
          Out.prln("Didn't find any tokens for the following annotation.  This is weird.  \n String:  " + getStringForAnnotation(nameAnnot, document) +
                  "\tOffset:  " + nameAnnot.getStartNode().getOffset());
          continue;
        }
        Collections.sort(tokens, new gate.util.OffsetComparator());
        //check if these actually do not end after the name
        //needed coz new tokeniser conflates
        //strings with dashes. So British Gas-style is two tokens
        //instead of three. So cannot match properly British Gas
//      tokens = checkTokens(tokens);
        tokensMap.put(nameAnnot.getId(), tokens);

//      Out.prln("Matching annot " + nameAnnot + ": string " + annotString);

        //first check whether we have not matched such a string already
        //if so, just consider it matched, don't bother calling the rules
        // Exception:  AB, Spock:
        // Note that we require one-token Person annotations to be matched even if an identical string
        // has been matched earlier because there could be multiple people named "David", for instance,
        // on a page.
        if (processedAnnots.containsValue(annotString) &&
                (! ((nameAnnot.getType() == personType) && (tokens.size() == 1)))) {
          // System.out.println("Found an exact match for " + annotString);
          Annotation returnAnnot = updateMatches(nameAnnot, annotString);
          // System.out.println("Return value was: " + returnAnnot);
          if (returnAnnot != null) {
            // System.out.println("We're sure this is an exact match: " + annotString);
            processedAnnots.put(nameAnnot.getId(), annotString);
            // System.out.println("Just put " + annotString + " on processedAnnots");
            continue;
          }
        } else if (processedAnnots.isEmpty()) {
          // System.out.println("First item put in processedAnnots: " + annotString);
          processedAnnots.put(nameAnnot.getId(), annotString);
          continue;
        }

        //if a person, then remove their title before matching
        if (nameAnnot.getType().equals(personType))
          annotString = containTitle(annotString, nameAnnot);
        else if (nameAnnot.getType().equals(organizationType))
          annotString = stripCDG(annotString, nameAnnot);

        if(null == annotString || "".equals(annotString)) {
          Out.prln("Just found a null or empty string");
          continue;
        }
        //otherwise try matching with previous annotations
        matchWithPrevious(nameAnnot, annotString,sortedNameAnnots,snaIndex);

        // Out.prln("Putting in previous " + nameAnnot + ": string " + annotString);
        //finally add the current annotations to the processed map
        processedAnnots.put(nameAnnot.getId(), annotString);
      }//while through name annotations
      if (matchingUnknowns) {
        matchUnknown(sortedNameAnnots);
      }
    }//while through annotation types

  }

  protected void matchUnknown(ArrayList<Annotation> sortedAnnotationsForAType) throws ExecutionException {
    //get all Unknown annotations
    AnnotationSet unknownAnnots = nameAllAnnots.get(unknownType);
    annots2Remove.clear();
    if (unknownAnnots.isEmpty()) return;

    AnnotationSet nameAllTokens = nameAllAnnots.get(TOKEN_ANNOTATION_TYPE);
    if (nameAllTokens.isEmpty()) return;

    Iterator<Annotation> iter = unknownAnnots.iterator();
    //loop through the unknown annots
    while (iter.hasNext()) {
      Annotation unknown = iter.next();

      // get string and value
      String unknownString = getStringForAnnotation(unknown, document);
      //convert to lower case if we are not doing a case sensitive match
      if (!caseSensitive)
        unknownString = unknownString.toLowerCase();

      // System.out.println("Now trying to match the unknown string: " + unknownString);
      //get the tokens
      List tokens = new ArrayList((Set)
              nameAllTokens.getContained(
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
        if (matchedAnnot == null) {
          System.out.println("Unable to find the annotation: " +
                  getStringForAnnotation(unknown, document) +
          " in matchUnknown");
        }
        else {
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
      }

      //check if we should do sub-string matching in case it's hyphenated
      //for example US-led
      if (tokens.size() == 1
              && "hyphen".equals(unknown.getFeatures().get(TOKEN_KIND_FEATURE_NAME))) {
        if (matchHyphenatedUnknowns(unknown, unknownString, iter))
          continue;
      }//if

      // TODO:  The below results in a assigning the unknown's to the last annotation that it matches in a document.
      // It would probably be better to first start with things which precede the current unknown and then do
      // annotations after
      matchWithPrevious(unknown, unknownString,sortedAnnotationsForAType,sortedAnnotationsForAType.size() - 1);

    } //while though unknowns

    if (! annots2Remove.isEmpty()) {
      Iterator unknownIter = annots2Remove.keySet().iterator();
      while (unknownIter.hasNext()) {
        Integer unknId = (Integer) unknownIter.next();
        Annotation unknown = nameAllAnnots.get(unknId);
        Integer newID = nameAllAnnots.add(
                unknown.getStartNode(),
                unknown.getEndNode(),
                (String) annots2Remove.get(unknId),
                unknown.getFeatures()
        );
        nameAllAnnots.remove(unknown);

        //change the id in the matches list
        List mList = (List)unknown.getFeatures().
        get(ANNOTATION_COREF_FEATURE_NAME);
        mList.remove(unknId);
        mList.add(newID);
      }//while
    }//if
  }

  private boolean matchHyphenatedUnknowns(Annotation unknown, String unknownString,
          Iterator iter){
    boolean matched = false;

    //only take the substring before the hyphen
    int stringEnd = unknownString.indexOf("-");
    unknownString = unknownString.substring(0, stringEnd);
    //check if we've already matched this string
    //because only exact match of the substring are considered
    if (processedAnnots.containsValue(unknownString)) {
      matched = true;
      Annotation matchedAnnot = updateMatches(unknown, unknownString);
      //only do the matching if not a person, because we do not match
      //those on sub-strings
      iter.remove();
      String newType;
      if (matchedAnnot.getType().equals(unknownType))
        newType = (String)annots2Remove.get(matchedAnnot.getId());
      else
        newType = matchedAnnot.getType();

      Integer newID = new Integer(-1);
      try {
        newID = nameAllAnnots.add(
                unknown.getStartNode().getOffset(),
                new Long(unknown.getStartNode().getOffset().longValue()
                        + stringEnd),
                        newType,
                        unknown.getFeatures()
        );
      } catch (InvalidOffsetException ex) {
        throw new GateRuntimeException(ex.getMessage());
      }
      nameAllAnnots.remove(unknown);

      //change the id in the matches list
      List mList = (List)unknown.getFeatures().
      get(ANNOTATION_COREF_FEATURE_NAME);
      mList.remove(unknown.getId());
      mList.add(newID);

    }
    return matched;
  }

  /**
   * Attempt to match nameAnnot against all previous annotations of the same type, which are passed down
   * in listOfThisType.  Matches are tested in order from most recent to oldest.
   * @param nameAnnot    Annotation we are trying to match
   * @param annotString  Normalized string representation of annotation
   * @param listOfThisType  ArrayList of Annotations of the same type as nameAnnot
   * @param startIndex   Index in listOfThisType that we will start from in matching the current annotation
   */
  protected void matchWithPrevious(Annotation nameAnnot, String annotString,
          ArrayList<Annotation> listOfThisType,
          int startIndex) {
    boolean matchedUnknown = false;
    // Out.prln("matchWithPrevious now processing: " + annotString);

    for (int curIndex = startIndex - 1;curIndex >= 0;curIndex--) {
      Integer prevId = listOfThisType.get(curIndex).getId();
      Annotation prevAnnot = nameAllAnnots.get(prevId);  // Note that this line probably isn't necessary anymore

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

      //now changed to a rule, here we just match by gender
      if (prevAnnot.getType().equals(personType)) {
        String prevGender =
          (String) prevAnnot.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
        String nameGender =
          (String) nameAnnot.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
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
      //
      // A. Borthwick, Spock:  If the earlier annotation is shorter than the current annotation and it
      // has already been matched with a longer annotations, then don't match it with the current annotation.
      // Reasoning is that with the sequence David Jones . . . David  . . . David Smith, we don't want to match
      // David Smith with David.  However, with the sequence, David  . . . David Jones, it's okay to match the
      // shorter version with the longer, because it hasn't already been matched with a longer.
      if (matchAnnotations(nameAnnot, annotString,  prevAnnot)) {
//      Out.prln("Matched " + shortName + "and " + longName);
        updateMatches(nameAnnot, prevAnnot);
        boolean prevAnnotUsedToMatchWithLonger = prevAnnot.getFeatures().containsKey("matchedWithLonger");
        if (!prevAnnotUsedToMatchWithLonger && prevAnnot.getFeatures().containsKey("matchedWithLonger")) {
          propagatePropertyToExactMatchingMatches(prevAnnot,"matchedWithLonger",true);
        }
        //if unknown annotation, we need to change to the new type
        if (nameAnnot.getType().equals(unknownType)) {
          matchedUnknown = true;
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

  protected void propagatePropertyToExactMatchingMatches(Annotation updateAnnot,String featureName,Object value) {
    try {
      List<Integer> matchesList = (List<Integer>) updateAnnot.getFeatures().get(ANNOTATION_COREF_FEATURE_NAME);
      if ((matchesList == null) || matchesList.isEmpty()) {
        return;
      }
      else {
        String updateAnnotString = getStringForAnnotation(updateAnnot, document).toLowerCase();
        for (Integer nextId : matchesList) {
          Annotation a = nameAllAnnots.get(nextId);

          if (getStringForAnnotation(a, document).equalsIgnoreCase(updateAnnotString)) {
            a.getFeatures().put(featureName, value);
          }
        }
      }
    }
    catch (Exception e) {
      System.out.println("Very strange!!!!!  Error in propogatePropertyToExactMatchingMatches!!!!!!!!!");
    }
  }

  static private String getStringForAnnotation(Annotation a, gate.Document d) throws ExecutionException {
    String annotString = getStringForSpan(a.getStartNode().getOffset(),a.getEndNode().getOffset(), d);
    // now do the reg. exp. substitutions
    annotString = annotString.replaceAll("\\s+", " ");

    return annotString;
  }

  static private String getStringForSpan(Long start, Long end,gate.Document d) throws ExecutionException {
    try {
      return d.getContent().getContent(start, end).toString();
    }
    catch (InvalidOffsetException e) {
      System.out.println("Weird offset exception in getStringForSpan");
      e.printStackTrace();
      throw new ExecutionException(e);
    }
  }

  protected boolean matchAnnotations(Annotation newAnnot, String annotString,
          Annotation prevAnnot) {
    //do not match two annotations that overlap
    if (newAnnot.overlaps(prevAnnot))
      return false;

    // find which annotation string of the two is longer
    //  this is useful for some of the matching rules
    String prevAnnotString = (String) processedAnnots.get(prevAnnot.getId());
    try {
      // Out.prln("matchAnnotations processing " + annotString + " and " + prevAnnotString);
      if (prevAnnotString == null) {
        Out.prln("We discovered that the following string is null!:  " + prevAnnot.getId() +
                " For the previous annotation " + getStringForAnnotation(prevAnnot, document) +
                " which has annotation type " + prevAnnot.getType() +
                " with the annotation string " + annotString);
        return false;
      }
    }
    catch (ExecutionException e) {}

    String longName = prevAnnotString;
    String shortName = annotString;
    longAnnot = prevAnnot;
    shortAnnot = newAnnot;
    boolean longerPrevious = true;

    if (shortName.length()>longName.length()) {
      String temp = longName;
      longName = shortName;
      shortName = temp;
      Annotation tempAnn = longAnnot;
      longAnnot = shortAnnot;
      shortAnnot = tempAnn;
      longerPrevious = false;
    }//if

    tokensLongAnnot = (ArrayList) tokensMap.get(longAnnot.getId());
    tokensShortAnnot = (ArrayList) tokensMap.get(shortAnnot.getId());

    List matchesList = (List) prevAnnot.getFeatures().
    get(ANNOTATION_COREF_FEATURE_NAME);
    if (matchesList == null || matchesList.isEmpty())
      return apply_rules_namematch(prevAnnot.getType(), shortName,longName,
              prevAnnot,newAnnot,longerPrevious);

    //if these two match, then let's see if all the other matching one will too
    //that's needed, because sometimes names can share a token (e.g., first or
    //last but not be the same
    if (apply_rules_namematch(prevAnnot.getType(), shortName,longName,prevAnnot,newAnnot,
            longerPrevious)) {
      /**
       * Check whether we need to ensure that there is a match with the rest
       * of the matching annotations, because the rule requires that
       * transtivity is not assummed.
       */
      if (allMatchingNeeded) {
        allMatchingNeeded = false;

        List toMatchList = new ArrayList(matchesList);
        //      if (newAnnot.getType().equals(unknownType))
        //        Out.prln("Matching new " + annotString + " with annots " + toMatchList);
        toMatchList.remove(prevAnnot.getId());

        return matchOtherAnnots(toMatchList, newAnnot, annotString);
      } else
        return true;
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
        boolean longerPrevious = true;
        if (shortName.length()>=longName.length()) {
          String temp = longName;
          longName = shortName;
          shortName = temp;
          Annotation tempAnn = longAnnot;
          longAnnot = shortAnnot;
          shortAnnot = tempAnn;
          longerPrevious = false;
        }//if

        tokensLongAnnot = (ArrayList) tokensMap.get(longAnnot.getId());
        tokensShortAnnot = (ArrayList) tokensMap.get(shortAnnot.getId());

        matchedAll = apply_rules_namematch(prevAnnot.getType(), shortName,longName,prevAnnot,newAnnot,
                longerPrevious);
//      if (newAnnot.getType().equals(unknownType))
//      Out.prln("Loop: " + shortName + " and " + longName + ": result: " + matchedAll);

        i++;
    }//while
    return matchedAll;
  }


  protected boolean matchedAlready(Annotation annot1, Annotation annot2) {
    //the two annotations are already matched if the matches list of the first
    //contains the id of the second
    List matchesList = (List) annot1.getFeatures().
    get(ANNOTATION_COREF_FEATURE_NAME);
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
    // TODO: Andrew Borthwick 7/26/08:  The below is very inefficient.  We should be doing a lookup into a hash
    // which is indexed on string rather than testing every id. Need to have the index be String + Type
    // for safety
    Iterator iter = processedAnnots.keySet().iterator();
    // System.out.println("ID's examined: ");
    while (iter.hasNext()) {
      id = (Integer) iter.next();
      String oldString = (String) processedAnnots.get(id);
      // System.out.print(id + " ");
      if (annotString.equals(oldString)) {
        Annotation tempAnnot = nameAllAnnots.get(id);
        if (tempAnnot == null) {
          System.out.println("Oh no!!!  tempAnnot is null when looking at " + annotString
                  + " | " + oldString + " | old id: " + id);
          return null;
        }
        // Below is a new Spock addition to prevent unpredictable behavior when
        // the same string is given more than one type.  We want to return null
        // if there is no match on name + type (other than Unknown)
        if (newAnnot.getType().equals(unknownType) ||
                tempAnnot.getType().equals(newAnnot.getType())) {
          matchedAnnot = tempAnnot;
          break;
        }
      }
    }//while
    // System.out.println();

    if (matchedAnnot == null) return null;

    List matchesList = (List) matchedAnnot.getFeatures().
    get(ANNOTATION_COREF_FEATURE_NAME);
    if ((matchesList == null) || matchesList.isEmpty()) {
      //no previous matches, so need to add
      if (matchesList == null) {
        matchesList = new ArrayList();
        matchedAnnot.getFeatures().put(ANNOTATION_COREF_FEATURE_NAME,
                matchesList);
        matchesDocFeature.add(matchesList);
      }//if
      matchesList.add(matchedAnnot.getId());
      matchesList.add(newAnnot.getId());
    } else {
      //just add the new annotation
      matchesList.add(newAnnot.getId());
    }//if
    //add the matches list to the new annotation
    newAnnot.getFeatures().put(ANNOTATION_COREF_FEATURE_NAME, matchesList);
    return matchedAnnot;
  }

  protected void updateMatches(Annotation newAnnot, Annotation prevAnnot) {

    List matchesList = (List) prevAnnot.getFeatures().
    get(ANNOTATION_COREF_FEATURE_NAME);
    if ((matchesList == null) || matchesList.isEmpty()) {
      //no previous matches, so need to add
      if (matchesList == null) {
        matchesList = new ArrayList();
        prevAnnot.getFeatures().put(ANNOTATION_COREF_FEATURE_NAME, matchesList);
        matchesDocFeature.add(matchesList);
      }//if
      matchesList.add(prevAnnot.getId());
      matchesList.add(newAnnot.getId());
    } else {
      //just add the new annotation
      matchesList.add(newAnnot.getId());
    }//if
    //add the matches list to the new annotation
    newAnnot.getFeatures().put(ANNOTATION_COREF_FEATURE_NAME, matchesList);
    //propagate the gender if two persons are matched
    if (prevAnnot.getType().equals(personType)) {
      String prevGender =
        (String) prevAnnot.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
      String newGender =
        (String) newAnnot.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
      boolean unknownPrevGender = isUnknownGender(prevGender);
      boolean unknownNewGender = isUnknownGender(newGender);
      if (unknownPrevGender && !unknownNewGender)
        prevAnnot.getFeatures().put(PERSON_GENDER_FEATURE_NAME, newGender);
      else if (unknownNewGender && !unknownPrevGender)
        newAnnot.getFeatures().put(PERSON_GENDER_FEATURE_NAME, prevGender);
    }//if
  }


  protected void docCleanup() {
    Object matchesValue = document.getFeatures().get(DOCUMENT_COREF_FEATURE_NAME);
    if (matchesValue != null && (matchesValue instanceof Map))
      ((Map)matchesValue).remove(nameAllAnnots.getName());
    else if (matchesValue != null) {
      document.getFeatures().put(DOCUMENT_COREF_FEATURE_NAME, new HashMap());
    }

    //get all annotations that have a matches feature
    HashSet fNames = new HashSet();
    fNames.add(ANNOTATION_COREF_FEATURE_NAME);
    AnnotationSet annots =
      nameAllAnnots.get(null, fNames);

//  Out.prln("Annots to cleanup" + annots);

    if (annots == null || annots.isEmpty())
      return;

    Iterator<Annotation> iter = annots.iterator();
    while (iter.hasNext()) {
      while (iter.hasNext())
        iter.next().getFeatures().remove(ANNOTATION_COREF_FEATURE_NAME);
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
    AnnotationSet as1 = nameAllAnnots.getContained(startAnnot,endAnnot);
    if (as1 == null || as1.isEmpty())
      return annotString;
    AnnotationSet as =
      as1.get("Lookup", queryFM);
    if (as !=null && ! as.isEmpty()) {
      List<Annotation> titles = new ArrayList<Annotation>(as);
      Collections.sort(titles, new gate.util.OffsetComparator());

      Iterator<Annotation> iter = titles.iterator();
      while (iter.hasNext()) {
        Annotation titleAnn = iter.next();

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
//          Out.prln("Removing title from: " + annot + " with string " + annotString);
//          Out.prln("Tokens are" + tokensMap.get(annot.getId()));
//          Out.prln("Title is" + annotTitle);
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

    ArrayList<Annotation> tokens = (ArrayList) tokensMap.get(annot.getId());
    // ArrayList tokens = (ArrayList) tokensMap.get(annot.getId());

    //strip starting The first
    if ( ((String) ((Annotation) tokens.get(0)
    ).getFeatures().get(TOKEN_STRING_FEATURE_NAME))
    .equalsIgnoreCase(THE_VALUE))
      tokens.remove(0);


    // New code by A. Borthwick of Spock Networks
    // June 13, 2008
    // Strip everything on the cdg list, which now encompasses not just cdg's, but also other stopwords
    // Start from the right side so we don't mess up the arraylist
    for (int i = tokens.size() - 1; i >= 0; i--) {
      String tokenString = ((String) tokens.get(i).getFeatures().get(TOKEN_STRING_FEATURE_NAME));
      String kind = (String) tokens.get(i).getFeatures().get(TOKEN_KIND_FEATURE_NAME);
      String category = (String) tokens.get(i).getFeatures().get(TOKEN_CATEGORY_FEATURE_NAME);
      if (!caseSensitive)  {
        tokenString = tokenString.toLowerCase();
      }
      // Out.prln("tokenString: " + tokenString + " kind: " + kind + " category: " + category);
      if (kind.equals(PUNCTUATION_VALUE) || category.equals("DT") || category.equals("IN")
              || cdg.contains(tokenString)) {
        // Out.prln("Now tagging it!");
        tokens.get(i).getFeatures().put("ortho_stop", true);
      }
    }

    // AB, Spock:  Need to check for CDG even for 1 token so we don't automatically match
    // a one-token annotation called "Company", for instance
    String compareString = (String) tokens.get(tokens.size()-1).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
    if (!caseSensitive) {
      compareString = compareString.toLowerCase();
    }
    if (cdg.contains(compareString)) {
      tokens.remove(tokens.size()-1);
    }

    StringBuffer newString = new StringBuffer(50);
    for (int i = 0; i < tokens.size(); i++){
      newString.append((String) ((Annotation) tokens.get(i)
      ).getFeatures().get(TOKEN_STRING_FEATURE_NAME) );
      if (i != tokens.size()-1)
        newString.append(" ");
    }
    // Out.prln("Strip CDG returned: " + newString + "for string " + annotString);

    if (caseSensitive)
      return newString.toString();

    return newString.toString().toLowerCase();
  }

  /** creates the lookup tables */
  protected void createAnnotList(String nameFile,String nameList)
  throws IOException{
    //create the relative URL
    URL fileURL = new URL(definitionFileURL, nameFile);
    BufferedReader bufferedReader =
      new BufferedReader(new InputStreamReader(fileURL.openStream(),
              encoding));

    String lineRead = null;
    while ((lineRead = bufferedReader.readLine()) != null){
      if (nameList.compareTo(CDGLISTNAME)==0){
        Matcher matcher = punctPat.matcher(lineRead.toLowerCase().trim());
        lineRead = matcher.replaceAll(" ").trim();
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


  /**
   * This is the skeleton of a function which should be available in OrthoMatcher to allow a pairwise comparison of two name strings
   * It should eventually be made public.  It is private here (and thus non-functional) because OrthoMatcher is currently reliant
   * on the tokenization of the names, which are held in the global variables tokensShortAnnot and tokensLongAnnot
   *
   * @param name1
   * @param name2
   * @return  true if the two names indicate the same person
   */
  private boolean pairwise_person_name_match(String name1, String name2) {
    String shortName,longName;
    if (name1.length() > name2.length()) {
      longName = name1;
      shortName = name2;
    }
    else {
      longName = name2;
      shortName = name1;
    }
    if (matchRule0(longName,shortName)) {
      return false;
    }
    else {
      if (longName.equals(shortName) || matchRule2(longName, shortName) ||
              matchRule3(longName, shortName)) {
        return true;
      }
      else {
        return (matchRule1Name(longName, shortName,true));
        // boolean throwAway[] = new boolean[17];
        // return basic_person_match_criteria(shortName,longName,throwAway);
        // The above doesn't work because basic_person_match_criteria is reliant on the global
        // variables tokensShortAnnot and tokensLongAnnot so I just call what I can directly
      }
    }
  }

  /**
   * basic_person_match_criteria
   * Note that this function relies on various global variables in matchRule4, matchRule5, matchRule14,
   * and matchRule15
   * @param shortName
   * @param longName
   * @param mr
   * @return
   */
  private boolean basic_person_match_criteria(String shortName,
          String longName, boolean mr[]) {
    if (  (mr[1] = matchRule1Name(longName, shortName,true))
            ||   // For 4, 5, 14, and 15, need to mark shorter annot
            (mr[4] = matchRule4Name(longName, shortName))
            ||
            (mr[5] = matchRule5(longName, shortName))
            ||
            (mr[12] = matchRule12Name(longName, shortName))
            ||
            (mr[14] = matchRule14(longName, shortName))
            || //kalina: added this, so it matches names when contain more
            //than one first and one last name
            (mr[15] = matchRule15(longName, shortName))) {
      return true;
    }
    return false;
  }


  /** apply_rules_namematch: apply rules similarly to lasie1.5's namematch */
  private boolean apply_rules_namematch(String annotationType, String shortName,
          String longName,Annotation prevAnnot,
          Annotation followAnnot,
          boolean longerPrevious) {
    boolean mr[] = new boolean[17];
    // first apply rule for spurious matches i.e. rule0
    if (DEBUG) {
      System.out.println("Now matching |" + shortName + "|" + longName + "|");
    }
    if (matchRule0(longName, shortName))
      return false;
    if (
            (// rules for all annotations
                    //no longer use rule1, coz I do the check for same string via the
                    //hash table
                    (mr[2] = matchRule2(longName, shortName))
                    ||
                    (mr[3] = matchRule3(longName, shortName))
            ) // rules for all annotations
            ||
            (// rules for organisation annotations
                    ( annotationType.equals(organizationType)
                            //ACE addition
                            || annotationType.equals("Facility"))
                            &&
                            // Should basically only match when you have a match of all tokens other than
                            // CDG's and function words
                            ((  !highPrecisionOrgs &&  (
                                    (mr[4] = matchRule4(longName, shortName))
                                    ||
                                    (mr[5]=   matchRule5(longName, shortName))
                                    ||
                                    (mr[6] =matchRule6(longName, shortName))
                                    ||
                                    (mr[7] = matchRule7(longName, shortName))
                                    ||
//                                  matchRule8(longName, shortName)
//                                  ||
                                    (mr[9] = matchRule9(longName, shortName))
                                    ||
                                    (mr[10] = matchRule10(longName, shortName))
                                    ||
                                    (mr[11]= matchRule11(longName, shortName))
                                    ||
                                    (mr[12]= matchRule12(longName, shortName))
                                    ||
                                    (mr[13] = matchRule13(shortName, longName)))
                            ) ||
                            (highPrecisionOrgs &&
                                    (mr[6] =matchRule6(longName, shortName))
                                    ||
                                    (mr[7] = matchRule7(longName, shortName))
                                    ||
                                    (mr[10] = matchRule10(longName, shortName))
                                    ||
                                    (mr[11]= matchRule11(longName, shortName))
                                    ||
                                    (mr[16]= matchRule16(longName, shortName))
                            )
                            ))) {// rules for organisation annotations
      if (DEBUG) {
        printMatchRules(shortName, longName, mr);
      }
      return true;
    }

    if  (// rules for person annotations
            (    annotationType.equals(personType))) {
      if (noMatchRule1(longName, shortName,prevAnnot, longerPrevious)) {
        // Out.prln("noMatchRule1 rejected match between " + longName + " and " + shortName);
        return false;
      }
      else {
        if (  basic_person_match_criteria(shortName,longName,mr))
        {
          if ((longName.length() != shortName.length()) && (mr[4] || mr[5] || mr[14])) {
            if (longerPrevious) {
              followAnnot.getFeatures().put("matchedWithLonger", true);
            }
            else {
              prevAnnot.getFeatures().put("matchedWithLonger", true);
            }
          }
          else if ((longName.length() == shortName.length()) && (mr[1])) {
            if (prevAnnot.getFeatures().containsKey("matchedWithLonger")) {
              followAnnot.getFeatures().put("matchedWithLonger", true);
            }
          }
          if (DEBUG) {
            printMatchRules(shortName, longName, mr);
          }
          return true;
        }
        return false;
      }
    }
    return false;
  }//apply_rules

  /**
   * Print out index numbers of all rules which fired on a pair of names.
   *
   * @param shortName
   * @param longName
   * @param mr  Holds the match results.
   */
  private void printMatchRules(String shortName, String longName, boolean[] mr) {
    boolean foundOne = false;
    for (int i = 0;i < mr.length;i++) {
      if (mr[i]) {
        if (!foundOne) {
          Out.prln("OrthoMatcher:  The following rules fired for: " + longName + " | "
                  + shortName);
          foundOne = true;
        }
        Out.pr(i + " ");
      }
    }
    if (foundOne) {
      Out.prln("");
    }
  }


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

  /** set whether to process the Unknown annotations*/
  public void setProcessUnknown(Boolean processOrNot) {
    this.matchingUnknowns = processOrNot.booleanValue();
  }//setAnnotationTypes

  public void setOrganizationType(String newOrganizationType) {
    organizationType = newOrganizationType;
  }//setOrganizationType

  public void setPersonType(String newPersonType) {
    personType = newPersonType;
  }//setPersonType

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

  public Boolean getExtLists() {
    return new Boolean(extLists);
  }

  /** Are we running in a case-sensitive mode?*/
  public Boolean getCaseSensitive() {
    return new Boolean(caseSensitive);
  }

  /** Return whether or not we're processing the Unknown annots*/
  public Boolean getProcessUnknown() {
    return new Boolean(matchingUnknowns);
  }

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
   * Applied to: annotations other than names
   */
  public boolean matchRule1(String s1,
          String s2,
          boolean matchCase) {
//  Out.prln("Rule1: Matching " + s1 + "and " + s2);

    boolean matched = false;
    if (!matchCase)
      matched = s1.equalsIgnoreCase(s2);
    else matched =  s1.equals(s2) ;
//  kalina: do not remove, nice for debug
//  if (matched && (s2.startsWith("Kenneth") || s1.startsWith("Kenneth")))
//  Out.prln("Rule1: Matched " + s1 + "and " + s2);
    return matched;
  }//matchRule1

  public boolean matchRule1Name(String s1,
          String s2,
          boolean matchCase) {
    boolean retVal = matchRule1(s1, s2, matchCase);
    //if straight compare didn't work, try a little extra logic
    if (!retVal)
      retVal = fuzzyMatch(s1, s2);

    if (retVal) {
      log.debug("rule1Name matched " + s1 + "(id: " + longAnnot.getId() + ") to " + s2+ "(id: " + shortAnnot.getId() + ")");
    }
    return retVal;
  }//matchRule1Name


  /**
   * RULE #2: if the two names are listed as equivalent in the
   * lookup table (alias) then they match
   * Condition(s): -
   * Applied to: all name annotations
   */
  public boolean matchRule2(String s1,
          String s2) {

    if (alias.containsKey(s1) && alias.containsKey(s2)) {
      if (alias.get(s1).toString().equals(alias.get(s2).toString())) {
        log.debug("rule2 matched " + s1 + " to " + s2);
        return true;
      }
    }

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

      if (s2_poss != null && matchRule1(s1, s2_poss,caseSensitive)) {
        log.debug("rule3 matched " + s1 + " to " + s2);
        return true;
      }

      // now check the second case i.e. "Standard and Poor" == "Standard's"
      String token = (String)
      ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

      if (!token.endsWith("'s")) s2_poss = token.concat("'s");
      else s2_poss = token.concat("'");

      if (s2_poss != null && matchRule1(s2_poss,s2,caseSensitive)) {
        log.debug("rule3 matched " + s1 + " to " + s2);
        return true;
      }

    } // if (s2.endsWith("'s")
    return false;
  }//matchRule3

  /**
   * RULE #4: Does the first non-punctuation token from the long string match
   * the first token from the short string?
   * e.g. "fred jones" == "fred"
   * Condition(s): case-insensitive match
   * Applied to: person annotations
   *
   * Modified by Andrew Borthwick, Spock Networks:  Disallow stop words
   */
  public boolean matchRule4(String s1,
          String s2) {

    boolean allTokensMatch = true;
    // Out.prln("MR4:  Matching" + s1 + " with " + s2);

    Iterator tokensLongAnnotIter = tokensLongAnnot.iterator();
    Iterator tokensShortAnnotIter = tokensShortAnnot.iterator();
    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
      Annotation token = (Annotation) tokensLongAnnotIter.next();
      if (((String)token.getFeatures().get(TOKEN_KIND_FEATURE_NAME)).equals(PUNCTUATION_VALUE) ||
              token.getFeatures().containsKey("ortho_stop"))
        continue;
      if (! ((String)(((Annotation) tokensShortAnnotIter.next()).
              getFeatures().get(TOKEN_STRING_FEATURE_NAME))).equals(
                      (String) token.getFeatures().get(TOKEN_STRING_FEATURE_NAME))) {
        allTokensMatch = false;
        break;
      } // if (!tokensLongAnnot.nextToken()
    } // while
//  if (allTokensMatch)
//  Out.prln("rule4 fired. result is: " + allTokensMatch);
    if (allTokensMatch)
      log.debug("rule4 matched " + s1 + "(id: " + longAnnot.getId() + ") to " + s2+ "(id: " + shortAnnot.getId() + ")");
    return allTokensMatch;
  }//matchRule4

  /**
   * RULE #4: Does the first non-punctuation token from the long string match
   * the first token from the short string?
   * e.g. "fred jones" == "fred"
   * Condition(s): case-insensitive match
   * Applied to: person annotations
   *
   * Modified by Andrew Borthwick, Spock Networks:  Allowed for nickname match
   */
  public boolean matchRule4Name(String s1,
          String s2) {

    boolean allTokensMatch = true;
    // Out.prln("MR4 Name: Matching" + tokensLongAnnot + " with " + tokensShortAnnot);
    // Out.prln("MR4 Name: Matching " + s1 + " with " + s2);

    Iterator tokensLongAnnotIter = tokensLongAnnot.iterator();
    Iterator tokensShortAnnotIter = tokensShortAnnot.iterator();
    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
      Annotation token = (Annotation) tokensLongAnnotIter.next();
      if (((String)token.getFeatures().get(TOKEN_KIND_FEATURE_NAME)).equals(PUNCTUATION_VALUE))
        continue;
      if (! fuzzyMatch((String)(((Annotation) tokensShortAnnotIter.next()).
              getFeatures().get(TOKEN_STRING_FEATURE_NAME)),
              (String) token.getFeatures().get(TOKEN_STRING_FEATURE_NAME))) {
        allTokensMatch = false;
        break;
      }
    }
    if (allTokensMatch)
      log.debug("rule4n matched " + s1 + "(id: " + longAnnot.getId() + ") to " + s2+ "(id: " + shortAnnot.getId() + ")");
    return allTokensMatch;
  }//matchRule4

  /**
   * RULE #5: if the 1st token of one name
   * matches the second name
   * e.g. "Pepsi Cola" == "Pepsi"
   * Condition(s): case-insensitive match
   * Applied to: all name annotations
   *
   * Note that we don't want to use nicknames here because you don't use nicknames for last names
   */
  public boolean matchRule5(String s1,
          String s2) {

    //do not match numbers by this rule
    if (tokensLongAnnot.size()> 1 &&
            ((Annotation) tokensLongAnnot.get(0)).getFeatures().get("kind").equals("number"))
      return false;

//  if (s1.startsWith("Patrick") || s2.startsWith("Patrick")) {
//  Out.prln("Rule 5: " + s1 + "and " + s2);
//  }

    //require that when matching person names, the shorter one to be of length 1
    //for the rule to apply. In other words, avoid matching Peter Smith and
    //Peter Kline, because they share a Peter token.
    if ( (shortAnnot.getType().equals(personType)
            || longAnnot.getType().equals(personType)
    )
    &&
    tokensShortAnnot.size()>1
    )
      return false;

    if (tokensLongAnnot.size()<=1)
      return false;
    if (((Annotation) tokensShortAnnot.get(0)).getFeatures().containsKey("ortho_stop")) {
      return false;
    }
    boolean result = matchRule1((String)
            ((Annotation) tokensLongAnnot.get(0)
            ).getFeatures().get(TOKEN_STRING_FEATURE_NAME),
            s2,
            caseSensitive);
    if (result)
      log.debug("rule5 matched " + s1 + " to " + s2);
    return result;

  }//matchRule5

  /**
   * RULE #6: if one name is the acronym of the other
   * e.g. "Imperial Chemical Industries" == "ICI"
   * Applied to: organisation annotations only
   */
  public boolean matchRule6(String s1,
          String s2) {

    int i = 0;

    //check and if the shorted string has a space in it, then it's not
    //an acronym
    if (s2.indexOf(" ") > 0)
      return false;

    // Abbreviations of one-word names are very rare and can lead to weird errors
    if (tokensLongAnnot.size() <= 1) {
      return false;
    }

    //Out.prln("Acronym: Matching " + s1 + "and " + s2);
    StringBuffer acronym_s1 = new StringBuffer("");
    StringBuffer acronymDot_s1 = new StringBuffer("");

    for ( ;i < tokensLongAnnot.size(); i++ ) {
      String toAppend = ( (String) ((Annotation) tokensLongAnnot.get(i)
      ).getFeatures().get(TOKEN_STRING_FEATURE_NAME)).substring(0,1);
      acronym_s1.append(toAppend);
      acronymDot_s1.append(toAppend);
      acronymDot_s1.append(".");
    }

    //Out.prln("Acronym dot: To Match " + acronymDot_s1 + "and " + s2);
    //Out.prln("Result: " + matchRule1(acronymDot_s1.toString(),s2,caseSensitive));

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
      ).getFeatures().get(TOKEN_STRING_FEATURE_NAME) )) {
        previous_token = (String) ((Annotation) tokensLongAnnot.get(i-1)
        ).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

        break;
      }
    }

    //now match previous_token with other name
    if (previous_token != null) {
//    if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//    Out.prln("Rule7");
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

//  if (s1.equalsIgnoreCase("news") || s2.equalsIgnoreCase("news"))
//  Out.prln("Rule 9 " + s1 + " and " + s2);
    String s1_short = (String)
    ((Annotation) tokensLongAnnot.get(
            tokensLongAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
//  Out.prln("Converted to " + s1_short);
    if (tokensLongAnnot.size()>1) {
      boolean matched = matchRule1(s1_short, s2, caseSensitive);
      //we need to make sure all names match, instead of assuming transitivity,
      //to avoid matching BBC News with News then News with ITV News, which
      //by transitivity leads to BBC News matching ITV News which is not what
      //we want
      if (matched)
        allMatchingNeeded = true;
      return matched;
    } //if

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
        ((Annotation) tokensLongAnnot.get(i)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
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
        ((Annotation) tokensLongAnnot.get(i++)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      else return false;

      String s21 = (String)
      ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      String s22 = (String)
      ((Annotation) tokensShortAnnot.get(1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
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
    ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
    token12 = (String)
    ((Annotation) tokensLongAnnot.get(1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

    // now check for the first case i.e. "Pan American" == "Pan Am"
    if (tokensShortAnnot.size() == 2)  {

      token21 = (String)
      ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      token22 = (String)
      ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

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
//    Out.prln("Rule 12");

      // get first and last tokens of s1 & s2
      String s1_first = (String)
      ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      String s2_first = (String)
      ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

      if (!matchRule1(s1_first,s2_first,caseSensitive))
        return false;

      String s1_last = (String)
      ((Annotation) tokensLongAnnot.get(tokensLongAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      String s2_last = (String)
      ((Annotation) tokensShortAnnot.get(tokensShortAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

      return matchRule1(s1_last,s2_last,caseSensitive);
    } // if (tokensLongAnnot.countTokens()>1
    return false;
  }//matchRule12

  /**
   * RULE #12: do the first and last tokens of one name
   * match the first and last tokens of the other?
   * Condition(s): case-sensitive match
   * Applied to: person annotations only
   */
  public boolean matchRule12Name(String s1,
          String s2) {

    // first do the easy case e.g. "Pan American" == "Pan Am"

    if (tokensLongAnnot.size()>1 && tokensShortAnnot.size()>1) {
//    Out.prln("Rule 12");

      // get first and last tokens of s1 & s2
      String s1_first = (String)
      ((Annotation) tokensLongAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      String s2_first = (String)
      ((Annotation) tokensShortAnnot.get(0)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

      if (!matchRule1Name(s1_first,s2_first,caseSensitive))
        return false;

      String s1_last = (String)
      ((Annotation) tokensLongAnnot.get(tokensLongAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      String s2_last = (String)
      ((Annotation) tokensShortAnnot.get(tokensShortAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);

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

//  if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin")) {
//  Out.prln("Rule 13: Matching tokens" + tokensLongAnnot);
//  Out.prln("with tokens " + tokensShortAnnot);
//  }

    // now do the matching
    for (int i=0,j= 0; i < tokensShortAnnot.size() && mismatches < 2; i++) {

//    Out.prln("i = " + i);
//    Out.prln("j = " + j);
      if ( ((Annotation) tokensLongAnnot.get(j)).getFeatures().get(TOKEN_STRING_FEATURE_NAME).equals(
              ((Annotation) tokensShortAnnot.get(i)).getFeatures().get(TOKEN_STRING_FEATURE_NAME)) ) {
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
   *
   * Don't need to nicknames here
   */
  public boolean matchRule14(String s1,
          String s2) {

//  if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//  Out.prln("Rule 14 " + s1 + " and " + s2);
    String s1_short = (String)
    ((Annotation) tokensLongAnnot.get(
            tokensLongAnnot.size()-1)).getFeatures().get(TOKEN_STRING_FEATURE_NAME);
//  Out.prln("Converted to " + s1_short);
    if (tokensLongAnnot.size()>1 && matchRule1(s1_short, s2, caseSensitive)) {
      log.debug("rule14 matched " + s1 + " to " + s2);
      return true;
    }

    return false;

  }//matchRule14

  /**
   * RULE #15: Does every token in the shorter name appear in the longer name?
   */
  public boolean matchRule15(String s1,
          String s2) {

    //do a token-by-token test
    Annotation token1, token2;
    // catch (ExecutionException e) {}
    for (int i=0; i < tokensShortAnnot.size(); i++) {
      token1 = (Annotation) tokensShortAnnot.get(i);
      //first check if not punctuation, because we need to skip it
      if (token1.getFeatures().get(TOKEN_KIND_FEATURE_NAME).equals(PUNCTUATION_VALUE))
        continue;

      String ts1 = (String)token1.getFeatures().get(TOKEN_STRING_FEATURE_NAME);
      boolean foundMatch = false;
      for (int j=0; j<tokensLongAnnot.size(); j++) {
        // Out.prln("i = " + i);
        token2 = (Annotation) tokensLongAnnot.get(j);
        if (token2.getFeatures().get(TOKEN_KIND_FEATURE_NAME).equals(PUNCTUATION_VALUE))
          continue;

        String ts2 = (String)token2.getFeatures().get(TOKEN_STRING_FEATURE_NAME);

        if (i == 0 && j == 0) {
          foundMatch = fuzzyMatch(ts1, ts2);
        }
        else {
          if (caseSensitive) {
            if (ts2.equals(ts1)) {
              foundMatch = true;
              break;
            }
          }
          else {
            if (ts2.equalsIgnoreCase(ts1)) {
              foundMatch = true;
              break;
            }
          }
        }
      }//for
      //if no match for the current tokenShortAnnot, then it is not a coref of the
      //longer annot
      if (!foundMatch)
        return false;
    } // for

    //only get to here if all word tokens in the short annot were found in
    //the long annot, so there is a coref relation
    log.debug("rule15 matched " + s1 + " to " + s2);
    return true;
  }//matchRule15

  /**
   * RULE #16: Conservative match rule
   * Require every token in one name to match the other except for tokens that are on a stop word list
   */
  public boolean matchRule16(String s1,
          String s2) {
    if (allNonStopTokensInOtherAnnot(tokensLongAnnot,tokensShortAnnot)) {
      return allNonStopTokensInOtherAnnot(tokensShortAnnot, tokensLongAnnot);
    }
    else {
      return false;
    }
  }

  /**
   * @return true if all of the tokens in firstName are either found in second name or are stop words
   */
  private boolean allNonStopTokensInOtherAnnot(ArrayList<Annotation> firstName,ArrayList<Annotation> secondName) {
    for (Annotation a : firstName) {
      if (!a.getFeatures().containsKey("ortho_stop")) {
        String aString = (String) a.getFeatures().get(TOKEN_STRING_FEATURE_NAME);
        boolean foundAMatchInSecond = false;
        for (Annotation b: secondName) {
          if (matchRule1(aString,(String) b.getFeatures().get(TOKEN_STRING_FEATURE_NAME),caseSensitive)) {
            foundAMatchInSecond = true;
            break;
          }
        }
        if (!foundAMatchInSecond) {
          return false;
        }
      }
    }
    return true;
  }

  /**
  No Match Rule 1:
  Avoids the problem of matching
  David Jones ...
  David ...
  David Smith
  Since "David" was matched with David Jones, we don't match David with David Smith.
   */
  public boolean noMatchRule1(String s1,
          String s2,Annotation previousAnnot, boolean longerPrevious) {
//  try {
//  String annotString = getStringForAnnotation(previousAnnot, document );

//  Out.prln("Previous annotation was " + annotString + " features are " + previousAnnot.getFeatures());
//  }
//  catch (ExecutionException e) {}

    if (longerPrevious || !previousAnnot.getFeatures().containsKey("matchedWithLonger")) {
      return false;
    }
    else {
      return true;
    }
  }//noMatchRule1


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
      tempMap.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME, "cdg");
      //now get all lookup annotations which are cdg
      AnnotationSet nameAnnots =
        nameAllAnnots.get(LOOKUP_ANNOTATION_TYPE, tempMap);

      if ((nameAnnots ==null) || nameAnnots.isEmpty())
        return;

      Iterator<Annotation> iter = nameAnnots.iterator();
      while (iter.hasNext()) {
        Annotation annot = iter.next();
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

  public boolean fuzzyMatch (String s1, String s2) {
    String s1Lower = s1.toLowerCase();
    String s2Lower = s2.toLowerCase();
    if (s1Lower.equals(s2Lower)) {
      return true;
    }
    // System.out.println("Now comparing " + s1 + " | " + s2) ;
    Set<String> formalNameSet = nicknameMap.get(s1Lower);
    if (formalNameSet != null) {
      if (formalNameSet.contains(s2Lower)) {
        return true;
      }
    }
    formalNameSet = nicknameMap.get(s2Lower);
    if (formalNameSet != null) {
      if (formalNameSet.contains(s1Lower)) {
        return true;
      }
    }
    return false;
  }


  public void setDefinitionFileURL(java.net.URL definitionFileURL) {
    this.definitionFileURL = definitionFileURL;
  }

  public java.net.URL getDefinitionFileURL() {
    return definitionFileURL;
  }
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  public String getEncoding() {
    return encoding;
  }


  public Double getMinimumNicknameLikelihood() {
    return minimumNicknameLikelihood;
  }

  public void setMinimumNicknameLikelihood(Double minimumNicknameLikelihood) {
    this.minimumNicknameLikelihood = minimumNicknameLikelihood;
  }

  /**
   * @return the highPrecisionOrgs
   */
  public Boolean getHighPrecisionOrgs() {
    return highPrecisionOrgs;
  }

  /**
   * @param highPrecisionOrgs the highPrecisionOrgs to set
   */
  public void setHighPrecisionOrgs(Boolean highPrecisionOrgs) {
    this.highPrecisionOrgs = highPrecisionOrgs;
  }

  static Pattern punctPat = Pattern.compile("[\\p{Punct}]+");



}

