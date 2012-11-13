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

import gate.*;
import gate.creole.*;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import java.io.*;
import java.net.URL;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.apache.log4j.Logger;

/**
 * Wrapper PR for the OpenNLP chunker.
 */
@CreoleResource(name = "OpenNLP Chunker", 
    comment = "Chunker using an OpenNLP maxent model",
    helpURL = "http://gate.ac.uk/sale/tao/splitch21.html#sec:misc-creole:opennlp")
public class OpenNlpChunker extends AbstractLanguageAnalyser {

  private static final long serialVersionUID = 3254481728303447340L;
  private static final Logger logger = Logger.getLogger(OpenNlpChunker.class);

  
  /* CREOLE PARAMETERS & SUCH*/
  private String inputASName, outputASName, chunkFeature;
  private URL modelUrl;
  private ChunkerModel model;
  private ChunkerME chunker;
  private String tokenType = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
  private String sentenceType = ANNIEConstants.SENTENCE_ANNOTATION_TYPE;
  private String posFeature = ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME;
  private String stringFeature = ANNIEConstants.TOKEN_STRING_FEATURE_NAME;



	@Override
	public void execute() throws ExecutionException {
	  AnnotationSet inputAS = document.getAnnotations(inputASName);
	  AnnotationSet outputAS = document.getAnnotations(outputASName);
	  boolean sameAS = inputAS.equals(outputAS);

		AnnotationSet sentences = inputAS.get(sentenceType);
		for (Annotation sentence : sentences)  {
		  AnnotationSet tokenSet = Utils.getContainedAnnotations(inputAS, sentence, tokenType);
      Sentence tokens = new Sentence(tokenSet, stringFeature, posFeature);
      String[] strings = tokens.getStrings();
      String[] posTags = tokens.getTags();

      String[] chunkTags = chunker.chunk(strings, posTags);
      
      
      
      for (int i=0 ; i < chunkTags.length ; i++) {
        
        if (sameAS) { 
          // add feature to existing annotation
          tokens.get(i).getFeatures().put(chunkFeature, chunkTags[i]);
        }
        else { 
          // new annotation with old features and new one
          Annotation oldToken = tokens.get(i);
          long start = oldToken.getStartNode().getOffset();
          long end   = oldToken.getEndNode().getOffset();
          FeatureMap fm = Factory.newFeatureMap();
          fm.putAll(oldToken.getFeatures());
          fm.put(chunkFeature, chunkTags[i]);
          try {
            outputAS.add(start, end, tokenType, fm);
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
    InputStream modelInput = null;
    try {
      modelInput = modelUrl.openStream();
      this.model = new ChunkerModel(modelInput);
      this.chunker = new ChunkerME(model);
      logger.info("OpenNLP POS Chunker initialized!");
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

	
	
 /* CREOLE PARAMETERS */
  
  @RunTime
  @CreoleParameter(defaultValue = "",
      comment = "annotation set containing tokens and sentences")
  public void setInputASName(String name) {
    this.inputASName = name;
  }
  
  public String getInputASName() {
    return this.inputASName;
  }

  @RunTime
  @CreoleParameter(defaultValue = "",
      comment = "annotation set for tagged tokens")
  public void setOutputASName(String name) {
    this.outputASName = name;
  }
  
  public String getOutputASName() {
    return this.outputASName;
  }

  
  @RunTime
  @CreoleParameter(defaultValue = "chunk",
      comment = "feature for chunk tags")
  public void setChunkFeature(String name) {
    this.chunkFeature = name;
  }
  
  public String getChunkFeature() {
    return this.chunkFeature;
  }

  
  @CreoleParameter(defaultValue = "models/english/en-chunker.bin",
      comment = "location of the tagger model")
  public void setModel(URL model) {
    this.modelUrl = model;
  }

  public URL getModel() {
    return modelUrl;
  }

}
