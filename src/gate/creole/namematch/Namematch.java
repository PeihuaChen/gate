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

  protected String annotationSetName;

  /** the type of the annotation */
  protected Set annotationTypes = new HashSet();

  /** the organization type*/
  protected String organizationType;

  /** the person type*/
  protected String personType;

  /** the type of annotation*/
  protected String annotationType;

  /** internal or external list cdg*/
  protected Boolean intCdgList;

  /** internal or external list */
  protected Boolean intExtLists;

  /** the annotation set for the document */
  protected AnnotationSet nameAnnots ;

  /** the vector with all the matches from document*/
  protected Vector matchesDocument = null;

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
    intExtLists = new Boolean(true);
    intCdgList = new Boolean(true);
    annotationTypes.add("Organization");
    annotationTypes.add("Person");

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

    if (intExtLists.equals("true"))
      buildTables(document, intCdgList);
    else {
      try {
      createLists();
      } catch (IOException ioe) {ioe.printStackTrace();}
    }

    AnnotationSet nameAllAnnots;
    if ((annotationSetName == null)|| (annotationSetName == "")){
      nameAllAnnots = document.getAnnotations();
    } else {
      nameAllAnnots = document.getAnnotations(annotationSetName);
    }

    if (nameAllAnnots != null) {
      if (nameAllAnnots.isEmpty()) {
        Out.prln("No annotations");
        return;
      }
    } else {Out.prln("No annotations");return;}

    // go through all the annotation types
    Iterator iterAnnotationTypes = annotationTypes.iterator();
    while (iterAnnotationTypes.hasNext()) {
      annotationType = (String)iterAnnotationTypes.next();

      nameAnnots = nameAllAnnots.get(annotationType,
                                  Factory.newFeatureMap());
      // return if no such annotations exist
      if (nameAnnots != null) {
        if (nameAnnots.isEmpty()) {
          Out.prln("No annotations of this type");
        }
        else
        {
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

              // put string in vector
              annotStringMap.put(annot,annotString);

            } catch (InvalidOffsetException ioe) {
              executionException = new ExecutionException
                                     ("Invalid offset of the annotation");
            }
          } // for

          // now go through the annotations
          // table for matched annotations
          Hashtable matched_annots = new Hashtable();
          Vector previousAnnots = new Vector();

          Iterator iteratorNameAnnots = nameAnnots.iterator();
          while (iteratorNameAnnots.hasNext()) {
            Annotation annot1 = (Annotation)iteratorNameAnnots.next();

            // get string from vector and NOT from spans
            String annotString1 = (String) annotStringMap.get(annot1);

            Integer annot1_id = annot1.getId();

            AnnotationMatches matchedAnnot1 = new AnnotationMatches();

            // now compare the annotation with the previous ones
            for (int j=0;j<previousAnnots.size();j++) {
              Annotation annot2 = (Annotation)previousAnnots.get(j);
              String annotString2 = (String) annotStringMap.get(annot2);

              Integer annot2_id = annot2.getId();
              // first check that annot2 is NOT already in the list of
              // matched annotations for annot1 - do not proceed with
              // annot2 if so
              if (matched_annots.containsKey(annot1_id.toString())) {
                matchedAnnot1 =
                  (AnnotationMatches) matched_annots.get(annot1_id.toString());
                if (matchedAnnot1.containsMatched(annot2_id.toString())) continue;
              }

              // find which annotation string of the two is longer
              //  this is useful for some of the matching rules
              String longName = null;
              String shortName = null;

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

                if (matched_annots.containsKey(annot2_id.toString()))
                  matchedAnnot2 =
                   (AnnotationMatches) matched_annots.get(annot2_id.toString());

                // first add the annotation ids matched by annot2 to those
                // matched by annot1
                for (int k=0;k<matchedAnnot2.howMany();k++) {
                  String matchedByAnnot2Id = matchedAnnot2.matchedAnnotAt(k);
                  if (!matchedAnnot1.containsMatched(matchedByAnnot2Id))
                    matchedAnnot1.addMatchedAnnotId(
                                              matchedAnnot2.matchedAnnotAt(k));

                    // then add annot1 to all those annotations
                    // that have been matched with annot2 so far
                    matchedByAnnot2 = (AnnotationMatches)
                                          matched_annots.get(matchedByAnnot2Id);

                    matchedByAnnot2.addMatchedAnnotId(annot1_id.toString());
                } // for (int k=0;

                // last add annotation 2 to those of annot1
                // and annotation 1 to those of annot2
                matchedAnnot1.addMatchedAnnotId(annot2_id.toString());
                matchedAnnot2.addMatchedAnnotId(annot1_id.toString());

                matched_annots.put(annot1_id.toString(),matchedAnnot1);
                matched_annots.put(annot2_id.toString(),matchedAnnot2);

              } // if (apply_rules_namematch
            } //for j
            previousAnnots.add(annot1);
          }// for i

          // append the "matches" attribute to existing annotations
          for (Enumeration enum =
                            matched_annots.keys(); enum.hasMoreElements() ;) {
            String annot_id = (String) enum.nextElement();
            AnnotationMatches matchedAnnot =
              (AnnotationMatches) matched_annots.get(annot_id);
            Vector matchesVector = matchedAnnot.getMatched();

            // remove attribute "matches" if such exists:
            // i.e has the namematcher run on the same doc b4?
            Annotation annot = nameAnnots.get(new Integer(annot_id));
            FeatureMap attr = annot.getFeatures();
            attr.remove("matches");
            attr.put("matches", matchesVector);
          } // for Enumeration

          AnnotationSet nameAnnots1 =
            document.getAnnotations().get(
                                      annotationType, Factory.newFeatureMap());

          // get the annotation from the document
          if ((annotationSetName == null)||(annotationSetName == "")){
            AnnotationSet namedAnnots = document.getAnnotations();

            FeatureMap fm1;
            Annotation annot;
            Iterator iterator;

            // a vector with the matches from the current document
            matchesDocument = new Vector();

            Map booleanMatches = new HashMap();
            Iterator iteratorAnnotation = namedAnnots.iterator();
            while (iteratorAnnotation.hasNext()){
                booleanMatches.put(iteratorAnnotation.next(),"false");
              }

            // go through all the annotations and put all the matches in a vector
            iteratorAnnotation = namedAnnots.iterator();
            while (iteratorAnnotation.hasNext()){
              //the vector with the matches of the current annotation
              Vector matchesAnnotation = new Vector();

              annot = (Annotation)iteratorAnnotation.next();
              Integer idAnnot = annot.getId();

              fm1 = annot.getFeatures();
              iterator = fm1.keySet().iterator();
              while (iterator.hasNext()) {
                String type = (String) iterator.next();
                if (type == "matches") {
                  // add the id of the annotation
                  String valueId = (String)booleanMatches.get(annot);
                  if (valueId.compareTo("false")==0)
                                                matchesAnnotation.add(idAnnot);
                  // update the vector (true)
                  booleanMatches.remove(annot);
                  booleanMatches.put(annot,new String ("true"));
                  Vector vector = (Vector)fm1.get(type);
                  for (int j=0; j< vector.size(); j++) {
                    String value = (String)vector.get(j);
                    int valueInt =(new Integer(value)).intValue();

                    //verify whether it isn't already in matchesDocument
                    Annotation newAnnot = namedAnnots.get(new Integer(value));
                    String id = (String)booleanMatches.get(newAnnot);
                    if (id.compareTo("false")==0){
                      matchesAnnotation.add(new Integer(valueInt));
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
          }
          else {
            Map namedAnnotationSets = document.getNamedAnnotationSets();
            Iterator iter = namedAnnotationSets.keySet().iterator();

            while (iter.hasNext()) {
              AnnotationSet namedAnnots =
                (AnnotationSet)namedAnnotationSets.get(iter.next());

              FeatureMap fm1;
              Annotation annot;
              Iterator iterator;

              // a vector with the matches from the current document
              matchesDocument = new Vector();

              Map booleanMatches = new HashMap();
              Iterator iteratorAnnotation = namedAnnots.iterator();
              while (iteratorAnnotation.hasNext()){
                booleanMatches.put(iteratorAnnotation.next(),"false");
              }

              iteratorAnnotation = namedAnnots.iterator();
              while (iteratorAnnotation.hasNext()){
                //the vector with the matches of the current annotation
                Vector matchesAnnotation = new Vector();

                annot = (Annotation)iteratorAnnotation.next();
                Integer idAnnot = annot.getId();
                fm1 = annot.getFeatures();
                iterator = fm1.keySet().iterator();
                while (iterator.hasNext()) {
                  String type = (String) iterator.next();
                  if (type == "matches") {
                    // add the id of the annotation
                    String valueId = (String)booleanMatches.get(annot);
                    if (valueId.compareTo("false")==0)
                                                matchesAnnotation.add(idAnnot);
                    // update the vector (true)

                    booleanMatches.remove(annot);
                    booleanMatches.put(annot,new String ("true"));
                    Vector vector = (Vector)fm1.get(type);
                    for (int j=0; j< vector.size(); j++) {
                      String value = (String)vector.get(j);

                      //verify whether it isn't already in matchesDocument
                      Annotation newAnnot = namedAnnots.get(new Integer(value));
                      String id = (String)booleanMatches.get(newAnnot);
                      if (id.compareTo("false")==0){
                        matchesAnnotation.add(new Integer(value));
                        booleanMatches.remove(newAnnot);
                        booleanMatches.put(newAnnot,new String ("true"));
                      }
                    } // for
                  } // if
                } // while
                if (matchesAnnotation != null)
                  if (matchesAnnotation.size()>0){
                    matchesDocument.add(matchesAnnotation);
                  }
              } // for
            } // while
          }// if
        }// else
      }//if
    }//for

    return;
  } // run()

  public void check() throws ExecutionException {
    if (executionException != null) {
      ExecutionException e = executionException;
      executionException = null;
      throw e;
    }
  } // check()

  /** if (intExtLists == false) then reads the names of files in order
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

      if (nameList.compareTo("cdg")==0){
        cdg.put(lineRead,"cdg");
      }// if
      else {
        int index = lineRead.indexOf("£");
        if (index != -1){
          String  expr = lineRead.substring(0,index);
          String code = lineRead.substring(index+1,lineRead.length());
          if (nameList.compareTo("alias")==0 )
                            alias.put(expr, code);
          else
          if (nameList.compareTo("def_art")== 0)
                            def_art.put(expr, code);
          else
          if (nameList.compareTo("prepos")== 0)
                            prepos.put(expr, code);
          else
          if (nameList.compareTo("connector")== 0)
                            connector.put(expr, code);
          else
          if (nameList.compareTo("spur_match")== 0)
                            spur_match.put(expr, code);

      }// if

      }
    }//while
  }


  /** apply_rules_namematch: apply rules similarly to lasie1.5's namematch */
  private boolean apply_rules_namematch(String shortName, String longName) {
    // apply matching rules only IF:
    // the two names are of the same type
    // or one has been classified as "unknown"
/*    if (   annot1IdValue.equals(annot2IdValue)
	   ||
	   (
	    annot1IdValue.equals("unknown")
	    &&
	    !annot2IdValue.equals("unknown")
           )
	   ||
	   (
	    !annot1IdValue.equals("unknown")
	    &&
	    annot2IdValue.equals("unknown")
	   )
	   )
      {*/
      // first apply rule for spurius matches i.e. rule0
      if (!matchRule0(longName, shortName)) {
	if (
	     (// rules for all annotations
	        matchRule1(longName, shortName, true)
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
		      matchRule12(shortName, longName))
		  )// rules for person annotations
	     ) // if
	   return true;
      } // if (!matchRule0
//    } // if (annot1.getType().equals(annot2.getType()...
    return false;
  }

  /** set the document */
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }

  /** set the annotations */
  public void setIntExtLists(Boolean newInt_Ext_Lists) {
    intExtLists = newInt_Ext_Lists;
  }

  /** set the annotation set */
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }

  /** set the type of the annotation*/
  public void setAnnotationTypes(Set newType) {
    annotationTypes = newType;
  }

  public void setOrganizationType(String newOrganizationType) {
    organizationType = newOrganizationType;
  }

  public void setPersonType(String newPersonType) {
    personType = newPersonType;
  }

  /** set intCdgList */
  public void setIntCdgList(Boolean newIntCdgList) {
    intCdgList = newIntCdgList;
  }
  /**
   * Gets the document currently set as target for this namematch.
   * @return a {@link gate.Document}
   */
  public gate.Document getDocument() {
    return document;
  }

  /**get the name of the annotation set*/
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  /** get the types of the annotation*/
  public Set getAnnotationTypes() {
    return annotationTypes;
  }

  /** */
  public String getOrganizationType() {
    return organizationType;
  }

  public String getPersonType() {
    return personType;
  }

  public Boolean getIntExtList() {
    return intExtLists;
  }

  public Boolean getIntCdgList() {
    return intCdgList;
  }

  public Vector getMatchesDocument() {
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
	&& spur_match.containsKey(s2)) {
      return
	spur_match.get(s1).toString().equals(spur_match.get(s2).toString());
      }
    return false;
  }

  /** RULE #1: If the two names are identical then they are the same
    * Condition(s): depend on case
    * Applied to: all name annotations
    */
  public boolean matchRule1(String s1,
			     String s2,
			     boolean MatchCase) {
    if (MatchCase == true) return s1.equalsIgnoreCase(s2);
    return s1.equals(s2) ;
  }


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
  }

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
  }

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
      while (tokens1.hasMoreTokens()) {
	if (!tokens1.nextToken().equalsIgnoreCase(tokens2.nextToken())) {
	  allTokensMatch = false;
	  break;
	} // if (!tokens1.nextToken()
      } // while
      return allTokensMatch;
    } // if (tokens1.countTokens() ==
    return false;
  }

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

    if (tokens1.countTokens()>1)
      return matchRule1(tokens1.nextToken(),s2,true);

    return false;
  }

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

    while (tokens1.hasMoreTokens()) {
      token = tokens1.nextToken();
      acronym_s1.append(token.substring(0,1));
    }

    //now check if last token is cdg
    s1 = acronym_s1.toString();

    if (cdg.containsKey(token)) s1 = s1.substring(0,s1.length()-1);

    return matchRule1(s1,s2,false);
  }

  /**
    * RULE #7: if one of the tokens in one of the
    * names is in the list of separators eg. "&"
    * then check if the token before the separator
    * matches the other name
    * e.g. "R.H. Macy & Co." == "Macy"
    * Condition(s): case-sensitive match
    * Applied to: organisation and person annotations only
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
    if (previous_token != null) return matchRule1(previous_token,s2,false);
    return false;
  }

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
  }
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
  }

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
  }

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
  }

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
  }

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
    * Applied to: organisation annotations only
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
    Vector v1 = new Vector();
    Vector v2 = new Vector();
    Vector largerVector = new Vector();
    Vector smallerVector = new Vector();
    int matched_tokens = 0;

    while (tokens1.hasMoreTokens()) {
      	token1 = tokens1.nextToken();
      	if(!tokens1.hasMoreTokens()
          &&  cdg.containsKey(token1)) cdg1=token1;
        else v1.addElement(token1);
    }

    while (tokens2.hasMoreTokens()) {
      token2 = tokens2.nextToken();
      if(!tokens2.hasMoreTokens()
          &&  cdg.containsKey(token2)) cdg2=token2;
          else v2.addElement(token2);
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
      for (Enumeration enum = smallerVector.elements();
    	                        enum.hasMoreElements() ;) {
        token1 = (String) enum.nextElement();
        if (largerVector.contains(token1))  matched_tokens++;
      } // for (Enumeration enum
    }
    if (matched_tokens >= largerVector.size()-1) return true;
    return false;
  }


  /** Tables for namematch info
    * (used by the namematch rules)
    */

  private void buildTables(Document doc, Boolean intCdgList) {

    // aliases table: corresponding aliases have the same value
    alias = new HashMap(17);
    alias.put("National Aeronautics and Space Administration","1");
    alias.put("NASA","1");
    alias.put("New York Stock Exchange","2");
    alias.put("Big Board","2");
    alias.put("Aluminum Co.","3");
    alias.put("Aluminum Co","3");
    alias.put("Alcoa","3");
    alias.put("New York Times Inc.","4");
    alias.put("Times","4");
    alias.put("New York Times","4");
    alias.put("Coca-Cola Co.","5");
    alias.put("Coca-Cola Co","5");
    alias.put("Coca-Cola","5");
    alias.put("Coke","5");
    alias.put("IBM","6");
    alias.put("Big Blue","6");
    alias.put("New York","7");
    alias.put("Big Apple","7");

    // spurius matches
    spur_match= new HashMap(2);
    spur_match.put("Eastern Airways","1");
    spur_match.put("Eastern Air","1");

    // company designators
    cdg = new HashMap();
    if (intCdgList.equals("False")) {// i.e. get cdg from Lookup annotations
      // get all Lookup annotations
      AnnotationSet nameAnnots =
      doc.getAnnotations().get("Lookup", Factory.newFeatureMap());

      if (nameAnnots!=null)
      if (!nameAnnots.isEmpty()) {
        for (int i=0;i<nameAnnots.size();i++) {

          Annotation annot = nameAnnots.get(new Integer(i));
          String CdgValue = annot.getFeatures().get("listName").toString();
          if (CdgValue.equals("Cdg")) {
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
        }// for
      } // if (!nameAnnots.isEmpty())

    } //if (!intCdgList)
    else {
      cdg.put("Co","cdg");
      cdg.put("PTE LTD","cdg");
      cdg.put("AMBA","cdg");
      cdg.put("Ltd.","cdg");
      cdg.put("NA.","cdg");
      cdg.put("Co.","cdg");
      cdg.put("CDERL","cdg");
      cdg.put("L. P.","cdg");
      cdg.put("SARL","cdg");
      cdg.put("PLC.","cdg");
      cdg.put("Pty ltd","cdg");
      cdg.put("S. p. A.","cdg");
      cdg.put("Oy","cdg");
      cdg.put("GMBH & COKG","cdg");
      cdg.put("GMBH","cdg");
      cdg.put("Bv","cdg");
      cdg.put("Pte ltd","cdg");
      cdg.put("L. L. C.","cdg");
      cdg.put("G. M. B. H.","cdg");
      cdg.put("NV.","cdg");
      cdg.put("AG &AMP; COKG","cdg");
      cdg.put("LTD.","cdg");
      cdg.put("S. P. A.","cdg");
      cdg.put("KK.","cdg");
      cdg.put("AG & COKG","cdg");
      cdg.put("GMBH.","cdg");
      cdg.put("Cos","cdg");
      cdg.put("PT","cdg");
      cdg.put("Lp","cdg");
      cdg.put("corp","cdg");
      cdg.put("AB","cdg");
      cdg.put("GMBH &AMP; COKG","cdg");
      cdg.put("Associates","cdg");
      cdg.put("BDH","cdg");
      cdg.put("MIJ","cdg");
      cdg.put("SV","cdg");
      cdg.put("G. m. b. H. &AMP; CO , KG","cdg");
      cdg.put("NV","cdg");
      cdg.put("Ag","cdg");
      cdg.put("Group","cdg");
      cdg.put("CO","cdg");
      cdg.put("Brothers","cdg");
      cdg.put("Sons Co","cdg");
      cdg.put("Ay","cdg");
      cdg.put("eGmbh","cdg");
      cdg.put("CPORA","cdg");
      cdg.put("plc","cdg");
      cdg.put("PERSERO","cdg");
      cdg.put("C.V.","cdg");
      cdg.put("Spa","cdg");
      cdg.put("Corp","cdg");
      cdg.put("gGmbH","cdg");
      cdg.put("LDA","cdg");
      cdg.put("BV","cdg");
      cdg.put("SAC","cdg");
      cdg.put("Corp.","cdg");
      cdg.put("SPA","cdg");
      cdg.put("Ltd","cdg");
      cdg.put("Sons","cdg");
      cdg.put("LLC","cdg");
      cdg.put("PTY LTD","cdg");
      cdg.put("S. A. R. L.","cdg");
      cdg.put("N. V.","cdg");
      cdg.put("Corporation","cdg");
      cdg.put("AG & Co KG","cdg");
      cdg.put("HMIG","cdg");
      cdg.put("LTD","cdg");
      cdg.put("B. V.","cdg");
      cdg.put("INC.","cdg");
      cdg.put("KK","cdg");
      cdg.put("Inc","cdg");
      cdg.put("Na","cdg");
      cdg.put("LP","cdg");
      cdg.put("Incorporated","cdg");
      cdg.put("INC","cdg");
      cdg.put("GmbH.","cdg");
      cdg.put("AG","cdg");
      cdg.put("ASSOCIATES","cdg");
      cdg.put("Gmbh","cdg");
      cdg.put("Bhd","cdg");
      cdg.put("PERUM","cdg");
      cdg.put("CV","cdg");
      cdg.put("Kk","cdg");
      cdg.put("AY","cdg");
      cdg.put("C. por A.","cdg");
      cdg.put("BHD","cdg");
      cdg.put("Company","cdg");
      cdg.put("S. A.","cdg");
      cdg.put("AENP","cdg");
      cdg.put("Limited","cdg");
      cdg.put("plc.","cdg");
      cdg.put("COS","cdg");
      cdg.put("HVER","cdg");
      cdg.put("AG &AMP; Co KG","cdg");
      cdg.put("CORP.","cdg");
      cdg.put("G. m. b. H.","cdg");
      cdg.put("BDH.","cdg");
      cdg.put("Bros.","cdg");
      cdg.put("Group.","cdg");
      cdg.put("Plc","cdg");
      cdg.put("G. m. b. H. & CO , KG","cdg");
      cdg.put("Cos.","cdg");
      cdg.put("SA","cdg");
      cdg.put("PLC","cdg");
      cdg.put("NA","cdg");
      cdg.put("N. A.","cdg");
      cdg.put("PERJAN","cdg");
      cdg.put("Inc.","cdg");
      cdg.put("PP","cdg");
      cdg.put("CORP","cdg");
      cdg.put("C. de R. L.","cdg");
      cdg.put("L.P.","cdg");
      cdg.put("Plc.","cdg");
      cdg.put("EGMBH","cdg");
      cdg.put("PN","cdg");
      cdg.put("OYAB","cdg");
      cdg.put("Limitada","cdg");
      cdg.put("SPA.","cdg");
      cdg.put("OY","cdg");
      cdg.put("Ab","cdg");
      cdg.put("AG.","cdg");
      cdg.put("GmbH","cdg");
      cdg.put("AB.","cdg");
      cdg.put("S. A. C.","cdg");
      cdg.put("Nv","cdg");
      cdg.put("SA.","cdg");
      cdg.put("Sa","cdg");
    }// else

    // definite article - but never used in the program so far!
    def_art=new HashMap(2);
    def_art.put("The","def");
    def_art.put("the","def");

    // connectors
    connector=new HashMap(7);
    connector.put("of","con");
    connector.put("for","con");
    connector.put("de","con");
    connector.put("di","con");
    connector.put("von","con");
    connector.put("van","con");
    connector.put("&","con");

    // prepositions
    prepos = new HashMap(2);
    prepos.put("of","prepos");
    prepos.put("for","prepos");
  }

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
  }

} // public class Namematch

/*
 * AnnotationMatches is a class encapsulating
 * information about an annotation and
 * the annotations that it matches
 * it is used to assist in implementing the
 * "transitive" rule namematch effect
 */
class AnnotationMatches {

  /** a vector of matched annotation ids */
  private Vector matchedAnnots;

  /** constructor */
  AnnotationMatches() {
   matchedAnnots = new Vector();
  }

  /** method to add an annotation id into the vector of matched annotations */
  void addMatchedAnnotId(String matchedId) {
    matchedAnnots.addElement(matchedId);
  }

  /** method to check if an annotation (to me metched)
    * is already in the list of matched annotations
    */
  boolean containsMatched(String matchedId) {
    return matchedAnnots.contains(matchedId);
  }

  Vector getMatched() {
    return matchedAnnots;
  }

  int howMany(){
    return matchedAnnots.size();
  }

  String matchedAnnotAt(int i) {
    return (String) matchedAnnots.elementAt(i);
  }

} //class AnnotationMatches
