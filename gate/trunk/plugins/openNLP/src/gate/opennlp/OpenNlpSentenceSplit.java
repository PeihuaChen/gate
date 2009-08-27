package gate.opennlp;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.sentdetect.SentenceDetectorME;

/**
 * Wrapper for the open nlp sentence splitter
 * @author <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * Created: Thu Dec 11 16:25:59 EET 2008
 */

public @SuppressWarnings("all") class OpenNlpSentenceSplit extends AbstractLanguageAnalyser {

	public static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(OpenNlpSentenceSplit.class);

	// private members
	private String inputASName = null;
	SentenceDetectorME splitter = null;
	URL model;

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
		// run tokenizer
		int[] spans = splitter.sentPosDetect(text);
		// compare the resulting
		// sentences and add annotations
		int prevSpan = 0;
		for (int i = 0; i < spans.length; i++) {

			FeatureMap fm = Factory.newFeatureMap();
			// type
			fm.put("source", "openNLP");
			// source
			fm.put("type", "urn:lsid:ontotext.com:kim:iextraction:Sentence");

			try {
				// annotations.add(Long.valueOf(spans[i].getStart()),
				// Long.valueOf(spans[i].getEnd()), "Sentence", fm);
				annotations.add(i == 0 ? Long.valueOf(prevSpan) : Long
						.valueOf(prevSpan + countSpaces(prevSpan - 1)),
						i == (spans.length-1) ? Long.valueOf(spans[i]) : Long
								.valueOf(spans[i] - 1), "Sentence", fm);
			} catch (InvalidOffsetException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			prevSpan = spans[i];
		}
	}

	int countSpaces(int lastSpan) {

		int ws = 0;
		String text = document.getContent().toString();

		char[] context = text.substring(lastSpan - 1,
				text.length() >= lastSpan + 50 ? lastSpan + 50 : text.length())
				.toCharArray();

		for (int i = 0; i < context.length; i++) {
			if (Character.isWhitespace(context[i]))
				ws++;
			else
				break;
		}

		return ws;
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		logger.info("Sentence split url is: " + model.getFile());
		try{
		splitter = new SentenceDetectorME(
				getModel(new File(model.getFile())));
		}catch (Exception e){
			logger.error("Sentence Splitter can not be initialized!");
			throw new RuntimeException("Sentence Splitter cannot be initialized!", e);
		}
		
		logger.warn("Sentence split initialized!");//System.out.println("Sentence split initialized!");

		return this;

	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	/**
	 * @author joro
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

}
