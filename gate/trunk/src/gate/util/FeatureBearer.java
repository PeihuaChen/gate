/*
 *	FeatureBearer.java
 *
 *	Hamish Cunningham, 7/Feb/2000
 *
 *	$Id$
 */

package gate.util;
import java.util.*;
import gate.*;

/** Classes that have features.
  */
public interface FeatureBearer 
{
  /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
  public FeatureMap getFeatures();

} // interface FeatureBearer
