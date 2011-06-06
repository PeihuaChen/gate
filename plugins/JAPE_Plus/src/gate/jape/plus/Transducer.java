/*
 *  Tansducer.java
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 17 Aug 2009
 *
 *  $Id$ 
 */
package gate.jape.plus;

import gate.*;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.StatusListener;
import gate.gui.MainFrame;
import gate.jape.MultiPhaseTransducer;
import gate.jape.parser.ParseException;
import gate.util.GateException;
import gate.jape.DefaultActionContext;
import gate.creole.ControllerAwarePR;
import gate.creole.ontology.Ontology;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.ontotext.jape.pda.ParseCpslPDA;
import com.ontotext.jape.pda.SinglePhaseTransducerPDA;

/**
 * A JAPE-Plus transducer (with a {@link LanguageAnalyser} interface.
 */
@CreoleResource(name = "JAPE-PDA-Plus Transducer", 
    comment = "An optimised, JAPE-compatible transducer.")
public class Transducer
  extends AbstractLanguageAnalyser
  implements ControllerAwarePR
{

  /**
   * A comparator for annotations based on start offset and inverse length.
   */
  protected class AnnotationComparator implements Comparator<Annotation>{

    public int compare(Annotation a0, Annotation a1) {
      long start0 = a0.getStartNode().getOffset();
      long start1 = a1.getStartNode().getOffset();
      if(start0 < start1) {
        return -1;
      } else if(start0 > start1) {
        return 1;
      } else {
        long end0 = a0.getEndNode().getOffset();
        long end1 = a1.getEndNode().getOffset();
        if(end0 > end1) {
          return -1;
        } else if(end0 < end1) { return 1; }
        return 0;
      }
    }
    
  }
  
  
  /**
   * A listener for the input annotation set, which invalidates the pre-built
   * lists of sorted annotation when they change due to the execution of one of
   * the phases.
   */
  protected class AnnSetListener implements AnnotationSetListener{

    /* (non-Javadoc)
     * @see gate.event.AnnotationSetListener#annotationAdded(gate.event.AnnotationSetEvent)
     */
    @Override
    public void annotationAdded(AnnotationSetEvent e) {
      changedTypes.add(e.getAnnotation().getType());
    }

    /* (non-Javadoc)
     * @see gate.event.AnnotationSetListener#annotationRemoved(gate.event.AnnotationSetEvent)
     */
    @Override
    public void annotationRemoved(AnnotationSetEvent e) {
      changedTypes.add(e.getAnnotation().getType());
    }
  }
  
  public URL getSourceURL() {
    return sourceURL;
  }

  @CreoleParameter(
      comment="URL for the data from which this transducer should be built.")
  public void setSourceURL(URL source) {
    this.sourceURL = source;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  @CreoleParameter(
          comment="The type of input used to create this transducer.", 
          defaultValue="JAPE")
  public void setSourceType(SourceType sourceType) {
    this.sourceType = sourceType;
  }

  /**
   * The source from which this transducer is created.
   */
  protected URL sourceURL;
  
  protected String encoding;
  
  protected String inputASName;
  
  protected String outputASName;

  protected DefaultActionContext actionContext;
  
  /**
   * Instance of {@link AnnotationComparator} used for sorting annots for the
   * phases.
   */
  protected AnnotationComparator annotationComparator;
  
  /**
   * The sets of annotations (of a given type) that have already been sorted.
   */
  protected Map<String, Annotation[]> sortedAnnotations;
  
  /**
   * A set of annotation types that were modified during the latest execution of
   * a pahse.
   */
  protected Set<String> changedTypes;
  
  /**
   * The listener that keeps track of the annotation types that have changed.
   */
  protected AnnotationSetListener inputASListener;
  
  /**
   * The type of the source.
   */
  protected SourceType sourceType;
  
  /**
   * The list of phases used in this transducer.
   */
  protected SPTBase[] singlePhaseTransducers;
  
  
  
  public String getEncoding() {
    return encoding;
  }

  @CreoleParameter(defaultValue="UTF-8", 
          comment="The encoding used for the input .jape files.")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  
  /**
   * 
   */
  public Transducer() {
    sortedAnnotations = new HashMap<String, Annotation[]>();
    changedTypes = new HashSet<String>();
    inputASListener = new AnnSetListener();
    annotationComparator = new AnnotationComparator();
  }

 
  
  @Override
  public Resource init() throws ResourceInstantiationException {
    super.init();
    try {
      switch(sourceType){
        case JAPE:
          parseJape();
          break;
        case BINARY:
          break;
        default:
          throw new ResourceInstantiationException("Unsupported source type " + 
                  sourceType + "!");
      }
    } catch(IOException e) {
      throw new ResourceInstantiationException(e);
    } catch(ParseException e) {
      throw new ResourceInstantiationException(e);
    }
    actionContext = new DefaultActionContext();
    return this;
  }

  protected void parseJape() throws IOException, ParseException, ResourceInstantiationException{
	Class oldClass = Factory.getJapeParserClass();
	Factory.setJapeParserClass(ParseCpslPDA.class);
	try{
		ParseCpslPDA parser = (ParseCpslPDA) Factory.newJapeParser(sourceURL, encoding);

	    StatusListener listener = new StatusListener(){
	      public void statusChanged(String text){
	        fireStatusChanged(text);
	      }
	    };
	    parser.addStatusListener(listener);
	    MultiPhaseTransducer intermediate =  parser.MultiPhaseTransducer();
	    parser.removeStatusListener(listener);
	    
	    singlePhaseTransducers = new SPTBase[intermediate.getPhases().size()];
	    SPTBuilder builder = new SPTBuilder();
	    for(int i = 0; i < intermediate.getPhases().size(); i++){
	      singlePhaseTransducers[i] = builder.buildSPT((SinglePhaseTransducerPDA)
	              intermediate.getPhases().get(i));
	    }
	}
	finally{
		Factory.setJapeParserClass(oldClass);
	}
  }
  
  @Override
  public void cleanup() {
    // TODO Auto-generated method stub
    super.cleanup();
  }

  @Override
  public void execute() throws ExecutionException {
	if (singlePhaseTransducers == null) {
		if (SourceType.JAPE.equals(sourceType))
			throw new IllegalStateException("init() was not called.");
		throw new IllegalStateException("init() was not called or an invalid binary grammar was supplied.");
	}
    AnnotationSet inputAs = (inputASName == null || inputASName.length() == 0) ?
            document.getAnnotations() : document.getAnnotations(inputASName);
    try {
      inputAs.addAnnotationSetListener(inputASListener);
      sortedAnnotations.clear();
      for(SPTBase aSpt : singlePhaseTransducers){
        changedTypes.clear();
        aSpt.setCorpus(corpus);
        aSpt.setDocument(document);
        aSpt.setInputASName(inputASName);
        aSpt.setOutputASName(outputASName);
        aSpt.setOwner(this);
        actionContext.setCorpus(corpus);
        actionContext.setPRFeatures(features);
        aSpt.setActionContext(actionContext);
        aSpt.setOntology(ontology);
        aSpt.execute();
        aSpt.setCorpus(null);
        aSpt.setDocument(null);
        aSpt.setInputASName(null);
        aSpt.setOutputASName(null);
        aSpt.setOwner(null);
        for(String type : changedTypes) sortedAnnotations.remove(type);
        changedTypes.clear();
      }
    } finally {
      inputAs.removeAnnotationSetListener(inputASListener);
    }
  }

  /**
   * Get the set of annotations, of a given type, sorted by start offset and
   * inverse length, obtained from the input annotation set of the current 
   * document.
   * 
   * @param type the type of annotations requested. 
   * @return an array of {@link Annotation} values.
   */
  public Annotation[] getSortedAnnotations(String type){
    Annotation[] annots = sortedAnnotations.get(type);
    if(annots == null){
      //not calculated yet
      AnnotationSet inputAS = 
        (inputASName == null || inputASName.trim().length() == 0) ?
        document.getAnnotations() : document.getAnnotations(inputASName);
      ArrayList<Annotation> annOfType = new ArrayList<Annotation>(
              inputAS.get(type));
      Collections.sort(annOfType, annotationComparator);
      annots = annOfType.toArray(new Annotation[annOfType.size()]);
      sortedAnnotations.put(type, annots);
    }
    return annots;
  }
  
  /**
   * @return the inputASName
   */
  public String getInputASName() {
    return inputASName;
  }

  /**
   * @param inputASName the inputASName to set
   */
  @CreoleParameter(comment="The name of the input annotation set.")
  @Optional
  @RunTime
  public void setInputASName(String inputASName) {
    this.inputASName = inputASName;
  }

  /**
   * @return the outputASName
   */
  public String getOutputASName() {
    return outputASName;
  }

  /**
   * @param outputASName the outputASName to set
   */
  @CreoleParameter(comment="The name of the output annotation set.")
  @Optional
  @RunTime
  public void setOutputASName(String outputASName) {
    this.outputASName = outputASName;
  }

  @CreoleParameter(comment="The ontology LR to be used by this transducer")
  @Optional
  @RunTime
  public void setOntology(Ontology onto) {
    ontology = onto;
  }

  public Ontology getOntology() {
    return ontology;
  }

  protected Ontology ontology = null;

  public static void main(String[] args){
    try {
      Gate.setUserSessionFile(new File(".session"));
      Gate.init();
      MainFrame.getInstance().setVisible(true);
      Gate.getCreoleRegister().registerDirectories(
              new File("").toURI().toURL());
      
      Document doc = Factory.newDocument(new File("/home/valyt/tmp/jplus/news/ft-BT-briefing-02-aug-2001.xml").toURI().toURL());
      Corpus corpus = Factory.newCorpus("TestABC");
      corpus.add(doc);
      
      FeatureMap params = Factory.newFeatureMap();
      params.put("sourceType", SourceType.JAPE);
      params.put("sourceURL", new File("C:\\gateOrig\\gate\\plugins\\ANNIE\\resources\\NE\\main.jape").toURI().toURL());
      LanguageAnalyser analyser = (LanguageAnalyser)Factory.createResource(Transducer.class.getName(), params);
      
      analyser.setCorpus(corpus);
      analyser.setDocument(doc);
      long start = System.currentTimeMillis();
      analyser.execute();
      System.out.print("Executed in " + (System.currentTimeMillis() - start) + " ms");
    } catch(GateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // methods implemeting ControllerAwarePR
  public void controllerExecutionStarted(Controller c)
    throws ExecutionException {
    actionContext.setController(c);
    for(SPTBase aSpt : singlePhaseTransducers){
      aSpt.runControllerExecutionStartedBlock(actionContext,c,ontology);
    }
  }

  public void controllerExecutionFinished(Controller c)
    throws ExecutionException {
    for(SPTBase aSpt : singlePhaseTransducers){
      // ontologies not supported yet, pass null
      Ontology o = null;
      aSpt.runControllerExecutionFinishedBlock(actionContext,c,ontology);
    }
    actionContext.setCorpus(null);
    actionContext.setController(null);
  }

  public void controllerExecutionAborted(Controller c, Throwable t)
    throws ExecutionException {
    for(SPTBase aSpt : singlePhaseTransducers){
      // ontologies not supported yet, pass null
      Ontology o = null;
      aSpt.runControllerExecutionAbortedBlock(actionContext,c,t,ontology);
    }
    actionContext.setCorpus(null);
    actionContext.setController(null);
  }

}
