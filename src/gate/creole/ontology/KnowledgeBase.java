/* KnowledgeBase.java
 *
 * Copyright (c) 2002-2003, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Kalina Bontcheva 03/2003
 *
 */

package gate.creole.ontology;

import java.util.List;

public interface KnowledgeBase extends Ontology {

  /**Adds an instance to the knowledge base.
   * @param name the instance name to be added
   * @param theClass the class to be added
   * @return the OInstance that has been added to the KB */
  public OInstance addInstance(String name, OClass theClass);

  /**Adds an instance to the knowledge base.*/
  public void addInstance(OInstance theInstance);

  /**Removes the instance from the knowledge base.
   * @param theInstance to be removed */
  public void removeInstance(OInstance theInstance);

  /**Gets all instances in the KB.
   * @return List of OInstance objects */
  public List getInstances();

  /**Gets all instances in the KB, which belong to this class,
   * including instances of sub-classes. If only the instances
   * of the given class are needed, then use getDirectInstances.
   * @param theClass the class of the instances
   * @return List of OInstance objects */
  public List getInstances(OClass theClass);

  /**Gets all instances in the KB, which belong to the
   * given class only.
   * @param theClass the class of the instances
   * @return List of OInstance objects */
  public List getDirectInstances(OClass theClass);

  /**Gets the instance with the given name.
   * @param String the instance name
   * @return the OInstance object with this name */
  public OInstance getInstanceByName(String instanceName);



}