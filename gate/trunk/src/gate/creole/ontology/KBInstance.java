/*
 * KBInstance.java
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
 * Kalina Bontcheva 11/2003
 *
 *
 *  $Id$
 */

package gate.creole.ontology;

import java.util.Set;

public interface KBInstance extends OInstance {

  public void setDifferentFrom(KBInstance theIndividual);

  public Set getDifferentFrom();

  public void setPropertyValue(KBProperty theProperty, Object theValue);

  public Object getPropertyValue(KBProperty theProperty);

  public void setSameIndividualAs(KBInstance theIndividual);

  public Set getSameIndividualAs();
}