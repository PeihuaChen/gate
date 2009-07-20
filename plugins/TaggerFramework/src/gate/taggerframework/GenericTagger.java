package gate.taggerframework;

import gate.AnnotationSet;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;

import java.net.URL;

public class GenericTagger extends AbstractLanguageAnalyser implements
                                                           ProcessingResource {

  //The annotations sets used for input and output
  private AnnotationSet inputAS, outputAS;
  
  //The character encoding the tagger expects and a regex to process the output
  private String encoding, regex;
  
  //the path to the tagger binary
  private URL taggerBinary;
  
  //flags to pass to the tagger
  private String taggerFlags;

  //should we...
  //  fail if mapping between charsets fails
  //  display debug information
  //  update or replace existing output annotations
  private Boolean failOnUnmappableCharacter, debug, updateAnnotations;
  
  //The type of the input and output annotations
  private String inputAnnotationType, outputAnnotationType;

  //a comma separated list of feature names mapped to regex capturing groups
  private String featureMapping;
  
  public String getFeatureMapping() {
    return featureMapping;
  }

  public void setFeatureMapping(String featureMapping) {
    this.featureMapping = featureMapping;
  }

  public URL getTaggerBinary() {
    return taggerBinary;
  }

  public void setTaggerBinary(URL taggerBinary) {
    this.taggerBinary = taggerBinary;
  }

  public String getTaggerFlags() {
    return taggerFlags;
  }

  public void setTaggerFlags(String taggerFlags) {
    this.taggerFlags = taggerFlags;
  }
  
  public Boolean getUpdateAnnotations() {
    return updateAnnotations;
  }

  public void setUpdateAnnotations(Boolean updateAnnotations) {
    this.updateAnnotations = updateAnnotations;
  }
  
  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }
  
  public String getInputAnnotationType() {
    return inputAnnotationType;
  }

  public void setInputAnnotationType(String inputAnnotationType) {
    this.inputAnnotationType = inputAnnotationType;
  }

  public String getOutputAnnotationType() {
    return outputAnnotationType;
  }

  public void setOutputAnnotationType(String outputAnnotationType) {
    this.outputAnnotationType = outputAnnotationType;
  }

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public AnnotationSet getInputAS() {
    return inputAS;
  }
  
  public void setInputAS(AnnotationSet inputAS) {
    this.inputAS = inputAS;
  }
  
  public AnnotationSet getOutputAS() {
    return outputAS;
  }
  
  public void setOutputAS(AnnotationSet outputAS) {
    this.outputAS = outputAS;
  }
  
  public String getEncoding() {
    return encoding;
  }
  
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  
  public Boolean getFailOnUnmappableCharacter() {
    return failOnUnmappableCharacter;
  }
  
  public void setFailOnUnmappableCharacter(Boolean failOnUnmappableCharacter) {
    this.failOnUnmappableCharacter = failOnUnmappableCharacter;
  }
}