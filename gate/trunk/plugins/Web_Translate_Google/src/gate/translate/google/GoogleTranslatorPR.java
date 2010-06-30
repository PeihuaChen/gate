package gate.translate.google;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.Resource;
import gate.Utils;
import gate.alignment.Alignment;
import gate.compound.CompoundDocument;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

/**
 * The PR uses google translation service to translate documents from one
 * language to the other. It requires users to provide a compound document as
 * input. It also asks users to specify the member document, the language of the
 * member document and the target langauge that user wants to translate into.
 * User can also provide a unit of translation if user wants to align the
 * translation automatically.
 * 
 * @author niraj
 */
@CreoleResource(name = "Google Translator PR", comment = "Runs a google translator over the source member document and produces the translated document. User can also specify if he/she wants to align unitOfTranslation in the source and the target documents.")
public class GoogleTranslatorPR extends gate.creole.AbstractLanguageAnalyser {

  /**
   * 
   */
  private static final long serialVersionUID = -8443994795704361590L;

  /**
   * Used internally - this is the document that will be used for holding the
   * original document and the composite documents.
   */
  private CompoundDocument compoundDoc;

  /**
   * Unit of translation.
   */
  private String unitOfTranslation;

  /**
   * name of the alignment feature that should be used for storing
   * unitOfTranslation alignment
   */
  private String alignmentFeatureName;

  /**
   * Input annotation set name, incase unit of Translation is specified.
   */
  private String inputASName;

  /**
   * Id of the source document that needs to be translated.
   */
  private String sourceDocumentId;

  /**
   * Id of the target document that needs to be created as a result of
   * translation.
   */
  private String targetDocumentId;

  /**
   * Language of the source document.
   */
  private Language sourceLanguage;

  /**
   * Language of the target document.
   */
  private Language targetLanguage;

  /**
   * Site referrer that is needed by the google.
   */
  private String siteReferrer;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    if(siteReferrer == null) { throw new ResourceInstantiationException(
            "Invalid value for siteReferrer:" + siteReferrer); }

