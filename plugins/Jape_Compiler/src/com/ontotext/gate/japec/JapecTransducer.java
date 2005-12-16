package com.ontotext.gate.japec;

import java.io.*;
import java.net.*;
import java.util.*;
import gate.*;
import gate.util.*;
import gate.creole.*;

public class JapecTransducer extends AbstractLanguageAnalyser {
  private File grammarFile;
  private URL grammarURL;
  private File workDir;
//  private File classesDir;
  private File srcDir;
  private String packageName;
  private String encoding;
  private String inputASName;
  private String outputASName;
  private gate.creole.ontology.Ontology ontology;
  private List phases;

  public JapecTransducer() {
    grammarURL   = null;
    workDir      = null;
//    classesDir   = null;
    srcDir       = null;
    packageName  = null;
    encoding     = null;
    inputASName  = null;
    outputASName = null;
    ontology     = null;
    phases       = new ArrayList();
  }

  public Resource init() throws ResourceInstantiationException
  {
    //sanity checks
    if(grammarURL == null) throw new ResourceInstantiationException(
            "No URL provided for the grammar!");
    //Japec can only read file: URLs
    if(!grammarURL.getProtocol().equals("file")) 
      throw new ResourceInstantiationException(
              "Japec JAPE Compiler only supports file URLs for input!");
    
    loadGrammar();
    return this;
  }

  public void reInit() throws ResourceInstantiationException
  {
    loadGrammar();
  }

  protected void loadGrammar() throws ResourceInstantiationException
  {
    try{
      //create the temporary work directory
      workDir = File.createTempFile("japec-workdir", Gate.genSym());
      workDir.delete();
      workDir.mkdir();
      workDir.deleteOnExit();
      
//      classesDir = new File(workDir, "classes");
      srcDir = new File(workDir, "src");
      srcDir.deleteOnExit();
        
      grammarFile = new File(URI.create(grammarURL.toExternalForm()));
      
      packageName = "com.ontotext.gate.japec." + 
          grammarFile.getParentFile().getName() + Gate.genSym();
  
      compileGrammar();
      
      Files.rmdir(workDir);
  
//      loadAllClasses(classesDir, null);
//  
//      phases.clear();
//  
//      String line;
//      BufferedReader reader = new BufferedReader(new FileReader(new File(srcDir, "phases")));
//      while ((line = reader.readLine()) != null) {
//        Class phaseClass = Gate.getClassLoader().loadClass(packageName+"."+line);
//        SinglePhaseTransducer phase = (SinglePhaseTransducer) phaseClass.newInstance();
//        phase.setInputASName(inputASName);
//        phase.setOutputASName(outputASName);
//        phase.setOntology(ontology);
//        phases.add(phase);
//      }
//      reader.close();
    }catch(Exception e){
      throw new ResourceInstantiationException(e);
    }
  }

  public void execute() throws ExecutionException {
    for (int i = 0; i < phases.size(); i++)
    {
      SinglePhaseTransducer phase = (SinglePhaseTransducer) phases.get(i);
      phase.setDocument(document);
      phase.execute();
    }
  }

