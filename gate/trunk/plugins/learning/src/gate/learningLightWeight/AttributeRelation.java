package gate.learningLightWeight;

import gate.util.GateException;
import java.util.ArrayList;
import org.jdom.Element;

public class AttributeRelation extends Attribute {
  // These constants are used only for returning values from
  // semanticType

  private String arg1;

  private String arg2;

  public AttributeRelation(Element jdomElement) throws GateException {
    // find the name
    Element anElement = jdomElement.getChild("NAME");
    if(anElement == null)
      throw new GateException(
              "Required element \"NAME\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else name = anElement.getTextTrim();
    // find the semantic type
    anElement = jdomElement.getChild("SEMTYPE");
    if(anElement == null)
      throw new GateException(
              "Required element \"SEMTYPE\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else {
      if(anElement.getTextTrim().equalsIgnoreCase("NOMINAL"))
        this.semantic_type = Attribute.NOMINAL;
      else if(anElement.getTextTrim().equalsIgnoreCase("NUMERIC"))
        this.semantic_type = Attribute.NUMERIC;
      else if(anElement.getTextTrim().equalsIgnoreCase("BOOLEAN"))
        this.semantic_type = Attribute.BOOLEAN;
    }
    // find the type
    anElement = jdomElement.getChild("TYPE");
    if(anElement == null)
      throw new GateException(
              "Required element \"TYPE\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else type = anElement.getTextTrim();
    // find the feature if present
    anElement = jdomElement.getChild("FEATURE");
    if(anElement != null) feature = anElement.getTextTrim();
    // find the arg1 if present
    anElement = jdomElement.getChild("ARG1");
    if(anElement != null) arg1 = anElement.getTextTrim();
    // find the arg2 if present
    anElement = jdomElement.getChild("ARG2");
    if(anElement != null) arg2 = anElement.getTextTrim();
    // find the position if present
    anElement = jdomElement.getChild("POSITION");
    if(anElement == null)
      position = 0;
    else position = Integer.parseInt(anElement.getTextTrim());
    // find the weighting if present
    anElement = jdomElement.getChild("WEIGHTING");
    if(anElement == null)
      weighting = 1.0;
    else weighting = Double.parseDouble(anElement.getTextTrim());
    // find the class if present
    // confidence_feature
    anElement = jdomElement.getChild("CLASS");
    isClass = anElement != null;
  }

  public AttributeRelation() {
    name = null;
    type = null;
    feature = null;
    arg1 = null;
    arg2 = null;
    isClass = false;
    position = 0;
    weighting = 1.0;
    confidence_feature = null;
  }

  public String toString() {
    StringBuffer res = new StringBuffer();
    res.append("Name: " + name + "\n");
    res.append("SemType: " + this.semantic_type + "\n");
    res.append("Type: " + type + "\n");
    res.append("Feature: " + feature + "\n");
    res.append("Arg1: " + arg1 + "\n");
    res.append("Arg2: " + arg2 + "\n");
    res.append("Weighting: " + weighting + "\n");
    if(isClass) res.append("Class");
    return res.toString();
  }

  public void setArg1(String arg) {
    this.arg1 = arg;
  }

  public String getArg1() {
    return arg1;
  }

  public void setArg2(String arg) {
    this.arg2 = arg;
  }

  public String getArg2() {
    return arg2;
  }

  /**
   * This method reports whether the attribute is nominal, numeric or
   * boolean.
   * 
   * @return Attribute.NOMINAL, Attribute.NUMERIC or Attribute.BOOLEAN
   */

  public String toXML() {
    StringBuffer sb = new StringBuffer();
    sb.append("     ").append("<ATTRIBUTE>\n");
    sb.append("      ").append("<NAME>").append(this.name).append("</NAME>\n");
    sb.append("      ").append("<SEMTYPE>");
    if(this.semantic_type == Attribute.NOMINAL)
      sb.append("NOMINAL");
    else if(this.semantic_type == Attribute.BOOLEAN)
      sb.append("BOOLEAN");
    else sb.append("NUMERIC");
    sb.append("</SEMTYPE>\n");
    sb.append("      ").append("<TYPE>").append(this.type).append("</TYPE>\n");
    if(feature != null) {
      sb.append("      ").append("<FEATURE>").append(this.feature).append(
              "</FEATURE>\n");
    }
    if(arg1 != null) {
      sb.append("      ").append("<ARG1>").append(this.arg1)
              .append("</ARG1>\n");
    }
    if(arg2 != null) {
      sb.append("      ").append("<ARG2>").append(this.arg2)
              .append("</ARG2>\n");
    }
    sb.append("      ").append("<POSITION>").append(this.position).append(
            "</POSITION>\n");
    if(isClass) sb.append("      ").append("<CLASS/>\n");
    sb.append("     ").append("</ATTRIBUTE>\n");
    return sb.toString();
  }

  /** * */
  public static java.util.List parseSerie(Element jdomElement)
          throws GateException {
    // find the name
    Element anElement = jdomElement.getChild("NAME");
    if(anElement == null)
      throw new GateException(
              "Required element \"NAME\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    String name = anElement.getTextTrim();
    // find the semantic type
    anElement = jdomElement.getChild("SEMTYPE");
    if(anElement == null)
      throw new GateException(
              "Required element \"SEMTYPE\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    int semantic_type = Attribute.NOMINAL;
    if(anElement.getTextTrim().equalsIgnoreCase("NUMERIC"))
      semantic_type = Attribute.NUMERIC;
    else if(anElement.getTextTrim().equalsIgnoreCase("BOOLEAN"))
      semantic_type = Attribute.BOOLEAN;
    // find the type
    anElement = jdomElement.getChild("TYPE");
    if(anElement == null)
      throw new GateException(
              "Required element \"TYPE\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    String type = anElement.getTextTrim();
    String feature = null;
    // find the feature if present
    anElement = jdomElement.getChild("FEATURE");
    if(anElement != null) feature = anElement.getTextTrim();
    int minpos = 0;
    int maxpos = 0;
    // find the range of this element (e.g. from - to)
    anElement = jdomElement.getChild("RANGE");
    try {
      minpos = Integer.parseInt(anElement.getAttributeValue("from").trim());
      maxpos = Integer.parseInt(anElement.getAttributeValue("to").trim());
    }
    catch(Exception e) {
      throw new GateException("Range element is uncorrect:\n"
              + jdomElement.toString() + "!");
    }
    double weighting = 1.0;
    // find the weighting if present
    anElement = jdomElement.getChild("WEIGHTING");
    if(anElement != null)
      weighting = Double.parseDouble(anElement.getTextTrim());
    // find the class if present
    boolean isClass = jdomElement.getChild("CLASS") != null;
    if(isClass) {
      throw new GateException("Cannot define the class in a serie:\n"
              + jdomElement.toString() + "!");
    }
    // Create a list of Attributes
    ArrayList attributes = new ArrayList();
    for(int position = minpos; position < maxpos + 1; position++) {
      AttributeRelation attribute = new AttributeRelation();
      attribute.setClass(false);
      attribute.setFeature(feature);
      // attribute.setArg1(arg1);
      // attribute.setArg2(arg2);
      attribute.setName(name);
      attribute.setPosition(position);
      attribute.setSemanticType(semantic_type);
      attribute.setType(type);
      attribute.setWeighting(weighting);
      attributes.add(attribute);
    }
    return attributes;
  }
}
