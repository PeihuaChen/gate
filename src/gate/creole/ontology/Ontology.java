/*
 *  Ontology.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id$
 */
package gate.creole.ontology;

import gate.LanguageResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Set;
import org.openrdf.sesame.repository.SesameRepository;

/**
 * This is the base interface for all concrete implementations of
 * ontologies.
 */
public interface Ontology extends LanguageResource {
  // *****************************
  // Ontology related methods and not specific to its members (e.g.
  // classes,
  // properties and so on)
  // *****************************
  /**
   * This method removes the entire data from the ontology and emptys
   * it.
   */
  public void cleanOntology();

  /**
   * The data from ontology repository is retrieved in the specified
   * format. (@see OntologyConstants)
   * 
   * @param format
   * @return
   */
  public String getOntologyData(byte format);

  /**
   * Exports the ontology data into the provided format to the provided
   * output stream.
   * 
   * @param out
   * @param format <@see OConstants>
   */
  public void writeOntologyData(OutputStream out, byte format);

  /**
   * Exports the ontology data into the provided format using the
   * provided writer.
   * 
   * @param out
   * @param format
   */
  public void writeOntologyData(Writer out, byte format);

  /**
   * Gets the url of this ontology
   * 
   * @return the url of this ontology
   */
  public URL getURL();

  /**
   * Set the url of this ontology
   * 
   * @param aUrl the url to be set
   */
  public void setURL(URL aUrl);

  /**
   * Saves the ontology in the provided File
   */
  public void store(File newOntology) throws IOException;

  /**
   * Sets the default name space, which is (by default) used for the
   * newly created resources.
   * 
   * @param theURI the URI to be set
   */
  public void setDefaultNameSpace(String aNameSpace);

  /**
   * Gets the default name space for this ontology. The defaultNameSpace
   * is (by default) used for the newly created resources.
   * 
   * @return a String value.
   */
  public String getDefaultNameSpace();

  /**
   * Sets version to this ontology.
   * 
   * @param theVersion the version to be set
   */
  public void setVersion(String theVersion);

  /**
   * Gets the version of this ontology.
   * 
   * @return the version of this ontology
   */
  public String getVersion();

  // *****************************
  // OClass methods
  // *****************************
  /**
   * Creates a new OClass and adds it the ontology.
   * 
   * @param aURI URI of this class
   * @param classType one of the values from
   *          OConstants.OCLASS_TYPE_OWL_CLASS and
   *          OConstants.OCLASS_TYPE_OWL_RESTRICTION;
   * @return the newly created class or an existing class if available
   *         with the same URI.
   */
  public OClass addOClass(URI aURI, byte classType);

  /**
   * Creates a new OWL_Class and adds it the ontology.
   * 
   * @param aURI URI of this class
   * @return the newly created class or an existing class if available
   *         with the same URI.
   */
  public OClass addOClass(URI aURI);
  
  
  /**
   * Retrieves a class by its URI.
   * 
   * @param theClassURI the URI of the class
   * @return the class matching the name or null if no matches.
   */
  public OClass getOClass(URI theClassURI);

  /**
   * Removes a class from this ontology.
   * 
   * @param theClass the class to be removed
   * @return
   */
  public void removeOClass(OClass theClass);

  /**
   * Checks whether the provided URI is a type of RDFS.CLASS or
   * OWL.CLASS and that the ontology contains this class.
   * 
   * @param theURI
   * @return true, if the class exists and is a type of RDFS.CLASS or
   *         OWL.CLASS, otherwise - false.
   */
  public boolean containsOClass(URI theURI);

  /**
   * Checks whether the ontology contains this class or not.
   * 
   * @param theClass
   * @return true, if the class exists, otherwise - false.
   */
  public boolean containsOClass(OClass theClass);

  /**
   * Retrieves all classes as a set.
   * 
   * @param top If set to true, only returns those classes with no super
   *          classes, otherwise - a set of all classes.
   * @return set of all the classes in this ontology
   */
  public Set<OClass> getOClasses(boolean top);

  /**
   * Gets the taxonomic distance between 2 classes.
   * 
   * @param class1 the first class
   * @param class2 the second class
   * @return the taxonomic distance between the 2 classes
   */
  public int getDistance(OClass class1, OClass class2);

  // *****************************
  // OInstance methods
  // *****************************
  /**
   * Creates a new OInstance and returns it.
   * 
   * @param theURI the URI for the new instance.
   * @param theClass the class to which the instance belongs.
   * @return the OInstance that has been added to the ontology.
   */
  public OInstance addOInstance(URI theInstanceURI, OClass theClass);

  /**
   * Removes the instance from the ontology.
   * 
   * @param theInstance to be removed
   */
  public void removeOInstance(OInstance theInstance);

