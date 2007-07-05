package gate.composite.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.composite.CombiningMethod;
import gate.composite.CombiningMethodException;
import gate.composite.CompositeDocument;
import gate.composite.OffsetDetails;
import gate.compound.CompoundDocument;
import gate.compound.impl.AnnotationStream;
import gate.util.OffsetComparator;

/**
 * Default Implementation of the Combining Method. This method requires three
 * parameters.
 * <p>
 * unitAnnotationType
 * <p>
 * inputASName
 * <p>
 * copyUnderlyingAnnotations.
 * <p>
 * unit Annotation type is the unit of combining two texts. E.g. if it is set to
 * "Sentence", one "Sentence" annotation from each document member is considered
 * and put together in the composite document.
 * <p>
 * inputASName tells the method from which annotation set the annotations should
 * be used from.
 * <p>
 * if set to true the copyUnderlyingAnnotations, all the underlying annotations
 * are copied to the composite document.
 * 
 * @author niraj
 */
public class DefaultCombiningMethod implements CombiningMethod {

	private static final long serialVersionUID = 4050197561715800118L;

	private String unitAnnotationType;

	private String inputASName;

	private boolean copyUnderlyingAnnotations;

	private HashMap<String, List<OffsetDetails>> offsetMappings;

	public DefaultCombiningMethod() {
		offsetMappings = new HashMap<String, List<OffsetDetails>>();
	}

	/**
	 * Returns the Ids of combined documents
	 * 
	 * @return
	 */
	public Set<String> getCombinedDocumentsIds() {
		return offsetMappings.keySet();
	}

