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
  public ResourceData() {  }// ResourceData

  /** String representation */
  public String toString() {
    int noInst = (instantiationStack == null) ? 0: instantiationStack.size();
/*
    int noSmallViews = (smallViews == null) ? 0: smallViews.size();
    int noViews = (views == null) ? 0: views.size();
*/
    StringBuffer s = new StringBuffer(
      "ResourceDataImpl, name=" + name + "; className=" + className +
      "; jarFileName=" + jarFileName + "; jarFileUrl=" + jarFileUrl +
      "; xmlFileName=" + xmlFileName + "; xmlFileUrl=" + xmlFileUrl +
      "; isAutoLoading=" + autoLoading + "; numberInstances=" + noInst +
      "; isPrivate=" + priv +"; isTool="+ tool +
      "; validityMessage=" + validityMessage +
      "; interfaceName=" + interfaceName +
      "; guiType=" + guiType +
      "; mainViewer=" + isMainView +
      "; resourceDisplayed=" + resourceDisplayed +
      "; annotationTypeDisplayed=" + annotationTypeDisplayed +
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

  /////////////////////////////////////////////////////
  // Fields added for GUI element
  /////////////////////////////////////////////////////
  /** This type indicates that the resource is not a GUI */
  public static final int NULL_GUI = 0;
  /**This type indicates that the resource goes into the large area of GATE GUI*/
  public static final int LARGE_GUI = 1;
  /**This type indicates that the resource goes into the small area of GATE GUI*/
  public static final int SMALL_GUI = 2;
  /** A filed which can have one of the 3 predefined values. See above.*/
  protected int guiType = NULL_GUI;
  /** Whether or not this viewer will be the default one*/
  protected boolean isMainView = false;
  /** The full class name of the resource displayed by this viewer.*/
  protected String resourceDisplayed = null;
  /** The full type name of the annotation displayed by this viewer.*/
  protected String annotationTypeDisplayed = null;
  /** A simple mutator for guiType field*/
  public void setGuiType(int aGuiType){guiType = aGuiType;}
  /** A simple accessor for guiType field*/
  public int getGuiType(){return guiType;}
  /** A simple mutator for isMainView field*/
  public void setIsMainView(boolean mainView){isMainView = mainView;}
  /** A simple accessor for isMainView field*/
  public boolean isMainView(){return isMainView;}
  /** A simple mutator for resourceDisplayed field*/
  public void setResourceDisplayed(String aResourceDisplayed){
    resourceDisplayed = aResourceDisplayed;
  }// setResourceDisplayed
  /** A simple accessor for resourceDisplayed field*/
  public String getResourceDisplayed(){return resourceDisplayed;}
  /** A simple mutator for annotationTypeDisplayed field*/
  public void setAnnotationTypeDisplayed(String anAnnotationTypeDisplayed){
    annotationTypeDisplayed = anAnnotationTypeDisplayed;
  }// setAnnotationTypeDisplayed
  /** A simple accessor for annotationTypeDisplayed field*/
  public String getAnnotationTypeDisplayed(){return annotationTypeDisplayed;}
} // ResourceData
