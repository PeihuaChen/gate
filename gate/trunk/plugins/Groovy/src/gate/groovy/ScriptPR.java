/*
 *  Copyright (c) 2010, The University of Sheffield.
 *
 *  This file is part of the GATE/Groovy integration layer, and is free
 *  software, released under the terms of the GNU Lesser General Public
 *  Licence, version 2.1 (or any later version).  A copy of this licence
 *  is provided in the file LICENCE in the distribution.
 *
 *  Groovy is developed by The Codehaus, details are available from
 *  http://groovy.codehaus.org
 */


package gate.groovy;

import gate.ProcessingResource;
import gate.Resource;
import gate.creole.*;
import gate.util.*;
import gate.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ScriptException;
import groovy.util.ResourceException;


/**
 * Groovy Script PR.
 * 
 * @author Angus Roberts
 * 
 */
public class ScriptPR extends AbstractLanguageAnalyser
                       implements ProcessingResource {

  /**
   * Groovy script file
   */
  private URL scriptURL;

  /**
   * Groovy script file
   */
  private File scriptFile;

  /**
   * Parameters passed to the Groovy script
   */
  private FeatureMap scriptParams;
 
  /**
   * The Groovy script engine
   */
  private GroovyScriptEngine scriptEngine;

  /**
   * Name of the output annotation set
   */
  private String outputASName;

  /**
   * Name of the input annotation set
   */
  private String inputASName;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    // Create the script engine
    try{
      scriptFile = new File(scriptURL.toURI());
    } catch(URISyntaxException use) {
      throw new ResourceInstantiationException(
        "Problem with path to script file", use);
    }

    if(!scriptFile.exists())
      throw new ResourceInstantiationException(
        "Groovy script file does not exist");
    if(!scriptFile.isFile())
      throw new ResourceInstantiationException(
        "Path given for Groovy script file is not a file");

    try{ 
      scriptEngine= new GroovyScriptEngine(scriptFile.getParent());
    } catch(IOException ioe) {
      throw new ResourceInstantiationException(
        "Problem with Groovy script file", ioe);
    }

    return this;
  }

  public void reInit() throws ResourceInstantiationException {
    init();
  }

  /**
   * Execute method. Runs the groovy script, first passing a set of bindings
   * including the document, the input AnnotationSet and the output
   * AnnotationSet
   */
  public void execute() throws ExecutionException {

    if(document == null) {
      throw new ExecutionException("There is no loaded document");
    }

    AnnotationSet outputAS = null;
    if(outputASName == null || outputASName.trim().length() == 0)
      outputAS = document.getAnnotations();
    else outputAS = document.getAnnotations(outputASName);

    AnnotationSet inputAS = null;
    if(inputASName == null || inputASName.trim().length() == 0)
      inputAS = document.getAnnotations();
    else inputAS = document.getAnnotations(inputASName);

    // Status
    fireStatusChanged("Groovy script PR running on " + document.getSourceUrl());
    fireProgressChanged(0);


    // Create the variable bindings
    Binding binding = new Binding();
    binding.setVariable("doc", document);
    binding.setVariable("content", document.getContent().toString());
    binding.setVariable("inputAS", inputAS);
    binding.setVariable("outputAS", outputAS);
    binding.setVariable("gate", Gate.class);
    binding.setVariable("factory", gate.Factory.class);

    // The FeatureMap is passed in its entirety, making the keys available in
    // a bean-like way. So in a map with k=v, the script can say 
    // assert scriptParams.k == v
    binding.setVariable("scriptParams", scriptParams);

    // Run the script engine
    try {
      scriptEngine.run(scriptFile.getName(), binding);
    } catch(ResourceException re) {
      throw new ExecutionException("Problem accessing Groovy script", re);
    } catch(ScriptException se) {
      throw new ExecutionException("Problem parsing Groovy script", se);
    }

    // We've done
    fireProgressChanged(100);	
    fireProcessFinished();
    fireStatusChanged( "Groovy script PR finished" );

  }

  /**
   * gets name of the output annotation set
   * @return
   */
  public String getOutputASName() {
    return outputASName;
  }

  /**
   * sets name of the output annotaiton set
   * @param outputAS
   */
  public void setOutputASName(String outputAS) {
    this.outputASName = outputAS;
  }

  /**
   * gets name of the input annotation set
   * @return
   */
  public String getInputASName() {
    return inputASName;
  }

  /**
   * sets name of the input annotaiton set
   * @param inputAS
   */
  public void setInputASName(String inputAS) {
    this.inputASName = inputAS;
  }

  /**
   * gets URL of the Groovy script
   * @return
   */
  public URL getScriptURL() {
    return scriptURL;
  }

  /**
   * sets File of the Groovy script
   * @param script
   */
  public void setScriptURL(URL script) {
    this.scriptURL = script;
  }


  /**
   * Get Map of parameters for the Groovy script
   * @return
   */
  public FeatureMap getScriptParams() {
    return scriptParams;
  }

  /**
   * Set Map of parameters for the Groovy script
   * @return
   */
  public void setScriptParams(FeatureMap params) {
    this.scriptParams = params;
  }



}
