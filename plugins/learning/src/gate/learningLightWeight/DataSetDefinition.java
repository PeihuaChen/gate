/*
 * Created on 2005-5-25
 * DataSetDefinition.java 
 */
package gate.learningLightWeight;

import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Stores data describing a dataset. Same as existing
 * gate.ml.DataSetDefinition. Different from DSD's in ML frameworks
 * because we specify information about things in GATE (like relative
 * position,...)
 */
public class DataSetDefinition implements Serializable {
  protected java.util.List attributes;

  protected Attribute classAttribute = null;

  protected String instanceType;

  protected int classIndex;

  /** Store the Ngram. */
  protected java.util.List ngrams;

  public short dataType;

  public final static short ChunkLearningData = 1;

  public final static short ClassificationData = 2;

  public final static short RelationData = 3;

  /** The arrays and variables for fast computations. */
  ArraysDataSetDefinition arrs;

  // The variables for relation extraction

  /** The feature in instance for the first argument of relation. */
  String arg1Feat;

  /** The feature in instance for the second argument of relation. */
  String arg2Feat;

  /** The first argument of relation. */
  ArgOfRelation arg1 = null;

  /** The second argument of relation. */
  ArgOfRelation arg2 = null;

  protected java.util.List relAttributes;

  // protected AttributeRelation classRelAttribute = null;

  /** A DataSetDefinition is built using a XML file* */
  public DataSetDefinition(Element domElement) throws GateException {
    if(!domElement.getName().equals("DATASET"))
      throw new GateException("Dataset defintion element is \""
              + domElement.getName() + "\" instead of \"DATASET\"!");
    // find instance the type
    Element anElement = domElement.getChild("INSTANCE-TYPE");
    if(anElement != null)
      instanceType = anElement.getTextTrim();
    else throw new GateException(
            "Required element \"INSTANCE-TYPE\" not present!");
    // Check the dataset definition file is for relation extraction or
    // not
    anElement = domElement.getChild("INSTANCE-ARG1");
    if(anElement != null) { //
      dataType = this.RelationData;

      arg1Feat = anElement.getTextTrim();
      anElement = domElement.getChild("INSTANCE-ARG2");
      if(anElement != null)
        arg2Feat = anElement.getTextTrim();
      else throw new GateException(
              "Required element \"INSTANCE-ARG2\" not present!");
      // Get the features associated with arg1
      anElement = domElement.getChild("FEATURES-ARG1");
      if(anElement != null) {// Features for the first argument.
        arg1 = new ArgOfRelation();
        Element element1 = anElement.getChild("ARG");
        if(element1 != null) {
          arg1.type = element1.getChild("TYPE").getTextTrim();
          arg1.feat = element1.getChild("FEATURE").getTextTrim();
        }
        else throw new GateException(
                "Required element \"ARG\" in \"FEATURES-ARG1\" not present!");
        // Find the attribute features of the argument
        obtainArgumentFeatures(anElement, arg1);
        // Put the type and feat of data types into some arrays for fast
        // computation
        arg1.arrs = new ArraysDataSetDefinition();
        arg1.arrs.putTypeAndFeatIntoArray(arg1.attributes);
        arg1.arrs.numNgrams = arg1.ngrams.size();
      }
      // Get the features associated with arg2
      anElement = domElement.getChild("FEATURES-ARG2");
      if(anElement != null) {// Features for the first argument.
        arg2 = new ArgOfRelation();
        Element element1 = anElement.getChild("ARG");
        if(element1 != null) {
          arg2.type = element1.getChild("TYPE").getTextTrim();
          arg2.feat = element1.getChild("FEATURE").getTextTrim();
        }
        else throw new GateException(
                "Required element \"ARG\" in \"FEATURES-ARG1\" not present!");
        // Find the attribute features of the argument
        obtainArgumentFeatures(anElement, arg2);
        // Put the type and feat of data types into some arrays for fast
        // computation
        arg2.arrs = new ArraysDataSetDefinition();
        arg2.arrs.putTypeAndFeatIntoArray(arg2.attributes);
        arg2.arrs.numNgrams = arg2.ngrams.size();
      }

      // find the relation attributes
      int attrIndex = 0;
      relAttributes = new ArrayList();
      Iterator childrenIter = domElement.getChildren("ATTRIBUTE_REL")
              .iterator();
      while(childrenIter.hasNext()) {
        Element child = (Element)childrenIter.next();
        AttributeRelation relAttribute = new AttributeRelation(child);
        if(relAttribute.isClass()) {
          if(classAttribute != null)
            throw new GateException(
                    "RelAttribute \""
                            + relAttribute.getName()
                            + "\" marked as class attribute but the class is already known to be\""
                            + classAttribute.getName() + "\"!");
          classAttribute = relAttribute;
          classIndex = attrIndex;
        }
        relAttributes.add(relAttribute);
        attrIndex++;
      }
      arrs = new ArraysDataSetDefinition();
      arrs.putTypeAndFeatIntoArray(relAttributes);
      // get the args for the relation attribute terms
      arrs.obtainArgs(relAttributes);
    }
    else {// for other types of learning
      dataType = this.ChunkLearningData;

      // find the attributes
      int attrIndex = 0;

      attributes = new ArrayList();
      Iterator childrenIter = domElement.getChildren("ATTRIBUTE").iterator();
      while(childrenIter.hasNext()) {
        Element child = (Element)childrenIter.next();
        Attribute attribute = new Attribute(child);
        if(attribute.isClass()) {
          if(classAttribute != null)
            throw new GateException(
                    "Attribute \""
                            + attribute.getName()
                            + "\" marked as class attribute but the class is already known to be\""
                            + classAttribute.getName() + "\"!");
          classAttribute = attribute;
          classIndex = attrIndex;
        }
        attributes.add(attribute);
        attrIndex++;
      }

      Iterator childrenSerieIter = domElement.getChildren("ATTRIBUTELIST")
              .iterator();
      while(childrenSerieIter.hasNext()) {
        Element child = (Element)childrenSerieIter.next();
        List attributelist = Attribute.parseSerie(child);
        attributes.addAll(attributelist);
        attrIndex += attributelist.size();
      }

      if(classAttribute == null)
        throw new GateException("No class attribute defined!");

      // find the Ngrams
      ngrams = new ArrayList();
      childrenIter = domElement.getChildren("NGRAM").iterator();
      while(childrenIter.hasNext()) {
        Element child = (Element)childrenIter.next();
        Ngram ngram = new Ngram(child);
        ngrams.add(ngram);
      }

      arrs = new ArraysDataSetDefinition();
      arrs.putTypeAndFeatIntoArray(attributes);
      arrs.numNgrams = ngrams.size();
    }

    System.out.println("*** dataType=" + dataType + " classType="
            + arrs.classType + " classFeat=" + arrs.classFeature);

  }

