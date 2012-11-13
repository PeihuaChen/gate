/*
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.opennlp;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.apache.log4j.Logger;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;


/**
 * Wrapper for the OpenNLP NameFinder
 */
@CreoleResource(name = "OpenNLP NER", 
    comment = "NER PR using a set of OpenNLP maxent models",
    helpURL = "http://gate.ac.uk/sale/tao/splitch21.html#sec:misc-creole:opennlp")
public class OpenNLPNameFin extends AbstractLanguageAnalyser {
	
  private static final long serialVersionUID = -5507338627058320125L;
  private static final Logger logger = Logger.getLogger(OpenNLPNameFin.class);
	
	
  /* CREOLE PARAMETERS & SUCH*/
  private String inputASName, outputASName;
  private URL configUrl;
  private Map<NameFinderME, String> finders;

  private String tokenType = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
  private String sentenceType = ANNIEConstants.SENTENCE_ANNOTATION_TYPE;
  private String posFeature = ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME;
  private String stringFeature = ANNIEConstants.TOKEN_STRING_FEATURE_NAME;


	@Override
	public void execute() throws ExecutionException {
	  AnnotationSet inputAS = document.getAnnotations(inputASName);
	  AnnotationSet outputAS = document.getAnnotations(outputASName);
	  AnnotationSet sentences = inputAS.get(sentenceType);
		
		for (Annotation sentence : sentences) {
		  /* For each input Sentence annotation, produce a list of
		   * Token.string values and the data structure for translating
		   * offsets.		   */
		  AnnotationSet tokenSet = Utils.getContainedAnnotations(inputAS, sentence, tokenType);
		  Sentence tokens = new Sentence(tokenSet, stringFeature, posFeature);
		  String[] strings = tokens.getStrings();
		  
		  // Run each NameFinder over the sentence
		  for (NameFinderME finder : finders.keySet()) {
		    String type = finders.get(finder);
		    Span[] spans = finder.find(strings);

		    for (Span span : spans) {
		      // Translate the offsets and create the output NE annotation
		      long start = tokens.getStartOffset(span);
		      long end = tokens.getEndOffset(span);
		      FeatureMap fm = Factory.newFeatureMap();
		      fm.put("source", "OpenNLP");
		      try {
		        outputAS.add(start, end, type, fm);
		      }
		      catch (InvalidOffsetException e) {
		        throw new ExecutionException(e);
		      }
		    }
		  }
		}
	}

	
	@Override
	public Resource init() throws ResourceInstantiationException {
	  try {
	    loadModels(this.configUrl);
	  }
	  catch (IOException e) {
	    throw new ResourceInstantiationException(e);
	  }
    super.init();
    return this;
	}

	
	private void loadModels(URL configUrl) throws IOException  {
	  this.finders = new HashMap<NameFinderME, String>();
	  Properties properties = new Properties();
	  InputStream configInput = null;
	  try {
	    configInput = configUrl.openStream();
	    properties.load(configInput);
	  }
	  finally {
	    if (configInput != null) {
	      configInput.close();
	    }
	  }
	  
	  Set<String> modelFiles = properties.stringPropertyNames();
	  for (String filename : modelFiles) {
	    InputStream modelInput = null;
	    try {
	      URL modelUrl = new URL(configUrl, filename);
	      String type = properties.getProperty(filename);
	      modelInput = modelUrl.openStream();
	      TokenNameFinderModel model = new TokenNameFinderModel(modelInput);
	      NameFinderME finder = new NameFinderME(model);
	      this.finders.put(finder, type);
	      logger.info("OpenNLP NameFinder: " + modelUrl.toString() + " -> " + type);
	    }
	    finally {
	      if (modelInput != null) {
	        modelInput.close();
	      }
	    }
	  }
	}
	
	
	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	
 /* CREOLE PARAMETERS */
  
  @RunTime
  @CreoleParameter(defaultValue = "",
      comment = "Input AS with Token and Sentence annotations")
  public void setInputASName(String name) {
    this.inputASName = name;
  }
  
  public String getInputASName() {
    return this.inputASName;
  }

  @RunTime
  @CreoleParameter(defaultValue = "",
      comment = "Output AS for named entities")
  public void setOutputASName(String name) {
    this.outputASName = name;
  }
  
  public String getOutputASName() {
    return this.outputASName;
  }

  
  @CreoleParameter(defaultValue = "models/english/en-ner.conf",
      comment = "config file for the NER models")
  public void setConfig(URL model) {
    this.configUrl = model;
  }

  public URL getConfig() {
    return configUrl;
  }

}
