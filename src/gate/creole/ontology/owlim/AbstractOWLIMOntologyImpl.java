/*
 *  AbstractOWLIMOntologyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AbstractOWLIMOntologyImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.sesame.repository.SesameRepository;
import gate.creole.AbstractLanguageResource;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.DataType;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.Literal;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyModificationListener;
import gate.creole.ontology.OntologyUtilities;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.SymmetricProperty;
import gate.creole.ontology.TransitiveProperty;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * This class provides implementation of most of the methods of Ontology
 * interface. This implementation is based on the OWLIM (a SAIL) that
 * stores data in repository using SESAME.
 * 
 * @author niraj
 * 
 */
public abstract class AbstractOWLIMOntologyImpl
                                               extends
                                                 AbstractLanguageResource
                                                                         implements
                                                                         Ontology {
  /**
   * ID of the repository
   */
  protected String sesameRepositoryID;

  /**
   * instance of the OWLIM
   */
  protected OWLIM owlim;

  /**
   * URL of the ontology
   */
  protected URL ontologyURL;

  /**
   * Ontology Format
   */
  protected byte format;

  /**
   * Default Namespace
   */
  protected String defaultNameSpace;

  /**
   * Parameter that keeps track of if the ontology is modified
   */
  protected boolean isModified;

  /**
   * A List of ontology modification listeners
   */
  protected transient List<OntologyModificationListener> modificationListeners;

  /**
   * Indicates whether the data in repository should be persisted
   */
  protected Boolean persistRepository;

  /**
   * A Map that keeps record of resources given their uris
   */
  protected Map<String, OResource> urisToOResouceMap;

  /**
   * Map where the key is a resource name and value is a list of
   * resources with that name.
   */
  protected Map<String, List<OResource>> resourceNamesToOResourcesMap;

  /**
   * Constructor
   */
  public AbstractOWLIMOntologyImpl() {
    urisToOResouceMap = new HashMap<String, OResource>();
    resourceNamesToOResourcesMap = new HashMap<String, List<OResource>>();
    persistRepository = new Boolean(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#cleanOntology()
   */
  public void cleanOntology() {
    try {
      owlim.cleanOntology(sesameRepositoryID);
      urisToOResouceMap.clear();
      resourceNamesToOResourcesMap.clear();
      if(!callFromCleanup)
        fireOntologyReset();
      else callFromCleanup = false;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOntologyData(byte)
   */
  public String getOntologyData(byte format) {
    try {
      return owlim.getOntologyData(sesameRepositoryID, format);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#writeOntologyData(java.io.OutputStream,
   *      byte)
   */
  public abstract void writeOntologyData(OutputStream out, byte format);

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#writeOntologyData(java.io.Writer,
   *      byte)
   */
  public abstract void writeOntologyData(Writer out, byte format);

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getURL()
   */
  public URL getURL() {
    return ontologyURL;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#setURL(java.net.URL)
   */
  public void setURL(URL aUrl) {
    this.ontologyURL = aUrl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#setDefaultNameSpace(gate.creole.ontology.URI)
   */
  public void setDefaultNameSpace(String theURI) {
    defaultNameSpace = theURI;
    if(defaultNameSpace != null && -1 == defaultNameSpace.indexOf('#')) {
      defaultNameSpace = defaultNameSpace + '#';
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getDefaultNameSpace()
   */
  public String getDefaultNameSpace() {
    try {
      return this.defaultNameSpace;
      // return owlim.getDefaultNameSpace(this.sesameRepositoryID);
    }
    catch(Exception e) {
      throw new GateRuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#setVersion(java.lang.String)
   */
  public void setVersion(String theVersion) {
    try {
      owlim.setVersion(sesameRepositoryID, theVersion);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getVersion()
   */
  public String getVersion() {
    try {
      return owlim.getVersion(sesameRepositoryID);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addOClass(gate.creole.ontology.URI)
   */
  public OClass addOClass(URI aURI) {
    try {
      OResource resource = getOResourceFromMap(aURI.toString());
      if(resource != null) {
        return (OClass)resource;
      }
      owlim.addClass(this.sesameRepositoryID, aURI.toString());
      OClass oClass = Utils.createOClass(this.sesameRepositoryID, this, owlim,
              aURI.toString(), aURI.isAnonymousResource());

      fireOntologyResourceAdded(oClass);

      // we need to add a label on this but only after the new class
      // addition event has been fired
      oClass.setLabel(aURI.getResourceName(), null);

      return oClass;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOClass(gate.creole.ontology.URI)
   */
  public OClass getOClass(URI theClassURI) {
    try {
      OResource resource = getOResourceFromMap(theClassURI.toString());
      if(resource != null) {
        return (OClass)resource;
      }
      if(owlim.hasClass(this.sesameRepositoryID, theClassURI.toString())) {
        return Utils.createOClass(this.sesameRepositoryID, this, owlim,
                theClassURI.toString(), theClassURI.isAnonymousResource());
      }
      return null;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeOClass(gate.creole.ontology.OClass)
   */
  public void removeOClass(OClass theClass) {
    try {

      if(!containsOClass(theClass.getURI())) {
        Utils.warning(theClass.getURI().toString() + " does not exist");
        return;
      }

      String[] deletedResources = owlim.removeClass(this.sesameRepositoryID,
              theClass.getURI().toString());
      fireOntologyResourcesRemoved(deletedResources);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#containsOClass(gate.creole.ontology.URI)
   */
  public boolean containsOClass(URI theURI) {
    try {
      return owlim.hasClass(this.sesameRepositoryID, theURI.toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#containsOClass(gate.creole.ontology.OClass)
   */
  public boolean containsOClass(OClass theClass) {
    return containsOClass(theClass.getURI());
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOClasses(boolean)
   */
  public Set<OClass> getOClasses(boolean top) {
    try {
      ResourceInfo[] oClasses = owlim.getClasses(this.sesameRepositoryID, top);
      Set<OClass> set = new HashSet<OClass>();
      for(int i = 0; i < oClasses.length; i++) {
        set.add(Utils.createOClass(this.sesameRepositoryID, this, this.owlim,
                oClasses[i].getUri(), oClasses[i].isAnonymous()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getDistance(gate.creole.ontology.OClass,
   *      gate.creole.ontology.OClass)
   */
  public int getDistance(OClass class1, OClass class2) {

    if(!containsOClass(class1.getURI())) {
      Utils.warning(class1.getURI().toString() + " does not exist");
      return -1;
    }

    if(!containsOClass(class2.getURI())) {
      Utils.warning(class2.getURI().toString() + " does not exist");
      return -1;
    }

    int result = 0;
    OClass c;
    ArrayList<Set<OClass>> supers1 = class1.getSuperClassesVSDistance();
    ArrayList<Set<OClass>> supers2 = class2.getSuperClassesVSDistance();
    for(int i1 = 0; i1 < supers1.size(); i1++) {
      if(supers1.get(i1).contains(class2)) {
        result = i1 + 1;
        break;
      }
    }
    for(int i2 = 0; i2 < supers2.size(); i2++) {
      if(supers2.get(i2).contains(class1)) {
        result = i2 + 1;
        break;
      }
    }
    if(0 == result) {
      for(int i1 = 0; i1 < supers1.size(); i1++) {
        for(int i2 = 0; i2 < supers2.size(); i2++) {
          Set<OClass> s1 = supers1.get(i1);
          Set<OClass> s2 = supers2.get(i2);
          Iterator<OClass> i3 = s1.iterator();
          while(i3.hasNext()) {
            c = i3.next();
            if(s2.contains(c)) {
              result = i1 + i2 + 2;
              i1 = supers1.size();
              i2 = supers2.size();
              break;
            }
          }
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addOInstance(gate.creole.ontology.URI,
   *      gate.creole.ontology.OClass)
   */
  public OInstance addOInstance(URI theInstanceURI, OClass theClass) {
    try {

      if(!containsOClass(theClass.getURI())) {
        Utils.error(theClass.getURI().toString() + " does not exist");
        return null;
      }

      OResource anInst = getOResourceFromMap(theInstanceURI.toString());
      if(anInst != null && !(anInst instanceof OInstance)) {
        Utils.error(anInst.getURI().toString() + " already exists but "
                + " is not an ontology instance!");
        return null;
      }

      if(anInst != null
              && ((OInstance)anInst).getOClasses(OConstants.TRANSITIVE_CLOSURE)
                      .contains(theClass)) {
        Utils.warning(theInstanceURI.toString()
                + " is already registered as an instanceof "
                + theClass.getURI().toString());
        return (OInstance)anInst;
      }

      owlim.addIndividual(this.sesameRepositoryID,
              theClass.getURI().toString(), theInstanceURI.toString());
      OInstance oInst = Utils.createOInstance(this.sesameRepositoryID, this,
              owlim, theInstanceURI.toString());
      fireOntologyResourceAdded(oInst);

      // we need to add a label on this but after the new class addition
      // event has been fired
      oInst.setLabel(theInstanceURI.getResourceName(), null);

      return oInst;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeOInstance(gate.creole.ontology.OInstance)
   */
  public void removeOInstance(OInstance theInstance) {
    try {

      if(!containsOInstance(theInstance.getURI())) {
        Utils.warning(theInstance.getURI().toString() + " does not exist");
        return;
      }

      String[] deletedResources = owlim.removeIndividual(
              this.sesameRepositoryID, theInstance.getURI().toString());
      fireOntologyResourcesRemoved(deletedResources);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOInstances()
   */
  public Set<OInstance> getOInstances() {
    try {
      String[] oInsts = owlim.getIndividuals(this.sesameRepositoryID);
      Set<OInstance> set = new HashSet<OInstance>();
      for(int i = 0; i < oInsts.length; i++) {
        set.add(Utils.createOInstance(this.sesameRepositoryID, this,
                this.owlim, oInsts[i]));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOInstances(gate.creole.ontology.OClass,
   *      boolean)
   */
  public Set<OInstance> getOInstances(OClass theClass, byte closure) {
    try {
      String[] oInsts = owlim.getIndividuals(this.sesameRepositoryID, theClass
              .getURI().toString(), closure);
      Set<OInstance> set = new HashSet<OInstance>();

      if(!containsOClass(theClass.getURI())) {
        Utils.warning(theClass.getURI().toString() + " does not exist");
        return set;
      }

      for(int i = 0; i < oInsts.length; i++) {
        set.add(Utils.createOInstance(this.sesameRepositoryID, this,
                this.owlim, oInsts[i]));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOInstance(gate.creole.ontology.URI)
   */
  public OInstance getOInstance(URI theInstanceURI) {
    try {
      OResource resource = getOResourceFromMap(theInstanceURI.toString());
      if(resource != null) return (OInstance)resource;
      List<String> individuals = Arrays.asList(owlim
              .getIndividuals(this.sesameRepositoryID));
      if(individuals.contains(theInstanceURI.toString())) {
        return Utils.createOInstance(this.sesameRepositoryID, this, owlim,
                theInstanceURI.toString());
      }
      return null;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#containsOInstance(gate.creole.ontology.OInstance)
   */
  public boolean containsOInstance(OInstance theInstance) {
    return containsOInstance(theInstance.getURI());
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#containsOInstance(gate.creole.ontology.URI)
   */
  public boolean containsOInstance(URI theInstanceURI) {
    try {
      List<String> individuals = Arrays.asList(owlim
              .getIndividuals(this.sesameRepositoryID));
      return individuals.contains(theInstanceURI.toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addRDFProperty(gate.creole.ontology.URI,
   *      java.util.Set, java.util.Set)
   */
  public RDFProperty addRDFProperty(URI aPropertyURI, Set<OResource> domain,
          Set<OResource> range) {
    try {

      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof RDFProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (RDFProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not an RDFProperty");
          return null;
        }
      }

      String[] domainURIs = new String[domain.size()];
      String[] rangeURIs = new String[range.size()];
      Iterator<OResource> iter = domain.iterator();
      int counter = 0;
      while(iter.hasNext()) {
        domainURIs[counter] = iter.next().getURI().toString();
      }
      iter = range.iterator();
      counter = 0;
      while(iter.hasNext()) {
        rangeURIs[counter] = iter.next().getURI().toString();
      }
      owlim.addRDFProperty(this.sesameRepositoryID, aPropertyURI.toString(),
              domainURIs, rangeURIs);
      RDFProperty rp = Utils.createOProperty(this.sesameRepositoryID, this,
              owlim, aPropertyURI.toString(), OConstants.RDF_PROPERTY);
      fireOntologyResourceAdded(rp);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      rp.setLabel(aPropertyURI.getResourceName(), null);

      return rp;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getRDFProperties()
   */
  public Set<RDFProperty> getRDFProperties() {
    try {
      Property[] properties = owlim.getRDFProperties(this.sesameRepositoryID);
      Set<RDFProperty> set = new HashSet<RDFProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add((RDFProperty)Utils.createOProperty(this.sesameRepositoryID,
                this, owlim, properties[i].getUri(), properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isRDFProperty(gate.creole.ontology.URI)
   */
  public boolean isRDFProperty(URI thePropertyURI) {
    try {
      return owlim.isRDFProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addAnnotationProperty(gate.creole.ontology.URI)
   */
  public AnnotationProperty addAnnotationProperty(URI aPropertyURI) {
    try {

      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof AnnotationProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (AnnotationProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not an AnnotationProperty");
          return null;
        }
      }

      owlim.addAnnotationProperty(this.sesameRepositoryID, aPropertyURI
              .toString());
      AnnotationProperty ap = (AnnotationProperty)Utils.createOProperty(
              this.sesameRepositoryID, this, owlim, aPropertyURI.toString(),
              OConstants.ANNOTATION_PROPERTY);
      fireOntologyResourceAdded(ap);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      ap.setLabel(aPropertyURI.getResourceName(), null);

      return ap;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getAnnotationProperties()
   */
  public Set<AnnotationProperty> getAnnotationProperties() {
    try {
      Property[] properties = owlim
              .getAnnotationProperties(this.sesameRepositoryID);
      Set<AnnotationProperty> set = new HashSet<AnnotationProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add((AnnotationProperty)Utils.createOProperty(
                this.sesameRepositoryID, this, owlim, properties[i].getUri(),
                properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isAnnotationProperty(gate.creole.ontology.URI)
   */
  public boolean isAnnotationProperty(URI thePropertyURI) {
    try {
      return owlim.isAnnotationProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addDatatypeProperty(gate.creole.ontology.URI,
   *      java.util.Set, gate.creole.ontology.DataType)
   */
  public DatatypeProperty addDatatypeProperty(URI aPropertyURI,
          Set<OClass> domain, DataType aDatatype) {
    try {

      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof DatatypeProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (DatatypeProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not a DatatypeProperty");
          return null;
        }
      }

      String[] domainURIs = new String[domain.size()];
      Iterator<OClass> iter = domain.iterator();
      int counter = 0;
      while(iter.hasNext()) {
        domainURIs[counter] = iter.next().getURI().toString();
      }
      owlim.addDataTypeProperty(this.sesameRepositoryID, aPropertyURI
              .toString(), domainURIs, aDatatype.getXmlSchemaURI().toString());
      DatatypeProperty dp = (DatatypeProperty)Utils.createOProperty(
              this.sesameRepositoryID, this, owlim, aPropertyURI.toString(),
              OConstants.DATATYPE_PROPERTY);
      fireOntologyResourceAdded(dp);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      dp.setLabel(aPropertyURI.getResourceName(), null);

      return dp;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getDatatypeProperties()
   */
  public Set<DatatypeProperty> getDatatypeProperties() {
    try {
      Property[] properties = owlim
              .getDatatypeProperties(this.sesameRepositoryID);
      Set<DatatypeProperty> set = new HashSet<DatatypeProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add((DatatypeProperty)Utils.createOProperty(
                this.sesameRepositoryID, this, owlim, properties[i].getUri(),
                properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isDatatypeProperty(gate.creole.ontology.URI)
   */
  public boolean isDatatypeProperty(URI thePropertyURI) {
    try {
      return owlim.isDatatypeProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addObjectProperty(gate.creole.ontology.URI,
   *      java.util.Set, java.util.Set)
   */
  public ObjectProperty addObjectProperty(URI aPropertyURI, Set<OClass> domain,
          Set<OClass> range) {
    try {
      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof ObjectProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (ObjectProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not an ObjectProperty");
          return null;
        }
      }

      String[] domainURIs = new String[domain.size()];
      String[] rangeURIs = new String[range.size()];
      Iterator<OClass> iter = domain.iterator();
      int counter = 0;
      while(iter.hasNext()) {
        domainURIs[counter] = iter.next().getURI().toString();
        counter++;
      }
      iter = range.iterator();
      counter = 0;
      while(iter.hasNext()) {
        rangeURIs[counter] = iter.next().getURI().toString();
        counter++;
      }
      owlim.addObjectProperty(this.sesameRepositoryID, aPropertyURI.toString(),
              domainURIs, rangeURIs);
      ObjectProperty op = (ObjectProperty)Utils.createOProperty(
              this.sesameRepositoryID, this, owlim, aPropertyURI.toString(),
              OConstants.OBJECT_PROPERTY);
      fireOntologyResourceAdded(op);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      op.setLabel(aPropertyURI.getResourceName(), null);

      return op;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getObjectProperties()
   */
  public Set<ObjectProperty> getObjectProperties() {
    try {
      Property[] properties = owlim
              .getObjectProperties(this.sesameRepositoryID);
      Set<ObjectProperty> set = new HashSet<ObjectProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add((ObjectProperty)Utils.createOProperty(this.sesameRepositoryID,
                this, owlim, properties[i].getUri(), properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isObjectProperty(gate.creole.ontology.URI)
   */
  public boolean isObjectProperty(URI thePropertyURI) {
    try {
      return owlim.isObjectProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addSymmetricProperty(gate.creole.ontology.URI,
   *      java.util.Set)
   */
  public SymmetricProperty addSymmetricProperty(URI aPropertyURI,
          Set<OClass> domainAndRange) {
    try {
      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof SymmetricProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (SymmetricProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not an SymmetricProperty");
          return null;
        }
      }

      String[] domainURIs = new String[domainAndRange.size()];
      Iterator<OClass> iter = domainAndRange.iterator();
      int counter = 0;
      while(iter.hasNext()) {
        domainURIs[counter] = iter.next().getURI().toString();
        counter++;
      }
      owlim.addSymmetricProperty(this.sesameRepositoryID, aPropertyURI
              .toString(), domainURIs);
      SymmetricProperty sp = (SymmetricProperty)Utils.createOProperty(
              this.sesameRepositoryID, this, owlim, aPropertyURI.toString(),
              OConstants.SYMMETRIC_PROPERTY);
      fireOntologyResourceAdded(sp);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      sp.setLabel(aPropertyURI.getResourceName(), null);

      return sp;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getSymmetricProperties()
   */
  public Set<SymmetricProperty> getSymmetricProperties() {
    try {

      Property[] properties = owlim
              .getSymmetricProperties(this.sesameRepositoryID);
      Set<SymmetricProperty> set = new HashSet<SymmetricProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add((SymmetricProperty)Utils.createOProperty(
                this.sesameRepositoryID, this, owlim, properties[i].getUri(),
                properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isSymmetricProperty(gate.creole.ontology.URI)
   */
  public boolean isSymmetricProperty(URI thePropertyURI) {
    try {
      return owlim.isSymmetricProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addTransitiveProperty(gate.creole.ontology.URI,
   *      java.util.Set, java.util.Set)
   */
  public TransitiveProperty addTransitiveProperty(URI aPropertyURI,
          Set<OClass> domain, Set<OClass> range) {
    try {
      OResource res = getOResourceFromMap(aPropertyURI.toString());
      if(res != null) {
        if(res instanceof TransitiveProperty) {
          Utils.warning(aPropertyURI.toString() + " already exists");
          return (TransitiveProperty)res;
        }
        else {
          Utils.error(aPropertyURI.toString()
                  + " already exists but it is not a TransitiveProperty");
          return null;
        }
      }

      String[] domainURIs = new String[domain.size()];
      String[] rangeURIs = new String[range.size()];
      Iterator<OClass> iter = domain.iterator();
      int counter = 0;
      while(iter.hasNext()) {
        domainURIs[counter] = iter.next().getURI().toString();
        counter++;
      }
      iter = range.iterator();
      counter = 0;
      while(iter.hasNext()) {
        rangeURIs[counter] = iter.next().getURI().toString();
        counter++;
      }
      owlim.addTransitiveProperty(this.sesameRepositoryID, aPropertyURI
              .toString(), domainURIs, rangeURIs);
      TransitiveProperty tp = (TransitiveProperty)Utils.createOProperty(
              this.sesameRepositoryID, this, owlim, aPropertyURI.toString(),
              OConstants.TRANSITIVE_PROPERTY);
      fireOntologyResourceAdded(tp);

      // we need to add a label on this but after the new resource
      // addition event has been fired
      tp.setLabel(aPropertyURI.getResourceName(), null);

      return tp;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getTransitiveProperties()
   */
  public Set<TransitiveProperty> getTransitiveProperties() {
    try {
      Property[] properties = owlim
              .getTransitiveProperties(this.sesameRepositoryID);
      Set<TransitiveProperty> set = new HashSet<TransitiveProperty>();
      for(int i = 0; i < properties.length; i++) {

        set.add((TransitiveProperty)Utils.createOProperty(
                this.sesameRepositoryID, this, owlim, properties[i].getUri(),
                properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isTransitiveProperty(gate.creole.ontology.URI)
   */
  public boolean isTransitiveProperty(URI thePropertyURI) {
    try {
      return owlim.isTransitiveProperty(this.sesameRepositoryID, thePropertyURI
              .toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getPropertyDefinitions()
   */
  public Set<RDFProperty> getPropertyDefinitions() {
    Set<RDFProperty> set = new HashSet<RDFProperty>();
    set.addAll(getAnnotationProperties());
    set.addAll(getDatatypeProperties());
    set.addAll(getObjectProperties());
    set.addAll(getRDFProperties());
    return set;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getProperty(gate.creole.ontology.URI)
   */
  public RDFProperty getProperty(URI thePropertyURI) {
    try {
      Property property = owlim.getPropertyFromOntology(
              this.sesameRepositoryID, thePropertyURI.toString());
      if(property == null) return null;
      return Utils.createOProperty(this.sesameRepositoryID, this, owlim,
              thePropertyURI.toString(), property.getType());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeProperty(gate.creole.ontology.RDFProperty)
   */
  public void removeProperty(RDFProperty theProperty) {
    try {

      OResource res = getOResourceFromMap(theProperty.getURI().toString());
      if(res == null) {
        Utils.warning(theProperty.getURI().toString() + " does not exist");
        return;
      }

      String[] deletedResources = owlim.removePropertyFromOntology(
              this.sesameRepositoryID, theProperty.getURI().toString());
      fireOntologyResourcesRemoved(deletedResources);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#setModified(boolean)
   */
  public void setModified(boolean isModified) {
    this.isModified = isModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#isModified()
   */
  public boolean isModified() {
    return this.isModified;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addOntologyModificationListener(gate.creole.ontology.OntologyModificationListener)
   */
  public synchronized void addOntologyModificationListener(
          OntologyModificationListener oml) {
    List<OntologyModificationListener> newListeners =
            new ArrayList<OntologyModificationListener>();
    if(this.modificationListeners != null) {
      newListeners.addAll(this.modificationListeners);
    }
    newListeners.add(oml);
    this.modificationListeners = newListeners;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeOntologyModificationListener(gate.creole.ontology.OntologyModificationListener)
   */
  public synchronized void removeOntologyModificationListener(
          OntologyModificationListener oml) {
    if(this.modificationListeners == null
            || !this.modificationListeners.contains(oml)) {
      return;
    }
    else {
      List<OntologyModificationListener> newListeners =
              new ArrayList<OntologyModificationListener>();
      for(OntologyModificationListener l : this.modificationListeners) {
        if(l != oml) {
          newListeners.add(l);
        }
      }
      this.modificationListeners = newListeners;
    }
  }

  /**
   * A method to invoke when the ontology is modified
   * 
   * @param resource
   * @param eventType
   */
  public void fireOntologyModificationEvent(OResource resource, int eventType) {
    List<OntologyModificationListener> listeners = this.modificationListeners;
    if(listeners != null) {
      for(OntologyModificationListener l : listeners) {
        l.ontologyModified(this, resource, eventType);
      }
    }
  }

  public void fireOntologyReset() {
    List<OntologyModificationListener> listeners = this.modificationListeners;
    if(listeners != null) {
      for(OntologyModificationListener l : listeners) {
        l.ontologyReset(this);
      }
    }
  }

  /**
   * A Method to invoke an event for newly added ontology resource
   * 
   * @param resource
   */
  public void fireOntologyResourceAdded(OResource resource) {
    List<OntologyModificationListener> listeners = this.modificationListeners;
    if(listeners != null) {
      for(OntologyModificationListener l : listeners) {
        l.resourceAdded(this, resource);
      }
    }
  }

  /**
   * A Method to invoke an event for a removed ontology resource
   * 
   * @param resource
   */
  public void fireOntologyResourcesRemoved(String[] resources) {
    // we need to delete this resource from our maps
    for(int i = 0; i < resources.length; i++) {
      removeOResourceFromMap(resources[i]);
    }

    List<OntologyModificationListener> listeners = this.modificationListeners;
    if(listeners != null) {
      for(OntologyModificationListener l : listeners) {
        l.resourcesRemoved(this, resources);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#startTransaction()
   */
  public abstract void startTransaction();

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#commitTransaction()
   */
  public abstract void commitTransaction();

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#transationStarted()
   */
  public abstract boolean transationStarted();

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getSesameRepository()
   */
  public abstract SesameRepository getSesameRepository();

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getSesameRepositoryID()
   */
  public String getSesameRepositoryID() {
    return this.sesameRepositoryID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#getOResourceFromMap(java.lang.String)
   */
  public OResource getOResourceFromMap(String uri) {
    return urisToOResouceMap.get(uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#addOResourceToMap(java.lang.String,
   *      gate.creole.ontology.OResource)
   */
  public void addOResourceToMap(String uri, OResource resource) {
    urisToOResouceMap.put(uri, resource);
    String resourceName = OntologyUtilities.getResourceName(uri);
    List<OResource> resources = resourceNamesToOResourcesMap.get(resourceName);
    if(resources == null) {
      resources = new ArrayList<OResource>();
      resourceNamesToOResourcesMap.put(resourceName, resources);
    }
    resources.add(resource);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Ontology#removeOResourceFromMap(java.lang.String)
   */
  public void removeOResourceFromMap(String uri) {
    urisToOResouceMap.remove(uri);
    String resourceName = OntologyUtilities.getResourceName(uri);
    resourceNamesToOResourcesMap.remove(resourceName);
  }

  private boolean callFromCleanup = false;

  /*
   * (non-Javadoc)
   * 
   * @see gate.Resource#cleanup()
   */
  public void cleanup() {
    if(owlim != null && !getPersistRepository().booleanValue()) {
      callFromCleanup = true;
      cleanOntology();
      try {
        owlim.removeRepository(this.sesameRepositoryID, getPersistRepository()
                .booleanValue());
      }
      catch(Exception e) {
        throw new GateRuntimeException(e);
      }
    }
    urisToOResouceMap.clear();
  }

  /**
   * Returns an instance of OWLIM
   * 
   * @return
   */
  public OWLIM getOwlim() {
    return owlim;
  }

  /**
   * Sets an instance of OWLIM
   * 
   * @param owlim
   */
  public void setOwlim(OWLIM owlim) {
    this.owlim = owlim;
  }

  /**
   * Indicates whether the repository should be persisted.
   * 
   * @return
   */
  public Boolean getPersistRepository() {
    return persistRepository;
  }

  /**
   * Indicates whether the repository should be persisted.
   * 
   * @param persistRepository
   */
  public void setPersistRepository(Boolean persistRepository) {
    this.persistRepository = persistRepository;
  }

  /**
   * This method checks in its cache find out the URI for the given
   * resource name. However, doesn't guranttee that it will be able to
   * return the URI. It is also possible for two resources to have a
   * same name but different name spaces. This method returns a List
   * containing all such URIs.
   * 
   * @param resourceName
   * @return
   */
  public OResource getOResourceByName(String resourceName) {
    List<OResource> resources = resourceNamesToOResourcesMap.get(resourceName);
    if(resources != null) {
      if(resources.size() > 1)
        System.err
                .print("Warning : there are more than one resources matching with the name "
                        + resourceName);

      return resources.get(0);
    }
    return null;
  }

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
  public List<OResource> getOResourcesByName(String resourceName) {
    List<OResource> resources = resourceNamesToOResourcesMap.get(resourceName);
    if(resources == null) return null;
    List<OResource> toReturn = new ArrayList<OResource>();
    toReturn.addAll(resources);
    return toReturn;
  }

  /**
   * This method returns a list of OResources from the ontology. Please
   * note that deleting an instance from this list (e.g.
   * list.remove(int/Object)) does not delete the resource from an
   * ontology. One must use appropriate method from the Ontology
   * interface to delete such resources.
   * 
   * @return
   */
  public List<OResource> getAllResources() {
    try {
      getOClasses(false);
      getOInstances();
      getPropertyDefinitions();
      List<OResource> toReturn = new ArrayList<OResource>();
      Iterator<String> keys = resourceNamesToOResourcesMap.keySet().iterator();
      while(keys.hasNext()) {
        toReturn.addAll(resourceNamesToOResourcesMap.get(keys.next()));
      }
      return toReturn;
    }
    catch(Exception e) {
      throw new GateRuntimeException(e);
    }
  }

  /**
   * Tries to save the ontology at the provided File
   */
  public void store(File newOntology) throws IOException {
    try {
      String output = owlim.getOntologyData(sesameRepositoryID,
              OConstants.ONTOLOGY_FORMAT_NTRIPLES);
      BufferedWriter writer = new BufferedWriter(new FileWriter(newOntology));
      writer.write(output);
      writer.flush();
      writer.close();
    }
    catch(Exception e) {
      throw new IOException(e.getMessage());
    }

  }

  /**
   * This method given a property (either an annotation or datatype),
   * retrieves a list of resources which have the provided literal set
   * as a value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public List<OResource> getOResourcesWith(RDFProperty aProperty, Literal aValue) {
    List<OResource> toReturn = new ArrayList<OResource>();

    int propType = 1;

    if(aProperty instanceof AnnotationProperty) {
      propType = 1;
    }
    else if(aProperty instanceof DatatypeProperty) {
      propType = 2;
    }
    else {
      return toReturn;
    }

    // here the first thing is to obtain all the resources
    List<OResource> resources = getAllResources();

    // and on each resource we need to check if it has the above
    // property set on it
    for(OResource aResource : resources) {
      switch(propType) {
        case 1:
          if(aResource.hasAnnotationPropertyWithValue(
                  (AnnotationProperty)aProperty, aValue))
            toReturn.add(aResource);
          break;
        case 2:
          if(aResource instanceof OInstance
                  && ((OInstance)aResource).hasDatatypePropertyWithValue(
                          (DatatypeProperty)aProperty, aValue))
            toReturn.add(aResource);
          break;
      }
    }
    return toReturn;
  }

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
          OResource aValue) {
    List<OResource> toReturn = new ArrayList<OResource>();

    int propType = 1;

    if(aProperty instanceof ObjectProperty) {
      propType = 1;
    }
    else if(!(aProperty instanceof DatatypeProperty)) {
      propType = 2;
    }
    else {
      return toReturn;
    }

    // here the first thing is to obtain all the resources
    List<OResource> resources = getAllResources();

    // and on each resource we need to check if it has the above
    // property set on it
    for(OResource aResource : resources) {
      switch(propType) {
        case 1:
          if(aResource instanceof OInstance
                  && aValue instanceof OInstance
                  && ((OInstance)aResource).hasObjectPropertyWithValue(
                          (ObjectProperty)aProperty, (OInstance)aValue))
            toReturn.add(aResource);
          break;
        case 2:
          if(aResource instanceof OInstance
                  && ((OInstance)aResource).hasRDFPropertyWithValue(aProperty,
                          aValue)) toReturn.add(aResource);
          break;
      }
    }
    return toReturn;
  }
}