  // Obtain the attributes for one argument
  private int obtainArgumentFeatures(Element domElement, ArgOfRelation argRel)
          throws GateException {
    int attrIndex = 0;

    argRel.attributes = new ArrayList();
    Iterator childrenIter = domElement.getChildren("ATTRIBUTE").iterator();
    while(childrenIter.hasNext()) {
      Element child = (Element)childrenIter.next();
      Attribute attribute = new Attribute(child);

      argRel.attributes.add(attribute);
      attrIndex++;
    }

    Iterator childrenSerieIter = domElement.getChildren("ATTRIBUTELIST")
            .iterator();
    while(childrenSerieIter.hasNext()) {
      Element child = (Element)childrenSerieIter.next();
      List attributelist = Attribute.parseSerie(child);
      argRel.attributes.addAll(attributelist);
      attrIndex += attributelist.size();
    }

    // find the Ngrams
    argRel.ngrams = new ArrayList();
    childrenIter = domElement.getChildren("NGRAM").iterator();
    while(childrenIter.hasNext()) {
      Element child = (Element)childrenIter.next();
      Ngram ngram = new Ngram(child);
      argRel.ngrams.add(ngram);
    }

    return attrIndex;
  }

  public DataSetDefinition() {
    attributes = new ArrayList();
    classAttribute = null;
    classIndex = -1;
    instanceType = null;

    ngrams = new ArrayList();
  }

  public String toString() {
    StringBuffer res = new StringBuffer();
    res.append("Instance type: " + instanceType + "\n");
    Iterator attrIter = attributes.iterator();
    while(attrIter.hasNext()) {
      res.append("Attribute:" + attrIter.next().toString() + "\n");
    }

    res.append("Ngrams\n");
    attrIter = ngrams.iterator();
    while(attrIter.hasNext()) {
      res.append("Ngram:" + attrIter.next().toString() + "\n");
    }

    return res.toString();
  }

  public java.util.List getAttributes() {
    return attributes;
  }

  public Attribute getClassAttribute() {
    return classAttribute;
  }

  public String getInstanceType() {
    return instanceType;
  }

  public int getClassIndex() {
    return classIndex;
  }

  public java.util.List getNgrams() {
    return ngrams;
  }

  public void setClassAttribute(Attribute classAttribute) {
    this.classAttribute = classAttribute;
  }

  public void setClassIndex(int classIndex) {
    this.classIndex = classIndex;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  /** Returns the DSD as XML * */
  public String toXML() {
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<ML-CONFIG>\n");
    sb.append("   <DATASET>\n");
    sb.append("    <!-- The type of annotation used as instance -->\n");
    sb.append("    <INSTANCE-TYPE>").append(this.instanceType).append(
            "</INSTANCE-TYPE>\n");
    for(int i = 0; i < this.attributes.size(); i++) {
      Attribute attrib = (Attribute)this.attributes.get(i);
      sb.append("\n");
      sb.append(attrib.toXML());
    }
    sb.append("   </DATASET>\n");
    sb.append("</ML-CONFIG>\n");
    return sb.toString();
  }

  public static DataSetDefinition load(File file) throws Exception {
    DataSetDefinition dsd = null;
    org.jdom.Document jdomDoc;
    SAXBuilder saxBuilder = new SAXBuilder(false);
    try {
      try {
        jdomDoc = saxBuilder.build(file);
      }
      catch(JDOMException jde) {
        throw new ResourceInstantiationException(jde);
      }
    }
    catch(java.io.IOException ex) {
      throw new ResourceInstantiationException(ex);
    }

    // go through the jdom document to extract the data we need
    Element rootElement = jdomDoc.getRootElement();
    if(!rootElement.getName().equals("ML-CONFIG"))
      throw new ResourceInstantiationException(
              "Root element of dataset defintion file is \""
                      + rootElement.getName() + "\" instead of \"ML-CONFIG\"!");
    // create the dataset defintion
    Element datasetElement = rootElement.getChild("DATASET");

    if(datasetElement == null)
      throw new ResourceInstantiationException(
              "No dataset definition provided in the configuration file!");
    try {
      dsd = new DataSetDefinition(datasetElement);
    }
    catch(GateException ge) {
      throw new ResourceInstantiationException(ge);
    }
    return dsd;
  }
}
