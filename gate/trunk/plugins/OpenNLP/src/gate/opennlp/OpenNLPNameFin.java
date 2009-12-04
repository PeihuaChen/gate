package gate.opennlp;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.namefind.NameFinderME;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

/**
 * Wrapper for the opennlp namefinder
 * @author <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * Created: Thu Dec 11 16:25:59 EET 2008
 */

public @SuppressWarnings("all") class OpenNLPNameFin extends AbstractLanguageAnalyser {

	public static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(OpenNLPNameFin.class);

	// private members
	private String inputASName = null;
	private String outputASName = null;
	NameFinderME namefin = null;
	URL model;
	private List<NameFinder> finder;

	@Override
	public void execute() throws ExecutionException {
		// text doc annotations
		AnnotationSet annotations;
		if (inputASName != null && inputASName.length() > 0)
			annotations = document.getAnnotations(inputASName);
		else
			annotations = document.getAnnotations();

		AnnotationSet outputAnnots;
		if (outputASName != null && outputASName.length() > 0)
			outputAnnots = document.getAnnotations(outputASName);
		else
			outputAnnots = document.getAnnotations();

		// get sentence annotations
		//AnnotationSet sentences = document.getAnnotations("Sentence");

		// getdoc.get text
		String text = document.getContent().toString();
		
		//iterate on sentences
		
		gate.AnnotationSet sentences = annotations.get("Sentence");
		
		for (Annotation sentence : sentences) {
			
			//interate on tokens
			
			java.util.List<Annotation> tokenslist = new java.util.LinkedList<gate.Annotation>(annotations.get("Token", sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()));
			
			//sort them
			Collections.sort(tokenslist, new OffsetComparator());
			//make the string list
			String[] tokens = new String[tokenslist.size()];
			int k = 0;
			for (Annotation token : tokenslist) {
				tokens[k] = (String)token.getFeatures().get("string");
				k++;
				
			}
			// run tokenizer
//			Span[] spans = namefin.find(tokens);
			// compare the resulting
			// spans and add annotations
			
			for (Iterator iterator = finder.iterator(); iterator.hasNext();) {
				NameFinder tagger = (NameFinder) iterator.next();
				String[] off = tagger.model.find(tokens, Collections.EMPTY_MAP);
				for (int i = 0; i < off.length; i++) {
					if (!off[i].equals("other")) {
						///here is the logic to add annotation to gate
						
						
						
						FeatureMap fm = Factory.newFeatureMap();
						// type
						fm.put("source", "openNLP");
						//ok then add to string buff all the string to make shure we got all the span
//						fm.put("string", text.substring(spans[i].getStart(), spans[i]
//								.getEnd()));
//						fm.put("string", tokenslist.get(spans[i].getStart()+1).getStartNode().getOffset(), 
//								tokenslist.get(spans[i].getEnd()).getEndNode().getOffset());
						// source
						fm.put("type", tagger.name);
						
						//iterate until got the final span
						int start = i;
						while(i + 1 < off.length && off[i+1].equals("cont")){
							//while(off[++i].equals("cont"))
							i++;
						}

						try {
//							annotations.add(Long.valueOf(tokens[start].get()), Long
//									.valueOf(spans[i].getEnd()), "Name", fm);
							
							outputAnnots.add(tokenslist.get(start).getStartNode().getOffset(), 
									tokenslist.get(i).getEndNode().getOffset(), "Name", fm);

						} catch (InvalidOffsetException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
						
					}
//						buf.append(in[i] + " ");
//						continue;
					}
				}

//			for (int i = 0; i < spans.length; i++) {
//
//
//
//				//startSpan = spans[i].getEnd() + countSpaces(spans[i].getEnd());
//			}
			
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
//		
//		logger.info("current path is: " + System.getProperty("user.dir"));
//		logger.info("current path is: " + System.getProperty("user.home"));
		// add a trailing slash to the model dir if necessary
		try {
			URL modelDir = model;
			if(modelDir.toExternalForm().endsWith("/"))
				modelDir = new URL(modelDir.toExternalForm() + "/");
			List<String> models = new LinkedList<String>();
			models.add("person.bin.gz");
			models.add("money.bin.gz");
			models.add("time.bin.gz");
			models.add("percentage.bin.gz");
			models.add("location.bin.gz");
			models.add("date.bin.gz");
			models.add("organization.bin.gz");

			finder = new LinkedList<NameFinder>();
			for (Iterator iterator = models.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				logger.info("Initialized: "+ string);
				finder.add(new NameFinder(new URL(modelDir, string)));
			}
			
			logger.warn("OpenNLP Name Finder initialized!");
			
			return this;
		}
		catch(MalformedURLException e) {
			throw new ResourceInstantiationException(
				"OpenNLP Name Finder could not be initialized",
				e);
		}

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
	public static MaxentModel getModel(URL name) {
		try {
			return new BinaryGISModelReader(new DataInputStream(
					new GZIPInputStream(name.openStream()))).getModel();
		} catch (IOException E) {
			E.printStackTrace();
			logger.error("OpenNLP NameFinder can not be initialized!");
			throw new RuntimeException("OpenNLP NameFinder can not be initialized!", E);
		}
	}
	
	private class NameFinder {
		String name;
		NameFinderME model;

		public NameFinder(URL url) {
			String path = url.toExternalForm();
			try{
			name = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".", path.lastIndexOf("/") + 1));
			}catch (Exception e){
				name = path;
			}
			model = new NameFinderME(OpenNLPNameFin.getModel(url));
		}

	}

	/* getters and setters for the PR */
	/* public members */
	public void setInputASName(String a) {
		inputASName = a;
	}

	public String getInputASName() {
		return inputASName;
	}

	public void setOutputASName(String a) {
		outputASName = a;
	}

	public String getOutputASName() {
		return outputASName;
	}

	public URL getModel() {
		return model;
	}

	public void setModel(URL model) {

		this.model = model;
	}/* getters and setters for the PR */

}
