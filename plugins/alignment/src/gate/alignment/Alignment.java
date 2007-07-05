package gate.alignment;

import gate.Annotation;
import gate.Document;

import java.io.Serializable;
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
	protected Map<Annotation, Document> annotation2Document;

	/**
	 * Constructor
	 */
	public Alignment() {
		alignmentMatrix = new HashMap<Annotation, Set<Annotation>>();
		annotation2Document = new HashMap<Annotation, Document>();
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
	 * Aligns the given list of source annotations with the given list of target
	 * annotations.
	 * 
	 * @param srcAnnotations
	 * @param srcDocument
	 * @param targetAnnotations
	 * @param targetDocument
	 */
	public void align(List<Annotation> srcAnnotations, Document srcDocument,
			List<Annotation> targetAnnotations, Document targetDocument) {

		for (Annotation srcAnnotation : srcAnnotations) {
			for (Annotation targetAnnotation : targetAnnotations) {
				align(srcAnnotation, srcDocument, targetAnnotation,
						targetDocument);
			}
		}
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
		Set<Annotation> alignedToT = alignmentMatrix.get(srcAnnotation);
		if (alignedToT == null) {
			alignedToT = new HashSet<Annotation>();
			alignmentMatrix.put(srcAnnotation, alignedToT);
		}
		alignedToT.add(targetAnnotation);
		annotation2Document.put(srcAnnotation, srcDocument);

		Set<Annotation> alignedToS = alignmentMatrix.get(targetAnnotation);
		if (alignedToS == null) {
			alignedToS = new HashSet<Annotation>();
			alignmentMatrix.put(targetAnnotation, alignedToS);
		}
		alignedToS.add(srcAnnotation);
		annotation2Document.put(targetAnnotation, targetDocument);
	}

	/**
	 * Returns a set of aligned annotations.
	 * 
	 * @return
	 */
	public Set<Annotation> getAlignedAnnotations() {
		return alignmentMatrix.keySet();
	}

	/**
	 * This method tells which document the given annotation belongs to.
	 * 
	 * @param annotation
	 * @return
	 */
	public Document getDocument(Annotation annotation) {
		return annotation2Document.get(annotation);
	}

	/**
	 * Given the annotation, this method returns a set of the aligned
	 * annotations to that annotation.
	 * 
	 * @param srcAnnotation
	 * @return
	 */
	public Set<Annotation> getAlignedAnnotations(Annotation srcAnnotation) {
		return alignmentMatrix.get(srcAnnotation);
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
		else
			return !alignedTo.isEmpty();
	}
}
