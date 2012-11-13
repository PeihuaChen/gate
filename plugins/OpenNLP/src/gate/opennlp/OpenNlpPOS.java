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

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.Utils;
import gate.creole.*;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.log4j.Logger;

/**
 * Wrapper for the OpenNLP POS Tagger
 */
@CreoleResource(name = "OpenNLP POS Tagger", 
    comment = "POS Tagger using an OpenNLP maxent model",
    helpURL = "http://gate.ac.uk/sale/tao/splitch21.html#sec:misc-creole:opennlp")
public class OpenNlpPOS extends AbstractLanguageAnalyser {

  private static final long serialVersionUID = 4010938787910114221L;
  private static final Logger logger = Logger.getLogger(OpenNlpPOS.class);

  
  
  /* CREOLE PARAMETERS & SUCH*/
  private String inputASName, outputASName;
  private URL modelUrl;
  private POSModel model;
  private POSTaggerME tagger;
  private String tokenType = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
  private String sentenceType = ANNIEConstants.SENTENCE_ANNOTATION_TYPE;
  private String stringFeature = ANNIEConstants.TOKEN_STRING_FEATURE_NAME;
  private String posFeature = ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME;


	@Override
	public void execute() throws ExecutionException {
    interrupted = false;
    long startTime = System.currentTimeMillis();
    if(document == null) {
      throw new ExecutionException("No document to process!");
    }
    fireStatusChanged("Running " + this.getName() + " on " + document.getName());
    fireProgressChanged(0);
	  
	  AnnotationSet inputAS = document.getAnnotations(inputASName);
	  AnnotationSet outputAS = document.getAnnotations(outputASName);
	  boolean sameAS = inputAS.equals(outputAS);

		AnnotationSet sentences = inputAS.get(sentenceType);
		int nbrDone = 0;
		int nbrSentences = sentences.size();

		for (Annotation sentence : sentences) {
		  AnnotationSet tokenSet = Utils.getContainedAnnotations(inputAS, sentence, tokenType);
		  Sentence tokens = new Sentence(tokenSet, stringFeature, null);
		  String[] strings = tokens.getStrings();

		  if (strings.length > 0) {
		    /* Run the OpenNLP tagger on this sentence,
		     * then apply the tags. 	   */
		    String[] tags = tagger.tag(strings);
		    
		    for (int i=0 ; i < tags.length ; i++) {
		      if (sameAS) { 
		        // add feature to existing annotation
		        tokens.get(i).getFeatures().put(posFeature, tags[i]);
		      }
		      else { 
		        // new annotation with old features and new one
		        Annotation oldToken = tokens.get(i);
		        long start = oldToken.getStartNode().getOffset();
		        long end   = oldToken.getEndNode().getOffset();
		        FeatureMap fm = Factory.newFeatureMap();
		        fm.putAll(oldToken.getFeatures());
		        fm.put(posFeature, tags[i]);
		        try {
		          outputAS.add(start, end, tokenType, fm);
		        }
		        catch (InvalidOffsetException e) {
		          throw new ExecutionException(e);
		        }
		      }
		    } // for loop applying tags
		  } // if strings is not empty
		  
      if(isInterrupted()) { 
        throw new ExecutionInterruptedException("Execution of " + 
            this.getName() + " has been abruptly interrupted!");
      }
      fireProgressChanged((int)(100 * nbrDone / nbrSentences));
		} // for sentence : sentences
		
    fireProcessFinished();
    fireStatusChanged("Finished " + this.getName() + " on " + document.getName()
        + " in " + NumberFormat.getInstance().format(
            (double)(System.currentTimeMillis() - startTime) / 1000)
        + " seconds!");
	}

	
	@Override
	public Resource init() throws ResourceInstantiationException {
    InputStream modelInput = null;
    try {
      modelInput = modelUrl.openStream();
      this.model = new POSModel(modelInput);
      this.tagger = new POSTaggerME(model);
      logger.info("OpenNLP POS Tagger initialized!");
    }
    catch(IOException e) {
      throw new ResourceInstantiationException(e);
    }
    finally {
      if (modelInput != null) {
        try {
          modelInput.close();
        }
        catch (IOException e) {
          throw new ResourceInstantiationException(e);
        }
      }
    }
    
    super.init();
    return this;
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
      comment = "Output AS for POS-tagged tokens")
  public void setOutputASName(String name) {
    this.outputASName = name;
  }
  
  public String getOutputASName() {
    return this.outputASName;
  }

  
  @CreoleParameter(defaultValue = "models/english/en-pos-maxent.bin",
      comment = "location of the tagger model")
  public void setModel(URL model) {
    this.modelUrl = model;
  }

  public URL getModel() {
    return modelUrl;
  }

}
