/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov 18/04/2002
 *
 *  $Id$
 *
 */
package gate.creole;

import java.net.URL;
import java.util.*;

import gate.*;
import gate.creole.ontology.*;
import gate.gui.ProtegeWrapper;
import com.ontotext.gate.ontology.OntologyImpl;
// Protege import
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.event.*;


/** Dummy Protege LR. Just keep the Protege project file name */
public class ProtegeProjectName extends AbstractLanguageResource
                                implements ProtegeProject, Ontology {

  /** Protege project file name */
  private String projectName;

  /** Protege ontology */
  private KnowledgeBase knBase = null;

  /** Ontotext Ontology object */
  private Ontology ontotextOntology = null;
  private URL ontotextOntologyUrl = null;

  /** Keep visual resource to refresh Ontotext Editor if any */
  ProtegeWrapper visualResource = null;

  /** Track changes in Protege KnowledgeBase to transffer in Ontotext Editor */
  private KnowledgeBaseListener _knowledgeBaseListener = null;

  public ProtegeProjectName() {
    projectName = null;
  }

  public void setProjectName(String name) {
    projectName = name;
  } // setProjectName(String name)

  public String getProjectName() {
    return projectName;
  } // getProjectName()

  public void setViewResource(ProtegeWrapper visual) {
    visualResource = visual;
  } // setViewResource(AbstractVisualResource visual)

  public void setKnowledgeBase(KnowledgeBase base) {
    knBase = base;
    fillOntotextOntology();
    createKBListener();
  } // setKnowledgeBase(KnowledgeBase base)

  public KnowledgeBase getKnowledgeBase() {
    return knBase;
  } // getKnowledgeBase()

  private void createKBListener() {
    _knowledgeBaseListener = new KnowledgeBaseAdapter() {
      public void clsCreated(KnowledgeBaseEvent event) {
        fillOntotextOntology();
        visualResource.refreshOntoeditor(ontotextOntology);
      } // clsCreated(KnowledgeBaseEvent event)

      public void clsDeleted(KnowledgeBaseEvent event) {
        fillOntotextOntology();
        visualResource.refreshOntoeditor(ontotextOntology);
      } // clsDeleted(KnowledgeBaseEvent event)
    };
    knBase.addKnowledgeBaseListener(_knowledgeBaseListener);
  } // createKBListener()

  private void fillOntotextOntology() {
    Collection coll = knBase.getRootClses();
    Iterator it = coll.iterator();
    Cls cls;
    OClass oCls;

    ontotextOntology = new OntologyImpl();
    ontotextOntology.setURL(ontotextOntologyUrl);

    while(it.hasNext()) {
      cls = (Cls) it.next();
      oCls = ontotextOntology.createClass(cls.getName(), "Protege class");
      ontotextOntology.addClass(oCls);
      createSubClasses(cls, oCls);
    }

  } // fillOntotextOntology()

  private void createSubClasses(Cls protegeClass, OClass ontotextClass) {
    Cls cls;
    OClass oCls;

    Collection coll = protegeClass.getDirectSubclasses();
    Iterator it = coll.iterator();
    while(it.hasNext()) {
      cls = (Cls) it.next();
      oCls = ontotextOntology.createClass(cls.getName(), "Protege class");
      ontotextClass.addSubClass(oCls);
      createSubClasses(cls, oCls);
    }
  } // createSubClasses(Cls protegeClass, OClass ontotextClass)

//------------------------------------------------------------------------------
//  Ontology interface methods

  public Ontology getOntology(URL someUrl)
        throws ResourceInstantiationException {
    return ontotextOntology.getOntology(someUrl);
  }

  public String getLabel() {
     return ontotextOntology.getLabel();
  }

  public void setLabel(String label) {
    ontotextOntology.setLabel(label);
  }

  public URL getURL() {
    return ontotextOntologyUrl;
  }
  public void setURL(URL aUrl) {
    ontotextOntologyUrl = aUrl;
    if(ontotextOntology != null) {
      ontotextOntology.setURL(aUrl);
    } // if
  }
  public void setSourceURI(String theURI) {
    ontotextOntology.setSourceURI(theURI);
  }
  public String getSourceURI() {
    return ontotextOntology.getSourceURI();
  }
  public void setVersion(String theVersion) {
    ontotextOntology.setVersion(theVersion);
  }
  public String getVersion() {
    return ontotextOntology.getVersion();
  }
  public void load() throws ResourceInstantiationException {
    ontotextOntology.load();
  }
  public void store() throws ResourceInstantiationException {
    ontotextOntology.store();
  }
  public String getId() {
    return ontotextOntology.getId();
  }
  public void setId(String theId) {
    ontotextOntology.setId(theId);
  }
  public String getComment() {
    return ontotextOntology.getComment();
  }
  public void setComment(String theComment) {
    ontotextOntology.setComment(theComment);
  }

  public OClass createClass(String aName, String aComment) {
    return ontotextOntology.createClass(aName, aComment);
  }

  public void removeClass(OClass theClass) {
    ontotextOntology.removeClass(theClass);
  }

  public void addClass(OClass theClass) {
    ontotextOntology.addClass(theClass);
  }

  public OClass getClassByName(String theName) {
    return ontotextOntology.getClassByName(theName);
  }

  public boolean containsClassByName(String theName) {
    return ontotextOntology.containsClassByName(theName);
  }

  public Set getClasses() {
    return ontotextOntology.getClasses();
  }

  public Iterator getClasses(Comparator comp) {
    return ontotextOntology.getClasses(comp);
  }

  public Set getTopClasses() {
    return ontotextOntology.getTopClasses();
  }

  public int getTaxonomicDistance(OClass class1,OClass class2) {
    return ontotextOntology.getTaxonomicDistance(class1, class2);
  }

  public boolean equals(Object o) {
    return ontotextOntology.equals(o);
  }

  public void setModified(boolean isModified) {
    ontotextOntology.setModified(isModified);
  }

  public boolean isModified() {
    return ontotextOntology.isModified();
  }

} // class ProtegeProjectName extends AbstractLanguageResource