package gate.learningLightWeight;

import gate.util.GateException;

import org.jdom.Element;

/* 
 For the NGAM as features
 */
public class Ngram {

  private String name;

  private short number;

  private short consnum;

  private String[] typesGate = null;

  private String[] featuresGate = null;

  public Ngram(Element jdomElement) throws GateException {
    // find the name
    Element anElement = jdomElement.getChild("NAME");
    if(anElement == null)
      throw new GateException(
              "Required element \"NAME\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else name = anElement.getTextTrim();

    // find how many tokens (N) are used for the Ngram
    anElement = jdomElement.getChild("NUMBER");
    if(anElement == null)
      throw new GateException(
              "Required element \"NUMBER\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else number = (new Short(anElement.getTextTrim())).shortValue();

    // find how many constituents are used for the each token
    anElement = jdomElement.getChild("CONSNUM");
    if(anElement == null)
      throw new GateException(
              "Required element \"CONSNUM\" not present in attribute:\n"
                      + jdomElement.toString() + "!");
    else consnum = (new Short(anElement.getTextTrim())).shortValue();

    // allocate memory for the types and features for all the
    // constituents
    typesGate = new String[consnum];
    featuresGate = new String[consnum];

    for(int i = 0; i < consnum; ++i) {

      // find the type
      anElement = jdomElement.getChild("CONS-" + new Integer(i + 1));
      if(anElement == null)
        throw new GateException(
                "Required element \"TYPE\" not present in attribute:\n"
                        + jdomElement.toString() + "!");
      else {
        obtainTypeAndFeat(anElement, typesGate, featuresGate, i);

        // test to ensure that the types of the two or more annotations
        // are the same.
        // if(i>0 && !typesGate[i].equals(typesGate[i-1]))
        // throw new GateException(
        // "The annotation types in one Ngram should be the same: \n"
        // + jdomElement.toString() + "!");

      }
    }

  }

  private void obtainTypeAndFeat(Element anElement, String[] typesGate,
          String[] featuresGate, int i) throws GateException {
    Element lowerElement = anElement.getChild("TYPE");

    if(anElement != null)
      typesGate[i] = lowerElement.getTextTrim();
    else throw new GateException(
            "Required element \"TYPE\" not present in attribute:\n"
                    + anElement.toString() + "!");

    lowerElement = anElement.getChild("FEATURE");

    if(anElement != null)
      featuresGate[i] = lowerElement.getTextTrim();
    else throw new GateException(
            "Required element \"FEATURE\" not present in attribute:\n"
                    + anElement.toString() + "!");

  }

  public Ngram() {
    name = null;
    typesGate = null;
    featuresGate = null;
    number = 0;
    consnum = 0;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setNumber(short number) {
    this.number = number;
  }

  public short getNumber() {
    return number;
  }

  public void setConsnum(short consnum) {
    this.consnum = consnum;
  }

  public short getConsnum() {
    return consnum;
  }

  public void setTypesGate(String[] typesGate) {
    this.typesGate = typesGate;
  }

  public String[] getTypessGate() {
    return typesGate;
  }

  public String[] setFeaturesGate(String[] featuresGate) {
    return this.featuresGate = featuresGate;
  }

  public String[] getFeaturesGate() {
    return featuresGate;
  }

  public String toString() {
    StringBuffer res = new StringBuffer();
    res.append("Name: " + name + "\n");
    res.append("Number: " + this.number + "\n");
    res.append("Consnum: " + this.consnum + "\n");
    for(int i = 0; i < typesGate.length; ++i) {
      res.append("cons-" + new Integer(i + 1) + "\n");
      res.append("Types: " + typesGate[i] + "\n");
      res.append("Features: " + featuresGate[i] + "\n");
    }
    return res.toString();
  }

}
