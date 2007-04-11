/**
 * 
 */
package gate.creole.ontology.ocat;

import gate.FeatureMap;
import gate.creole.ANNIEConstants;

/**
 * @author niraj
 */
public class Utils {

	/**
	 * Given an Annotation this method gets the value of
	 * gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME feature.
	 * 
	 * @param annot
	 * @return
	 */
	public static String getClassOrInstanceFeatureValue(gate.Annotation annot) {
		String ontoInstanceName = gate.creole.ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME;
		String ontoClassName = gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME;
		FeatureMap map = annot.getFeatures();

    String aName = (String)map.get(ontoClassName);
    String aValue = "";

    if(map.containsKey(ontoClassName)) {
      aName = (String)map.get(ontoClassName);
    }
    else if(map.containsKey(ontoInstanceName)) {
      aName = (String)map.get(ontoInstanceName);
    }
    System.out.println(aName);
    
    int index = aName.lastIndexOf("#");
    if(index < 0) index = aName.lastIndexOf(":");
    if(index < 0) index = aName.lastIndexOf("/");
    if(index >= 0) {
      aValue = aName.substring(index + 1, aName.length());
    }
    else {
      aValue = aName;
    }
    return aValue;
	} 
}
