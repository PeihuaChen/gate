/*NodeImpl.java
*@author Valentin Tablan
*24.01.2000
*/

package  gate.impl.ag;

import gate.*;
import gate.util.*;

/**Provides an implementation for the interface gate.Node.
*/
public class NodeImpl
    implements gate.Node
{

    /** Default constructor. Creates a new node with a random unique Id.*/
    public NodeImpl () {
        id = Tools.gensym();
        offset = null;
    } //Node()


    /** Constructor
    *@param id the Id of the new node
    *@param offset the (temporal) offset of the Node; Should be <b>null</b>
    *for not anchored nodes.
    */
    public NodeImpl (Long id, Long offset) {
        this.id = id;
        this.offset = offset;
    } //Node(int i)

    /**Constructor.
    @param offset The offset for the new node.
    *The id will be assigned automatically as a random, unique Long.
    */
    public NodeImpl(Long offset){
      this(Tools.gensym(),offset);
    }


    /**Returns the Id of the Node. */
    public Long getId () {
        return  id;
    }


    /** Offset (will be null when the node is not anchored)*/
    public Long getOffset () {
        return  offset;
    }


    /** Does this node structurally precede n? */
    public boolean sPrecedes (gate.Node n) {
        return  false;
    }


    /** Does this node temporally (i.e. by offset) precede or is equal to n? */
    public boolean tPrecedes (gate.Node n) {
        return  (offset.compareTo(n.getOffset()) <= 0);
    }


    /** Does this node precede n? */
    public boolean precedes (gate.Node n) {
        return  sPrecedes(n) || tPrecedes(n);
    }

    /**Adds an annotation to the list of annotations departing from this node.
    *@param annot The annotation to be added.
    */
    public void addStartAnnotation (gate.Annotation annot) {
        startAnnotations.put(annot.getId(), annot);
    }

    /**Adds an annotation to the list of annotations arriving in this node.
    *@param annot The annotation to be added.
    */
    public void addEndAnnotation (gate.Annotation annot) {
        endAnnotations.put(annot.getId(), annot);
    }


    private Long id;
    private Long offset;
    private java.util.Hashtable startAnnotations = new java.util.Hashtable();
    private java.util.Hashtable endAnnotations = new java.util.Hashtable();

}

