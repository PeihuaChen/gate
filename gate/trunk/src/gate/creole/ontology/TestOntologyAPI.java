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
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.owlim.OWLIMOntologyLR;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Simple test class that load an ontology available online and accesses its
 * content via the ontology API
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
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  // load a known ontology and access it
  // through the API objects
  public void testLoadingOWLOntology() throws MalformedURLException,
          ResourceInstantiationException {
    OWLIMOntologyLR onto = new OWLIMOntologyLR();
    URL url = new URL("http://gate.ac.uk/tests/demo.owl");
    onto.setPersistRepository(new Boolean(false));
    try {
      onto.setPersistLocation(File.createTempFile("abc","abc").getParentFile().toURI().toURL());
    } catch(IOException ioe) {
      throw new ResourceInstantiationException(ioe);
    }
    onto.setDefaultNameSpace("http://www.owl-ontologies.com/unnamed.owl#");
    onto.setRdfXmlURL(url);
    onto.init();

    // the ontology is loaded let's access some of its values
    Ontology ontology = (Ontology)onto;
    int classNum = ontology.getOClasses(false).size();
    assertEquals(classNum, 18);
    // count the number of top classes
    Set topclasses = ontology.getOClasses(true);
    assertEquals(topclasses.size(), 5);
    // get the class Department
    OClass aClass = ontology.getOClass(new URI(ontology.getDefaultNameSpace()+"Department", false));
    assertNotNull(aClass);
    // and count the number of super classes
    Set supclassbydist = aClass.getSuperClasses(OConstants.TRANSITIVE_CLOSURE);
    // the list contains 2 arrays of classes i-e 2 levels
    assertEquals(supclassbydist.size(), 2);
    // get the class Department
    aClass = ontology.getOClass(new URI(ontology.getDefaultNameSpace()+"Organization", false));
    assertNotNull(aClass);
    assertTrue(aClass.isTopClass());
    Set subclasses = aClass.getSubClasses(OConstants.TRANSITIVE_CLOSURE);
    assertEquals(subclasses.size(), 5);
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestOntologyAPI.class);
  } // suite
}
