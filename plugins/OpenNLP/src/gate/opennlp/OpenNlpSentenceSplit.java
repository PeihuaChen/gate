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
import opennlp.tools.sentdetect.*;
import opennlp.tools.util.Span;
import org.apache.log4j.Logger;


/**
 * Wrapper PR for the OpenNLP sentence splitter.
 */
@CreoleResource(name = "OpenNLP Sentence Splitter", 
    comment = "Sentence splitter using an OpenNLP maxent model",
    helpURL = "http://gate.ac.uk/sale/tao/splitch21.html#sec:misc-creole:opennlp")
public class OpenNlpSentenceSplit extends AbstractLanguageAnalyser {

  
  private static final long serialVersionUID = 3833973991517701119L;
  private static final Logger logger = Logger.getLogger(OpenNlpSentenceSplit.class);

  
  /* CREOLE PARAMETERS & SUCH*/
  private String annotationSetName = null;
  private SentenceDetectorME splitter = null;
  private SentenceModel model = null;
  private URL modelUrl;
  private String sentenceType = ANNIEConstants.SENTENCE_ANNOTATION_TYPE;


	@Override
	public void execute() throws ExecutionException {
		AnnotationSet annotations = document.getAnnotations(annotationSetName);
		String text = document.getContent().toString();
		Span[] spans = splitter.sentPosDetect(text);

		for (Span span : spans) {
			FeatureMap fm = Factory.newFeatureMap();
			fm.put("source", "OpenNLP");
      long start = (long) span.getStart();
      long end   = (long) span.getEnd();

			try {
				annotations.add(start, end, sentenceType, fm);
			} 
			catch (InvalidOffsetException e) {
				throw new ExecutionException(e);
			}
		}
		
		//TODO: generate Split annotations!
	}


	@Override
	public Resource init() throws ResourceInstantiationException {
	   InputStream modelInput = null;
	    try {
	      modelInput = modelUrl.openStream();
	      this.model = new SentenceModel(modelInput);
	      this.splitter = new SentenceDetectorME(model);
	      logger.info("OpenNLP Splitter initialized!");
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
      comment = "annotation set for Sentences")
  public void setAnnotationSetName(String a) {
    annotationSetName = a;
  }

  public String getAnnotationSetName() {
    return annotationSetName;
  }
  
  @CreoleParameter(defaultValue = "models/english/en-sent.bin",
      comment = "location of the splitter model")
  public void setModel(URL model) {
    this.modelUrl = model;
  }

  public URL getModel() {
    return modelUrl;
  }

}
