package gate.creole.ontology.owlim;

import gate.creole.ontology.GateOntologyException;

public interface OWLIM extends java.rmi.Remote {

  /**
   * Gets the default name space for this ontology. The defaultNameSpace
   * is (by default) used for the newly created resources.
   * 
   * @return a String value.
   */
  public String getDefaultNameSpace(String repositoryID) throws GateOntologyException;

  /**
   * Adds the ontology data
   * 
   * @param repositoryID
   * @param data
   * @param baseURI
   * @param format
   * @throws GateOntologyException
   */
  public void addOntologyData(String repositoryID, String data, String baseURI,
          byte format) throws GateOntologyException;

  /**
   * This method tells whether the resource is imported or added as an explicit statement.
   * @param repositoryID
   * @param resourceID
   * @return
   */
  public boolean isImplicitResource(String repositoryID, String resourceID) throws GateOntologyException ;
  
  /**
   * Returns whether the theSuperClass is indeed a super class of the
   * theSubClassURI.
   * 
   * @param repositoryID
   * @param theSuperClassURI
   * @param theSubClassURI
   * @param direct
   * @return
   */
  public boolean isSuperClassOf(String repositoryID, String theSuperClassURI,
          String theSubClassURI, byte direct) throws GateOntologyException;

  /**
   * Returns whether the theSubClass is indeed a sub class of the
   * theSuperClassURI.
   * 
   * @param repositoryID
   * @param theSuperClassURI
   * @param theSubClassURI
   * @param direct
   * @return
   */
  public boolean isSubClassOf(String repositoryID, String theSuperClassURI,
          String theSubClassURI, byte direct) throws GateOntologyException;

  /**
   * Given a property URI, this method returns an object of Property
   * 
   * @param repositoryID
   * @param thePropertyURI
   * @return
   * @throws GateOntologyException
   */
  public Property getPropertyFromOntology(String repositoryID,
          String thePropertyURI) throws GateOntologyException;

  /**
   * Checks whether the two classes defined as same in the ontology.
   * 
   * @param theClassURI1
   * @param theClassURI2
   * @return
   * @throws Exception
   */
  public boolean isEquivalentClassAs(String repositoryID, String theClassURI1,
          String theClassURI2) throws GateOntologyException;

  // *******************************************************************
  // property methods
  // *******************************************************************
  // **************
  // Annotation Property
  // ************
  /**
   * Creates a new AnnotationProperty.
   * 
   * @param aPropertyURI URI of the property to be added into the
   *          ontology. Done
   */
  public void addAnnotationProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * Gets the annotation properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getAnnotationProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException;

  /**
   * Gets the RDF properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getRDFProperties(String repositoryID, String theResourceURI)
          throws GateOntologyException;

  /**
   * Gets the datatype properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getDatatypeProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException;

  /**
   * Gets the object properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getObjectProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException;

  /**
   * Gets the transitive properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getTransitiveProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException;

  /**
   * Gets the symmetric properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getSymmetricProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException;

  /**
   * returns if the given property is an Annotation property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isAnnotationProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * Adds a new annotation property value and specifies the language.
   * 
   * @param theAnnotationProperty the annotation property
   * @param value the value containing some value
   * @return
   */
  public void addAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws GateOntologyException;

  /**
   * Gets the list of annotation property values
   * 
   * @param repositoryID
   * @param theResourceURI
   * @param theAnnotationPropertyURI
   * @return
   */
  public PropertyValue[] getAnnotationPropertyValues(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI)
          throws GateOntologyException;

