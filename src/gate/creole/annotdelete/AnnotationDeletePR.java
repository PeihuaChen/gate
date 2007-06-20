/*
 *  AnnotationDeletePR.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 19/10/2001
 *
 *  $Id$
 */

package gate.creole.annotdelete;

import java.util.*;

import gate.*;
import gate.creole.*;
import gate.util.GateRuntimeException;

/**
 * This class is the implementation of a processing resource which
 * deletes all annotations and sets other than 'original markups'.
 * If put at the start of an application, it'll ensure that the
 * document is restored to its clean state before being processed.
 */
public class AnnotationDeletePR extends AbstractLanguageAnalyser
  implements ProcessingResource {

  public static final String
    TRANSD_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    TRANSD_ANNOT_TYPES_PARAMETER_NAME = "annotationTypes";

  public static final String
    TRANSD_SETS_KEEP_PARAMETER_NAME = "setsToKeep";

  public static final String
    TRANSD_SETS_KEEP_ORIGIANL_MARKUPS_ANNOT_SET = "keppOriginalMarkupsAS";
  
  protected String markupSetName = GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME;
  protected List annotationTypes;
  protected List setsToKeep;
  protected Boolean keepOriginalMarkupsAS;
  

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException
  {
    return super.init();
  } // init()

  /**
  * Reinitialises the processing resource. After calling this method the
  * resource should be in the state it is after calling init.
  * If the resource depends on external resources (such as rules files) then
  * the resource will re-read those resources. If the data used to create
  * the resource has changed since the resource has been created then the
  * resource will change too after calling reInit().
  */
  public void reInit() throws ResourceInstantiationException
  {
    init();
  } // reInit()

  /** Run the resource. */
  public void execute() throws ExecutionException {

    if(document == null)
      throw new GateRuntimeException("No document to process!");

    Map matchesMap = null;
    Object matchesMapObject = document.getFeatures().get(ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME);
    if(matchesMapObject instanceof Map) {
      matchesMap = (Map) matchesMapObject;
    }

    //first clear the default set, which cannot be removed
    if (annotationTypes == null || annotationTypes.isEmpty()) {
      document.removeAnnotationSet(null);
      document.getAnnotations();
      removeFromDocumentCorefData( (String)null, matchesMap);
    } else {
      removeSubSet(document.getAnnotations(), matchesMap);
    }

    //get the names of all sets
    Map namedSets = document.getNamedAnnotationSets();
    //nothing left to do if there are no named sets
    if (namedSets == null || namedSets.isEmpty())
      return;

    //loop through the sets and delete them all unless they're original markups
    List setNames = new ArrayList(namedSets.keySet());
    Iterator iter = setNames.iterator();
    String setName;
    
    if(setsToKeep == null) setsToKeep = new ArrayList();
    if(keepOriginalMarkupsAS.booleanValue()) {
        setsToKeep.add(markupSetName);
    } else {
        setsToKeep.remove(markupSetName);
    }
    
    while (iter.hasNext()) {
      setName = (String) iter.next();
      //check first whether this is the original markups or one of the sets
      //that we want to keep
      if (setName != null) {
        // skip named sets from setsToKeep
        if(setsToKeep != null && setsToKeep.contains(setName)) continue;

        if (annotationTypes == null || annotationTypes.isEmpty()) {
          document.removeAnnotationSet(setName);
          removeFromDocumentCorefData( (String) setName, matchesMap);
        } else {
          removeSubSet(document.getAnnotations(setName), /* Niraj */ matchesMap /* End */);
        }
      }//if
    }

    /* Niraj */
    // and finally we add it to the document
    if(matchesMap != null) {
      document.getFeatures().put(ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME,
                                 matchesMap);
    }
    /* End */

  } // execute()

  /* Niraj */
  // method to undate the Document-Coref-data
  private void removeFromDocumentCorefData(String currentSet, Map matchesMap) {
    if(matchesMap == null)
      return;

    // if this is defaultAnnotationSet, we cannot remove this
    if(currentSet == null) {
      java.util.List matches = (java.util.List) matchesMap.get(currentSet);
      if (matches == null || matches.size() == 0) {
        // do nothing
        return;
      }
      else {
        matchesMap.put(currentSet, new java.util.ArrayList());
      }
    } else {
      // we remove this set from the Coref Data
      matchesMap.remove(currentSet);
    }
  }

  // method to update the Document-Coref-data
  private void removeAnnotationsFromCorefData(AnnotationSet annotations, String setName, Map matchesMap) {
    if(matchesMap == null) {
      return;
    }

    java.util.List matches = (java.util.List) matchesMap.get(setName);
    if(matches == null)
      return;

    // each element in the matches is a group of annotation IDs
    // so for each annotation we will have to traverse through all the lists and
    // find out the annotation and remove it
    ArrayList annots = new ArrayList(annotations);
    for(int i=0;i<annots.size();i++) {
      Annotation toRemove = (Annotation) annots.get(i);
      Iterator idIters = matches.iterator();
      ArrayList ids = new ArrayList();
      while(idIters.hasNext()) {
        ids = (ArrayList) idIters.next();
        if(ids.remove(toRemove.getId())) {
          // yes removed
          break;
        }
      }
      if(ids.size()==0) {
        matches.remove(ids);
      }
    }
    // and finally see if there is any group available
    if(matches.size()==0) {
      matchesMap.remove(setName);
    }
  }

  /* End */

  private void removeSubSet(AnnotationSet theSet, Map matchMap) {
    AnnotationSet toRemove = theSet.get(new HashSet(annotationTypes));
    if (toRemove == null || toRemove.isEmpty())
      return;
    theSet.removeAll(toRemove);
    /* Niraj */
    removeAnnotationsFromCorefData(toRemove, theSet.getName(), matchMap);
    /* End */
  }//removeSubSet

  public void setMarkupASName(String newMarkupASName) {
    markupSetName = newMarkupASName;
  }

  public String  getMarkupASName() {
    return markupSetName;
  }

  public List getAnnotationTypes() {
    return this.annotationTypes;
  }

  public void setAnnotationTypes(List newTypes) {
    annotationTypes = newTypes;
  }

  public List getSetsToKeep() {
    return this.setsToKeep;
  }

  public void setSetsToKeep(List newSetNames) {
    setsToKeep = newSetNames;
  }

  public Boolean getKeepOriginalMarkupsAS() {
    return keepOriginalMarkupsAS;
  }

  public void setKeepOriginalMarkupsAS(Boolean emptyDefaultAnnotationSet) {
    this.keepOriginalMarkupsAS = emptyDefaultAnnotationSet;
  }


} // class AnnotationSetTransfer
