/*
 * OntologyResourceImpl.java
 *
 * Copyright (c) 2005, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan 15-Sep-2005
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

/**
 * This is an implementation for ontology resource. It provides implementations
 * for the methods on the {@link gate.creole.ontology.OntologyResource}
 * interface and is intended to be used as a base class for other classes
 * implementing that interface or its sub-interfaces.
 */
public class OntologyResourceImpl implements OntologyResource {
  protected String uri;
  protected String comment;
  protected String name;
  protected Taxonomy taxonomy;
  protected Ontology ontology;

  public OntologyResourceImpl(String uri, String name, String comment,
          Taxonomy taxonomy) {
    this.comment = comment;
    this.name = name;
    this.taxonomy = taxonomy;
    this.ontology = taxonomy instanceof Ontology ? (Ontology)taxonomy : null;
    this.uri = uri;
  }

  /**
   * Constructor variant using the name as the local URI. 
   * @param name
   * @param comment
   * @param ontology
   */
  public OntologyResourceImpl(String name, String comment,
          Taxonomy taxonomy) {
    this(name, name, comment, taxonomy);
  }
    
  /**
   * @return Returns the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment
   *          The comment to set.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the taxonomy.
   */
  public Taxonomy getTaxonomy() {
    return taxonomy;
  }

  /**
   * @return Returns the ontology.
   */
  public Ontology getOntology() {
    return ontology;
  }
  
  /**
   * @param ontology
   *          The ontology to set.
   */
  public void setOntology(Taxonomy ontology) {
    this.taxonomy = ontology;
  }

  /**
   * @return Returns the URI.
   */
  public String getURI() {
    return uri;
  }

  /**
   * @param uri
   *          The URI to set.
   */
  public void setURI(String uri) {
    this.uri = uri;
  }
}
