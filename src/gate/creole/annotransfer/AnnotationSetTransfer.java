/*
 *  AnnotationSetTransfer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 6/8/2001
 *
 *  $Id$
 */

package gate.creole.annotransfer;

import java.util.*;
import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.corpora.*;

/**
 * This class is the implementation of the resource ACEPROCESSOR.
 */
public class AnnotationSetTransfer extends AbstractLanguageAnalyser
  implements ProcessingResource {

  public static final String
    AST_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    AST_INPUT_AS_PARAMETER_NAME = "inputASName";

  public static final String
    AST_OUTPUT_AS_PARAMETER_NAME = "outputASName";

  public static final String
    AST_TAG_AS_PARAMETER_NAME = "tagASName";

  public static final String
    AST_TEXT_TAG_PARAMETER_NAME = "textTagName";

  public static final String DEFAULT_OUTPUT_SET_NAME = "Filtered";
  public static final String DEFAULT_TEXT_TAG_NAME = "BODY";

  protected String   tagASName =  GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME;
  protected String                outputASName = DEFAULT_OUTPUT_SET_NAME;
  protected String                inputASName = null;
  protected String                textTagName = DEFAULT_TEXT_TAG_NAME;
  protected gate.AnnotationSet    bodyAnnotations = null;
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

    if(inputASName != null && inputASName.equals(""))
      inputASName = null;
    if(outputASName != null && outputASName.equals(""))
      outputASName = null;

    //get the input annotation set and the output one
    AnnotationSet inputAS = (inputASName == null) ?
                            document.getAnnotations() :
                            document.getAnnotations(inputASName);
    AnnotationSet outputAS = (outputASName == null) ?
                             document.getAnnotations() :
                             document.getAnnotations(outputASName);
    AnnotationSet tagAS = (tagASName == null) ?
                            document.getAnnotations() :
                            document.getAnnotations(tagASName);

    //if we want to transfer only some types, then select only annotations
    //from these types

    if (annotationTypes != null && annotationTypes.size() > 0)
      inputAS = inputAS.get(new HashSet(annotationTypes));

    //check if we have a BODY annotation
    //if not, just copy all
    if (textTagName == null || textTagName.equals("")) {
      outputAS.addAll(inputAS);
      return;
    }

    //get the BODY annotation
    bodyAnnotations = tagAS.get(textTagName);
    if (bodyAnnotations == null || bodyAnnotations.isEmpty()) {
      Out.prln("AST Warning: No text annotations of type " + textTagName +
        " found, so transferring all annotations to the target set");
      outputAS.addAll(inputAS);
      return;
    }

    List annots2Move = new ArrayList();
    Iterator bodyIter = bodyAnnotations.iterator();
    while (bodyIter.hasNext()) {
      Annotation bodyAnn = (Annotation)bodyIter.next();
      Long start = bodyAnn.getStartNode().getOffset();
      Long end = bodyAnn.getEndNode().getOffset();

      //get all annotations we want transferred
      AnnotationSet annots2Copy = inputAS.getContained(start, end);
      //copy them to the new set and delete them from the old one
      annots2Move.addAll(annots2Copy);
    }
    outputAS.addAll(annots2Move);
    inputAS.removeAll(annots2Move);


  } // execute()

  public void setTagASName(String newTagASName) {
    //if given an empty string, set to the default set
    if ("".equals(newTagASName))
      tagASName = null;
    else
      tagASName = newTagASName;
  }

  public String getTagASName() {
    return tagASName;
  }

  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }

  public String getInputASName() {
    return inputASName;
  }

  public void setOutputASName(String newOutputASName) {
    outputASName = newOutputASName;
  }

  public String getOutputASName() {
    return outputASName;
  }

  public void setTextTagName(String newTextTagName) {
    textTagName = newTextTagName;
  }

  public String getTextTagName() {
    return textTagName;
  }

  public List getAnnotationTypes() {
    return this.annotationTypes;
  }

  public void setAnnotationTypes(List newTypes) {
    annotationTypes = newTypes;
  }


} // class AnnotationSetTransfer
