/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva 21/10/2001
 *
 *  $Id$
 */

package gate.annotation;

import gate.event.*;
import gate.*;
import java.util.*;
import gate.util.*;


public class DatabaseAnnotationSetImpl extends AnnotationSetImpl {

  /**
   * The listener for the events coming from the document (annotations and
   * annotation sets added or removed).
   */
  protected EventsHandler eventHandler;

  protected HashSet addedAnnotations = new HashSet();
  protected HashSet removedAnnotations = new HashSet();
  protected HashSet updatedAnnotations = new HashSet();

  /** Construction from Document. */
  public DatabaseAnnotationSetImpl(Document doc) {
    super(doc);
    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);
  } // construction from document

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, String name) {
    super(doc, name);
    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);
  } // construction from document and name

  /** Construction from Collection (which must be an AnnotationSet) */
  public DatabaseAnnotationSetImpl(Collection c) throws ClassCastException {
    super(c);
    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);
  } // construction from collection


  public String toString() {
    return super.toString()
              + "added annots: " + addedAnnotations
              + "removed annots: " + removedAnnotations
              + "updated annots: " + updatedAnnotations;
  }

  /**
   * All the events from the document or its annotation sets are handled by
   * this inner class.
   */
  class EventsHandler implements AnnotationListener,
                                 AnnotationSetListener{

    public void annotationAdded(gate.event.AnnotationSetEvent e) {
      AnnotationSet set = (AnnotationSet)e.getSource();
      String setName = set.getName();
      if (setName != DatabaseAnnotationSetImpl.this.name &&
          ! setName.equals(DatabaseAnnotationSetImpl.this.name))
        return;
      Annotation ann = e.getAnnotation();
      ann.addAnnotationListener(this);
      DatabaseAnnotationSetImpl.this.addedAnnotations.add(ann.getId());
    }

    public void annotationRemoved(AnnotationSetEvent e){
      AnnotationSet set = (AnnotationSet)e.getSource();
      String setName = set.getName();
      if (setName != DatabaseAnnotationSetImpl.this.name &&
          ! setName.equals(DatabaseAnnotationSetImpl.this.name))
        return;
      Annotation ann = e.getAnnotation();
      ann.removeAnnotationListener(this);

      //1. check if this annot is in the newly created annotations set
      if (addedAnnotations.contains(ann.getId())) {
        //a new annotatyion that was deleted afterwards, remove it from all sets
        DatabaseAnnotationSetImpl.this.addedAnnotations.remove(ann.getId());
        return;
      }
      //2. check if the annotation was updated, if so, remove it from the
      //update list
      if (updatedAnnotations.contains(ann.getId())) {
        DatabaseAnnotationSetImpl.this.updatedAnnotations.remove(ann.getId());
      }

      DatabaseAnnotationSetImpl.this.removedAnnotations.add(ann.getId());
    }

    public void annotationUpdated(AnnotationEvent e){
      Annotation ann = (Annotation) e.getSource();

      //check if the annotation is newly created
      //if so, do not add it to the update list, since it was not stored in the
      //database yet, so the most recent value will be inserted into the DB upon
      //DataStore::sync()
      if (addedAnnotations.contains(ann.getId())) {
        return;
      }

      DatabaseAnnotationSetImpl.this.updatedAnnotations.add(ann.getId());
    }

  }//inner class EventsHandler

}