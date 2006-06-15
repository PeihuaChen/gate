/*
 *  SimpleFeatureMap.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, Jul/23/2004
 *
 *  $Id$
 */

package gate;
import java.util.Map;

/** An attribute-value matrix. Represents the content of an annotation, the
  * meta-data on a resource, and anything else we feel like.
  *
  * The event code is needed so a persistent annotation can fire updated events
  * when its features are updated
  */
public interface SimpleFeatureMap extends Map
{
} // interface SimpleFeatureMap