  /**
   * Gets all instances in the ontology.
   * 
   * @return a {@link Set} of OInstance objects
   */
  public Set<OInstance> getOInstances();

  /**
   * Gets instances in the ontology, which belong to this class. If only
   * the instances of the given class are needed, then the value of
   * direct should be set to true.
   * 
   * @param theClass the class of the instances
   * @param closure either DIRECT_CLOSURE or TRANSITIVE_CLOSURE of
   *          {@link OConstants}
   * 
   * @return {@link Set} of OInstance objects
   */
  public Set<OInstance> getOInstances(OClass theClass, byte closure);

  /**
   * Gets the instance with the given URI.
   * 
   * @param theInstanceURI the instance URI
   * @return the OInstance object with this URI. If there is no such
   *         instance then null.
   */
  public OInstance getOInstance(URI theInstanceURI);

  /**
   * Checks whether the provided Instance exists in the ontology.
   * 
   * @param theInstance
   * @return true, if the Instance exists in ontology, otherwise -
   *         false.
   */
  public boolean containsOInstance(OInstance theInstance);

  /**
   * Checks whether the provided URI refers to an Instance that exists
   * in the ontology.
   * 
   * @param theInstanceURI
   * @return true, if the URI exists in ontology and refers to an
   *         Instance, otherwise - false.
   */
  public boolean containsOInstance(URI theInstanceURI);

  // *****************************
  // Property definitions methods
  // *****************************
  /**
   * Creates a new RDFProperty.
   * 
   * @param aPropertyURI URI of the property to be added into the
   *          ontology.
   * @param domain a set of {@link OResource} (e.g. a Class, a Property
   *          etc.).
   * @param range a set of {@link OResource} (e.g. a Class, a Property
   *          etc.).
   */
  public RDFProperty addRDFProperty(URI aPropertyURI, Set<OResource> domain,
          Set<OResource> range);

  /**
   * Gets the set of RDF Properties in the ontology where for a property
   * there exists a statement <theProperty, RDF:Type, RDF:Property>.
   * 
   * @return a {@link Set} of {@link Property}.
   */
  public Set<RDFProperty> getRDFProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * RDF:Property> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isRDFProperty(URI thePropertyURI);

  /**
   * Creates a new AnnotationProperty.
   * 
   * @param aPropertyURI URI of the property to be added into the
   *          ontology.
   */
  public AnnotationProperty addAnnotationProperty(URI aPropertyURI);

  /**
   * Gets the set of Annotation Properties in the ontology where for a
   * property there exists a statement <theProperty, RDF:Type,
   * OWL:AnnotationProperty>.
   * 
   * @return a {@link Set} of {@link AnnotationProperty}.
   */
  public Set<AnnotationProperty> getAnnotationProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * OWL:AnnotationProperty> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isAnnotationProperty(URI thePropertyURI);

  /**
   * Create a DatatypeProperty with the given domain and range.
   * 
   * @param aPropertyURI the URI for the new property.
   * @param domain the set of ontology classes (i.e. {@link OClass}
   *          objects} that constitutes the range for the new property.
   *          The property only applies to instances that belong to
   *          <b>all</b> classes included in its domain. An empty set
   *          means that the property applies to instances of any class.
   * @param range the range specifying the {@link DataType} of a value
   *          that this property can have.
   * @return the newly created property.
   */
  public DatatypeProperty addDatatypeProperty(URI aPropertyURI,
          Set<OClass> domain, DataType aDatatype);

  /**
   * Gets the set of Datatype Properties in the ontology.
   * 
   * @return a {@link Set} of {@link DatatypeProperty}.
   */
  public Set<DatatypeProperty> getDatatypeProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * OWL:DatatypeProperty> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isDatatypeProperty(URI thePropertyURI);

  /**
   * Creates a new object property (a property that takes instances as
   * values).
   * 
   * @param aPropertyURI the URI for the new property.
   * @param domain the set of ontology classes (i.e. {@link OClass}
   *          objects} that constitutes the range for the new property.
   *          The property only applies to instances that belong to
   *          <b>all</b> classes included in its domain. An empty set
   *          means that the property applies to instances of any class.
   * @param range the set of ontology classes (i.e. {@link OClass}
   *          objects} that constitutes the range for the new property.
   * @return the newly created property.
   */
  public ObjectProperty addObjectProperty(URI aPropertyURI, Set<OClass> domain,
          Set<OClass> range);

  /**
   * Gets the set of Object Properties in the ontology.
   * 
   * @return a {@link Set} of {@link ObjectProperty}.
   */
  public Set<ObjectProperty> getObjectProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * OWL:ObjectProperty> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isObjectProperty(URI thePropertyURI);

