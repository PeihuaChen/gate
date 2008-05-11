package gate.alignment;

import gate.Annotation;
import gate.Document;
import gate.compound.CompoundDocument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class stores all the alignment information about a document. It provides
 * various methods to know which annotation is aligned with which annotations
 * and what is the source document of each annotation.
 * 
 * @author niraj
 */
public class Alignment implements Serializable {

	private static final long serialVersionUID = 3977299936398488370L;

	public static int counter = 0;
	
	/**
	 * a map that stores information about annotation alignment. As a key, a
	 * source annotation and as value, a set of aligned annotations to the
	 * source annotation are stored.
	 */
	protected Map<Annotation, Set<Annotation>> alignmentMatrix;

	/**
	 * For each annotation we store the information about its document. This is
	 * used for letting the user know which document the given annotation
	 * belongs to.
	 */
	protected Map<Annotation, String> annotation2Document;

	protected transient List<AlignmentListener> listeners = new ArrayList<AlignmentListener>();
	protected transient CompoundDocument compoundDocument;
	
  /**
	 * Constructor
	 */
	public Alignment(CompoundDocument compoundDocument) {
		this.compoundDocument = compoundDocument;
    alignmentMatrix = new HashMap<Annotation, Set<Annotation>>();
		annotation2Document = new HashMap<Annotation, String>();
		counter++;
	}

  public void setSourceDocument(CompoundDocument cd) {
    this.compoundDocument = cd;
  }
  
	/**
	 * Returns if two annotations are aligned with each other.
	 * 
	 * @param srcAnnotation
	 * @param targetAnnotation
	 * @return
	 */
	public boolean areTheyAligned(Annotation srcAnnotation,
			Annotation targetAnnotation) {
		Set<Annotation> alignedTo = alignmentMatrix.get(srcAnnotation);
		if (alignedTo == null)
			return false;
		else
			return alignedTo.contains(targetAnnotation);
	}

	/**
	 * Aligns the given source annotation with the given target annotation.
	 * 
	 * @param srcAnnotation
	 * @param srcDocument
	 * @param targetAnnotation
	 * @param targetDocument
	 */
	public void align(Annotation srcAnnotation, Document srcDocument,
			Annotation targetAnnotation, Document targetDocument) {

	  
	  if(srcAnnotation == null || targetAnnotation == null) return;
    if(areTheyAligned(srcAnnotation, targetAnnotation)) return;
    
    Set<Annotation> alignedToT = alignmentMatrix.get(srcAnnotation);
    if (alignedToT == null) {
      alignedToT = new HashSet<Annotation>();
      alignmentMatrix.put(srcAnnotation, alignedToT);
    }
    Set<Annotation> alignedToS = alignmentMatrix.get(targetAnnotation);
    if (alignedToS == null) {
      alignedToS = new HashSet<Annotation>();
      alignmentMatrix.put(targetAnnotation, alignedToS);
    }
    
    alignedToT.add(targetAnnotation);
    annotation2Document.put(srcAnnotation, srcDocument.getName());

    alignedToS.add(srcAnnotation);
    annotation2Document.put(targetAnnotation, targetDocument.getName());
    fireAnnotationsAligned(srcAnnotation, srcDocument, targetAnnotation, targetDocument);
	}

  /**
   * Aligns the given source annotation with the given target annotation.
   * 
   * @param srcAnnotation
   * @param srcDocument
   * @param targetAnnotation
   * @param targetDocument
   */
  public void unalign(Annotation srcAnnotation, Document srcDocument,
      Annotation targetAnnotation, Document targetDocument) {

    if(srcAnnotation == null || targetAnnotation == null) return;
    if(!areTheyAligned(srcAnnotation, targetAnnotation)) return;
    
    Set<Annotation> alignedToT = alignmentMatrix.get(srcAnnotation);
    Set<Annotation> alignedToS = alignmentMatrix.get(targetAnnotation);

    if (alignedToT != null) {
      alignedToT.remove(targetAnnotation);
      if(alignedToT.isEmpty()) {
        alignmentMatrix.remove(srcAnnotation);
        annotation2Document.remove(srcAnnotation);
      } else {
        alignmentMatrix.put(srcAnnotation, alignedToT);
      }
    }

    if (alignedToS != null) {
      alignedToS.remove(srcAnnotation);
      if(alignedToS.isEmpty()) {
        alignmentMatrix.remove(targetAnnotation);
        annotation2Document.remove(targetAnnotation);
      } else {
        alignmentMatrix.put(targetAnnotation, alignedToS);
      }
    }
    fireAnnotationsUnAligned(srcAnnotation, srcDocument, targetAnnotation, targetDocument);
  }
	
	
	/**
	 * Returns a set of aligned annotations.
	 * 
	 * @return
	 */
	public Set<Annotation> getAlignedAnnotations() {
		Set<Annotation> annots = alignmentMatrix.keySet();
		if(annots == null) return null;
		else {
      return new HashSet<Annotation>(annots);
    }
	}

	/**
	 * This method tells which document the given annotation belongs to.
	 * 
	 * @param annotation
	 * @return
	 */
	public Document getDocument(Annotation annotation) {
		return compoundDocument.getDocument(annotation2Document.get(annotation));
	}

	/**
	 * Given the annotation, this method returns a set of the aligned
	 * annotations to that annotation.
	 * 
	 * @param srcAnnotation
	 * @return
	 */
	public Set<Annotation> getAlignedAnnotations(Annotation srcAnnotation) {
		Set<Annotation> annots = alignmentMatrix.get(srcAnnotation);
		if(annots != null) return new HashSet<Annotation>(annots);
		else return null;
	}

	/**
	 * This method tells whether the given annotation is aligned or not.
	 * 
	 * @param srcAnnotation
	 * @return
	 */
	public boolean isAnnotationAligned(Annotation srcAnnotation) {
		Set<Annotation> alignedTo = alignmentMatrix.get(srcAnnotation);
		if (alignedTo == null)
			return false;
		else {
			return !alignedTo.isEmpty();
		}
	}
	
	public void addAlignmentListener(AlignmentListener listener) {
	  if(this.listeners == null) {
	   this.listeners = new ArrayList<AlignmentListener>(); 
	  }
	  if(listener != null) this.listeners.add(listener);
	}
	
	public void removeAlignmentListener(AlignmentListener listener) {
    if(this.listeners == null) {
       this.listeners = new ArrayList<AlignmentListener>(); 
      }

    if(listener !=null) this.listeners.remove(listener);
	}
	
	protected void fireAnnotationsAligned(Annotation srcAnnotation, Document srcDocument,
	        Annotation targetAnnotation, Document targetDocument) {
	  for(AlignmentListener aListener : listeners) {
	    aListener.annotationsAligned(srcAnnotation, srcDocument, targetAnnotation, targetDocument);
	  }
	}
	
  protected void fireAnnotationsUnAligned(Annotation srcAnnotation, Document srcDocument,
          Annotation targetAnnotation, Document targetDocument) {
    for(AlignmentListener aListener : listeners) {
      aListener.annotationsUnaligned(srcAnnotation, srcDocument, targetAnnotation, targetDocument);
    }
  }
}