  private static boolean clearDir(File dir)
  {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++)
    {
      if (files[i].isDirectory())
      {
        if (!clearDir(files[i]))
          return false;
      }
      else
      {
        if (!files[i].delete())
          return false;
      }
    }
    return true;
  }

  protected void compileGrammar() throws IOException, 
      ResourceInstantiationException, ClassNotFoundException, GateException, 
      InstantiationException, IllegalAccessException
  {
    //compile the JAPE file(s) to Java
    if (srcDir.exists()) clearDir(srcDir);

    String osname = System.getProperty("os.name").toLowerCase();
    String suffix = null;
    if (osname.indexOf("windows") >= 0) suffix = "-windows.exe";
    else if (osname.indexOf("linux") >= 0) suffix = "-linux";
    else if (osname.indexOf("mac") >= 0) suffix = "-mac";
    else throw new ResourceInstantiationException(
            "Unsupported host platform: " + osname);
    
    
    File japecPath = new File (
            new File(Gate.getGateHome(), "plugins/Jape_Compiler"), 
            "japec" + suffix);

    Process p = Runtime.getRuntime().exec(new String[] {japecPath.getPath(),
                                                        grammarFile.getPath(),
                                                        "--odir", srcDir.getPath(),
                                                        "--package", packageName,
                                                        "--temp-names"
                                                       });

    BufferedReader error = 
      new BufferedReader(new InputStreamReader(p.getErrorStream()));
    String line;
    while ((line = error.readLine()) != null) {
      Err.prln(line);
    }
    error.close();

    BufferedReader input = 
      new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((line = input.readLine()) != null) {
      Err.prln(line);
    }
    input.close();
    
    
    
    //compile the generated classes and load the classes into the classloader
    Map sources = new LinkedHashMap();
    //read the phases file
    File phasesFile = new File(srcDir, "phases");
    phasesFile.deleteOnExit();
    BufferedReader phasesReader = new BufferedReader(
            new FileReader(phasesFile));
    for(line = phasesReader.readLine();
        line != null;
        line = phasesReader.readLine()){  
      String phaseName = line.trim();
      String phaseClassName = packageName + "." + phaseName;
      File phaseFile = new File(srcDir, phaseName + ".java");
      phaseFile.deleteOnExit();
      BufferedReader sourceReader = new BufferedReader(new FileReader(
              phaseFile));
      StringBuffer source = new StringBuffer();
      for(String sourceLine = sourceReader.readLine();
          sourceLine != null; 
          sourceLine = sourceReader.readLine()){
        source.append(sourceLine);
        source.append(Strings.getNl());
      }
      
      sources.put(phaseClassName, source.toString());
    }
    //call the Java compiler
    Javac.loadClasses(sources);
    
    // create the phases
    phases.clear();
    for(Iterator phaseClassNameIter = sources.keySet().iterator();
        phaseClassNameIter.hasNext();){
      String phaseClassName = (String)phaseClassNameIter.next();
      Class phaseClass = Gate.getClassLoader().loadClass(phaseClassName);
      SinglePhaseTransducer phase = (SinglePhaseTransducer) phaseClass.newInstance();
      phase.setInputASName(inputASName);
      phase.setOutputASName(outputASName);
      phase.setOntology(ontology);
      phases.add(phase);
    }
    
    
    
    
    
//    
//    File[] sources = srcDir.listFiles();
//    String[] args = new String[sources.length+5];
//    int count = 0;
//    args[count++] = "-sourcepath";
//    args[count++] = srcDir.getAbsolutePath();
//    args[count++] = "-encoding";
//    args[count++] = encoding;
//    args[count++] = "-d";
//    args[count++] = classesDir.getAbsolutePath();
//
//    for (int i = 0; i < sources.length; i++)
//    {
//      if (!sources[i].getName().equals("phases"))
//        args[count++] = sources[i].getAbsolutePath();
//    }
//
//    if (classesDir.exists())
//    {
//      if (!clearDir(classesDir))
//        throw new GateRuntimeException("Cannot clear " + classesDir.getPath() + " directory!");
//    }
//    else
//    {
//      if (!classesDir.mkdir())
//        throw new GateRuntimeException("Cannot create " + classesDir.getPath() + " directory!");
//    }
//
//    Out.prln("Compiling...");
//    com.sun.tools.javac.Main.compile(args);
  }

  protected void loadAllClasses(File classesDirectory,
                                       String packageName) throws IOException {
    File[] files = classesDirectory.listFiles();
    //adjust the package name
    if(packageName == null)
    {
        //top level directory -> not a package name
        packageName = "";
    }
    else
    {
        //internal directory -> a package name
        packageName += packageName.length() == 0 ?
            classesDirectory.getName() : "." + classesDirectory.getName();
    }

    for(int i = 0; i < files.length; i++)
    {
      if (files[i].isDirectory())
        loadAllClasses(files[i], packageName);
      else
      {
        String filename = files[i].getName();
        if (filename.endsWith(".class"))
        {
          String className = packageName + "." +
                             filename.substring(0, filename.length() - 6);

          if (Gate.getClassLoader().findExistingClass(className) == null)
          {
            //load the class from the file
            byte[] bytes = Files.getByteArray(files[i]);
            Gate.getClassLoader().defineGateClass(className,
                                                  bytes, 0, bytes.length);
          }
        }
      }
    }
  }

  public URL getGrammarURL()
  {
    return grammarURL;
  }

  public void setGrammarURL(URL url)
  {
    grammarURL = url;
  }

  /**
   *
   * Sets the encoding to be used for reding the input file(s) forming the Jape
   * grammar. Note that if the input grammar is a multi-file one than the same
   * encoding will be used for reding all the files. Multi file grammars with
   * different encoding across the composing files are not supported!
   *
   * @param newEncoding
   *          a {link String} representing the encoding.
   */
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }

  /**
   * Gets the encoding used for reding the grammar file(s).
   */
  public String getEncoding() {
    return encoding;
  }

  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }

  public String getInputASName() {
    return inputASName;
  }

  public void setOutputASName(String newOutputASName) {
    outputASName = newOutputASName;
  }

  public String getOutputASName() {
    return outputASName;
  }

  public gate.creole.ontology.Ontology getOntology() {
    return ontology;
  }

  public void setOntology(gate.creole.ontology.Ontology newOntology) {
    this.ontology = newOntology;
  }
}
