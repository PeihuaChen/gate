/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 01 Feb 2000
 *
 *  $Id$
 */

package gate.creole;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gate.Gate;
import gate.Resource;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.jape.Batch;
import gate.jape.JapeException;
import gate.jape.MultiPhaseTransducer;
import gate.jape.SinglePhaseTransducer;
import gate.util.Err;

/**
 * A cascaded multi-phase transducer using the Jape language which is a
 * variant of the CPSL language.
 */
public class Transducer extends AbstractLanguageAnalyser implements gate.gui.ActionsPublisher {

  public static final String TRANSD_DOCUMENT_PARAMETER_NAME = "document";

  public static final String TRANSD_INPUT_AS_PARAMETER_NAME = "inputASName";

  public static final String TRANSD_OUTPUT_AS_PARAMETER_NAME = "outputASName";

  public static final String TRANSD_ENCODING_PARAMETER_NAME = "encoding";

  public static final String TRANSD_GRAMMAR_URL_PARAMETER_NAME = "grammarURL";

  public static final String TRANSD_BINARY_GRAMMAR_URL_PARAMETER_NAME = "binaryGrammarURL";
  
  protected List actionList;
  
  /**
   * Default constructor. Does nothing apart from calling the default
   * constructor from the super class. The actual object initialisation is done
   * via the {@link #init} method.
   */
  public Transducer() {
	    actionList = new ArrayList();
	    actionList.add(null);
		actionList.add(new SerializeTransducerAction());
  }

  /*
  private void writeObject(ObjectOutputStream oos) throws IOException {
    Out.prln("writing transducer");
    oos.defaultWriteObject();
    Out.prln("finished writing transducer");
  } // writeObject
  */

  /**
   * This method is the one responsible for initialising the transducer. It
   * assumes that all the needed parameters have been already set using the
   * appropiate setXXX() methods.
   *@return a reference to <b>this</b>
   */
  public Resource init() throws ResourceInstantiationException {
	  if(grammarURL == null && binaryGrammarURL == null) {
		  throw new ResourceInstantiationException("grammarURL is null");
	  }
	  
      if(encoding != null){
      try{
        fireProgressChanged(0);
		
		if(grammarURL != null) {
			batch = new Batch(grammarURL, encoding, new InternalStatusListener());
			if(enableDebugging != null) {
				batch.setEnableDebugging(enableDebugging.booleanValue());
			} else {
				batch.setEnableDebugging(false);
			}
			batch.setOntology(ontology);
		} else {
			ObjectInputStream s = new ObjectInputStream(binaryGrammarURL.openStream());
			batch = (gate.jape.Batch) s.readObject();
		}
		fireProcessFinished();
      }catch(Exception e){
        throw new ResourceInstantiationException(e);
      }
    } else
      throw new ResourceInstantiationException (
        "enconding is not set!"
      );

      batch.addProgressListener(new IntervalProgressListener(0, 100));

    return this;
  }

 
  /**
   * Implementation of the run() method from {@link java.lang.Runnable}.
   * This method is responsible for doing all the processing of the input
   * document.
   */
  public void execute() throws ExecutionException{
    interrupted = false;
    if(document == null) throw new ExecutionException("No document provided!");
    if(inputASName != null && inputASName.equals("")) inputASName = null;
    if(outputASName != null && outputASName.equals("")) outputASName = null;
    try{
      batch.transduce(document,
                      inputASName == null ?
                        document.getAnnotations() :
                        document.getAnnotations(inputASName),
                      outputASName == null ?
                        document.getAnnotations() :
                        document.getAnnotations(outputASName));
    }catch(JapeException je){
      throw new ExecutionException(je);
    }
  }

  /**
   * Gets the list of actions that can be performed on this resource.
   * @return a List of Action objects (or null values)
   */
  public List getActions() {
	    List result = new ArrayList();
	    result.addAll(actionList);
	    return result;
  }

  
  /**
   * Saves the Jape Transuder to the binary file.
   * @author niraj
   */
  protected class SerializeTransducerAction extends javax.swing.AbstractAction{
	    public SerializeTransducerAction(){
	      super("Serialize Transducer");
	      putValue(SHORT_DESCRIPTION, "Serializes the Transducer as binary file");
	    }

