/*
 * GENIASentenceSplitter
 * 
 * Copyright (c) 2011, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 3, June 2007
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 01/06/2011
 */
package gate.creole.genia.splitter;

import gate.AnnotationSet;
import gate.Factory;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.Files;
import gate.util.ProcessManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;

@CreoleResource(name = "GENIA Sentence Splitter", icon = "sentence-splitter.png")
public class GENIASentenceSplitter extends AbstractLanguageAnalyser {

  private boolean debug = false;

  private String annotationSetName;

  private URL splitterBinary;

  private ProcessManager manager = new ProcessManager();

  public Boolean getDebug() {
    return debug;
  }

  @RunTime
  @CreoleParameter(defaultValue = "false")
  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public URL getSplitterBinary() {
    return splitterBinary;
  }

  @RunTime
  @CreoleParameter()
  public void setSplitterBinary(URL splitterBinary) {
    this.splitterBinary = splitterBinary;
  }

  public String getAnnotationSetName() {
    return annotationSetName;
  }

  @RunTime
  @Optional
  @CreoleParameter()
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  public void execute() throws ExecutionException {
    AnnotationSet annotationSet = document.getAnnotations(annotationSetName);

    File splitter = Files.fileFromURL(splitterBinary);

    String docContent =
            document.getContent().toString().replace((char)160, ' ');

    try {
      File tmpIn = File.createTempFile("GENIA", ".txt");
      File tmpOut = File.createTempFile("GENIA", ".txt");

      FileOutputStream fos = new FileOutputStream(tmpIn);
      fos.write(docContent.getBytes("utf8"));
      fos.close();

      String[] args =
              new String[]{splitter.getAbsolutePath(), tmpIn.getAbsolutePath(),
                  tmpOut.getAbsolutePath()};

      manager.runProcess(args, splitter.getParentFile(), (debug ? System.out : null), (debug ? System.err : null));

      int end = 0;

      BufferedReader in = new BufferedReader(new FileReader(tmpOut));
      String sentence = in.readLine();
      while(sentence != null) {

        sentence = sentence.trim();

        int start = docContent.indexOf(sentence, end);

        end = start + sentence.length();

        if(end > start && sentence.length() > 0) {
          annotationSet.add((long)start, (long)end, "Sentence",
                  Factory.newFeatureMap());
        }

        sentence = in.readLine();
      }
      
      if (!debug && !tmpIn.delete()) tmpIn.deleteOnExit();
      if (!debug && !tmpOut.delete()) tmpOut.deleteOnExit();

    } catch(Exception ioe) {
      throw new ExecutionException("An error occured running the splitter", ioe);
    }
  }
}
