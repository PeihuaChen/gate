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
  /** Get the feature set */
  public FeatureMap getFeatures();

  /** Set the feature set */
  public void setFeatures(FeatureMap features);

} // interface FeatureBearer
