/*
 *  OResource.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OResource.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import java.util.List;
import java.util.Set;

/**
 * This is the top level interface for all ontology resources such as
 * classes, instances and properties.
 */
public interface OResource {
  /**
   * Gets the URI of the resource.
   * 
   * @return the URI.
   */
  public URI getURI();

  /**
   * Sets the URI of the resource
   * 
   * @param uri
   */
  public void setURI(URI uri);

  
  /**
   * This method returns a set of labels specified on this resource.
   * @return
   */
  public Set<Literal> getLabels();
  
  /**
   * This method returns a set of comments specified on this resource.
   * @return
   */
  public Set<Literal> getComments();
  
  /**
   * Gets the comment set on the resource in the specified language.
   * Returns null if no comment found for the specified language.
   * 
   * @param language
   * @return the comment of the resource
   */
  public String getComment(String language);

  /**
   * Sets the comment for the resource with the specified language.
   * 
   * @param aComment the comment to be set.
   * @param language the language of the comment.
   */
  public void setComment(String aComment, String language);

  /**
   * Gets the comment set on the resource in the specified language.
   * Returns null if no comment found for the specified language.
   * 
   * @param language
   * @return the label of the resource
   */
  public String getLabel(String language);

  /**
   * Sets the label for the resource with the specified language.
   * 
   * @param aLabel the label to be set.
   * @param language the anguage of the label.
   */
  public void setLabel(String aLabel, String language);

  /**
   * Gets resource name. Typically a string after the last '#' or '/'
   * 
   * @return the name of the resource.
   */
  public String getName();

  /**
   * Gets the ontology to which the resource belongs.
   * 
   * @return the {@link Ontology} to which the resource belongs
   */
  public Ontology getOntology();

  /**
   * Adds a new annotation property value and specifies the language.
   * 
   * @param theAnnotationProperty the annotation property
   * @param literal the Literal containing some value
   * @return
   */
  public void addAnnotationPropertyValue(
          AnnotationProperty theAnnotationProperty, Literal literal);

  /**
   * Gets the list of values for a given property name.
   * 
   * @param propertyName the name of the property
   * @return a List of {@link Literal}.
   */
  public List<Literal> getAnnotationPropertyValues(
          AnnotationProperty theAnnotationProperty);

  /**
   * This method returns the annotation properties set on this resource.
   * @return
   */
  public Set<AnnotationProperty> getSetAnnotationProperties();

  
  /**
   * This method returns all the set properties set on this resource.
   * @return
   */
  public Set<RDFProperty> getAllSetProperties();
  
  
  /**
   * This method returns a set of all applicable properties on this resource.
   * @return
   */
  public Set<RDFProperty> getProperties();
  
  /**
   * Checks if the resource has the provided annotation property set on it with the specified value.
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasAnnotationPropertyWithValue(AnnotationProperty aProperty, Literal aValue);
  

  
  /**
   * For the current resource, the method removes the given literal for
   * the given property.
   * 
   * @param theAnnotationProperty
   * @param literal
   * @return
   */
  public void removeAnnotationPropertyValue(
          AnnotationProperty theAnnotationProperty, Literal literal);

  /**
   * Removes all values for a named property.
   * 
   * @param theProperty the property
   */
  public void removeAnnotationPropertyValues(AnnotationProperty theProperty);

}
