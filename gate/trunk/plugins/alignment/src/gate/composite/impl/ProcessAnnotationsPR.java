package gate.composite.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gate.*;
import gate.composite.CombiningMethod;
import gate.composite.CombiningMethodException;
import gate.composite.CompositeDocument;
import gate.compound.CompoundDocument;
import gate.compound.impl.CompoundDocumentImpl;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;

/**
 * A PR that takes a document, annotation type, allows combining members
 * of a compound document. The newly created document is an instance of
 * composite document, which becomes another member of the compound
 * document.
 * 
 * @author niraj
 */
public class ProcessAnnotationsPR extends AbstractLanguageAnalyser implements
                                                                  ProcessingResource {

  private SerialAnalyserController controller;

  private String unitAnnotationType;

  private List<String> annotationTypesToCopy;

  private String inputAS;

  private CompoundDocument compoundDoc;

  protected CombiningMethod combiningMethodInst;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    try {
      combiningMethodInst = new CombineFromAnnotID();
      compoundDoc = new CompoundDocumentImpl();
      compoundDoc.init();
      return this;
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
  }

  /* this method is called to reinitialize the resource */
  public void reInit() throws ResourceInstantiationException {
    // reinitialization code
    init();
  }

  public void execute() throws ExecutionException {
    if(document == null) {
      throw new ExecutionException("Document is null!");
    }

    AnnotationSet set = inputAS == null || inputAS.trim().length() == 0
            ? document.getAnnotations()
            : document.getAnnotations(inputAS);

    AnnotationSet unitSet = set.get(unitAnnotationType);
    if(set.isEmpty())
      throw new ExecutionException("Could not find annotations of type :"
              + unitAnnotationType);

    Set<String> annotTypesToCopy = null;
    if(this.annotationTypesToCopy != null
            && !this.annotationTypesToCopy.isEmpty()) {
      annotTypesToCopy = new HashSet<String>(this.annotationTypesToCopy);
    }

    // add the current document as a member of the compound document
    compoundDoc.addDocument(document.getName(), document);
    try {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(CombineFromAnnotID.INPUT_AS_NAME_FEATURE_NAME, inputAS);
      map.put(CombineFromAnnotID.DOCUMENT_ID_FEATURE_NAME, document.getName());
      map.put(CombineFromAnnotID.ANNOTATION_TYPES_TO_COPY_FEATURE_NAME,
              annotTypesToCopy);

      for(Annotation annotation : unitSet) {

        map.put(CombineFromAnnotID.ANNOTATION_ID_FEATURE_NAME, annotation
                .getId());
        CompositeDocument compositeDoc = null;
        Corpus corpus = null;
        try {
          compositeDoc = combiningMethodInst.combine(
                  compoundDoc, map);
          compoundDoc.removeDocument(CompositeDocument.COMPOSITE_DOC_NAME);
          compoundDoc.addDocument(CompositeDocument.COMPOSITE_DOC_NAME,
                  compositeDoc);
          
          // change focus to composite document
          compoundDoc.setCurrentDocument(CompositeDocument.COMPOSITE_DOC_NAME);
          
          // now run the application on the composite document
          corpus = gate.Factory.newCorpus("compoundDocCorpus");
          corpus.add(compoundDoc);
          controller.setCorpus(corpus);
          controller.execute();
          
          // finally get rid of the composite document
          compoundDoc.removeDocument(CompositeDocument.COMPOSITE_DOC_NAME);
          gate.Factory.deleteResource(compositeDoc);
          gate.Factory.deleteResource(corpus);
          compositeDoc = null;
        }
        catch(CombiningMethodException e) {
          throw new ExecutionException(e);
        }
        catch(ResourceInstantiationException e) {
          throw new ExecutionException(e);
        } finally {
          if(compositeDoc != null) {
            gate.Factory.deleteResource(compositeDoc);
          }
          
          if(corpus != null) {
            gate.Factory.deleteResource(corpus);
          }
        }
      }
    }
    finally {
      compoundDoc.removeDocument(document.getName());
      compoundDoc.removeDocument(CompositeDocument.COMPOSITE_DOC_NAME);
    }
  }

  public SerialAnalyserController getController() {
    return controller;
  }

  public void setController(SerialAnalyserController controller) {
    this.controller = controller;
  }

  public String getUnitAnnotationType() {
    return unitAnnotationType;
  }

  public void setUnitAnnotationType(String unitAnnotationType) {
    this.unitAnnotationType = unitAnnotationType;
  }

  public List<String> getAnnotationTypesToCopy() {
    return annotationTypesToCopy;
  }

  public void setAnnotationTypesToCopy(List<String> annotationTypesToCopy) {
    this.annotationTypesToCopy = annotationTypesToCopy;
  }

  public String getInputAS() {
    return inputAS;
  }

  public void setInputAS(String inputAS) {
    this.inputAS = inputAS;
  }
} // class ProcessAnnotationsPR