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


/**
 * An interface defining the methods of an Ontology Editor.
 */
public interface OntologyEditor extends VisualResource {

/**Creates a new ontology
 * @param name the name of the ontology
 * @param sourceURI the URI of the ontology
 * @param theURL the URL of the ontology
 * @param comment ontology's comment */
public void createOntology
  (String name, String sourceURI, String theURL, String comment)
  throws ResourceInstantiationException;

/**Sets the ontology to be loaded in the editor
 * @param o the ontology to be loaded */
public void setOntology(Ontology o);

/** Gets the loaded ontology
 * @return the current ontology in the editor*/
public Ontology getOntology();

/**Sets the list of ontologies to be loaded in the editor
 * @param list the list of ontologies */
public void setOntologyList(Vector list);

/**Gets the list of ontologies currently in the editor
 * @return the list of ontologies */
public Vector getOntologyList();

/** Visualizes the editor */
public void visualize();

/**Invokes an add sub class dialog in position x,y
 * @param x the x coordinate of the dialog
 * @param y the y coordinate of the dialog*/
public void addSubClass(int x,int y) ;

/**Adds Sub Class given a ClassNode and the resulting info
 * from the Add Sub Class dialog.
 * @param root the node which is root to the sub class being added
 * @param className the name from the dialog
 * @param classComment the comment from the dialog */
public void addSubClass(ClassNode root, String className, String classComment);

/**Removes the node/class
 * @param node the node to be removed*/
public void removeClass(ClassNode node);

/**Renames a class
 * @param c the class to be renamed
 * @param n the class node associated with the class
 * @param x coords
 * @param y coords */
public void renameClass(OClass c, ClassNode n, int x, int y);

/**Selects an ontology.
 * Is called when an ontology has been selecte
 * from the ontology list.
 * @param o the selected ontology */
public void ontologySelected(Ontology o) ;

/**Saves a list of ontologies.
 * @param list a list of ontologies to be saved*/
public void saveOntologies(Vector list);

/**Closes list of ontologies
 * @param list a list of ontologies to be saved*/
 public void closeOntologies(Vector list) throws ResourceInstantiationException;

/**Gets all modified ontologies.
 * @return list of the modified ontologies */
public Vector getModifiedOntologies() ;

/*----------ontologies list popup menu item listeners------------
note: these methods could be invoked from within a listener or explicitly*/

/**Saves this ontology
 * @param o the ontology to be saved
 * @throws  */
public void saveOntology(Ontology o) throws ResourceInstantiationException ;

/**Invokes a Save As dialog for this ontology and saves it
 *  to the specified location.
 *  @param o the ontology to be saved
 *  @param x the x coordinate of the save as dialog
 *  @param y the y coordinate of the save as dialog*/
public void saveAsOntology(Ontology o, int x, int y) throws ResourceInstantiationException;

/**Renames an ontology
 * @param o the ontology to be renamed
 * @param x the x coordinate of the rename dialog
 * @param y the y coordinate of the rename dialog*/
public void renameOntology(Ontology o, int x, int y);

/**Deletes an ontology. Invokes AreYouSureDialog if the
 * ontology has been changed.
 * @param o the ontology to be deleted
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void deleteOntology(Ontology o, int x, int y)
  throws ResourceInstantiationException;

/** Edits the URI of an ontology.
 * @param o the ontology to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 */
public void editURI(Ontology o, int x, int y);

/** Edit the URI of an ontology class
 * @param c class to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void editClassURI(OClass c, int x, int y);

/**
 * Gets all URIs that are present at the moment as ontology URIs.
 * @return all the uris that are available in the editor
 */
public Set getAllURIs() ;

/**Retrieve a set of all the class URIs in an ontology
 * @param o the ontology
 * @return set of all the URIs in the ontology
 */
public Set getAllURIs(Ontology o) ;

/**Closes an ontology. Invokes AreYouSureDialog if the
 * ontology has been changed.
 * @param o the ontology to be closed
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void closeOntology(Ontology o, int x, int y)
  throws ResourceInstantiationException;

/*End-------ontologies list popup menu item listeners------------*/

/**Wanna Save Dialog invocation.
 * @param o the ontology to be saved or not
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked
 * @return the result of the option pane */
public int AskWannaSave(Ontology o, int x, int y);

/*------------- menu bar methods --------------*/

/**
 * Acts on choosing Exit from the File menu.
 */
public void fileExit();

/**
 * Acts on choosing Open from the File menu.
 * @param x the x coordinate of the invocation
 * @param y the y coordinate of the invocation
 * @throws ResourceInstantiationException if something goes wrong with the loading.
 */
public void fileOpen(int x,int y)throws ResourceInstantiationException;

/**
 * Invoke a mutiple selection save dialog with a list of ontologies to be saved.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally saved
 */
public void fileSave(int x, int y,Vector ontologies) ;

/**
 * Invokes a mutiple selection close dialog with a list of ontologies to be closed.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally closed
 */
public void fileClose(int x, int y,Vector ontologies) ;

/**Inovkes a 'new ontology dialog'.
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void fileNew(int x, int y);


}//interface OntologyEditor