  /**
   * Gets the annotation property for the given resource uri.
   * 
   * @param repositoryID
   * @param theResourceURI
   * @param theAnnotationPropertyURI
   * @param language
   * @return
   */
  public String getAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI,
          String language) throws GateOntologyException;

  /**
   * For the current resource, the method removes the given literal for
   * the given property.
   * 
   * @param theAnnotationProperty
   * @param literal
   */
  public void removeAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws GateOntologyException;

  /**
   * Removes all values for a named property.
   * 
   * @param theProperty the property
   */
  public void removeAnnotationPropertyValues(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI)
          throws GateOntologyException;

  // **************
  // RDFProperties
  // *************
  /**
   * The method adds a generic property specifiying domain and range for
   * the same. All classes specified in domain and range must exist.
   * 
   * @param aPropertyURI
   * @param domainClassesURIs
   * @param rangeClassesTypes Done
   */
  public void addRDFProperty(String repositoryID, String aPropertyURI,
          String[] domainClassesURIs, String[] rangeClassesTypes)
          throws GateOntologyException;

  /**
   * returns if the given property is an RDF property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isRDFProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  // **************
  // Datatype Properties
  // *************
  /**
   * The method adds a data type property specifiying domain and range
   * for the same. All classes specified in domain and range must exist.
   * 
   * @param aPropertyURI
   * @param domainClassesURIs
   * @param dataTypeURI Done
   */
  public void addDataTypeProperty(String repositoryID, String aPropertyURI,
          String[] domainClassesURIs, String dataTypeURI)
          throws GateOntologyException;

  /**
   * Returns the datatype uri specified for the given datatype property.
   * 
   * @param repositoryID
   * @param theDatatypePropertyURI
   * @return
   * @throws GateOntologyException
   */
  public String getDatatype(String repositoryID, String theDatatypePropertyURI)
          throws GateOntologyException;

  // **************
  // Symmetric Properties
  // *************
  /**
   * The method adds a symmetric property specifiying domain and range
   * for the same. All classes specified in domain and range must exist.
   * 
   * @param aPropertyURI
   * @param domainAndRangeClassesURIs Done
   */
  public void addSymmetricProperty(String repositoryID, String aPropertyURI,
          String[] domainAndRangeClassesURIs) throws GateOntologyException;

  /**
   * Checkes whether the two properties are Equivalent.
   * 
   * @param repositoryID
   * @param aPropertyURI
   * @return
   * @throws GateOntologyException
   */
  public boolean isEquivalentPropertyAs(String repositoryID,
          String aPropertyURI1, String aPropertyURI2) throws GateOntologyException;

  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, byte direct) throws GateOntologyException;

  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          byte direct) throws GateOntologyException;

  /**
   * Checkes whether the two properties have a super-sub relation.
   * 
   * @param repositoryID
   * @param aSuperPropertyURI
   * @param aSubPropertyURI
   * @param direct
   * @return
   * @throws GateOntologyException
   */
  public boolean isSuperPropertyOf(String repositoryID,
          String aSuperPropertyURI, String aSubPropertyURI, byte direct)
          throws GateOntologyException;

  /**
   * Checkes whether the two properties have a super-sub relation.
   * 
   * @param repositoryID
   * @param aSuperPropertyURI
   * @param aSubPropertyURI
   * @param direct
   * @return
   * @throws GateOntologyException
   */
  public boolean isSubPropertyOf(String repositoryID, String aSuperPropertyURI,
          String aSubPropertyURI, byte direct) throws GateOntologyException;

  /**
   * Given a class and instance URIs, the method checks if the latter is
   * a member of former. If the boolean parameter direct is set to true,
   * the method also checks if the literal is a direct instance of the
   * class.
   * 
   * @param aSuperClassURI
   * @param individualURI
   * @return Done
   */
  public boolean hasIndividual(String repositoryID, String aSuperClassURI,
          String individualURI, byte direct) throws GateOntologyException;

  /**
   * Returns whether the individual1 is different from the individual2.
   * 
   * @param theInstanceURI1
   * @param theInstanceURI2
   * @return
   * @throws GateOntologyException
   */
  public boolean isDifferentIndividualFrom(String repositoryID,
          String theInstanceURI1, String theInstanceURI2)
          throws GateOntologyException;

  /**
   * Checkes whether the two individuals are same.
   * 
   * @param repositoryID
   * @param individualURI1
   * @param invidualURI2
   * @return
   * @throws GateOntologyException
   */
  public boolean isSameIndividualAs(String repositoryID,
          String theInstanceURI1, String theInstanceURI2)
          throws GateOntologyException;

  // *************
  // Instances and properties
  // **************
  /**
   * adds the RDF Property value on the specified instance
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @param aResourceURI
   * @throws InvalidValueException
   */
  public void addRDFPropertyValue(String repositoryID, String anInstanceURI,
          String anRDFPropertyURI, String aResourceURI) throws GateOntologyException;

  /**
   * Removes the specified RDF Property Value
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @param aResourceURI
   */
  public void removeRDFPropertyValue(String repositoryID, String anInstanceURI,
          String anRDFPropertyURI, String aResourceURI) throws GateOntologyException;

  /**
   * gets the rdf property values for the specified instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @return resource URIs
   */
  public ResourceInfo[] getRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI) throws GateOntologyException;

  /**
   * Removes all the RDF Property values from the given instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   */
  public void removeRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI) throws GateOntologyException;

  // ******************
  // DataType Properties
  // *****************
  /**
   * Adds the value for the given Property.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param aDatatypePropertyURI
   * @param datatypeURI
   * @param value
   * @throws InvalidValueException
   */
  public void addDatatypePropertyValue(String repositoryID,
          String anInstanceURI, String aDatatypePropertyURI,
          String datatypeURI, String value) throws GateOntologyException;

  /**
   * Removes the provided value for the given instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param aDatatypePropertyURI
   * @param datatypeURI
   * @param value
   */
  public void removeDatatypePropertyValue(String repositoryID,
          String anInstanceURI, String aDatatypePropertyURI,
          String datatypeURI, String value) throws GateOntologyException;

  /**
   * Gets a list of values for the given Property.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param aDatatypePropertyURI
   * @return
   */
  public PropertyValue[] getDatatypePropertyValues(String repositoryID,
          String anInstanceURI, String aDatatypePropertyURI)
          throws GateOntologyException;

  /**
   * Removes all property values set on the provided instance for the
   * current property.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param aDatatypePropertyURI
   */
  public void removeDatatypePropertyValues(String repositoryID,
          String anInstanceURI, String aDatatypePropertyURI)
          throws GateOntologyException;

  // ******************
  // Object, Symmetric and Transitive Properties
  // *****************
  /**
   * Adds the value for the given property (Object, Symmetric and
   * Transitive).
   * 
   * @param repositoryID
   * @param sourceInstanceURI
   * @param anObjectPropertyURI
   * @param theValueInstanceURI
   * @throws InvalidValueException
   */
  public void addObjectPropertyValue(String repositoryID,
          String sourceInstanceURI, String anObjectPropertyURI,
          String theValueInstanceURI) throws GateOntologyException;

  /**
   * Remove the provided value for the given property (Object, Symmetric
   * and Transitive).
   * 
   * @param repositoryID
   * @param sourceInstanceURI
   * @param anObjectPropertyURI
   * @param theValueInstanceURI
   * @return
   */
  public void removeObjectPropertyValue(String repositoryID,
          String sourceInstanceURI, String anObjectPropertyURI,
          String theValueInstanceURI) throws GateOntologyException;

  /**
   * Gets a list of values for the given Property (Object, Symmetric and
   * Transitive).
   * 
   * @param repositoryID
   * @param sourceInstanceURI
   * @param anObjectPropertyURI
   * @return
   */
  public String[] getObjectPropertyValues(String repositoryID,
          String sourceInstanceURI, String anObjectPropertyURI)
          throws GateOntologyException;

  /**
   * Removes all property values set for the current property (Object,
   * Symmetric and Transitive).
   * 
   * @param repositoryID
   * @param sourceInstanceURI
   * @param anObjectPropertyURI
   */
  public void removeObjectPropertyValues(String repositoryID,
          String sourceInstanceURI, String anObjectPropertyURI)
          throws GateOntologyException;

  // ****************************************************************************
  // user management methods
  // ****************************************************************************
  /**
   * Call to this method is necessary in order to login in to the Sesame
   * server. Unless user is registered with Sesame server, he/she cannot
   * have write or modify access to any of the repositories (unless
   * given write access to world users) available on the server.
   * However, unregistered users are and will be allowed to have read
   * access on all repositories.
   * 
   * @param username
   * @param password
   * @return
   */
  public boolean login(String username, String password) throws GateOntologyException;

  /**
   * End the session by logging out
   */
  public void logout(String repositoryID) throws GateOntologyException;

  // ****************************************************************************
  // repository methods
  // ****************************************************************************
  /**
   * Find out the list of repository list
   */
  public String[] getRepositoryList() throws GateOntologyException;

  /**
   * sets the provided repository as a current repository
   */
  public void setCurrentRepositoryID(String repositoryID)
          throws GateOntologyException;

  /**
   * This method returns the ID of current repository
   */
  public String getCurrentRepositoryID() throws GateOntologyException;

  /**
   * Users are allowed to create new repositories and add data into it.
   * In order to create new repository, they don’t necessarily need to
   * be registered. The username and password parameters are used to
   * assign access rights over the repository. Apart from the owner of
   * repository, administrator also gets the full rights over the
   * repository. All other users are given read access. User is also
   * asked to provide a URL, or the RDF data from the ontology. Incase
   * if the url is null or an empty string, an empty graph is created
   * allowing users to add more data into it. Otherwise the graph is
   * populated with the given ontology. The user is also asked to
   * provide the RDF format information (i.e. ''N3'', ''TURTLE'',
   * ''NTRIPLES'' or ''RDFXML'') .
   * 
   * @param repositoryID
   * @param username
   * @param password
   * @param ontoData
   * @param baseURI
   * @param format
   * @param persist
   * @return
   */
  public String createRepository(String repositoryID, String username,
          String password, String ontoData, String baseURI, byte format,
          String absolutePersistLocation, boolean persist,
          boolean returnSystemStatements) throws GateOntologyException;

  /**
   * Users are allowed to create new repositories and add data into it.
   * In order to create new repository, they don’t necessarily need to
   * be registered. The username and password parameters are used to
   * assign access rights over the repository. Apart from the owner of
   * repository, administrator also gets the full rights over the
   * repository. All other users are given read access. User is also
   * asked to provide a URL for the ontology. Incase if the url is null
   * or an empty string, an empty graph is created allowing user to add
   * more data into it. Otherwise the graph is populated with the given
   * ontology URL. The user is also asked to provide the RDF format
   * information (i.e. ''N3'', ''TURTLE'', ''NTRIPLES'' or ''RDFXML'') .
   * 
   * @param repositoryID
   * @param username
   * @param password
   * @param ontoFileUrl
   * @param baseURI
   * @param format
   * @param persist
   * @return
   */
  public String createRepositoryFromUrl(String repositoryID, String username,
          String password, String ontoFileUrl, String baseURI, byte format,
          String absolutePersistLocation, boolean persist,
          boolean returnSystemStatements) throws GateOntologyException;

  /**
   * Removes the repository with given ID
   * 
   * @param repositoryID
   * @return
   */
  public void removeRepository(String repositoryID, boolean persist)
          throws GateOntologyException;

  // *******************************************************************
  // *************************** Ontology Methods **********************
  // *******************************************************************
  /**
   * The method removes all data from the available graph.
   */
  public void cleanOntology(String repositoryID) throws GateOntologyException;

  /**
   * This method is useful to export results. Given one of the four
   * RDFFormat parameters (i.e. ''N3'', ''TURTLE'', ''NTRIPLES'' or
   * ''RDFXML'') , the method returns an equivalent string
   * representation of the data in the supplied format.
   * 
   * @param format
   * @return
   */
  public String getOntologyData(String repositoryID, byte format)
          throws GateOntologyException;

  /**
   * The method allows adding version information to the repository.
   * 
   * @param versionInfo
   */
  public void setVersion(String repositoryID, String versionInfo)
          throws GateOntologyException;

  /**
   * The method returns the version information of the repository.
   * 
   * @return
   */
  public String getVersion(String repositoryID) throws GateOntologyException;

  // *******************************************************************
  // class methods
  // *******************************************************************
  /**
   * The method allows adding a class to repository.
   * 
   * @param classURI
   * @param classType - one of the following constant values from the
   *          OConstants class. OWL_CLASS, CARDINALITY_RESTRICTION,
   *          MIN_CARDINALITY_RESTRICTION, MAX_CARDINALITY_RESTRICTION,
   *          HAS_VALUE_RESTRICTION, ALL_VALUES_FROM_RESTRICTION.
   */
  public void addClass(String repositoryID, String classURI, byte classType)
          throws GateOntologyException;

  /**
   * Given a class to delete, it removes it from the repository.
   * 
   * @param classURI
   * @return a list of other resources, which got removed as a result of
   *         this deletion
   */
  public String[] removeClass(String repositoryID, String classURI)
          throws GateOntologyException;

  /**
   * The method returns if the current repository has a class with URI
   * that matches with the class parameter.
   * 
   * @return
   */
  public boolean hasClass(String repositoryID, String classURI)
          throws GateOntologyException;

  /**
   * if top set to true, the method returns only the top classes (i.e.
   * classes with no super class). Otherwise it returns all classes
   * available in repository.
   * 
   * @param top
   * @return
   */
  public ResourceInfo[] getClasses(String repositoryID, boolean top)
          throws GateOntologyException;

  /**
   * Returns if the given class is a top class. It also returns false if
   * the class is an instance of BNode
   * 
   * @param classURI
   * @return
   */
  public boolean isTopClass(String repositoryID, String classURI)
          throws GateOntologyException;

  // ****************************************************************************
  // relations among classes
  // ****************************************************************************
  /**
   * The method creates a new class with the URI as specified in
   * className and adds it as a subClassOf the parentClass. It also adds
   * the provided comment on the subClass.
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void addSubClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException;

  /**
   * The method creates a new class with the URI as specified in
   * className and adds it as a superClassOf the parentClass. It also
   * adds the provided comment on the subClass.
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void addSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException;

  /**
   * Removes the subclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSubClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException;

  /**
   * Removes the superclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException;

  /**
   * This method returns all sub classes of the given class
   * 
   * @param superClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSubClasses(String repositoryID,
          String superClassURI, byte direct) throws GateOntologyException;

  /**
   * This method returns all super classes of the given class
   * 
   * @param subClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSuperClasses(String repositoryID,
          String subClassURI, byte direct) throws GateOntologyException;

  /**
   * Sets the classes as disjoint
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setDisjointClassWith(String repositoryID, String class1URI,
          String class2URI) throws GateOntologyException;

  /**
   * Sets the classes as same classes
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setEquivalentClassAs(String repositoryID, String class1URI,
          String class2URI) throws GateOntologyException;

  /**
   * returns an array of classes which are marked as disjoint for the
   * given class
   * 
   * @param classURI
   * @return
   */
  public String[] getDisjointClasses(String repositoryID, String classURI)
          throws GateOntologyException;

  /**
   * returns an array of classes which are equivalent as the given class
   * 
   * @param aClassURI
   * @return
   */
  public ResourceInfo[] getEquivalentClasses(String repositoryID,
          String aClassURI) throws GateOntologyException;

  /**
   * Removes the given property
   * 
   * @param aPropertyURI
   */
  public String[] removePropertyFromOntology(String repositoryID,
          String aPropertyURI) throws GateOntologyException;

  /**
   * The method adds an object property specifiying domain and range for
   * the same. All classes specified in domain and range must exist.
   * 
   * @param aPropertyURI
   * @param domainClassesURIs
   * @param rangeClassesTypes
   */
  public void addObjectProperty(String repositoryID, String aPropertyURI,
          String[] domainClassesURIs, String[] rangeClassesTypes)
          throws GateOntologyException;

  /**
   * The method adds a transitive property specifiying domain and range
   * for the same. All classes specified in domain and range must exist.
   * 
   * @param aPropertyURI
   * @param domainClassesURIs
   * @param rangeClassesTypes
   */
  public void addTransitiveProperty(String repositoryID, String aPropertyURI,
          String[] domainClassesURIs, String[] rangeClassesTypes)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getRDFProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getObjectProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getSymmetricProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getTransitiveProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getDatatypeProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getAnnotationProperties(String repositoryID)
          throws GateOntologyException;

  /**
   * Given a property, this method returns its domain
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getDomain(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * Given a property, this method returns its range
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getRange(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * Returns if the provided property is functional
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isFunctional(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * sets the current property as functional
   * 
   * @param aPropertyURI
   * @param isFunctional
   */
  public void setFunctional(String repositoryID, String aPropertyURI,
          boolean isFunctional) throws GateOntologyException;

  /**
   * returns if the given property is inverse functional property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isInverseFunctional(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * Sets the current property as inverse functional property
   * 
   * @param aPropertyURI
   * @param isInverseFunctional
   */
  public void setInverseFunctional(String repositoryID, String aPropertyURI,
          boolean isInverseFunctional) throws GateOntologyException;

  /**
   * returns if the given property is a symmetric property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isSymmetricProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * returns if the given property is a transitive property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isTransitiveProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * returns if the given property is a datatype property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isDatatypeProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  /**
   * returns if the given property is an object property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isObjectProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException;

  // *************************************
  // Relations among properties
  // *************************************
  /**
   * Sets two properties as same
   * 
   * @param property1URI
   * @param property2URI
   */
  public void setEquivalentPropertyAs(String repositoryID, String property1URI,
          String property2URI) throws GateOntologyException;

  /**
   * For the given property, this method returns all properties marked
   * as Equivalent as it
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getEquivalentPropertyAs(String repositoryID,
          String aPropertyURI) throws GateOntologyException;

  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException;

  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException;

  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException;

  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException;

  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, boolean direct) throws GateOntologyException;

  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          boolean direct) throws GateOntologyException;

  /**
   * for the given property, the method returns all its inverse
   * properties
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getInverseProperties(String repositoryID,
          String aPropertyURI) throws GateOntologyException;

  /**
   * property1 is set as inverse of property 2
   * 
   * @param property1URI
   * @param property2URI
   */
  public void setInverseOf(String repositoryID, String propertyURI1,
          String propertyURI2) throws GateOntologyException;

  // *******************************************************************
  // *************************** Instance Methods **********************
  // *******************************************************************
  /**
   * The method adds a new instance (literal) into the repository. It
   * then creates a statement indicating membership relation with the
   * provided class.
   * 
   * @param superClassURI
   * @param individualURI
   */
  public void addIndividual(String repositoryID, String superClassURI,
          String individualURI) throws GateOntologyException;

  /**
   * The method removes the provided instance from the repository.
   * 
   * @param individual
   * @return
   */
  public String[] removeIndividual(String repositoryID, String individualURI)
          throws GateOntologyException;

  /**
   * The method returns all member instances of the provided class. It
   * returns only the direct instances if the boolean parameter direct
   * is set to true.
   * 
   * @param superClassURI
   * @param direct
   */
  public String[] getIndividuals(String repositoryID, String superClassURI,
          byte direct) throws GateOntologyException;

  /**
   * returns all resources registered as individuals in the ontology
   * 
   * @return
   */
  public String[] getIndividuals(String repositoryID) throws GateOntologyException;

  /**
   * Given a class and instance URIs, the method checks if the latter is
   * a member of former. If the boolean parameter direct is set to true,
   * the method also checks if the literal is a direct instance of the
   * class.
   * 
   * @param aSuperClassURI
   * @param individualURI
   * @return
   */
  public boolean hasIndividual(String repositoryID, String aSuperClassURI,
          String individualURI, boolean direct) throws GateOntologyException;

  /**
   * For the given individual, the method returns a set of classes for
   * which the individual is registered as instance of
   * 
   * @param individualURI
   */
  public ResourceInfo[] getClassesOfIndividual(String repositoryID,
          String individualURI, byte direct) throws GateOntologyException;

  // *******************************************************************
  // relations among individuals
  // *******************************************************************
  /**
   * individual1 is sets as different individual from individual2
   * 
   * @param individual1URI
   * @param individual2URI
   */
  public void setDifferentIndividualFrom(String repositoryID,
          String individual1URI, String individual2URI) throws GateOntologyException;

  /**
   * for the given individual, the method returns all individuals
   * registered as different from the given individual
   * 
   * @param individualURI
   * @return
   */
  public String[] getDifferentIndividualFrom(String repositoryID,
          String individualURI) throws GateOntologyException;

  /**
   * individual1 is set as same as the individual2
   * 
   * @param individual1URI
   * @param individual2URI
   */
  public void setSameIndividualAs(String repositoryID, String individual1URI,
          String individual2URI) throws GateOntologyException;

  /**
   * for the given individual, the method returns all individuals which
   * are registered as same as the provided individual
   * 
   * @param inidividualURI
   * @return
   */
  public String[] getSameIndividualAs(String repositoryID, String individualURI)
          throws GateOntologyException;

  // ***********************************************
  // ********* Restrictions ***********************
  // ***********************************************

  /**
   * This method given a restriction uri returns the value for the
   * onProperty element.
   * 
   * @param repositoryId
   * @param restrictionURI
   * @return
   * @throws GateOntologyException
   */
  public Property getOnPropertyValue(String repositoryId, String restrictionURI)
          throws GateOntologyException;

  /**
   * This method sets the value for onProperty element on the given
   * restriction.
   * 
   * @param repositoryId
   * @param restrictionURI
   * @param propertyURI
   * @throws GateOntologyException
   */
  public void setOnPropertyValue(String repositoryId, String restrictionURI,
          String propertyURI) throws GateOntologyException;

  /**
   * Gets the property value specified on the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @param restrictionType
   * @return
   * @throws GateOntologyException
   */
  public PropertyValue getPropertyValue(String repositoryID,
          String restrictionURI, byte restrictionType) throws GateOntologyException;

  /**
   * Sets the datatype uri for the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @param restrictionType
   * @param value
   * @param datatypeURI
   * @throws GateOntologyException
   */
  public void setPropertyValue(String repositoryID, String restrictionURI,
          byte restrictionType, String value, String datatypeURI)
          throws GateOntologyException;

  /**
   * Gets the cardinality value specified on the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @param restrictionType - either of the following constants from the
   *          OConstants - ALL_VALUES_FROM_RESTRICTION,
   *          SOME_VALUES_FROM_RESTRICTION, and HAS_VALUE_RESTRICTION
   * @return
   * @throws GateOntologyException
   */
  public ResourceInfo getRestrictionValue(String repositoryID, String restrictionURI,
          byte restrictionType) throws GateOntologyException;

  /**
   * Sets the cardinality value for the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @param restrictionType - either of the following constants from the
   *          OConstants - ALL_VALUES_FROM_RESTRICTION,
   *          SOME_VALUES_FROM_RESTRICTION, and HAS_VALUE_RESTRICTION
   * @param value
   * @return
   * @throws GateOntologyException
   */
  public void setRestrictionValue(String repositoryID, String restrictionURI,
          byte restrictionType, String value) throws GateOntologyException;

  /**
   * This method tells what type of restriction the given uri refers to.
   * If the given URI is not a restriction, the method returns -1.
   * Otherwise one of the following values from the OConstants class.
   * OWL_CLASS, CARDINALITY_RESTRICTION, MIN_CARDINALITY_RESTRICTION,
   * MAX_CARDINALITY_RESTRICTION, HAS_VALUE_RESTRICTION,
   * ALL_VALUES_FROM_RESTRICTION.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @return
   * @throws GateOntologyException
   */
  public byte getClassType(String repositoryID, String restrictionURI)
          throws GateOntologyException;

  
  public Property[] getPropertiesWithResourceAsDomain(String repositoryID,
          String theResourceURI) throws GateOntologyException;
  
  
  public Property[] getPropertiesWithResourceAsRange(String repositoryID,
          String theResourceURI) throws GateOntologyException;
  
  // ****************************************************
  // ******************** Generic statements ************
  // ****************************************************
  /**
   * The method is useful for adding statements into the graph. All
   * three values must exist in repository. These values are cast in
   * Resources and then added into the graph of repository.
   * 
   * @param subjectURI
   * @param predicateURI
   * @param objectURI
   */
  public void addStatement(String repositoryID, String subjectURI,
          String predicateURI, String objectURI) throws GateOntologyException;

  /**
   * The method is useful for removing statements from the graph of
   * repository. All three values must exist in repository. these values
   * are cast in Resources and then removed from teh graph of
   * repository.
   * 
   * @param subjectURI
   * @param predicateURI
   * @param objectURI
   */
  public void removeStatement(String repositoryID, String subjectURI,
          String predicateURI, String objectURI) throws GateOntologyException;

}
