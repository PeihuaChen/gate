/*
 *  SimpleFeatureMapImpl.java
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

/** Simple case of features.
  */
public class SimpleFeatureMapImpl extends HashMap implements FeatureMap
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Test if <b>this</b> featureMap object is included in aFeatureMap
    * @param aFeatureMap object which will incude or not this FeatureMap obj.
    * @return <code>true</code> if <b>this</b> is incuded in aFeatureMap
    * and <code>false</code> if not.
    */
  public boolean subsumes(FeatureMap aFeatureMap){


    // null is included in everything
    if (aFeatureMap == null) return true;

    // aFeatureMap != null
    if (!this.keySet().containsAll(aFeatureMap.keySet())) return false;

    // For each key of this, test if the values are equals
    Set aFeatureMapKeySet = aFeatureMap.keySet();
    Iterator aFeatureMapKeySetIter = aFeatureMapKeySet.iterator();
    while (aFeatureMapKeySetIter.hasNext()){
      // Get the key from aFeatureMap
      Object key = aFeatureMapKeySetIter.next();
      // Get the value corresponding to key from aFeatureMap
      Object keyValueFromAFeatureMap = aFeatureMap.get(key);
      // Get the value corresponding to key from this
      Object keyValueFromThis = this.get(key);

      if ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
           (keyValueFromThis != null && keyValueFromAFeatureMap == null)
         ) return false;

      if (keyValueFromThis != null && keyValueFromAFeatureMap != null)
        if (!keyValueFromThis.equals(keyValueFromAFeatureMap)) return false;
    }// End while
    return true;
  }//includedIn

} // class SimpleFeatureMapImpl
