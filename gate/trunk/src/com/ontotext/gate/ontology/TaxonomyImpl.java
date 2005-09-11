/*
 * TaxonomyImpl.java
 * Copyright:    Copyright (c) 2001, OntoText Lab.
 * Company:      OntoText Lab.
 * borislav popov 02/2002 */

package com.ontotext.gate.ontology;

import java.util.*;
import java.net.*;
import gate.creole.*;
import gate.creole.ontology.*;
import gate.*;
import gate.event.*;
import gate.util.*;

/**An Ontology Implementation Class
  * @author borislav popozv*/
public class TaxonomyImpl
extends gate.creole.AbstractLanguageResource implements Taxonomy {

  /**denotes a direct closure(no transitivity)*/
  public static final byte DIRECT_CLOSURE = 0;
  /**denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  /** Object Modification Listeners */
  private Set listeners = new HashSet();

  private String label;
  private URL url;
  private String sourceURI;
  private String version;
  private String id;
  private String comment;
  private Map classesByName = new HashMap();
  private Set classes = new HashSet();
  private Set tops;
  private String name;
  protected long lastGeneratedId = 0;

  /**
   * Adds an object modification listener.
   * @param listener listener to be added.
   */
  public void addObjectModificationListener(ObjectModificationListener listener) {
    if ( null==listener )
      throw new IllegalArgumentException(
        "The object modification listener should not be [null].");
    listeners.add(listener);
  }

  /**Fires an object modification event.
   * @param event the event to be fired   */
  protected void fireObjectModificationEvent(Object source) {
    ObjectModificationEvent event = new ObjectModificationEvent(source,
      ObjectModificationEvent.OBJECT_MODIFIED,ObjectModificationEvent.OBJECT_MODIFIED);
    ArrayList ll = new ArrayList(listeners);
    for (int i = 0 ; i < ll.size(); i++ ) {
      ((ObjectModificationListener)ll.get(i)).objectModified(event);
    }
  }

  /* this method might cause performance problems
  and should be updated when naso's idea with the ids
  associated with documents is realized.*/
  public Taxonomy getOntology(URL someUrl)throws ResourceInstantiationException{

    List lrs = Gate.getCreoleRegister().getLrInstances(
        "com.ontotext.gate.ontology.DAMLOntology");

    Taxonomy result = null;
    Taxonomy tempo = null;

    /* unpack the gate:path urls to absolute form*/
    if (-1 != someUrl.getProtocol().indexOf("gate")) {
      someUrl = gate.util.protocols.gate.Handler.class.getResource(
                    Files.getResourcePath() + someUrl.getPath());
    }// if


    /*iterate through the list of lrs and search for the wanted url
    :this is a temporary solution*/
    for (int i = 0 ; i < lrs.size() ; i++ ) {
       tempo = (Taxonomy) lrs.get(i);
       if (tempo.getURL().equals(someUrl)) {
        result = tempo;
        break;
       }
    }
    if ( null == result ) {
      FeatureMap fm = Factory.newFeatureMap();
      fm.put("URL",someUrl);

      try {
        result = (Taxonomy)Factory.createResource(
            "com.ontotext.gate.ontology.DAMLOntology",
            fm
          );
      } catch (Exception e) {
        throw new ResourceInstantiationException(e);
      }
    }
    return result;
  }// getOntology(url)


  /** Whether the ontology has been modified
   *  switches to true when null-ing and reinfering the
   *  subclasses and super classes and tops */
  protected boolean nullBuffers = false;

  /**Whether the ontology has been modified after loading.
   * once it became true it stays true till a save takes place*/
  protected boolean modified = false;

  /** Initialises this resource, and returns it. */
  public Resource init() throws ResourceInstantiationException {
    if (null == url )
      throw new ResourceInstantiationException("URL not set (null).");

    load();
    return this;
  } // init()

  public URL getURL() {
    return url;
  }

  public void setURL(URL aUrl) {
    url = aUrl;
    if ( null == url ) {
      throw new GateRuntimeException("Ontology URL set to null.");
    }
    /* unpack the gate:path urls to absolute form*/
    if (-1 != url.getProtocol().indexOf("gate")) {
      url = gate.util.protocols.gate.Handler.class.getResource(
                    Files.getResourcePath() + url.getPath());
    }// if
  }// void setURL(URL)

  /**Sets the label of the ontology
   * @param theLabel the label to be set
   */
  public void setLabel(String theLabel) {
    label = theLabel;
  }

  /** Retrieves the label of the ontology
   *  @return the label of the ontology */
  public String getLabel() {
    return label;
  }


  public void load() throws ResourceInstantiationException {
    throw new UnsupportedOperationException(
      "OntologyImpl does not support load().\nRefer to DAMLOntology.");
  }

  public void store() throws ResourceInstantiationException  {
    throw new UnsupportedOperationException(
      "OntologyImpl does not support store().\nRefer to DAMLOntology.");
  }

  public void setSourceURI(String theURI) {
    this.modified = true;
    sourceURI = theURI;
    if (-1==sourceURI.indexOf('#')){
      sourceURI = sourceURI+'#';
    }
    fireObjectModificationEvent(this);
  }

  public String getSourceURI(){
    return sourceURI;
  }

  public void setVersion(String theVersion) {
    this.modified = true;
    version = theVersion;
    fireObjectModificationEvent(this);
  }

  public String getVersion() {
    return version;
  }

  public String getId() {
    return id;
  }

  public void setId(String theID){
    this.modified = true;
    id = theID;
    fireObjectModificationEvent(this);
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String theComment) {
    this.modified = true;
    comment = theComment;
    fireObjectModificationEvent(this);
  }


  public TClass createClass(String aName, String aComment) {
    this.modified = true;
    TClass theClass
      = new TClassImpl(Long.toString(++lastGeneratedId),aName,aComment,this);
    addClass(theClass);
    nullBuffers = true;
    fireObjectModificationEvent(this);
    return theClass;
  }

  /**
   * note: if a class is deleted and there aresome subclasses of this class
   * which lack any other super classes : then they become top classes.
   * this could be changed on request or made optional.
   * @param theClass the class to be removed    */
  public void removeClass(TClass theClass) {
    try {
      this.modified = true;
      Iterator superi = theClass.getSuperClasses(TClass.DIRECT_CLOSURE).iterator();
      while ( superi.hasNext() ) {
        TClass sc = (TClass)superi.next();
        sc.removeSubClass(theClass);
      } // while supers
      Iterator subi = theClass.getSubClasses(TClass.DIRECT_CLOSURE).iterator();
      while(subi.hasNext()) {
        TClass sc = (TClass) subi.next();
        sc.removeSuperClass(theClass);
      } // while subs

      classes.remove(theClass);
      classesByName.remove(theClass.getName());
      nullBuffers = true;
      fireObjectModificationEvent(this);

    } catch (NoSuchClosureTypeException x) {
      throw new GateRuntimeException(x.getMessage()) ;
    }
  }

  public void addClass(TClass theClass) {
    this.modified = true;
    classes.add(theClass);
    classesByName.put(theClass.getName(),theClass);
    nullBuffers = true;
    fireObjectModificationEvent(this);

  }

  public TClass getClassByName(String theName) {
    return (TClass) classesByName.get(theName);
  }

  public boolean containsClassByName(String theName) {
    return classesByName.containsKey(theName);
  }

  public Set getClasses() {
    return classes;
  }

  public Iterator getClasses(Comparator comp) {
    /**@todo: to be implemented */
    return null;
  }

  private void determineTops() {
    tops = new HashSet();
    TClass currentClass;
    Iterator citer = classes.iterator();
    while (citer.hasNext()) {
      currentClass = (TClass)citer.next();
      if (currentClass.isTopClass()) {
        tops.add(currentClass);
      }
    } //while citer
  } // determineTops();

  public Set getTopClasses() {
    if ( nullBuffers ) {
      reinfer();
    } // if nullBuffers
    if (null == tops) {
      determineTops();
    }

    return new HashSet(tops);
  }

  /** calculates the taxonomic distance between two classes.
   *  note that the method is relatively big, but in case similar
   *  methods are developed for graph traversal, some parts of this
   *  method would naturally become separate methods/members.
   *
   *  @param class1 the first class
   *  @param class2 the second class */
  public int getTaxonomicDistance(TClass class1, TClass class2) {
    int result=0;
    ArrayList root = new ArrayList();
    TClass c;

    /* */
    ArrayList supers1 = class1.getSuperClassesVSDistance();
    ArrayList supers2 = class2.getSuperClassesVSDistance();

    /* test if class1-2 are sub/super of each other */
    for ( int i1 = 0; i1<supers1.size(); i1++ ) {
      if (((Set)supers1.get(i1)).contains(class2)) {
        result = i1 +1 ;
        break;
      }
    } // for i1
    for ( int i2 = 0; i2<supers2.size(); i2++ ) {
      if (((Set)supers2.get(i2)).contains(class1)) {
        result = i2 +1 ;
        break;
      }
    } // for i2


    /*find common classes/nodes*/
    if ( 0 == result ) {
      for ( int i1 = 0; i1<supers1.size(); i1++ ) {

        for ( int i2 = 0; i2<supers2.size(); i2++) {

          Set s1 = (Set)supers1.get(i1);
          Set s2 = (Set)supers2.get(i2);

          Iterator i3 = s1.iterator();

          while ( i3.hasNext() ) {
            c = (TClass)i3.next();
            if (s2.contains(c)) {
              result = i1 + i2 + 2;
              i1 = supers1.size();
              i2 = supers2.size();
              break;
            }

          } // while i3

        } //for i2

      } // for i1
    } // if result is zero



    return result;
  }


  /**
   * Compares the id,uri and url of the ontology.
   * @param o another ontology to compare with
   * @return true if id,uri and url match
   */
  public boolean equals ( Object o ) {
    boolean result = false;
    if (o instanceof Taxonomy) {
      Taxonomy onto = (Taxonomy) o;
      result = true;
      if (null != this.getId() & null != onto.getId())
        result &= this.getId().equals(onto.getId());
      else {
        /* check if both ids are null; if so, consider the ontologies
        partially equal*/
        result = this.getId() == onto.getId();
      }

      if (null != this.getURL() & null != onto.getURL())
        result &= this.getURL().equals(onto.getURL());
      else
        result = this.getURL() == onto.getURL();

      if (null != this.getSourceURI() & null != onto.getSourceURI())
        result &= this.getSourceURI().equals(onto.getSourceURI());
      else
        result = this.getSourceURI() == onto.getSourceURI();
    }
    return result ;
  } // equals

  public String toString() {
    return getName();
  }

  /**Called when the ontology has been modified to re-infer
   * all sub/super classes, tops, etc.
   * currently could be implemented simpler but
   * this implementation could be useful in the future*/
  protected void reinfer() {
    tops = null;
  }  //reinfer

  public void setModified(boolean isModified) {
    modified = isModified;
    if (modified) fireObjectModificationEvent(this);
  }

  public boolean isModified() {
    return modified;
  }


  /** Check for subclass relation with transitive closure
   * @param cls1 the first class
   * @param cls2 the second class
   */
  public boolean isSubClassOf(String cls1, String cls2)
      throws gate.creole.ontology.NoSuchClosureTypeException {

    boolean result = false;
    TClass c1 = getClassByName(cls1);
    TClass c2 = getClassByName(cls2);

    if (null != c1 && null != c2) {
      if (c1.equals(c2)) {
        result = true;
      }
      else {
        Set subs1;
        subs1 = c1.getSubClasses(TClass.TRANSITIVE_CLOSURE);
        if (subs1.contains(c2))
          result = true;
      } // else
    } // if not null classes
    return result;
  }


  /** Check for subclass relation with direct closure
   * @param cls1 the first class
   * @param cls2 the second class
  */
  public boolean isDirectSubClassOf(String cls1, String cls2)
      throws gate.creole.ontology.NoSuchClosureTypeException {

    boolean result = false;
    TClass c1 = getClassByName(cls1);
    TClass c2 = getClassByName(cls2);

    if (null != c1 && null != c2) {
      if (c1.equals(c2)) {
        result = true;
      }
      else {
        Set subs1;
        subs1 = c1.getSubClasses(TClass.DIRECT_CLOSURE);
        if (subs1.contains(c2))
          result = true;
      } // else
    } // if not null classes
    return result;
  }

} // Taxonomy