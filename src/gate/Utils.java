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
import gate.util.OffsetComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Various utility methods to make often-needed tasks more easy and
 * using up less code.  In Java code (or JAPE grammars) you may wish to
 * <code>import static gate.Utils.*</code> to access these methods without
 * having to qualify them with a class name.  In Groovy code, this class can be
 * used as a category to inject each utility method into the class of its first
 * argument, e.g.
 * <pre>
 * Document doc = // ...
 * Annotation ann = // ...
 * use(gate.Utils) {
 *   println "Annotation has ${ann.length()} characters"
 *   println "and covers the string \"${doc.stringFor(ann)}\""
 * }
 * </pre>
 *
 * @author Johann Petrak, Ian Roberts
 */
public class Utils {
  /**
   * Return the length of the document content covered by an Annotation as an
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
   * Return the length of the document content covered by an Annotation as a
   * long.
   * @param ann the annotation for which to determine the length
   * @return the length of the document content covered by this annotation.
   */
  public static long lengthLong(SimpleAnnotation ann) {
    return ann.getEndNode().getOffset() -
       ann.getStartNode().getOffset();
  }

  /**
   * Return the length of the document as an
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
   * Return the length of the document as a long.
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
  public static DocumentContent contentFor(
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
   * Return the document text as a String corresponding to the annotation.
   * @param doc the document from which to extract the document text
   * @param ann the annotation for which to return the text.
   * @return a String representing the text content spanned by the annotation.
   */
  public static String stringFor(
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
   * Returns the document text between the provided offsets.
   * @param doc the document from which to extract the document text
   * @param start the start offset 
   * @param end the end offset
   * @return document text between the provided offsets
   */
  public static String stringFor(
          Document doc, Long start, Long end) {
    try {
      return doc.getContent().getContent(
              start,
              end).toString();
    } catch(gate.util.InvalidOffsetException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  /**
   * Return the DocumentContent covered by the given annotation set.
   * <p>
   * Note: the DocumentContent object returned will also contain the
   * original content which can be accessed using the getOriginalContent()
   * method.
   * @param doc the document from which to extract the content
   * @param anns the annotation set for which to return the content.
   * @return a DocumentContent representing the content spanned by the
   * annotation set.
   */
  public static DocumentContent contentFor(
          SimpleDocument doc, AnnotationSet anns) {
    try {
      return doc.getContent().getContent(
              anns.firstNode().getOffset(),
              anns.lastNode().getOffset());
    } catch(gate.util.InvalidOffsetException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  /**
   * Return the document text as a String covered by the given annotation set.
   * @param doc the document from which to extract the document text
   * @param anns the annotation set for which to return the text.
   * @return a String representing the text content spanned by the annotation
   * set.
   */
  public static String stringFor(
          Document doc, AnnotationSet anns) {
    try {
      return doc.getContent().getContent(
              anns.firstNode().getOffset(),
              anns.lastNode().getOffset()).toString();
    } catch(gate.util.InvalidOffsetException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  /**
   * Get the start offset of an annotation.
   */
  public static Long start(SimpleAnnotation a) {
    return (a.getStartNode() == null) ? null : a.getStartNode().getOffset();
  }

  /**
   * Get the start offset of an annotation set.
   */
  public static Long start(AnnotationSet as) {
    return (as.firstNode() == null) ? null : as.firstNode().getOffset();
  }

  /**
   * Get the start offset of a document (i.e. 0L).
   */
  public static Long start(SimpleDocument d) {
    return Long.valueOf(0L);
  }

  /**
   * Get the end offset of an annotation.
   */
  public static Long end(SimpleAnnotation a) {
    return (a.getEndNode() == null) ? null : a.getEndNode().getOffset();
  }

  /**
   * Get the end offset of an annotation set.
   */
  public static Long end(AnnotationSet as) {
    return (as.lastNode() == null) ? null : as.lastNode().getOffset();
  }

  /**
   * Get the end offset of a document.
   */
  public static Long end(SimpleDocument d) {
    return d.getContent().size();
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

  /**
   * Return a List containing the annotations in the given annotation set, in
   * document order (i.e. increasing order of start offset).
   *
   * @param as the annotation set
   * @return a list containing the annotations from <code>as</code> in document
   * order.
   */
  public static List<Annotation> inDocumentOrder(AnnotationSet as) {
    List<Annotation> ret = new ArrayList<Annotation>();
    if(as != null) {
      ret.addAll(as);
      Collections.sort(ret, OFFSET_COMPARATOR);
    }
    return ret;
  }

  /**
   * A single instance of {@link OffsetComparator} that can be used by any code
   * that requires one.
   */
  public static final OffsetComparator OFFSET_COMPARATOR =
          new OffsetComparator();

  /**
   * Create a feature map from an array of values.  The array must have an even
   * number of items, alternating keys and values i.e. [key1, value1, key2,
   * value2, ...].
   *
   * @param values an even number of items, alternating keys and values.
   * @return a feature map containing the given items.
   */
  public static FeatureMap featureMap(Object... values) {
    FeatureMap fm = Factory.newFeatureMap();
    if(values != null) {
      for(int i = 0; i < values.length; i++) {
        fm.put(values[i], values[++i]);
      }
    }
    return fm;
  }

  /**
   * Create a feature map from an existing map (typically one that does not
   * itself implement FeatureMap).
   *
   * @param map the map to convert.
   * @return a new FeatureMap containing the same mappings as the source map.
   */
  public static FeatureMap toFeatureMap(Map map) {
    FeatureMap fm = Factory.newFeatureMap();
    fm.putAll(map);
    return fm;
  }
  
  /**
   * Issue a message to the log but only if the same message has not
   * been logged already in the same GATE session.
   * This is intended for explanations or warnings that should not be 
   * repeated every time the same situation occurs.
   * 
   * @param logger - the logger instance to use
   * @param level  - the severity level for the message
   * @param message - the message itself
   */
  public static void logOnce (Logger logger, Level level, String message) {
    if(!alreadyLoggedMessages.contains(message)) { 
      logger.log(level, message);
      alreadyLoggedMessages.add(message);
    }
  }
  private static final Set<String> alreadyLoggedMessages = 
    Collections.synchronizedSet(new HashSet<String>());

}