	/**
	 * The parameters must contain two parameters "unitAnnotationType" and
	 * "inputASName" and "copyUnderlyingAnnotations" The parameter names are
	 * Case Sensitive. Example:
	 * <p>
	 * map.put("unitAnnotationType","Sentence");
	 * <p>
	 * map.put("inputASName","Key");
	 * <p>
	 * map.put("copyUnderlyingAnnotations","true");
	 */
	public CompositeDocument combine(CompoundDocument compoundDocument,
			Map parameters) throws CombiningMethodException {
		try {

			// first find out what user has provided
			// param1 = unitAnnotationType = what's the unit of combination.
			// For example given two documents if user says Sentence, the new
			// document will have the
			// following arrangement
			// doc1Sentence1
			// doc2Sentence1

			// doc1Sentence2
			// doc2Sentence2
			this.unitAnnotationType = (String) parameters
					.get("unitAnnotationType");
			this.inputASName = (String) parameters.get("inputASName");
			String copy = (String) parameters.get("copyUnderlyingAnnotations");
			this.copyUnderlyingAnnotations = copy == null ? false : Boolean
					.parseBoolean((String) parameters
							.get("copyUnderlyingAnnotations"));

			// obtain a list of documentIDs
			List<String> documentIDs = compoundDocument.getDocumentIDs();
			int total = 0;
			for (int i = 0; i < documentIDs.size(); i++) {
				String documentID = documentIDs.get(i);
				Document doc = compoundDocument.getDocument(documentID);
				if (doc instanceof CompositeDocument)
					continue;
				total++;
			}

			AnnotationStream streams[] = new AnnotationStream[total];
			for (int i = 0, j = 0; i < documentIDs.size() && j < total; i++, j++) {
				String documentID = documentIDs.get(i);
				Document doc = compoundDocument.getDocument(documentID);
				if (doc instanceof CompositeDocument) {
					j--;
					continue;
				}
				streams[j] = new AnnotationStream(doc, inputASName,
						unitAnnotationType, documentID, new OffsetComparator());
			}

			// we also need to create a composite document
			// for that we need text from all documents
			// 1. alignment
			// 2. unitAnnotationType
			StringBuffer str = new StringBuffer();
			CompositeDocument doc = null;

			if (unitAnnotationType != null
					&& unitAnnotationType.trim().length() > 0) {
				String toAdd = "<?xml version=\"1.0\"?><composite>";
				ArrayList<OffsetDetails> annotations = new ArrayList<OffsetDetails>();

				outer: while (true) {
					OffsetDetails unitOffset = new OffsetDetails();
					unitOffset.setOldStartOffset(-1);
					unitOffset.setOldEndOffset(-1);
					unitOffset.setNewStartOffset(str.length());
					unitOffset.setAnnotionType("alignment");
					unitOffset.setFeatures(Factory.newFeatureMap());
					boolean breaked = false;
					for (int i = 0; i < streams.length; i++) {
						Annotation annot = streams[i].next();

						String docID = streams[i].getLanguage();
						ArrayList<OffsetDetails> offsets = (ArrayList<OffsetDetails>) offsetMappings
								.get(docID);
						if (offsets == null)
							offsets = new ArrayList<OffsetDetails>();

						if (annot == null) {
							breaked = true;
							break;
						}

						OffsetDetails offset = new OffsetDetails();
						offset.setOldStartOffset(annot.getStartNode()
								.getOffset().longValue());
						offset.setOldEndOffset(annot.getEndNode().getOffset()
								.longValue());
						offset.setNewStartOffset(str.length());
						str.append(streams[i].getText(annot));
						offset.setNewEndOffset(str.length());
						offset.setAnnotionType(docID);
						offset.setFeatures(annot.getFeatures());
						offsets.add(offset);
						annotations.add(offset);

						if (copyUnderlyingAnnotations) {
							AnnotationSet tempSet = streams[i]
									.getUnderlyingAnnotations(annot);
							Iterator iter = tempSet.iterator();
							while (iter.hasNext()) {
								Annotation anAnnot = (Annotation) iter.next();
								OffsetDetails anOffset = new OffsetDetails();
								anOffset
										.setOldStartOffset(anAnnot
												.getStartNode().getOffset()
												.longValue());
								anOffset.setOldEndOffset(anAnnot.getEndNode()
										.getOffset().longValue());

								long stDiff = anOffset.getOldStartOffset()
										- offset.getOldStartOffset();
								long enDiff = anOffset.getOldEndOffset()
										- offset.getOldEndOffset();

								anOffset.setNewStartOffset(offset
										.getNewStartOffset()
										+ stDiff);
								anOffset.setNewEndOffset(offset
										.getNewEndOffset()
										+ enDiff);
								anOffset.setAnnotionType(anAnnot.getType());
								anOffset.setFeatures(anAnnot.getFeatures());
								offsets.add(anOffset);
								annotations.add(anOffset);
							}
						}
						str.append("\n");

						offsetMappings.put(docID, offsets);
					}
					unitOffset.setNewEndOffset(str.length());
					if (unitOffset.getNewStartOffset() != unitOffset
							.getNewEndOffset())
						annotations.add(unitOffset);
					if (breaked)
						break;
				}
				str = str.insert(0, toAdd);
				str.append("</composite>");
				FeatureMap features = Factory.newFeatureMap();
				features.put("collectRepositioningInfo", compoundDocument
						.getCollectRepositioningInfo());
				features.put("encoding", compoundDocument.getEncoding());
				features.put("markupAware", new Boolean(true));
				features.put("preserveOriginalContent", compoundDocument
						.getPreserveOriginalContent());
				features.put("stringContent", str.toString());
				FeatureMap subFeatures = Factory.newFeatureMap();
				Gate.setHiddenAttribute(subFeatures, true);
				doc = (CompositeDocument) Factory.createResource(
						"gate.composite.impl.CompositeDocumentImpl", features,
						subFeatures);

				((gate.composite.impl.CompositeDocumentImpl) doc).disableListener = true;
				AnnotationSet aSet = (String) this.inputASName == null
						|| this.inputASName.trim().length() == 0 ? doc
						.getAnnotations() : doc
						.getAnnotations(this.inputASName);

				// lets add all annotations now
				for (OffsetDetails od : annotations) {
					aSet.add(new Long(od.getNewStartOffset()), new Long(od
							.getNewEndOffset()), od.getAnnotionType(), od
							.getFeatures());
				}
				((gate.composite.impl.CompositeDocumentImpl) doc).disableListener = false;

			} else {
				str.append("<?xml version=\"1.0\"?>");
				str.append("<alignment>");
				for (int i = 0; i < documentIDs.size(); i++) {
					String documentID = (String) documentIDs.get(i);
					Document docMember = (Document) compoundDocument
							.getDocument(documentID);
					if (docMember instanceof CompositeDocument)
						continue;

					ArrayList<OffsetDetails> offsets = (ArrayList<OffsetDetails>) offsetMappings
							.get(documentID);
					if (offsets == null)
						offsets = new ArrayList<OffsetDetails>();
					OffsetDetails offset = new OffsetDetails();
					offset.setOldStartOffset(0);

					offset.setOldEndOffset(docMember.getContent().size()
							.longValue());
					str.append("<" + unitAnnotationType + ">");
					offset.setNewStartOffset(str.length());
					str.append(docMember.getContent().toString());
					offset.setNewEndOffset(str.length());
					str.append("</" + unitAnnotationType + ">");
					str.append("\n");
					offsets.add(offset);
					offsetMappings.put(documentID, offsets);
				}
				str.append("</alignment>");
				FeatureMap features = Factory.newFeatureMap();
				features.put("collectRepositioningInfo", compoundDocument
						.getCollectRepositioningInfo());
				features.put("encoding", compoundDocument.getEncoding());
				features.put("markupAware", new Boolean(true));
				features.put("preserveOriginalContent", compoundDocument
						.getPreserveOriginalContent());
				features.put("stringContent", str.toString());
				FeatureMap subFeatures = Factory.newFeatureMap();
				Gate.setHiddenAttribute(subFeatures, true);
				doc = (CompositeDocument) Factory.createResource(
						"gate.composite.impl.CompositeDocumentImpl", features,
						subFeatures);
			}
			doc.setName(CompositeDocument.COMPOSITE_DOC_NAME);
			doc.setCombiningMethod(this);
			doc.setOffsetMappingInformation(offsetMappings);
			doc.setCombinedDocumentsIds(new HashSet<String>(compoundDocument
					.getDocumentIDs()));
			doc.setCompoundDocument(compoundDocument);
			return doc;
		} catch (Exception e) {
			throw new CombiningMethodException(e);
		}
	}

	public boolean isCopyUnderlyingAnnotations() {
		return copyUnderlyingAnnotations;
	}

	public void setCopyUnderlyingAnnotations(boolean copyUnderlyingAnnotations) {
		this.copyUnderlyingAnnotations = copyUnderlyingAnnotations;
	}

	public String getInputASName() {
		return inputASName;
	}

	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getUnitAnnotationType() {
		return unitAnnotationType;
	}

	public void setUnitAnnotationType(String unitAnnotationType) {
		this.unitAnnotationType = unitAnnotationType;
	}
}
