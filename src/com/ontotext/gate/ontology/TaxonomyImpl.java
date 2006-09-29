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
import gate.util.*;

/**
 * A Taxonomy Implementation Class
 * 
 * @author borislav popov
 * @author Valentin Tablan
 */
public class TaxonomyImpl extends gate.creole.AbstractLanguageResource
                                                                      implements
                                                                      Taxonomy {

  /** denotes a direct closure(no transitivity) */
  public static final byte DIRECT_CLOSURE = 0;

  /** denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  public static final boolean DEBUG = false;

  private String label;

  private URL url;

  private String defaultNameSpace;

  private String version;

  private String id;

  private String comment;

  private Map classesByName = new HashMap();

  private Set classes = new HashSet();

  private Set tops;

  private String name;

  protected long lastGeneratedId = 0;

  /**
   * Listeners of ontology modification event
   */
  protected ArrayList ontologyModificationListeners;

  /**
   * constructor
   */
  public TaxonomyImpl() {
    super();
    ontologyModificationListeners = new ArrayList();
  }

  /**
   * Gets a taxonomy by URL. The taxonomy will be searched first among
   * the existing LRs and afterwards loaded by the URL if not found
   * 
   * @param someUrl the url of the taxonomy
   * @return the retrieved or loaded taxonomy
   * @throws ResourceInstantiationException if something gets wrong with
   *           the loading
   */
  public static Taxonomy getOntology(URL someUrl)
          throws ResourceInstantiationException {
    // This is the type of taxonomy that gets created when a previously
    // loaded one was not found. This would normally be a constant, but
    // as this method is a horrible
    // hack and will be removed at the first opportunity, it was
    // preferred to
    // store this value here to insulate the hacky code from the rest of
    // this
    // class.
    String DEFAULT_ONTOLOGY_TYPE = "gate.creole.ontology.jena.JenaOntologyImpl";

    // first try and find an appropriate already loaded taxonomy
    List loadedTaxonomies = null;
    try {
      loadedTaxonomies = Gate.getCreoleRegister().getAllInstances(
              Taxonomy.class.getName());
    }
    catch(GateException ge) {
      throw new ResourceInstantiationException("Cannot list loaded taxonomies",
              ge);
    }

    Taxonomy result = null;
    Iterator taxIter = loadedTaxonomies.iterator();
    while(result == null && taxIter.hasNext()) {
      Taxonomy aTaxonomy = (Taxonomy)taxIter.next();
      if(aTaxonomy.getURL().equals(someUrl)) result = aTaxonomy;
    }

    // if not found, load it
    if(result == null) {
      // hardcoded to use OWL as the ontology type
      FeatureMap params = Factory.newFeatureMap();
      params.put("owlLiteFileURL", someUrl);
      result = (Taxonomy)Factory.createResource(DEFAULT_ONTOLOGY_TYPE, params);
    }

    return result;
  }

  /**
   * Whether the ontology has been modified switches to true when
   * null-ing and reinfering the subclasses and super classes and tops
   */
  protected boolean nullBuffers = false;

  /**
   * Whether the ontology has been modified after loading. once it
   * became true it stays true till a save takes place
   */
  protected boolean modified = false;

  /** Initialises this resource, and returns it. */
  public Resource init() throws ResourceInstantiationException {
    if(null == url)
      throw new ResourceInstantiationException("URL not set (null).");
    load();
    return this;
  } // init()

  public URL getURL() {
    return url;
  }

  public void setURL(URL aUrl) {
    url = aUrl;
    if(url != null) {
      /* unpack the gate:path urls to absolute form */
      if(-1 != url.getProtocol().indexOf("gate")) {
        url = gate.util.protocols.gate.Handler.class.getResource(Files
                .getResourcePath()
                + url.getPath());
      }// if
    }
  }// void setURL(URL)

  /**
   * Sets the label of the ontology
   * 
   * @param theLabel the label to be set
   */
  public void setLabel(String theLabel) {
    label = theLabel;
    fireOntologyLabelChanged();
  }

  /**
   * Retrieves the label of the ontology
   * 
   * @return the label of the ontology
   */
  public String getLabel() {
    return label;
  }

  public void load() throws ResourceInstantiationException {
    throw new UnsupportedOperationException(
            "OntologyImpl does not support load().\nRefer to DAMLOntology.");
  }

  public void store() throws ResourceInstantiationException {
    throw new UnsupportedOperationException(
            "OntologyImpl does not support store().\nRefer to DAMLOntology.");
  }

  public void setDefaultNameSpace(String theURI) {
    this.modified = true;
    defaultNameSpace = theURI;
    if(defaultNameSpace != null && -1 == defaultNameSpace.indexOf('#')) {
      defaultNameSpace = defaultNameSpace + '#';
    }
    fireOntologyDefaultNameSpaceChanged();
  }

  public String getDefaultNameSpace() {
    return defaultNameSpace;
  }

  public void setVersion(String theVersion) {
    this.modified = true;
    version = theVersion;
    fireOntologyVersionChanged();
  }

  public String getVersion() {
    return version;
  }

  public String getId() {
    return id;
  }

  public void setId(String theID) {
    this.modified = true;
    id = theID;
    fireOntologyIDChanged();
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String theComment) {
    this.modified = true;
    comment = theComment;
    fireOntologyCommentChanged();
  }

  public TClass createClass(String aName, String aComment) {
    this.modified = true;
    TClass theClass = new TClassImpl(Long.toString(++lastGeneratedId), aName,
            aComment, this);
    theClass.setURI(getDefaultNameSpace() + aName);
    addClass(theClass);
    nullBuffers = true;
    return theClass;
  }

  /**
   * note: if a class is deleted and there aresome subclasses of this
   * class which lack any other super classes : then they become top
   * classes. this could be changed on request or made optional.
   * 
   * @param theClass the class to be removed
   */
  public void removeClass(TClass theClass) {
    classesByName.remove(theClass.getName());
    classes.remove(theClass);
    setModified(true);
    nullBuffers = true;
    // the class as a listener should be removed first
    // otherwise it will cause in an infinite loop
    fireOntologyResourceRemoved(theClass);
  }

  public void addClass(TClass theClass) {
    setModified(true);
    classes.add(theClass);
    if(DEBUG) System.out.println("Class Added :" + theClass.getName());

    classesByName.put(theClass.getName(), theClass);
    nullBuffers = true;
    fireOntologyResourceAdded(theClass);
  }

  public TClass getClassByName(String theName) {
    return (TClass)classesByName.get(theName);
  }

  public boolean containsClassByName(String theName) {
    return classesByName.containsKey(theName);
  }

  public Set getClasses() {
    return classes;
  }

  public Iterator getClasses(Comparator comp) {
    /** @todo: to be implemented */
    return null;
  }

  private void determineTops() {
    tops = new HashSet();
    TClass currentClass;
    Iterator citer = classes.iterator();
    while(citer.hasNext()) {
      currentClass = (TClass)citer.next();
      if(currentClass.isTopClass()) {
        tops.add(currentClass);
      }
    } // while citer
  } // determineTops();

  public Set getTopClasses() {
    if(nullBuffers) {
      reinfer();
    } // if nullBuffers
    if(null == tops) {
      determineTops();
    }
    return new HashSet(tops);
  }

  /**
   * calculates the taxonomic distance between two classes. note that
   * the method is relatively big, but in case similar methods are
   * developed for graph traversal, some parts of this method would
   * naturally become separate methods/members.
   * 
   * @param class1 the first class
   * @param class2 the second class
   */
  public int getTaxonomicDistance(TClass class1, TClass class2) {
    int result = 0;
    ArrayList root = new ArrayList();
    TClass c;
    /* */
    ArrayList supers1 = class1.getSuperClassesVSDistance();
    ArrayList supers2 = class2.getSuperClassesVSDistance();
    /* test if class1-2 are sub/super of each other */
    for(int i1 = 0; i1 < supers1.size(); i1++) {
      if(((Set)supers1.get(i1)).contains(class2)) {
        result = i1 + 1;
        break;
      }
    } // for i1
    for(int i2 = 0; i2 < supers2.size(); i2++) {
      if(((Set)supers2.get(i2)).contains(class1)) {
        result = i2 + 1;
        break;
      }
    } // for i2
    /* find common classes/nodes */
    if(0 == result) {
      for(int i1 = 0; i1 < supers1.size(); i1++) {
        for(int i2 = 0; i2 < supers2.size(); i2++) {
          Set s1 = (Set)supers1.get(i1);
          Set s2 = (Set)supers2.get(i2);
          Iterator i3 = s1.iterator();
          while(i3.hasNext()) {
            c = (TClass)i3.next();
            if(s2.contains(c)) {
              result = i1 + i2 + 2;
              i1 = supers1.size();
              i2 = supers2.size();
              break;
            }
          } // while i3
        } // for i2
      } // for i1
    } // if result is zero
    return result;
  }

  /**
   * Compares the id,uri and url of the ontology.
   * 
   * @param o another ontology to compare with
   * @return true if id,uri and url match
   */
  public boolean equals(Object o) {
    boolean result = false;
    if(o instanceof Taxonomy) {
      Taxonomy onto = (Taxonomy)o;
      result = true;
      if(null != this.getId() & null != onto.getId())
        result &= this.getId().equals(onto.getId());
      else {
        /*
         * check if both ids are null; if so, consider the ontologies
         * partially equal
         */
        result = this.getId() == onto.getId();
      }
      if(null != this.getURL() & null != onto.getURL())
        result &= this.getURL().equals(onto.getURL());
      else result = this.getURL() == onto.getURL();
      if(null != this.getDefaultNameSpace()
              & null != onto.getDefaultNameSpace())
        result &= this.getDefaultNameSpace().equals(onto.getDefaultNameSpace());
      else result = this.getDefaultNameSpace() == onto.getDefaultNameSpace();
    }
    return result;
  } // equals

  public String toString() {
    return getName();
  }

  /**
   * Called when the ontology has been modified to re-infer all
   * sub/super classes, tops, etc. currently could be implemented
   * simpler but this implementation could be useful in the future
   */
  protected void reinfer() {
    tops = null;
  } // reinfer

  public void setModified(boolean isModified) {
    modified = isModified;
  }

  public boolean isModified() {
    return modified;
  }

  /**
   * Check for subclass relation with transitive closure
   * 
   * @param cls1 the first class
   * @param cls2 the second class
   */
  public boolean isSubClassOf(String cls1, String cls2) {
    boolean result = false;
    TClass c1 = getClassByName(cls1);
    TClass c2 = getClassByName(cls2);
    if(null != c1 && null != c2) {
      if(c1.equals(c2)) {
        result = true;
      }
      else {
        Set subs1;
        subs1 = c1.getSubClasses(TClass.TRANSITIVE_CLOSURE);
        if(subs1.contains(c2)) result = true;
      } // else
    } // if not null classes
    return result;
  }

  /**
   * Check for subclass relation with direct closure
   * 
   * @param cls1 the first class
   * @param cls2 the second class
   */
  public boolean isDirectSubClassOf(String cls1, String cls2) {
    boolean result = false;
    TClass c1 = getClassByName(cls1);
    TClass c2 = getClassByName(cls2);
    if(null != c1 && null != c2) {
      if(c1.equals(c2)) {
        result = true;
      }
      else {
        Set subs1;
        subs1 = c1.getSubClasses(TClass.DIRECT_CLOSURE);
        if(subs1.contains(c2)) result = true;
      } // else
    } // if not null classes
    return result;
  }

  /**
   * A Method to invoke an event for newly added ontology resource
   * @param resource
   */
  protected void fireOntologyResourceAdded(OntologyResource resource) {
    OntologyModificationEvent ome = new OntologyModificationEvent(this,
            resource, OntologyModificationEvent.ONTOLOGY_RESOURCE_ADDED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a removed ontology resource
   * @param resource
   */
  protected void fireOntologyResourceRemoved(OntologyResource resource) {
    OntologyModificationEvent ome = new OntologyModificationEvent(this,
            resource, OntologyModificationEvent.ONTOLOGY_RESOURCE_REMOVED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a changed label of taxonomy
   */
  protected void fireOntologyLabelChanged() {
    OntologyModificationEvent ome = new OntologyModificationEvent(this, this,
            OntologyModificationEvent.ONTOLOGY_LABEL_CHANGED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a changed default name space
   */
  protected void fireOntologyDefaultNameSpaceChanged() {
    OntologyModificationEvent ome = new OntologyModificationEvent(this, this,
            OntologyModificationEvent.ONTOLOGY_DEFAULT_NAMESPACE_CHANGED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a changed version of taxonomy
   */
  protected void fireOntologyVersionChanged() {
    OntologyModificationEvent ome = new OntologyModificationEvent(this, this,
            OntologyModificationEvent.ONTOLOGY_VERSION_CHANGED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a changed taxonomy ID
   */
  protected void fireOntologyIDChanged() {
    OntologyModificationEvent ome = new OntologyModificationEvent(this, this,
            OntologyModificationEvent.ONTOLOGY_ID_CHANGED);
    fireOntologyModified(ome);
  }

  /**
   * A Method to invoke an event for a changed comment of taxonomy
   */
  protected void fireOntologyCommentChanged() {
    OntologyModificationEvent ome = new OntologyModificationEvent(this, this,
            OntologyModificationEvent.ONTOLOGY_COMMENT_CHANGED);
    fireOntologyModified(ome);
  }

  /**
   * This method fires the ontology modified event to all registered listeners 
   * for the given ontology modification event
   * @param ome
   */
  protected void fireOntologyModified(OntologyModificationEvent ome) {
    // before firing this event, we make necessary changes in the ontology
    ontologyModified(ome);
    
    for(int i = ontologyModificationListeners.size() - 1; i > -1; i--) {
      ((OntologyModificationListener)ontologyModificationListeners.get(i))
              .ontologyModified(ome);
    }
  }

  /**
   * Register the Ontology Modification Listeners
   * 
   * @param oml
   */
  public void addOntologyModificationListener(OntologyModificationListener oml) {
    this.ontologyModificationListeners.add(oml);
  }

  /**
   * Removed the registered ontology modification listeners
   * 
   * @param oml
   */
  public void removeOntologyModificationListener(
          OntologyModificationListener oml) {
    this.ontologyModificationListeners.remove(oml);
  }
  
  /**
   * This method is invoked whenever a resource in ontology is modified
   */
  public void ontologyModified(OntologyModificationEvent ome) {
    if(ome.getEventType() == OntologyModificationEvent.ONTOLOGY_RESOURCE_REMOVED) {
      Taxonomy taxonomy = ome.getSource();
      if(ome.getResource() instanceof TClass) {
        TClass deleted = (TClass) ome.getResource();
        // we need to delete this class as a sub class of all its super classes
        Set superClasses = deleted.getSuperClasses(TClass.DIRECT_CLOSURE);
        if(superClasses != null) {
          Iterator iter = superClasses.iterator();
          while(iter.hasNext()) {
            TClass superClass = (TClass) iter.next();
            superClass.removeSubClass(deleted);
          }
        }
        
        // we need to delete all its subclasses as well as instances
        Set subClasses = deleted.getSubClasses(TClass.DIRECT_CLOSURE);
        if(subClasses != null) {
          Iterator iter = subClasses.iterator();
          while(iter.hasNext()) {
            TClass subClass = (TClass) iter.next();
            taxonomy.removeClass(subClass);
          }
        }
      }
    }    
  }
} // Taxonomy
