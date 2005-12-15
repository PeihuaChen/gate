/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
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
import com.ontotext.gate.ontology.OntologyImpl;
import com.ontotext.gate.ontology.TaxonomyImpl;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;

import gate.ProtegeProject;
import gate.creole.ontology.TClass;
import gate.creole.ontology.Taxonomy;
import gate.gui.ProtegeWrapper;


/** Dummy Protege LR. Just keep the Protege project file name */
public class ProtegeProjectName extends AbstractLanguageResource
                                implements ProtegeProject, Taxonomy {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Protege project file name */
  private URL projectName;

  /** Protege ontology */
  private edu.stanford.smi.protege.model.KnowledgeBase knBase = null;

  /** Ontotext Ontology object */
  private Taxonomy ontotextOntology = null;
  private URL ontotextOntologyUrl = null;

  /** Keep visual resource to refresh Ontotext Editor if any */
  ProtegeWrapper visualResource = null;

  /** Track changes in Protege KnowledgeBase to transffer in Ontotext Editor */
  private KnowledgeBaseListener _knowledgeBaseListener = null;

  public ProtegeProjectName() {
    projectName = null;
  }

  public void setProjectName(URL name) {
    projectName = name;
  } // setProjectName(String name)

  public URL getProjectName() {
    return projectName;
  } // getProjectName()

  public void setViewResource(ProtegeWrapper visual) {
    visualResource = visual;
  } // setViewResource(AbstractVisualResource visual)

  public void setKnowledgeBase(edu.stanford.smi.protege.model.KnowledgeBase base) {
    knBase = base;
    fillOntotextOntology();
    createKBListener();
  } // setKnowledgeBase(KnowledgeBase base)

  public edu.stanford.smi.protege.model.KnowledgeBase getKnowledgeBase() {
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

      public void frameNameChanged(KnowledgeBaseEvent event) {
        Frame frame = event.getFrame();
        if(frame instanceof Cls) {
          fillOntotextOntology();
          visualResource.refreshOntoeditor(ontotextOntology);
        } // if
      } // frameNameChanged(KnowledgeBaseEvent event)
    };
    knBase.addKnowledgeBaseListener(_knowledgeBaseListener);
  } // createKBListener()

  private void fillOntotextOntology() {
    Collection coll = knBase.getRootClses();
    Iterator it = coll.iterator();
    Cls cls;
    TClass oCls;

    ontotextOntology = new OntologyImpl();
    ontotextOntology.setURL(ontotextOntologyUrl);

    while(it.hasNext()) {
      cls = (Cls) it.next();
      oCls = ontotextOntology.createClass(cls.getName(), "Protege class");
      oCls.setURI("");
      ontotextOntology.addClass(oCls);
      createSubClasses(cls, oCls);
    }

  } // fillOntotextOntology()

  private void createSubClasses(Cls protegeClass, TClass ontotextClass) {
    Cls cls;
    TClass oCls;

    Collection coll = protegeClass.getDirectSubclasses();
    Iterator it = coll.iterator();
    while(it.hasNext()) {
      cls = (Cls) it.next();
      oCls = ontotextOntology.createClass(cls.getName(), "Protege class");
      ontotextClass.addSubClass(oCls);
      createSubClasses(cls, oCls);
    }
  } // createSubClasses(Cls protegeClass, TClass ontotextClass)

//------------------------------------------------------------------------------
//  Ontology interface methods

  public Taxonomy getOntology(URL someUrl)
        throws ResourceInstantiationException {
    return TaxonomyImpl.getOntology(someUrl);
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
      fillOntotextOntology();
      visualResource.refreshOntoeditor(ontotextOntology);
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
    if(ontotextOntology != null) {
      ontotextOntology.setURL(ontotextOntologyUrl);
      ontotextOntology.load();
    } // if
  }
  public void store() throws ResourceInstantiationException {
    if(ontotextOntology != null) {
      ontotextOntology.setURL(ontotextOntologyUrl);
      ontotextOntology.store();
    } // if
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

  public TClass createClass(String aName, String aComment) {
    return ontotextOntology.createClass(aName, aComment);
  }

  public void removeClass(TClass theClass) {
    ontotextOntology.removeClass(theClass);
  }

  public void addClass(TClass theClass) {
    ontotextOntology.addClass(theClass);
  }

  public TClass getClassByName(String theName) {
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
    if(ontotextOntology != null)
      return ontotextOntology.getTopClasses();
    else return new HashSet();
  }

  public int getTaxonomicDistance(TClass class1,TClass class2) {
    return ontotextOntology.getTaxonomicDistance(class1, class2);
  }

  public boolean equals(Object o) {
    boolean result = false;

    if(o instanceof ProtegeProjectName) {
      ProtegeProjectName prj = (ProtegeProjectName) o;
      if(ontotextOntology != null) {
        result = ontotextOntology.equals(prj.ontotextOntology);
      }
      else { // ontology is null
        result = prj.ontotextOntology == null;
      }
    }

    return result;
  }

  public void setModified(boolean isModified) {
    ontotextOntology.setModified(isModified);
  }

  public boolean isModified() {
    if(ontotextOntology == null) return false;
    return ontotextOntology.isModified();
  }

  public boolean isSubClassOf(String cls1, String cls2) {
    return ontotextOntology.isSubClassOf(cls1, cls2);
  }

  public boolean isDirectSubClassOf(String cls1, String cls2) {
    return ontotextOntology.isSubClassOf(cls1, cls2);
  }

} // class ProtegeProjectName extends AbstractLanguageResource