/*
 *  ACEProcessor.java
 *
 *  You should probably put a copyright notice here. Why not use the
 *  GNU licence? (See http://www.gnu.org/.)
 *
 *  kalina, 6/8/2001
 *
 *  $Id$
 */

package gate.creole.aceprocessor;

import java.util.*;
import gate.*;
import gate.creole.*;
import gate.util.*;

/**
 * This class is the implementation of the resource ACEPROCESSOR.
 */
public class ACEProcessor extends AbstractProcessingResource
  implements ProcessingResource {

  /**  The document we're working with. */
  protected gate.Document         document;

  protected String                tagASName = "Original markups";
  protected String                outputASName = "ACE";
  protected String                inputASName = null;
  protected String                textTagName = "BODY";
  protected gate.AnnotationSet    bodyAnnotations = null;

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
  public void run()
  {
    if(document == null) throw new GateRuntimeException("No document to process!");
    if(inputASName != null && inputASName.equals("")) inputASName = null;
    if(outputASName != null && outputASName.equals("")) outputASName = null;

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

    //get the BODY annotation
    bodyAnnotations = tagAS.get(textTagName);
    if (bodyAnnotations == null || bodyAnnotations.isEmpty())
      return;

    Iterator bodyIter = bodyAnnotations.iterator();
    while (bodyIter.hasNext()) {
      Annotation bodyAnn = (Annotation)bodyIter.next();
      Long start = bodyAnn.getStartNode().getOffset();
      Long end = bodyAnn.getEndNode().getOffset();

      AnnotationSet annots2Copy = inputAS.get(start, end);
      outputAS.addAll(annots2Copy);
    }


  } // run()


  /** Get the document we're running on. */
  public Document getDocument()
  {
    return document;
  }

  /** Set the document to run on. */
  public void setDocument(Document newDocument)
  {
    this.document = newDocument;
  }

  public void setTagASName(String newTagASName) {
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


} // class ACEProcessor
