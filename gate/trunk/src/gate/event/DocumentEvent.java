package gate.event;

import gate.*;

import java.util.EventObject;

public class DocumentEvent extends GateEvent {

  public static int ANNOTATION_SET_ADDED = 1;
  public static int ANNOTATION_SET_REMOVED = 2;
  public DocumentEvent(Document source, int type, String setName) {
    super(source);
    this.type = type;
    this.annotationSetName = setName;
  }

  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }

  public String getAnnotationSetName() {
    return annotationSetName;
  }

  private String annotationSetName;

}