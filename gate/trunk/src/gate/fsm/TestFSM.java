/*
*	TestFSM.java
*
*	Valentin Tablan, 11/Apr/2000
*
*	$Id$
*/
package gate.fsm;

import gate.jape.*;

import java.util.*;
import java.io.*;

import junit.framework.*;

import EDU.auburn.VGJ.graph.*;
import EDU.auburn.VGJ.gui.*;
import EDU.auburn.VGJ.examplealg.ExampleAlg2;
import EDU.auburn.VGJ.algorithm.tree.TreeAlgorithm;
import EDU.auburn.VGJ.algorithm.cgd.CGDAlgorithm;
import EDU.auburn.VGJ.algorithm.shawn.Spring;
import EDU.auburn.VGJ.algorithm.cartegw.BiconnectGraph;


public class TestFSM extends TestCase {

  /** Construction */
  public TestFSM(String name) { super(name); }

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
      System.out.println(aFSM.getGML());
    }
  }

  public void graphTest()throws java.io.IOException,
                                 EDU.auburn.VGJ.graph.ParseError{
    Enumeration phases = transducer.getPhases().elements();
    while(phases.hasMoreElements()){
      SinglePhaseTransducer phase = (SinglePhaseTransducer)phases.nextElement();
      FSM aFSM = new FSM(phase);
      showGraph("Non-deterministic (" + phase.getName() +")",aFSM);
      aFSM.eliminateVoidTransitions();
      showGraph("Deterministic (" + phase.getName()+")", aFSM);
    }

  }

  private void showGraph(String title, FSM fsm) throws java.io.IOException,
                                 EDU.auburn.VGJ.graph.ParseError{
    String gml = fsm.getGML();
    GMLlexer gl = new GMLlexer(new StringBufferInputStream(gml));
    GMLobject go = new GMLobject(gl, null);
    Graph graph =
          new Graph(go.getGMLSubObject("graph", GMLobject.GMLlist, false));
    GraphWindow graph_editing_window = new GraphWindow(graph);
    // Here the algorithms are added.
    TreeAlgorithm talg = new TreeAlgorithm('d');
    graph_editing_window.addAlgorithm(talg, "Tree Down");
    talg = new TreeAlgorithm('u');
    graph_editing_window.addAlgorithm(talg, "Tree Up");
    talg = new TreeAlgorithm('l');
    graph_editing_window.addAlgorithm(talg, "Tree Left");
    talg = new TreeAlgorithm('r');
    graph_editing_window.addAlgorithm(talg, "Tree Right");
    graph_editing_window.setTitle(title);
    graph_editing_window.pack();
    graph_editing_window.show();
    graph_editing_window.applyAlgorithm("Tree Right");
  }

  public static void main(String[] args) {
    try{
      TestFSM testFSM = new TestFSM("TestFSM");
      testFSM.setUp();
      testFSM.graphTest();
      testFSM.tearDown();
    }catch(Exception e){
      e.printStackTrace(System.err);
    }
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFSM.class);
  } // suite

  private Batch batch;
  private MultiPhaseTransducer transducer;
}