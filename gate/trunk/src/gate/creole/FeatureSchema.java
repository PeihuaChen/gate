/**
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Cristian URSU, 27/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;

import gate.util.*;

/**
  * This class describes a schema for a feature. It is used as part of
  * @see gate.creole.AnnotationSchema class.
  */
public class FeatureSchema {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** The name of this feature. */
  String featureName = null;

  /** The class name of the feature value*/
  String featureValueClassName = null;

  Object featureDefaultValue = null;
  String featureUse = null;

  /** The default or fixed value for that feature*/

  /** Permisible value set, if appropriate. */
  Set featurePermissibleValuesSet = null;

  /** Construction given a name of an feature and a feature value class name
   */
  public FeatureSchema( String aFeatureName,
                        String aFeatureValueClassName,
                        String aFeatureDefaultValue,
                        String aFeatureUse,
                        Set    aFeaturePermissibleValuesSet ){

    featureName                 = aFeatureName;
    featureValueClassName       = aFeatureValueClassName;
    featureDefaultValue         = aFeatureDefaultValue;
    featureUse                  = aFeatureUse;
    featurePermissibleValuesSet = aFeaturePermissibleValuesSet;
  }

  /** Whether the values are an enumeration or not. */
  public boolean isEnumeration() {
    return featurePermissibleValuesSet != null;
  }

  /** Get the feature name */
  public String getFeatureName() {
    return featureName;
  }


  /** Get the feature value class name */
  public String getValueClassName() {
    return featureValueClassName;
  }

  /** Returns the permissible values as a Set*/
  public Set getPermissibleValues() {
    return featurePermissibleValuesSet;
  }


  /** Adds all values from the given set as permissible values for
    * the given feature. No check is performed to see if the
    * class name of the feature value is the same as the
    * the elements of the given set. Returns true if the set has been assigned.
    */
  public boolean setPermissibleValues(Set aPermisibleValuesSet) {
    featurePermissibleValuesSet.clear();
    return featurePermissibleValuesSet.addAll(aPermisibleValuesSet);
  }

 /** Adds a value to the enumeration of permissible value for an
    * feature of this type. Returns false, i.e. fails, if the
    * class name of the feature value does not match the class name
    * of the given object
    */
  public boolean addPermissibleValue(Object obj) {
    if (! obj.getClass().getName().equals(featureValueClassName))
        return false;
    if (featurePermissibleValuesSet == null)
        featurePermissibleValuesSet = new HashSet();
    return featurePermissibleValuesSet.add(obj);
  }

  /**
    * This method transforms a feature to its XSchema representation
    */
  public String toXSchema(){
    StringBuffer schemaString = new StringBuffer();
    schemaString.append("<attribute name=\"" + featureName + "\" ");
    schemaString.append("use=\"" + featureUse + "\"");
    // If there are no permissible values that means that the type must
    // be specified as an attribute for the attribute element
    if (featurePermissibleValuesSet == null)
      schemaString.append(" type=\"" + featureValueClassName + "\"/>\n");
    else {
      schemaString.append(">\n <simpleType>\n");
      schemaString.append("  <restriction base=\"" + featureValueClassName +
                                                                     "\">\n");
      Iterator featurePermissibleValuesSetIterator =
                               featurePermissibleValuesSet.iterator();
      while (featurePermissibleValuesSetIterator.hasNext()){
        String featurePermissibleValue =
                    (String) featurePermissibleValuesSetIterator.next();
        schemaString.append("   <enumeration value=\"" +
                            featurePermissibleValue + "\"/>\n");
      }// end while
      schemaString.append("  </restriction>\n");
      schemaString.append(" </simpleType>\n");
      schemaString.append("</attribute>\n");
    }// end if else
    return schemaString.toString();
  }// end toXSchema
}//FeatureSchema
