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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.apache.log4j.Logger;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

/**
 * Wrapper PR for the OpenNLP tokenizer.
 */
@CreoleResource(name = "OpenNLP Tokenizer", 
    comment = "Tokenizer using an OpenNLP maxent model",
    helpURL = "http://gate.ac.uk/sale/tao/splitch21.html#sec:misc-creole:opennlp")
public class OpenNlpTokenizer extends AbstractLanguageAnalyser {

  private static final long serialVersionUID = 6965074842061250720L;
  private static final Logger logger = Logger.getLogger(OpenNlpTokenizer.class);

  
	/* CREOLE PARAMETERS & SUCH*/
	private String annotationSetName = null;
	private TokenizerME tokenizer = null;
	private TokenizerModel model = null;
	private URL modelUrl;
  private String tokenType = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
  private String spaceTokenType = ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE;
  private String stringFeature = ANNIEConstants.TOKEN_STRING_FEATURE_NAME;
  
	

	public void execute() throws ExecutionException {
		AnnotationSet annotations = document.getAnnotations(annotationSetName);
		String text = document.getContent().toString();
		Span[] spans = tokenizer.tokenizePos(text);
		
		/* The spans ought to be ordered, but the OpenNLP
		 * API is unclear.	We need them in order so we can 
		 * spot the gaps and put SpaceToken annotations
		 * in them.	 */
		Arrays.sort(spans);
		long previousEnd = 0;
		
		for (Span span : spans) {
      long start = (long) span.getStart();
      long end   = (long) span.getEnd();

      if (start > previousEnd) {
        FeatureMap sfm = Factory.newFeatureMap();
        sfm.put("source", "OpenNLP");
        try {
          annotations.add(previousEnd, start, spaceTokenType, sfm);
        }
        catch (InvalidOffsetException e) {
          throw new ExecutionException(e);
        }
      }
      
      previousEnd = end;
      
			FeatureMap fm = Factory.newFeatureMap();
			fm.put("source", "OpenNLP");
			fm.put(stringFeature, text.substring(span.getStart(), span.getEnd()));
			try {
				annotations.add(start, end, tokenType, fm);
			} 
			catch (InvalidOffsetException e) {
				throw new ExecutionException(e);
			}
		}
	}


	public Resource init() throws ResourceInstantiationException {
	  InputStream modelInput = null;
    try {
      modelInput = modelUrl.openStream();
      this.model = new TokenizerModel(modelInput);
      this.tokenizer = new TokenizerME(model);
      logger.info("OpenNLP Tokenizer initialized!");
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


	public void reInit() throws ResourceInstantiationException {
		init();
	}


	/* CREOLE PARAMETERS */

	@RunTime
	@CreoleParameter(defaultValue = "",
	    comment = "Output AS for Tokens")
	public void setAnnotationSetName(String a) {
		annotationSetName = a;
	}

	public String getAnnotationSetName() {
		return annotationSetName;
	}
	
	
	@CreoleParameter(defaultValue = "models/english/en-token.bin",
	    comment = "location of the tokenizer model")
  public void setModel(URL model) {
    this.modelUrl = model;
  }
  
	public URL getModel() {
		return modelUrl;
	}

}
