package gate.event;

import java.util.EventListener;

public interface DocumentListener extends GateListener {
  public void annotationSetAdded(DocumentEvent e);
  public void annotationSetRemoved(DocumentEvent e);
}