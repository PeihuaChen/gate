package gate.creole.ontology.jena;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.creole.ontology.ObjectProperty;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.util.Err;
import gate.util.Out;
import java.awt.event.ActionEvent;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.ontotext.gate.ontology.OntologyImpl;

/**
 * An implementation for GATE Ontologies based on Jena2
 * 
 * @author Valentin Tablan
 * 
 */
public class JenaOntologyImpl extends OntologyImpl implements ActionsPublisher{
  public JenaOntologyImpl() {
    actionsList = new ArrayList();
    actionsList.add(new LoadOntologyDataAction("Load OWL-Lite data", 
            "Reads OWL-Lite data from a file and populates the ontology.", 
            OWL_LITE));
    actionsList.add(new LoadOntologyDataAction("Load OWL-DL data", 
            "Reads OWL-DL data from a file and populates the ontology.", 
            OWL_DL));
    actionsList.add(new LoadOntologyDataAction("Load OWL-Full data", 
            "Reads OWL-Full data from a file and populates the ontology.", 
            OWL_FULL));
    actionsList.add(new LoadOntologyDataAction("Load RDF(S) data", 
            "Reads RDF(S) data from a file and populates the ontology.", 
            RDFS));
    actionsList.add(new LoadOntologyDataAction("Load DAML data", 
            "Reads DAML data from a file and populates the ontology.", 
            DAML));
    actionsList.add(null);
    actionsList.add(new CleanUpAction());
  }
  
  public Resource init() throws ResourceInstantiationException {
    load();
    return this;
  }

  /* (non-Javadoc)
   * @see gate.gui.ActionsPublisher#getActions()
   */
  public List getActions() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Loads the ontology from an external file.
   */
  public void load() throws ResourceInstantiationException {
    // find what type of ontolgoy are we loading
    URL inputURL = null;
    int ontologyType = -1;
    if(owlLiteFileURL != null){
      inputURL = owlLiteFileURL;
      ontologyType = OWL_LITE;
    }else if(owlDlFileURL != null){
      inputURL = owlDlFileURL;
      ontologyType = OWL_DL;
    }else if(owlFullFileURL != null){
      inputURL = owlFullFileURL;
      ontologyType = OWL_FULL;
    }else if(rdfsFileURL != null){
      inputURL = rdfsFileURL;
      ontologyType = RDFS;
    }else if(damlFileURL != null){
      inputURL = damlFileURL;
      ontologyType = DAML;
    }else{
      // no input file provided - create an empty OWL Lite ontology
      inputURL = null;
      ontologyType = OWL_LITE;
    }
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
    if(inputURL != null){
      try{
        jenaModel.read(new BufferedInputStream(inputURL.openStream()), "");
      }catch(IOException ioe){
        throw new ResourceInstantiationException(ioe);
      }
    }
    // convert the Jena model into a GATE ontology
    // create the class hierarchy
    ExtendedIterator topClassIter = jenaModel.listHierarchyRootClasses();
    while(topClassIter.hasNext()){
      OntClass aTopClass = (OntClass)topClassIter.next();
      addClassRec(aTopClass, null);
    }
    // create the properties
    ExtendedIterator propIter = jenaModel.listObjectProperties();
    propwhile: while(propIter.hasNext()){
      OntProperty aJenaProperty = (OntProperty)propIter.next();
      String propertyName = aJenaProperty.getLocalName();
      String propertyComment = aJenaProperty.getComment(language);
      Set gateDomain = buildPropertyDomain(jenaModel, aJenaProperty);
      Set gateRange = buildPropertyRange(jenaModel, aJenaProperty);
      // add the property
      ObjectProperty theProp = null;
      if(aJenaProperty.isTransitiveProperty()){
        theProp = addTransitiveProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      }else if(aJenaProperty.isSymmetricProperty()){
        theProp = addSymmetricProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      }else{
        theProp = addObjectProperty(propertyName, propertyComment, gateDomain,
                gateRange);
      }
      theProp.setURI(aJenaProperty.getURI());
      theProp.setFunctional(aJenaProperty.isFunctionalProperty());
      theProp.setInverseFunctional(aJenaProperty.isInverseFunctionalProperty());
    }
    // create the properties hierarchy
    propIter = jenaModel.listObjectProperties();
    while(propIter.hasNext()){
      OntProperty aJenaProp = (OntProperty)propIter.next();
      gate.creole.ontology.Property aGateProp = getPropertyDefinitionByName(aJenaProp
              .getLocalName());
      for(Iterator superIter = aJenaProp.listSuperProperties(); superIter
              .hasNext();){
        OntProperty aJenaSuperProp = (OntProperty)superIter.next();
        gate.creole.ontology.Property aGateSuperProp = getPropertyDefinitionByName(aJenaSuperProp
                .getLocalName());
        if(aGateSuperProp == null){
          Err.prln("WARNING: Unknown super property \""
                  + aJenaSuperProp.getLocalName() + "\" for property \""
                  + aJenaProp.getLocalName() + "\"!");
        }else{
          aGateProp.addSuperProperty(aGateSuperProp);
          aGateSuperProp.addSubProperty(aGateProp);
        }
      }
    }
    // add the datatype properties
    propIter = jenaModel.listDatatypeProperties();
    while(propIter.hasNext()){
      DatatypeProperty aJenaProperty = (DatatypeProperty)propIter.next();
      String propertyName = aJenaProperty.getLocalName();
      String propertyComment = aJenaProperty.getComment(language);
      Set gateDomain = buildPropertyDomain(jenaModel, aJenaProperty);
      Iterator rangeIter = aJenaProperty.listRange();
      Set range = new HashSet();
      while(rangeIter.hasNext())
        range.add(rangeIter.next());
      if(!range.isEmpty()){
        Out.prln("WARNING: Support for datatype properties ranges is not "
                + "implemented!\nRange " + range.toString() + " for property "
                + aJenaProperty.getLocalName() + " has been ignored!");
      }
      gate.creole.ontology.DatatypeProperty theProp = addDatatypeProperty(
              propertyName, propertyComment, gateDomain, Object.class);
      theProp.setFunctional(aJenaProperty.isFunctionalProperty());
      theProp.setInverseFunctional(aJenaProperty.isInverseFunctionalProperty());
      theProp.setURI(aJenaProperty.getURI());
    }
  }

