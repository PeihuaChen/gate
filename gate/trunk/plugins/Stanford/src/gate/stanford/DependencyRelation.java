package gate.stanford;

/**
 * Simple class representing a single dependency relation.
 */
public class DependencyRelation {
  /**
   * The type of the dependency relation (det, amod, etc.).
   */
  private String type;
  
  /**
   * The ID of the token that is the target of this relation.
   */
  private Integer targetId;
  
  public DependencyRelation(String type, Integer targetId) {
    this.type = type;
    this.targetId = targetId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getTargetId() {
    return targetId;
  }

  public void setTargetId(Integer targetId) {
    this.targetId = targetId;
  }
  
  public String toString() {
    return type + "(" + targetId + ")";
  }
}
