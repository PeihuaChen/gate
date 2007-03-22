/*
 *  OntologyModificationListener.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyModificationListener.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import java.util.EventListener;

/**
 * Objects wishing to listen to various ontology events, must implement
 * this interface (using implements java keyword) and the methods of
 * this interface. They must get registered themselves with the
 * respective ontology by using the
 * ontology.addOntologyModificationListener(OntologyModificationListener)
 * method.
 * 
 * @author niraj
 * 
 */
public interface OntologyModificationListener extends EventListener {
  /**
   * This method is invoked whenever the respective ontology is
   * modified. This doesnot involve addition and deletion of resources
   * (e.g. classes/properties/instances) in the ontology but
   * addition/removal/change in property values, relations (e.g.
   * addition of sub/super class/property etc) among
   * classes/properties/instances.
   * 
   * @param ontology the source of the event
   * @param resouce the affected OResource
   * @param eventType the type of an event (@see OConstants) for more
   *          details
   */
  public void ontologyModified(Ontology ontology, OResource resource,
          int eventType);

  /**
   * This method is invoked whenever a resource
   * (class/property/instance) is removed from the ontology.
   * 
   * @param ontology the source of the event
   * @param resources an array of URIs of resources which were deleted
   *          (including the resource which was asked to be deleted).
   */
  public void resourcesRemoved(Ontology ontology, String[] resources);

  /**
   * This method is invoked whenever a resource
   * (class/property/instance) is added to the ontology.
   * 
   * @param ontology the source of the event
   * @param resource an instance of OResource, which was created as a
   *          result of addition of a resource.
   */
  public void resourceAdded(Ontology ontology, OResource resource);
}
