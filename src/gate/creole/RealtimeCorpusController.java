/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 08/05/2008
 *
 *  $Id$
 *
 */package gate.creole;

import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import gate.*;
import gate.creole.metadata.*;
import gate.util.Err;
import gate.util.profile.Profiler;
import gate.util.Out;
/**
 * A custom GATE controller that interrupts the execution over a document when a
 * specified amount of time has elapsed. It also ignores all errors/exceptions 
 * that may occur during execution and simply carries on with the next document
 * when that happens.
 * 
 * @author Valentin Tablan
 *
 */
@CreoleResource(name = "Real-Time Corpus Pipeline",
    comment = "A serial controller for PR pipelines over corpora which "
        + "limits the run time of each PR.",
    icon = "application-realtime",
    helpURL = "http://gate.ac.uk/userguide/sec:creole-model:applications")
public class RealtimeCorpusController extends SerialAnalyserController {
	
  private final static boolean DEBUG = false;

  /** Profiler to track PR execute time */
  protected Profiler prof;
  protected HashMap<String,Long> timeMap;
   
  public RealtimeCorpusController(){
    super();
    if(DEBUG) {
      prof = new Profiler();
      prof.enableGCCalling(false);
      prof.printToSystemOut(true);
      timeMap = new HashMap<String,Long>();
    }
  }
  
  protected class DocRunner implements Runnable{
    public void run(){
      try{
        //run the system over the current document
        //set the doc and corpus
        for(int j = 0; j < prList.size(); j++){
          ((LanguageAnalyser)prList.get(j)).setDocument(document);
          ((LanguageAnalyser)prList.get(j)).setCorpus(corpus);
        }        
        interrupted = false;
        //execute the PRs
        //check all the PRs have the right parameters
        checkParameters();
        if(DEBUG) {
          prof.initRun("Execute controller [" + getName() + "]");
        }

        //execute all PRs in sequence
        interrupted = false;
        for (int j = 0; j < prList.size(); j++){
          if(isInterrupted()) throw new ExecutionInterruptedException(
              "The execution of the " + getName() +
              " application has been abruptly interrupted!");
  
          if (Thread.currentThread().isInterrupted()) {
            Err.println("Execution on document " + document.getName() + 
              " has been stopped");
            break;
          }
          
          try {
            runComponent(j);
          }
          catch (Throwable e)
          {
            if (!Thread.currentThread().isInterrupted())
              throw e;
            
            Err.println("Execution on document " + document.getName() + 
              " has been stopped");
            break;
          }
          
          if (DEBUG) {
            prof.checkPoint("~Execute PR ["+((ProcessingResource)
                                   prList.get(j)).getName()+"]");
            Long timeOfPR = timeMap.get(((ProcessingResource)
                                   prList.get(j)).getName());
            if (timeOfPR == null) 
              timeMap.put(((ProcessingResource)
                                   prList.get(j)).getName(), new Long(prof.getLastDuration()));
            else 
              timeMap.put(((ProcessingResource)
                                   prList.get(j)).getName(), new Long(timeOfPR.longValue() + prof.getLastDuration())); 
            Out.println("Time taken so far by " + ((ProcessingResource)
                                   prList.get(j)).getName() + ": " 
				      + timeMap.get(((ProcessingResource) prList.get(j)).getName()));

      }

        }
      }catch(ThreadDeath td){
        //special case as we need to re-throw this one
        Err.prln("Execution on document " + document.getName() + 
                " has been stopped");
        throw(td);
      }catch(Throwable cause){
        Err.prln("Execution on document " + document.getName() + 
                " has caused an error:\n=========================");
        cause.printStackTrace(Err.getPrintWriter());
        Err.prln("=========================\nError ignored...\n");
      }finally{
        //unset the doc and corpus
        for(int j = 0; j < prList.size(); j++){
          ((LanguageAnalyser)prList.get(j)).setDocument(null);
          ((LanguageAnalyser)prList.get(j)).setCorpus(null);
        }
        
        if (DEBUG) {
          prof.checkPoint("Execute controller [" + getName() + "] finished");
        }
      }
    }
    
    Document document;
    public Document getDocument() {
      return document;
    }
    public void setDocument(Document document) {
      this.document = document;
    }
    
  }
  
