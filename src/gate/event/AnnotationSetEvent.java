package gate.event;

import java.util.EventObject;
import gate.*;

public class AnnotationSetEvent extends GateEvent{

  public static int ANNOTATION_ADDED = 1;
  public static int ANNOTATION_REMOVED = 2;

  public AnnotationSetEvent(AnnotationSet source,
                            int type,
                            Document sourceDocument,
                            Annotation annotation) {
    super(source);
    this.sourceDocument = sourceDocument;
    this.annotation = annotation;
    this.type = type;
  }

  public void setSourceDocument(gate.Document newSourceDocument) {
    sourceDocument = newSourceDocument;
  }
  public gate.Document getSourceDocument() {
    return sourceDocument;
  }
  public void setAnnotation(gate.Annotation newAnnotation) {
    annotation = newAnnotation;
  }
  public gate.Annotation getAnnotation() {
    return annotation;
  }

  private gate.Document sourceDocument;
  private gate.Annotation annotation;
}