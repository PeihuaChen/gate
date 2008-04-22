/*
 *  TestOntologyAPI.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TestOntologyAPI.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.owlim.OWLIMOntologyLR;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Simple test class that load an ontology available online and accesses
 * its content via the ontology API
 */
public class TestOntologyAPI extends TestCase {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestOntologyAPI.class);
  }

  public TestOntologyAPI(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    // make sure the right plugin is loaded
    File pluginsHome = new File(System
            .getProperty(GateConstants.GATE_HOME_PROPERTY_NAME), "plugins");
    Gate.getCreoleRegister().registerDirectories(
            new File(pluginsHome, "Ontology_Tools").toURI().toURL());

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  // load a known ontology and access it
  // through the API objects
  public void testLoadingOWLOntology() throws MalformedURLException,
          ResourceInstantiationException {

    FeatureMap fm = Factory.newFeatureMap();
    URL url = new URL("http://gate.ac.uk/tests/demo.owl");
    fm.put("rdfXmlURL", url);
    fm.put("defaultNameSpace", "http://www.owl-ontologies.com/unnamed.owl");
    Ontology ontology = (Ontology)Factory.createResource(
            "gate.creole.ontology.owlim.OWLIMOntologyLR", fm);

    int classNum = ontology.getOClasses(false).size();
    assertEquals(20, classNum);
    // count the number of top classes
    Set topclasses = ontology.getOClasses(true);
    assertEquals(topclasses.size(), 6);
    // get the class Department
    OClass aClass = ontology.getOClass(new URI(ontology.getDefaultNameSpace()
            + "Department", false));
    assertNotNull(aClass);
    // and count the number of super classes
    Set supclassbydist = aClass.getSuperClasses(OConstants.TRANSITIVE_CLOSURE);
    // the list contains 2 arrays of classes i-e 2 levels
    assertEquals(supclassbydist.size(), 2);
    // get the class Department
    aClass = ontology.getOClass(new URI(ontology.getDefaultNameSpace()
            + "Organization", false));
    assertNotNull(aClass);
    assertTrue(aClass.isTopClass());
    Set subclasses = aClass.getSubClasses(OConstants.TRANSITIVE_CLOSURE);
    assertEquals(subclasses.size(), 5);
    Factory.deleteResource(ontology);
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestOntologyAPI.class);
  } // suite
}
