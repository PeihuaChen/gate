/*
 *	TestCreole.java
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
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
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
    // Initialise the creole register
    Gate.init();
    Gate.initCreoleRegister();

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

  /** Test resource discovery */
  public void testDiscovery() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }

    ResourceData rd = (ResourceData) reg.get("gate.creole.Tokeniser");
    assertNotNull("couldn't find unicode tok in register of resources", rd);
    assert(rd.getName().equals("Sheffield Unicode Tokeniser"));

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
    Iterator iter = pr1rd.getParameterListsSet().iterator();
    Iterator iter2 = null;
    Parameter param = null;
    while(iter.hasNext()) {
      iter2 = ((List) iter.next()).iterator();
      while(iter2.hasNext()) {
        param = (Parameter) iter2.next();
        if(param.valueString.equals("param0"))
          break;
      }
      if(param.valueString.equals("param0"))
        break;
    }
    assert("param0 was null", param != null);
    assert(param.valueString.equals("param0"));
    assert(! param.optional);
    assert(param.runtime);
    assert(param.comment == null);
    assert(param.defaultValueString == null);
    assert(param.name.equals("HOOPY DOOPY"));

    reg.clear();
  } // testMetadata()

  /** Test resource loading */
  public void testLoading() throws Exception {

    // get some res data from the register
    assert("wrong number of resources in the register", reg.size() == 6);
    //ResourceData pr1rd = (ResourceData) reg.get("Sheffield Test PR 1");
    //ResourceData pr2rd = (ResourceData) reg.get("Sheffield Test PR 2");
    ResourceData pr1rd = (ResourceData) reg.get("testpkg.TestPR1");
    ResourceData pr2rd = (ResourceData) reg.get("testpkg.TestPR2");
    assert("couldn't find PR1/PR2 res data", pr1rd != null && pr2rd != null);
    assert("wrong name on PR1", pr1rd.getName().equals("Sheffield Test PR 1"));

// try instantiation here

    reg.clear();
  } // testLoading()

  /** Test resource indexing by interface */
  public void testInterfaceIndex() throws Exception {

    ResourceData docRd = (ResourceData) reg.get("gate.Document");
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
  } // testInterfaceIndex()

  /** Test resource introspection */
  public void testIntrospection() throws Exception {
    // get the gate.Document resource and its class
    ResourceData docRd = (ResourceData) reg.get("gate.Document");
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

      if(DEBUG) {
        Out.pr("prop dispname= " + propDescrs[i].getDisplayName() + "; ");
        Out.pr("prop type name= " + propClass.getName() + "; ");
        if(getMethodDescr != null)
          Out.pr("get meth name= " + getMethodDescr.getName() + "; ");
        if(setMethodDescr != null)
          Out.pr("set meth name= " + setMethodDescr.getName() + "; ");
        Out.prln();
      }
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
    Resource res = Factory.createResource("gate.Document", params);
  } // testFactory

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
