package gate.impl.ag;

public class Node implements gate.Node{
  /** Default constructor. The -1 Id indicates a non-valid Id*/
  public Node() {
    id=new Integer(-1);
    offset=null;
  }//Node()

  /** Contructor
  @param id the Id of the new node
  @param offset the (temporal) offset of the Node; Should be -1 for not anchored nodes*/
  public Node(int id, Double offset){
    this.id=new Integer(id);
    this.offset=offset;
  }//Node(int i)

  /**returns the Id of the Node */
  public Integer getId(){
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
    return (offset.compareTo(n.getId())<=0);
  }

  /** Does this node precede n? */
  public boolean precedes(gate.Node n){
    return sPrecedes(n)||tPrecedes(n);
  }
  private Integer id;
  private Double offset;
}