	    public void actionPerformed(java.awt.event.ActionEvent evt){
	      Runnable runnable = new Runnable(){
	        public void run(){
	          JFileChooser fileChooser = MainFrame.getFileChooser();
	          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
	          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	          fileChooser.setMultiSelectionEnabled(false);
	          if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
	            File file = fileChooser.getSelectedFile();
	            try{
	              MainFrame.lockGUI("Serializing JAPE Transducer...");
					FileOutputStream out = new FileOutputStream(file);
					ObjectOutputStream s = new ObjectOutputStream(out);
					s.writeObject(batch);
					s.flush();
					s.close();
					out.close();
				}catch(IOException ioe){
	              JOptionPane.showMessageDialog(null,
	                              "Error!\n"+
	                               ioe.toString(),
	                               "GATE", JOptionPane.ERROR_MESSAGE);
	              ioe.printStackTrace(Err.getPrintWriter());
	            }finally{
	              MainFrame.unlockGUI();
	            }
	          }
	        }
	      };
	      Thread thread = new Thread(runnable, "Transduer Serialization");
	      thread.setPriority(Thread.MIN_PRIORITY);
	      thread.start();
	    }
	  }
  
  /**
   * Notifies all the PRs in this controller that they should stop their
   * execution as soon as possible.
   */
  public synchronized void interrupt(){
    interrupted = true;
    batch.interrupt();
  }
  /**
   * Sets the grammar to be used for building this transducer.
   * @param newGrammarURL an URL to a file containing a Jape grammar.
   */
  public void setGrammarURL(java.net.URL newGrammarURL) {
    grammarURL = newGrammarURL;
  }

  /**
   * Gets the URL to the grammar used to build this transducer.
   * @return a {@link java.net.URL} pointing to the grammar file.
   */
  public java.net.URL getGrammarURL() {
    return grammarURL;
  }

  /**
   *
   * Sets the encoding to be used for reding the input file(s) forming the Jape
   * grammar. Note that if the input grammar is a multi-file one than the same
   * encoding will be used for reding all the files. Multi file grammars with
   * different encoding across the composing files are not supported!
   * @param newEncoding a {link String} representing the encoding.
   */
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }

  /**
   * Gets the encoding used for reding the grammar file(s).
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Sets the {@link gate.AnnotationSet} to be used as input for the transducer.
   * @param newInputASName a {@link gate.AnnotationSet}
   */
  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }

  /**
   * Gets the {@link gate.AnnotationSet} used as input by this transducer.
   * @return a {@link gate.AnnotationSet}
   */
  public String getInputASName() {
    return inputASName;
  }

  /**
   * Sets the {@link gate.AnnotationSet} to be used as output by the transducer.
   * @param newOutputASName a {@link gate.AnnotationSet}
   */
  public void setOutputASName(String newOutputASName) {
    outputASName = newOutputASName;
  }

  /**
   * Gets the {@link gate.AnnotationSet} used as output by this transducer.
   * @return a {@link gate.AnnotationSet}
   */
  public String getOutputASName() {
    return outputASName;
  }

  public Boolean getEnableDebugging() {
    return enableDebugging;
  }

  public void setEnableDebugging(Boolean enableDebugging) {
    this.enableDebugging = enableDebugging;
  }

  /**
   * The URL to the jape file used as grammar by this transducer.
   */
  private java.net.URL grammarURL;

  /**
   * The URL to the serialized jape file used as grammar by this transducer.
   */
  private java.net.URL binaryGrammarURL;
  
  /**
   * The actual JapeTransducer used for processing the document(s).
   */
  protected Batch batch;

  /**
   * The encoding used for reding the grammar file(s).
   */
  private String encoding;

  /**
   * The {@link gate.AnnotationSet} used as input for the transducer.
   */
  private String inputASName;

  /**
   * The {@link gate.AnnotationSet} used as output by the transducer.
   */
  private String outputASName;

  /**
   * The ontology that will be available on the RHS of JAPE rules.
   */
  private gate.creole.ontology.Ontology ontology;

  /**
   * Gets the ontology used by this transducer.
   * @return an {@link gate.creole.ontology.Ontology} value.
   */
  public gate.creole.ontology.Ontology getOntology() {
    return ontology;
  }

  /**
   * Sets the ontology used by this transducer.
   * @param ontology an {@link gate.creole.ontology.Ontology} value.
   */
  public void setOntology(gate.creole.ontology.Ontology ontology) {
    this.ontology = ontology;
  }
  
  
  /**
   * A switch used to activate the JAPE debugger.
   */
  private Boolean enableDebugging;

  /**
   * Return the Batch Object
   * @return
   */
  public Batch getBatch() {
	return batch;
  }

public java.net.URL getBinaryGrammarURL() {
	return binaryGrammarURL;
}

public void setBinaryGrammarURL(java.net.URL binaryGrammarURL) {
	this.binaryGrammarURL = binaryGrammarURL;
}

}