  protected Set buildPropertyDomain(OntModel jenaModel, OntProperty jenaProperty)
          throws ResourceInstantiationException {
    Set domain = new HashSet();
    // add the direct domain entries
    for(Iterator domIter = jenaProperty.listDomain(); domIter.hasNext();)
      domain.add(domIter.next());
    // convert the domain to GATE classes
    Set gateDomain = new HashSet();
    for(Iterator domIter = domain.iterator(); domIter.hasNext();){
      OntClass aJenaClass = (OntClass)domIter.next();
      OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
      if(aGateClass == null){
        throw new ResourceInstantiationException("Class not found "
                + aJenaClass.getLocalName());
      }else{
        gateDomain.add(aGateClass);
      }
    }
    // reduce the domain to the most specific classes
    reduceToMostSpecificClasses(gateDomain);
    return gateDomain;
  }

  protected Set buildPropertyRange(OntModel jenaModel, OntProperty jenaProperty)
          throws ResourceInstantiationException {
    Set range = new HashSet();
    // add the direct domain entries
    for(Iterator rangeIter = jenaProperty.listRange(); rangeIter.hasNext();)
      range.add(rangeIter.next());
    // convert the domain to GATE classes
    Set gateRange = new HashSet();
    for(Iterator rangeIter = range.iterator(); rangeIter.hasNext();){
      OntClass aJenaClass = (OntClass)rangeIter.next();
      OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
      if(aGateClass == null){
        throw new ResourceInstantiationException("Class not found "
                + aJenaClass.getLocalName());
      }else{
        gateRange.add(aGateClass);
      }
    }
    // reduce the range to the most specific classes
    reduceToMostSpecificClasses(gateRange);
    return gateRange;
  }

  /**
   * Adds a class and all its subclasses recursively.
   * 
   * @param aClass
   *          the Class in Jena model
   * @param parent
   *          the parent class for the class to be added in this ontology.
   */
  protected void addClassRec(OntClass jenaClass, OClass parent) {
    // create the class
    String uri = jenaClass.getURI();
    String name = jenaClass.getLocalName();
    String comment = jenaClass.getComment(language);
    // check whether the class exists already
    OClass aClass = (OClass)getClassByName(name);
    if(aClass == null){
      // if class not found, create a new one
      aClass = new OClassImpl(uri, name, comment, this);
      aClass.setURI(uri);
      addClass(aClass);
    }
    // create the hierachy
    if(parent != null){
      aClass.addSuperClass(parent);
      parent.addSubClass(aClass);
    }
    // make the recursive calls
    ExtendedIterator subClassIter = jenaClass.listSubClasses(true);
    while(subClassIter.hasNext()){
      OntClass subJenaClass = (OntClass)subClassIter.next();
      addClassRec(subJenaClass, aClass);
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
    while(instIter.hasNext()){
      removeInstance((OInstance)instIter.next());
    }
    
    Iterator classIter = new ArrayList(getClasses()).iterator();
    while(classIter.hasNext()){
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

  protected class LoadOntologyDataAction extends AbstractAction{
    public LoadOntologyDataAction(String name, String description, 
            int ontologyType) {
      super(name);
      putValue(SHORT_DESCRIPTION, description);
      this.ontologyType = ontologyType;
    }
    
    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          JFileChooser fileChooser = MainFrame.getFileChooser();
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setMultiSelectionEnabled(false);
          if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try{
              MainFrame.lockGUI("Loading model...");
              load(file.toURL(), ontologyType);
            }catch(Exception e){
              JOptionPane.showMessageDialog(null,
                              "Error!\n"+
                               e.toString(),
                               "GATE", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace(Err.getPrintWriter());
            }finally{
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
  
  protected class CleanUpAction extends AbstractAction{
    public CleanUpAction() {
      super("Clean ontology");
      putValue(SHORT_DESCRIPTION, "Deletes all data rom this ontology");
    }
    
    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          try{
            MainFrame.lockGUI("Cleaning up...");
            cleanup();
          }catch(Exception e){
            JOptionPane.showMessageDialog(null,
                            "Error!\n"+
                             e.toString(),
                             "GATE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(Err.getPrintWriter());
          }finally{
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
  
  protected List actionsList;
  /**
   * Constant for Owl-Lite ontology type.
   */
  protected static final int OWL_LITE = 0;
  /**
   * Constant for Owl-DL ontology type.
   */
  protected static final int OWL_DL = 1;
  /**
   * Constant for Owl-Full ontology type.
   */
  protected static final int OWL_FULL = 2;
  /**
   * Constant for RDF-S ontology type.
   */
  protected static final int RDFS = 3;
  /**
   * Constant for DAML ontology type.
   */
  protected static final int DAML = 13;
}
