/*
 *  TestCreole.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 16/Mar/00
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;

/** CREOLE test class
  */
public class TestCreole extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestCreole(String name) throws GateException { super(name); }

  /** Local shorthand for the CREOLE register */
  private CreoleRegister reg;

  /** Fixture set up */
  public void setUp() throws Exception {
    // Initialise the GATE library and creole register
    Gate.init();

    // clear the register and the creole directory set
    reg = Gate.getCreoleRegister();
    reg.clear();
    reg.getDirectories().clear();

    // find a URL for finding test files and add to the directory set
    URL testUrl = Gate.getUrl("tests/");
    reg.addDirectory(testUrl);

    reg.registerDirectories();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
    reg.clear();
    Gate.init();
  } // tearDown

  /** Test resource discovery */
  public void testDiscovery() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }

    ResourceData rd = (ResourceData)
      reg.get("gate.creole.tokeniser.DefaultTokeniser");
    assertNotNull("couldn't find unicode tok in register of resources", rd);
    assert(rd.getName().equals("Gate Unicode Tokeniser"));

    String docFormatName = "gate.corpora.XmlDocumentFormat";
    ResourceData xmlDocFormatRD = (ResourceData) reg.get(docFormatName);
    assert(xmlDocFormatRD.getName().equals("Sheffield XML Document Format"));
    assert(xmlDocFormatRD.isAutoLoading());
    assert(xmlDocFormatRD.getJarFileName().equals("ShefDocumentFormats.jar"));
  } // testDiscovery()

  /** Test resource metadata */
  public void testMetadata() throws Exception {

    // get some res data from the register
    ResourceData pr1rd = (ResourceData) reg.get("testpkg.TestPR1");
    ResourceData pr2rd = (ResourceData) reg.get("testpkg.TestPR2");
    assert(pr1rd != null & pr2rd != null);
    assert(pr2rd.getName().equals("Sheffield Test PR 2"));

    // checks values of parameters of param0 in test pr 1
    assert(pr1rd.getClassName().equals("testpkg.TestPR1"));
    Iterator iter = pr1rd.getParameterList().getRuntimeParameters().iterator();
    Iterator iter2 = null;
    Parameter param = null;
    while(iter.hasNext()) {
      iter2 = ((List) iter.next()).iterator();
      while(iter2.hasNext()) {
        param = (Parameter) iter2.next();
        if(param.typeName.equals("param0"))
          break;
      }
      if(param.typeName.equals("param0"))
        break;
    }

    assert("param0 was null", param != null);
    assert(param.typeName.equals("param0"));
    assert(! param.optional);
    assert(param.runtime);
    assert(param.comment == null);
    assert(param.defaultValue == null);
    assert(param.name.equals("HOOPY DOOPY"));

    reg.clear();
  } // testMetadata()

  /** Test resource loading */
  public void testLoading() throws Exception {

    // get some res data from the register
    assert(
      "wrong number of resources in the register: " + reg.size(),
      reg.size() == 7
    );
    ResourceData pr1rd = (ResourceData) reg.get("testpkg.TestPR1");
    ResourceData pr2rd = (ResourceData) reg.get("testpkg.TestPR2");
    assert("couldn't find PR1/PR2 res data", pr1rd != null && pr2rd != null);
    assert("wrong name on PR1", pr1rd.getName().equals("Sheffield Test PR 1"));

    // instantiation
    ProcessingResource pr1 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR1", Factory.newFeatureMap());
    ProcessingResource pr2 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR2", Factory.newFeatureMap());

    // run the beasts
    FeatureMap pr1features = pr1.getFeatures();
    FeatureMap pr2features = pr2.getFeatures();
    assertNotNull("PR1 features are null", pr1features);
    assert(
      "PR2 got some features from somewhere: " + pr2features,
      pr2features == null || pr2features.isEmpty()
    );
    pr1.run();
    pr2.run();
    assert(
      "PR1 feature not present",
      pr1.getFeatures().get("I").equals("have been run, thankyou")
    );
    assert(
      "PR2 feature not present",
      pr2.getFeatures().get("I").equals("am in a bad mood")
    );

    reg.clear();
  } // testLoading()

  /** Test resource indexing by class */
  public void testClassIndex() throws Exception {

    ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
    assertNotNull("couldn't find document res data", docRd);
    assert(
      "doc res data has wrong class name",
      docRd.getClassName().equals("gate.corpora.DocumentImpl")
    );
    assert(
      "doc res data has wrong interface name",
      docRd.getInterfaceName().equals("gate.Document")
    );

    Class docClass = docRd.getResourceClass();
    assertNotNull("couldn't get doc class", docClass);
    LanguageResource docRes = (LanguageResource) docClass.newInstance();
    assert(
      "instance of doc is wrong type",
      docRes instanceof LanguageResource &&
      docRes instanceof gate.Document
    );

    reg.clear();
  } // testClassIndex()

  /** Test comments on resources */
  public void testComments() throws Exception {

    ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
    assertNotNull("testComments: couldn't find document res data", docRd);
    String comment = docRd.getComment();
    assert(
      "testComments: incorrect or missing COMMENT on document",
      comment != null && comment.equals("GATE document")
    );
  } // testComments()

  /** Test parameter defaults */
  public void testParameterDefaults1() throws Exception {

    ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
    assertNotNull("Couldn: couldn't find document res data", docRd);

    ParameterList paramList = docRd.getParameterList();
    if(DEBUG) Out.prln(docRd);

    // runtime params - none for a document
    Iterator iter = paramList.getRuntimeParameters().iterator();
    assert("Document has runtime params: " + paramList, ! iter.hasNext());

    // init time params
    Parameter param = null;
    iter = paramList.getInitimeParameters().iterator();
    while(iter.hasNext()) {
      List paramDisj = (List) iter.next();
      Iterator iter2 = paramDisj.iterator();

      for(int i=0; iter2.hasNext(); i++) {
        param = (Parameter) iter2.next();

        switch(i) {
          case 0:
            assert(
              "Doc param 0 wrong type: " + param.getTypeName(),
              param.getTypeName().equals("java.lang.String")
            );
            assert(
              "Doc param 0 wrong name: " + param.getName(),
              param.getName().equals("sourceUrlName")
            );
            Object defaultValue = param.calculateDefaultValue();
            assert(
              "Doc param 0 default should be null but was: " + defaultValue,
              defaultValue == null
            );
            break;
          case 1:
            assert(
              "Doc param 1 wrong name: " + param.getName(),
              param.getName().equals("encoding")
            );
            break;
          case 2:
            assert(
              "Doc param 2 wrong name: " + param.getName(),
              param.getName().equals("sourceUrlStartOffset")
            );
            break;
          case 3:
            assert(
              "Doc param 3 wrong name: " + param.getName(),
              param.getName().equals("sourceUrlEndOffset")
            );
            break;
          default:
            fail("Doc has more than 4 params; 5th is: " + param);
        } // switch
      }
    }

  } // testParameterDefaults1()

  /** Test parameter defaults (2) */
  public void testParameterDefaults2() throws Exception {

    ResourceData rd = (ResourceData) reg.get("testpkg.PrintOutTokens");
    assertNotNull("Couldn't find testpkg.POT res data", rd);

    // create a document, so that the parameter default will pick it up
    Factory.newDocument(Gate.getUrl("tests/doc0.html"));

    ParameterList paramList = rd.getParameterList();
    if(DEBUG) Out.prln(rd);

    // init time params - none for this one
    Iterator iter = paramList.getInitimeParameters().iterator();
    assert("POT has initime params: " + paramList, ! iter.hasNext());

    // runtime params
    Parameter param = null;
    iter = paramList.getRuntimeParameters().iterator();
    while(iter.hasNext()) {
      List paramDisj = (List) iter.next();
      Iterator iter2 = paramDisj.iterator();

      for(int i=0; iter2.hasNext(); i++) {
        param = (Parameter) iter2.next();

        switch(i) {
          case 0:
            assert(
              "POT param 0 wrong type: " + param.getTypeName(),
              param.getTypeName().equals("gate.corpora.DocumentImpl")
            );
            assert(
              "POT param 0 wrong name: " + param.getName(),
              param.getName().equals("document")
            );
            Object defaultValue = param.calculateDefaultValue();
            assert(
              "POT param 0 default should be Document but is " +
              defaultValue.getClass().getName(),
              defaultValue instanceof Document
            );
            break;
          default:
            fail("POT has more than 1 param; 2nd is: " + param);
        } // switch
      }
    }

  } // testParameterDefaults2()

  /** Test default run() on processing resources */
  public void testDefaultRun() throws Exception {
    ProcessingResource defaultPr = new AbstractProcessingResource() {
    };
    defaultPr.run();
    boolean gotExceptionAsExpected = false;
    try {
      defaultPr.check();
    } catch(ExecutionException e) {
      gotExceptionAsExpected = true;
    }

    assert("check should have thrown exception", gotExceptionAsExpected);
  } // testDefaultRun()

  /** Test arbitrary metadata elements on resources */
  public void testArbitraryMetadata() throws Exception {

    ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
    assertNotNull("testArbitraryMetadata: couldn't find doc res data", docRd);
    FeatureMap features = docRd.getFeatures();
    String comment = (String) features.get("FUNKY-METADATA-THAING");
    assert(
      "testArbitraryMetadata: incorrect FUNKY-METADATA-THAING on document",
      comment != null && comment.equals("hubba hubba")
    );
  } // testArbitraryMetadata()

  /** Test resource introspection */
  public void testIntrospection() throws Exception {
    // get the gate.Document resource and its class
    ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
    assertNotNull("couldn't find document res data (2)", docRd);
    Class resClass = docRd.getResourceClass();

    // get the beaninfo and property descriptors for the resource
    BeanInfo docBeanInfo = Introspector.getBeanInfo(resClass, Object.class);
    PropertyDescriptor[] propDescrs = docBeanInfo.getPropertyDescriptors();

    // print all the properties in the reource's bean info;
    // remember the setFeatures method
    Method setFeaturesMethod = null;
    for(int i = 0; i<propDescrs.length; i++) {
      Method getMethodDescr = null;
      Method setMethodDescr = null;
      Class propClass = null;

      PropertyDescriptor propDescr = propDescrs[i];
      propClass = propDescr.getPropertyType();
      getMethodDescr = propDescr.getReadMethod();
      setMethodDescr = propDescr.getWriteMethod();

      if(
        setMethodDescr != null &&
        setMethodDescr.getName().equals("setFeatures")
      )
        setFeaturesMethod = setMethodDescr;

      if(DEBUG) printProperty(propDescrs[i]);
    }

    // try setting the features property
    // invoke(Object obj, Object[] args)
    LanguageResource res = (LanguageResource) resClass.newInstance();
    FeatureMap feats = Factory.newFeatureMap();
    feats.put("things are sunny in sunny countries", "aren't they?");
    Object[] args = new Object[1];
    args[0] = feats;
    setFeaturesMethod.invoke(res, args);
    assert(
      "features not added to resource properly",
      res.getFeatures().get("things are sunny in sunny countries")
        .equals("aren't they?")
    );
  } // testIntrospection

  /** Test the Factory resource creation provisions */
  public void testFactory() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    params.put("features", Factory.newFeatureMap());
    params.put(
      "sourceUrlName",
      Gate.getUrl("tests/doc0.html").toExternalForm()
    );
    Resource res =
      Factory.createResource("gate.corpora.DocumentImpl", params);
  } // testFactory

  /** Utility method to print out the values of a property descriptor
    * @see java.beans.PropertyDescriptor
    */
  public static void printProperty(PropertyDescriptor prop) {
    Class propClass = prop.getPropertyType();
    Method getMethodDescr = prop.getReadMethod();
    Method setMethodDescr = prop.getWriteMethod();
    Out.pr("prop dispname= " + prop.getDisplayName() + "; ");
    Out.pr("prop type name= " + propClass.getName() + "; ");
    if(getMethodDescr != null)
      Out.pr("get meth name= " + getMethodDescr.getName() + "; ");
    if(setMethodDescr != null)
      Out.pr("set meth name= " + setMethodDescr.getName() + "; ");
    Out.prln();
  } // printProperty

  /** Example of what bean info classes do.
    * If this was a public class in gate.corpora it would be used
    * by the beans Introspector to generation bean info for the
    * gate.corpora.DocumentImpl class. It inherits from SimpleBeanInfo
    * whose default behaviour is to return null for the various methods;
    * this tells the Introspector to do its own investigations.
    */
  class DocumentImplBeanInfo extends SimpleBeanInfo {

    /** Override the SimpleBeanInfo behaviour and return a 0-length
      * array of properties; this will be passed on by the Introspector,
      * the effect being to block info on the properties of the bean.
      */
    public PropertyDescriptor[] getPropertyDescriptors() {
      return new PropertyDescriptor[0];
    } // getPropertyDescriptors

  } // DocumentImplBeanInfo

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCreole.class);
  } // suite

} // class TestCreole
