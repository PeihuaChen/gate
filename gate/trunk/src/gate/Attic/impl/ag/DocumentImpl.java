/*DocumentImpl.java
*@author Valentin Tablan
*24.01.2000
*/
package  gate.impl.ag;




import  java.net.URL;
import  java.util.Map;
import gate.*;
import gate.util.*;



/**Provides a partial implementation for gate.Document
*This class is abstract and only provides implementation for some of the methods
*of the interface gate.Document.
*/
public abstract class DocumentImpl
    implements Document, FeatureBearer
{
    /*Constructor. Build a new document with an automatically assigned ID.
    */
    public DocumentImpl () {
        this(Tools.gensym());
    }
    /**Constructor. Builds a new document with the given ID.
      *@param id the ID for the new document.
      */
    public DocumentImpl (Long id) {
        agSet = new java.util.Hashtable();
        this.id = id;
    }


    /** Documents are identified by URLs */
    public URL getUrl () {
        return  url;
    }


    /** The annotation graphs for this document */
    public Map getAnnotationGraphs () {
        return  agSet;
    }

    public gate.AnnotationGraph getAnnotationGraph (Long id) {
        return  (AnnotationGraph)agSet.get(id);
    }


    /** The features of this document */
    public FeatureMap getFeatures () {
        return  featureMap;
    }


    /**Creates a new empty annotation graph associated with this document and
    *returns it.
    *@param id the id for the new annotation graph.
    */
    public gate.AnnotationGraph newAnnotationGraph (Long id) {
        AnnotationGraphImpl ag = new gate.impl.ag.AnnotationGraphImpl(this, id);
        agSet.put(id, ag);
        return  ag;
    }

    /**Creates a new empty annotation graph associated with this document and
    *returns it.
    *The id for the new annotation graph will be a random unique Long.
    */
    public gate.AnnotationGraph newAnnotationGraph () {
        AnnotationGraphImpl ag = new gate.impl.ag.AnnotationGraphImpl(this);
        agSet.put(id, ag);
        return  ag;
    }


    /**The id of the document.
    */
    public Long getId () {
        return  id;
    }


    private java.net.URL url = null;
    /**The set of Annotation Graphs associated with this document*/
    private java.util.Hashtable agSet = null;
    /**The set of features for this document*/
    private FeatureMap featureMap = null;
    private Long id;

}

