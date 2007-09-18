package gate.creole.ontology.owlim;

import gate.creole.ontology.GateOntologyException;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OntologyUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RdfXmlWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.RepositoryInfo;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.config.SystemConfig;
import org.openrdf.sesame.config.UnknownRepositoryException;
import org.openrdf.sesame.config.UserInfo;
import org.openrdf.sesame.config.handlers.SystemConfigFileHandler;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.RepositoryList;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.repository.local.LocalService;
import org.openrdf.sesame.sail.NamespaceIterator;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sailimpl.OWLIMSchemaRepository;
import org.openrdf.sesame.server.SesameServer;
import org.openrdf.vocabulary.OWL;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;

/**
 * Implementation of the GATE Ontology Services. This class provides an
 * implementation of each and every service defined under the OWLIM
 * interface.
 * 
 * @author niraj
 */
public class OWLIMServiceImpl implements OWLIM,
                             javax.xml.rpc.server.ServiceLifecycle,
                             AdminListener {
  private HashMap<String, RepositoryDetails> mapToRepositoryDetails = new HashMap<String, RepositoryDetails>();

  /**
   * Debug parameter, if set to true, shows various messages when
   * different methods are invoked
   */
  private static boolean DEBUG = false;

  /**
   * Certain operations should be invoked only once. The variable is set
   * to true after the invocation of such operations.
   */
  private static boolean initiated = false;

  /**
   * OWLIMSchemaRepository is used as an interaction layer on top of
   * Sesame server. The class provides various methods of manipulating
   * ontology data.
   */
  private OWLIMSchemaRepository sail;

  /**
   * The reference of currently selected repository is stored in this
   * variable
   */
  private SesameRepository currentRepository;

  /**
   * Log of different operations is stored under this file
   */
  private final String OWLIM_LOG_FILE = "log.html";

  /**
   * The class that provides an implementation of the
   * OWLIM_SCHEMA_REPOSITORY
   */
  private final String OWLIM_SCHEMA_REPOSITORY_CLASS = "org.openrdf.sesame.sailimpl.OWLIMSchemaRepository";

  /**
   * The file that stores the various configuration parameters
   */
  private static URL systemConf = null;

  private static URL owlRDFS = null;

  private static URL rdfSchema = null;

  /**
   * Ontology URL
   */
  private String ontologyUrl;

  /**
   * Whether the repository should return system statements
   */
  private boolean returnSystemStatements;

  /**
   * GOS Home
   */
  private static URL gosHome;

  private LocalService service;

  /**
   * Constructor
   */
  public OWLIMServiceImpl() {
    super();
  }

  // *************************************
  // Listener methods
  // *************************************

  /**
   * An error message with optionally line and column number and the
   * statement to indicate the source of the error.
   */
  public void error(String msg, int lineNo, int columnNo, Statement statement) {
    System.err.println("ERROR :" + msg + "\n at line number :" + lineNo
            + " column number :" + columnNo);
    if(statement != null) {
      System.err.println("With Statement <");
      if(statement.getSubject() != null) {
        System.err.println("Subject :" + statement.getSubject().toString());
      }
      else {
        System.err.println("Subject : not available");
      }
      if(statement.getPredicate() != null) {
        System.err.println("Predicate :" + statement.getPredicate().toString());
      }
      else {
        System.err.println("Predicate : not available");
      }
      if(statement.getObject() != null) {
        System.err.println("Object :" + statement.getObject().toString());
      }
      else {
        System.err.println("Object : not available");
      }
    }
    throw new RuntimeException("Some Error Occured");
  }

  /**
   * A notification message (not an error) with optionally line and
   * column number and the statement to indicate the source of the
   * notification.
   */
  public void notification(String msg, int lineNo, int columnNo,
          Statement statement) {
  }

  /**
   * A status message with optional line and column number to indicate
   * progress.
   */
  public void status(String msg, int lineNo, int columnNo) {

  }

  /**
   * Indicates the end of a transaction.
   */
  public void transactionEnd() {
    // don't do anything
  }

  /**
   * Indicates the start of a transaction.
   */
  public void transactionStart() {
    // don't do anything here
  }

  /**
   * warning messages
   */
  public void warning(String msg, int lineNo, int columnNo, Statement statement) {
    System.err.println("WARNING :" + msg + "\n at line number :" + lineNo
            + " column number :" + columnNo);
    if(statement != null) {
      System.err.println("With Statement <");
      if(statement.getSubject() != null) {
        System.err.println("Subject :" + statement.getSubject().toString());
      }
      else {
        System.err.println("Subject : not available");
      }
      if(statement.getPredicate() != null) {
        System.err.println("Predicate :" + statement.getPredicate().toString());
      }
      else {
        System.err.println("Predicate : not available");
      }
      if(statement.getObject() != null) {
        System.err.println("Object :" + statement.getObject().toString());
      }
      else {
        System.err.println("Object : not available");
      }
    }
  }

  /**
   * This method intializes the OWLIMService. It tries to locate the
   * system configuration file. The system configuration file contains
   * various parameters/settings such as available repositories and
   * users with their rights on each repository
   * 
   * @param context
   * @throws GateOntologyException
   */
  public void init(ServletContext context) throws GateOntologyException {
    if(!initiated) {
      try {
        if(DEBUG) System.out.println("Initiating OWLIMService...");
        // create an instance of service
        URL classURL = this.getClass().getResource(
                "/" + this.getClass().getName().replace('.', '/') + ".class");
        if(classURL.getProtocol().equals("jar")) {
          // running from annic.jar
          String classURLStr = classURL.getFile();
          URL gosJarURL = new URL(classURLStr.substring(0, classURLStr
                  .indexOf('!')));
          // gosURLJar is "file:/path/to/gos/lib/file.jar"
          gosHome = new URL(gosJarURL, "..");
          // gosHome is "file:/path/to/gos/"
        }
        else if(classURL.getProtocol().equals("file")) {
          // running from classes directory (e.g.inside Eclipse)
          // classURL is
          // "file:/path/to/gos/classes/gate/creole/ontology/owlim/OWLIMServiceImpl.class"
          gosHome = new URL(classURL, "../../../../..");
          // gosHome is "file:/path/to/gos/"
        }
        else {
          // this should not happen and will cause a JUnit error if it
          // does.
          gosHome = null;
        }

        systemConf = new URL(gosHome, "system.conf");
        owlRDFS = new URL(gosHome, "owl.rdfs");
        rdfSchema = new URL(gosHome, "rdf-schema.xml");

        SesameServer.setSystemConfig(readConfiguration());
        service = SesameServer.getLocalService();
        initiated = true;
      }
      catch(IOException ioe) {
        throw new GateOntologyException(ioe);
      }
    }
  }

  /**
   * This method intializes the OWLIMService. It locates the system
   * configuration file in the directory whose URL is passed in. The
   * system configuration file contains various parameters/settings such
   * as available repositories and users with their rights on each
   * repository
   * 
   * @param gosHomeURL the URL to the GOS home directory. This must
   *          point to a directory, i.e. it must end in a forward slash.
   * @throws ServiceException
   */
  public void init(URL gosHomeURL) throws GateOntologyException {
    try {
      if(DEBUG) System.out.println("Initiating OWLIMService...");
      gosHome = gosHomeURL;

      systemConf = null;
      owlRDFS = new URL(gosHome, "owl.rdfs");
      rdfSchema = new URL(gosHome, "rdf-schema.xml");

      SystemConfig conf = readConfiguration();
      service = new LocalService(conf);
    }
    catch(IOException ioe) {
      throw new GateOntologyException(ioe);
    }

  }

  /** This is called by axis before calling the operation* */
  public void init(Object arg0) throws ServiceException {
    if(arg0 instanceof javax.xml.rpc.server.ServletEndpointContext) {
      javax.xml.rpc.server.ServletEndpointContext servlet_context = (javax.xml.rpc.server.ServletEndpointContext)arg0;
      ServletContext context = servlet_context.getServletContext();
      try {
        init(context);
      }
      catch(GateOntologyException ioe) {
        throw new ServiceException(ioe);
      }
    }
  }

  /**
   * Gets the default name space for this ontology. The defaultNameSpace
   * is (by default) used for the newly created resources.
   * 
   * @return a String value.
   */
  public String getDefaultNameSpace(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    NamespaceIterator iter = sail.getNamespaces();
    while(iter.hasNext()) {
      iter.next();
      if(iter.getPrefix() == null || iter.getPrefix().trim().length() == 0)
        return iter.getName();
    }
    return null;
  }

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
          byte format) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    try {
      startTransaction(repositoryID);
      currentRepository
              .addData(data, baseURI, getRDFFormat(format), true, this);
      endTransaction(repositoryID);
    }
    catch(AccessDeniedException ioe) {
      throw new GateOntologyException(ioe);
    }
    catch(IOException ioe) {
      throw new GateOntologyException(ioe);
    }
  }

  /**
   * Exports the ontology data into the provided format to the provided
   * output stream.
   * 
   * @param out
   * @param format
   */
  public void writeOntologyData(String repositoryID, OutputStream out,
          byte format) throws Exception {
    loadRepositoryDetails(repositoryID);
    if(sail.transactionStarted()) sail.commitTransaction();

    RdfDocumentWriter writer = null;
    switch(format) {
      case OConstants.ONTOLOGY_FORMAT_N3:
        writer = new N3Writer(out);
        break;
      case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
        writer = new NTriplesWriter(out);
        break;
      case OConstants.ONTOLOGY_FORMAT_TURTLE:
        writer = new TurtleWriter(out);
        break;
      default:
        writer = new RdfXmlWriter(out);
        break;
    }
    writeData(writer);
    switch(format) {
      case OConstants.ONTOLOGY_FORMAT_N3:
        ((N3Writer)writer).endDocument();
        break;
      case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
        ((NTriplesWriter)writer).endDocument();
        break;
      case OConstants.ONTOLOGY_FORMAT_TURTLE:
        ((TurtleWriter)writer).endDocument();
        break;
      default:
        ((RdfXmlWriter)writer).endDocument();
        break;
    }
  }

  /**
   * Exports the ontology data into the provided format using the
   * provided writer.
   * 
   * @param out
   * @param format
   */
  public void writeOntologyData(String repositoryID, Writer out, byte format)
          throws Exception {
    loadRepositoryDetails(repositoryID);
    if(sail.transactionStarted()) sail.commitTransaction();

    RdfDocumentWriter writer = null;
    switch(format) {
      case OConstants.ONTOLOGY_FORMAT_N3:
        writer = new N3Writer(out);
        break;
      case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
        writer = new NTriplesWriter(out);
        break;
      case OConstants.ONTOLOGY_FORMAT_TURTLE:
        writer = new TurtleWriter(out);
        break;
      default:
        writer = new RdfXmlWriter(out);
        break;
    }
    writeData(writer);
    switch(format) {
      case OConstants.ONTOLOGY_FORMAT_N3:
        ((N3Writer)writer).endDocument();
        break;
      case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
        ((NTriplesWriter)writer).endDocument();
        break;
      case OConstants.ONTOLOGY_FORMAT_TURTLE:
        ((TurtleWriter)writer).endDocument();
        break;
      default:
        ((RdfXmlWriter)writer).endDocument();
        break;
    }
  }

  private void writeData(RdfDocumentWriter writer) throws IOException {
    NamespaceIterator nIter = sail.getNamespaces();
    while(nIter.hasNext()) {
      nIter.next();
      writer.setNamespace(nIter.getPrefix(), nIter.getName());
    }
    writer.startDocument();
    writer.writeComment("Ontology Generated from GATE");
    StatementIterator iter = sail.getStatements(null, null, null);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      writer.writeStatement(stmt.getSubject(), stmt.getPredicate(), stmt
              .getObject());
    }
  }

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
          String theSubClassURI, byte direct) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    // check if the classes are equivalent of each
    if(theSuperClassURI.equals(theSubClassURI)) return false;
    if(sail.hasStatement(getResource(theSuperClassURI),
            getURI(OWL.EQUIVALENTCLASS), getResource(theSubClassURI)))
      return false;
    
    if(direct != OConstants.DIRECT_CLOSURE) {
      return sail.getSubClassOf(getResource(theSubClassURI),
              getResource(theSuperClassURI)).hasNext();      
    }
    
    // first lets find out the subClasses of the super class
    StatementIterator iter = sail.getSubClassOf(null, getResource(theSuperClassURI));
    // we just need to check if any of these subclasses is a super class of the sub class
    // if so, return false, otherwise true.
    boolean atleastOne = false;
    while(iter.hasNext()) {

      // here subject is a subclass
      Statement stmt = iter.next();
      if(stmt.getSubject().toString().equals(theSuperClassURI)) continue;
      if(sail.hasStatement(getResource(theSuperClassURI),
              getURI(OWL.EQUIVALENTCLASS), getResource(stmt.getSubject().toString()))) continue;

      atleastOne = true;
      
      if(isSuperClassOf(repositoryID, stmt.getSubject().toString(), theSubClassURI, OConstants.TRANSITIVE_CLOSURE)) {
        return false;
      }
    }
    if(atleastOne)
      return true;
    else
      return false;
  }

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
          String theSubClassURI, byte direct) {
    return isSuperClassOf(repositoryID, theSuperClassURI, theSubClassURI,
            direct);
  }

  /**
   * Given a property URI, this method returns an object of Property
   * 
   * @param repositoryID
   * @param thePropertyURI
   * @return
   * @throws GateOntologyException
   */
  public Property getPropertyFromOntology(String repositoryID,
          String thePropertyURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    // here we need to check which type of property it is
    return createPropertyObject(repositoryID, thePropertyURI);
  }

  /**
   * Checks whether the two classes defined as same in the ontology.
   * 
   * @param theClassURI1
   * @param theClassURI2
   * @return
   * @throws Exception
   */
  public boolean isEquivalentClassAs(String repositoryID, String theClassURI1,
          String theClassURI2) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(theClassURI1),
            getURI(OWL.EQUIVALENTCLASS), getResource(theClassURI2));
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, OWL.ANNOTATIONPROPERTY);
  }

  /**
   * Gets the annotation properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getAnnotationProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.ANNOTATIONPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list.add(new Property(OConstants.ANNOTATION_PROPERTY, anAnnProp
                .toString()));
      }
    }
    boolean allowSystemStatements = this.returnSystemStatements;
    this.returnSystemStatements = true;
    Property[] props = listToPropertyArray(list);
    this.returnSystemStatements = allowSystemStatements;
    return props;
  }

  /**
   * Gets the RDF properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getRDFProperties(String repositoryID, String theResourceURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(RDF.PROPERTY));

    ResourceInfo[] superClasses = new ResourceInfo[0];
    if(hasClass(repositoryID, theResourceURI)) {
      superClasses = getSuperClasses(repositoryID, theResourceURI,
              OConstants.TRANSITIVE_CLOSURE);
    }
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // lets check if the property is indeed an rdf property and not
      // any other
      if(isAnnotationProperty(repositoryID, anAnnProp.toString()))
        continue;
      else if(isDatatypeProperty(repositoryID, anAnnProp.toString()))
        continue;
      else if(isObjectProperty(repositoryID, anAnnProp.toString())) continue;

      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list.add(new Property(OConstants.RDF_PROPERTY, anAnnProp.toString()));
      }
      for(int i = 0; i < superClasses.length; i++) {
        if(sail.getStatements(getResource(superClasses[i].getUri()),
                getURI(anAnnProp.toString()), null).hasNext()) {
          list.add(new Property(OConstants.RDF_PROPERTY, anAnnProp.toString()));
        }
      }
    }
    return listToPropertyArray(list);
  }

  /**
   * Gets the datatype properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getDatatypeProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.DATATYPEPROPERTY));
    ResourceInfo[] superClasses = new ResourceInfo[0];
    if(hasClass(repositoryID, theResourceURI)) {
      superClasses = getSuperClasses(repositoryID, theResourceURI,
              OConstants.TRANSITIVE_CLOSURE);
    }
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list.add(new Property(OConstants.DATATYPE_PROPERTY, anAnnProp
                .toString()));
      }
      for(int i = 0; i < superClasses.length; i++) {
        if(sail.getStatements(getResource(superClasses[i].getUri()),
                getURI(anAnnProp.toString()), null).hasNext()) {
          list.add(new Property(OConstants.DATATYPE_PROPERTY, anAnnProp
                  .toString()));
        }
      }
    }
    return listToPropertyArray(list);
  }

  /**
   * Gets the object properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getObjectProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.OBJECTPROPERTY));
    ResourceInfo[] superClasses = new ResourceInfo[0];
    if(hasClass(repositoryID, theResourceURI)) {
      superClasses = getSuperClasses(repositoryID, theResourceURI,
              OConstants.TRANSITIVE_CLOSURE);
    }
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list
                .add(new Property(OConstants.OBJECT_PROPERTY, anAnnProp
                        .toString()));
      }
      for(int i = 0; i < superClasses.length; i++) {
        if(sail.getStatements(getResource(superClasses[i].getUri()),
                getURI(anAnnProp.toString()), null).hasNext()) {
          list.add(new Property(OConstants.OBJECT_PROPERTY, anAnnProp
                  .toString()));
        }
      }
    }
    return listToPropertyArray(list);
  }

  /**
   * Gets the transitive properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getTransitiveProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.TRANSITIVEPROPERTY));
    ResourceInfo[] superClasses = new ResourceInfo[0];
    if(hasClass(repositoryID, theResourceURI)) {
      superClasses = getSuperClasses(repositoryID, theResourceURI,
              OConstants.TRANSITIVE_CLOSURE);
    }
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list.add(new Property(OConstants.TRANSITIVE_PROPERTY, anAnnProp
                .toString()));
      }
      for(int i = 0; i < superClasses.length; i++) {
        if(sail.getStatements(getResource(superClasses[i].getUri()),
                getURI(anAnnProp.toString()), null).hasNext()) {
          list.add(new Property(OConstants.TRANSITIVE_PROPERTY, anAnnProp
                  .toString()));
        }
      }
    }
    return listToPropertyArray(list);
  }

  /**
   * Gets the symmetric properties set on the specified resource
   * 
   * @param repositoryID
   * @param theResourceURI
   * @return
   * @throws GateOntologyException
   */
  public Property[] getSymmetricProperties(String repositoryID,
          String theResourceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.SYMMETRICPROPERTY));
    ResourceInfo[] superClasses = new ResourceInfo[0];
    if(hasClass(repositoryID, theResourceURI)) {
      superClasses = getSuperClasses(repositoryID, theResourceURI,
              OConstants.TRANSITIVE_CLOSURE);
    }
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource anAnnProp = stmt.getSubject();
      // for this property, check if there is any value available for
      // the
      // given resource
      if(sail.getStatements(getResource(theResourceURI),
              getURI(anAnnProp.toString()), null).hasNext()) {
        list.add(new Property(OConstants.SYMMETRIC_PROPERTY, anAnnProp
                .toString()));
      }
      for(int i = 0; i < superClasses.length; i++) {
        if(sail.getStatements(getResource(superClasses[i].getUri()),
                getURI(anAnnProp.toString()), null).hasNext()) {
          list.add(new Property(OConstants.TRANSITIVE_PROPERTY, anAnnProp
                  .toString()));
        }
      }
    }
    return listToPropertyArray(list);
  }

  /**
   * returns if the given property is an Annotation property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isAnnotationProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.ANNOTATIONPROPERTY));
  }

  /**
   * Adds a new annotation property value and specifies the language.
   * 
   * @param theAnnotationProperty the annotation property
   * @param value the value containing some value
   * @return
   */
  public void addAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws GateOntologyException {
    // isAnnotationProperty also checks for the correct repository so no
    // need to give a call to it
    if(!isAnnotationProperty(repositoryID, theAnnotationPropertyURI)) {
      throw new GateOntologyException(
              "No annotation property found with the URI :"
                      + theAnnotationPropertyURI);
    }
    addUULStatement(theResourceURI, theAnnotationPropertyURI, value, language);
  }

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
          throws GateOntologyException {
    // isAnnotationProperty also checks for the correct repository so no
    // need to give a call to it
    if(!isAnnotationProperty(repositoryID, theAnnotationPropertyURI)) {
      throw new GateOntologyException(
              "No annotation property found with the URI :"
                      + theAnnotationPropertyURI);
    }
    ArrayList<PropertyValue> list = new ArrayList<PropertyValue>();
    StatementIterator iter = sail.getStatements(getResource(theResourceURI),
            getURI(theAnnotationPropertyURI), null);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      PropertyValue pv;
      if(stmt.getObject() instanceof Literal) {
        Literal literal = (Literal)stmt.getObject();
        pv = new PropertyValue(literal.getLanguage(), literal.getLabel());
      }
      else {
        pv = new PropertyValue(null, stmt.getObject().toString());
      }
      list.add(pv);
    }
    return listToPropertyValueArray(list);
  }

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
          String language) throws GateOntologyException {
    // isAnnotationProperty also checks for the correct repository so no
    // need to give a call to it
    if(!isAnnotationProperty(repositoryID, theAnnotationPropertyURI)) {
      throw new GateOntologyException(
              "No annotation property found with the URI :"
                      + theAnnotationPropertyURI);
    }
    StatementIterator iter = sail.getStatements(getResource(theResourceURI),
            getURI(theAnnotationPropertyURI), null);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      Literal literal = (Literal)stmt.getObject();
      if(language == null || literal.getLanguage().equals(language))
        return literal.getLabel();
    }
    return null;
  }

  /**
   * For the current resource, the method removes the given literal for
   * the given property.
   * 
   * @param theAnnotationProperty
   * @param literal
   */
  public void removeAnnotationPropertyValue(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI, String value,
          String language) throws GateOntologyException {
    // isAnnotationProperty also checks for the correct repository so no
    // need to give a call to it
    if(!isAnnotationProperty(repositoryID, theAnnotationPropertyURI)) {
      throw new GateOntologyException(
              "No annotation property found with the URI :"
                      + theAnnotationPropertyURI);
    }
    startTransaction(repositoryID);
    removeUULStatement(theResourceURI, theAnnotationPropertyURI, value,
            language);
    endTransaction(repositoryID);
  }

  /**
   * Removes all values for a named property.
   * 
   * @param theProperty the property
   */
  public void removeAnnotationPropertyValues(String repositoryID,
          String theResourceURI, String theAnnotationPropertyURI)
          throws GateOntologyException {
    try {
      // isAnnotationProperty also checks for the correct repository so
      // no
      // need to give a call to it
      if(!isAnnotationProperty(repositoryID, theAnnotationPropertyURI)) {
        throw new GateOntologyException(
                "No annotation property found with the URI :"
                        + theAnnotationPropertyURI);
      }
      startTransaction(repositoryID);
      sail.removeStatements(getResource(theResourceURI),
              getURI(theAnnotationPropertyURI), null);
      endTransaction(repositoryID);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, RDF.PROPERTY);
    if(domainClassesURIs != null) {
      for(int i = 0; i < domainClassesURIs.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.DOMAIN, domainClassesURIs[i]);
      }
    }
    if(rangeClassesTypes != null) {
      for(int i = 0; i < rangeClassesTypes.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.RANGE, rangeClassesTypes[i]);
      }
    }
  }

  /**
   * returns if the given property is an RDF property
   * 
   * @param aPropertyURI
   * @return Done
   */
  public boolean isRDFProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(RDF.PROPERTY));
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, OWL.DATATYPEPROPERTY);
    addUUUStatement(aPropertyURI, RDFS.RANGE, dataTypeURI);

    if(domainClassesURIs != null) {
      for(int i = 0; i < domainClassesURIs.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.DOMAIN, domainClassesURIs[i]);
      }
    }
  }

  /**
   * Returns the datatype uri specified for the given datatype property.
   * 
   * @param repositoryID
   * @param theDatatypePropertyURI
   * @return
   * @throws GateOntologyException
   */
  public String getDatatype(String repositoryID, String theDatatypePropertyURI)
          throws GateOntologyException {
    // isAnnotationProperty also checks for the correct repository so no
    // need to give a call to it
    if(!isDatatypeProperty(repositoryID, theDatatypePropertyURI)) {
      throw new GateOntologyException("Invalid DatatypeProperty :"
              + theDatatypePropertyURI);
    }

    StatementIterator iter = sail.getStatements(
            getResource(theDatatypePropertyURI), getURI(RDFS.RANGE), null);

    String toReturn = null;
    while(iter.hasNext()) {
      toReturn = iter.next().getObject().toString();
      if(OntologyUtilities.getDataType(toReturn) != null) {
        return toReturn;
      }
    }
    return null;
  }

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
          String[] domainAndRangeClassesURIs) throws GateOntologyException {
    if(DEBUG) print("addSymmetricProperty");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, OWL.SYMMETRICPROPERTY);
    if(domainAndRangeClassesURIs != null) {
      for(int i = 0; i < domainAndRangeClassesURIs.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.DOMAIN, domainAndRangeClassesURIs[i]);
        addUUUStatement(aPropertyURI, RDFS.RANGE, domainAndRangeClassesURIs[i]);
      }
    }
  }

  /**
   * Checkes whether the two properties are Equivalent.
   * 
   * @param repositoryID
   * @param aPropertyURI
   * @return
   * @throws GateOntologyException
   */
  public boolean isEquivalentPropertyAs(String repositoryID,
          String aPropertyURI1, String aPropertyURI2)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI1),
            getURI(OWL.EQUIVALENTPROPERTY), getResource(aPropertyURI2));
  }

  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, byte direct) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE) {
      iter = sail.getDirectSubPropertyOf(getResource(aPropertyURI), null);
    }
    else {
      iter = sail.getSubPropertyOf(getResource(aPropertyURI), null);
    }
    ArrayList<Property> properties = new ArrayList<Property>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      String aSuperProperty = stmt.getObject().toString();
      if(aSuperProperty.equals(aPropertyURI)) {
        continue;
      }

      if(sail.hasStatement(stmt.getSubject(), getURI(OWL.EQUIVALENTPROPERTY),
              stmt.getObject())) continue;

      byte type = getPropertyType(repositoryID, aSuperProperty);
      properties.add(new Property(type, aSuperProperty));
    }
    return listToPropertyArray(properties);
  }

  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          byte direct) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE) {
      iter = sail.getDirectSubPropertyOf(null, getResource(aPropertyURI));
    }
    else {
      iter = sail.getSubPropertyOf(null, getResource(aPropertyURI));
    }
    ArrayList<Property> properties = new ArrayList<Property>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      String aSubProperty = stmt.getSubject().toString();
      if(aSubProperty.equals(aPropertyURI)) {
        continue;
      }

      if(sail.hasStatement(stmt.getSubject(), getURI(OWL.EQUIVALENTPROPERTY),
              stmt.getObject())) continue;

      byte type = getPropertyType(repositoryID, aSubProperty);
      properties.add(new Property(type, aSubProperty));
    }
    return listToPropertyArray(properties);
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(sail.hasStatement(getResource(aSuperPropertyURI),
            getURI(OWL.EQUIVALENTPROPERTY), getResource(aSubPropertyURI)))
      return false;

    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE) {
      iter = sail.getDirectSubPropertyOf(getResource(aSubPropertyURI),
              getResource(aSuperPropertyURI));
    }
    else {
      iter = sail.getSubPropertyOf(getResource(aSubPropertyURI),
              getResource(aSuperPropertyURI));
    }
    return iter.hasNext();
    
  }

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
          String aSubPropertyURI, byte direct) throws GateOntologyException {
    return isSuperPropertyOf(repositoryID, aSuperPropertyURI, aSubPropertyURI,
            direct);
  }

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
          String individualURI, byte direct) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE)
      iter = sail.getDirectType(getResource(individualURI),
              getResource(aSuperClassURI));
    else iter = sail.getType(getResource(individualURI),
            getResource(aSuperClassURI));
    return iter.hasNext();
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(theInstanceURI1),
            getURI(OWL.DIFFERENTFROM), getResource(theInstanceURI2));
  }

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
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(theInstanceURI1),
            getURI(OWL.DIFFERENTFROM), getResource(theInstanceURI2));
  }

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
          String anRDFPropertyURI, String aResourceURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    addUUUStatement(anInstanceURI, anRDFPropertyURI, aResourceURI);
  }

  /**
   * Removes the specified RDF Property Value
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @param aResourceURI
   */
  public void removeRDFPropertyValue(String repositoryID, String anInstanceURI,
          String anRDFPropertyURI, String aResourceURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    startTransaction(repositoryID);
    removeUUUStatement(anInstanceURI, anRDFPropertyURI, aResourceURI);
    endTransaction(repositoryID);
  }

  /**
   * gets the rdf property values for the specified instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   * @return resource URIs
   */
  public ResourceInfo[] getRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(anInstanceURI),
            getURI(anRDFPropertyURI), null);
    ArrayList<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      list.add(stmt.getObject());
    }
    return listToResourceInfoArray(list);
  }

  /**
   * Removes all the RDF Property values from the given instance.
   * 
   * @param repositoryID
   * @param anInstanceURI
   * @param anRDFPropertyURI
   */
  public void removeRDFPropertyValues(String repositoryID,
          String anInstanceURI, String anRDFPropertyURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    try {
      startTransaction(repositoryID);
      sail.removeStatements(getResource(anInstanceURI),
              getURI(anRDFPropertyURI), null);
      endTransaction(repositoryID);
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }
  }

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
          String datatypeURI, String value) throws GateOntologyException {
    if(!isDatatypeProperty(repositoryID, aDatatypePropertyURI)) {
      throw new GateOntologyException("No datatype property exists with URI :"
              + aDatatypePropertyURI);
    }
    addUUDStatement(anInstanceURI, aDatatypePropertyURI, value, datatypeURI);
  }

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
          String datatypeURI, String value) throws GateOntologyException {
    if(!isDatatypeProperty(repositoryID, aDatatypePropertyURI)) {
      throw new GateOntologyException("No datatype property exists with URI :"
              + aDatatypePropertyURI);
    }
    startTransaction(repositoryID);
    removeUUDStatement(anInstanceURI, aDatatypePropertyURI, value, datatypeURI);
    endTransaction(repositoryID);
  }

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
          throws GateOntologyException {
    if(!isDatatypeProperty(repositoryID, aDatatypePropertyURI)) {
      throw new GateOntologyException("No datatype property exists with URI :"
              + aDatatypePropertyURI);
    }
    ArrayList<PropertyValue> propValues = new ArrayList<PropertyValue>();
    StatementIterator iter = sail.getStatements(getResource(anInstanceURI),
            getURI(aDatatypePropertyURI), null);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      Literal literal = (Literal)stmt.getObject();
      String datatype = "http://www.w3.org/2001/XMLSchema#string";
      if(literal.getDatatype() != null) {
        datatype = literal.getDatatype().toString();
      }
      propValues.add(new PropertyValue(datatype, literal.getLabel()));
    }
    return listToPropertyValueArray(propValues);
  }

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
          throws GateOntologyException {
    if(!isDatatypeProperty(repositoryID, aDatatypePropertyURI)) {
      throw new GateOntologyException("No datatype property exists with URI :"
              + aDatatypePropertyURI);
    }
    startTransaction(repositoryID);
    removeUUUStatement(anInstanceURI, aDatatypePropertyURI, null);
    endTransaction(repositoryID);
  }

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
          String theValueInstanceURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(!sail.hasStatement(getResource(anObjectPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.OBJECTPROPERTY))) {
      throw new GateOntologyException("No object property exists with URI :"
              + anObjectPropertyURI);
    }
    addUUUStatement(sourceInstanceURI, anObjectPropertyURI, theValueInstanceURI);
  }

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
          String theValueInstanceURI) throws GateOntologyException {
    if(!isObjectProperty(repositoryID, anObjectPropertyURI)) {
      throw new GateOntologyException("No object property exists with URI :"
              + anObjectPropertyURI);
    }

    startTransaction(repositoryID);
    removeUUUStatement(sourceInstanceURI, anObjectPropertyURI,
            theValueInstanceURI);
    endTransaction(repositoryID);
  }

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
          throws GateOntologyException {
    if(!isObjectProperty(repositoryID, anObjectPropertyURI)
            && !isTransitiveProperty(repositoryID, anObjectPropertyURI)
            && !isSymmetricProperty(repositoryID, anObjectPropertyURI)) {
      throw new GateOntologyException(
              "No object/transitive/symmetric property exists with URI :"
                      + anObjectPropertyURI);
    }
    ArrayList<String> propValues = new ArrayList<String>();
    StatementIterator iter = sail.getStatements(getResource(sourceInstanceURI),
            getURI(anObjectPropertyURI), null);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      propValues.add(stmt.getObject().toString());
    }
    return listToArray(propValues);
  }

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
          throws GateOntologyException {
    if(!isObjectProperty(repositoryID, anObjectPropertyURI)) {
      throw new GateOntologyException("No object property exists with URI :"
              + anObjectPropertyURI);
    }
    startTransaction(repositoryID);
    removeUUUStatement(sourceInstanceURI, anObjectPropertyURI, null);
    endTransaction(repositoryID);
  }

  /** This should be called by axis after each call to the operator?* */
  public void destroy() {
    // we don't want to do anything here
    // because we want to keep alive all our resources
    // until the logout method is called
  }

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
  public boolean login(String username, String password) {
    if(DEBUG) print("login");
    try {
      service.login(username, password);
    }
    catch(AccessDeniedException ade) {
      return false;
    }
    int id = getUserID(username, password);
    if(id == -1) {
      return false;
    }
    return true;
  }

  /**
   * End the session by logging out
   */
  public void logout(String repositoryID) throws GateOntologyException {
    if(DEBUG) print("logout");
    mapToRepositoryDetails.remove(repositoryID);
    service.logout();
    service.shutDown();
    currentRepository = null;
    sail = null;
    System.gc();
  }

  // ****************************************************************************
  // repository methods
  // ****************************************************************************
  /**
   * Find out the list of repository list
   */
  public String[] getRepositoryList() throws GateOntologyException {
    if(DEBUG) print("getRepositoryList");
    RepositoryList rList = service.getRepositoryList();
    List repositories = rList.getRepositories();
    if(repositories == null) return new String[0];
    String[] reps = new String[repositories.size()];
    for(int i = 0; i < reps.length; i++) {
      RepositoryInfo rInfo = (RepositoryInfo)repositories.get(i);
      reps[i] = rInfo.getRepositoryId();
    }
    return reps;
  }

  /**
   * sets the provided repository as a current repository
   */
  public void setCurrentRepositoryID(String repositoryID)
          throws GateOntologyException {
    if(DEBUG) print("setCurrentRepository with ID " + repositoryID);
    if(sail != null && sail.transactionStarted()) {
      // we need to commit all changes
      sail.commitTransaction();
    }
    try {
      LocalRepository lr = (LocalRepository)service.getRepository(repositoryID);

      if(lr == null) {
        throw new GateOntologyException("Repository ID " + repositoryID
                + " does not exist!");
      }
      if(!(lr.getSail() instanceof OWLIMSchemaRepository)) {
        throw new GateOntologyException("Repository ID " + repositoryID
                + "is not an OWLIMSchemaRepository!");
      }
      currentRepository = lr;
      sail = (OWLIMSchemaRepository)lr.getSail();

      RepositoryDetails rd = mapToRepositoryDetails.get(repositoryID);
      if(rd == null) {
        rd = new RepositoryDetails();
        rd.repository = currentRepository;
        rd.sail = sail;
        rd.ontologyUrl = ontologyUrl;
        rd.returnSystemStatements = returnSystemStatements;

        mapToRepositoryDetails.put(repositoryID, rd);
      }
      else {
        ontologyUrl = rd.ontologyUrl;
      }
    }
    catch(ConfigurationException ce) {
      throw new GateOntologyException(ce);
    }
    catch(UnknownRepositoryException ure) {
      throw new GateOntologyException(ure);
    }
  }

  /**
   * This method returns the ID of current repository
   */
  public String getCurrentRepositoryID() {
    if(DEBUG) print("getCurrentRepository");
    return currentRepository.getRepositoryId();
  }

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
          boolean returnSystemStatements) throws GateOntologyException {
    if(DEBUG) print("createRepository");
    if(absolutePersistLocation == null) {
      try {
        absolutePersistLocation = new File(gosHome.toURI()).getAbsolutePath();
      }
      catch(URISyntaxException e) {
        throw new GateOntologyException(
                "Cannot construct persistence location " + "from gosHome", e);
      }
    }
    // check if user exists
    if(password == null) password = "";
    createNewUser(username, password);
    this.returnSystemStatements = returnSystemStatements;
    boolean found = setRepository(repositoryID, ontoData, true, baseURI,
            format, absolutePersistLocation, persist);
    if(found) {
      return repositoryID;
    }
    // we create a new repository
    RepositoryConfig repConfig = createNewRepository(repositoryID, ontoData,
            true, baseURI, persist, absolutePersistLocation, username,
            password, format, false);

    addOntologyData(repositoryID, ontoData, true, baseURI, format);
    service.getSystemConfig().addRepositoryConfig(repConfig);
    if(persist) saveConfiguration();
    if(DEBUG) System.out.println("Repository created!");

    // and here we check if there's any statement referring to owl:Thing
    loadRepositoryDetails(repositoryID);
    return repositoryID;
  }

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
          boolean returnSystemStatements) throws GateOntologyException {
    if(DEBUG) print("createRepository");
    if(absolutePersistLocation == null) {
      try {
        absolutePersistLocation = new File(gosHome.toURI()).getAbsolutePath();
      }
      catch(URISyntaxException e) {
        throw new GateOntologyException(
                "Cannot construct persistence location " + "from gosHome", e);
      }
    }
    // check if user exists
    if(password == null) password = "";
    createNewUser(username, password);
    this.returnSystemStatements = returnSystemStatements;
    boolean found = setRepository(repositoryID, ontoFileUrl, false, baseURI,
            format, absolutePersistLocation, persist);
    if(found) {
      return repositoryID;
    }
    RepositoryConfig repConfig = createNewRepository(repositoryID, ontoFileUrl,
            false, baseURI, persist, absolutePersistLocation, username,
            password, format, false);
    addOntologyData(repositoryID, ontoFileUrl, false, baseURI, format);
    service.getSystemConfig().addRepositoryConfig(repConfig);
    if(persist) saveConfiguration();
    if(DEBUG) System.out.println("Repository created!");
    this.ontologyUrl = ontoFileUrl;

    // and here we check if there's any statement referring to owl:Thing
    loadRepositoryDetails(repositoryID);
    return repositoryID;
  }

  /**
   * Removes the repository with given ID
   * 
   * @param repositoryID
   * @return
   */
  public void removeRepository(String repositoryID, boolean persist)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    startTransaction(null);
    if(currentRepository == null) return;
    try {
      sail.clearRepository();
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }
    service.getSystemConfig().removeRepository(
            currentRepository.getRepositoryId());
    if(persist) saveConfiguration();
    endTransaction(null);
    mapToRepositoryDetails.remove(repositoryID);
  }

  // *******************************************************************
  // *************************** Ontology Methods **********************
  // *******************************************************************
  /**
   * The method removes all data from the available graph.
   */
  public void cleanOntology(String repositoryID) throws GateOntologyException {
    if(DEBUG) print("cleanOntology");
    loadRepositoryDetails(repositoryID);
    if(currentRepository == null) return;
    RepositoryConfig rc = service.getSystemConfig().getRepositoryConfig(
            currentRepository.getRepositoryId());
    if(rc == null) return;
    startTransaction(repositoryID);
    try {
      sail.clearRepository();
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }

    endTransaction(repositoryID);
  }

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
          throws GateOntologyException {
    if(DEBUG) print("getOntologyData");
    loadRepositoryDetails(repositoryID);
    // before we extract anything
    // lets commit all our changes
    if(sail.transactionStarted()) sail.commitTransaction();
    try {
      InputStream stream = currentRepository.extractRDF(getRDFFormat(format),
              true, true, true, true);
      BufferedReader br = new BufferedReader(new InputStreamReader(stream));
      String line = br.readLine();
      StringBuffer sb = new StringBuffer(1028);
      while(line != null) {
        sb.append(line + "\n");
        line = br.readLine();
      }
      return sb.toString();
    }
    catch(AccessDeniedException ade) {
      throw new GateOntologyException(ade);
    }
    catch(IOException ioe) {
      throw new GateOntologyException(ioe);
    }
  }

  /**
   * The method allows adding version information to the repository.
   * 
   * @param versionInfo
   */
  public void setVersion(String repositoryID, String versionInfo)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(DEBUG) print("setVersion");
    addUULStatement(this.ontologyUrl, OWL.VERSIONINFO, versionInfo, null);
  }

  /**
   * The method returns the version information of the repository.
   * 
   * @return
   */
  public String getVersion(String repositoryID) throws GateOntologyException {
    if(DEBUG) print("getVersion");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(this.ontologyUrl),
            getURI(OWL.VERSIONINFO), null);
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      return stmt.getObject().toString();
    }
    return null;
  }

  // *******************************************************************
  // class methods
  // *******************************************************************
  /**
   * The method allows adding a class to repository.
   * 
   * @param classURI
   */
  public void addClass(String repositoryID, String classURI, byte classType)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(DEBUG) print("addClass");
    switch(classType) {
      case OConstants.OWL_CLASS:
        addUUUStatement(classURI, RDF.TYPE, OWL.CLASS);
        return;
      default:
        addUUUStatement(classURI, RDF.TYPE, OWL.RESTRICTION);
        return;
    }
  }

  /**
   * Given a class to delete, it removes it from the repository.
   * 
   * @param classURI
   * @return a list of other resources, which got removed as a result of
   *         this deletion
   */
  public String[] removeClass(String repositoryID, String classURI)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(DEBUG) print("removeClass");
    List<String> deletedResources = new ArrayList<String>();
    if(removeUUUStatement(classURI, RDF.TYPE, null) == 0) {
      throw new GateOntologyException(classURI + " is not an explicit resource");
    }
    else {
      deletedResources.add(classURI);
    }

    try {
      startTransaction(repositoryID);
      sail.removeStatements(getResource(classURI), getURI(RDFS.SUBCLASSOF),
              null);
      endTransaction(repositoryID);
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue.getMessage());
    }
    ResourceInfo[] subClasses = getSubClasses(repositoryID, classURI,
            OConstants.DIRECT_CLOSURE);
    for(int i = 0; i < subClasses.length; i++) {
      String[] removedResources = removeClass(repositoryID, subClasses[i]
              .getUri());
      deletedResources.addAll(Arrays.asList(removedResources));
    }
    String[] individuals = getIndividuals(repositoryID, classURI,
            OConstants.DIRECT_CLOSURE);
    for(int i = 0; i < individuals.length; i++) {
      String[] removedResources = removeIndividual(repositoryID, individuals[i]);
      deletedResources.addAll(Arrays.asList(removedResources));
    }
    startTransaction(repositoryID);
    StatementIterator iter = sail.getStatements(null, getURI(RDFS.DOMAIN),
            getResource(classURI));
    endTransaction(repositoryID);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      Resource resource = stmt.getSubject();
      String[] removedResources = removePropertyFromOntology(repositoryID,
              resource.toString());
      deletedResources.addAll(Arrays.asList(removedResources));
    }
    startTransaction(repositoryID);
    iter = sail.getStatements(null, getURI(RDFS.RANGE), getResource(classURI));
    endTransaction(repositoryID);
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      Resource resource = stmt.getSubject();
      String[] removedResources = removePropertyFromOntology(repositoryID,
              resource.toString());
      deletedResources.addAll(Arrays.asList(removedResources));
    }
    // finaly remove all statements concerning this resource
    try {
      startTransaction(repositoryID);
      sail.removeStatements(getResource(classURI), null, null);
      if(!(getResource(classURI) instanceof BNode))
        sail.removeStatements(null, getURI(classURI), null);
      sail.removeStatements(null, null, getResource(classURI));

      endTransaction(repositoryID);
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }

    return listToArray(deletedResources);
  }

  /**
   * The method returns if the current repository has a class with URI
   * that matches with the class parameter.
   * 
   * @return
   */
  public boolean hasClass(String repositoryID, String classURI)
          throws GateOntologyException {
    if(DEBUG) print("hasClass");
    loadRepositoryDetails(repositoryID);
    return sail.isClass(getResource(classURI));
  }

  /**
   * if top set to true, the method returns only the top classes (i.e.
   * classes with no super class). Otherwise it returns all classes
   * available in repository.
   * 
   * @param top
   * @return
   */
  public ResourceInfo[] getClasses(String repositoryID, boolean top)
          throws GateOntologyException {
    if(DEBUG) print("getClasses");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getClasses();
    ArrayList<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement statement = iter.next();
      Resource uri = null;
      uri = getResource(statement.getSubject().toString());
      if(top) {
        if(!isTopClass(repositoryID, uri.toString())) {
          continue;
        }
      }
      list.add(uri);
    }
    if(DEBUG) System.out.println("Top Classes : " + list.size());
    return listToResourceInfoArray(list);
  }

  /**
   * Returns if the given class is a top class. It also returns false if
   * the class is an instance of BNode
   * 
   * @param classURI
   * @return
   */
  public boolean isTopClass(String repositoryID, String classURI)
          throws GateOntologyException {
    if(DEBUG) print("isTopClass");
    loadRepositoryDetails(repositoryID);
    Resource resource = getResource(classURI);
    StatementIterator iter = sail.getSubClassOf(resource, null);
    boolean result = true;
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      if(stmt.getObject().toString().equals(classURI)) continue;
      if(sail.getStatements(stmt.getSubject(), getURI(OWL.EQUIVALENTCLASS),
              stmt.getObject()).hasNext()) continue;
      
      // if the parent class is an instance of BNode we still consider
      // the object to be a top class
      if(stmt.getObject() instanceof BNode) {
        if(stmt.getSubject() instanceof BNode) {
          result = false;
          break;
        }
        continue;
      }
      result = false;
      break;
    }

    
    // if there is any element, the class is not a super class
    return result;
  }

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
          String subClassURI) throws GateOntologyException {
    if(DEBUG) print("addSubClass");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(subClassURI, RDFS.SUBCLASSOF, superClassURI);
  }

  /**
   * The method creates a new class with the URI as specified in
   * className and adds it as a superClassOf the parentClass. It also
   * adds the provided comment on the subClass.
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void addSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException {
    if(DEBUG) print("addSuperClass");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(subClassURI, RDFS.SUBCLASSOF, superClassURI);
  }

  /**
   * Removes the subclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSubClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException {
    if(DEBUG) print("removeSubClass");
    loadRepositoryDetails(repositoryID);

    StatementIterator iter = sail.getDirectType(null, getResource(subClassURI));
    removeUUUStatement(subClassURI, RDFS.SUBCLASSOF, superClassURI);
  }

  /**
   * Removes the superclass relationship
   * 
   * @param superClassURI
   * @param subClassURI
   */
  public void removeSuperClass(String repositoryID, String superClassURI,
          String subClassURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    if(DEBUG) print("removeSuperClass");
    removeUUUStatement(subClassURI, RDFS.SUBCLASSOF, superClassURI);
  }

  /**
   * This method returns all sub classes of the given class
   * 
   * @param superClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSubClasses(String repositoryID,
          String superClassURI, byte direct) throws GateOntologyException {
    if(DEBUG) print("getSubClasses");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    iter = sail.getSubClassOf(null, getResource(superClassURI));
    List<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      if(isSubClassOf(repositoryID, superClassURI, stmt.getSubject().toString(), direct)) {
        list.add(stmt.getSubject());
      }
    }
    return listToResourceInfoArray(list);
  }

  
  
  /**
   * This method returns all super classes of the given class
   * 
   * @param subClassURI
   * @param direct
   * @return
   */
  public ResourceInfo[] getSuperClasses(String repositoryID,
          String subClassURI, byte direct) throws GateOntologyException {
    if(DEBUG) print("getSuperClasses");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    iter = sail.getSubClassOf(getResource(subClassURI), null);
    List<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      if(isSubClassOf(repositoryID, stmt.getObject().toString(), subClassURI, direct)) {
        list.add(stmt.getObject());
      }
    }
    return listToResourceInfoArray(list);
  }

  /**
   * Sets the classes as disjoint
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setDisjointClassWith(String repositoryID, String class1URI,
          String class2URI) throws GateOntologyException {
    if(DEBUG) print("setDisjointWith");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(class1URI, OWL.DISJOINTWITH, class2URI);
  }

  /**
   * Sets the classes as same classes
   * 
   * @param class1URI
   * @param class2URI
   */
  public void setEquivalentClassAs(String repositoryID, String class1URI,
          String class2URI) throws GateOntologyException {
    if(DEBUG) print("setEquivalentClassAs");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(class1URI, OWL.EQUIVALENTCLASS, class2URI);
  }

  /**
   * returns an array of classes which are marked as disjoint for the
   * given class
   * 
   * @param classURI
   * @return
   */
  public String[] getDisjointClasses(String repositoryID, String classURI)
          throws GateOntologyException {
    if(DEBUG) print("setDisjointClasses");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(classURI),
            getURI(OWL.DISJOINTWITH), null);
    List<String> list = new ArrayList<String>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();

      list.add(stmt.getObject().toString());
    }
    return listToArray(list);
  }

  /**
   * returns an array of classes which are equivalent as the given class
   * 
   * @param aClassURI
   * @return
   */
  public ResourceInfo[] getEquivalentClasses(String repositoryID,
          String aClassURI) throws GateOntologyException {
    if(DEBUG) print("getSameClasses");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(aClassURI),
            getURI(OWL.EQUIVALENTCLASS), null);
    List<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();

      list.add(stmt.getObject());
    }
    return listToResourceInfoArray(list);
  }

  /**
   * Removes the given property
   * 
   * @param aPropertyURI
   */
  public String[] removePropertyFromOntology(String repositoryID,
          String aPropertyURI) throws GateOntologyException {
    if(DEBUG) print("removePropertyWithName");
    loadRepositoryDetails(repositoryID);
    List<String> deletedResources = new ArrayList<String>();
    if(removeUUUStatement(aPropertyURI, RDF.TYPE, null) == 0) {
      throw new GateOntologyException(aPropertyURI
              + " is not an explicit Property");
    }
    else {
      deletedResources.add(aPropertyURI);
    }

    try {
      startTransaction(repositoryID);
      // removing all values set for the current property
      sail.removeStatements(null, getURI(aPropertyURI), null);
      sail.removeStatements(getResource(aPropertyURI),
              getURI(RDFS.SUBPROPERTYOF), null);
      endTransaction(repositoryID);
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }
    Property[] subProps = getSubProperties(repositoryID, aPropertyURI,
            OConstants.DIRECT_CLOSURE);
    for(int i = 0; i < subProps.length; i++) {
      if(sail.hasExplicitStatement(getResource(subProps[i].getUri()),
              getURI(RDF.TYPE), null)) continue;
      String[] removedResources = removePropertyFromOntology(repositoryID,
              subProps[i].getUri());
      deletedResources.addAll(Arrays.asList(removedResources));
    }
    // deletedResources.add(aPropertyURI);
    // todo convert name into full uri
    // removeUUUStatement(aPropertyURI, RDF.TYPE, null);
    removeUUUStatement(aPropertyURI, null, null);
    removeUUUStatement(null, aPropertyURI, null);
    removeUUUStatement(null, null, aPropertyURI);
    return listToArray(deletedResources);
  }

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
          throws GateOntologyException {
    if(DEBUG) print("addObjectProperty");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, OWL.OBJECTPROPERTY);
    if(domainClassesURIs != null) {
      for(int i = 0; i < domainClassesURIs.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.DOMAIN, domainClassesURIs[i]);
      }
    }
    if(rangeClassesTypes != null) {
      for(int i = 0; i < rangeClassesTypes.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.RANGE, rangeClassesTypes[i]);
      }
    }
  }

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
          throws GateOntologyException {
    if(DEBUG) print("addTransitiveProperty");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(aPropertyURI, RDF.TYPE, OWL.TRANSITIVEPROPERTY);
    if(domainClassesURIs != null) {
      for(int i = 0; i < domainClassesURIs.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.DOMAIN, domainClassesURIs[i]);
      }
    }
    if(rangeClassesTypes != null) {
      for(int i = 0; i < rangeClassesTypes.length; i++) {
        addUUUStatement(aPropertyURI, RDFS.RANGE, rangeClassesTypes[i]);
      }
    }
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getRDFProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(RDF.PROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      // we also need to check that the property is neither of the
      // annotation, datatype or object property
      if(isObjectProperty(repositoryID, stmt.getSubject().toString())
              || isAnnotationProperty(repositoryID, stmt.getSubject()
                      .toString())
              || isDatatypeProperty(repositoryID, stmt.getSubject().toString()))
        continue;

      Property prop = new Property(OConstants.RDF_PROPERTY, stmt.getSubject()
              .toString());
      list.add(prop);
    }
    return listToPropertyArray(list);
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getObjectProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.OBJECTPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.OBJECT_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }

    iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.SYMMETRICPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.SYMMETRIC_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }

    iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.TRANSITIVEPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.TRANSITIVE_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }
    return listToPropertyArray(list);
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getSymmetricProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.SYMMETRICPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.SYMMETRIC_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }
    return listToPropertyArray(list);
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getTransitiveProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.TRANSITIVEPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.TRANSITIVE_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }
    return listToPropertyArray(list);
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getDatatypeProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.DATATYPEPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.DATATYPE_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }
    return listToPropertyArray(list);
  }

  /**
   * The method returns an array of properties. Property is a complex
   * structure, which contains name, comment, information about its
   * domain and range.
   * 
   * @return
   */
  public Property[] getAnnotationProperties(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    List<Property> list = new ArrayList<Property>();
    StatementIterator iter = sail.getStatements(null, getURI(RDF.TYPE),
            getResource(OWL.ANNOTATIONPROPERTY));
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Property prop = new Property(OConstants.ANNOTATION_PROPERTY, stmt
              .getSubject().toString());
      list.add(prop);
    }
    boolean allowSystemStatements = this.returnSystemStatements;
    this.returnSystemStatements = true;
    Property[] props = listToPropertyArray(list);
    this.returnSystemStatements = allowSystemStatements;
    return props;
  }

  /**
   * Given a property, this method returns its domain
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getDomain(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("getDomain");
    if(isAnnotationProperty(repositoryID, aPropertyURI)) {
      throw new GateOntologyException(
              "AnnotationProperties do no specify any domain or range");
    }
    StatementIterator iter = sail.getDomain(getResource(aPropertyURI), null);
    List<ResourceInfo> list = new ArrayList<ResourceInfo>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource res = getResource(stmt.getObject().toString());
      boolean isRestriction = sail.hasStatement(res, getURI(RDF.TYPE),
              getResource(OWL.RESTRICTION));
      byte classType = OConstants.OWL_CLASS;
      if(isRestriction) {
        if(sail.hasStatement(res, getURI(OWL.HASVALUE), null)) {
          classType = OConstants.HAS_VALUE_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.SOMEVALUESFROM), null)) {
          classType = OConstants.SOME_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.ALLVALUESFROM), null)) {
          classType = OConstants.ALL_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.CARDINALITY), null)) {
          classType = OConstants.CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MINCARDINALITY), null)) {
          classType = OConstants.MIN_CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MAXCARDINALITY), null)) {
          classType = OConstants.MAX_CARDINALITY_RESTRICTION;
        }
      }
      else {
        if(res instanceof BNode) {
          classType = OConstants.ANNONYMOUS_CLASS;
        }
      }
      list.add(new ResourceInfo(stmt.getObject().toString(), classType));
    }
    return reduceToMostSpecificClasses(repositoryID, list);
  }

  /**
   * Given a property, this method returns its range
   * 
   * @param aPropertyURI
   * @return
   */
  public ResourceInfo[] getRange(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("getRange");
    if(isAnnotationProperty(repositoryID, aPropertyURI)) {
      throw new GateOntologyException(
              "AnnotationProperties do no specify any domain or range");
    }
    if(isDatatypeProperty(repositoryID, aPropertyURI)) {
      throw new GateOntologyException(
              "Please use getDatatype(String repositoryID, String theDatatypeProerptyURI) method instead");
    }
    StatementIterator iter = sail.getRange(getResource(aPropertyURI), null);
    List<ResourceInfo> list = new ArrayList<ResourceInfo>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      Resource res = getResource(stmt.getObject().toString());
      boolean isRestriction = sail.hasStatement(res, getURI(RDF.TYPE),
              getResource(OWL.RESTRICTION));
      byte classType = OConstants.OWL_CLASS;
      if(isRestriction) {
        if(sail.hasStatement(res, getURI(OWL.HASVALUE), null)) {
          classType = OConstants.HAS_VALUE_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.SOMEVALUESFROM), null)) {
          classType = OConstants.SOME_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.ALLVALUESFROM), null)) {
          classType = OConstants.ALL_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.CARDINALITY), null)) {
          classType = OConstants.CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MINCARDINALITY), null)) {
          classType = OConstants.MIN_CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MAXCARDINALITY), null)) {
          classType = OConstants.MAX_CARDINALITY_RESTRICTION;
        }
      }
      else {
        if(res instanceof BNode) {
          classType = OConstants.ANNONYMOUS_CLASS;
        }
      }
      list.add(new ResourceInfo(stmt.getObject().toString(), classType));
    }
    return reduceToMostSpecificClasses(repositoryID, list);
  }

  /**
   * Returns if the provided property is functional
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isFunctional(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isFunctional");
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.FUNCTIONALPROPERTY));
  }

  /**
   * sets the current property as functional
   * 
   * @param aPropertyURI
   * @param isFunctional
   */
  public void setFunctional(String repositoryID, String aPropertyURI,
          boolean isFunctional) throws GateOntologyException {
    if(DEBUG) print("setFunctional");
    loadRepositoryDetails(repositoryID);
    if(isFunctional) {
      addUUUStatement(aPropertyURI, RDF.TYPE, OWL.FUNCTIONALPROPERTY);
    }
    else {
      removeUUUStatement(aPropertyURI, RDF.TYPE, OWL.FUNCTIONALPROPERTY);
    }
  }

  /**
   * returns if the given property is inverse functional property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isInverseFunctional(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isInverseFunctional");
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.INVERSEFUNCTIONALPROPERTY));
  }

  /**
   * Sets the current property as inverse functional property
   * 
   * @param aPropertyURI
   * @param isInverseFunctional
   */
  public void setInverseFunctional(String repositoryID, String aPropertyURI,
          boolean isInverseFunctional) throws GateOntologyException {
    if(DEBUG) print("setInverseFunctional");
    loadRepositoryDetails(repositoryID);
    if(isInverseFunctional) {
      addUUUStatement(aPropertyURI, RDF.TYPE, OWL.INVERSEFUNCTIONALPROPERTY);
    }
    else {
      removeUUUStatement(aPropertyURI, RDF.TYPE, OWL.INVERSEFUNCTIONALPROPERTY);
    }
  }

  /**
   * returns if the given property is a symmetric property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isSymmetricProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isSymmetricProperty");
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.SYMMETRICPROPERTY));
  }

  /**
   * returns if the given property is a transitive property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isTransitiveProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isTransitiveProperty");
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.TRANSITIVEPROPERTY));
  }

  /**
   * returns if the given property is a datatype property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isDatatypeProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isDatatypeProperty");
    loadRepositoryDetails(repositoryID);
    return sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
            getResource(OWL.DATATYPEPROPERTY));
  }

  /**
   * returns if the given property is an object property
   * 
   * @param aPropertyURI
   * @return
   */
  public boolean isObjectProperty(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("isObjectProperty");
    loadRepositoryDetails(repositoryID);
    boolean reply = sail.hasStatement(getResource(aPropertyURI),
            getURI(RDF.TYPE), getResource(OWL.OBJECTPROPERTY));
    if(!reply)
      reply = sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
              getResource(OWL.SYMMETRICPROPERTY));
    if(!reply)
      reply = sail.hasStatement(getResource(aPropertyURI), getURI(RDF.TYPE),
              getResource(OWL.TRANSITIVEPROPERTY));
    return reply;
  }

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
          String property2URI) throws GateOntologyException {
    if(DEBUG) print("setEquivalentPropertyAs");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(property1URI, OWL.EQUIVALENTPROPERTY, property2URI);
  }

  /**
   * For the given property, this method returns all properties marked
   * as Equivalent as it
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getEquivalentPropertyAs(String repositoryID,
          String aPropertyURI) throws GateOntologyException {
    if(DEBUG) print("getEquivalentPropertyAs");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(aPropertyURI),
            getURI(OWL.EQUIVALENTPROPERTY), null);
    List<Property> list = new ArrayList<Property>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();
      list.add(createPropertyObject(repositoryID, stmt.getObject().toString()));
    }
    return listToPropertyArray(list);
  }

  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException {
    if(DEBUG) print("addSuperProperty");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(subPropertyURI, RDFS.SUBPROPERTYOF, superPropertyURI);
  }

  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSuperProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException {
    if(DEBUG) print("removeSuperProperty");
    loadRepositoryDetails(repositoryID);
    removeUUUStatement(subPropertyURI, RDFS.SUBPROPERTYOF, superPropertyURI);
  }

  /**
   * For the given properties, this method registers the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void addSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException {
    if(DEBUG) print("addSubProperty");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(subPropertyURI, RDFS.SUBPROPERTYOF, superPropertyURI);
  }

  /**
   * For the given properties, this method removes the super, sub
   * relation
   * 
   * @param superPropertyURI
   * @param subPropertyURI
   */
  public void removeSubProperty(String repositoryID, String superPropertyURI,
          String subPropertyURI) throws GateOntologyException {
    if(DEBUG) print("removeSubProperty");
    loadRepositoryDetails(repositoryID);
    removeUUUStatement(subPropertyURI, RDFS.SUBPROPERTYOF, superPropertyURI);
  }

  /**
   * for the given property, the method returns all its super properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSuperProperties(String repositoryID,
          String aPropertyURI, boolean direct) throws GateOntologyException {
    if(DEBUG) print("getSuperProperties");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct)
      iter = sail.getDirectSubPropertyOf(getResource(aPropertyURI), null);
    else iter = sail.getSubPropertyOf(getResource(aPropertyURI), null);
    List<Property> list = new ArrayList<Property>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      if(stmt.getObject().toString().equalsIgnoreCase(aPropertyURI)) continue;
      list.add(createPropertyObject(repositoryID, stmt.getObject().toString()));
    }
    return listToPropertyArray(list);
  }

  /**
   * for the given property, the method returns all its sub properties
   * 
   * @param aPropertyURI
   * @param direct
   * @return
   */
  public Property[] getSubProperties(String repositoryID, String aPropertyURI,
          boolean direct) throws GateOntologyException {
    if(DEBUG) print("getSubProperties");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct)
      iter = sail.getDirectSubPropertyOf(null, getResource(aPropertyURI));
    else iter = sail.getSubPropertyOf(null, getResource(aPropertyURI));
    List<Property> list = new ArrayList<Property>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();
      if(stmt.getSubject().toString().equals(aPropertyURI)) continue;
      list
              .add(createPropertyObject(repositoryID, stmt.getSubject()
                      .toString()));
    }
    return listToPropertyArray(list);
  }

  /**
   * for the given property, the method returns all its inverse
   * properties
   * 
   * @param aPropertyURI
   * @return
   */
  public Property[] getInverseProperties(String repositoryID,
          String aPropertyURI) throws GateOntologyException {
    if(DEBUG) print("getInverseProperties");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(aPropertyURI),
            getURI(OWL.INVERSEOF), null);
    List<Property> list = new ArrayList<Property>();
    while(iter.hasNext()) {
      list.add(createPropertyObject(repositoryID, ((Statement)iter.next())
              .getObject().toString()));
    }
    return listToPropertyArray(list);
  }

  /**
   * property1 is set as inverse of property 2
   * 
   * @param property1URI
   * @param property2URI
   */
  public void setInverseOf(String repositoryID, String propertyURI1,
          String propertyURI2) throws GateOntologyException {
    if(DEBUG) print("setInverseOf");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(propertyURI1, OWL.INVERSEOF, propertyURI2);
  }

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
          String individualURI) throws GateOntologyException {
    if(DEBUG) print("addIndividual");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(individualURI, RDF.TYPE, superClassURI);
  }

  /**
   * The method removes the provided instance from the repository.
   * 
   * @param individual
   * @return
   */
  public String[] removeIndividual(String repositoryID, String individualURI)
          throws GateOntologyException {
    if(DEBUG) print("removeIndividual");
    loadRepositoryDetails(repositoryID);
    int no = removeUUUStatement(individualURI, RDF.TYPE, null);
    if(no == 0)
      throw new GateOntologyException(individualURI
              + " is not an explicit Individual");
    // we need to go though all ontology resources of the ontology
    // check if they have property with value the current resource
    // we need to delete it
    List<Property> properties = new ArrayList<Property>();
    properties.addAll(Arrays.asList(getObjectProperties(repositoryID)));
    try {
      startTransaction(repositoryID);
      for(int i = 0; i < properties.size(); i++) {
        sail.removeStatements(null, getURI(properties.get(i).getUri()),
                getResource(individualURI));
      }
      endTransaction(repositoryID);
    }
    catch(SailUpdateException sue) {
      throw new GateOntologyException(sue);
    }
    removeUUUStatement(individualURI, null, null);
    removeUUUStatement(null, null, individualURI);
    removeUUUStatement(null, individualURI, null);
    return new String[] {individualURI};
  }

  /**
   * The method returns all member instances of the provided class. It
   * returns only the direct instances if the boolean parameter direct
   * is set to true.
   * 
   * @param superClassURI
   * @param direct
   */
  public String[] getIndividuals(String repositoryID, String superClassURI,
          byte direct) throws GateOntologyException {
    if(DEBUG) print("getIndividulas");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE)
      iter = sail.getDirectType(null, getResource(superClassURI));
    else iter = sail.getType(null, getResource(superClassURI));
    List<String> list = new ArrayList<String>();
    while(iter.hasNext()) {
      Resource res = iter.next().getSubject();

      String subjectString = res.toString();
      if(hasClass(repositoryID, subjectString)) continue;
      list.add(subjectString);
    }
    return listToArray(list);
  }

  /**
   * returns all resources registered as individuals in the ontology
   * 
   * @return
   */
  public String[] getIndividuals(String repositoryID)
          throws GateOntologyException {
    if(DEBUG) print("getIndividuals");
    loadRepositoryDetails(repositoryID);
    ResourceInfo[] classes = getClasses(repositoryID, false);
    List<String> list = new ArrayList<String>();
    for(int i = 0; i < classes.length; i++) {
      if(hasSystemNameSpace(classes[i].getUri())) continue;
      String[] individuals = getIndividuals(repositoryID, classes[i].getUri(),
              OConstants.DIRECT_CLOSURE);
      for(int m = 0; m < individuals.length; m++) {
        list.add(individuals[m]);
      }
    }
    return listToArray(list);
  }

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
          String individualURI, boolean direct) throws GateOntologyException {
    if(DEBUG) print("hasIndividual");
    loadRepositoryDetails(repositoryID);
    if(direct) {
      return sail.isDirectType(getResource(individualURI),
              getResource(aSuperClassURI));
    }
    else {
      return sail.isType(getResource(individualURI),
              getResource(aSuperClassURI));
    }
  }

  /**
   * For the given individual, the method returns a set of classes for
   * which the individual is registered as instance of
   * 
   * @param individualURI
   */
  public ResourceInfo[] getClassesOfIndividual(String repositoryID,
          String individualURI, byte direct) throws GateOntologyException {
    if(DEBUG) print("getClassesOfIndividual");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = null;
    if(direct == OConstants.DIRECT_CLOSURE) {
      iter = sail.getDirectType(getResource(individualURI), null);
    }
    else {
      iter = sail.getType(getResource(individualURI), null);
    }
    List<Value> list = new ArrayList<Value>();
    while(iter.hasNext()) {
      Statement stmt = iter.next();

      list.add(stmt.getObject());
    }
    return listToResourceInfoArray(list);
  }

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
          String individual1URI, String individual2URI)
          throws GateOntologyException {
    if(DEBUG) print("setDifferentIndividualFrom");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(individual1URI, OWL.DIFFERENTFROM, individual2URI);
  }

  /**
   * for the given individual, the method returns all individuals
   * registered as different from the given individual
   * 
   * @param individualURI
   * @return
   */
  public String[] getDifferentIndividualFrom(String repositoryID,
          String individualURI) throws GateOntologyException {
    if(DEBUG) print("getDifferentIndividualFrom");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(individualURI),
            getURI(OWL.DIFFERENTFROM), null);
    List<String> list = new ArrayList<String>();
    while(iter.hasNext()) {
      list.add(((Statement)iter.next()).getObject().toString());
    }
    return listToArray(list);
  }

  /**
   * individual1 is set as same as the individual2
   * 
   * @param individual1URI
   * @param individual2URI
   */
  public void setSameIndividualAs(String repositoryID, String individual1URI,
          String individual2URI) throws GateOntologyException {
    if(DEBUG) print("setSameIndividualAs");
    loadRepositoryDetails(repositoryID);
    addUUUStatement(individual1URI, OWL.SAMEAS, individual2URI);
  }

  /**
   * for the given individual, the method returns all individuals which
   * are registered as same as the provided individual
   * 
   * @param inidividualURI
   * @return
   */
  public String[] getSameIndividualAs(String repositoryID, String individualURI)
          throws GateOntologyException {
    if(DEBUG) print("getSameIndividualAs");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(individualURI),
            getURI(OWL.SAMEAS), null);
    List<String> list = new ArrayList<String>();
    while(iter.hasNext()) {
      Value res = iter.next().getObject();

      list.add(res.toString());
    }
    return listToArray(list);
  }

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
          throws GateOntologyException {
    if(DEBUG) print("getOnPropertyValue");
    loadRepositoryDetails(repositoryId);
    StatementIterator iter = sail.getStatements(getResource(restrictionURI),
            getURI(OWL.ONPROPERTY), null);
    if(iter.hasNext()) {
      // here we need to check which type of property it is
      return createPropertyObject(repositoryId, iter.next().getObject()
              .toString());
    }
    return null;
  }

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
          String propertyURI) throws GateOntologyException {

    if(DEBUG) print("setOnPropertyValue");
    loadRepositoryDetails(repositoryId);
    addUUUStatement(restrictionURI, OWL.ONPROPERTY, propertyURI);
  }

  /**
   * Gets the datatype uri specified on the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @return
   * @throws GateOntologyException
   */
  public PropertyValue getPropertyValue(String repositoryId,
          String restrictionURI, byte restrictionType)
          throws GateOntologyException {

    if(DEBUG) print("getDataType");
    loadRepositoryDetails(repositoryId);
    String whatValueURI = null;
    switch(restrictionType) {
      case OConstants.CARDINALITY_RESTRICTION:
        whatValueURI = OWL.CARDINALITY;
        break;
      case OConstants.MAX_CARDINALITY_RESTRICTION:
        whatValueURI = OWL.MAXCARDINALITY;
        break;
      case OConstants.MIN_CARDINALITY_RESTRICTION:
        whatValueURI = OWL.MINCARDINALITY;
        break;
      default:
        throw new GateOntologyException("Invalid restriction type");
    }

    StatementIterator iter = sail.getStatements(getResource(restrictionURI),
            getURI(whatValueURI), null);
    if(iter.hasNext()) {
      Value v = iter.next().getObject();
      if(v instanceof Literal) {
        return new PropertyValue(((Literal)v).getDatatype().toString(),
                ((Literal)v).getLabel());
      }
    }
    return null;
  }

  /**
   * Sets the datatype uri for the given restriction uri.
   * 
   * @param repositoryID
   * @param restrictionURI
   * @param datatypeURI
   * @return
   * @throws GateOntologyException
   */
  public void setPropertyValue(String repositoryId, String restrictionURI,
          byte restrictionType, String value, String datatypeURI)
          throws GateOntologyException {
    if(DEBUG) print("getDataType");
    loadRepositoryDetails(repositoryId);
    String whatValueURI = null;
    switch(restrictionType) {
      case OConstants.CARDINALITY_RESTRICTION:
        whatValueURI = OWL.CARDINALITY;
        break;
      case OConstants.MAX_CARDINALITY_RESTRICTION:
        whatValueURI = OWL.MAXCARDINALITY;
        break;
      case OConstants.MIN_CARDINALITY_RESTRICTION:
        whatValueURI = OWL.MINCARDINALITY;
        break;
      default:
        throw new GateOntologyException("Invalid restriction type");
    }

    StatementIterator iter = sail.getStatements(getResource(restrictionURI),
            getURI(whatValueURI), null);
    Statement toDelete = null;
    if(iter.hasNext()) {
      Statement stmt = iter.next();
      Value v = stmt.getObject();
      if(v instanceof Literal) {
        if(((Literal)v).getDatatype().toString().equals(datatypeURI)) {
          toDelete = stmt;
        }
      }
    }

    if(toDelete != null) {
      Literal l = (Literal)toDelete.getObject();
      removeUUDStatement(restrictionURI, whatValueURI, l.getLabel(), l
              .getDatatype().toString());
    }
    addUUDStatement(restrictionURI, whatValueURI, value, datatypeURI);
  }

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
  public ResourceInfo getRestrictionValue(String repositoryId,
          String restrictionURI, byte restrictionType)
          throws GateOntologyException {
    if(DEBUG) print("getRestrictionValue");
    loadRepositoryDetails(repositoryId);

    String whatValueURI = null;
    switch(restrictionType) {
      case OConstants.ALL_VALUES_FROM_RESTRICTION:
        whatValueURI = OWL.ALLVALUESFROM;
        break;
      case OConstants.HAS_VALUE_RESTRICTION:
        whatValueURI = OWL.HASVALUE;
        break;
      case OConstants.SOME_VALUES_FROM_RESTRICTION:
        whatValueURI = OWL.SOMEVALUESFROM;
        break;
      default:
        throw new GateOntologyException("Invalid restriction type");
    }

    StatementIterator iter = sail.getStatements(getResource(restrictionURI),
            getURI(whatValueURI), null);
    if(iter.hasNext()) {
      String resourceURI = iter.next().getObject().toString();
      Resource res = getResource(resourceURI);
      boolean isRestriction = sail.hasStatement(res, getURI(RDF.TYPE),
              getResource(OWL.RESTRICTION));
      byte classType = OConstants.OWL_CLASS;
      if(isRestriction) {
        if(sail.hasStatement(res, getURI(OWL.HASVALUE), null)) {
          classType = OConstants.HAS_VALUE_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.SOMEVALUESFROM), null)) {
          classType = OConstants.SOME_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.ALLVALUESFROM), null)) {
          classType = OConstants.ALL_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.CARDINALITY), null)) {
          classType = OConstants.CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MINCARDINALITY), null)) {
          classType = OConstants.MIN_CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MAXCARDINALITY), null)) {
          classType = OConstants.MAX_CARDINALITY_RESTRICTION;
        }
      }
      return new ResourceInfo(resourceURI, classType);
    }
    return null;
  }

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
  public void setRestrictionValue(String repositoryId, String restrictionURI,
          byte restrictionType, String value) throws GateOntologyException {

    if(DEBUG) print("setRestrictionValue");
    loadRepositoryDetails(repositoryId);

    String whatValueURI = null;
    switch(restrictionType) {
      case OConstants.ALL_VALUES_FROM_RESTRICTION:
        whatValueURI = OWL.ALLVALUESFROM;
        break;
      case OConstants.HAS_VALUE_RESTRICTION:
        whatValueURI = OWL.HASVALUE;
        break;
      case OConstants.SOME_VALUES_FROM_RESTRICTION:
        whatValueURI = OWL.SOMEVALUESFROM;
        break;
      default:
        throw new GateOntologyException("Invalid restriction type");

    }

    StatementIterator iter = sail.getStatements(getResource(restrictionURI),
            getURI(whatValueURI), null);
    Statement toDelete = null;
    if(iter.hasNext()) {
      Statement stmt = iter.next();
      Value v = stmt.getObject();
      toDelete = stmt;
    }

    if(toDelete != null) {
      String objectString = toDelete.getObject().toString();
      removeUUUStatement(restrictionURI, whatValueURI, objectString);
    }
    addUUUStatement(restrictionURI, whatValueURI, value);
  }

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
          throws GateOntologyException {

    loadRepositoryDetails(repositoryID);
    byte classType = -1;
    Resource res = getResource(restrictionURI);
    if(sail.hasStatement(res, getURI(OWL.HASVALUE), null)) {
      classType = OConstants.HAS_VALUE_RESTRICTION;
    }
    else if(sail.hasStatement(res, getURI(OWL.SOMEVALUESFROM), null)) {
      classType = OConstants.SOME_VALUES_FROM_RESTRICTION;
    }
    else if(sail.hasStatement(res, getURI(OWL.ALLVALUESFROM), null)) {
      classType = OConstants.ALL_VALUES_FROM_RESTRICTION;
    }
    else if(sail.hasStatement(res, getURI(OWL.CARDINALITY), null)) {
      classType = OConstants.CARDINALITY_RESTRICTION;
    }
    else if(sail.hasStatement(res, getURI(OWL.MINCARDINALITY), null)) {
      classType = OConstants.MIN_CARDINALITY_RESTRICTION;
    }
    else if(sail.hasStatement(res, getURI(OWL.MAXCARDINALITY), null)) {
      classType = OConstants.MAX_CARDINALITY_RESTRICTION;
    }
    else {
      if(hasClass(repositoryID, restrictionURI)) {
        if(res instanceof BNode) {
          classType = OConstants.ANNONYMOUS_CLASS;
        }
        else {
          classType = OConstants.OWL_CLASS;
        }
      }
    }
    return classType;
  }

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
          String predicateURI, String objectURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    addUUUStatement(subjectURI, predicateURI, objectURI);
  }

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
          String predicateURI, String objectURI) throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    removeUUUStatement(subjectURI, predicateURI, objectURI);
  }

  // ***************************************************************************
  // *********************** Other Utility Methods
  // **************************************************************************
  private void addUUUStatement(String subject, String predicate, String object)
          throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      Resource o = object != null ? getResource(object) : null;
      sail.addStatement(s, p, o);
      endTransaction(null);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  private void addUULStatement(String subject, String predicate, String object,
          String language) throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      Literal o = null;
      if(language == null)
        o = object != null
                ? sail.getValueFactory().createLiteral(object)
                : null;
      else o = object != null ? sail.getValueFactory().createLiteral(object,
              language) : null;
      sail.addStatement(s, p, o);
      endTransaction(null);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  private void addUUDStatement(String subject, String predicate, String object,
          String datatype) throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      URI d = sail.getValueFactory().createURI(datatype);
      Literal l = object != null ? sail.getValueFactory().createLiteral(object,
              d) : null;
      sail.addStatement(s, p, l);
      endTransaction(null);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  private int removeUUUStatement(String subject, String predicate, String object)
          throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      Resource o = object != null ? getResource(object) : null;
      int no = sail.removeStatements(s, p, o);
      endTransaction(null);
      return no;
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  private void removeUULStatement(String subject, String predicate,
          String object, String language) throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      Literal l = null;
      if(language == null) {
        l = object != null
                ? sail.getValueFactory().createLiteral(object)
                : null;
      }
      else {
        l = object != null ? sail.getValueFactory().createLiteral(object,
                language) : null;
      }
      sail.removeStatements(s, p, l);
      endTransaction(null);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  private void removeUUDStatement(String subject, String predicate,
          String object, String datatype) throws GateOntologyException {
    try {
      startTransaction(null);
      Resource s = subject != null ? getResource(subject) : null;
      URI p = predicate != null
              ? sail.getValueFactory().createURI(predicate)
              : null;
      URI d = sail.getValueFactory().createURI(datatype);
      Literal l = object != null
              ? sail.getValueFactory().createLiteral(object)
              : null;

      sail.removeStatements(s, p, l);

      l = object != null
              ? sail.getValueFactory().createLiteral(object, d)
              : null;
      sail.removeStatements(s, p, l);
      endTransaction(null);
    }
    catch(SailUpdateException e) {
      throw new GateOntologyException(e);
    }
  }

  public void startTransaction(String repositoryID)
          throws GateOntologyException {
    if(repositoryID != null) {
      loadRepositoryDetails(repositoryID);
    }

    if(!sail.transactionStarted()) sail.startTransaction();
  }

  public void endTransaction(String repositoryID) throws GateOntologyException {
    if(repositoryID != null) {
      loadRepositoryDetails(repositoryID);
    }
    if(sail.transactionStarted()) sail.commitTransaction();
  }

  public boolean transactionStarted(String repositoryID)
          throws GateOntologyException {
    if(repositoryID != null) {
      loadRepositoryDetails(repositoryID);
    }
    return sail.transactionStarted();
  }

  public void commitTransaction(String repositoryID)
          throws GateOntologyException {
    if(repositoryID != null) {
      loadRepositoryDetails(repositoryID);
    }

    if(sail != null && sail.transactionStarted()) {
      // we need to commit all changes
      sail.commitTransaction();
    }
  }

  private Property[] listToPropertyArray(List<Property> list) {
    if(list == null) return null;
    ArrayList<Property> subList = new ArrayList<Property>();
    for(int i = 0; i < list.size(); i++) {
      if(hasSystemNameSpace(list.get(i).getUri())) continue;
      subList.add(list.get(i));
    }
    Property[] props = new Property[subList.size()];
    for(int i = 0; i < subList.size(); i++) {
      props[i] = subList.get(i);
    }
    return props;
  }

  private PropertyValue[] listToPropertyValueArray(List<PropertyValue> subList) {
    if(subList == null) return null;
    PropertyValue[] props = new PropertyValue[subList.size()];
    for(int i = 0; i < subList.size(); i++) {
      props[i] = subList.get(i);
    }
    return props;
  }

  private ResourceInfo[] listToResourceInfoArray(List<Value> list) {
    if(list == null) return null;
    ArrayList<ResourceInfo> subList = new ArrayList<ResourceInfo>();
    for(int i = 0; i < list.size(); i++) {
      if(list.get(i) instanceof BNode
              && list.get(i).toString().startsWith("FictiveNode_")) continue;
      if(subList.contains(list.get(i))) continue;
      if(hasSystemNameSpace(list.get(i).toString())) continue;
      String resourceURI = list.get(i).toString();
      Resource res = getResource(resourceURI);
      boolean isRestriction = sail.hasStatement(res, getURI(RDF.TYPE),
              getResource(OWL.RESTRICTION));
      byte classType = OConstants.OWL_CLASS;
      if(isRestriction) {
        if(sail.hasStatement(res, getURI(OWL.HASVALUE), null)) {
          classType = OConstants.HAS_VALUE_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.SOMEVALUESFROM), null)) {
          classType = OConstants.SOME_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.ALLVALUESFROM), null)) {
          classType = OConstants.ALL_VALUES_FROM_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.CARDINALITY), null)) {
          classType = OConstants.CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MINCARDINALITY), null)) {
          classType = OConstants.MIN_CARDINALITY_RESTRICTION;
        }
        else if(sail.hasStatement(res, getURI(OWL.MAXCARDINALITY), null)) {
          classType = OConstants.MAX_CARDINALITY_RESTRICTION;
        }
      }
      else if(res instanceof BNode) {
        classType = OConstants.ANNONYMOUS_CLASS;
      }
      subList.add(new ResourceInfo(list.get(i).toString(), classType));
    }

    ResourceInfo[] strings = new ResourceInfo[subList.size()];
    for(int i = 0; i < subList.size(); i++) {
      strings[i] = subList.get(i);
    }
    return strings;
  }

  /**
   * This method tells whether the resource is imported or added as an
   * explicit statement.
   * 
   * @param repositoryID
   * @param resourceURI
   * @return
   */
  public boolean isImplicitResource(String repositoryID, String resourceURI)
          throws GateOntologyException {
    if(repositoryID != null) {
      loadRepositoryDetails(repositoryID);
    }
    return !sail.hasExplicitStatement(getResource(resourceURI),
            getURI(RDF.TYPE), null);
  }

  private String[] listToArray(List<String> list) {
    if(list == null) return null;
    ArrayList<String> subList = new ArrayList<String>();
    for(int i = 0; i < list.size(); i++) {
      if(subList.contains(list.get(i))) continue;
      if(hasSystemNameSpace(list.get(i))) continue;
      subList.add(list.get(i));
    }
    String[] strings = new String[subList.size()];
    for(int i = 0; i < subList.size(); i++) {
      strings[i] = subList.get(i);
    }
    return strings;
  }

  private URI getURI(String string) {
    return sail.getValueFactory().createURI(string);
  }

  private Resource getResource(String string) {
    try {
      return sail.getValueFactory().createURI(string);
    }
    catch(Exception e) {
      return sail.getValueFactory().createBNode(string);
    }
  }

  private int getUserID(String username, String password) {
    List userInfos = service.getSystemConfig().getUserInfoList();
    for(int i = 0; i < userInfos.size(); i++) {
      UserInfo userInfo = (UserInfo)userInfos.get(i);
      if(userInfo.getLogin().equals(username)
              && userInfo.getPassword().equals(password)) {
        return userInfo.getID();
      }
    }
    return -1;
  }

  private int createUser(String username, String password)
          throws GateOntologyException {
    int counter = service.getSystemConfig().getUnusedUserId();
    service.getSystemConfig().addUser(counter, username, username, password);
    return counter;
  }

  private void saveConfiguration() throws GateOntologyException {
    try {
      if(systemConf == null) return;
      if(DEBUG) System.out.println("System conf : " + systemConf);
      Writer writer = new BufferedWriter(new FileWriter(new File(systemConf
              .toURI())));
      SystemConfigFileHandler.writeConfiguration(service.getSystemConfig(),
              writer);
      writer.close();
    }
    catch(IOException e) {
      throw new GateOntologyException(e);
    }
    catch(URISyntaxException use) {
      throw new GateOntologyException(use);
    }
  }

  private SystemConfig readConfiguration() throws IOException {
    if(systemConf == null) {
      String s = "<?xml version='1.0'?>"
              + "<system-conf>"
              + "<admin password=''/>"
              + "<log dir='plugins/Ontology_Tools/logs' level='3'/>"
              + "<tmp dir='plugins/Ontology_Tools/tmp'/>"
              + "<rmi-factory enabled='false' class='com.ontotext.util.rmi.CustomRMIFactory' port='1099'/>"
              + "<userlist>"
              + "<user id='1' login='admin'><fullname>Admin</fullname><password>admin</password></user>"
              + "<user id='3' login='guest'><fullname>Guest</fullname><password>guest</password></user>"
              + "</userlist>"
              + "<repositorylist></repositorylist></system-conf>";
      Reader reader = new StringReader(s);
      SystemConfig config = SystemConfigFileHandler.readConfiguration(reader);
      reader.close();
      return config;
    }
    else {
      Reader reader = new BufferedReader(new InputStreamReader(systemConf
              .openStream()));
      SystemConfig config = SystemConfigFileHandler.readConfiguration(reader);
      reader.close();
      return config;
    }
  }

  /**
   * given a string, return the equivalent RDFFormat
   * 
   * @param format
   * @return
   */
  private RDFFormat getRDFFormat(byte format) {
    switch((int)format) {
      case OConstants.ONTOLOGY_FORMAT_N3:
        return RDFFormat.N3;
      case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
        return RDFFormat.NTRIPLES;
      case OConstants.ONTOLOGY_FORMAT_RDFXML:
        return RDFFormat.RDFXML;
      default:
        return RDFFormat.TURTLE;
    }
  }

  private Property createPropertyObject(String repositoryID, String uri)
          throws GateOntologyException {
    byte type = OConstants.ANNOTATION_PROPERTY;
    if(isObjectProperty(repositoryID, uri)) {
      type = OConstants.OBJECT_PROPERTY;
    }
    else if(isDatatypeProperty(repositoryID, uri)) {
      type = OConstants.DATATYPE_PROPERTY;
    }
    else if(isTransitiveProperty(repositoryID, uri)) {
      type = OConstants.TRANSITIVE_PROPERTY;
    }
    else if(isSymmetricProperty(repositoryID, uri)) {
      type = OConstants.SYMMETRIC_PROPERTY;
    }
    else if(sail.isProperty(getResource(uri))) {
      type = OConstants.RDF_PROPERTY;
    }
    else {
      return null;
    }
    return new Property(type, uri);
  }

  private void print(String methodName) {
    System.out.println(methodName + " called.");
  }

  /**
   * Set the provided repository as a current repository
   * 
   * @param repositoryID
   * @param ontoFileUrl
   * @param baseURI
   * @param format
   * @param persist
   * @return
   */
  private boolean setRepository(String repositoryID, String ontoFileUrl,
          boolean isOntologyData, String baseURI, byte format,
          String absolutePersistLocation, boolean persist) {
    // check if repository exists
    if(DEBUG) print("setRepository");
    boolean found = false;
    try {
      setCurrentRepositoryID(repositoryID);
      RepositoryConfig repConfig = service.getSystemConfig()
              .getRepositoryConfig(repositoryID);
      if(repConfig != null) {
        // lets find out the new import values those have come through
        // the new
        // ontoFileUrl
        Set<String> importValues = getImportValues(repositoryID, ontoFileUrl,
                baseURI, format, absolutePersistLocation, isOntologyData,
                new HashSet<String>());
        SailConfig syncSail = repConfig.getSail(OWLIM_SCHEMA_REPOSITORY_CLASS);
        if(syncSail != null) {
          String formatToUse = "ntriples";
          switch(format) {
            case OConstants.ONTOLOGY_FORMAT_N3:
              formatToUse = "n3";
              break;
            case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
              formatToUse = "ntriples";
              break;
            case OConstants.ONTOLOGY_FORMAT_TURTLE:
              formatToUse = "turtle";
              break;
            default:
              formatToUse = "rdfxml";
              break;
          }
          Map map = syncSail.getConfigParameters();
          if(map == null) map = new HashMap();
          map.put("noPersist", Boolean.toString(!persist));
          map.put("compressFile", "no");
          map.put("dataFormat", formatToUse);
          String imports = (String)map.get("imports");
          String defaultNS = (String)map.get("defaultNS");
          if(imports == null) imports = "";
          if(defaultNS == null) defaultNS = "";
          if(imports.length() > 0) {
            for(String imValue : importValues) {
              imports += ";" + imValue;
              defaultNS += ";" + imValue + "#";
            }
          }
          else {
            imports = owlRDFS.toExternalForm();
            defaultNS = "http://www.w3.org/2002/07/owl#";
          }
          map.put("imports", imports);
          map.put("defaultNS", defaultNS);
          String persistFile = absolutePersistLocation == null
                  || absolutePersistLocation.trim().length() == 0 ? new File(
                  repositoryID + ".nt").getAbsolutePath() : new File(new File(
                  absolutePersistLocation), repositoryID + ".nt")
                  .getAbsolutePath();
          String tempTripplesFile = new File(new File(absolutePersistLocation),
                  repositoryID + "-tripples.nt").getAbsolutePath();
          map.put("file", persistFile);
          map.put("new-triples-file", tempTripplesFile);
          map.put("auto-write-time-minutes", "0");
          syncSail.setConfigParameters(map);
        }
      }
      if(ontoFileUrl != null && ontoFileUrl.trim().length() != 0) {
        currentRepository.addData(ontoFileUrl, baseURI, getRDFFormat(format),
                true, this);
        if(persist) saveConfiguration();
        if(DEBUG) System.out.println("Data added!");
      }
      found = true;
    }
    catch(AccessDeniedException exception) {
      // repository doesn't exist
      // lets create one
      if(DEBUG) exception.printStackTrace();
      found = false;
    }
    catch(IOException ioe) {
      if(DEBUG) ioe.printStackTrace();
      found = false;
    }
    catch(GateOntologyException goe) {
      if(DEBUG) goe.printStackTrace();
      found = false;
    }
    return found;
  }

  private HashSet<String> getImportValues(String currentRepository,
          String ontoFileUrl, String baseURI, byte format,
          String absolutePersistLocation, boolean isOntologyData,
          Set<String> parsedValues) {
    System.out.println("Importing : " + ontoFileUrl);
    String baseUrl = ontoFileUrl;
    ArrayList<String> toReturn = new ArrayList<String>();
    try {
      String dummyRepository = "dummy" + Math.random();
      createNewRepository(dummyRepository, ontoFileUrl, isOntologyData,
              baseURI, false, absolutePersistLocation, "admin", "admin",
              format, true);
      setCurrentRepositoryID(dummyRepository);
      if(ontoFileUrl != null && ontoFileUrl.trim().length() != 0) {
        if(isOntologyData) {
          this.currentRepository.addData(ontoFileUrl, baseURI,
                  getRDFFormat(format), true, this);
        }
        else if(ontoFileUrl.startsWith("file:")) {
          this.currentRepository.addData(new File(new URL(ontoFileUrl)
                  .getFile()), baseURI, getRDFFormat(format), true, this);
        }
        else {
          this.currentRepository.addData(new URL(ontoFileUrl), baseURI,
                  getRDFFormat(format), true, this);
        }
        StatementIterator si = sail.getStatements(null, getURI(OWL.IMPORTS),
                null);
        si = sail.getStatements(null, getURI(OWL.IMPORTS), null);
        if(si.hasNext()) {
          ontoFileUrl = si.next().getSubject().toString();
          this.ontologyUrl = ontoFileUrl;
        }
        else {
          ontoFileUrl = null;
        }
        if(ontoFileUrl != null) {
          PropertyValue[] values = getPropertyValues(dummyRepository,
                  ontoFileUrl, OWL.IMPORTS);

          for(int i = 0; i < values.length; i++) {
            String fileName = values[i].getValue();
            if(fileName
                    .equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema")
                    || fileName
                            .equalsIgnoreCase("www.w3.org/2000/01/rdf-schema")) {
              fileName = rdfSchema.toExternalForm();
            }
            // here we check what user has provided is a valid URL or
            // a relative path
            try {
              new URL(fileName).openStream();
              if(parsedValues.contains(fileName)) continue;
              if(DEBUG) System.out.println("\t URL To Be Imported : " + fileName);
              toReturn.add(fileName);
              continue;
            }
            catch(MalformedURLException e) {
            }
            catch(IOException ioe) {
            }

            // url is not valid, lets try to find out the file on local
            // system
            int m = 0;
            boolean allFound = true;
            int lastIndexOfSlash = -1;
            for(; m < fileName.length() && m < ontoFileUrl.length(); m++) {
              if(fileName.charAt(m) != ontoFileUrl.charAt(m)) {
                allFound = false;
                break;
              }
            }

            if(!allFound) {
              if(m < 1) {
                throw new GateOntologyException("Invalid Import URL" + fileName);
              }
              
              // exception
              // lets find out the index of the last /
              String tempFileName = fileName.substring(0, m);
              m = tempFileName.lastIndexOf('/') + 1;
              if(fileName.charAt(m - 1) == '/') {
                int counter = 1;
                for(int k = m; k < ontoFileUrl.length(); k++) {
                  if(ontoFileUrl.charAt(k) == '/') {
                    counter++;
                  }
                }

                String tempString = baseUrl;
                for(int k = 0; k < counter; k++) {
                  int tempIndex = tempString.lastIndexOf('/');
                  if(tempIndex != -1) {
                    tempString = tempString.substring(0, tempIndex);
                  }
                  else {
                    throw new GateOntologyException("Invalid Import URL"
                            + ontoFileUrl);
                  }
                }

                
                fileName = tempString
                        + fileName.substring(m - 1, fileName.length());
                
                fileName = new File(new URL(fileName).getFile()).getAbsolutePath();
                // lets normalize the name by replacing .. in the path
                //file://abc/xyz/../../pqr
                //13 16
                while(true) {
                  int index = fileName.indexOf("/../");
                  if(index == -1) break;
                  tempString = fileName.substring(0, index);
                  int index1 = tempString.lastIndexOf("/");
                  if(index1 == -1) {
                    throw new GateOntologyException("Invalid Import URL"
                            + ontoFileUrl);
                  }
                  tempString = tempString.substring(0, index1);
                  fileName = tempString + fileName.substring(index + 3, fileName.length());
                }
                fileName = new File(fileName).toURI().toURL().toExternalForm();
                if(parsedValues.contains(fileName)) continue;
                
                toReturn.add(fileName);
                if(DEBUG) System.out.println("\t File To Be Imported : " + fileName);
              }
              else {
                throw new GateOntologyException("Invalid Import URL" + fileName);
              }
            }
          }
        }
      }
      removeRepository(dummyRepository, false);
      if(currentRepository != null) loadRepositoryDetails(currentRepository);
    }
    catch(AccessDeniedException e) {
      if(DEBUG) e.printStackTrace();
    }
    catch(IOException ioe) {
      if(DEBUG) ioe.printStackTrace();
    }

    HashSet<String> finallyToReturn = new HashSet<String>();
    finallyToReturn.addAll(toReturn);
    parsedValues.addAll(toReturn);
    isOntologyData = false;
    for(String value : toReturn) {
      finallyToReturn.addAll(getImportValues(currentRepository, value, baseURI,
              OConstants.ONTOLOGY_FORMAT_RDFXML, absolutePersistLocation,
              isOntologyData, parsedValues));
    }

    return finallyToReturn;
  }

  /**
   * Creates a new user
   * 
   * @param username
   * @param password
   * @return
   * @throws GateOntologyException
   */
  private int createNewUser(String username, String password)
          throws GateOntologyException {
    if(DEBUG) print("createUser");
    int id = -1;
    if(username != null) {
      id = getUserID(username, password);
      // user does not exist create a new user
      if(id == -1) {
        id = createUser(username, password);
      }
    }
    return id;
  }

  /**
   * Creates a new Repository
   * 
   * @param repositoryID
   * @param baseURI
   * @param persist
   * @param username
   * @param password
   * @return
   * @throws GateOntologyException
   */
  private RepositoryConfig createNewRepository(String repositoryID,
          String ontoFileUrl, boolean isOntologyData, String baseURI,
          boolean persist, String absolutePersistLocation, String username,
          String password, byte format, boolean isDummyRepository)
          throws GateOntologyException {
    try {
      // we create a new repository
      RepositoryConfig repConfig = new RepositoryConfig(repositoryID);
      repConfig.setTitle(repositoryID);
      SailConfig syncSail = new SailConfig(OWLIM_SCHEMA_REPOSITORY_CLASS);
      Map map = syncSail.getConfigParameters();
      if(map == null) {
        map = new HashMap();
      }
      String formatToUse = "ntriples";
      switch(format) {
        case OConstants.ONTOLOGY_FORMAT_N3:
          formatToUse = "n3";
          break;
        case OConstants.ONTOLOGY_FORMAT_NTRIPLES:
          formatToUse = "ntriples";
          break;
        case OConstants.ONTOLOGY_FORMAT_TURTLE:
          formatToUse = "turtle";
          break;
        default:
          formatToUse = "rdfxml";
          break;
      }
      String imports = owlRDFS.toExternalForm();
      String defaultNS = "http://www.w3.org/2002/07/owl#";
      if(!isDummyRepository) {
        Set<String> importValues = getImportValues(null, ontoFileUrl, baseURI,
                format, absolutePersistLocation, isOntologyData,
                new HashSet<String>());
        for(String imValue : importValues) {
          imports += ";" + imValue;
          defaultNS += ";" + imValue + "#";
//                  + (imValue.startsWith("http:")
//                          ? imValue
//                          : "http://www.gate.ac.uk/owlim") + "#";
        }
      }
      map.put("imports", imports);
      map.put("defaultNS", defaultNS);
      map.put("ruleset", "owl-max");
      map.put("partialRDFS", "true");
      map.put("dropOnRemove", "false");
      map.put("base-URL", baseURI);
      map.put("indexSize", "100000");
      map.put("stackSafe", "true");
      map.put("noPersist", Boolean.toString(!persist));
      map.put("compressFile", "no");
      map.put("dataFormat", formatToUse);
      String persistFile = absolutePersistLocation == null
              || absolutePersistLocation.trim().length() == 0 ? new File(
              repositoryID + ".nt").getAbsolutePath() : new File(new File(
              absolutePersistLocation), repositoryID + ".nt").getAbsolutePath();
      String tempTripplesFile = new File(new File(absolutePersistLocation),
              repositoryID + "tripples.nt").getAbsolutePath();
      map.put("file", persistFile);
      map.put("new-triples-file", tempTripplesFile);
      map.put("auto-write-time-minutes", "0");
      syncSail.setConfigParameters(map);
      repConfig.addSail(syncSail);
      repConfig.setWorldReadable(true);
      repConfig.setWorldWriteable(true);
      service.createRepository(repConfig);
      setCurrentRepositoryID(repositoryID);
      if(username != null) {
        service.getSystemConfig().setReadAccess(repositoryID, username, true);
        service.getSystemConfig().setWriteAccess(repositoryID, username, true);
      }
      return repConfig;
    }
    catch(ConfigurationException ce) {
      throw new GateOntologyException(ce);
    }
  }

  /**
   * Adds ontology Data
   * 
   * @param ontoFileUrl
   * @param baseURI
   * @param format
   * @throws GateOntologyException
   */
  private void addOntologyData(String repositoryID, String ontoFileUrl,
          boolean isOntologyData, String baseURI, byte format)
          throws GateOntologyException {
    try {
      if(ontoFileUrl != null && ontoFileUrl.trim().length() != 0) {
        boolean findURL = false;
        if(isOntologyData) {
          currentRepository.addData(ontoFileUrl, baseURI, getRDFFormat(format),
                  true, this);

          findURL = true;
        }
        else if(ontoFileUrl.startsWith("file:")) {
          currentRepository.addData(new File(new URL(ontoFileUrl).getFile()),
                  baseURI, getRDFFormat(format), true, this);
          findURL = true;
        }
        else {
          currentRepository.addData(new URL(ontoFileUrl), baseURI,
                  getRDFFormat(format), true, this);
        }
        StatementIterator si = sail.getStatements(null, getURI(OWL.IMPORTS),
                null);
        if(si.hasNext()) {
          ontoFileUrl = si.next().getSubject().toString();
          this.ontologyUrl = ontoFileUrl;
        }
        else {
          ontoFileUrl = null;
        }
        if(DEBUG) System.out.println("Data added!");
      }
    }
    catch(IOException e) {
      throw new GateOntologyException(e);
    }
    catch(AccessDeniedException ade) {
      throw new GateOntologyException(ade);
    }
  }

  /**
   * This method is used to obtain the most specific classes
   * 
   * @param repositoryID
   * @param values
   * @return
   * @throws GateOntologyException
   */
  private ResourceInfo[] reduceToMostSpecificClasses(String repositoryID,
          List<ResourceInfo> values) throws GateOntologyException {
    if(values == null || values.isEmpty()) return new ResourceInfo[0];
    List<String> classes = new ArrayList<String>();
    for(int i = 0; i < values.size(); i++) {
      classes.add(values.get(i).getUri());
    }
    outer: for(int i = 0; i < classes.size(); i++) {
      String c = classes.get(i);
      // if the class's children appear in list, it is not the most
      // specific class
      ResourceInfo[] sc = getSubClasses(repositoryID, c,
              OConstants.TRANSITIVE_CLOSURE);
      for(int j = 0; j < sc.length; j++) {
        if(classes.contains(sc[j].getUri())) {
          classes.remove(i);
          values.remove(i);
          i--;
          continue outer;
        }
      }
    }
    ResourceInfo[] toReturn = new ResourceInfo[classes.size()];
    for(int i = 0; i < classes.size(); i++) {
      toReturn[i] = values.get(i);
    }
    return toReturn;
  }

  private void loadRepositoryDetails(String repositoryID)
          throws GateOntologyException {
    RepositoryDetails rd = mapToRepositoryDetails.get(repositoryID);
    if(rd == null) {
      throw new GateOntologyException("Repository :" + repositoryID
              + " does not exist");
    }
    currentRepository = rd.repository;
    sail = rd.sail;
    ontologyUrl = rd.ontologyUrl;
    returnSystemStatements = rd.returnSystemStatements;
  }

  private byte getPropertyType(String repositoryID, String aPropertyURI)
          throws GateOntologyException {
    if(isDatatypeProperty(repositoryID, aPropertyURI))
      return OConstants.DATATYPE_PROPERTY;
    else if(isTransitiveProperty(repositoryID, aPropertyURI))
      return OConstants.TRANSITIVE_PROPERTY;
    else if(isSymmetricProperty(repositoryID, aPropertyURI))
      return OConstants.SYMMETRIC_PROPERTY;
    else if(isObjectProperty(repositoryID, aPropertyURI))
      return OConstants.OBJECT_PROPERTY;
    else if(isAnnotationProperty(repositoryID, aPropertyURI))
      return OConstants.ANNOTATION_PROPERTY;
    else return OConstants.RDF_PROPERTY;
  }

  private PropertyValue[] getPropertyValues(String repositoryID,
          String aResourceURI, String aPropertyURI)
          throws GateOntologyException {
    if(DEBUG) print("getPropertyValues");
    loadRepositoryDetails(repositoryID);
    StatementIterator iter = sail.getStatements(getResource(aResourceURI),
            getURI(aPropertyURI), null);
    List<PropertyValue> list = new ArrayList<PropertyValue>();
    while(iter.hasNext()) {
      Statement stmt = (Statement)iter.next();

      PropertyValue prop = new PropertyValue(String.class.getName(), stmt
              .getObject().toString());
      list.add(prop);
    }
    return listToPropertyValueArray(list);
  }

  public boolean hasSystemNameSpace(String uri) {

    if(returnSystemStatements) return false;
    if(uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Thing"))
      return false;
    return Utils.hasSystemNameSpace(uri);
  }

  class RepositoryDetails {
    /**
     * OWLIMSchemaRepository is used as an interaction layer on top of
     * Sesame server. The class provides various methods of manipulating
     * ontology data.
     */
    OWLIMSchemaRepository sail;

    /**
     * The reference of currently selected repository is stored in this
     * variable
     */
    SesameRepository repository;

    /**
     * Ontology URL
     */
    private String ontologyUrl;

    /**
     * Whether to return system statements
     */
    private boolean returnSystemStatements;
  }

  public SesameRepository getSesameRepository(String repositoryID)
          throws GateOntologyException {
    loadRepositoryDetails(repositoryID);
    return currentRepository;
  }
}
