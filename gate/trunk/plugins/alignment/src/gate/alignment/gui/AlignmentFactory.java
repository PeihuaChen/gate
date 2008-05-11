package gate.alignment.gui;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.composite.CompositeDocument;
import gate.compound.CompoundDocument;
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

  protected String inputAS;

  protected String tokenAnnotationType;

  protected String comparatorClass;

  protected Comparator<Object> comparator;

  private String unitAnnotationType;

  private List<String> documentIDs;

  private HashMap<String, AASequence> asMap;

  /**
   * AlignmentFactory makes alignment easier
   * 
   * @param compoundDocument ->
   *            document where we want to achieve alignment
   * @param inputAS ->
   *            name of the inputAnnotationSet
   * @param tokenAnnotationType ->
   *            the level at what we want to achieve alignment (e.g. Token
   *            or may be some other annotation type)
   * @param unitAnnotationType ->
   *            AlignedParentAnnotationType (e.g. if sentences are already
   *            aligned)
   * @throws Exception
   */
  public AlignmentFactory(CompoundDocument alignedDocument,
      String inputAS, String tokenAnnotationType,
      String unitAnnotationType, String comparatorClass)
      throws Exception {

    this.compoundDocument = alignedDocument;
    this.inputAS = inputAS;
    this.tokenAnnotationType = tokenAnnotationType;
    this.unitAnnotationType = unitAnnotationType;
    this.comparatorClass = comparatorClass;
    init();
  }

  @SuppressWarnings("unchecked")
  private void init() throws ClassNotFoundException,
      IllegalAccessException, InstantiationException {
    comparator = (Comparator) Class.forName(comparatorClass)
        .newInstance();
    documentIDs = compoundDocument.getDocumentIDs();
    asMap = new HashMap<String, AASequence>();
    for (int i = 0; i < documentIDs.size(); i++) {
      String lang = documentIDs.get(i);
      Document doc = compoundDocument.getDocument(lang);
      if (doc instanceof CompositeDocument) {
        documentIDs.remove(i);
        i--;
        continue;
      }

      AnnotationSet as = inputAS == null
          || inputAS.trim().length() == 0 ? doc.getAnnotations()
          : doc.getAnnotations(inputAS);
      AASequence aas = new AASequence(doc, as);
      asMap.put(lang, aas);
    }
  }

  public String getText(Annotation annot, String language) {
    AASequence seq = asMap.get(language);
    if (seq == null) {
      return null;
    }

    try {
      return seq.getText(annot);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public AnnotationSet getAnnotationSet(String language) {
    AASequence seq = asMap.get(language);
    if (seq == null) {
      return null;
    }
    return seq.set;
  }

  public AnnotationSet getUnderlyingAnnotations(Annotation annot,
      String language) {
    AASequence seq = asMap.get(language);
    if (seq == null) {
      return null;
    }

    try {
      return seq.getUnderlyingAnnotations(annot, tokenAnnotationType);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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
    for (String lang : documentIDs) {
      annotations.put(lang, ((AASequence) asMap.get(lang)).next());
    }
    this.currentAnnotations = annotations;
    return annotations;
  }

  public HashMap<String, Annotation> previous() {
    HashMap<String, Annotation> annotations = new HashMap<String, Annotation>();
    for (String lang : documentIDs) {
      annotations
          .put(lang, ((AASequence) asMap.get(lang)).previous());
    }
    this.currentAnnotations = annotations;
    return annotations;
  }
  
  public HashMap<String, Annotation> current() {
    return currentAnnotations;
  }

  public boolean hasNext() {
    boolean available = true;
    for (int i = 0; i < documentIDs.size(); i++) {
      String lang = (String) documentIDs.get(i);
      available = (available && ((AASequence) asMap.get(lang))
          .hasNext());
    }
    return available;
  }

  public boolean hasPrevious() {
    boolean available = true;
    for (int i = 0; i < documentIDs.size(); i++) {
      String lang = (String) documentIDs.get(i);
      available = (available && ((AASequence) asMap.get(lang))
          .hasPrevious());
    }
    return available;
  }

  public List<String> getDocumentIDs() {
    return documentIDs;
  }

  class AASequence {
    Document document;

    AnnotationSet set;

    List<Annotation> annotations;

    int counter = -1;

    public AASequence(Document doc, AnnotationSet set) {
      this.document = doc;
      this.set = set;
      // collecting all sentences for example
      annotations = new ArrayList<Annotation>(set
          .get(unitAnnotationType));
      Collections.sort(annotations, comparator);
    }

    public boolean hasNext() {
      if (counter + 1 < annotations.size()) {
        return true;
      } else {
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
      if (counter - 1 >= 0) {
        return true;
      }
      return false;
    }

    public void reset() {
      counter = -1;
    }

    public AnnotationSet getUnderlyingAnnotations(
        Annotation parentAnnot, String annotationType) {
      return set.getContained(parentAnnot.getStartNode().getOffset(),
          parentAnnot.getEndNode().getOffset()).get(
          annotationType);
    }

    public String getText(Annotation ann) throws InvalidOffsetException {
      return document.getContent().getContent(
          ann.getStartNode().getOffset(),
          ann.getEndNode().getOffset()).toString();
    }
  }
}