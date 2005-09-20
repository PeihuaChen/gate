package gate.creole.ontology.jena;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.creole.ontology.ObjectProperty;
import gate.util.Err;
import gate.util.Out;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
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
public class JenaOntologyImpl extends OntologyImpl {
  public Resource init() throws ResourceInstantiationException {
    if(owlFileURL == null)
      throw new ResourceInstantiationException("No input file specified!");
    if(language != null && language.length() == 0) language = null;
    load();
    return this;
  }
  /**
   * Loads the ontology from an external file.
   */
  public void load() throws ResourceInstantiationException {
    // build the Jena model
    // we need some type of inference here in order to get all triples (i.e.
    // including the inferred ones)
    OntModel jenaModel = ModelFactory
            .createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
    try{
      jenaModel.read(new BufferedInputStream(owlFileURL.openStream()), "");
    }catch(IOException ioe){
      throw new ResourceInstantiationException(ioe);
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
      
      Set domain = new HashSet();
      Set range = new HashSet();
      // add the direct domain/range entries
      for(Iterator domIter = aJenaProperty.listDomain(); domIter.hasNext();)
        domain.add(domIter.next());
      for(Iterator rangIter = aJenaProperty.listRange(); rangIter.hasNext();)
        range.add(rangIter.next());
      // add the values from super-properties
      Iterator superJenaPropIter = aJenaProperty.listSuperProperties();
      while(superJenaPropIter.hasNext()){
        OntProperty aJenaSuperProp = (OntProperty)superJenaPropIter.next();
        for(Iterator domIter = aJenaSuperProp.listDomain(); domIter.hasNext();)
          domain.add(domIter.next());
        for(Iterator rangIter = aJenaSuperProp.listRange(); rangIter.hasNext();)
          range.add(rangIter.next());
      }
      //convert the domain to GATE classes
      Set gateDomain = new HashSet();
      for(Iterator domIter = domain.iterator(); domIter.hasNext();) {
        OntClass aJenaClass = (OntClass)domIter.next();
        OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
        if(aGateClass == null) {
          throw new ResourceInstantiationException("Class not found " +
                  aJenaClass.getLocalName());
        }else {
          gateDomain.add(aGateClass);
        }
      }
      //convert the range to GATE classes
      Set gateRange = new HashSet();
      for(Iterator rangIter = range.iterator(); rangIter.hasNext();) {
        OntClass aJenaClass = (OntClass)rangIter.next();
        OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
        if(aGateClass == null) {
          throw new ResourceInstantiationException("Class not found " +
                  aJenaClass.getLocalName());
        }else {
          gateRange.add(aGateClass);
        }
      }
      
      //reduce the domain to the most specific classes
      eliminateMoreGenericClasses(gateDomain);

      //reduce the range to the most specific classes
      eliminateMoreGenericClasses(gateRange);
      
      // add the property
      ObjectProperty theProp = null;
      if(aJenaProperty.isTransitiveProperty()) {
        theProp = addTransitiveProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      }else if(aJenaProperty.isSymmetricProperty()) {
        theProp = addSymmetricProperty(propertyName, propertyComment,
                gateDomain, gateRange);
      }else {
        theProp = addObjectProperty(propertyName, propertyComment,
              gateDomain, gateRange);
      }
      theProp.setURI(aJenaProperty.getURI());
      theProp.setFunctional(aJenaProperty.isFunctionalProperty());
      theProp.setInverseFunctional(aJenaProperty.isInverseFunctionalProperty());
    }
    
    //create the properties hierarchy
    propIter = jenaModel.listObjectProperties();
    while(propIter.hasNext()) {
      OntProperty aJenaProp = (OntProperty)propIter.next();
      gate.creole.ontology.Property aGateProp = getPropertyDefinitionByName(aJenaProp.getLocalName());
      for(Iterator superIter = aJenaProp.listSuperProperties(); 
          superIter.hasNext();) {
        OntProperty aJenaSuperProp = (OntProperty)superIter.next();
        gate.creole.ontology.Property aGateSuperProp = 
          getPropertyDefinitionByName(aJenaSuperProp.getLocalName());
        if(aGateSuperProp == null) {
          Err.prln("WARNING: Unknown super property \"" + 
                  aJenaSuperProp.getLocalName() + "\" for property \"" +
                  aJenaProp.getLocalName() + "\"!");
        }else {
          aGateProp.addSuperProperty(aGateSuperProp);
          aGateSuperProp.addSubProperty(aGateProp);          
        }
      }
    }
    
    //add the datatype properties
    propIter = jenaModel.listDatatypeProperties();
    while(propIter.hasNext()){
      DatatypeProperty aProp = (DatatypeProperty)propIter.next();
      Out.pr(aProp.getLocalName() + ":");
      Iterator domIter = aProp.listDomain();
      while(domIter.hasNext()){
        Out.pr(((OntResource)domIter.next()).getLocalName() + ", ");
      }
      Out.pr(" -> ");
      Iterator rangIter = aProp.listRange();
      while(rangIter.hasNext()){
        OntResource aRangeItem = (OntResource)rangIter.next(); 
        Out.pr("(" + aRangeItem.getClass().getName() + ")" + 
                aRangeItem.toString() + ", ");
      }
      Out.prln();
    }
    
  }
  
