package gate.impl.ag;

public class Node implements gate.Node{
  /** Default constructor. The -1 Id indicates a non-valid Id*/
  public Node() {
    id=gate.util.Tools.gensym("Node");
    offset=null;
  }//Node()

  /** Constructor
  @param id the Id of the new node
  @param offset the (temporal) offset of the Node; Should be <b>null</b> for not anchored nodes*/
  public Node(String id, Double offset){
    this.id=id;
    this.offset=offset;
  }//Node(int i)

  /**returns the Id of the Node */
  public String getId(){
    return id;
  }

  /** Offset (will be null when the node is not anchored)*/
  public Double getOffset(){
    return offset;
  }

  /** Does this node structurally precede n? */
  public boolean sPrecedes(gate.Node n){
    return false;
  }

  /** Does this node temporally (i.e. by offset) precede or is equal to n? */
  public boolean tPrecedes(gate.Node n){
    return (offset.compareTo(n.getOffset())<=0);
  }

  /** Does this node precede n? */
  public boolean precedes(gate.Node n){
    return sPrecedes(n)||tPrecedes(n);
  }

  public void addStartAnnotation(gate.Annotation annot){
    startAnnotations.put(annot.getId(),annot);
  }

  public void addEndAnnotation(gate.Annotation annot){
    endAnnotations.put(annot.getId(),annot);
  }

  private String id;
  private Double offset;
  private java.util.Hashtable startAnnotations=new java.util.Hashtable();
  private java.util.Hashtable endAnnotations=new java.util.Hashtable();
}