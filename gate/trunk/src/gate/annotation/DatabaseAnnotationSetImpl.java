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

  protected HashMap addedAnnotationsList = new HashMap();
  protected HashMap removedAnnotationsList = new HashMap();
  protected HashMap updatedAnnotationsList = new HashMap();

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
              + "added annots: " + addedAnnotationsList
              + "removed annots: " + removedAnnotationsList
              + "updated annots: " + updatedAnnotationsList;
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
      addedAnnotationsList.put(ann.getId(), ann);
    }

    public void annotationRemoved(AnnotationSetEvent e){
      AnnotationSet set = (AnnotationSet)e.getSource();
      String setName = set.getName();
      if (setName != DatabaseAnnotationSetImpl.this.name &&
          ! setName.equals(DatabaseAnnotationSetImpl.this.name))
        return;
      Annotation ann = e.getAnnotation();
      ann.removeAnnotationListener(this);
      removedAnnotationsList.put(ann.getId(), ann);
    }

    public void annotationUpdated(AnnotationEvent e){
      Annotation ann = (Annotation) e.getSource();
      updatedAnnotationsList.put(ann.getId(), ann);
    }

  }//inner class EventsHandler

}