/*
 *	TestFSM.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Valentin Tablan, 11/Apr/2000
 *
 *	$Id$
 */
package gate.fsm;

import gate.jape.*;
import gate.util.*;

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

/** JUnit style test code for the gate.fsm package.
  *Unfortunatley it doesn't actually do any testing because of the difficulty
  *to test this kind of stuff.
  * However the main() method runs a visual test that will print the graphs
  *(deterministic and nod-~) resulted form parsing a Jape file.
  *The real functionality of this package is tested in the jape package that
  *make extensive use of the services provided by his package.
  */
public class TestFSM extends TestCase {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestFSM(String name) { super(name); }

  public void setUp() throws JapeException, IOException, GateException {
//    String japeFileName = "/gate/fsm/fsmtestgrammar.jape";
//    String japeFileName = "jape/TestABC.jape";
/*    String japeFileName = "jape/combined/brian-soc-loc1.jape";
    InputStream japeFileStream = Files.getResourceAsStream(japeFileName);

    if(japeFileStream == null)
      throw new JapeException("couldn't open " + japeFileName);
    batch = new Batch(japeFileStream);
*/

    Gate.init();
    String resPath = "gate/resources/jape/combined/";
    String resName = "brian-soc-loc1.jape";
//    batch = new Batch(resPath, resName);

String japeFileName = "d:/tmp/jape/persontest.jape";
batch = new Batch(japeFileName);

    transducer = (MultiPhaseTransducer)batch.getTransducer();
    transducer.finish();
  } // setUp

  public void tearDown(){
  }

  /** Does some kind of a test that verifies whether parsing the jape file
    *results in a graph. It doesn't check the structure graph.
    */
  public void testOne(){
    Enumeration phases = transducer.getPhases().elements();
    while(phases.hasMoreElements()){
      FSM aFSM = new FSM((SinglePhaseTransducer)phases.nextElement());
      //Out.println(aFSM.getGML());
      String gml = aFSM.getGML();
      assert(gml.startsWith("graph["));
    }
  }

  /**Will try to parse a .jape file and display the graphs resulted.*/
  public void graphTest()throws java.io.IOException,
                                 EDU.auburn.VGJ.graph.ParseError{
    Enumeration phases = transducer.getPhases().elements();
    while(phases.hasMoreElements()){
      SinglePhaseTransducer phase = (SinglePhaseTransducer)phases.nextElement();
      FSM aFSM = new FSM(phase);
      showGraph("Non-deterministic (" + phase.getName() +")",aFSM.getGML());
      aFSM.eliminateVoidTransitions();
      showGraph("Deterministic (" + phase.getName()+")", aFSM.getGML());
    }

  }

  /** Opens anew window containing the visual representation of a FSM and
    *having a given title*/
  static public void showGraph(String title, String gml) throws java.io.IOException,
                                 EDU.auburn.VGJ.graph.ParseError{
    GMLlexer gl = new GMLlexer(new ByteArrayInputStream(gml.getBytes()));
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

  /** runs the graphical test*/
  public static void main(String[] args) {
    try{
      TestFSM testFSM = new TestFSM("TestFSM");
      testFSM.setUp();
      testFSM.graphTest();
      testFSM.tearDown();
    }catch(Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFSM.class);
  } // suite

  private Batch batch;
  private MultiPhaseTransducer transducer;
}