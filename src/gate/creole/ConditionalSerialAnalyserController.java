/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 08/10/2001
 *
 *  $Id$
 *
 */

package gate.creole;

import gate.*;
import gate.util.*;

import java.util.*;

/**
 * This class implements a SerialController that only contains
 * {@link gate.LanguageAnalyser}s.
 * It has a {@link gate.Corpus} and its execute method runs all the analysers in
 * turn over each of the documents in the corpus.
 * This is a copy of the {@link SerialAnalyserController}, the only difference
 * being that it inherits from {@link ConditionalSerialController} rather than
 * from {@link SerialController} which makes it a <b>conditional</b> serial
 * analyser controller.
 */
public class ConditionalSerialAnalyserController
       extends ConditionalSerialController implements CorpusController {

  public gate.Corpus getCorpus() {
    return corpus;
  }

  public void setCorpus(gate.Corpus corpus) {
    this.corpus = corpus;
  }

  /** Run the Processing Resources in sequence. */
  public void execute() throws ExecutionException{
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
      //run the system over this document
      //set the doc and corpus
      for(int j = 0; j < prList.size(); j++){
        ((LanguageAnalyser)prList.get(j)).setDocument(doc);
        ((LanguageAnalyser)prList.get(j)).setCorpus(corpus);
      }

      try{
        super.execute();
      }catch(Exception e){
        e.printStackTrace(Err.getPrintWriter());
      }

      //unset the doc and corpus
      for(int j = 0; j < prList.size(); j++){
        ((LanguageAnalyser)prList.get(j)).setDocument(null);
        ((LanguageAnalyser)prList.get(j)).setCorpus(null);
      }

      corpus.unloadDocument(doc);
      if(!docWasLoaded) Factory.deleteResource(doc);
    }
  }

  /**
   * Overidden from {@link SerialController} to only allow
   * {@link LanguageAnalyser}s as components.
   */
  public void add(ProcessingResource pr){
    if(pr instanceof LanguageAnalyser){
      super.add(pr);
    }else{
      throw new GateRuntimeException(getClass().getName() +
                                     "only accepts " +
                                     LanguageAnalyser.class.getName() +
                                     "s as components\n" +
                                     pr.getClass().getName() +
                                     " is not!");
    }
  }
  /**
   * Sets the current document to the memeber PRs
   */
  protected void setDocToPrs(Document doc){
    Iterator prIter = getPRs().iterator();
    while(prIter.hasNext()){
      ((LanguageAnalyser)prIter.next()).setDocument(doc);
    }
  }


  /**
   * Checks whether all the contained PRs have all the required runtime
   * parameters set. Ignores the corpus and document parameters as these will
   * be set at run time.
   *
   * @return a {@link List} of {@link ProcessingResource}s that have required
   * parameters with null values if they exist <tt>null</tt> otherwise.
   * @throw {@link ResourceInstantiationException} if problems occur while
   * inspecting the parameters for one of the resources. These will normally be
   * introspection problems and are usually caused by the lack of a parameter
   * or of the read accessor for a parameter.
   */
  public List getOffendingPocessingResources()
         throws ResourceInstantiationException{
    //take all the contained PRs
    ArrayList badPRs = new ArrayList(getPRs());
    //remove the ones that no parameters problems
    Iterator prIter = getPRs().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                              get(pr.getClass().getName());
      //this is a list of lists
      List parameters = rData.getParameterList().getRuntimeParameters();
      //remove corpus and document
      List newParameters = new ArrayList();
      Iterator pDisjIter = parameters.iterator();
      while(pDisjIter.hasNext()){
        List aDisjunction = (List)pDisjIter.next();
        List newDisjunction = new ArrayList(aDisjunction);
        Iterator internalParIter = newDisjunction.iterator();
        while(internalParIter.hasNext()){
          Parameter parameter = (Parameter)internalParIter.next();
          if(parameter.getName().equals("corpus") ||
             parameter.getName().equals("document")) internalParIter.remove();
        }
        if(!newDisjunction.isEmpty()) newParameters.add(newDisjunction);
      }

      if(AbstractResource.checkParameterValues(pr, newParameters)){
        badPRs.remove(pr);
      }
    }
    return badPRs.isEmpty() ? null : badPRs;
  }


  private gate.Corpus corpus;
}