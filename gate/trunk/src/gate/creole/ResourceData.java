/*
 *  ResourceData.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.util.*;

/** Models an individual CREOLE resource metadata, plus configuration data,
  * plus the instantiations of the resource current within the system.
  * Some metadata elements are used by GATE to load resources, or index
  * the members of the CREOLE register; some are used during resource
  * parameterisation and initialisation.
  * Metadata elements which are used by the CREOLE registration and loading
  * mechanisms are properties of ResourceData implementations and have their
  * own get/set methods. Other metadata elements are made features of the
  * ResourceData. So, for example, if you add an element "FunkyElementThaing"
  * to the metadata of a resource, this will be made a feature of that
  * resource's ResourceData.
  * @see CreoleRegister
  */
public class ResourceData extends AbstractFeatureBearer implements Serializable
{

  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** Construction */
  public ResourceData() { }

  /** String representation */
  public String toString() {
    StringBuffer s = new StringBuffer(
      "ResourceDataImpl, name=" + name + "; className=" + className +
      "; jarFileName=" + jarFileName + "; jarFileUrl=" + jarFileUrl +
      "; xmlFileName=" + xmlFileName + "; xmlFileUrl=" + xmlFileUrl +
      "; autoLoading=" + autoLoading + "; interfaceName=" + interfaceName +
      "; parameterList=" + parameterList +
      "; features=" + features
    );
    return s.toString();
  } // toString

  /** Equality: two resource data objects are the same if they have the
    * same name
    */
  public boolean equals(Object other) {
    if(name.equals(((ResourceData) other).getName()))
      return true;
    return false;
  } // equals

  /** Hashing, based on the name field of the object */
  public int hashCode() {
    return name.hashCode();
  } // hashCode

  /** The name of the resource */
  protected String name;

  /** Set method for the resource name */
  public void setName(String name) { this.name = name; }

  /** Get method for the resource name */
  public String getName() { return name; }

  /** Location of an icon for the resource */
  protected String icon;

  /** Set method for the resource icon */
  public void setIcon(String icon) { this.icon = icon; }

  /** Get method for the resource icon */
  public String getIcon() { return icon; }

  /** The stack of instantiations */
  protected BumpyStack instantiationStack = new BumpyStack();

  /** Get the list of instantiations of resources */
  public BumpyStack getInstantiations() {
    return instantiationStack;
  } // getInstantiations

  /** Add an instantiation of the resource to the register of these */
  public void addInstantiation(Resource resource) {
    instantiationStack.push(resource);
  } // addInstantiation

  /** Remove an instantiation of the resource from the register of these */
  public void removeInstantiation(Resource resource) {
    instantiationStack.remove(resource);
  } // removeInstantiation

  /** Bump an instantiation to the top of the instantiation stack */
  public void bumpInstantiation(Resource resource) {
    instantiationStack.bump(resource);
  } // bumpInstantiation

  /** The class name of the resource */
  protected String className;

  /** Set method for the resource class name */
  public void setClassName(String className) { this.className = className; }

  /** Get method for the resource class name */
  public String getClassName() { return className; }

  /** The interface name of the resource */
  protected String interfaceName;

