package gate.util;

public class OrderByRestriction implements java.io.Serializable{

  /* Type of operator for cmarision in query*/
  public static final int OPERATOR_ASCENDING = 100;
  public static final int OPERATOR_DESCENDING = 101;

  private String key;
  private int operator_;

  /** --- */
  public OrderByRestriction(String key,  int operator_){
    this.key = key;
    this.operator_ = operator_;
  }

  /** --- */
  public String getKey(){
    return key;
  }

  /** --- */
  public int getOperator(){
    return operator_;
  }
}