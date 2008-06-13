package gate.alignment;

import gate.Annotation;
import gate.Document;

public interface AlignmentListener {

  public void annotationsAligned(Annotation srcAnnotation, String srcAS, Document srcDocument,
          Annotation targetAnnotation, String tgtAS, Document targetDocument);
  
  public void annotationsUnaligned(Annotation srcAnnotation, String srcAS, Document srcDocument,
          Annotation targetAnnotation, String tgtAS, Document targetDocument);
}