    Translate.setHttpReferrer(siteReferrer);
    return this;
  }

  /* this method is called to reinitialize the resource */
  public void reInit() throws ResourceInstantiationException {
    // reinitialization code
    init();
  }

  /**
   * Should be called to execute this PR on a document.
   */
  public void execute() throws ExecutionException {
    // if no document provided
    if(document == null) { throw new ExecutionException("Document is null!"); }

    if(!(document instanceof CompoundDocument))
      throw new ExecutionException(
              "Document must be an instance of compound document!");

    if(sourceDocumentId == null)
      throw new ExecutionException("Source document id can not be null");

    compoundDoc = (CompoundDocument)document;

    // obtain source document
    Document sourceDoc = compoundDoc.getDocument(sourceDocumentId);

    if(sourceDoc == null) { throw new ExecutionException(
            "Invalid sourceDocumentId:" + sourceDocumentId
                    + " - no member document found with id:" + sourceDocumentId); }

    if(unitOfTranslation == null || unitOfTranslation.trim().length() == 0) { throw new ExecutionException(
            "unitOfTranslation cannot be null"); }

    if(targetDocumentId == null)
      throw new ExecutionException("Target document id can not be null");

    // annotation set to use
    AnnotationSet set =
            inputASName == null || inputASName.trim().length() == 0 ? sourceDoc
                    .getAnnotations() : sourceDoc.getAnnotations(inputASName);

    set = set.get(unitOfTranslation);
    if(set == null || set.isEmpty()) { throw new ExecutionException(
            "No annotations found of the type:" + unitOfTranslation); }

    List<String> textsToTranslate = new ArrayList<String>();
    List<Annotation> annotations = new ArrayList<Annotation>();
    annotations = Utils.inDocumentOrder(set);
    for(Annotation a : annotations) {
      textsToTranslate.add(Utils.stringFor(sourceDoc, a));
    }

    // we will modify the following buffer with translated text
    StringBuffer targetDocumentText =
            new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Document>\n");

    // maximum length to translate
    int MAX_LENGTH = 200;

    StringBuffer toTrans = new StringBuffer();
    for(int i = 0; i < textsToTranslate.size(); i++) {
      if(textsToTranslate.get(i).trim().length() == 0) continue;
      toTrans.append(textsToTranslate.get(i) + "<br>");
      if(toTrans.length() > MAX_LENGTH || i == textsToTranslate.size() - 1) {
        try {
          String result =
                  Translate.execute(toTrans.toString(), sourceLanguage,
                          targetLanguage);
          String[] translatedTexts = result.split("(<br>)");
          for(int j = 0; j < translatedTexts.length; j++) {
            targetDocumentText.append("<" + unitOfTranslation + ">"
                    + encodeXml(translatedTexts[j]) + "</" + unitOfTranslation
                    + ">");
            targetDocumentText.append("\n");
          }
        } catch(Exception e) {
          throw new ExecutionException(e);
        }
        toTrans = new StringBuffer();
      }
    }

    // first create a new Document
    Document targetDoc;
    try {
      File newFile =
              new File(System.getProperty("java.io.tmpdir"), targetDocumentId
                      + Gate.genSym() + ".xml");
      BufferedWriter bw =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                      newFile), "UTF-8"));
      targetDocumentText.append("</Document>");
      bw.write(targetDocumentText.toString());
      bw.close();
      targetDoc = Factory.newDocument(newFile.toURI().toURL(), "UTF-8");
      targetDoc.setName(targetDocumentId);
    } catch(ResourceInstantiationException e) {
      throw new ExecutionException(e);
    } catch(MalformedURLException e) {
      throw new ExecutionException(e);
    } catch(UnsupportedEncodingException e) {
      throw new ExecutionException(e);
    } catch(FileNotFoundException e) {
      throw new ExecutionException(e);
    } catch(IOException e) {
      throw new ExecutionException(e);
    }

    compoundDoc.addDocument(targetDocumentId, targetDoc);
    AnnotationSet targetSet =
            targetDoc.getAnnotations("Original markups").get(unitOfTranslation);
    List<Annotation> targetAnnots = Utils.inDocumentOrder(targetSet);

    AnnotationSet outputAS =
            inputASName == null || inputASName.trim().length() == 0 ? targetDoc
                    .getAnnotations() : targetDoc.getAnnotations(inputASName);
    Alignment alignment =
            compoundDoc.getAlignmentInformation(alignmentFeatureName);
    String asName =
            inputASName == null || inputASName.trim().length() == 0
                    ? null
                    : inputASName;

    for(int i = 0; i < annotations.size(); i++) {
      Annotation srcAnnot = annotations.get(i);
      Annotation tgtAnnot = targetAnnots.get(i);
      try {
        Integer id =
                outputAS.add(tgtAnnot.getStartNode().getOffset(), tgtAnnot
                        .getEndNode().getOffset(), tgtAnnot.getType(), Factory
                        .newFeatureMap());
        Annotation toAlign = outputAS.get(id);
        alignment
                .align(srcAnnot, asName, sourceDoc, toAlign, asName, targetDoc);
      } catch(InvalidOffsetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static String encodeXml(String str) {
    str = str.replaceAll("&", "&amp;");
    str = str.replaceAll("<", "&lt;");
    str = str.replaceAll(">", "&rt;");
    str = str.replaceAll("\"", "&quot;");
    str = str.replaceAll("\'", "&apos;");
    return str;
  }

  /**
   * Annotation set to use for obtaining segment annotations and the annotations
   * to copy into the composite document.
   * 
   * @return
   */
  public String getInputASName() {
    return inputASName;
  }

  @Optional
  @RunTime
  @CreoleParameter
  public void setInputASName(String inputAS) {
    this.inputASName = inputAS;
  }

  public String getUnitOfTranslation() {
    return unitOfTranslation;
  }

  @RunTime
  @CreoleParameter(defaultValue = "Sentence")
  public void setUnitOfTranslation(String unitOfTranslation) {
    this.unitOfTranslation = unitOfTranslation;
  }

  public String getAlignmentFeatureName() {
    return this.alignmentFeatureName;
  }

  @RunTime
  @CreoleParameter(defaultValue = "sentence-alignment")
  public void setAlignmentFeatureName(String alignmentFeatureName) {
    this.alignmentFeatureName = alignmentFeatureName;
  }

  public String getSourceDocumentId() {
    return sourceDocumentId;
  }

  @RunTime
  @CreoleParameter
  public void setSourceDocumentId(String sourceDocumentId) {
    this.sourceDocumentId = sourceDocumentId;
  }

  public String getTargetDocumentId() {
    return targetDocumentId;
  }

  @RunTime
  @CreoleParameter
  public void setTargetDocumentId(String targetDocumentId) {
    this.targetDocumentId = targetDocumentId;
  }

  public Language getSourceLanguage() {
    return sourceLanguage;
  }

  @RunTime
  @CreoleParameter
  public void setSourceLanguage(Language sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  public Language getTargetLanguage() {
    return targetLanguage;
  }

  @RunTime
  @CreoleParameter
  public void setTargetLanguage(Language targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  public String getSiteReferrer() {
    return siteReferrer;
  }

  @CreoleParameter
  public void setSiteReferrer(String siteReferrer) {
    this.siteReferrer = siteReferrer;
  }
} // class Google Translator PR
