/*
 *  AnnotationDeletePR.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
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
import gate.util.*;

/**
 * This class is the implementation of a processing resource which
 * deletes all annotations and sets other than 'original markups'.
 * If put at the start of an application, it'll ensure that the
 * document is restored to its clean state before being processed.
 */
public class AnnotationDeletePR extends AbstractLanguageAnalyser
  implements ProcessingResource {

  protected String markupSetName = GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME;
  protected List annotationTypes;

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

    //first clear the default set, which cannot be removed
    if (annotationTypes == null || annotationTypes.isEmpty())
      document.getAnnotations().clear();
    else
      removeSubSet(document.getAnnotations());

    //get the names of all sets
    Map namedSets = document.getNamedAnnotationSets();
    //nothing left to do if there are no named sets
    if (namedSets == null || namedSets.isEmpty())
      return;

    //loop through the sets and delete them all unless they're original markups
    List setNames = new ArrayList(namedSets.keySet());
    Iterator iter = setNames.iterator();
    while (iter.hasNext()) {
      String setName = (String) iter.next();
      if (! setName.equals(markupSetName)) {
        if(annotationTypes == null || annotationTypes.isEmpty())
          document.removeAnnotationSet(setName);
        else
          removeSubSet(document.getAnnotations(setName));
      }//if
    }

  } // execute()

  private void removeSubSet(AnnotationSet theSet) {
    AnnotationSet toRemove = theSet.get(new HashSet(annotationTypes));
    if (toRemove == null || toRemove.isEmpty())
      return;
    theSet.removeAll(toRemove);

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

} // class AnnotationSetTransfer
