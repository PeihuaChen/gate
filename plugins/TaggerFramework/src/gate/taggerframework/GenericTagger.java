package gate.taggerframework;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.Transducer;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.Files;
import gate.util.GateRuntimeException;
import gate.util.OffsetComparator;
import gate.util.ProcessManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CreoleResource(comment = "The Generic Tagger is Generic!")
public class GenericTagger extends AbstractLanguageAnalyser implements
                                                           ProcessingResource {

  // TODO Think about moving this to a runtime parameter
  public static final String STRING_FEATURE_NAME = "string";

  // The transducers that will do the pre and post processing
  private Transducer preProcess, postProcess;

  // The URLs of the JAPE grammars for pre and post processing
  private URL preProcessURL, postProcessURL;

  // The annotations sets used for input and output
  private String inputASName, outputASName;

  // The character encoding the tagger expects and a regex to process
  // the output
  private String encoding, regex;

  // the path to the tagger binary
  private URL taggerBinary, taggerDir;

  // flags to pass to the tagger
  private List<String> taggerFlags;

  // should we...
  // fail if mapping between charsets fails
  // display debug information
  // update or replace existing output annotations
  private Boolean failOnUnmappableCharacter, debug, updateAnnotations;

  // The type of the input and output annotations
  private String inputAnnotationType, outputAnnotationType;

  // a comma separated list of feature names mapped to regex capturing
  // groups
  private FeatureMap featureMapping;

  public Resource init() throws ResourceInstantiationException {
    String tmpDir = System.getProperty("java.io.tmpdir");
    if(tmpDir == null || tmpDir.indexOf(' ') >= 0) {
      throw new ResourceInstantiationException(
              "The tagger requires your temporary directory to be set to a value "
                      + "that does not contain spaces.  Please set java.io.tmpdir to a "
                      + "suitable value.");
    }

    FeatureMap hidden = Factory.newFeatureMap();
    Gate.setHiddenAttribute(hidden, true);
    FeatureMap params = Factory.newFeatureMap();

    if(preProcessURL != null) {
      params.put("grammarURL", preProcessURL);
      preProcess = (Transducer)Factory.createResource("gate.creole.Transducer",
              params, hidden);
    }

    if(postProcessURL != null) {
      params.put("grammarURL", postProcessURL);
      postProcess = (Transducer)Factory.createResource(
              "gate.creole.Transducer", params, hidden);
    }

    return this;
  }

  public void execute() throws ExecutionException {
    if(document == null)
      throw new ExecutionException("No document to process!");

    if(taggerBinary == null)
      throw new ExecutionException(
              "Cannot proceed unless a tagger executable is specified.");

    if(encoding == null) {
      throw new ExecutionException("No encoding specified");
    }

    if(preProcess != null) {
      preProcess.setInputASName(inputASName);
      preProcess.setOutputASName(inputASName);
      preProcess.setDocument(document);

      try {
        preProcess.execute();
      }
      finally {
        preProcess.setDocument(null);
      }
    }

    // get current text from GATE for the tagger
    File textfile = getCurrentText();

    // build the command line for running the tagger
    String[] taggerCmd = buildCommandLine(textfile);

    // run the tagger and put the output back into GATE
    readOutput(runTagger(taggerCmd));

    if(postProcess != null) {
      postProcess.setInputASName(outputASName);
      postProcess.setOutputASName(outputASName);
      postProcess.setDocument(document);

      try {
        postProcess.execute();
      }
      finally {
        postProcess.setDocument(null);
      }
    }

    // delete the temporary text file
    if(!debug) textfile.delete();
  }

  protected String[] buildCommandLine(File textfile) throws ExecutionException {
    // check that the file exists
    File scriptfile = Files.fileFromURL(taggerBinary);
    if(scriptfile.exists() == false)
      throw new ExecutionException("Script " + scriptfile.getAbsolutePath()
              + " does not exist");

    // build the command line.
    // If the system property shell.path is set, use this as the path to
    // the bourne shell interpreter and place it as the first item on
    // the command line. If not, then just pass the script as the first
    // item. The system property is useful on platforms that don't
    // support shell scripts with #! lines natively, e.g. on Windows you
    // can set the property to c:\cygwin\bin\sh.exe (or whatever is
    // appropriate on your system) to invoke the script via Cygwin sh
    int index = 0;
    String[] taggerCmd;
    String shPath = System.getProperty("shell.path");
    if(shPath != null) {
      taggerCmd = new String[3 + taggerFlags.size()];
      taggerCmd[0] = shPath;
      index = 1;
    }
    else {
      taggerCmd = new String[2 + taggerFlags.size()];
    }

    String[] flags = taggerFlags.toArray(new String[0]);

    System.arraycopy(flags, 0, taggerCmd, index + 1, flags.length);

    // generate tagger command line
    taggerCmd[index] = scriptfile.getAbsolutePath();
    taggerCmd[taggerCmd.length - 1] = textfile.getAbsolutePath();

    if(debug) {
      String sanityCheck = "";
      for(String s : taggerCmd)
        sanityCheck += " " + s;
      System.out.println(sanityCheck);
    }

    return taggerCmd;
  }

  private File getCurrentText() throws ExecutionException {
    File gateTextFile = null;
    try {
      gateTextFile = File.createTempFile("treetagger", ".txt");
      Charset charset = Charset.forName(encoding);
      // depending on the failOnUnmappableCharacter parameter, we either
      // make the output stream writer fail or replace the unmappable
      // character with '?'
      CharsetEncoder charsetEncoder = charset.newEncoder()
              .onUnmappableCharacter(
                      failOnUnmappableCharacter
                              ? CodingErrorAction.REPORT
                              : CodingErrorAction.REPLACE);
      FileOutputStream fos = new FileOutputStream(gateTextFile);
      OutputStreamWriter osw = new OutputStreamWriter(fos, charsetEncoder);
      BufferedWriter bw = new BufferedWriter(osw);
      AnnotationSet annotSet = (inputASName == null || inputASName.trim()
              .length() == 0) ? document.getAnnotations() : document
              .getAnnotations(inputASName);
      annotSet = annotSet.get(inputAnnotationType);
      if(annotSet == null || annotSet.size() == 0) {
        throw new GateRuntimeException("No " + inputAnnotationType
                + " found in the document.");
      }

      // sort tokens according to their offsets
      List<Annotation> inputAnnotations = new ArrayList<Annotation>(annotSet);
      Collections.sort(inputAnnotations, new OffsetComparator());

      // and now start writing them in a file
      for(int i = 0; i < inputAnnotations.size(); i++) {
        FeatureMap features = inputAnnotations.get(i).getFeatures();

        if(features == null || features.size() == 0
                || !features.containsKey(STRING_FEATURE_NAME)) {
          throw new GateRuntimeException(inputAnnotationType + " must have '"
                  + STRING_FEATURE_NAME + "' feature, which couldn't be found");
        }
        String string = (String)features.get(STRING_FEATURE_NAME);
        bw.write(string);
        if(i + 1 < inputAnnotations.size()) bw.newLine();
      }
      bw.close();
    }
    catch(CharacterCodingException cce) {
      throw (ExecutionException)new ExecutionException(
              "Document contains a character that cannot be represented "
                      + "in " + encoding).initCause(cce);
    }
    catch(java.io.IOException except) {
      throw (ExecutionException)new ExecutionException(
              "Error creating temporary file for tagger").initCause(except);
    }
    return (gateTextFile);
  }

  private ProcessManager processManager = new ProcessManager();

  private InputStream runTagger(String[] cmdline) throws ExecutionException {
    // TODO: replace this with the ProcessManager from gate.util

    Process p = null;

    try {
      if(taggerDir == null)
        p = Runtime.getRuntime().exec(cmdline);
      else p = Runtime.getRuntime().exec(cmdline, new String[] {},
              Files.fileFromURL(taggerDir));

      return p.getInputStream();
    }
    catch(Exception e) {
      throw new ExecutionException(e);
    }

    /*
     * try { ByteArrayOutputStream out = new ByteArrayOutputStream();
     * processManager.runProcess(cmdline, out, null); return new
     * ByteArrayInputStream(out.toByteArray()); } catch(Exception e) {
     * throw new ExecutionException(e); }
     */
  }

  private void readOutput(InputStream in) throws ExecutionException {
    String line;

    // sorted list of input annotations
    AnnotationSet annotSet = (inputASName == null || inputASName.trim()
            .length() == 0) ? document.getAnnotations() : document
            .getAnnotations(inputASName);
    annotSet = annotSet.get(inputAnnotationType);
    List<Annotation> inputAnnotations = new ArrayList<Annotation>(annotSet);
    Collections.sort(inputAnnotations, new OffsetComparator());

    Annotation currentInput = inputAnnotations.remove(0);

    AnnotationSet aSet = (outputASName == null || outputASName.trim().length() == 0)
            ? document.getAnnotations()
            : document.getAnnotations(outputASName);

    Charset charset = Charset.forName(encoding);

    CharsetDecoder charsetDecoder = charset.newDecoder();
    // run tagger and save output
    try {
      // get the tagger output (gate input)
      BufferedReader input = new BufferedReader(new InputStreamReader(in,
              charsetDecoder));

      Pattern resultPattern = Pattern.compile(regex);

      List<Annotation> outputAnnotations = new ArrayList<Annotation>(aSet.get(
              outputAnnotationType, currentInput.getStartNode().getOffset(),
              currentInput.getEndNode().getOffset()));

      if(!updateAnnotations)
        aSet.removeAll(outputAnnotations);
      else Collections.sort(outputAnnotations, new OffsetComparator());

      int currentPosition = 0;

      while((line = input.readLine()) != null) {
        if (debug) System.out.println(line);
        
        Matcher m = resultPattern.matcher(line);

        if(m.matches()) {
          FeatureMap features = Factory.newFeatureMap();

          for(Map.Entry<Object, Object> kv : featureMapping.entrySet()) {
            features.put(kv.getKey(), m.group(Integer.parseInt(String
                    .valueOf(kv.getValue()))));
          }

          while(updateAnnotations && outputAnnotations.size() == 0) {
            if(inputAnnotations.size() == 0)
              throw new Exception("no remaining annotations of type "
                      + outputAnnotationType + " to update");

            currentInput = inputAnnotations.remove(0);
            outputAnnotations.addAll(aSet.get(outputAnnotationType,
                    currentInput.getStartNode().getOffset(), currentInput
                            .getEndNode().getOffset()));
            Collections.sort(outputAnnotations, new OffsetComparator());
          }

          Annotation next = (outputAnnotations.size() == 0
                  ? null
                  : outputAnnotations.remove(0));

          if(next != null && updateAnnotations) {

            String encoded = new String(charset.encode(
                    (String)next.getFeatures().get(STRING_FEATURE_NAME))
                    .array(), encoding);

            if(!encoded.equals(features.get(STRING_FEATURE_NAME)))
              throw new Exception("annotations are out of sync: " + encoded
                      + " != " + features.get(STRING_FEATURE_NAME));

            features.remove(STRING_FEATURE_NAME);

            next.getFeatures().putAll(features);
          }
          else {

            String encodedInput = new String(charset.encode(
                    document.getContent().getContent(
                            currentInput.getStartNode().getOffset(),
                            currentInput.getEndNode().getOffset()).toString())
                    .array(), encoding);

            while((currentPosition = encodedInput.indexOf((String)features
                    .get("string"), currentPosition)) == -1) {
              if(inputAnnotations.size() == 0)
                throw new Exception("no remaning annotations of type "
                        + inputAnnotationType + " to add within");

              currentInput = inputAnnotations.remove(0);

              aSet.removeAll(aSet.get(outputAnnotationType, currentInput
                      .getStartNode().getOffset(), currentInput.getEndNode()
                      .getOffset()));

              currentPosition = 0;
              encodedInput = new String(charset
                      .encode(
                              document.getContent().getContent(
                                      currentInput.getStartNode().getOffset(),
                                      currentInput.getEndNode().getOffset())
                                      .toString()).array(), encoding);
            }

            Long start = currentPosition
                    + currentInput.getStartNode().getOffset();
            Long end = start
                    + ((String)features.get(STRING_FEATURE_NAME)).length();

            aSet.add(start, end, outputAnnotationType, features);

            currentPosition = (int)(end - currentInput.getStartNode()
                    .getOffset());
          }
        }
      }
    }
    catch(Exception err) {
      err.printStackTrace();
      throw (ExecutionException)new ExecutionException(
              "Error occurred running tagger").initCause(err);
    }
  }

  public FeatureMap getFeatureMapping() {
    return featureMapping;
  }

  @RunTime
  @CreoleParameter(defaultValue = "string=1;category=2;lemma=3", comment = "mapping from matching groups to feature names")
  public void setFeatureMapping(FeatureMap featureMapping) {
    this.featureMapping = featureMapping;
  }

  public URL getTaggerBinary() {
    return taggerBinary;
  }

  @RunTime
  @CreoleParameter(comment = "Name of the TreeTagger command file")
  public void setTaggerBinary(URL taggerBinary) {
    this.taggerBinary = taggerBinary;
  }

  public URL getTaggerDir() {
    return taggerDir;
  }

  @Optional
  @RunTime
  @CreoleParameter(comment = "directory in which to run the tagger")
  public void setTaggerDir(URL taggerDir) {
    this.taggerDir = taggerDir;
  }

  public List<String> getTaggerFlags() {
    return taggerFlags;
  }

  @RunTime
  @CreoleParameter(defaultValue = "", comment = "flags passed to tagger script")
  public void setTaggerFlags(List<String> taggerFlags) {
    this.taggerFlags = taggerFlags;
  }

  public Boolean getUpdateAnnotations() {
    return updateAnnotations;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true", comment = "do you want to update annotations or add new ones?")
  public void setUpdateAnnotations(Boolean updateAnnotations) {
    this.updateAnnotations = updateAnnotations;
  }

  public String getRegex() {
    return regex;
  }

  @RunTime
  @CreoleParameter(defaultValue = "(.+)\t(.+)\t(.+)", comment = "regex to process tagger ouptut")
  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getInputAnnotationType() {
    return inputAnnotationType;
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token", comment = "annotation used as input to tagger")
  public void setInputAnnotationType(String inputAnnotationType) {
    this.inputAnnotationType = inputAnnotationType;
  }

  public String getOutputAnnotationType() {
    return outputAnnotationType;
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token", comment = "annotation output by tagger")
  public void setOutputAnnotationType(String outputAnnotationType) {
    this.outputAnnotationType = outputAnnotationType;
  }

  public Boolean getDebug() {
    return debug;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true", comment = "turn on debugging options")
  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public String getInputASName() {
    return inputASName;
  }

  @Optional
  @RunTime
  @CreoleParameter(comment = "annotation set in which annotations are created")
  public void setInputASName(String inputASName) {
    this.inputASName = inputASName;
  }

  public String getOutputASName() {
    return outputASName;
  }

  @Optional
  @RunTime
  @CreoleParameter(comment = "annotation set in which annotations are created")
  public void setOutputASName(String outputASName) {
    this.outputASName = outputASName;
  }

  public String getEncoding() {
    return encoding;
  }

  @RunTime
  @CreoleParameter(defaultValue = "ISO-8859-1", comment = "Character encoding for temporary files, must match "
          + "the encoding of your tagger data files")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public Boolean getFailOnUnmappableCharacter() {
    return failOnUnmappableCharacter;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true", comment = "Should the tagger fail if it encounters a character which "
          + "is not mappable into the specified encoding?")
  public void setFailOnUnmappableCharacter(Boolean failOnUnmappableCharacter) {
    this.failOnUnmappableCharacter = failOnUnmappableCharacter;
  }

  public URL getPreProcessURL() {
    return preProcessURL;
  }

  @Optional
  @CreoleParameter(comment = "JAPE grammar to use for pre-processing")
  public void setPreProcessURL(URL preProcessURL) {
    this.preProcessURL = preProcessURL;
  }

  public URL getPostProcessURL() {
    return postProcessURL;
  }

  @Optional
  @CreoleParameter(comment = "JAPE grammar to use for post-processing")
  public void setPostProcessURL(URL postProcessURL) {
    this.postProcessURL = postProcessURL;
  }
}
