/*
 * OntologyEditor.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 17/04/2002
 *
 * $Id$
 */
package gate.creole.ontology;

import gate.*;
import gate.creole.ResourceInstantiationException;

import com.ontotext.gate.vr.*;

import java.util.*;

import javax.swing.*;


public interface OntologyEditor extends VisualResource {

/**create a new ontology
 * @param name the name of the ontology
 * @param sourceURI
 * @param theURL
 * @param comment */
public void createOntology
  (String name, String sourceURI, String theURL, String comment)
  throws ResourceInstantiationException;

/** set ontology to be loaded in the editor
 * @param o the ontology to be loaded */
public void setOntology(Ontology o);

/** get the loaded ontology
 * @return the current ontology in the editor*/
public Ontology getOntology();

/**set the list of ontologies to be loaded in the editor
 * @param list the list of ontologies */
public void setOntologyList(Vector list);

/**get the list of ontologies currently in the editor
 * @return the list of ontologies */
public Vector getOntologyList();

/** visualize the editor */
public void visualize();

/**invokes an add sub class dialog in position x,y
 * @param x the x coordinate of the dialog
 * @param y the y coordinate of the dialog*/
public void addSubClass(int x,int y) ;

/**addSubClass given a ClassNode and the resulting info from the dialog
 * @param root the node which is root to the sub class being added
 * @param className the name from the dialog
 * @param classComment the comment from the dialog */
public void addSubClass(ClassNode root, String className, String classComment);

/**removes the node/class
 * @param node the node to be removed*/
public void removeClass(ClassNode node);

/**rename a class
 * @param c the class to be renamed
 * @param n the class node associated with the class
 * @param x coords
 * @param y coords */
public void renameClass(OClass c, ClassNode n, int x, int y);

/**is called when an ontology has been selecte from the ontology list
 * @param o the selected ontology */
public void ontologySelected(Ontology o) ;

/**Save a list of ontologies.
 * @param list a list of ontologies to be saved*/
public void saveOntologies(Vector list);

/**close list of ontologies
 * @param list a list of ontologies to be saved*/
 public void closeOntologies(Vector list) throws ResourceInstantiationException;

/**Get Modified Ontologies
 * @return list of the modified ontologies */
public Vector getModifiedOntologies() ;

/*----------ontologies list popup menu item listeners------------
note: these methods could be invoked from within a listener or explicitly*/

/**save this ontology
 * @param o the ontology to be saved */
public void saveOntology(Ontology o) throws ResourceInstantiationException ;

/** invoke a saveas dialog for this ontology and save it
 *  to the location specified
 *  @param o the ontology to be saved
 *  @param x the x coordinate of the save as dialog
 *  @param y the y coordinate of the save as dialog*/
public void saveAsOntology(Ontology o, int x, int y) throws ResourceInstantiationException;

/**rename an ontology
 * @param o the ontology to be renamed
 * @param x the x coordinate of the rename dialog
 * @param y the y coordinate of the rename dialog*/
public void renameOntology(Ontology o, int x, int y);

/**delete an ontology. invoke AreYouSureDialog if the
 * ontology has been changed.
 * @param o the ontology to be deleted
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void deleteOntology(Ontology o, int x, int y)
  throws ResourceInstantiationException;

/** edit the URI of an ontology
 * @param o the ontology to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 */
public void editURI(Ontology o, int x, int y);

/** edit the URI of an ontology class
 * @param c class to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void editClassURI(OClass c, int x, int y);

/**
 * @return all the uris that are available in the editor
 */
public Set getAllURIs() ;

/**retrieve a set of all the URIs in an ontology
 * @param o the ontology
 * @return set of all the URIs in the ontology
 */
public Set getAllURIs(Ontology o) ;

/**close an ontology. invoke AreYouSureDialog if the
 * ontology has been changed.
 * @param o the ontology to be closed
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void closeOntology(Ontology o, int x, int y)
  throws ResourceInstantiationException;

/*End-------ontologies list popup menu item listeners------------*/

/**Wanna Save Dialog invocation
 * @param o the ontology to be saved or not
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked
 * @return the result of the option pane */
public int AskWannaSave(Ontology o, int x, int y);

/*------------- menu bar methods --------------*/

public void fileExit();

public void fileOpen(int x,int y)throws ResourceInstantiationException;

/**
 * invoke a mutiple selection save dialog with a list of ontologies.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally saved
 */
public void fileSave(int x, int y,Vector ontologies) ;

/**
 * invoke a mutiple selection close dialog with a list of ontologies.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally closed
 */
public void fileClose(int x, int y,Vector ontologies) ;

/**inovke a 'new ontology dialog'
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void fileNew(int x, int y);


}//interface OntologyEditor

