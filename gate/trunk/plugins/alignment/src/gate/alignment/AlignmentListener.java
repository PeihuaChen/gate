package gate.alignment;

import gate.Annotation;
import gate.Document;

public interface AlignmentListener {

  public void annotationsAligned(Annotation srcAnnotation, Document srcDocument,
          Annotation targetAnnotation, Document targetDocument);
  
  public void annotationsUnaligned(Annotation srcAnnotation, Document srcDocument,
          Annotation targetAnnotation, Document targetDocument);
}
