package gate.impl.ag;
import java.net.URL;
import java.util.Map;
import gate.FeatureSet;
public abstract class Document implements gate.Document {

  public Document(String id){
    agSet=new java.util.Hashtable();
    this.id=id;
  }

  /** Documents are identified by URLs */
  public URL getUrl(){
    return url;
  };

  /** The annotation graphs for this document */
  public Map getAnnotationGraphs(){
    return agSet;
  };

  public gate.AnnotationGraph getAnnotationGraph(String id){
    return (gate.AnnotationGraph)agSet.get(id);
  }

  /** The features of this document */
  public FeatureSet getFeatureSet(){
    return featureSet;
  };

 /**Creates a new empty annotation graph associated with this document and returns it.*/
  public gate.AnnotationGraph newAnnotationGraph(String id){
    AnnotationGraph ag=new gate.impl.ag.AnnotationGraph(this,id);
    agSet.put(id,ag);
    return ag;
  }

  public double getLength(){
    return -1; // will be implemented properly in the inheriting classes
  }

  public String getId(){
    return id;
  }

  private java.net.URL url=null;
  /**The set of Annotation Graphs associated with this document*/
  private java.util.Hashtable agSet=null;
  /**The set of features for this document*/
  private FeatureSet featureSet=null;
  private String id="";
}