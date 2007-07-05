package gate.composite.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.composite.CombiningMethod;
import gate.composite.CompositeDocument;
import gate.composite.OffsetDetails;
import gate.compound.CompoundDocument;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceInstantiationException;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

/**
 * Implementation of the Composite Document.
 * 
 * @author niraj
 */
public class CompositeDocumentImpl extends DocumentImpl implements
		CompositeDocument, AnnotationSetListener, DocumentListener {

	private static final long serialVersionUID = -1379936764549428131L;

	/**
	 * CombiningMethod that was used for creating this document.
	 */
	protected CombiningMethod combiningMethod;

	/**
	 * A Map that contains offet mappings. This is used for copying and removing
	 * annotations to the respective documents.
	 */
	protected HashMap<String, List<OffsetDetails>> offsetMappings;

	/**
	 * Set of ids of combined documents.
	 */
	protected Set<String> combinedDocumentIds;

	/**
	 * The compound document which the current composite document belongs to.
	 */
	protected CompoundDocument compoundDocument;

	/**
	 * When a new annotation is added to the composite document, this parameter
	 * tells whether the annotation copying and deletion to their respective
	 * documents should be carried out or not.
	 */
	protected boolean disableListener = false;

	// init method
	public Resource init() throws ResourceInstantiationException {
		super.init();
		this.getAnnotations().addAnnotationSetListener(this);
		Set annotNames = this.getNamedAnnotationSets().keySet();
		if (annotNames != null && !annotNames.isEmpty()) {
			Iterator iter = annotNames.iterator();
			while (iter.hasNext()) {
				String asName = (String) iter.next();
				this.getAnnotations(asName).addAnnotationSetListener(this);
			}
		}
		this.addDocumentListener(this);
		return this;
	}

	/**
	 * Gets the combing method used for creating the composite document.
	 */
	public CombiningMethod getCombiningMethod() {
		return combiningMethod;
	}

	/**
	 * Sets the combining method to be used for creating the composite document.
	 */
	public void setCombiningMethod(CombiningMethod combiningMethod) {
		this.combiningMethod = combiningMethod;
	}

	/**
	 * Annotation added event
	 */
	public void annotationAdded(AnnotationSetEvent ase) {

		if (!disableListener && ase.getSourceDocument() == this) {
			AnnotationSet as = (AnnotationSet) ase.getSource();
			Annotation annot = ase.getAnnotation();
			FeatureMap features = Factory.newFeatureMap();
			features.putAll(annot.getFeatures());

			boolean defaultAS = as.getName() == null;
			for (String docID : combinedDocumentIds) {
				Document aDoc = compoundDocument.getDocument(docID);
				long stOffset = getOffsetInSrcDocument(docID, annot
						.getStartNode().getOffset().longValue());
				if (stOffset == -1)
					continue;
				long enOffset = getOffsetInSrcDocument(docID, annot
						.getEndNode().getOffset().longValue());
				if (enOffset == -1)
					continue;
				if (defaultAS) {
					try {
						aDoc.getAnnotations().add(new Long(stOffset),
								new Long(enOffset), annot.getType(), features);
						return;
					} catch (InvalidOffsetException ioe) {
						throw new GateRuntimeException(ioe);
					}
				} else {
					try {
						aDoc.getAnnotations(as.getName()).add(
								new Long(stOffset), new Long(enOffset),
								annot.getType(), features);
						return;
					} catch (InvalidOffsetException ioe) {
						throw new GateRuntimeException(ioe);
					}
				}
			}
		}
	}

	/**
	 * Annotation remove event
	 */
	public void annotationRemoved(AnnotationSetEvent ase) {
		if (!disableListener && ase.getSourceDocument() == this) {
			AnnotationSet as = (AnnotationSet) ase.getSource();
			Annotation annot = ase.getAnnotation();
			FeatureMap features = Factory.newFeatureMap();
			features.putAll(annot.getFeatures());

			boolean defaultAS = as.getName() == null;
			for (String docID : combinedDocumentIds) {
				Document aDoc = compoundDocument.getDocument(docID);
				long stOffset = getOffsetInSrcDocument(docID, annot
						.getStartNode().getOffset().longValue());
				if (stOffset == -1)
					continue;
				long enOffset = getOffsetInSrcDocument(docID, annot
						.getEndNode().getOffset().longValue());
				if (enOffset == -1)
					continue;
				AnnotationSet toRemove = null;

				if (defaultAS) {
					toRemove = aDoc.getAnnotations().getContained(
							new Long(stOffset), new Long(enOffset)).get(
							annot.getType(), features);
					if (toRemove != null && !toRemove.isEmpty()) {
						aDoc.getAnnotations()
								.remove(toRemove.iterator().next());
						return;
					}
				} else {
					toRemove = aDoc.getAnnotations(as.getName()).getContained(
							new Long(stOffset), new Long(enOffset)).get(
							annot.getType(), features);
					if (toRemove != null && !toRemove.isEmpty()) {
						aDoc.getAnnotations(as.getName()).remove(
								toRemove.iterator().next());
						return;
					}
				}
			}
		}
	}

	/**
	 * This method returns the respective offset in the source document.
	 */
	public long getOffsetInSrcDocument(String srcDocumentID, long offset) {
		List<OffsetDetails> list = offsetMappings.get(srcDocumentID);
		if (list == null)
			return -1;

		for (int i = 0; i < list.size(); i++) {
			OffsetDetails od = list.get(i);
			if (offset >= od.getNewStartOffset()
					&& offset <= od.getNewEndOffset()) {
				// so = 10 eo = 15
				// offset = 15
				// difference = offset - so = 5
				// oso = 0 oeo = 5 newoffset = oso + diff = 0 + 5 = 5
				long difference = offset - od.getNewStartOffset();
				return od.getOldStartOffset() + difference;
			}
		}
		return -1;
	}

	public void setOffsetMappingInformation(
			HashMap<String, List<OffsetDetails>> offsetMappings) {
		this.offsetMappings = offsetMappings;
	}

	/**
	 * return the IDs of combined documents
	 * 
	 * @return
	 */
	public Set<String> getCombinedDocumentsIds() {
		return this.combinedDocumentIds;
	}

	/**
	 * Sets the combined document IDs
	 * 
	 * @param combinedDocumentIds
	 */
	public void setCombinedDocumentsIds(Set<String> combinedDocumentIds) {
		this.combinedDocumentIds = combinedDocumentIds;
	}

	/**
	 * This method returns the compoundDocument whose member this composite
	 * document is.
	 * 
	 * @return
	 */
	public CompoundDocument getCompoundDocument() {
		return this.compoundDocument;
	}

	/**
	 * Sets the compound document.
	 * 
	 * @param compoundDocument
	 */
	public void setCompoundDocument(CompoundDocument compoundDocument) {
		this.compoundDocument = compoundDocument;
	}

	public void annotationSetAdded(DocumentEvent de) {
		Document doc = (Document) de.getSource();
		if (this == doc) {
			doc.getAnnotations(de.getAnnotationSetName())
					.addAnnotationSetListener(this);
		}
	}

	public void annotationSetRemoved(DocumentEvent de) {
		Document doc = (Document) de.getSource();
		if (this == doc) {
			doc.getAnnotations(de.getAnnotationSetName())
					.removeAnnotationSetListener(this);
		}
	}

	public void contentEdited(DocumentEvent de) {
		// do nothing
	}

}
