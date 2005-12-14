/*
 * JenaOntologyImpl.java
 *
 * Copyright (c) 2005, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan 09/2005
 *
 *
 *  $Id$
 */
package gate.creole.ontology.jena;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.creole.ontology.Property;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.DatatypeProperty;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.util.Err;
import gate.util.GateRuntimeException;
import gate.util.Out;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.ontotext.gate.ontology.OntologyImpl;

/**
 * An implementation for GATE Ontologies based on Jena2
 * 
 * @author Valentin Tablan
 * 
 */
public class JenaOntologyImpl extends OntologyImpl implements ActionsPublisher {
  public JenaOntologyImpl() {
    actionsList = new ArrayList();
    actionsList.add(new LoadOntologyDataAction("Load OWL-Lite data",
            "Reads OWL-Lite data from a file and populates the ontology.",
            OWL_LITE));
    actionsList
            .add(new LoadOntologyDataAction(
                    "Load OWL-DL data",
                    "Reads OWL-DL data from a file and populates the ontology.",
                    OWL_DL));
    actionsList.add(new LoadOntologyDataAction("Load OWL-Full data",
            "Reads OWL-Full data from a file and populates the ontology.",
            OWL_FULL));
    actionsList.add(new LoadOntologyDataAction("Load RDF(S) data",
            "Reads RDF(S) data from a file and populates the ontology.", RDFS));
    actionsList.add(new LoadOntologyDataAction("Load DAML data",
            "Reads DAML data from a file and populates the ontology.", DAML));
    actionsList.add(null);
    actionsList.add(new CleanUpAction());
    actionsList.add(null);
    actionsList.add(new SaveOntologyAction("Save to file",
            "Saves the ontology to a file", OWL_LITE));
  }

