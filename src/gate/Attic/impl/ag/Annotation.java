package gate.impl.ag;

public class Annotation implements gate.Annotation{

  public Annotation() {
  }

  public Annotation(String id,gate.Node start, gate.Node end, String type, String equivalenceClass){
    this.id=id;
    this.start=start;
    this.end=end;
    this.type=type;
    this.eqClass=equivalenceClass;
  }

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType(){
  return type;
  }

  /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
  public gate.FeatureSet getFeatures(){
    return featureSet;
  }

  /** The equivalence class of this annotation. */
  public String getEquivalenceClass(){
    return eqClass;
  }

  /** The start node. */
  public gate.Node getStartNode(){
    return start;
  }

  /** The end node. */
  public gate.Node getEndNode(){
    return end;
  }

  /** The stereotype associated with this annotation. */
  public gate.AnnotationStereotype getStereotype(){
    return stereotype;
  };

  public String getId(){
    return id;
  }
  
private String type="";
private gate.FeatureSet featureSet=null;
private String eqClass="";
private gate.Node start,end;
private gate.AnnotationStereotype stereotype=null;
private String id="";
} 