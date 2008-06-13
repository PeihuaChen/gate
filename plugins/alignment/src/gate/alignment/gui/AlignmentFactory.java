package gate.alignment.gui;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.compound.CompoundDocument;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides various methods that help in alignment process.
 * 
 * @author niraj
 */
public class AlignmentFactory {

  public static final String ALIGNMENT_FEATURE_NAME = "alignment";

  protected CompoundDocument compoundDocument;

  protected String comparatorClass;

  protected Comparator<Object> comparator;

  private String srcTokenAnnotationType;

  private String tgtTokenAnnotationType;

  private String srcDocumentID;

  private String tgtDocumentID;

  private AASequence srcSequence;

  private AASequence tgtSequence;

  /**
   * AlignmentFactory makes alignment easier
   * 
   * @param compoundDocument -> document where we want to achieve
   *          alignment
   * @param inputAS -> name of the inputAnnotationSet
   * @param tokenAnnotationType -> the level at what we want to achieve
   *          alignment (e.g. Token or may be some other annotation
   *          type)
   * @param unitAnnotationType -> AlignedParentAnnotationType (e.g. if
   *          sentences are already aligned)
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public AlignmentFactory(CompoundDocument alignedDocument,
          String srcDocumentId, String tgtDocumentId, String srcInputAS,
          String tgtInputAS, String srcTokenAnnotationType,
          String tgtTokenAnnotationType, String srcUnitAnnotationType,
          String tgtUnitAnnotationType, String comparatorClass)
          throws Exception {

    this.compoundDocument = alignedDocument;
    this.srcDocumentID = srcDocumentId;
    this.tgtDocumentID = tgtDocumentId;
    this.srcTokenAnnotationType = srcTokenAnnotationType;
    this.tgtTokenAnnotationType = tgtTokenAnnotationType;
    
    comparator = (Comparator)Class.forName(comparatorClass).newInstance();
    Document doc = compoundDocument.getDocument(srcDocumentId);
    AnnotationSet as = srcInputAS.equals("<null>")
            || srcInputAS.trim().length() == 0 ? doc.getAnnotations() : doc
            .getAnnotations(srcInputAS);
    srcSequence = new AASequence(doc, as, srcUnitAnnotationType);
    doc = compoundDocument.getDocument(tgtDocumentId);
    AnnotationSet as1 = tgtInputAS.equals("<null>")
            || tgtInputAS.trim().length() == 0 ? doc.getAnnotations() : doc
            .getAnnotations(tgtInputAS);
    tgtSequence = new AASequence(doc, as1, tgtUnitAnnotationType);
  }

  public String getText(Annotation annot, String language) {
    try {
      if(language.equals(srcDocumentID)) {
        return srcSequence.getText(annot);
      }
      else if(language.equals(tgtDocumentID)) {
        return tgtSequence.getText(annot);
      }
    }
    catch(InvalidOffsetException ioe) {
      throw new GateRuntimeException(ioe);
    }

    return null;
  }

  public AnnotationSet getAnnotationSet(String language) {
    if(language.equals(srcDocumentID)) {
      return srcSequence.set;
    }
    else if(language.equals(tgtDocumentID)) {
      return tgtSequence.set;
    }
    return null;
  }

  public AnnotationSet getUnderlyingAnnotations(Annotation annot,
          String language, String tokenAnnotationType) {
    if(language.equals(srcDocumentID)) {
      return srcSequence.getUnderlyingAnnotations(annot, tokenAnnotationType == null
              ? this.srcTokenAnnotationType
              : tokenAnnotationType);
    }
    else if(language.equals(tgtDocumentID)) {
      return tgtSequence.getUnderlyingAnnotations(annot, tokenAnnotationType == null
              ? this.tgtTokenAnnotationType
              : tokenAnnotationType);
    }
    return null;
  }

  private HashMap<String, Annotation> currentAnnotations;

  /**
   * The method returns a hashmap that has the following format e.g. en ->
   * english sentence (if document is sentence algined and user wants to
   * perform word alignment) e.g. hi -> hindi sentence
   * 
   * @return
   */
  public HashMap<String, Annotation> next() {
    HashMap<String, Annotation> annotations = new HashMap<String, Annotation>();
    annotations.put(srcDocumentID, srcSequence.next());
    annotations.put(tgtDocumentID, tgtSequence.next());
    this.currentAnnotations = annotations;
    return annotations;
  }

  public HashMap<String, Annotation> previous() {
    HashMap<String, Annotation> annotations = new HashMap<String, Annotation>();
    annotations.put(srcDocumentID, srcSequence.previous());
    annotations.put(tgtDocumentID, tgtSequence.previous());
    this.currentAnnotations = annotations;
    return annotations;
  }

  public HashMap<String, Annotation> current() {
    return currentAnnotations;
  }

  public boolean hasNext() {
    return srcSequence.hasNext() && tgtSequence.hasNext();
  }

  public boolean hasPrevious() {
    return srcSequence.hasPrevious() && tgtSequence.hasPrevious();
  }

  class AASequence {
    Document document;

    AnnotationSet set;

    // String parentType;

    List<Annotation> annotations;

    int counter = -1;

    public AASequence(Document doc, AnnotationSet set, String parentType) {
      this.document = doc;
      this.set = set;
      // collecting all sentences for example
      annotations = new ArrayList<Annotation>(set.get(parentType));
      Collections.sort(annotations, comparator);
    }

    public boolean hasNext() {
      if(counter + 1 < annotations.size()) {
        return true;
      }
      else {
        return false;
      }
    }

    // return next sentence
    public Annotation next() {
      counter++;
      return annotations.get(counter);
    }

    public Annotation previous() {
      counter--;
      return annotations.get(counter);
    }

    public boolean hasPrevious() {
      if(counter - 1 >= 0) {
        return true;
      }
      return false;
    }

    public void reset() {
      counter = -1;
    }

    public AnnotationSet getUnderlyingAnnotations(Annotation parentAnnot,
            String annotationType) {
      return set.getContained(parentAnnot.getStartNode().getOffset(),
              parentAnnot.getEndNode().getOffset()).get(annotationType);
    }

    public String getText(Annotation ann) throws InvalidOffsetException {
      return document.getContent().getContent(ann.getStartNode().getOffset(),
              ann.getEndNode().getOffset()).toString();
    }
  }

  public String getSrcDocumentID() {
    return srcDocumentID;
  }

  public String getTgtDocumentID() {
    return tgtDocumentID;
  }
}