  public Resource init() throws ResourceInstantiationException {
    load();
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.gui.ActionsPublisher#getActions()
   */
  public List getActions() {
    return actionsList;
  }

  /**
   * Loads the ontology from an external file.
   */
  public void load() throws ResourceInstantiationException {
    // find what type of ontolgoy are we loading
    URL inputURL = null;
    int ontologyType = -1;
    if(owlLiteFileURL != null) {
      inputURL = owlLiteFileURL;
      ontologyType = OWL_LITE;
    } else if(owlDlFileURL != null) {
      inputURL = owlDlFileURL;
      ontologyType = OWL_DL;
    } else if(owlFullFileURL != null) {
      inputURL = owlFullFileURL;
      ontologyType = OWL_FULL;
    } else if(rdfsFileURL != null) {
      inputURL = rdfsFileURL;
      ontologyType = RDFS;
    } else if(damlFileURL != null) {
      inputURL = damlFileURL;
      ontologyType = DAML;
    } else {
      // no input file provided - create an empty OWL Lite ontology
      inputURL = null;
      ontologyType = OWL_LITE;
    }
    super.setURL(inputURL);
    load(inputURL, ontologyType);
  }

  /**
   * Loads ontological data of a given type from a URL.
   * 
   * @param inputURL
   *          the URL for the file to be read.
   * @param ontologyType
   *          the type of ontology (one of {@link #OWL_LITE}, {@link #OWL_DL},
   *          {@link #OWL_FULL}, {@link #RDFS} or {@link #DAML}).
   */
  public void load(URL inputURL, int ontologyType)
          throws ResourceInstantiationException {
    // build the Jena model
    // we need some type of inference here in order to get all triples (i.e.
    // including the inferred ones)
    OntModel jenaModel = null;
    switch(ontologyType){
      case OWL_LITE:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
        break;
      case OWL_DL:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        break;
      case OWL_FULL:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
        break;
      case RDFS:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
        break;
      case DAML:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.DAML_MEM_RDFS_INF);
        break;
      default:
        throw new ResourceInstantiationException("Ontology type not specified!");
    }
    if(inputURL != null) {
      try {
        jenaModel.read(new BufferedInputStream(inputURL.openStream()), "");
        // get the default name space
        String defaultURIPrefix = jenaModel.getNsPrefixURI("");
        if(defaultURIPrefix != null) setSourceURI(defaultURIPrefix);
      } catch(IOException ioe) {
        throw new ResourceInstantiationException(ioe);
      }
    } else {
      if(defaultNameSpace != null && defaultNameSpace.trim().length() > 0) {
        setSourceURI(defaultNameSpace);
      }
    }
    if(this.getPropertyDefinitionByName("label") == null) {
      // add labels as properties of everything
      Set range = new HashSet();
      range.add(String.class);
      addProperty("label", "label property", new HashSet(), range);
    }
    // convert the Jena model into a GATE ontology
    // create the class hierarchy
    ExtendedIterator topClassIter = jenaModel.listHierarchyRootClasses();
    while(topClassIter.hasNext()) {
      OntClass aTopClass = (OntClass)topClassIter.next();
      toGateClassRec(aTopClass, null);
    }
    // create the properties
    Iterator propIter = jenaModel.listOntProperties();
    while(propIter.hasNext()) {
      OntProperty aJenaProperty = (OntProperty)propIter.next();
      if(aJenaProperty.isOntLanguageTerm()) continue;
      String propertyName = aJenaProperty.getLocalName();
      String propertyComment = aJenaProperty.getComment(language);
      Set gateDomain = convertoToGateClasses(aJenaProperty.listDomain());
      reduceToMostSpecificClasses(gateDomain);
      Property theProp = null;
      if(ontologyType != RDFS && aJenaProperty.isObjectProperty()) {
        Set gateRange = convertoToGateClasses(aJenaProperty.listRange());
        reduceToMostSpecificClasses(gateRange);
        theProp = addObjectProperty(propertyName, propertyComment, gateDomain,
                gateRange);
      } else if(ontologyType != RDFS && aJenaProperty.isTransitiveProperty()) {
        Set gateRange = convertoToGateClasses(aJenaProperty.listRange());
        reduceToMostSpecificClasses(gateRange);
        theProp = addTransitiveProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      } else if(ontologyType != RDFS && ontologyType != DAML
              && aJenaProperty.isSymmetricProperty()) {
        Set gateRange = convertoToGateClasses(aJenaProperty.listRange());
        reduceToMostSpecificClasses(gateRange);
        theProp = addSymmetricProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      } else if(ontologyType != RDFS && aJenaProperty.isDatatypeProperty()) {
        Iterator rangeIter = aJenaProperty.listRange();
        Set range = new HashSet();
        while(rangeIter.hasNext())
          range.add(rangeIter.next());
        if(!range.isEmpty()) {
          Out.prln("WARNING: Support for datatype properties ranges is not "
                  + "implemented!\nRange " + range.toString()
                  + " for property " + aJenaProperty.getLocalName()
                  + " has been ignored!");
        }
        theProp = addDatatypeProperty(propertyName, propertyComment,
                gateDomain, Object.class);
      } else {
        // generic type of property
        if(ontologyType == OWL_LITE || ontologyType == OWL_DL
                || ontologyType == OWL_FULL) {
          Err.prln("WARNING: Property: \"" + aJenaProperty.toString() + "\""
                  + " is neither an object or a datatype property!");
        }
        Set gateRange = convertoToGateClasses(aJenaProperty.listRange());
        theProp = addProperty(propertyName, propertyComment, gateDomain,
                gateRange);
      }
      theProp.setURI(aJenaProperty.getURI());
      if(ontologyType != RDFS) {
        theProp.setFunctional(aJenaProperty.isFunctionalProperty());
        theProp.setInverseFunctional(aJenaProperty
                .isInverseFunctionalProperty());
      }
    }
    // create the properties hierarchy
    propIter = jenaModel.listOntProperties();
    while(propIter.hasNext()) {
      OntProperty aJenaProp = (OntProperty)propIter.next();
      Property aGateProp = getPropertyDefinitionByName(aJenaProp.getLocalName());
      for(Iterator superIter = aJenaProp.listSuperProperties(); superIter
              .hasNext();) {
        OntProperty aJenaSuperProp = (OntProperty)superIter.next();
        if(aJenaSuperProp.isOntLanguageTerm()) continue;
        Property aGateSuperProp = getPropertyDefinitionByName(aJenaSuperProp
                .getLocalName());
        if(aGateSuperProp == null) {
          Err.prln("WARNING: Unknown super property \""
                  + aJenaSuperProp.getLocalName() + "\" for property \""
                  + aJenaProp.getLocalName() + "\"!");
        } else {
          aGateProp.addSuperProperty(aGateSuperProp);
          aGateSuperProp.addSubProperty(aGateProp);
        }
      }
    }
    // read the instances
    Iterator instanceIter = jenaModel.listIndividuals();
    while(instanceIter.hasNext()) {
      Individual aJenaInstance = (Individual)instanceIter.next();
      Set jenaClasses = new HashSet();
      Iterator classIter = aJenaInstance.listRDFTypes(true);
      while(classIter.hasNext()) {
        OntClass aClass = (OntClass)((com.hp.hpl.jena.rdf.model.Resource)classIter
                .next()).as(OntClass.class);
        jenaClasses.add(aClass);
      }
      Set gateClasses = convertoToGateClasses(jenaClasses.iterator());
      OInstance gateInstance = new OInstanceImpl(aJenaInstance.getLocalName(),
              aJenaInstance.getComment(language), gateClasses, this);
      addInstance(gateInstance);
      String label = aJenaInstance.getLabel(language);
      // if(label!=null)
      // gateInstance.addPropertyValue("label", label);
    }
    // add the property values
    instanceIter = jenaModel.listIndividuals();
    while(instanceIter.hasNext()) {
      Individual aJenaInstance = (Individual)instanceIter.next();
      OInstance aGateInstance = getInstanceByName(aJenaInstance.getLocalName());
      propIter = aJenaInstance.listProperties();
      while(propIter.hasNext()) {
        Statement propStatement = (Statement)propIter.next();
        com.hp.hpl.jena.rdf.model.Property aProperty = (com.hp.hpl.jena.rdf.model.Property)(propStatement)
                .getPredicate();
        RDFNode objectNode = propStatement.getObject();
        Property aGateProperty = getPropertyDefinitionByName(aProperty
                .getLocalName());
        if(aGateProperty == null) {
          // Err.prln("Property not found " + aProperty.getURI() + " type: " +
          // aProperty.getClass().getName());
          continue;
        }
        if(aGateProperty instanceof ObjectProperty) {
          if(objectNode.canAs(Individual.class)) {
            Individual aJenaValue = (Individual)objectNode.as(Individual.class);
            OInstance gateValue = getInstanceByName(aJenaValue.getLocalName());
            boolean success = aGateInstance.addPropertyValue(aProperty
                    .getLocalName(), gateValue);
            if(!success)
              Err.prln("Could not set value \"" + gateValue
                      + "\" for property \"" + aProperty + "\" on instance "
                      + aGateInstance.getName());
          } else {
            Err.prln("WARNING: object property " + aGateProperty.getName()
                    + " has a value which is not an object "
                    + objectNode.toString());
          }
        } else {
          // datatype property
          if(objectNode.canAs(Literal.class)) {
            Literal aJenaValue = (Literal)objectNode.as(Literal.class);
            Object aGateValue = aJenaValue.getDatatype() == null ? aJenaValue
                    .getLexicalForm() : aJenaValue.getDatatype().parse(
                    aJenaValue.getLexicalForm());
            boolean success = aGateInstance.addPropertyValue(aGateProperty
                    .getName(), aGateValue);
            if(!success)
              Err.prln("Could not set value \"" + aGateValue.toString()
                      + "\" for property \"" + aProperty + "\" on instance "
                      + aGateInstance.getName());
          } else {
            Err.prln("WARNING: datatype property " + aGateProperty.getName()
                    + " has a value which is not an RDF literal "
                    + objectNode.toString());
          }
        }
      }
    }
  }

