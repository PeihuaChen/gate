package gate.event;

import java.util.EventListener;

public interface AnnotationSetListener extends GateListener {

  public void annotationAdded(AnnotationSetEvent e);

  public void annotationRemoved(AnnotationSetEvent e);


}