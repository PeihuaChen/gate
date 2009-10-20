/*
 *  Copyright (c) 2005, The University of Sheffield.
 *
 *  This file is part of the GATE/UIMA integration layer, and is free
 *  software, released under the terms of the GNU Lesser General Public
 *  Licence, version 2.1 (or any later version).  A copy of this licence
 *  is provided in the file LICENCE in the distribution.
 *
 *  UIMA is a product of IBM, details are available from
 *  http://alphaworks.ibm.com/tech/uima
 */
package gate.uima.mapping;

import com.ibm.uima.cas.TypeSystem;
import com.ibm.uima.cas.text.TCAS;
import gate.Document;
import gate.Annotation;
import gate.AnnotationSet;
import com.ibm.uima.cas.FeatureStructure;
import org.jdom.Element;

/**
 * An ObjectBuilder that creates a fixed String given by the "value" attribute
 * of its configuration element.
 */
public class GateDocumentFeatureValueBuilder implements ObjectBuilder {
  /**
   * The String value to be returned by buildObject.
   */
  private String featureName;
  
  /**
   * Configure this ObjectBuilder by extracting the feature name from the
   * "name" attribute.  If no such attribute exists, and exception is thrown.
   */
  public void configure(Element elt, TypeSystem typeSystem)
                      throws MappingException {
    this.featureName = elt.getAttributeValue("name");
    if(featureName == null) {
      throw new MappingException("docFeatureValue element must have "
          + "name attribute");
    }
  }

  /**
   * Returns the value of the specified feature of the document.  If the
   * document has no feature by that name, <code>null</code> is returned.
   */
  public Object buildObject(TCAS cas, Document doc, AnnotationSet annSet,
      Annotation currentAnn, FeatureStructure currentFS) {
    return doc.getFeatures().get(featureName);
  }
}
