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
import com.ibm.uima.cas.Type;
import com.ibm.uima.cas.Feature;
import com.ibm.uima.cas.text.TCAS;
import gate.Document;
import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Factory;
import gate.util.InvalidOffsetException;
import com.ibm.uima.cas.FeatureStructure;
import org.jdom.Element;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An ObjectBuilder that creates a GATE annotation in the given annotation set.
 */
public class GateAnnotationBuilder implements ObjectBuilder {
  /**
   * The type of GATE annotation to create.
   */
  protected String annotationType;

  /**
   * The UIMA annotation type that this annotation is to be based on.
   */
  protected Type uimaType;

  /**
   * UIMA feature object for the begin feature of an annotation.
   */
  protected Feature uimaAnnotationBeginFeature;

  /**
   * UIMA feature object for the end feature of an annotation.
   */
  protected Feature uimaAnnotationEndFeature;

  /**
   * Feature definitions for the annotation.
   */
  protected List featureDefs;

  /**
   * Should this builder index the generated annotations, so that changes to
   * their features can be propagated back into GATE?  Default is false.
   */
  private boolean indexed;

  public boolean isIndexed() { return indexed; }

  /**
   * Returns the UIMA type that this annotation is to be based on.
   */
  public Type getUimaType() {
    return uimaType;
  }

  /**
   * Returns the GATE annotation type to be created.
   */
  public String getGateType() {
    return annotationType;
  }
  
  /**
   * Configure this ObjectBuilder by extracting the GATE and UIMA types and the
   * feature definitions from the XML element.
   */
  public void configure(Element elt, TypeSystem typeSystem)
        throws MappingException {
    annotationType = elt.getAttributeValue("type");
    if(annotationType == null) {
      throw new MappingException("No \"type\" attribute specified for "
          + "gateAnnotation");
    }

    String uimaTypeString = elt.getAttributeValue("uimaType");
    if(uimaTypeString == null) {
      throw new MappingException("No \"uimaType\" attribute specified for "
          + "gateAnnotation");
    }

    uimaType = typeSystem.getType(uimaTypeString);
    if(uimaType == null) {
      throw new MappingException("Type " + uimaTypeString
          + " not found in UIMA type system");
    }

    if(!typeSystem.subsumes(typeSystem.getType(TCAS.TYPE_NAME_ANNOTATION),
                            uimaType)) {
      throw new MappingException("Type " + uimaTypeString
          + " is not an annotation type");
    }

    uimaAnnotationBeginFeature = typeSystem.getFeatureByFullName(
         TCAS.FEATURE_FULL_NAME_BEGIN);
    if(uimaAnnotationBeginFeature == null) {
      throw new MappingException(TCAS.FEATURE_FULL_NAME_BEGIN + " feature not "
          + "found in type system - are you sure CAS is a TCAS?");
    }

    uimaAnnotationEndFeature = typeSystem.getFeatureByFullName(
         TCAS.FEATURE_FULL_NAME_END);
    if(uimaAnnotationEndFeature == null) {
      throw new MappingException(TCAS.FEATURE_FULL_NAME_END + " feature not "
          + "found in type system - are you sure CAS is a TCAS?");
    }

    String indexedString = elt.getAttributeValue("indexed");
    indexed = Boolean.valueOf(indexedString).booleanValue();

    // build the list of feature definitions
    List featureElements = elt.getChildren("feature");
    featureDefs = new ArrayList(featureElements.size());

    Iterator featureElementsIt = featureElements.iterator();
    while(featureElementsIt.hasNext()) {
      Element featureElt = (Element)featureElementsIt.next();
      String featureName = featureElt.getAttributeValue("name");
      if(featureName == null) {
        throw new MappingException("feature element must have \"name\" "
            + "attribute specified");
      }

      List children = featureElt.getChildren();
      if(children.isEmpty()) {
        throw new MappingException("feature element must have a child element "
            + "specifying its value");
      }
      Element valueElement = (Element)children.get(0);

      // create the object builder that gives this feature's value
      ObjectBuilder valueBuilder = ObjectManager.createBuilder(valueElement,
                                                               typeSystem);

      featureDefs.add(new FeatureDefinition(featureName, valueBuilder));
    }
  }

  /**
   * Create a GATE annotation in the given annotation set and return its ID.
   */
  public Object buildObject(TCAS cas, Document doc, AnnotationSet annSet,
      Annotation currentAnn, FeatureStructure currentFS)
          throws MappingException {
    //if(!(currentFS instanceof AnnotationFS)) {
    //  throw new MappingException("GATE annotations can only be created from "
    //      + "UIMA annotations, and not from arbitrary feature structures.");
    //}
    //AnnotationFS uimaAnnot = (AnnotationFS)currentFS;
    
    FeatureMap annotFeatures = Factory.newFeatureMap();

    applyFeatureDefs(annotFeatures, cas, doc, annSet, currentAnn, currentFS);

    int annotStart = currentFS.getIntValue(uimaAnnotationBeginFeature);
    int annotEnd = currentFS.getIntValue(uimaAnnotationEndFeature);

    // add returns the Integer ID
    try {
      return annSet.add(new Long(annotStart), new Long(annotEnd),
          annotationType, annotFeatures);
    }
    catch(InvalidOffsetException ioe) {
      throw new MappingException("Unexpected error creating annotation", ioe);
    }
  }

  /**
   * Updates the features of an existing annotation based on this builder's
   * list of feature definitions.
   */
  public void updateFeatures(TCAS cas, Document doc, AnnotationSet annSet,
      Annotation currentAnn, FeatureStructure currentFS)
          throws MappingException {
    //if(!(currentFS instanceof AnnotationFS)) {
    //  throw new MappingException("GATE annotations can only be created from "
    //      + "UIMA annotations, and not from arbitrary feature structures.");
    //}
    //AnnotationFS uimaAnnot = (AnnotationFS)currentFS;

    FeatureMap features = currentAnn.getFeatures();

    applyFeatureDefs(features, cas, doc, annSet, currentAnn, currentFS);
  }

  /**
   * Removes the current annotation from the annotation set.
   */
  public void removeAnnotation(TCAS cas, Document doc, AnnotationSet annSet,
      Annotation currentAnn, FeatureStructure currentFS)
          throws MappingException {
    annSet.remove(currentAnn);
  }

  /**
   * Use the set of feature definitions on this builder to update the given
   * feature map.  If the value builder for a given feature returns the value
   * <code>null</code> the corresponding feature will be removed from the map
   * if it is present.
   */
  protected void applyFeatureDefs(FeatureMap features, TCAS cas, Document doc,
      AnnotationSet annSet, Annotation currentGateAnn,
      FeatureStructure currentUimaAnn) throws MappingException {
    Iterator featuresIt = featureDefs.iterator();
    while(featuresIt.hasNext()) {
      FeatureDefinition def = (FeatureDefinition)featuresIt.next();
      
      // build the feature value
      ObjectBuilder valueBuilder = def.getValueBuilder();
      Object featureValue = valueBuilder.buildObject(cas, doc, annSet,
                                               currentGateAnn, currentUimaAnn);

      if(featureValue == null) {
        if(features.containsKey(def.getFeatureName())) {
          features.remove(def.getFeatureName());
        }
      }
      else {
        features.put(def.getFeatureName(), featureValue);
      }
    }
  }
}
