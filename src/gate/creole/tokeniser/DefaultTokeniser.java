package gate.creole.tokeniser;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.event.*;
import java.util.*;

/**
 * A composed tokeniser containing a {@link SimpleTokeniser} and a
 * {@link gate.creole.Transducer}.
 * The simple tokeniser tokenises the document and the transducer processes its
 * output.
 */
public class DefaultTokeniser extends AbstractProcessingResource {

  public DefaultTokeniser() {
  }


  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    try{
      //init super object
      super.init();
      //create all the componets
      FeatureMap params;
      FeatureMap features;
      Map listeners = new HashMap();
      listeners.put("gate.event.StatusListener", new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      });

      //tokeniser
      fireStatusChanged("Creating a tokeniser");
      params = Factory.newFeatureMap();
      if(tokeniserRulesURL != null) params.put("rulesURL",
                                               tokeniserRulesURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the tokeniser: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      tokeniser = (SimpleTokeniser)Factory.createResource(
                    "gate.creole.tokeniser.SimpleTokeniser",
                    params, features, listeners);
      tokeniser.setName("Tokeniser " + System.currentTimeMillis());
      fireProgressChanged(50);


      //transducer
      fireStatusChanged("Creating a Jape transducer");
      params = Factory.newFeatureMap();
      if(transducerGrammarURL != null) params.put("grammarURL",
                                                  transducerGrammarURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the transducer: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(51, 100));
      transducer = (Transducer)Factory.createResource("gate.creole.Transducer",
                                                      params, features,
                                                      listeners);
      fireProgressChanged(100);
      fireProcessFinished();
      transducer.setName("Transducer " + System.currentTimeMillis());
    }catch(ResourceInstantiationException rie){
      throw rie;
    }catch(Exception e){
      throw new ResourceInstantiationException(e);
    }
    return this;
  }

  public void run(){
    FeatureMap params;
    try{
      fireProgressChanged(0);
      //tokeniser
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("annotationSetName", annotationSetName);
      Factory.setResourceRuntimeParameters(tokeniser, params);

      //transducer
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("inputASName", annotationSetName);
      params.put("outputASName", annotationSetName);
      Factory.setResourceRuntimeParameters(transducer, params);

      fireProgressChanged(5);
      ProgressListener pListener = new CustomProgressListener(5, 50);
      StatusListener sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };

      //tokeniser
      tokeniser.addProgressListener(pListener);
      tokeniser.addStatusListener(sListener);
      tokeniser.run();
      tokeniser.check();
      tokeniser.removeProgressListener(pListener);
      tokeniser.removeStatusListener(sListener);

      //transducer
      pListener = new CustomProgressListener(50, 100);
      transducer.addProgressListener(pListener);
      transducer.addStatusListener(sListener);
      transducer.run();
      transducer.check();
      transducer.removeProgressListener(pListener);
      transducer.removeStatusListener(sListener);
    }catch(ExecutionException ee){
      executionException = ee;
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }//run

  public void setTokeniserRulesURL(java.net.URL tokeniserRulesURL) {
    this.tokeniserRulesURL = tokeniserRulesURL;
  }
  public java.net.URL getTokeniserRulesURL() {
    return tokeniserRulesURL;
  }
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setTransducerGrammarURL(java.net.URL transducerGrammarURL) {
    this.transducerGrammarURL = transducerGrammarURL;
  }
  public java.net.URL getTransducerGrammarURL() {
    return transducerGrammarURL;
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  } // init()

  private static final boolean DEBUG = false;

  /** the simple tokeniser used for tokenisation*/
  protected SimpleTokeniser tokeniser;

  /** the transducer used for post-processing*/
  protected Transducer transducer;
  private java.net.URL tokeniserRulesURL;
  private String encoding;
  private java.net.URL transducerGrammarURL;
  private transient Vector statusListeners;
  private transient Vector progressListeners;
  private gate.Document document;
  private String annotationSetName;

  /**
   * A progress listener used to convert a 0..100 interval into a smaller one
   */
  class CustomProgressListener implements ProgressListener{
    CustomProgressListener(int start, int end){
      this.start = start;
      this.end = end;
    }
    public void progressChanged(int i){
      fireProgressChanged(start + (end - start) * i / 100);
    }

    public void processFinished(){
      fireProgressChanged(end);
    }

    int start;
    int end;
  }
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  public void setDocument(gate.Document document) {
    this.document = document;
  }
  public gate.Document getDocument() {
    return document;
  }
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }
  public String getAnnotationSetName() {
    return annotationSetName;
  }/////////class CustomProgressListener implements ProgressListener
}