  /**
   * Saves the ontology to a file.
   * 
   * @param outputFile
   *          the file to write to.
   * @param ontologyType
   *          the output format to write.
   */
  public void save(File outputFile, int ontologyType) {
    OntModel jenaModel = null;
    switch(ontologyType){
      case OWL_LITE:
        jenaModel = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
        break;
      default:
        throw new IllegalArgumentException("Ontology type " + ontologyType
                + " is not supported!");
    }
    // set the default URI
    jenaModel.setNsPrefix("", getSourceURI());
    // create the class hierarchy
    Iterator topClassIter = getTopClasses().iterator();
    while(topClassIter.hasNext()) {
      OClass aGateClass = (OClass)topClassIter.next();
      toJenaClassRec(aGateClass, jenaModel, null);
    }
    // create the properties
    Iterator propIter = getPropertyDefinitions().iterator();
    while(propIter.hasNext()) {
      Property aGateProp = (Property)propIter.next();
      OntProperty aJenaProp = null;
      if(aGateProp instanceof ObjectProperty) {
        aJenaProp = jenaModel.createObjectProperty(aGateProp.getURI());
        Iterator rangIter = ((ObjectProperty)aGateProp).getRange().iterator();
        while(rangIter.hasNext()) {
          OClass aGateRangeClass = (OClass)rangIter.next();
          OntClass aJenaRangeClass = jenaModel.getOntClass(aGateRangeClass
                  .getURI());
          aJenaProp.addRange(aJenaRangeClass);
        }
      } else if(aGateProp instanceof DatatypeProperty) {
        aJenaProp = jenaModel.createDatatypeProperty(aGateProp.getURI());
      }
      aJenaProp.setLabel(aGateProp.getName(), language);
      String comment = aGateProp.getComment();
      if(comment != null) aJenaProp.setComment(comment, language);
      // set the domain
      Iterator domainIter = aGateProp.getDomain().iterator();
      while(domainIter.hasNext()) {
        OClass aGateDomainClass = (OClass)domainIter.next();
        OntClass aJenaDomainClass = jenaModel.getOntClass(aGateDomainClass
                .getURI());
        aJenaProp.addDomain(aJenaDomainClass);
      }
    }
    // create the properties hierarchy
    propIter = jenaModel.listObjectProperties();
    while(propIter.hasNext()) {
      com.hp.hpl.jena.ontology.ObjectProperty aJenaProp = (com.hp.hpl.jena.ontology.ObjectProperty)propIter
              .next();
      Property aGateProp = getPropertyDefinitionByName(aJenaProp.getLocalName());
      Iterator superPropIter = aGateProp.getSuperProperties(DIRECT_CLOSURE)
              .iterator();
      while(superPropIter.hasNext()) {
        Property aGateSuperProp = (Property)superPropIter.next();
        OntProperty aJenaSuperProp = jenaModel.getOntProperty(aGateSuperProp
                .getURI());
        aJenaProp.addSuperProperty(aJenaSuperProp);
      }
    }
    // create the instances
    // we need to keep track of anonymous nodes - use two synchronised lists
    List gateInstances = new ArrayList(getInstances());
    List jenaInstances = new ArrayList(gateInstances.size());
    for(int i = 0; i < gateInstances.size(); i++) {
      OInstance aGateInstace = (OInstance)gateInstances.get(i);
      String instanceURI = aGateInstace.getURI();
      Set types = aGateInstace.getOClasses();
      Iterator typeIter = types.iterator();
      // get the first type and use for creation
      OClass aGateType = (OClass)typeIter.next();
      OntClass aJenaType = jenaModel.getOntClass(aGateType.getURI());
      Individual aJenaIndiv = instanceURI == null ? jenaModel
              .createIndividual(aJenaType) : jenaModel.createIndividual(
              instanceURI, aJenaType);
      jenaInstances.add(aJenaIndiv);
      // add all the remaining types, if any
      while(typeIter.hasNext()) {
        aGateType = (OClass)typeIter.next();
        aJenaType = jenaModel.getOntClass(aGateType.getURI());
        aJenaIndiv.addRDFType(aJenaType);
      }
    }
    // add the property values for all instances
    for(int i = 0; i < gateInstances.size(); i++) {
      OInstance aGateInstace = (OInstance)gateInstances.get(i);
      Individual aJenaIndiv = (Individual)jenaInstances.get(i);
      Iterator propNameIter = aGateInstace.getSetPropertiesNames().iterator();
      while(propNameIter.hasNext()) {
        String propertyName = (String)propNameIter.next();
        Property aProperty = getPropertyDefinitionByName(propertyName);
        OntProperty aJenaProp = jenaModel.getOntProperty(aProperty.getURI());
        Iterator valuesIter = aGateInstace.getPropertyValues(propertyName)
                .iterator();
        while(valuesIter.hasNext()) {
          Object value = valuesIter.next();
          if(value instanceof OInstance) {
            // object value
            OInstance instanceValue = (OInstance)value;
            String instURI = instanceValue.getURI();
            Individual jenaInstValue = null;
            if(instURI != null) {
              jenaInstValue = jenaModel.getIndividual(instURI);
            } else {
              jenaInstValue = (Individual)jenaInstances.get(gateInstances
                      .indexOf(instanceValue));
            }
            aJenaIndiv.addProperty(aJenaProp, jenaInstValue);
          } else {
            // datatype value
            aJenaIndiv.addProperty(aJenaProp, value);
          }
        }
      }
    }
    // save to the file
    try {
      jenaModel.write(new FileOutputStream(outputFile));
    } catch(IOException ioe) {
      throw new GateRuntimeException(ioe);
    }
  }

