/*
 * BulStemPR.java
 * 
 * 
 * Copyright (c) 2010,2011 The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June1991.
 * 
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 * Ivelina Nikolova, 05/12/2013
 */
package gate.bulstem;

import gate.Annotation;
import gate.AnnotationSet;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stemming algorithm by Preslav Nakov.
 * 
 * @author Alexander Alexandrov, e-mail: sencko@mail.bg, provided the JAVA
 *         implementation of the algorithm
 * @author Ivelina Nikolova, e-mail:iva@lml.bas.bg, wrapped the stemmer for GATE
 * @since 2013-12-05
 */
@CreoleResource(name = "Stemmer BulStem", helpURL = "http://lml.bas.bg/~nakov/bulstem/", comment = "This plugin is an implementation of the BulStem stemmer algorithm for Bulgarian developed by Preslav Nakov.")
public class BulStemPR extends AbstractLanguageAnalyser implements
  ProcessingResource, Serializable {

  private URL rulesURL;

  public Hashtable stemmingRules = new Hashtable();

  public int STEM_BOUNDARY = 1;

  public static Pattern vocals = Pattern.compile("[^аъоуеиюя]*[аъоуеиюя]");

  public static Pattern p = Pattern
    .compile("([а-я]+)\\s==>\\s([а-я]+)\\s([0-9]+)");

  // Exit gracefully if exception caught on init()
  private boolean gracefulExit;

  @Override
  public Resource init() throws ResourceInstantiationException {
    // check required parameters are set
    if(rulesURL == null) {
      // throw new
      // ResourceInstantiationException("outputMode parameter must be set");
      gate.util.Err.println("rulesURL parameter must be set");
      gracefulExit = true;
    }

    return this;

  }

  /* Set gracefulExit flag and clean up */
  private void gracefulExit(String msg) {
    gate.util.Err.println(msg);
    cleanup();
    fireProcessFinished();
  }

  @Override
  public void execute() throws ExecutionException {
    // check required parameters are set
    if(rulesURL == null) {
      // throw new
      // ResourceInstantiationException("outputMode parameter must be set");
      gracefulExit("rulesURL parameter must be set in BulStem PR");
      return;
    }

    try {
      loadStemmingRules(this.rulesURL.getPath());
    } catch(URISyntaxException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch(Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // Just process the entire document
    // String docText = document.getContent().toString();
    AnnotationSet allTokens = document.getAnnotations().get("Token");
    try {
      // System.out.println("bustem works");
      this.processWithBulstem(allTokens);
    } catch(Exception e) {
      gracefulExit(e.getMessage());
    }
  }

  private void processWithBulstem(AnnotationSet allTokens) {
    // TODO Auto-generated method stub
    for(Annotation token : allTokens) {
      String tokenString = token.getFeatures().get("string").toString();
      String stem = stem(tokenString).toLowerCase();
      token.getFeatures().put("stem", stem);
    }

  }

  public void loadStemmingRules(String fileName) throws Exception {
    stemmingRules.clear();
    FileInputStream fis = new FileInputStream(fileName);
    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
    String s = null;
    while((s = br.readLine()) != null) {
      Matcher m = p.matcher(s);
      if(m.matches()) {
        int j = m.groupCount();
        if(j == 3) {
          if(Integer.parseInt(m.group(3)) > STEM_BOUNDARY) {
            stemmingRules.put(m.group(1), m.group(2));
          }
        }
      }
    }
  }

  public String stem(String word) {
    Matcher m = vocals.matcher(word);
    if(!m.lookingAt()) { return word; }
    for(int i = m.end() + 1; i < word.length(); i++) {
      String suffix = word.substring(i);
      if((suffix = (String)stemmingRules.get(suffix)) != null) { return word
        .substring(0, i) + suffix; }
    }
    return word;
  }

  // PR parameters
  @Optional
  @RunTime
  @CreoleParameter(comment = "Path to rules", defaultValue = "resources/stem_rules_context_2_UTF-8.txt")
  public void setPathToRules(URL rulesURL) {
    this.rulesURL = rulesURL;
  }

  public URL getPathToRules() {
    return rulesURL;
  }

} // class MetaMapPR

