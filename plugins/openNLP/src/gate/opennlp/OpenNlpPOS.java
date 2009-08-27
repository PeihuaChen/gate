package gate.opennlp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Span;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;


/**
 * Wrapper for the open nlp pos tagger
 * @author <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * Created: Thu Dec 11 16:25:59 EET 2008
 */

public @SuppressWarnings("all") class OpenNlpPOS extends AbstractLanguageAnalyser {

	public static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(OpenNlpPOS.class);

	// private members
	private String inputASName = null;
	POSTaggerME pos = null;
	URL model;
	URL dictionary;
	
	
	@Override
	public void execute() throws ExecutionException {
		// text doc annotations
		AnnotationSet annotations;
		if (inputASName != null && inputASName.length() > 0)
			annotations = document.getAnnotations(inputASName);
		else
			annotations = document.getAnnotations();

		// getdoc.get text
		String text = document.getContent().toString();
		
		// get sentence annotations
		 AnnotationSet sentences = document.getAnnotations().get("Sentence");
		 
			
		//order sentences
		
		List<Annotation> sentList = new LinkedList<Annotation>();
		
		for (Iterator iterator = sentences.iterator(); iterator.hasNext();) {
			sentList.add( (Annotation) iterator.next());
			
		}
		
		java.util.Collections.sort(sentList, new gate.util.OffsetComparator());
		
		// for each sentence get token annotations
		 for (Iterator iterator = sentList.iterator(); iterator.hasNext();) {
			Annotation annotation = (Annotation) iterator.next();
			
			AnnotationSet sentenceTokens = document.getAnnotations().get("Token", 
					annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
			
			//create a list
			
			List<Annotation> tokenList = new LinkedList<Annotation>();
			
			for (Iterator iterator2 = sentenceTokens.iterator(); iterator2
					.hasNext();) {
				tokenList.add((Annotation) iterator2.next());
				
			}
			
			//order on offset
			
			Collections.sort(tokenList, new gate.util.OffsetComparator());
			
			//make the array be string[] sentence
			String[] sentence = new String[tokenList.size()];
			int i = 0;
			for (Iterator iterator2 = tokenList.iterator(); iterator2
					.hasNext();) {
				
				Annotation token = (Annotation) iterator2.next();
				
				sentence[i] = token.getFeatures().get("string").toString().replaceAll("\\s+", "").trim();
				
				i++;
			}
			
			StringBuffer buf = new StringBuffer();
			for (int j = 0; j < sentence.length; j++) {
				buf.append(sentence[j]+ "@@@");
			}
			
			//run pos tagger
			String[] postags = null;
			/**
			 * we will make shure to not 
			 * allow smth to breack the tagger
			 */
			try{
			postags = pos.tag(sentence);
			}catch (Exception e){
				e.printStackTrace();
				System.out.println("There is a problem....\n with this sentence");
				System.out.println(buf);
				continue;
			}
			
			//add tohose spans to token annotations
			
			int j = 0;
			for (Iterator iterator2 = tokenList.iterator(); iterator2
					.hasNext();) {
				Annotation token = (Annotation) iterator2.next();
				
				FeatureMap fm = token.getFeatures();
				fm.put("pos", postags[j]);
				
				token.setFeatures(fm);
				
				j++;
				
			}
		}
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		logger.warn("OpenNLP POS initializing strings are: model - " + model.getFile() + 
				" dictionary: "+dictionary.getFile());
		try {
			pos = new POSTaggerME(getModel(new File(model.getFile())), new POSDictionary(
					dictionary.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("OpenNLP POS can not be initialized!");
			throw new RuntimeException("OpenNLP POS can not be initialized!", e);
		}
		logger.warn("OpenNLP POS initialized!");//System.out.println("OpenNLP POS initialized!");
		return this;

	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	/**
	 * @author georgiev
	 * @return MaxentModel
	 * @param String
	 *            path to MaxentModel
	 */
	public static MaxentModel getModel(File name) {
		try {
			return new BinaryGISModelReader(new DataInputStream(
					new GZIPInputStream(new FileInputStream(name)))).getModel();
		} catch (IOException E) {
			E.printStackTrace();
			return null;
		}
	}

	/* getters and setters for the PR */
	/* public members */
	
	
	
	public void setInputASName(String a) {
		inputASName = a;
	}

	public String getInputASName() {
		return inputASName;
	}/* getters and setters for the PR */

	public URL getModel() {
		return model;
	}

	public void setModel(URL model) {
		this.model = model;
	}

	public URL getDictionary() {
		return dictionary;
	}

	public void setDictionary(URL dictionary) {
		this.dictionary = dictionary;
	}

}