  /**
   * Creates a new symmetric property (an object property that is
   * symmetric).
   * 
   * @param aPropertyURI the URI for the new property.
   * @param domainAndRange the set of ontology classes (i.e.
   *          {@link OClass} objects} that constitutes the domain and
   *          the range for the new property. The property only applies
   *          to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property
   *          applies to instances of any class.
   * @return the newly created property.
   */
  public SymmetricProperty addSymmetricProperty(URI aPropertyURI,
          Set<OClass> domainAndRange);

  /**
   * Gets the set of Symmetric Properties in the ontology.
   * 
   * @return a {@link Set} of {@link SymmetricProperty}.
   */
  public Set<SymmetricProperty> getSymmetricProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * OWL:SymmetricProperty> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isSymmetricProperty(URI thePropertyURI);

  /**
   * Creates a new transitive property (an object property that is
   * transitive).
   * 
   * @param aPropertyURI the URI for the new property.
   * @param domain the set of ontology classes (i.e. {@link OClass}
   *          objects} that constitutes the range for the new property.
   *          The property only applies to instances that belong to
   *          <b>all</b> classes included in its domain. An empty set
   *          means that the property applies to instances of any class.
   * @param range the set of ontology classes (i.e. {@link OClass}
   *          objects} that constitutes the range for the new property.
   * @return the newly created property.
   */
  public TransitiveProperty addTransitiveProperty(URI aPropertyURI,
          Set<OClass> domain, Set<OClass> range);

  /**
   * Gets the set of Transitive Properties in the ontology.
   * 
   * @return a {@link Set} of {@link TransitiveProperty}.
   */
  public Set<TransitiveProperty> getTransitiveProperties();

  /**
   * Checkes whether there exists a statement <thePropertyURI, RDF:Type,
   * OWL:TransitiveProperty> in the ontology or not.
   * 
   * @param thePropertyURI
   * @return true, only if there exists the above statement, otherwise -
   *         false.
   */
  public boolean isTransitiveProperty(URI thePropertyURI);

  /**
   * Gets the set of RDF, Object, Datatype, Symmetric and Transitive
   * property definitions in this ontology.
   * 
   * @return a {@link Set} of {@link RDFProperty},
   *         {@link DatatypeProperty}, {@link ObjectProperty},
   *         {@link TransitiveProperty} and , {@link SymmetricProperty}
   *         objects. <B>Please note that the method does not include
   *         annotation properties</B>.
   */
  public Set<RDFProperty> getPropertyDefinitions();

  /**
   * Returns the property definition for a given property. This does not
   * include Annotation properties.
   * 
   * @param thePropertyURI the URI for which the definition is sought.
   * @return a {@link Property} object.
   */
  public RDFProperty getProperty(URI thePropertyURI);

  /**
   * A method to remove the existing propertyDefinition (exclusive of
   * Annotation Property).
   * 
   * @param theProperty
   */
  public void removeProperty(RDFProperty theProperty);

  // *****************************
  // Restrictions
  // *****************************

  /**
   * Adds a new MinCardinality Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction. The default datatype is set to NonNegativeIntegerNumber
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param minCardinalityValue - generally a numeric number.
   * @return
   * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber datatype.
   */
  public MinCardinalityRestriction addMinCardinalityRestriction(
          RDFProperty onProperty, String minCardinalityValue)
          throws InvalidValueException;
  
  /**
   * Adds a new MaxCardinality Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction. The default datatype is set to NonNegativeIntegerNumber
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param minCardinalityValue - generally a numeric number.
   * @return
   * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber datatype.
   */
  public MaxCardinalityRestriction addMaxCardinalityRestriction(
          RDFProperty onProperty, String maxCardinalityValue)
          throws InvalidValueException;

  /**
   * Adds a new Cardinality Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction. The default datatype is set to NonNegativeIntegerNumber
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param minCardinalityValue - generally a numeric number.
   * @return
   * @throws InvalidValueException - if a value is not compatible with the nonNegativeIntegerNumber datatype.
   */
  public CardinalityRestriction addCardinalityRestriction(
          RDFProperty onProperty, String cardinalityValue)
          throws InvalidValueException;

  /**
   * Adds a new HasValue Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction.
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param hasValue - a resource used as a value for hasValue element of the restriction.
   * @return
   */
  public HasValueRestriction addHasValueRestriction(
          RDFProperty onProperty, OResource hasValue);

  /**
   * Adds a new AllValuesFrom Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction.
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param hasValue - a resource used as a value for hasValue element of the restriction.
   * @return
   */
  public AllValuesFromRestriction addAllValuesFromRestriction(
          RDFProperty onProperty, OResource hasValue);

