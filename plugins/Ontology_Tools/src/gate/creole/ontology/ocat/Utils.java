/**
 * 
 */
package gate.creole.ontology.ocat;

import gate.FeatureMap;

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
	public static String getClassFeatureValue(gate.Annotation annot) {
		String ontoFeatureName = gate.creole.ANNIEConstants.LOOKUP_ONTOLOGY_FEATURE_NAME;
		String ontoClassName = gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME;
		FeatureMap map = annot.getFeatures();
		String classValue = null;
		if (map.containsKey(ontoClassName)) {
			String className = (String) map.get(ontoClassName);
			if (map.containsKey(ontoFeatureName)) {
				classValue = className;
			} else {
				int index = className.lastIndexOf("#");
				if (index < 0)
					index = className.lastIndexOf(":");
				if (index < 0)
					index = className.lastIndexOf("/");
				if (index >= 0) {
					classValue = className.substring(index + 1, className
							.length());
				} else {
					classValue = className;
				}
			}
		}
		return classValue;
	}
}
