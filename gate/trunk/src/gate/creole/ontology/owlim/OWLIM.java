package gate.creole.ontology.owlim;

import java.rmi.RemoteException;

public interface OWLIM extends java.rmi.Remote {

  /**
   * Gets the default name space for this ontology. The defaultNameSpace
   * is (by default) used for the newly created resources.
   * 
   * @return a String value.
   */
  public String getDefaultNameSpace(String repositoryID) throws RemoteException;
  
  /**
   * Adds the ontology data
   * 
   * @param repositoryID
   * @param data
   * @param baseURI
   * @param format
   * @throws RemoteException
   */
  public void addOntologyData(String repositoryID, String data, String baseURI,
          byte format) throws RemoteException;


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
          String theSubClassURI, byte direct) throws RemoteException;
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
          String theSubClassURI, byte direct) throws RemoteException;
  /**
   * Given a property URI, this method returns an object of Property
   * 
   * @param repositoryID
   * @param thePropertyURI
   * @return
   * @throws RemoteException
   */
  public Property getPropertyFromOntology(String repositoryID,
          String thePropertyURI) throws RemoteException;

  /**
   * Checks whether the two classes defined as same in the ontology.
   * 
   * @param theClassURI1
   * @param theClassURI2
   * @return
   * @throws Exception
   */
  public boolean isEquivalentClassAs(String repositoryID, String theClassURI1,
          String theClassURI2) throws RemoteException;
  
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
          throws RemoteException;
  
  /**
   * Gets the annotation properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getAnnotationProperties(String repositoryID,
          String theResourceURI) throws RemoteException;
  
  /**
   * Gets the RDF properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getRDFProperties(String repositoryID, String theResourceURI)
          throws RemoteException;
  
  /**
   * Gets the datatype properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getDatatypeProperties(String repositoryID,
          String theResourceURI) throws RemoteException;
  
  /**
   * Gets the object properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getObjectProperties(String repositoryID,
          String theResourceURI) throws RemoteException;
  /**
   * Gets the transitive properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getTransitiveProperties(String repositoryID,
          String theResourceURI) throws RemoteException;
  /**
   * Gets the symmetric properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws RemoteException
   */
  public Property[] getSymmetricProperties(String repositoryID,
          String theResourceURI) throws RemoteException; 
  /**
   * returns if the given property is an Annotation property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isAnnotationProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;

  /**
   * Adds a new annotation property value and specifies the language.
   * 
   * @param theAnnotationProperty the annotation property
   * @param value the value containing some value
   * @return
   */
  public void addAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws RemoteException;

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
          throws RemoteException;

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
          String language) throws RemoteException;
  
  /**
   * For the current resource, the method removes the given literal for
   * the given property.
   * 
   * @param theAnnotationProperty
   * @param literal
   */
  public void removeAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws RemoteException;
  
  /**
   * Removes all values for a named property.
   * 
   * @param theProperty the property
   */
  public void removeAnnotationPropertyValues(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI)
          throws RemoteException;
  

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
          throws RemoteException;
  /**
   * returns if the given property is an RDF property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isRDFProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;

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
          throws RemoteException;
  /**
   * Returns the datatype uri specified for the given datatype property.
   * 
   * @param repositoryID
   * @param theDatatypePropertyURI
   * @return
   * @throws RemoteException
   */
  public String getDatatype(String repositoryID, String theDatatypePropertyURI)
          throws RemoteException;
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
          String[] domainAndRangeClassesURIs) throws RemoteException;
  /**
   * Checkes whether the two properties are Equivalent.
   * 
   * @param repositoryID
   * @param aPropertyURI
   * @return
   * @throws RemoteException
   */
  public boolean isEquivalentPropertyAs(String repositoryID,
          String aPropertyURI1, String aPropertyURI2) throws RemoteException;

  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, byte direct) throws RemoteException;
  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          byte direct) throws RemoteException;
  /**
   * Checkes whether the two properties have a super-sub relation.
   * 
   * @param repositoryID
   * @param aSuperPropertyURI
   * @param aSubPropertyURI
   * @param direct
   * @return
   * @throws RemoteException
   */
  public boolean isSuperPropertyOf(String repositoryID,
          String aSuperPropertyURI, String aSubPropertyURI, byte direct)
          throws RemoteException;
  
  /**
   * Checkes whether the two properties have a super-sub relation.
   * 
   * @param repositoryID
   * @param aSuperPropertyURI
   * @param aSubPropertyURI
   * @param direct
   * @return
   * @throws RemoteException
   */
  public boolean isSubPropertyOf(String repositoryID, String aSuperPropertyURI,
          String aSubPropertyURI, byte direct) throws RemoteException;

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
          String individualURI, byte direct) throws RemoteException;
  /**
   * Returns whether the individual1 is different from the individual2.
   * 
   * @param theInstanceURI1
   * @param theInstanceURI2
   * @return
   * @throws RemoteException
   */
  public boolean isDifferentIndividualFrom(String repositoryID,
          String theInstanceURI1, String theInstanceURI2)
          throws RemoteException;
  /**
   * Checkes whether the two individuals are same.
   * 
   * @param repositoryID
   * @param individualURI1
   * @param invidualURI2
   * @return
   * @throws RemoteException
   */
  public boolean isSameIndividualAs(String repositoryID,
          String theInstanceURI1, String theInstanceURI2)
          throws RemoteException;
  
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
          String anRDFPropertyURI, String aResourceURI) throws RemoteException;
  /**
   * Removes the specified RDF Property Value
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @param aResourceURI
   */
  public void removeRDFPropertyValue(String repositoryID, String anInstanceURI,
          String anRDFPropertyURI, String aResourceURI) throws RemoteException;
  /**
   * gets the rdf property values for the specified instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @return resource URIs
   */
  public ResourceInfo[] getRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI) throws RemoteException;
  /**
   * Removes all the RDF Property values from the given instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   */
  public void removeRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI) throws RemoteException;

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
          String datatypeURI, String value) throws RemoteException;

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
          String datatypeURI, String value) throws RemoteException;
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
          throws RemoteException;
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
          throws RemoteException;

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
          String theValueInstanceURI) throws RemoteException;
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
          String theValueInstanceURI) throws RemoteException;

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
          throws RemoteException;
  
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
          throws RemoteException;
  

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
  public boolean login(String username, String password) throws RemoteException;

  /**
   * End the session by logging out
   */
  public void logout(String repositoryID) throws RemoteException;

  // ****************************************************************************
  // repository methods
  // ****************************************************************************
  /**
   * Find out the list of repository list
   */
  public String[] getRepositoryList() throws RemoteException;

  /**
   * sets the provided repository as a current repository
   */
  public void setCurrentRepositoryID(String repositoryID)
          throws RemoteException;
  /**
   * This method returns the ID of current repository
   */
  public String getCurrentRepositoryID() throws RemoteException;

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
          boolean returnSystemStatements) throws RemoteException;
  
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
          boolean returnSystemStatements) throws RemoteException;
  /**
   * Removes the repository with given ID
   * 
   * @param repositoryID
   * @return
   */
  public void removeRepository(String repositoryID, boolean persist)
          throws RemoteException;

  // *******************************************************************
  // *************************** Ontology Methods **********************
  // *******************************************************************
  /**
   * The method removes all data from the available graph.
   */
  public void cleanOntology(String repositoryID) throws RemoteException;

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
          throws RemoteException;
  /**
   * The method allows adding version information to the repository.
   * 
   * @param versionInfo
   */
  public void setVersion(String repositoryID, String versionInfo)
          throws RemoteException;
  /**
   * The method returns the version information of the repository.
   * 
   * @return
   */
  public String getVersion(String repositoryID) throws RemoteException;

  // *******************************************************************
  // class methods
  // *******************************************************************
  /**
   * The method allows adding a class to repository.
   * 
   * @param classURI
   */
  public void addClass(String repositoryID, String classURI)
          throws RemoteException;

  /**
   * Given a class to delete, it removes it from the repository.
   * 
   * @param classURI
   * @return a list of other resources, which got removed as a result of
   *         this deletion
   */
  public String[] removeClass(String repositoryID, String classURI)
          throws RemoteException;
  /**
   * The method returns if the current repository has a class with URI
   * that matches with the class parameter.
   * 
   * @return
   */
  public boolean hasClass(String repositoryID, String classURI)
          throws RemoteException;

  /**
   * if top set to true, the method returns only the top classes (i.e.
   * classes with no super class). Otherwise it returns all classes
   * available in repository.
   * 
   * @param top
   * @return
   */
  public ResourceInfo[] getClasses(String repositoryID, boolean top)
          throws RemoteException;
  /**
   * Returns if the given class is a top class. It also returns false if
   * the class is an instance of BNode
   * 
   * @param classURI
   * @return
   */
  public boolean isTopClass(String repositoryID, String classURI)
          throws RemoteException;
  
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
          String subClassURI) throws RemoteException;
  

  /**
   * The method creates a new class with the URI as specified in
   * className and adds it as a superClassOf the parentClass. It also
   * adds the provided comment on the subClass.
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void addSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws RemoteException;
  
  /**
   * Removes the subclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSubClass(String repositoryID, String superClassURI,
          String subClassURI) throws RemoteException;
  /**
   * Removes the superclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws RemoteException;

  /**
   * This method returns all sub classes of the given class
   * 
   * @param superClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSubClasses(String repositoryID,
          String superClassURI, byte direct) throws RemoteException;

  /**
   * This method returns all super classes of the given class
   * 
   * @param subClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSuperClasses(String repositoryID,
          String subClassURI, byte direct) throws RemoteException;
  /**
   * Sets the classes as disjoint
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setDisjointClassWith(String repositoryID, String class1URI,
          String class2URI) throws RemoteException;

  /**
   * Sets the classes as same classes
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setEquivalentClassAs(String repositoryID, String class1URI,
          String class2URI) throws RemoteException;

  /**
   * returns an array of classes which are marked as disjoint for the
   * given class
   * 
   * @param classURI
   * @return
   */
  public String[] getDisjointClasses(String repositoryID, String classURI)
          throws RemoteException;
  /**
   * returns an array of classes which are equivalent as the given class
   * 
   * @param aClassURI
   * @return
   */
  public ResourceInfo[] getEquivalentClasses(String repositoryID,
          String aClassURI) throws RemoteException;
  /**
   * Removes the given property
   * 
   * @param aPropertyURI
   */
  public String[] removePropertyFromOntology(String repositoryID,
          String aPropertyURI) throws RemoteException;
  
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
          throws RemoteException;

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
          throws RemoteException;
  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getRDFProperties(String repositoryID)
          throws RemoteException;
  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getObjectProperties(String repositoryID)
          throws RemoteException;
  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getSymmetricProperties(String repositoryID)
          throws RemoteException;

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getTransitiveProperties(String repositoryID)
          throws RemoteException;
  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getDatatypeProperties(String repositoryID)
          throws RemoteException;
  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getAnnotationProperties(String repositoryID)
          throws RemoteException;
  /**
   * Given a property, this method returns its domain
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getDomain(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * Given a property, this method returns its range
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getRange(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * Returns if the provided property is functional
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isFunctional(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * sets the current property as functional
   * 
   * @param aPropertyURI
   * @param isFunctional
   */
  public void setFunctional(String repositoryID, String aPropertyURI,
          boolean isFunctional) throws RemoteException;
  /**
   * returns if the given property is inverse functional property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isInverseFunctional(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * Sets the current property as inverse functional property
   * 
   * @param aPropertyURI
   * @param isInverseFunctional
   */
  public void setInverseFunctional(String repositoryID, String aPropertyURI,
          boolean isInverseFunctional) throws RemoteException;
  /**
   * returns if the given property is a symmetric property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isSymmetricProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * returns if the given property is a transitive property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isTransitiveProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * returns if the given property is a datatype property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isDatatypeProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;
  /**
   * returns if the given property is an object property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isObjectProperty(String repositoryID, String aPropertyURI)
          throws RemoteException;
  
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
          String property2URI) throws RemoteException;
  /**
   * For the given property, this method returns all properties marked
   * as Equivalent as it
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getEquivalentPropertyAs(String repositoryID,
          String aPropertyURI) throws RemoteException;
  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws RemoteException;

  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws RemoteException;
  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws RemoteException;
  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws RemoteException;
  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, boolean direct) throws RemoteException;
  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          boolean direct) throws RemoteException;
  /**
   * for the given property, the method returns all its inverse
   * properties
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getInverseProperties(String repositoryID,
          String aPropertyURI) throws RemoteException;
  /**
   * property1 is set as inverse of property 2
   * 
   * @param property1URI
   * @param property2URI
   */
  public void setInverseOf(String repositoryID, String propertyURI1,
          String propertyURI2) throws RemoteException;
  
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
          String individualURI) throws RemoteException;
  
  /**
   * The method removes the provided instance from the repository.
   * 
   * @param individual
   * @return
   */
  public String[] removeIndividual(String repositoryID, String individualURI)
          throws RemoteException;
  
  /**
   * The method returns all member instances of the provided class. It
   * returns only the direct instances if the boolean parameter direct
   * is set to true.
   * 
   * @param superClassURI
   * @param direct
   */
  public String[] getIndividuals(String repositoryID, String superClassURI,
          byte direct) throws RemoteException;
  

  /**
   * returns all resources registered as individuals in the ontology
   * 
   * @return
   */
  public String[] getIndividuals(String repositoryID) throws RemoteException;

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
          String individualURI, boolean direct) throws RemoteException;

  /**
   * For the given individual, the method returns a set of classes for
   * which the individual is registered as instance of
   * 
   * @param individualURI
   */
  public ResourceInfo[] getClassesOfIndividual(String repositoryID,
          String individualURI, byte direct) throws RemoteException;
  

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
          String individual1URI, String individual2URI) throws RemoteException;

  /**
   * for the given individual, the method returns all individuals
   * registered as different from the given individual
   * 
   * @param individualURI
   * @return
   */
  public String[] getDifferentIndividualFrom(String repositoryID,
          String individualURI) throws RemoteException;
  /**
   * individual1 is set as same as the individual2
   * 
   * @param individual1URI
   * @param individual2URI
   */
  public void setSameIndividualAs(String repositoryID, String individual1URI,
          String individual2URI) throws RemoteException;
  /**
   * for the given individual, the method returns all individuals which
   * are registered as same as the provided individual
   * 
   * @param inidividualURI
   * @return
   */
  public String[] getSameIndividualAs(String repositoryID, String individualURI)
          throws RemoteException;
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
          String predicateURI, String objectURI) throws RemoteException;

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
          String predicateURI, String objectURI) throws RemoteException;
  
}
