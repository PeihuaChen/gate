package gate.taggerframework;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.Files;
import gate.util.GateRuntimeException;
import gate.util.OffsetComparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CreoleResource(comment = "The Generic Tagger is Generic!")
public class GenericTagger extends AbstractLanguageAnalyser implements
                                                           ProcessingResource {

  // The annotations sets used for input and output
  private String inputAS, outputAS;

  // The character encoding the tagger expects and a regex to process
  // the output
  private String encoding, regex;

  // the path to the tagger binary
  private URL taggerBinary, taggerDir;

  // flags to pass to the tagger
  private String taggerFlags;

  // should we...
  // fail if mapping between charsets fails
  // display debug information
  // update or replace existing output annotations
  private Boolean failOnUnmappableCharacter, debug, updateAnnotations;

  // The type of the input and output annotations
  private String inputAnnotationType, outputAnnotationType;

  // a comma separated list of feature names mapped to regex capturing
  // groups
  private String featureMapping;

  public Resource init() throws ResourceInstantiationException {
    String tmpDir = System.getProperty("java.io.tmpdir");
    if(tmpDir == null || tmpDir.indexOf(' ') >= 0) {
      throw new ResourceInstantiationException(
              "The tagger requires your temporary directory to be set to a value "
                      + "that does not contain spaces.  Please set java.io.tmpdir to a "
                      + "suitable value.");
    }

    return this;
  }

  public void execute() throws ExecutionException {
    if(document == null)
      throw new ExecutionException("No document to process!");

    if(taggerBinary == null)
      throw new ExecutionException(
              "Cannot proceed unless a tagger script is specified.");

    if(encoding == null) {
      throw new ExecutionException("No encoding specified");
    }

    // get current text from GATE for the tagger
    File textfile = getCurrentText();

    String[] taggerCmd = buildCommandLine(textfile);
    
    // run the tagger
    runTagger(taggerCmd);

    // delete the temporary text file
    if (!debug) textfile.delete();
  }

  protected String[] buildCommandLine(File textfile) throws ExecutionException {
    // check that the file exists
    File scriptfile = Files.fileFromURL(taggerBinary);
    if(scriptfile.exists() == false)
      throw new ExecutionException("Script " + scriptfile.getAbsolutePath()
              + " does not exist");

    
    // build the command line.
    // If the system property shell.path is set, use this as the
    // path
    // to the bourne shell interpreter and place it as the first item on
    // the
    // command line. If not, then just pass the script as the first
    // item. The
    // system property is useful on platforms that don't support shell
    // scripts
    // with #! lines natively, e.g. on Windows you can set the property
    // to
    // c:\cygwin\bin\sh.exe (or whatever is appropriate on your system)
    // to
    // invoke the script via Cygwin sh
    int index = 0;
    String[] taggerCmd;
    String shPath = null;
    if((shPath = System.getProperty("shell.path")) != null) {
      taggerCmd = new String[3];
      taggerCmd[0] = shPath;
      index = 1;
    }
    else {
      taggerCmd = new String[2];
    }

    // generate TreeTagger command line
    taggerCmd[index] = scriptfile.getAbsolutePath();
    taggerCmd[index + 1] = textfile.getAbsolutePath();
    
    return taggerCmd;
  }

  private File getCurrentText() throws ExecutionException {
    File gateTextFile = null;
    try {
      gateTextFile = File.createTempFile("treetagger", ".txt");
      Charset charset = Charset.forName(encoding);
      // depending on the failOnUnmappableChar parameter, we either make
      // the
      // output stream writer fail or replace the unmappable character
      // with '?'
      CharsetEncoder charsetEncoder = charset.newEncoder()
              .onUnmappableCharacter(
                      failOnUnmappableCharacter
                              ? CodingErrorAction.REPORT
                              : CodingErrorAction.REPLACE);
      FileOutputStream fos = new FileOutputStream(gateTextFile);
      OutputStreamWriter osw = new OutputStreamWriter(fos, charsetEncoder);
      BufferedWriter bw = new BufferedWriter(osw);
      AnnotationSet annotSet = (inputAS == null || inputAS.trim().length() == 0) ? document.getAnnotations() : document
              .getAnnotations(inputAS);
      annotSet = annotSet.get(inputAnnotationType);
      if(annotSet == null || annotSet.size() == 0) {
        throw new GateRuntimeException(
                "No "+inputAnnotationType+" found in the document.");
      }

      // sort tokens according to their offsets
      List<Annotation> inputAnnotations = new ArrayList<Annotation>(annotSet);
      Collections.sort(inputAnnotations, new OffsetComparator());
      // and now start writing them in a file
      for(int i = 0; i < inputAnnotations.size(); i++) {
        FeatureMap features = inputAnnotations.get(i).getFeatures();
        
        //TODO: remove hard coded feature name
        
        if(features == null
                || features.size() == 0
                || !features
                        .containsKey("string")) {
          throw new GateRuntimeException(
                  inputAnnotationType + " must have 'string' feature, which couldn't be found");
        }
        String string = (String)features
                .get("string");
        bw.write(string);
        if(i + 1 < inputAnnotations.size()) bw.newLine();
      }
      bw.close();

      // now read the temp file back in and iterate over tokens pulling
      // the
      // string that has been passed to the tree tagger into a feature
      // of the
      // Token
      
      //TODO: Come back and think if this is really needed
      
      /*CharsetDecoder charsetDecoder = charset.newDecoder();
      FileInputStream fis = new FileInputStream(gateTextFile);
      InputStreamReader isr = new InputStreamReader(fis, charsetDecoder);
      BufferedReader br = new BufferedReader(isr);

      for(int i = 0; i < inputAnnotations.size(); i++) {
        FeatureMap features = ((Annotation)inputAnnotations.get(i)).getFeatures();
        features.put(TREE_TAGGER_STRING_FEATURE_NAME, br.readLine());
      }*/

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

  private void runTagger(String[] cmdline) throws ExecutionException {
    String line, word, tag, lemma;
    int indx = 0;
    AnnotationSet aSet;

    aSet = (outputAS == null || outputAS.trim().length() == 0)
            ? document.getAnnotations()
            : document.getAnnotations(outputAS);

            Charset charset = Charset.forName(encoding);
            
    CharsetDecoder charsetDecoder = charset.newDecoder();
    // run tagger and save output
    try {
      //TODO: replace this with the ProcessManager from gate.util
      Process p = null;
      
      if (taggerDir == null || taggerDir.trim().length() == 0)
        p = Runtime.getRuntime().exec(cmdline);
      else
        p = Runtime.getRuntime().exec(cmdline, new String[]{}, Files.fileFromURL(taggerDir));

      // get the tagger output (gate input)
      BufferedReader input = new BufferedReader(new InputStreamReader(p
              .getInputStream(), charsetDecoder));

      Pattern resultPattern = Pattern.compile(regex);
      
      List<Annotation> annotations = new ArrayList<Annotation>(aSet.get(outputAnnotationType));
      Collections.sort(annotations, new OffsetComparator());
      
      
      
      //TODO: change to use an array
      //TODO: mapping must map string=column of annotation text
      Map<String,Integer> mapping = new HashMap<String,Integer>();
      for (String pair : featureMapping.split(",")) {
        String[] kv = pair.trim().split("=");
        mapping.put(kv[0], Integer.parseInt(kv[1]));
      }
      
      while ((line = input.readLine()) != null) {
        Matcher m = resultPattern.matcher(line);
        
        if (m.matches()) {
          FeatureMap features = Factory.newFeatureMap();
          
          for (Map.Entry<String,Integer> kv : mapping.entrySet()) {
            features.put(kv.getKey(), m.group(kv.getValue()));
          }
          
          //TODO update or add new
          if (updateAnnotations && annotations.size() == 0)
              throw new Exception("no remaining annotations of type " + outputAnnotationType + " to update");
          
          Annotation next = (annotations.size() == 0 ? null : annotations.remove(0));
          
          if (next != null && updateAnnotations) {
            //TODO update existing annotation, check the annotations are in sync
            //TODO handle the fact that strings may not match due to different encodings
            
            String encoded = new String(charset.encode((String)next.getFeatures().get("string")).array(),encoding);
            
            if (!encoded.equals(features.get("string")))
              throw new Exception("annotations are out of sync: " + encoded + " != " + features.get("string"));
            
            features.remove("string");
            
            next.getFeatures().putAll(features);
          }
          else {
            //TODO add new annotation
          }
        }
      }
      
      /*
      // let us store all tagged data in lines
      // there must be at least the original form a tab a POS
      // and possibly a lemma preceded by a tab
      ArrayList lines = new ArrayList();
      while((line = input.readLine()) != null) {
        if(line.split("\t").length > 1) lines.add(line);
      }

      
      // sort tokens according to their offsets
      ArrayList tokens = new ArrayList(annotSet);
      Collections.sort(tokens, new OffsetComparator());

      if(tokens.size() != lines.size()) {
        System.out.println("Tokens : " + tokens.size() + " lines : "
                + lines.size());
        throw new GateRuntimeException(
                "Document does not have the expected number of tokens created by the treeTaggerBinary file");
      }

      // take one line at a time
      // check its length and the string feature and go on addition
      // features
      for(int i = 0; i < lines.size(); i++) {
        line = (String)lines.get(i);
        StringTokenizer st = new StringTokenizer(line);
        Annotation token = (Annotation)tokens.get(i);
        FeatureMap features = token.getFeatures();

        if(st.hasMoreTokens()) {
          tag = null;
          lemma = null;
          word = st.nextToken();

          // check if the word matches with the expected string (stored
          // in the
          // treeTaggerString feature of the token).
          String expectedTokenString = (String)features
                  .get(TREE_TAGGER_STRING_FEATURE_NAME);
          if(expectedTokenString == null)
            throw new GateException("Invalid Token in GATE document");
          if(!word.equals(expectedTokenString)) {
            throw new GateRuntimeException(
                    "Document does not have the expected number/sequence of tokens created by the treeTaggerBinary file");
          }

          if(st.hasMoreTokens()) tag = st.nextToken();
          if(tag != null && st.hasMoreTokens()) lemma = st.nextToken();

          // finally add features on the top of tokens
          if(lemma != null) features.put("lemma", lemma);
          if(tag != null) features.put("category", tag);

        }
        else {
          throw new GateRuntimeException(
                  "Document does not have the expected number of tokens created by the treeTaggerBinary file");
        }
      }

      // clean up the treeTaggerString features
      for(int i = 0; i < tokens.size(); i++) {
        FeatureMap features = ((Annotation)tokens.get(i)).getFeatures();
        features.remove(TREE_TAGGER_STRING_FEATURE_NAME);
      }*/
    }
    catch(Exception err) {
      err.printStackTrace();
      throw (ExecutionException)new ExecutionException(
              "Error occurred running TreeTagger with command line "
                      + Arrays.asList(cmdline)).initCause(err);
    }

  }

  public String getFeatureMapping() {
    return featureMapping;
  }

  @RunTime
  @CreoleParameter(defaultValue = "string=1,category=2,lemma=3",
        comment = "mapping from matching groups to feature names")
  public void setFeatureMapping(String featureMapping) {
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

  @RunTime
  @CreoleParameter(comment = "directory in which to run the tagger")
  public void setTaggerDir(URL taggerDir) {
    this.taggerDir = taggerDir;
  }

  public String getTaggerFlags() {
    return taggerFlags;
  }

  @RunTime
  @CreoleParameter(defaultValue = "",
        comment = "flags passed to tagger script")  
  public void setTaggerFlags(String taggerFlags) {
    this.taggerFlags = taggerFlags;
  }

  public Boolean getUpdateAnnotations() {
    return updateAnnotations;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true",
        comment = "do you want to update annotations or add new ones?")  
  public void setUpdateAnnotations(Boolean updateAnnotations) {
    this.updateAnnotations = updateAnnotations;
  }

  public String getRegex() {
    return regex;
  }

  @RunTime
  @CreoleParameter(defaultValue = "(.+)\t(.+)\t(.+)",
        comment = "regex to process tagger ouptut")  
  public void setRegex(String regex) {
    this.regex = regex;
  }

  public String getInputAnnotationType() {
    return inputAnnotationType;
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token",
        comment = "annotation used as input to tagger")
  public void setInputAnnotationType(String inputAnnotationType) {
    this.inputAnnotationType = inputAnnotationType;
  }

  public String getOutputAnnotationType() {
    return outputAnnotationType;
  }

  @RunTime
  @CreoleParameter(defaultValue = "Token",
        comment = "annotation output by tagger")
  public void setOutputAnnotationType(String outputAnnotationType) {
    this.outputAnnotationType = outputAnnotationType;
  }

  public Boolean getDebug() {
    return debug;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true",
      comment = "turn on debugging options")
  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public String getInputAS() {
    return inputAS;
  }

  @RunTime
  @CreoleParameter(comment = "annotation set in which annotations are created")  
  public void setInputAS(String inputAS) {
    this.inputAS = inputAS;
  }

  public String getOutputAS() {
    return outputAS;
  }

  @RunTime
  @CreoleParameter(comment = "annotation set in which annotations are created")  
  public void setOutputAS(String outputAS) {
    this.outputAS = outputAS;
  }

  public String getEncoding() {
    return encoding;
  }

  @RunTime
  @CreoleParameter(defaultValue = "ISO-8859-1",
      comment = "Character encoding for temporary files, must match "
        + "the encoding of your tagger data files")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public Boolean getFailOnUnmappableCharacter() {
    return failOnUnmappableCharacter;
  }

  @RunTime
  @CreoleParameter(defaultValue = "true",
        comment = "Should the tagger fail if it encounters a character which "
          + "is not mappable into the specified encoding?")
  public void setFailOnUnmappableCharacter(Boolean failOnUnmappableCharacter) {
    this.failOnUnmappableCharacter = failOnUnmappableCharacter;
  }
}