  /** Run the Processing Resources in sequence. */
  public void executeImpl() throws ExecutionException{
    Timer timeoutTimer = new Timer(this.getClass().getName() + 
        " timeout timer");
    interrupted = false;
    if(corpus == null) throw new ExecutionException(
      "(SerialAnalyserController) \"" + getName() + "\":\n" +
      "The corpus supplied for execution was null!");
    //iterate through the documents in the corpus
    for(int i = 0; i < corpus.size(); i++){
      if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the " + getName() +
        " application has been abruptly interrupted!");

      boolean docWasLoaded = corpus.isDocumentLoaded(i);
      Document doc = (Document)corpus.get(i);
      DocRunner docRunner = new DocRunner();
      docRunner.setDocument(doc);
      final Thread workerThread = new Thread(docRunner, 
              this.getClass().getCanonicalName() + " worker thread (document " +
              doc.getName() + ")");
      
      //We need three timer tasks to do a staged stop that gets harsher as we go on...
      
      //first we simply interrupt the PRs and the thread they are being run in
      TimerTask gracefulTask = new TimerTask()
      {
        public void run() {
          Err.prln("Execution timeout, attempting to gracefully stop worker thread...");
          workerThread.interrupt();
          for(int j = 0; j < prList.size(); j++){
            ((Executable)prList.get(j)).interrupt();
          }
        }
      };
      
      //if using interrupt didn't stop things then we nullify the document and corpus params
      //in the hope that we will induce a null pointer exception
      TimerTask nullifyTask = new TimerTask()
      {
        public void run() {
          if (!workerThread.isAlive()) return;
          Err.println("Execution timeout, attempting to induce exception in order to stop worker thread...");
          for(int j = 0; j < prList.size(); j++){
            ((LanguageAnalyser)prList.get(j)).setDocument(null);
            ((LanguageAnalyser)prList.get(j)).setCorpus(null);
          }
        }
      };
      
      //if the PR still hasn't stopped then be ruthless and just stop the thread dead in it's tracks!
      TimerTask timeoutTask = new TimerTask()
      {
        public void run() {
          if (!workerThread.isAlive()) return;
          Err.println("Execution timout, worker thread will be forcibly terminated!");
          workerThread.stop();
        }
      };
      
      //only schedule the first two tasks if the graceful timeout param makes sense 
      if (graceful.longValue() != -1 && (timeout.longValue() == -1 || graceful.longValue() < timeout.longValue())) {
        timeoutTimer.schedule(gracefulTask, graceful.longValue());
        
        long nullTime = graceful.longValue() + (timeout.longValue() != -1 ? (timeout.longValue()-graceful.longValue())/2 : graceful.longValue() / 2);
        
        timeoutTimer.schedule(nullifyTask, nullTime);
      }
      
      if (timeout.longValue() != -1) timeoutTimer.schedule(timeoutTask, timeout.longValue());
      
      //start the execution
      workerThread.start();
      
      //wait for the execution to finish or timeout
      while(workerThread.isAlive()){
        try{
          Thread.sleep(POLL_INTERVAL);
        }catch(InterruptedException ie){
          //we don't like being interrupted -> clear the interrupted flag
          Thread.interrupted();
        }
      }
      timeoutTask.cancel();
      
      if(!docWasLoaded){
        //trigger saving
        getCorpus().unloadDocument(doc);
        //close the previously unloaded Doc
        Factory.deleteResource(doc);
      }
    }
    
    //get rid of the timeout timer.
    timeoutTimer.cancel();
  }
  
  
  protected Long timeout;

  public Long getTimeout() {
    return timeout;
  }
  
  @CreoleParameter(defaultValue = "60000",
      comment = "Timout in milliseconds before execution on a document is forcibly stopped (forcibly stopping execution may result in memory leaks and/or unexpected behaviour)")
  public void setTimeout(Long timeout) {
    this.timeout = timeout;
  }
  
  protected Long graceful;

  public Long getGracefulTimeout() {
    return graceful;
  }

  @CreoleParameter(defaultValue = "-1",
      comment = "Timeout in milliseconds before execution on a document is gracefully stopped. Defaults to -1 which disables this functionality and relies, as previously, on forcibly stoping execution.")
  public void setGracefulTimeout(Long graceful) {
    this.graceful = graceful;
  }

  /**
   * Sleep time in milliseconds while waiting for worker thread to finish.
   */
  private static final int POLL_INTERVAL = 50;
}