  protected Set buildPropertyDomain(OntModel jenaModel, 
          OntProperty jenaProperty) throws ResourceInstantiationException{
    Set domain = new HashSet();
    // add the direct domain entries
    for(Iterator domIter = jenaProperty.listDomain(); domIter.hasNext();)
      domain.add(domIter.next());
    //convert the domain to GATE classes
    Set gateDomain = new HashSet();
    for(Iterator domIter = domain.iterator(); domIter.hasNext();) {
      OntClass aJenaClass = (OntClass)domIter.next();
      OClass aGateClass = (OClass)getClassByName(aJenaClass.getLocalName());
      if(aGateClass == null) {
        throw new ResourceInstantiationException("Class not found " +
                aJenaClass.getLocalName());
      }else {
        gateDomain.add(aGateClass);
      }
    }
    
    //reduce the domain to the most specific classes
    eliminateMoreGenericClasses(gateDomain);
    return gateDomain;
  }
  
  /**
   * Eliminates the more general classes from a set, keeping only the most 
   * specific ones. The changes are made to the set provided as a parameter.
   * @param classSet a set of {@link OClass} objects.
   */
  protected void eliminateMoreGenericClasses(Set classSet) {
    Map superClassesForClass = new HashMap();
    for(Iterator classIter = classSet.iterator(); classIter.hasNext();) {
      OClass aGateClass = (OClass)classIter.next();
      superClassesForClass.put(aGateClass, 
              aGateClass.getSuperClasses(OClass.TRANSITIVE_CLOSURE));
    }
    Set classesToRemove = new HashSet();
    List resultList = new ArrayList(classSet);
    for(int i = 0; i < resultList.size() -1; i++)
      for(int j = i + 1; j< resultList.size(); j++) {
        OClass aClass = (OClass)resultList.get(i);
        OClass anotherClass = (OClass)resultList.get(j);
        if(((Set)superClassesForClass.get(aClass)).contains(anotherClass))
          classesToRemove.add(anotherClass);
        else if(((Set)superClassesForClass.get(anotherClass)).contains(aClass))
          classesToRemove.add(aClass);
      }
    classSet.removeAll(classesToRemove);
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
    // TODO Auto-generated method stub
    super.cleanup();
  }
  /**
   * @return Returns the owlFileURL.
   */
  public URL getOwlFileURL() {
    return owlFileURL;
  }
  /**
   * @param owlFileURL
   *          The owlFileURL to set.
   */
  public void setOwlFileURL(URL owlFileURL) {
    this.owlFileURL = owlFileURL;
  }

  protected URL owlFileURL;
  protected String language;

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
}
