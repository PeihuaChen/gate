/*
 *  DumpingPR.java
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

package gate.creole.dumpingPR;

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
public class DumpingPR extends AbstractLanguageAnalyser
  implements ProcessingResource {

  /**
   * A list of annotation types, which are to be dumped into the output file
   */
  protected List annotationTypes;

  /**the name of the annotation set
   * from which to take the annotations for dumping
   */
  protected String annotationSetName;

  /**
   * Whether or not to include the annotation features during export
   */
  protected boolean includeFeatures = false;

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

    AnnotationSet allAnnots;
    // get the annotations from document
    if ((annotationSetName == null)|| (annotationSetName.equals("")))
      allAnnots = document.getAnnotations();
    else
      allAnnots = document.getAnnotations(annotationSetName);

    //if none found, print warning and exit
    if ((allAnnots == null) || allAnnots.isEmpty()) {
      Out.prln("DumpingPR Warning: No annotations found for export. "
               + "Including only those from the Original markups set.");
      write2File(null);
      return;
    }

    //first transfer the annotation types from a list to a set
    //don't I just hate this!
    Set types2Export = new HashSet();
    for(int i=0; i<annotationTypes.size(); i++)
      types2Export.add(annotationTypes.get(i));

    //then get the annotations for export
    AnnotationSet annots2Export = allAnnots.get(types2Export);
    write2File(annots2Export);

  } // execute()

  protected void write2File(AnnotationSet exportSet) {
    Out.prln(document.toXml(exportSet, includeFeatures));
  }

  /**get the name of the annotation set*/
  public String getAnnotationSetName() {
    return annotationSetName;
  }//getAnnotationSetName

  /** set the annotation set name*/
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }//setAnnotationSetName

  public List getAnnotationTypes() {
    return this.annotationTypes;
  }

  public void setAnnotationTypes(List newTypes) {
    annotationTypes = newTypes;
  }

} // class AnnotationSetTransfer
