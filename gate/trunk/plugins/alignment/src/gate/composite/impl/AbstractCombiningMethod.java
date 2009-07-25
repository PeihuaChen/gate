package gate.composite.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

/**
 * Abstract implementation of the combining method. Classes extending
 * this class must use startDocument() before adding any content (i.e.
 * addContent) and must finalizeDocument() at the end of all additions.
 * 
 * @author niraj
 */
public abstract class AbstractCombiningMethod implements CombiningMethod {

  protected HashMap<String, List<OffsetDetails>> offsetMappings;

  protected StringBuffer documentContent;

  protected String toAdd;

  protected CompoundDocument containerDocument;

  protected List<OffsetDetails> annotations;

  protected List<OffsetDetails> offsets;

  protected Set<String> annotationTypesToCopy;

  protected String inputASName;

  private boolean startDocumentCalled = false;

  /**
   * User must call this method to start a composite document
   * 
   * @param containerDocument - instance of compound document that the
   *          new composite is going to become member of.
   * @param inputASName - name of the annotation set the input
   *          annotations should be taken from and stored into the
   *          composite document.
   * @param annotationTypesToCopy - list of types of annotations to copy
   *          underlying the unit annotation. Supply null to copy all
   *          the annotations. Supply an empty set to copy nothing.
   */
  protected void startDocument(CompoundDocument containerDocument,
          String inputASName, Set<String> annotationTypesToCopy)
          throws CombiningMethodException {
    offsetMappings = new HashMap<String, List<OffsetDetails>>();
    this.containerDocument = containerDocument;
    this.inputASName = inputASName;
    this.annotationTypesToCopy = annotationTypesToCopy;
    this.annotations = new ArrayList<OffsetDetails>();
    this.offsets = new ArrayList<OffsetDetails>();
    documentContent = new StringBuffer();
    toAdd = "<?xml version=\"1.0\"?><composite>";
    startDocumentCalled = true;
  }

