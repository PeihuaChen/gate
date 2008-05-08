/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
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
public class RealtimeCorpusController extends SerialAnalyserController {
	
  private final static boolean DEBUG = false;

  /** Profiler to track PR execute time */
  protected Profiler prof;
  protected HashMap timeMap;
   
  public RealtimeCorpusController(){
    super();
    if(DEBUG) {
      prof = new Profiler();
      prof.enableGCCalling(false);
      prof.printToSystemOut(true);
      timeMap = new HashMap();
    }

  }
    
  protected class TimeoutTask extends TimerTask{
    public void run(){
      Err.prln("Execution timeout, stopping worker thread...");
      executionThread.stop();
    }
    Thread executionThread;
    public Thread getExecutionThread() {
      return executionThread;
    }
    public void setExecutionThread(Thread executionThread) {
      this.executionThread = executionThread;
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
  
          runComponent(j);
          if (DEBUG) {
            prof.checkPoint("~Execute PR ["+((ProcessingResource)
                                   prList.get(j)).getName()+"]");
            Long timeOfPR = (Long) timeMap.get(((ProcessingResource)
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
  public void execute() throws ExecutionException{
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
      Thread workerThread = new Thread(docRunner, 
              this.getClass().getCanonicalName() + " worker thread (document " +
              doc.getName() + ")");
      
      //prepare the timeout timer
      TimeoutTask timeoutTask = new TimeoutTask();
      timeoutTask.setExecutionThread(workerThread);
      timeoutTimer.schedule(timeoutTask, timeout.longValue());
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
        //close the previoulsy unloaded Doc
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

  public void setTimeout(Long timeout) {
    this.timeout = timeout;
  }

  /**
   * Sleep time in milliseconds while waiting for worker thread to finish.
   */
  private static final int POLL_INTERVAL = 50;
}
