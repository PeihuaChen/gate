/*
 *  OntoLexLR.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 23/February/2003
 *
 *  $Id$
 */

package gate.lexicon;

import gate.LanguageResource;
import java.util.List;
import java.util.Set;

/**
 *
 * <p>Title: OntoLexLR interface, GATE2 </p>
 * <p>Description: This interface describes the mapping between ontological
 * concepts/instances and lexical entries. Such mapping is needed to
 * support language generation and also understanding that uses both lexicons
 * of synonyms and other lexical info and an ontology of the domain.
 * </p>
 * <P> Developed for the purpose of the
 * <a href="http://www.aktors.org/miakt/">MIAKT project</a>, February 2003.</P>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Sheffield</p>
 * @author Kalina Bontcheva
 * @version 1.0
 */

public interface OntoLexLR extends LanguageResource {

  /** Returns a list of objects which are the concept IDs corresponding to
   * the given lexical Id. A list is returned because there might be more than
   * one such concept IDs. Null is returned if there is no corresponding
   * concept ID.
   */
  public List getConceptIds(Object lexId);

  /** Returns a list of objects which are the lexical IDs corresponding to
   * the given concept Id. A list is returned because there might be more than
   * one such lexical IDs. Null is returned if there is no corresponding
   * lexical ID.
   */
  public List getLexIds(Object conceptId);

  /** Returns a list of objects which are all lexical IDs in this mapping.
   */
  public Set getAllLexIds();

  /** Returns a list of objects which are all concept IDs in this mapping.
   */
  public Set getAllConceptIds();

  /** Add a concept<->lexical ID pair
   */
  public void add(Object conceptId, Object lexId);

  /** Remove all mappings to lexical items for the given concept Id
   */
  public void removeByConcept(Object conceptId);

  /** Remove all mappings to concept items for the given lexical Id
   */
  public void removeByLexId(Object lexId);

  /**
   * True if the mapping is empty
   */
  public boolean isEmpty();

  /**
   * Clear the mapping
   */
  public void clear();

  /**
   * Accessor for the lexical Id property. It
   * specifies which lexicon is this mapping for
   */
  public Object getLexKBIdentifier();

  /**
   * Set method for the lexical Id property. It
   * specifies which lexicon is this mapping for
   */
  public void setLexKBIdentifier(Object lexId);

  /** Accessor for the ontology Id property */
  public Object getOntologyIdentifier();

  /**
   * Set method for the ontology Id property. It
   * specifies which ontology is this mapping for
   */
  public void setOntologyIdentifier(Object ontoId);
}