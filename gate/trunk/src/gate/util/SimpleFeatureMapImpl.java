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
import gate.event.*;
/** Simple case of features. */
//>>> DAM: was (derived from HashMap)
/*
public class SimpleFeatureMapImpl  extends HashMap implements FeatureMap
*/
//=== DAM: FeatArray optimization, now derived from SimpleMapImpl
public class SimpleFeatureMapImpl
    extends SimpleMapImpl
//    extends HashMap
    implements FeatureMap, java.io.Serializable, java.lang.Cloneable
//>>> DAM: end
{
  /** Debug flag */
  private static final boolean DEBUG = false;

 /** Freeze the serialization UID. */
  static final long serialVersionUID = -2747241616127229116L;

  /** Test if <b>this</b> featureMap includes all features from aFeatureMap
    * @param aFeatureMap object which will be included or not in
    * <b>this</b> FeatureMap obj.If this param is null then it will return true.
    * @return <code>true</code> if aFeatureMap is incuded in <b>this</b> obj.
    * and <code>false</code> if not.
    */
  public boolean subsumes(FeatureMap aFeatureMap){
    // null is included in everything
    if (aFeatureMap == null) return true;

    if (this.size() < aFeatureMap.size()) return false;

    SimpleFeatureMapImpl sfm = (SimpleFeatureMapImpl)aFeatureMap;

    Object key;
    Object keyValueFromAFeatureMap;
    Object keyValueFromThis;

    for (int i = 0; i < sfm.count; i++) {
      key = sfm.theKeys[i];
      keyValueFromAFeatureMap = sfm.theValues[i];
      int v = super.getSubsumeKey(key);
      if (v < 0) return false;
      keyValueFromThis = theValues[v];//was: get(key);

      if  ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
            (keyValueFromThis != null && keyValueFromAFeatureMap == null)
          ) return false;

      if ((keyValueFromThis != null) && (keyValueFromAFeatureMap != null))
        if (!keyValueFromThis.equals(keyValueFromAFeatureMap)) return false;
    } // for

    return true;
  }//subsumes()

  /** Tests if <b>this</b> featureMap object includes aFeatureMap but only
    * for the those features present in the aFeatureNamesSet.
    * @param aFeatureMap which will be included or not in <b>this</b>
    * FeatureMap obj.If this param is null then it will return true.
    * @param aFeatureNamesSet is a set of strings representing the names of the
    * features that would be considered for subsumes. If aFeatureNamesSet is
    * <b>null</b> then subsumes(FeatureMap) will be called.
    * @return <code>true</code> if all features present in the aFeaturesNameSet
    * from aFeatureMap are included in <b>this</b> obj, or <code>false</code>
    * otherwise.
    */
  public boolean subsumes(FeatureMap aFeatureMap, Set aFeatureNamesSet){
    // This means that all features are taken into consideration.
    if (aFeatureNamesSet == null) return this.subsumes(aFeatureMap);
    // null is included in everything
    if (aFeatureMap == null) return true;
    // This means that subsumes is supressed.
    if (aFeatureNamesSet.isEmpty()) return true;

    SimpleFeatureMapImpl sfm = (SimpleFeatureMapImpl)aFeatureMap;

    Object key;
    Object keyValueFromAFeatureMap;
    Object keyValueFromThis;

    for (int i = 0; i < sfm.count; i++) {
      key = sfm.theKeys[i];

      if (!aFeatureNamesSet.contains(key))
        continue;

      keyValueFromAFeatureMap = sfm.theValues[i];
        keyValueFromThis = get(key);

      if  ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
            (keyValueFromThis != null && keyValueFromAFeatureMap == null)
          ) return false;

      if ((keyValueFromThis != null) && (keyValueFromAFeatureMap != null))
        if (!keyValueFromThis.equals(keyValueFromAFeatureMap)) return false;
    } // for

    return true;
  }// subsumes()


  /**
   * Overriden to fire events, so that the persistent objects
   *  can keep track of what's updated
   */
  public Object put(Object key, Object value) {
    Object result = super.put(key, value);
    this.fireMapUpdatedEvent();
    return result;
  } // put

  /**
   * Overriden to fire events, so that the persistent objects
   *  can keep track of what's updated
   */
  public Object remove(Object key) {
    Object result = super.remove(key);
    this.fireMapUpdatedEvent();
    return result;
  } // remove

  public void clear() {
    super.clear();
    //tell the world if they're listening
    this.fireMapUpdatedEvent();
  } // clear

  // Views
  public Object clone() {
    return super.clone();
  } //clone

  public boolean equals(Object o) {
    return super.equals(o);
  } // equals

//////////////////THE EVENT HANDLING CODE//////////////
//Needed so an annotation can listen to its features//
//and update correctly the database//////////////////
  private transient Vector mapListeners;
  /**
   * Removes a gate listener
   */
  public synchronized void removeFeatureMapListener(FeatureMapListener l) {
    if (mapListeners != null && mapListeners.contains(l)) {
      Vector v = (Vector) mapListeners.clone();
      v.removeElement(l);
      mapListeners = v;
    }
  } //removeFeatureMapListener
  /**
   * Adds a gate listener
   */
  public synchronized void addFeatureMapListener(FeatureMapListener l) {
    Vector v = mapListeners == null ? new Vector(2) : (Vector)mapListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      mapListeners = v;
    }
  } //addFeatureMapListener
  /**
   *
   * @param e
   */
  protected void fireMapUpdatedEvent () {
    if (mapListeners != null) {
      Vector listeners = mapListeners;
      int count = listeners.size();
      if (count == 0) return;
      for (int i = 0; i < count; i++)
        ((FeatureMapListener) listeners.elementAt(i)).featureMapUpdated();
    }
  }//fireMapUpdatedEvent
} // class SimpleFeatureMapImpl

