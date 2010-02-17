/*
 *  Utils.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution annotationSet file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Johann Petrak, 2010-02-05
 *
 *  $Id: Main.java 12006 2009-12-01 17:24:28Z thomas_heitz $
 */

package gate;

import gate.annotation.AnnotationSetImpl;
import gate.util.GateRuntimeException;
import java.util.Iterator;

/**
 * Various utility methods to make often-needed tasks more easy and
 * using up less code.
 * @author Johann Petrak
 */
public class Utils {
  /**
   * Return the length of the document content covered by an Annotation annotationSet an
   * int -- if the content is too long for an int, the method will throw
   * a GateRuntimeException. Use getLengthLong(SimpleAnnotation ann) if
   * this situation could occur.
   * @param ann the annotation for which to determine the length
   * @return the length of the document content covered by this annotation.
   */
  public static int length(SimpleAnnotation ann) {
    long len = lengthLong(ann);
    if (len > java.lang.Integer.MAX_VALUE) {
      throw new GateRuntimeException(
              "Length of annotation too big to be returned as an int: "+len);
    } else {
      return (int)len;
    }
  }

  /**
   * Return the length of the document content covered by an Annotation annotationSet a
   * long.
   * @param ann the annotation for which to determine the length
   * @return the length of the document content covered by this annotation.
   */
  public static long lengthLong(SimpleAnnotation ann) {
    return ann.getEndNode().getOffset() -
       ann.getStartNode().getOffset();
  }

  /**
   * Return the length of the document annotationSet an
   * int -- if the content is too long for an int, the method will throw a
   * GateRuntimeException. Use getLengthLong(Document doc) if
   * this situation could occur.
   * @param doc the document for which to determine the length
   * @return the length of the document content.
   */
  public static int length(Document doc) {
    long len = doc.getContent().size();
    if (len > java.lang.Integer.MAX_VALUE) {
      throw new GateRuntimeException(
              "Length of document too big to be returned as an int: "+len);
    } else {
      return (int)len;
    }
  }

  /**
   * Return the length of the document annotationSet a long.
   * @param doc the document for which to determine the length
   * @return the length of the document content.
   */
  public static long lengthLong(Document doc) {
    return doc.getContent().size();
  }

  /**
   * Return the DocumentContent corresponding to the annotation.
   * <p>
   * Note: the DocumentContent object returned will also contain the
   * original content which can be accessed using the getOriginalContent()
   * method.
   * @param doc the document from which to extract the content
   * @param ann the annotation for which to return the content.
   * @return a DocumentContent representing the content spanned by the annotation.
   */
  public static DocumentContent getContentForAnnotation(
          SimpleDocument doc, SimpleAnnotation ann) {
    try {
      return doc.getContent().getContent(
              ann.getStartNode().getOffset(),
              ann.getEndNode().getOffset());
    } catch(gate.util.InvalidOffsetException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  /**
   * Return the document text annotationSet a String corresponding to the annotation.
   * @param doc the document from which to extract the document text
   * @param ann the annotation for which to return the text.
   * @return a String representing the text content spanned by the annotation.
   */
  public static String getStringForAnnotation(
          Document doc, SimpleAnnotation ann) {
    try {
      return doc.getContent().getContent(
              ann.getStartNode().getOffset(),
              ann.getEndNode().getOffset()).toString();
    } catch(gate.util.InvalidOffsetException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  /**
   * Return a the subset of annotations from the given annotation set
   * that start exactly at the given offset.
   *
   * @param annotationSet the set of annotations from which to select
   * @param atOffset the offset where the annoation to be returned should start
   * @return an annotation set containing all the annotations from the original
   * set that start at the given offset
   */
  public static AnnotationSet getAnnotationsAtOffset(
          AnnotationSet annotationSet, Long atOffset) {
    // this returns all annotations that start at this atOffset OR AFTER!
    AnnotationSet tmp = annotationSet.get(atOffset);
    // so lets filter ...
    AnnotationSet ret = new AnnotationSetImpl(annotationSet.getDocument());
    Iterator<Annotation> it = tmp.iterator();
    while(it.hasNext()) {
      Annotation ann = it.next();
      if(ann.getStartNode().getOffset().equals(atOffset)) {
        ret.add(ann);
      }
    }
    return ret;
  }


}
