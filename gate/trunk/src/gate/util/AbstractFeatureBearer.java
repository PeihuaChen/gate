/*
 *  AbstractFeatureBearer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Hamish Cunningham, 15/Oct/2000
 *
 *  $Id$
 */

package gate.util;
import java.util.*;
import gate.*;

/** A convenience implemetation of FeatureBearer.
  * @see FeatureBearer
  */
abstract public class AbstractFeatureBearer
{
  /** Get the feature set */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The feature set */
  protected FeatureMap features;

} // class AbstractFeatureBearer
