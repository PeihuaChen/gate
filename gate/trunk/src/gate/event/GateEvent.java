package gate.event;

import java.util.EventObject;

public class GateEvent extends EventObject {

  public GateEvent(Object source) {
    super(source);
  }
  protected int type;
  public int getType() {
    return type;
  }
  public void setType(int newType) {
    type = newType;
  }
}