  /**
   * Adds a new AllValuesFrom Restriction to the ontology. It
   * automatically creates a randon anonymous class, which it uses to
   * denote the restriction.
   * 
   * @param onProperty - Specifies the property for which the restriction is being set.
   * @param hasValue - a resource used as a value for hasValue element of the restriction.
   * @return
   */
  public SomeValuesFromRestriction addSomeValuesFromRestriction(
          RDFProperty onProperty, OResource hasValue);
  
  
  // *****************************
  // Ontology Modification Events
  // *****************************
  /**
   * Sets the modified flag.
   * 
   * @param isModified sets this param as a value of the modified
   *          property of the ontology
   */
  public void setModified(boolean isModified);

  /**
   * Checks the modified flag.
   * 
   * @return whether the ontology has been modified after the loading
   */
  public boolean isModified();

  /**
   * Register the Ontology Modification Listeners
   * 
   * @param oml
   */
  public void addOntologyModificationListener(OntologyModificationListener oml);

  /**
   * Removed the registered ontology modification listeners
   * 
   * @param oml
   */
  public void removeOntologyModificationListener(
          OntologyModificationListener oml);

  /**
   * A method to invoke when a resource's property value is changed
   * 
   * @param resource
   * @param eventType
   */
  public void fireResourcePropertyValueChanged(OResource resource, RDFProperty property, Object value, int eventType);

  /**
   * A method to invoke when a resource's property value is changed
   * 
   * @param resource
   * @param eventType
   */
  public void fireResourceRelationChanged(OResource resource1, OResource resource2,int eventType);

  /**
   * A Method to invoke an event for newly added ontology resource
   * 
   * @param resource
   */
  public void fireOntologyResourceAdded(OResource resource);

  /**
   * A Method to invoke an event for a removed ontology resource
   * 
   * @param resource
   */
  public void fireOntologyResourcesRemoved(String[] resources);

  /**
   * A method to invoke when the ontology is reset.
   * 
   */
  public void fireOntologyReset();

  // *************************
  // Sesame Repository methods
  // *************************
  /**
   * Start the transaction before additing statements.
   */
  public void startTransaction();

  /**
   * Commits all the transaction (so far included after the call to
   * start transaction) into the repository.
   */
  public void commitTransaction();

  /**
   * Checks whether the transation is already started.
   * 
   * @return
   */
  public boolean transationStarted();

  /**
   * Returns the repository created for this particular instance of the
   * ontology.
   * 
   * @return
   */
  public SesameRepository getSesameRepository();

  /**
   * Returns the ID of a Sesame Repository created for this particular
   * instance of the ontology.
   * 
   * @return
   */
  public String getSesameRepositoryID();

  /**
   * Given a URI object, this method returns its equivalent object
   * 
   * @param uri
   * @return
   */
  public OResource getOResourceFromMap(String uri);

  /**
   * Adds the resource to central map
   * 
   * @param uri
   * @param resource
   */
  public void addOResourceToMap(String uri, OResource resource);

  /**
   * Removes the resource from the central map
   * 
   * @param uri
   */
  public void removeOResourceFromMap(String uri);

  /**
   * This method checks in its cache to find out the OResource for the
   * given resource name. However, It is also possible for two resources
   * to have a same name but different name spaces. This method returns
   * the first found OResource (without gurantteeing the order) from its
   * list. If user wants to retrieve a list of resources, he/she must
   * use the getOResourcesByName(String resourceName).
   * 
   * @param resourceName
   * @return
   */
  public OResource getOResourceByName(String resourceName);

  /**
   * This method checks in its cache to find out the OResources for the
   * given resource name. It is possible for two resources to have a
   * same name but different name spaces. This method returns a list of
   * resources with the common name. Please note that deleting an
   * instance from this list (e.g. list.remove(int/Object)) does not
   * delete the resource from an ontology. One must use appropriate
   * method from the Ontology interface to delete such resources.
   * 
   * @param resourceName
   * @return
   */
  public List<OResource> getOResourcesByName(String resourceName);

  /**
   * This method returns a list of OResources from the ontology. Please
   * note that deleting an instance from this list (e.g.
   * list.remove(int/Object)) does not delete the resource from an
   * ontology. One must use appropriate method from the Ontology
   * interface to delete such resources.
   * 
   * @return
   */
  public List<OResource> getAllResources();

  /**
   * This method given a property (either an annotation or datatype),
   * retrieves a list of resources which have the provided literal set
   * as a value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public List<OResource> getOResourcesWith(RDFProperty aProperty, Literal aValue);

  /**
   * This method given a property (either object, transitive, symmetric
   * or rdf), retrieves a list of resources which have the provided
   * resource set as a value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public List<OResource> getOResourcesWith(RDFProperty aProperty,
          OResource aValue);

}
