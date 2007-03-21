package gate.learningLightWeight;

import java.util.List;

public class ArraysDataSetDefinition {
  String[] typesInDataSetDef;

  String[] featuresInDataSetDef;

  String[] namesInDataSetDef;

  String[] arg1s;

  String[] arg2s;

  int[] featurePosition;

  int numTypes = 0;

  int numNgrams = 0;

  String classType;

  String classFeature;

  String classArg1;

  String classArg2;

  int maxNegPosition = 0;

  int maxPosPosition = 0;

  void putTypeAndFeatIntoArray(List attrs) {

    numTypes = obtainNumberOfNLPTypes(attrs);

    typesInDataSetDef = new String[numTypes];
    featuresInDataSetDef = new String[numTypes];
    namesInDataSetDef = new String[numTypes];
    featurePosition = new int[numTypes];

    // added allFeat;
    obtainGATETypesAndFeatures(attrs);

    // System.out.println("name0="+namesInDataSetDef[0]);

    for(int i = 0; i < numTypes; ++i) {
      if(featurePosition[i] < maxNegPosition)
        maxNegPosition = featurePosition[i];
      if(featurePosition[i] > maxPosPosition)
        maxPosPosition = featurePosition[i];
    }
    maxNegPosition = -maxNegPosition;
  }

  static int obtainNumberOfNLPTypes(List attrs) {
    int num = 0;
    if(attrs == null) {
      return num;
    }
    else {
      for(int i = 0; i < attrs.size(); i++) {
        if(!((Attribute)attrs.get(i)).isClass()) num++;
      }
      return num;
    }
  }

  void obtainGATETypesAndFeatures(List attrs) {
    int num0 = 0;
    for(int i = 0; i < attrs.size(); i++) {
      Attribute attr = (Attribute)attrs.get(i);
      if(!attr.isClass()) {
        typesInDataSetDef[num0] = attr.getType();
        featuresInDataSetDef[num0] = attr.getFeature();
        namesInDataSetDef[num0] = attr.getName();
        featurePosition[num0] = attr.getPosition();
        // System.out.println(new Integer(num0+1) + " " +
        // namesInDataSetDef[num0] + " "
        // + typesInDataSetDef[num0] + " " +
        // featuresInDataSetDef[num0]);
        ++num0;
      }
      else {
        classType = attr.getType();
        classFeature = attr.getFeature();
      }
    }
  }

  void obtainArgs(List relAttrs) {
    int num0 = 0;
    arg1s = new String[numTypes];
    arg2s = new String[numTypes];
    for(int i = 0; i < relAttrs.size(); i++) {
      AttributeRelation attr = (AttributeRelation)relAttrs.get(i);
      if(!attr.isClass()) {
        arg1s[num0] = attr.getArg1();
        arg2s[num0] = attr.getArg2();
        ++num0;
      }
      else {
        classArg1 = attr.getArg1();
        classArg2 = attr.getArg2();
      }
    }
  }

}
