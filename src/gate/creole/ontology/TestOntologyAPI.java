package gate.creole.ontology;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.jena.JenaOntologyImpl;
import junit.framework.TestCase;

/**
 * Simple test class that load an ontology available online and 
 * accesses its content via the ontology API 
 **/
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
    JenaOntologyImpl onto = new JenaOntologyImpl();
    URL url = new URL("http://gate.ac.uk/tests/demo.owl");
    onto.load(url, JenaOntologyImpl.OWL_LITE);
    // the ontology is loaded let's access some of its values
    Ontology ontology = (Ontology)onto;
    int classNum = ontology.getClasses().size();
    assertEquals(classNum, 18);
    // get a specific class
    TClass aClass = ontology.getClassByName(null);
    assertNull(aClass);
    // count the number of top classes
    Set topclasses = ontology.getTopClasses();
    assertEquals(topclasses.size(), 5);
    // get the class Department
    aClass = ontology.getClassByName("Department");
    assertNotNull(aClass);
    // and count the number of super classes
    List supclassbydist = aClass.getSuperClassesVSDistance();
    // the list contains 2 arrays of classes i-e 2 levels
    assertEquals(supclassbydist.size(), 2);
    // get the class Department
    aClass = ontology.getClassByName("Organization");
    assertNotNull(aClass);
    assertTrue(aClass.isTopClass());
    List subclasses = aClass.getSubClassesVSDistance();
    assertEquals(subclasses.size(), 2);
  }
}
