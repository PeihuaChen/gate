
//Title:        FSM tester
//Version:
//Copyright:    Copyright (c) 2000
//Author:       Valentin Tablan
//Company:      NLP group, DCS, University of Sheffield
//Description:  test class for the FSM (finite state machine) package
package gate.fsm;

import gate.jape.*;

import java.util.*;

public class TestFSM {

  public TestFSM() {
  }

  public void setUp() throws JapeException{
    batch = new Batch("z:/gate2/src/gate/fsm/fsmtestgrammar.jape");
    transducer = (MultiPhaseTransducer)batch.getTransducer();
    transducer.finish();
  } // setUp

  public void tearDown(){
  }

  public void testOne(){
    Enumeration phases = transducer.getPhases().elements();
    while(phases.hasMoreElements()){
      FSM aFSM = new FSM((SinglePhaseTransducer)phases.nextElement());
      System.out.println(aFSM);
    }
  }
  public static void main(String[] args) {
    try{
      TestFSM testFSM = new TestFSM();
      testFSM.setUp();
      testFSM.testOne();
      testFSM.tearDown();
    }catch(Exception e){
      e.printStackTrace(System.err);
    }
  }
  private Batch batch;
  private MultiPhaseTransducer transducer;
}