  /** Set method for the resource interface name */
  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  } // setInterfaceName

  /** Get method for the resource interface name */
  public String getInterfaceName() { return interfaceName; }

  /** The class of the resource */
  protected Class resourceClass;

  /** Set method for the resource class */
  public void setResourceClass(Class resourceClass) {
    this.resourceClass = resourceClass;
  } // setResourceClass

  /** Get method for the resource class. Asks the GATE class loader
    * to load it, if it is not already present.
    */
  public Class getResourceClass() throws ClassNotFoundException {
    if(resourceClass == null) {
      GateClassLoader classLoader = Gate.getClassLoader();
      resourceClass = classLoader.loadClass(className);
    }

    return resourceClass;
  } // getResourceClass

  /** The jar file name of the resource */
  protected String jarFileName;

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName) {
    this.jarFileName = jarFileName;
  } // setJarFileName

  /** Get method for the resource jar file name */
  public String getJarFileName() { return jarFileName; }

  /** The jar file URL of the resource */
  protected URL jarFileUrl;

  /** Set method for the resource jar file URL */
  public void setJarFileUrl(URL jarFileUrl) { this.jarFileUrl = jarFileUrl; }

  /** Get method for the resource jar file URL */
  public URL getJarFileUrl() { return jarFileUrl; }

  /** The xml file name of the resource */
  protected String xmlFileName;

  /** Set method for the resource xml file name */
  public void setXmlFileName(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  } // setXmlFileName

  /** Get method for the resource xml file name */
  public String getXmlFileName() { return xmlFileName; }

  /** The xml file URL of the resource */
  protected URL xmlFileUrl;

  /** Set method for the resource xml file URL */
  public void setXmlFileUrl(URL xmlFileUrl) { this.xmlFileUrl = xmlFileUrl; }

  /**@deprecated Get method for the resource xml file URL */
  public URL getXmlFileUrl() { return xmlFileUrl; }

  /** The comment string */
  protected String comment;

  /** Get method for the resource comment */
  public String getComment() { return comment; }

  /** Set method for the resource comment */
  public void setComment(String comment) { this.comment = comment; }

  /** The set of parameter lists */
  protected ParameterList parameterList = new ParameterList();

  /** Set the parameter list */
  public void setParameterList(ParameterList parameterList) {
    this.parameterList = parameterList;
  } // addParameterList

  /** Get the parameter list */
  public ParameterList getParameterList() { return parameterList; }

  /** Autoloading flag */
  protected boolean autoLoading;

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading) {
    this.autoLoading = autoLoading;
  } // setAutoLoading

  /** Is the resource autoloading? */
  public boolean isAutoLoading() { return autoLoading; }

  /** Private flag */
  protected boolean priv = false;

  /** Set method for resource private flag */
  public void setPrivate(boolean priv) {
    this.priv = priv;
  } // setPrivate

  /** Is the resource private? */
  public boolean isPrivate() { return priv; }

  /** Tool flag */
  protected boolean tool = false;

  /** Set method for resource tool flag */
  public void setTool(boolean tool) {
    this.tool = tool;
  } // setTool

  /** Is the resource a tool? */
  public boolean isTool() { return tool; }

  /** Add a view (a feature map minimally defining the view's
    * TYPE and TITLE).
    */
  public void addView(FeatureMap viewFeatures) {
    String small = (String) viewFeatures.get("SMALL");
    if(small != null && small.toUpperCase().equals("TRUE"))
      smallViews.add(viewFeatures);
    else
      views.add(viewFeatures);
  } // addView(FeatureMap)

  /** Get the views registered for this resource. Each member of the
    * list is a FeatureMap with a TYPE attribute giving the class name
    * of the viewer. Other data to be passed to the viewer is present as
    * other features on the map.
    * <P>
    * This list excludes views that have the SMALL attribute set true.
    */
  public List getViews() { return views; }

  /** Get the views registered for this resource. Each member of the
    * list is a FeatureMap with a TYPE attribute giving the class name
    * of the viewer. Other data to be passed to the viewer is present as
    * other features on the map.
    * <P>
    * This list contains only those views that have the SMALL attribute
    * set true.
    */
  public List getSmallViews() { return smallViews; }

  /** Get all the (not small) views of this resource and those Resource classes
    * that it inherits from. The method uses reflection to traverse the
    * inheritance tree as far as gate.Resource; for each inherited class,
    * if it is itself a resource, its views are added to the list.
    * <P>
    * Each member of the
    * list is a FeatureMap with a TYPE attribute giving the class name
    * of the vcvs upiewer. Other data to be passed to the viewer is present as
    * other features on the map.
    */
  public List getAllViews() { return getAllViews(false); }

  /** Get all the small views of this resource and those Resource classes that
    * it inherits from. The method uses reflection to traverse the
    * inheritance tree as far as gate.Resource; for each inherited class,
    * if it is itself a resource, its views are added to the list.
    * <P>
    * Each member of the
    * list is a FeatureMap with a TYPE attribute giving the class name
    * of the vcvs upiewer. Other data to be passed to the viewer is present as
    * other features on the map.
    */
  public List getAllSmallViews() { return getAllViews(true); }

  /** Get all the views of this resource and those Resource classes that
    * it inherits from. The method uses reflection to traverse the
    * inheritance tree as far as gate.Resource; for each inherited class,
    * if it is itself a resource, its views are added to the list.
    * <P>
    * Each member of the
    * list is a FeatureMap with a TYPE attribute giving the class name
    * of the vcvs upiewer. Other data to be passed to the viewer is present as
    * other features on the map.
    * @param small If true then small views are returned; else non-small views
    */
  public List getAllViews(boolean small) {
    List allViews = new ArrayList();
    CreoleRegister reg = Gate.getCreoleRegister();

    // add all the views for the current class
    if(small)
      allViews.addAll(smallViews);
    else
      allViews.addAll(views);

    // get the class for this resource or give up
    Class resClass = null;
    try { resClass = getResourceClass(); } catch(ClassNotFoundException e) { }
    if(resClass == null) return allViews; // no inherited views

    // iterate over all superclasses up to gate.Resource
    for(
        Class superClass = resClass.getSuperclass()    // iterate up from super
      ;
        superClass != null &&                          // top of the tree
        ! superClass.getName().equals("gate.Resource")
      ;
        superClass = superClass.getSuperclass()        // on to the next
    ) {
      if(Resource.class.isAssignableFrom(superClass)) {
        ResourceData superResData =
          (ResourceData) reg.get(superClass.getName());
        if(superResData == null) continue;

        if(small)
          allViews.addAll(superResData.getSmallViews());
        else
          allViews.addAll(superResData.getViews());
      }
    } // for

    return allViews;
  } // getAllViews(boolean)

  /** The list of views registered for this resource */
  protected List views = new ArrayList();

  /** The list of small views registered for this resource */
  protected List smallViews = new ArrayList();

  /** Is this a valid resource data configuration? If not, leave an
    * error message that can be returned by <TT>getValidityMessage()</TT>.
    */
  public boolean isValid() {
    boolean valid = true;
//******************************
// here should check that the resource has all mandatory elements,
// e.g. class name, and non-presence of runtime params on LRs and VRs etc.
//******************************
    return valid;
  } // isValid()

  /** Status message set by isValid() */
  protected String validityMessage = "";

  /** Get validity statues message. */
  public String getValidityMessage() { return validityMessage; }

} // ResourceData
