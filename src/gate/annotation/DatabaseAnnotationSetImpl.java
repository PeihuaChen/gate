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

import java.util.*;

import junit.framework.*;

import gate.event.*;
import gate.*;
import gate.util.*;
import gate.corpora.*;
//import gate.persist.*;


public class DatabaseAnnotationSetImpl extends AnnotationSetImpl
                                       implements DatastoreListener,
                                                  EventAwareAnnotationSet {

  /**
   * The listener for the events coming from the document (annotations and
   * annotation sets added or removed).
   */
  protected EventsHandler eventHandler;

  protected HashSet addedAnnotations = new HashSet();
  protected HashSet removedAnnotations = new HashSet();
  protected HashSet updatedAnnotations = new HashSet();

  private boolean validating = false;

  public void assertValid() {

    if (validating)
      return;

    validating = true;
    //avoid recursion

    //doc can't be null
    Assert.assertNotNull(this.doc);
    //doc.assertValid();

    validating = false;
  }

  /** Construction from Document. */
  public DatabaseAnnotationSetImpl(Document doc) {

    super(doc);

    //preconditions
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);

    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);

    //add self as listener for sync events from the document's datastore
    doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);

  } // construction from document

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, String name) {
    super(doc, name);

    //preconditions
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);

    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);

    //add self as listener for sync events from the document's datastore
    doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);

  } // construction from document and name


  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, Collection c) {
    this(c);
    this.doc = (DocumentImpl) doc;
    //add self as listener for sync events from the document's datastore
    doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
  } // construction from document and name

  /** Construction from Document and name. */
  public DatabaseAnnotationSetImpl(Document doc, String name, Collection c) {
    this(doc,c);
    this.name = name;
    //add self as listener for sync events from the document's datastore
    doc.getDataStore().removeDatastoreListener(this);
    doc.getDataStore().addDatastoreListener(this);
  } // construction from document and name


  /** Construction from Collection (which must be an AnnotationSet) */
  public DatabaseAnnotationSetImpl(Collection c) throws ClassCastException {

    super(c);

    //also copy the name, because that super one doesn't
    this.name = ((AnnotationSet) c).getName();

    eventHandler = new EventsHandler();
    this.addAnnotationSetListener(eventHandler);

    Iterator iter = this.iterator();
    while(iter.hasNext())
      ((Annotation) iter.next()).addAnnotationListener(eventHandler);

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
      DatabaseAnnotationSetImpl.this.addedAnnotations.add(ann);
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
      if (addedAnnotations.contains(ann)) {
        //a new annotatyion that was deleted afterwards, remove it from all sets
        DatabaseAnnotationSetImpl.this.addedAnnotations.remove(ann);
        return;
      }
      //2. check if the annotation was updated, if so, remove it from the
      //update list
      if (updatedAnnotations.contains(ann)) {
        DatabaseAnnotationSetImpl.this.updatedAnnotations.remove(ann);
      }

      DatabaseAnnotationSetImpl.this.removedAnnotations.add(ann);
    }

    public void annotationUpdated(AnnotationEvent e){
      Annotation ann = (Annotation) e.getSource();

      //check if the annotation is newly created
      //if so, do not add it to the update list, since it was not stored in the
      //database yet, so the most recent value will be inserted into the DB upon
      //DataStore::sync()
      if (addedAnnotations.contains(ann)) {
        return;
      }

      DatabaseAnnotationSetImpl.this.updatedAnnotations.add(ann);
    }

  }//inner class EventsHandler



  /**
   * Called by a datastore when a new resource has been adopted
   */
  public void resourceAdopted(DatastoreEvent evt){
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //check if this is our resource
    //rememeber -  a data store handles many resources
    if (evt.getResourceID().equals(this.doc.getLRPersistenceId()))  {
//System.out.println("ASNAME=["+this.getName()+"], resourceAdopted() called");
      //we're synced wtith the DB now
      clearChangeLists();
    }
  }

  /**
   * Called by a datastore when a resource has been deleted
   */
  public void resourceDeleted(DatastoreEvent evt){

    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //check if this is our resource
    //rememeber -  a data store handles many resources
    if (evt.getResourceID().equals(this.doc.getLRPersistenceId()))  {
//System.out.println("ASNAME=["+this.getName()+"],resourceDeleted() called");

      //unregister self
      DataStore ds = (DataStore)evt.getResource();
      ds.removeDatastoreListener(this);
    }

  }//resourceDeleted

  /**
   * Called by a datastore when a resource has been wrote into the datastore
   */
  public void resourceWritten(DatastoreEvent evt){
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //check if this is our resource
    //rememeber -  a data store handles many resources
    if (evt.getResourceID().equals(this.doc.getLRPersistenceId()))  {
//System.out.println("ASNAME=["+this.getName()+"],resourceWritten() called");

      //clear lists with updates - we're synced with the DB
      clearChangeLists();
    }
  }


  private void clearChangeLists() {

    //ok, we're synced now, clear all lists with changed IDs
    synchronized(this) {
//System.out.println("clearing lists...");
      this.addedAnnotations.clear();
      this.updatedAnnotations.clear();
      this.removedAnnotations.clear();
    }
  }

  public Collection getAddedAnnotations() {
//System.out.println("getAddedIDs() called");
    HashSet result = new HashSet();
    result.addAll(this.addedAnnotations);

    return result;
  }


  public Collection getChangedAnnotations() {
//System.out.println("getChangedIDs() called");
    HashSet result = new HashSet();
    result.addAll(this.updatedAnnotations);

    return result;
  }


  public Collection getRemovedAnnotations() {
//System.out.println("getremovedIDs() called...");
    HashSet result = new HashSet();
    result.addAll(this.removedAnnotations);

    return result;
  }

}