/*
 *  FeatureBearer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 7/Feb/2000
 *
 *  $Id$
 */

package gate.util;
import java.util.*;
import gate.*;

/** Classes that have features.
  */
public interface FeatureBearer
{
  /** Get the feature set */
  public FeatureMap getFeatures();

  /** Set the feature set */
  public void setFeatures(FeatureMap features);

  /** Sets the name of this resource*/
  public void setName(String name);

  /** Returns the name of this resource*/
  public String getName();

} // interface FeatureBearer