  /**
   * Converts a GATE class and all its subclasses (recursively) to Jena classes
   * and adds them to the Jena model provided.
   * 
   * @param aClass
   *          the GATE class to be converted.
   * @param jenaModel
   *          the Jena model to be populated.
   * @param parentClass
   *          the parent of the GATE class in the Jena model. If <tt>null</tt>
   *          then the created Jena class will be marked as a top class.
   */
  protected void toJenaClassRec(OClass aGateClass, OntModel jenaModel,
          OntClass parentClass) {
    OntClass jenaClass = jenaModel.createClass(aGateClass.getURI());
    jenaClass.setLabel(aGateClass.getName(), language);
    if(parentClass != null) jenaClass.addSuperClass(parentClass);
    String comment = aGateClass.getComment();
    if(comment != null) jenaClass.setComment(comment, language);
    // make the recursive calls
    Iterator subClassIter = aGateClass.getSubClasses(DIRECT_CLOSURE).iterator();
    while(subClassIter.hasNext()) {
      toJenaClassRec((OClass)subClassIter.next(), jenaModel, jenaClass);
    }
  }

  protected Set convertoToGateClasses(Iterator jenaIterator) {
    Set result = new HashSet();
    while(jenaIterator.hasNext()) {
      com.hp.hpl.jena.rdf.model.Resource aResource = (com.hp.hpl.jena.rdf.model.Resource)jenaIterator
              .next();
      if(aResource.canAs(OntClass.class)) {
        OntClass aJenaClass = (OntClass)aResource.as(OntClass.class);
        OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
        if(aGateClass == null) {
          Err.prln("WARNING: class \"" + aJenaClass.getLocalName()
                  + "\" not found!");
        } else {
          result.add(aGateClass);
        }
      }
    }
    return result;
  }

