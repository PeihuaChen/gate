package gate.util;

public class Restriction implements java.io.Serializable{

  /* Type of operator for cmarision in query*/
  public static final int OPERATOR_EQUATION = 100;
  public static final int OPERATOR_LESS = 101;
  public static final int OPERATOR_BIGGER = 102;
  public static final int OPERATOR_EQUATION_OR_BIGGER = 103;
  public static final int OPERATOR_EQUATION_OR_LESS = 104;

  private Object m_value;
  private String m_key;
  private int m_operator;

  public Restriction(String key, Object value, int _operator){
    m_key = key;
    m_value = value;
    m_operator = _operator;

  }

  public Object getValue(){
    return m_value;
  }

  public String getStringValue(){
    return m_value.toString();
  }

  public String getKey(){
    return m_key;
  }

  public int getOperator(){
    return m_operator;
  }
}