  protected CompositeDocument finalizeDocument()
          throws CombiningMethodException {
    if(!startDocumentCalled)
      throw new CombiningMethodException(
              "CompositeDocument is not initialized - please "
                      + "call the startDocument() method to initialize the "
                      + "composite document");

    documentContent = documentContent.insert(0, toAdd);
    documentContent.append("</composite>");
    FeatureMap features = Factory.newFeatureMap();
    features.put("collectRepositioningInfo", containerDocument
            .getCollectRepositioningInfo());
    features.put("encoding", containerDocument.getEncoding());
    features.put("markupAware", new Boolean(true));
    features.put("preserveOriginalContent", containerDocument
            .getPreserveOriginalContent());
    features.put("stringContent", documentContent.toString());
    FeatureMap subFeatures = Factory.newFeatureMap();
    Gate.setHiddenAttribute(subFeatures, true);
    CompositeDocument doc = null;
    try {
      doc = (CompositeDocument)Factory.createResource(
              "gate.composite.impl.CompositeDocumentImpl", features,
              subFeatures);
    }
    catch(ResourceInstantiationException e1) {
      throw new CombiningMethodException(e1);
    }

    ((gate.composite.impl.CompositeDocumentImpl)doc).disableListener = true;
    AnnotationSet aSet = (String)this.inputASName == null
            || this.inputASName.trim().length() == 0
            ? doc.getAnnotations()
            : doc.getAnnotations(this.inputASName);

    // lets add all annotations now
    for(OffsetDetails od : annotations) {
      String type = od.getOriginalAnnotation().getType();
      gate.FeatureMap f = od.getOriginalAnnotation().getFeatures();
      Integer id;
      try {
        id = aSet.add(new Long(od.getNewStartOffset()), new Long(od
                .getNewEndOffset()), type, f);
        od.setNewAnnotation(aSet.get(id));
      }
      catch(InvalidOffsetException e) {
        throw new CombiningMethodException(e);
      }
    }
    ((gate.composite.impl.CompositeDocumentImpl)doc).disableListener = false;

    doc.setName(CompositeDocument.COMPOSITE_DOC_NAME);
    doc.setCombiningMethod(this);
    doc.setOffsetMappingInformation(offsetMappings);
    doc.setCombinedDocumentsIds(new HashSet<String>(containerDocument
            .getDocumentIDs()));
    doc.setCompoundDocument(containerDocument);
    return doc;
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
   * This method returns the new offset for where the content was added
   * 
   * @param srcDocument
   * @param inputAS
   * @param unitAnnotation
   * @param copyUnderlyingAnnotations
   * @return
   */
  protected long[] addContent(Document srcDocument, Annotation unitAnnotation)
          throws CombiningMethodException {
    if(!startDocumentCalled)
      throw new CombiningMethodException(
              "CompositeDocument is not initialized - please "
                      + "call the startDocument() method to initialize the "
                      + "composite document");
    AnnotationSet inputAS = inputASName == null
            || inputASName.trim().length() == 0
            ? srcDocument.getAnnotations()
            : srcDocument.getAnnotations(inputASName);

    String documentID = srcDocument.getName();
    OffsetDetails offset = new OffsetDetails();
    offset.setOldStartOffset(unitAnnotation.getStartNode().getOffset()
            .longValue());
    offset.setOldEndOffset(unitAnnotation.getEndNode().getOffset().longValue());
    offset.setNewStartOffset(documentContent.length());
    try {
      documentContent.append(srcDocument.getContent().getContent(
              unitAnnotation.getStartNode().getOffset(),
              unitAnnotation.getEndNode().getOffset()));
    }
    catch(InvalidOffsetException e2) {
      throw new CombiningMethodException(e2);
    }
    offset.setNewEndOffset(documentContent.length());
    offset.setOriginalAnnotation(unitAnnotation);
    offsets.add(offset);
    annotations.add(offset);

    OffsetDetails unitAnnotDetails = new OffsetDetails();
    unitAnnotDetails.setOldStartOffset(offset.getOldStartOffset());
    unitAnnotDetails.setOldEndOffset(offset.getOldEndOffset());
    unitAnnotDetails.setNewStartOffset(offset.getNewStartOffset());
    unitAnnotDetails.setNewEndOffset(offset.getNewEndOffset());
    offsets.add(unitAnnotDetails);

    if(annotationTypesToCopy == null || !annotationTypesToCopy.isEmpty()) {
      AnnotationSet tempSet = inputAS.getContained(unitAnnotation
              .getStartNode().getOffset(), unitAnnotation.getEndNode()
              .getOffset());
      if(annotationTypesToCopy != null && !annotationTypesToCopy.isEmpty()) {
        tempSet = tempSet.get(annotationTypesToCopy);
      }

      Iterator iter = tempSet.iterator();
      while(iter.hasNext()) {
        Annotation anAnnot = (Annotation)iter.next();
        OffsetDetails anOffset = new OffsetDetails();
        anOffset.setOldStartOffset(anAnnot.getStartNode().getOffset()
                .longValue());
        anOffset.setOldEndOffset(anAnnot.getEndNode().getOffset().longValue());

        long stDiff = anOffset.getOldStartOffset() - offset.getOldStartOffset();
        long enDiff = anOffset.getOldEndOffset() - offset.getOldEndOffset();

        anOffset.setNewStartOffset(offset.getNewStartOffset() + stDiff);
        anOffset.setNewEndOffset(offset.getNewEndOffset() + enDiff);
        anOffset.setOriginalAnnotation(anAnnot);
        offsets.add(anOffset);
        annotations.add(anOffset);
      }
    }
    documentContent.append("\n");
    offsetMappings.put(documentID, offsets);
    return new long[] {unitAnnotDetails.getNewStartOffset(),
        unitAnnotDetails.getNewEndOffset()};
  }
}