  /**
   * Converts a Jena class and all its subclasses to GATE classes and adds them
   * to this ontology.
   * 
   * @param aClass
   *          the Class in Jena model
   * @param parent
   *          the parent class for the class to be added in this ontology.
   */
  protected void toGateClassRec(OntClass jenaClass, OClass parent) {
    // create the class
    String uri = jenaClass.getURI();
    String name = jenaClass.getLocalName();
    String comment = jenaClass.getComment(language);
    String classLabel = jenaClass.getLabel(language);
    // check whether the class exists already
    OClass aClass = (OClass)getClassByName(name);
    if(aClass == null) {
      // if class not found, create a new one
      aClass = new OClassImpl(uri, name, comment, this);
      if(uri != null) aClass.setURI(uri);
      addClass(aClass);
      if(classLabel != null) aClass.addPropertyValue("label", classLabel);
    }
    // create the hierachy
    if(parent != null) {
      aClass.addSuperClass(parent);
      parent.addSubClass(aClass);
    }
    // make the recursive calls
    ExtendedIterator subClassIter = jenaClass.listSubClasses(true);
    while(subClassIter.hasNext()) {
      OntClass subJenaClass = (OntClass)subClassIter.next();
      toGateClassRec(subJenaClass, aClass);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.AbstractLanguageResource#cleanup()
   */
  public void cleanup() {
    super.cleanup();
    Iterator instIter = new ArrayList(getInstances()).iterator();
    while(instIter.hasNext()) {
      removeInstance((OInstance)instIter.next());
    }
    Iterator classIter = new ArrayList(getClasses()).iterator();
    while(classIter.hasNext()) {
      removeClass((TClass)classIter.next());
    }
    propertyDefinitionSet.clear();
  }

  /**
   * @return Returns the language.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language
   *          The language to set.
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return Returns the damlFileURL.
   */
  public URL getDamlFileURL() {
    return damlFileURL;
  }

  /**
   * @param damlFileURL
   *          The damlFileURL to set.
   */
  public void setDamlFileURL(URL damlFileURL) {
    this.damlFileURL = damlFileURL;
  }

  /**
   * @return Returns the owlDlFileURL.
   */
  public URL getOwlDlFileURL() {
    return owlDlFileURL;
  }

  /**
   * @param owlDlFileURL
   *          The owlDlFileURL to set.
   */
  public void setOwlDlFileURL(URL owlDlFileURL) {
    this.owlDlFileURL = owlDlFileURL;
  }

  /**
   * @return Returns the owlFullFileURL.
   */
  public URL getOwlFullFileURL() {
    return owlFullFileURL;
  }

  /**
   * @param owlFullFileURL
   *          The owlFullFileURL to set.
   */
  public void setOwlFullFileURL(URL owlFullFileURL) {
    this.owlFullFileURL = owlFullFileURL;
  }

  /**
   * @return Returns the owlLiteFileURL.
   */
  public URL getOwlLiteFileURL() {
    return owlLiteFileURL;
  }

  /**
   * @param owlLiteFileURL
   *          The owlLiteFileURL to set.
   */
  public void setOwlLiteFileURL(URL owlLiteFileURL) {
    this.owlLiteFileURL = owlLiteFileURL;
  }

  /**
   * @return Returns the rdfsFileURL.
   */
  public URL getRdfsFileURL() {
    return rdfsFileURL;
  }

  /**
   * @param rdfsFileURL
   *          The rdfsFileURL to set.
   */
  public void setRdfsFileURL(URL rdfsFileURL) {
    this.rdfsFileURL = rdfsFileURL;
  }

  protected class LoadOntologyDataAction extends AbstractAction {
    public LoadOntologyDataAction(String name, String description,
            int ontologyType) {
      super(name);
      putValue(SHORT_DESCRIPTION, description);
      this.ontologyType = ontologyType;
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser fileChooser = MainFrame.getFileChooser();
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setMultiSelectionEnabled(false);
          if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
              MainFrame.lockGUI("Loading model...");
              load(file.toURL(), ontologyType);
            } catch(Exception e) {
              JOptionPane.showMessageDialog(null, "Error!\n" + e.toString(),
                      "GATE", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace(Err.getPrintWriter());
            } finally {
              MainFrame.unlockGUI();
            }
          }
        }
      };
      Thread thread = new Thread(runnable, "Ontology Loader");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }

