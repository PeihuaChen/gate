package gate.event;

import java.util.EventListener;

public interface GateListener extends EventListener {

  public void processGateEvent(GateEvent e);

}