/*
 *  Namematch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Oana Hamza, 25/January/01
 *
 *  $Id$
 */


package gate.creole.namematch;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.corpora.*;
import gate.annotation.*;
import java.util.*;
import java.io.*;
import java.net.*;
import gnu.regexp.*;

public class Namematch extends AbstractProcessingResource
                       implements ProcessingResource{

  /** the document for namematch */
  protected gate.Document document;

  /**the name of the annotation set*/
  protected String annotationSetName;

  /** the types of the annotation */
  protected Set annotationTypes = new HashSet();

  /** the organization type*/
  protected String organizationType = "Organization";

  /** the person type*/
  protected String personType = "Person";

  /** the type of annotation*/
  protected String annotationType;

  /** internal or external list */
  protected Boolean extLists;

  /** the annotation set for the document */
  protected AnnotationSet nameAnnots ;

  /** the set with all the matches from document*/
  protected List matchesDocument = null;

  protected ExecutionException executionException;

  // name lookup tables (used for namematch)
  protected HashMap alias = new HashMap();
  protected HashMap cdg = new HashMap();
  protected HashMap spur_match = new HashMap();
  protected HashMap def_art = new HashMap();
  protected HashMap connector = new HashMap();
  protected HashMap prepos = new HashMap();

  /** a buffer in order to read an array of char */
  private char cbuffer[] = null;

  /** the size of the buffer */
  private final static int BUFF_SIZE = 65000;

  public Namematch () {}

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    cbuffer = new char[BUFF_SIZE];
    //kalina
    //commented those, because if parameters are set to
    //other types, they should take precedence
    //also be used below in the annotationTypes
    //put this initialization by default, where the variables are declared

    //organizationType = "Organization";
    //personType = "Person";
    extLists = new Boolean(false);
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

    // creates the lists from external files and the cdg list is created
    // both the external list and lookup
    if (!extLists.booleanValue()){
      buildTables(document);
    } else {
      try {
        createAnnotList("cdg.lst","cdg");
      } catch (IOException ioe){ioe.printStackTrace();}
    }

    // get the annotations from document
    AnnotationSet nameAllAnnots;
    if ((annotationSetName == null)|| (annotationSetName == "")){
      nameAllAnnots = document.getAnnotations();
    } else {
      nameAllAnnots = document.getAnnotations(annotationSetName);
    }

    if (nameAllAnnots != null) {
      if (nameAllAnnots.isEmpty()) {
        executionException = new ExecutionException(
          "No annotations to process!"
        );
       return;
      }
    } else {
      executionException = new ExecutionException(
        "No annotations to process!"
      );
      return;
    }// End if

    // the "unknown" annotations
    AnnotationSet nameAnnotsUnknown;
    nameAnnotsUnknown = nameAllAnnots.get("Unknown", Factory.newFeatureMap());
    // go through all the annotation types
    Iterator iterAnnotationTypes = annotationTypes.iterator();
    while (iterAnnotationTypes.hasNext()) {
      annotationType = (String)iterAnnotationTypes.next();

      nameAnnots = nameAllAnnots.get(annotationType,
                                  Factory.newFeatureMap());

      // return if no such annotations exist
      if (nameAnnots != null) {
        if (!nameAnnots.isEmpty()){
          // the "unknown" annotations
          if (nameAnnotsUnknown!=null){
            nameAnnotsUnknown = nameAnnotsUnknown.get("Unknown",
                                  Factory.newFeatureMap());
            // add the "unknown" annotations to the current set of annotation
            nameAnnots.addAll(nameAnnotsUnknown);
          }// if

          // PROBLEM:
          // due to the fact that the strings to be compared
          // may contain tabs or newlines, those should also be matched
          // as "whitespace" e.g.
          // "New York" should match "New \nYork" or "New\tYork")
          //
          // SOLUTION: substitute all multiple spaces, tabes and newlines
          // with a single space and create new annotation strings
          // for comparison
          Map annotStringMap = new HashMap();
          Iterator iterAnnotType = nameAnnots.iterator();
          while (iterAnnotType.hasNext()) {
            Annotation annot = (Annotation)iterAnnotType.next();
            // get string and value
            Long offsetStartAnnot = annot.getStartNode().getOffset();
            Long offsetEndAnnot = annot.getEndNode().getOffset();
            try {
              String annotString =
                document.getContent().getContent(
                  offsetStartAnnot,offsetEndAnnot).toString();
              // now do the reg. exp. substitutions
              annotString = regularExpressions(annotString," ", "\\s+");

              // put string in the map
              annotStringMap.put(annot.getId(),annotString);

            } catch (InvalidOffsetException ioe) {
              executionException = new ExecutionException
                                     ("Invalid offset of the annotation");
            }

          } // for

          // now go through the annotations
          // table for matched annotations
          Hashtable matched_annots = new Hashtable();

          // the list with the previous annotations of the current annotation
          List previousAnnots = new ArrayList();

          Iterator iteratorNameAnnots = nameAnnots.iterator();
          while (iteratorNameAnnots.hasNext()) {
            Annotation annot1 = (Annotation)iteratorNameAnnots.next();

            // get the id annotattion
            Integer annot1_id = annot1.getId();

            AnnotationMatches matchedAnnot1 = new AnnotationMatches();

            // now compare the annotation with the previous ones
            Iterator i = previousAnnots.iterator();
            while (i.hasNext()) {
              Annotation annot2 = (Annotation)i.next();

              String annotString2 = (String) annotStringMap.get(annot2.getId());

              // get the id annotattion
              Integer annot2_id = annot2.getId();

              // get string from map and NOT from spans
              String annotString1 = (String) annotStringMap.get(annot1.getId());

              // first check that annot2 is NOT already in the list of
              // matched annotations for annot1 - do not proceed with
              // annot2 if so
              if (matched_annots.containsKey(annot1_id.toString())) {
                matchedAnnot1 =
                  (AnnotationMatches) matched_annots.get(annot1_id.toString());
              if (matchedAnnot1.containsMatched(annot2_id)) continue;
              }

              // find which annotation string of the two is longer
              //  this is useful for some of the matching rules
              String longName = null;
              String shortName = null;

              // determine the title from annotation string
              if (annotationType.equals(personType)) {
                annotString1 = containTitle(nameAllAnnots, annotString1,annot1);
                annotString2 = containTitle(nameAllAnnots, annotString2,annot2);
//                Out.prln("Annot1 without title" + annotString1);
//                Out.prln("Annot2 without title" + annotString2);
              }

              if (annotString1.length()>=annotString2.length()) {
                longName = annotString1;
                shortName = annotString2;
              } else {
                longName = annotString2;
                shortName = annotString1;
              }

              // apply name matching rules
              if (apply_rules_namematch(shortName,longName)) {

                AnnotationMatches matchedAnnot2 = new AnnotationMatches();
                AnnotationMatches matchedByAnnot2 = new AnnotationMatches();
                AnnotationMatches matchedByAnnot1 = new AnnotationMatches();

                if (matched_annots.containsKey(annot2_id.toString())){
                  matchedAnnot2 =
                   (AnnotationMatches) matched_annots.get(annot2_id.toString());
                }
                // first add the annotation ids matched by annot2 to those
                // matched by annot1
                if (matchedAnnot2 !=null) {
                  for (int k=0;k<matchedAnnot2.howMany();k++) {
                    Integer matchedByAnnot2Id = matchedAnnot2.matchedAnnotAt(k);
                    if (!matchedAnnot1.containsMatched(matchedByAnnot2Id)) {
                      matchedAnnot1.addMatchedAnnotId(matchedByAnnot2Id);
                    }
                      // then add annot1 to all those annotations
                      // that have been matched with annot2 so far
                      matchedByAnnot2 = (AnnotationMatches)
                                         matched_annots.get(matchedByAnnot2Id);

                      if ((matchedByAnnot2 !=null)&&
                        (!matchedByAnnot2.containsMatched
                          (annot1_id))) {
                        matchedByAnnot2.addMatchedAnnotId(annot1_id);
                      }
                    } // for
                  }
                  // add annotation2 to the ids of annotation1
                  for (int l=0;l<matchedAnnot1.howMany();l++) {
                    Integer matchedByAnnot1Id = matchedAnnot1.matchedAnnotAt(l);

                    if (!matchedAnnot2.containsMatched(
                                            matchedAnnot1.matchedAnnotAt(l))) {
                      matchedAnnot2.addMatchedAnnotId(
                                      matchedAnnot1.matchedAnnotAt(l));

                    }
                    matchedByAnnot1 = (AnnotationMatches)
                                       matched_annots.get(matchedByAnnot1Id);

                    if ((matchedByAnnot1 !=null)&&
                      (!matchedByAnnot1.containsMatched
                        (annot2_id))) {
                      matchedByAnnot1.addMatchedAnnotId(annot2_id);
                    }

                  }

                // added the id annotation to the list of matches
                if ((nameAnnotsUnknown == null)||
                   (!nameAnnotsUnknown.contains(annot1))||
                   (!nameAnnotsUnknown.contains(annot2))) {
                  if (!matchedAnnot1.containsMatched(annot1_id))
                    matchedAnnot1.addMatchedAnnotId(annot1_id);
                  if (!matchedAnnot1.containsMatched(annot2_id))
                    matchedAnnot1.addMatchedAnnotId(annot2_id);

                  if (!matchedAnnot2.containsMatched(annot1_id))
                    matchedAnnot2.addMatchedAnnotId(annot1_id);
                  if (!matchedAnnot2.containsMatched(annot2_id))
                    matchedAnnot2.addMatchedAnnotId(annot2_id);

                  matched_annots.put(annot1_id.toString(),matchedAnnot1);
                  matched_annots.put(annot2_id.toString(),matchedAnnot2);
                }

                // classify the "unknown" annotation if such exists
                if (nameAnnotsUnknown!=null) {
                  if ((nameAnnotsUnknown.contains(annot1))
                      && (!nameAnnotsUnknown.contains(annot2))){
                    Integer id = annot1.getId();
                    Long start = annot1.getStartNode().getOffset();
                    Long end = annot1.getEndNode().getOffset();
                    // remove the "unknown" annotation
                    nameAnnotsUnknown.remove(annot1);
                    FeatureMap fm2 = annot2.getFeatures();
                    FeatureMap fm1 = Factory.newFeatureMap();
                    fm1.putAll(fm2);
                    fm1.put("NMrule","Unknown");
                    try {
                      // add the annotation with the new type
                      nameAnnotsUnknown.add(
                        id,start,end,annotationType,fm1);
                    } catch (InvalidOffsetException ioe){ioe.printStackTrace();}
                  } else if ((nameAnnotsUnknown.contains(annot2))
                      && (!nameAnnotsUnknown.contains(annot1))){
                    Integer id = annot2.getId();
                    Long start = annot2.getStartNode().getOffset();
                    Long end = annot2.getEndNode().getOffset();
                    // remove the "unknown" annotation
                    nameAnnotsUnknown.remove(annot2);
                    FeatureMap fm1 = annot1.getFeatures();
                    FeatureMap fm2 = Factory.newFeatureMap();
                    fm2.putAll(fm1);
                    fm2.put("NMrule","Unknown");
                    try {
                      // add the annotation with the new type
                      nameAnnotsUnknown.add(
                        id,start,end,annotationType,fm2);
                    } catch (InvalidOffsetException ioe){ioe.printStackTrace();}
                  } // else if
                }//if


              } // if (apply_rules_namematch
            }//while

            previousAnnots.add(annot1);
          }// while

          // added the "unknown" annotation if it matches with an annotation
          // of which the type has the current type
          if (nameAnnotsUnknown!=null) {
            //remove the unknown annotation
            Iterator it = nameAnnotsUnknown.iterator();
            while (it.hasNext()) {
              Annotation ann = (Annotation)it.next();
              if (nameAnnots.contains(ann)) nameAnnots.remove(ann);
              if (nameAllAnnots.contains(ann)) nameAllAnnots.remove(ann);
            }

            // add the new annotation
            AnnotationSet as = nameAnnotsUnknown.get(annotationType);
            if ( as != null) {
              Iterator iter = as.iterator();
              while (iter.hasNext()) {
                Annotation annot = (Annotation)iter.next();
                nameAnnots.add(annot);
                nameAllAnnots.add(annot);
              };
              // delete them
            }// if
          }// if

          // append the "matches" attribute to existing annotations
          for (Enumeration enum =
                            matched_annots.keys(); enum.hasMoreElements() ;) {
            String annot_id = (String) enum.nextElement();
            AnnotationMatches matchedAnnot =
              (AnnotationMatches) matched_annots.get(annot_id);

            // the matches found for the current annotation
            List matchesList = matchedAnnot.getMatched();
            // remove attribute "matches" if such exists:
            // i.e has the namematcher run on the same doc b4?
            Annotation annot = nameAnnots.get(new Integer(annot_id));
            if (annot!=null) {
              FeatureMap attr = annot.getFeatures();
              attr.remove("matches");
              attr.put("matches", matchesList);
            }
          } // for Enumeration
        }// else
      }//if
    }//while

    // update the annotation set
    // add the "unknown" annotations that are not matching
    if (nameAnnotsUnknown!=null) {
      nameAnnotsUnknown = nameAnnotsUnknown.get("Unknown",
                      Factory.newFeatureMap());
      if (nameAnnotsUnknown!=null)
       nameAllAnnots.addAll(nameAnnotsUnknown);
    }

    // set the matches of the document
    determineMatchesDocument();

    return;

  } // run()

  /** return a person name without title */
  public String containTitle (AnnotationSet annotSet,String annotString,
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

  /** all the matches from the current document are placed in a list */
  public void determineMatchesDocument() {
    AnnotationSet namedAnnots;
    // get the annotation from the document
    if ((annotationSetName == null)||(annotationSetName == ""))
      namedAnnots = document.getAnnotations();
    else namedAnnots = document.getAnnotations(annotationSetName);

    FeatureMap fm1;
    Annotation annot;
    Iterator iterator;

    // a list with the matches from the current document
    matchesDocument = new ArrayList();
    Map booleanMatches = new HashMap();
    Iterator iteratorAnnotation = namedAnnots.iterator();
    while (iteratorAnnotation.hasNext()){
        booleanMatches.put(iteratorAnnotation.next(),"false");
      }
    // go through all the annotations and put all the matches in a list
    iteratorAnnotation = namedAnnots.iterator();
    while (iteratorAnnotation.hasNext()){
      //the list with the matches of the current annotation
      List matchesAnnotation = new ArrayList();

      annot = (Annotation)iteratorAnnotation.next();
      Integer idAnnot = annot.getId();

      fm1 = annot.getFeatures();
      iterator = fm1.keySet().iterator();
      while (iterator.hasNext()) {
        String type = (String) iterator.next();
        if (type == "matches") {
          // add the id of the annotation
          String valueId = (String)booleanMatches.get(annot);
          if (valueId.compareTo("false")==0) matchesAnnotation.add(idAnnot);
          // update the list (true)
          booleanMatches.remove(annot);
          booleanMatches.put(annot,new String ("true"));
          List list = (List)fm1.get(type);
          for (int j=0; j< list.size(); j++) {
            Integer value = (Integer)list.get(j);
            int valueInt = value.intValue();

            //verify whether it isn't already in matchesDocument
            Annotation newAnnot = namedAnnots.get(value);
            String id = (String)booleanMatches.get(newAnnot);
            if (id.equals("false")){
              matchesAnnotation.add(value);
              booleanMatches.remove(newAnnot);
              booleanMatches.put(newAnnot,new String ("true"));
            }
          } // for
        } // if
      } // while
      if (matchesAnnotation != null)
        if (matchesAnnotation.size()>0)
          matchesDocument.add(matchesAnnotation);
    } // for

    FeatureMap aFeatureMap = null;
    if (matchesDocument != null){
      // If the Gate Document doesn't have a feature map atached then
      // We will create and set one.
      if(document.getFeatures() == null){
            aFeatureMap = Factory.newFeatureMap();
            document.setFeatures(aFeatureMap);
      }// end if
      document.getFeatures().put("MatchesAnnots",matchesDocument);
    }
  }//   public void determineMatchesDocument()

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
  public void createLists() throws IOException {
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
  public void createAnnotList(String nameFile,String nameList)
                                                          throws IOException{
    InputStream inputStream = Files.getGateResourceAsStream(
                                              "creole/namematcher/"+nameFile);
    InputStreamReader inputStreamReader = new InputStreamReader (
                                                    inputStream);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

    String lineRead = null;
    while ((lineRead = bufferedReader.readLine()) != null){
      if (extLists.booleanValue()){
        if (nameList.compareTo("cdg")==0){
          cdg.put(lineRead,"cdg");
        }// if
      }// if
      else {
        int index = lineRead.indexOf("£");
        if (index != -1){
          String  expr = lineRead.substring(0,index);
          String code = lineRead.substring(index+1,lineRead.length());
          if (nameList.equals("alias"))
                            alias.put(expr, code);
          else
          if (nameList.equals("def_art"))
                            def_art.put(expr, code);
          else
          if (nameList.equals("prepos"))
                            prepos.put(expr, code);
          else
          if (nameList.equals("connector"))
                            connector.put(expr, code);
          else
          if (nameList.equals("spur_match"))
                            spur_match.put(expr, code);

      }// if

      }
    }//while
  }//createAnnotList


  /** apply_rules_namematch: apply rules similarly to lasie1.5's namematch */
  private boolean apply_rules_namematch(String shortName, String longName) {
    // first apply rule for spurius matches i.e. rule0
    if (!matchRule0(longName, shortName)) {
      if (
           (// rules for all annotations
              matchRule1(longName, shortName, false)
           ||
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
           ) // if
        return true;
      } // if (!matchRule0
    return false;
  }//apply_rules

  /** set the document */
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }//setDocument

  /** set the annotations */
  public void setExtLists(Boolean newExtLists) {
    extLists = newExtLists;
  }//setextLists

  /** set the annotation set name*/
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }//setAnnotationSetName

  /** set the types of the annotations*/
  public void setAnnotationTypes(Set newType) {
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
  public Set getAnnotationTypes() {
    return annotationTypes;
  }//getAnnotationTypes

  public String getOrganizationType() {
    return organizationType;
  }

  public String getPersonType() {
    return personType;
  }

  public Boolean getExtList() {
    return extLists;
  }

  public List getMatchesDocument() {
    return matchesDocument;
  }

  /** RULE #0: If the two names are listed in table of
    * spurius matches then they do NOT match
    * Condition(s): -
    * Applied to: all name annotations
    */
  public boolean matchRule0(String s1,
           String s2) {
    if (spur_match.containsKey(s1)
  && spur_match.containsKey(s2)&&(!s1.equals(s2))) {
      return
  spur_match.get(s1).toString().equals(spur_match.get(s2).toString());
      }
    return false;
  }//matchRule0

  /** RULE #1: If the two names are identical then they are the same
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
    * Applied to: organisation annotations only
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
      && cdg.containsKey(token)) cdg1=token;
  else s1 = s1+token;
      }

      // do the same for s2
      while (tokens2.hasMoreTokens()) {
  token = tokens2.nextToken();
  if (!tokens2.hasMoreTokens()
      && cdg.containsKey(token)) cdg2=token;
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
  }
  if (cdg.containsKey(token)) {
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
          &&  cdg.containsKey(token1)) cdg1=token1;
        else {
        //kalina: move it to lower case, so we match irrespective of case
        token1 = token1.toLowerCase();
        v1.add(token1);
        }
    }

    while (tokens2.hasMoreTokens()) {
      token2 = tokens2.nextToken();
      if(!tokens2.hasMoreTokens()
          &&  cdg.containsKey(token2)) cdg2=token2;
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
    if (largerVector.size()>=2) {
//      Out.prln("Rule 13");
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
  private void buildTables(Document doc) {

    // company designators
    cdg = new HashMap();

    if (!extLists.booleanValue()) {// i.e. get cdg from Lookup annotations
       AnnotationSet nameAnnots;
      // get all Lookup annotations
      if (annotationSetName != null) {
        if (annotationSetName.equals("")) {
          nameAnnots = doc.getAnnotations().get("Lookup");
        }
        else {
          nameAnnots =
            doc.getAnnotations(annotationSetName).get("Lookup");
        }
      } else {
        nameAnnots = doc.getAnnotations().get("Lookup");
      }

      if (nameAnnots!=null)
        if (!nameAnnots.isEmpty()) {
          Iterator iter = nameAnnots.iterator();
          while (iter.hasNext()) {
            Annotation annot = (Annotation)iter.next();
            String CdgValue = (String)annot.getFeatures().get("majorType");
            if (CdgValue != null) {
              if (CdgValue.equals("cdg")) {
              // get the actual string
              Long offsetStartAnnot = annot.getStartNode().getOffset();
              Long offsetEndAnnot = annot.getEndNode().getOffset();
              try {
                String annotString =
                              doc.getContent().getContent(
                                offsetStartAnnot,offsetEndAnnot
                              ).toString();
                cdg.put(annotString,"cdg");
              } catch (InvalidOffsetException ioe) {
                ioe.printStackTrace(Err.getPrintWriter());
              }
            } // if (CdgValue.equals("Cdg"))
            }//if
        }// while
      } // if (!nameAnnots.isEmpty())

    } //if (!intCdgList)
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

} // public class Namematch

/*
 * AnnotationMatches is a class encapsulating
 * information about an annotation and
 * the annotations that it matches
 * it is used to assist in implementing the
 * "transitive" rule namematch effect
 */
class AnnotationMatches {

  /** a list of matched annotation ids */
  private List matchedAnnots;

  /** constructor */
  AnnotationMatches() {
   matchedAnnots = new ArrayList();
  }

  /** method to add an annotation id into the list of matched annotations */
  void addMatchedAnnotId(Integer matchedId) {
    matchedAnnots.add(matchedId);
  }

  /** method to check if an annotation (to me metched)
    * is already in the list of matched annotations
    */
  boolean containsMatched(Integer matchedId) {
    return matchedAnnots.contains(matchedId);
  }

  /** return the list with matches*/
  List getMatched() {
    return matchedAnnots;
  }

  /** the size of the matches list*/
  int howMany(){
    return matchedAnnots.size();
  }

  /** get the element i from the matches list */
  Integer matchedAnnotAt(int i) {
    return (Integer) matchedAnnots.get(i);
  }

} //class AnnotationMatches
