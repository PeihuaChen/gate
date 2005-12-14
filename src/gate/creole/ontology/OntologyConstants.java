/*
 * OntologyConstants.java
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
 * Valentin Tablan 16-Sep-2005
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

/**
 * This interface holds some constants used by several other intrfaces and
 * classes in the GATE ontology API.
 * 
 * @author Valentin Tablan
 * 
 */
public interface OntologyConstants {
  /** denotes a direct closure(no transitivity) */
  public static final byte DIRECT_CLOSURE = 0;
  /** denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;
}