    protected int ontologyType;
  }
  protected class SaveOntologyAction extends AbstractAction {
    public SaveOntologyAction(String name, String description, int ontologyType) {
      super(name);
      putValue(SHORT_DESCRIPTION, description);
      this.ontologyType = ontologyType;
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser fileChooser = MainFrame.getFileChooser();
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setMultiSelectionEnabled(false);
          if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
              MainFrame.lockGUI("Saving model...");
              save(file, ontologyType);
            } catch(Exception e) {
              JOptionPane.showMessageDialog(null, "Error!\n" + e.toString(),
                      "GATE", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace(Err.getPrintWriter());
            } finally {
              MainFrame.unlockGUI();
            }
          }
        }
      };
      Thread thread = new Thread(runnable, "Ontology Loader");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }

    protected int ontologyType;
  }
  protected class CleanUpAction extends AbstractAction {
    public CleanUpAction() {
      super("Clean ontology");
      putValue(SHORT_DESCRIPTION, "Deletes all data rom this ontology");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          try {
            MainFrame.lockGUI("Cleaning up...");
            cleanup();
          } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error!\n" + e.toString(),
                    "GATE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(Err.getPrintWriter());
          } finally {
            MainFrame.unlockGUI();
          }
        }
      };
      Thread thread = new Thread(runnable, "Ontology Cleaner");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  protected URL owlLiteFileURL;
  protected URL owlDlFileURL;
  protected URL owlFullFileURL;
  protected URL rdfsFileURL;
  protected URL damlFileURL;
  protected String language;
  protected String defaultNameSpace;
  protected List actionsList;
  /**
   * Constant for Owl-Lite ontology type.
   */
  public static final int OWL_LITE = 0;
  /**
   * Constant for Owl-DL ontology type.
   */
  public static final int OWL_DL = 1;
  /**
   * Constant for Owl-Full ontology type.
   */
  public static final int OWL_FULL = 2;
  /**
   * Constant for RDF-S ontology type.
   */
  public static final int RDFS = 3;
  /**
   * Constant for DAML ontology type.
   */
  public static final int DAML = 13;

  /**
   * @return Returns the defaultNameSpace.
   */
  public String getDefaultNameSpace() {
    return defaultNameSpace;
  }

  /**
   * @param defaultNameSpace
   *          The defaultNameSpace to set.
   */
  public void setDefaultNameSpace(String defaultNameSpace) {
    this.defaultNameSpace = defaultNameSpace;
  }
}
