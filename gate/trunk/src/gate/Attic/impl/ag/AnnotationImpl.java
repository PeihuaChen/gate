/*AnnotationImpl.java
*@author Valentin Tablan
*/
package  gate.impl.ag;
import gate.*;
import gate.util.*;


/**Provides an implementation for the interface gate.Annotation
*/
public class AnnotationImpl
    implements gate.Annotation, FeatureBearer
{
    /**Constructor. Builds a new annotation.
      *@param id The id of the new annotation;
      *@param start The node from where the annotation will depart;
      *@param end The node where trhe annotation ends;
      *@param type The type of the new annotation;
      *@param equivalenceClass the equivalence class of the new annotation.
      */
    public AnnotationImpl (Long id, gate.Node start, gate.Node end,
                           String type, String equivalenceClass) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.type = type;
        this.eqClass = equivalenceClass;
    }

    /**Constructor. Builds a new annotation with an automatically assigned ID.
      *@param start The node from where the annotation will depart;
      *@param end The node where trhe annotation ends;
      *@param type The type of the new annotation;
      *@param equivalenceClass the equivalence class of the new annotation.
      */
    public AnnotationImpl (gate.Node start, gate.Node end,
                           String type, String equivalenceClass) {
        this(Tools.gensym(), start, end, type, equivalenceClass);
    }


    /** The type of the annotation (corresponds to TIPSTER "name"). */
    public String getType () {
        return  type;
    }


    /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
    public gate.FeatureMap getFeatures () {
        return  featureMap;
    }


    /** The equivalence class of this annotation. */
    public String getEquivalenceClass () {
        return  eqClass;
    }


    /** The start node. */
    public gate.Node getStartNode () {
        return  start;
    }


    /** The end node. */
    public gate.Node getEndNode () {
        return  end;
    }


    /** The stereotype associated with this annotation. */
    public gate.AnnotationStereotype getStereotype () {
        return  stereotype;
    }


    ;
    /**The id of the annotation.*/
    public Long getId () {
        return  id;
    }


    private String type = "";
    private gate.FeatureMap featureMap = null;
    private String eqClass = "";
    private gate.Node start, end;
    private gate.AnnotationStereotype stereotype = null;
    private Long id;

}

