/*
 * OntologyImpl.java
 * Copyright:    Copyright (c) 2002, OntoText Lab.
 * Company:      OntoText Lab.
 * borislav popov 03/2002 */

package com.ontotext.gate.ontology;

import gate.creole.ontology.*;
import java.util.List;
import java.net.URL;
import gate.creole.ResourceInstantiationException;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import gate.DataStore;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.LanguageResource;
import gate.FeatureMap;
import gate.creole.ontology.Ontology;
import java.util.*;
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.arp.*;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.Files;
import gate.util.Out;

public class DAMLKnowledgeBaseImpl extends OntologyImpl  {

  /** DEBUG FLAG */
  private static final boolean DEBUG = false;
  private DAMLModel model = null;

  public void load() throws gate.creole.ResourceInstantiationException {
    model = loadAndGetModel();
    if (model == null || !model.getLoadSuccessful())
      return;

    //now load the DAML instances into the KB
    //currently I do not check for sameIndividualAs, this may have to change
    //if future applications require it
    DAMLInstance instance;
    Iterator ri = model.listDAMLInstances();
    while (ri.hasNext()) {
      Object inst = ri.next();
      if (! (inst instanceof DAMLInstance))
        continue;
      instance = (DAMLInstance) inst;

      Iterator classTypes = instance.getRDFTypes(false);
      TClass theClass = null;
      //take only the first class as a type for this instance
      //because we only support instances that belong to one
      //class. If an instance needs to belong to more than 1 class,
      //then make a class, which is a sub-class of those classes,
      //then add the instance to it
      while (classTypes.hasNext() && theClass == null) {
        String localName =
            ((com.hp.hpl.mesa.rdf.jena.model.Resource)classTypes.next()
             ).getLocalName();
        if (localName != null && this.containsClassByName(localName))
          theClass = this.getClassByName(localName);
      }//while
      if (theClass != null && instance.getLocalName() != null) {
        addInstance(instance.getLocalName(), (OClass) theClass);
        if (DEBUG)
          System.out.println("Loaded instance: " + instance.getLocalName() +
                             "in class: " + theClass.getName());
      }
    } //while

  }//load

  /** Introduced a protected method that retuns the model, so it can be
   * used in a subclass to get more data before discarding it
   */
  protected DAMLModel loadAndGetModel() throws ResourceInstantiationException{

    try {
      model = new DAMLModelImpl();


      /*get the actual url instead of gate:path*/
      URL curl = getURL();
      if ( null == curl )
        throw new ResourceInstantiationException(
        "Unable to load ontology because url is not set. (null)");

      if (-1 != curl.getProtocol().indexOf("gate")) {
        curl = gate.util.protocols.gate.Handler.class.getResource(
                      Files.getResourcePath() + getURL().getPath()
                    );
      } // if gate:path url

      model.read(curl.toString());
      DAMLVocabulary voc = DAML_OIL.getInstance();

      if (! model.getLoadSuccessful()) {
        /** if the url does not exist : store the model first*/
        File f = new File(curl.getFile());
        if (!f.exists()) {
          this.store();
        } // if file doesn't exist

        //try again
        model.read(curl.toString());

        if ( !model.getLoadSuccessful()) {
          throw new ResourceInstantiationException(
          "loading of ontology failed. url="+curl.toString());
        }
      }// if not sucessful load;

      if (model.getLoadSuccessful()) {
        /* load classes */
        /* on the first iteration the classes are created */
        DAMLClass clas;
        ResIterator ri = model.listSubjects();
        Resource res;
        while (ri.hasNext()) {
          res = ri.next();
          if ( (res instanceof DAMLClass) && ! (res instanceof DAMLRestriction) ) {
            clas = (DAMLClass)res;
            String comment = null;
            if (null != clas.prop_comment().getValue()) {
              comment = clas.prop_comment().getValue().getString();
            }

            /* avoid anonyomouses and restrictions */
            if ( null!=clas.getLocalName()) {
              TClass oClass = this.createClass(
                    clas.getLocalName(),
                    comment);
              /*currently only classes from the same ontology are expected*/
              /*the problem was caused by the gate:/.... urls. when reading a
              damlclass the urls are unpacked to e.g. file:///d:/.... which is a
              change of the uri : this is the reason as uri of the class to be set:
              ontology base uri + # + classURIafter#*/
              String localURI = clas.getURI();
//              if ( -1 == localURI.indexOf('#') ) {
              oClass.setURI(localURI);
//              }  else {
//                oClass.setURI(localURI.substring(
//                  localURI.indexOf('#')+1
//                ));
//              } // else
            } // if not null name - anonymous
          } else {
            if (res instanceof DAMLOntologyImpl) {
              /* only one ontology expected to be loaded */
              DAMLOntologyImpl onto = (DAMLOntologyImpl) res;


              try {
                Statement s = onto.getProperty(RDFS.label);
                this.setLabel(s.getObject().toString());
              } catch (RDFException rdfex) {
                // this means that there is no label property :
                // so just set it to an empty string
                this.setLabel("");
              }

              if ( null != onto.prop_comment().getValue()) {
                this.setComment(onto.prop_comment().getValue().toString());
              }

              if (null != onto.prop_versionInfo()) {
                LiteralAccessor la = onto.prop_versionInfo();
                NodeIterator niValues = la.getValues();
                if (niValues.hasNext()) {
                  this.setVersion(niValues.next().toString());
                }
              } // if version


              {/*set the pre # part as uri and the post # part as id*/
                String luri = onto.getURI();
                if (null == luri) {
                  this.setSourceURI("");
                } else {
                  int ix = luri.indexOf('#');
                  if (-1 != ix) {
                    this.setSourceURI(luri.substring(0,ix));
                    this.setId(luri.substring(ix+1));
                  } else {
                    this.setSourceURI(onto.getURI());
                  } // else
                } // else
              } // block
            }  // if ontology
          } // else

        }// while nodes


        /*append the base uri where needed to the class uris*/
        Iterator cli = this.getClasses().iterator();
        while(cli.hasNext()) {
          OClass ocl = (OClass)cli.next();
          if ( -1 == ocl.getURI().indexOf('#')) {
            ocl.setURI(this.getSourceURI()+'#'+ocl.getURI());
          }
        } // while

        /* infer subclassof superclassof relations */
        ri = model.listSubjects();
        while (ri.hasNext()) {
          res = ri.next();
          if ( (res instanceof DAMLClass) && ! (res instanceof DAMLRestriction) ) {

            clas = (DAMLClass) res;
            if ( null == clas.getLocalName()) {
              continue;
            }
            TClass oc = this.getClassByName(clas.getLocalName());
            if ( null == oc ) {
              throw new InvalidFormatException(
              curl,"class not found by name = "+clas.getLocalName());
            }

            com.hp.hpl.mesa.rdf.jena.model.Property propSCO = RDFS.subClassOf;
            StmtIterator subi = clas.listProperties(propSCO);
            while(subi.hasNext()) {
              Statement damlSub = (Statement)subi.next();
              String obj = damlSub.getObject().toString();
              /*euristic to remove < > from the obj string */
              if (obj.charAt(0) == '<' && obj.charAt(obj.length()-1) == '>') {
                obj=obj.substring(1,obj.length()-1);
              }
              obj = obj.substring(obj.lastIndexOf("#")+1);
              TClass sub = this.getClassByName(obj);

              if ( null != sub )
                oc.addSuperClass(sub);

            } //while sub classes
          } //
        } // while nodes

//        if (DEBUG)
//          Out.setPrintWriter(new PrintWriter(new FileOutputStream("err.log"), true));

        //Now read the properties of each class
        Iterator classIter = model.listDAMLClasses();
        while (classIter.hasNext()) {
          DAMLClass theClass = (DAMLClass) classIter.next();
          if ( null == theClass.getLocalName()) {
            continue;
          }

          Iterator propIter =
              theClass.getDefinedProperties(false);
          TClass oc = this.getClassByName(theClass.getLocalName());
          //if this class in the ontology is not a KBClass, we
          //cannot read properties and restrictions
          if (! (oc instanceof OClass) || oc == null)
            continue;

          OClass kbClass = (OClass) oc;

          if (DEBUG) {
            Out.println("==============================================");
            Out.println("Class is: " + theClass.getLocalName());

            Out.println("Properties: ");
            Out.println("----------------");
          }

          HashMap propertiesMap = new HashMap();

          while (propIter.hasNext()) {
            DAMLProperty property = (DAMLProperty) propIter.next();
            if (property.getLocalName() == null)
              continue;
            String propName = property.getLocalName();

            if (DEBUG) {
              Out.println("local name " + property.getLocalName());
              Out.println("domains ");
            }
            //look through all domains for this property and
            //only if we find one which is the same as our class
            //then we're going to add it to the GATE ontology model
            boolean toAdd = true;
            Iterator domainIter = property.getDomainClasses();
            while (domainIter.hasNext()) {
              DAMLClass theDomain = (DAMLClass) domainIter.next();
              if (DEBUG)
                Out.println(theDomain.getLocalName());
              if (theDomain == null)
                continue;
              if (kbClass.getName().equals(theDomain.getLocalName()))
                toAdd = true;
              else
                toAdd = false;
            }
            if (!toAdd)
              continue;

            if (DEBUG)
              Out.println("range ");

            addPropertyDefinition(property, propName, kbClass, propertiesMap);

            if (DEBUG) Out.println("superproperties ");
            Iterator superPropIter = property.getSuperProperties(true);
            while (superPropIter.hasNext()) {
              DAMLProperty superProp = (DAMLProperty) superPropIter.next();
              if (superProp == null || superProp.getLocalName() == null)
                continue;
              if (DEBUG) Out.println(superProp.getLocalName());
              ((gate.creole.ontology.Property) propertiesMap.get(propName)).
                                  setSubPropertyOf(superProp.getLocalName());
            }

          }//while over the properties

          //add these properties as defined in the ontology
          addPropertyDefinitions(propertiesMap);

          if (DEBUG) {
            Out.println("Restrictions: ");
            Out.println("----------------");
          }

          Iterator superIter = theClass.getSuperClasses();
          while (superIter.hasNext()) {
            DAMLClass superClass = (DAMLClass) superIter.next();
            if (superClass.isRestriction()) {
              DAMLRestriction restriction = (DAMLRestriction) superClass;
              if (DEBUG) {
                Out.println("onProperty "
                  + restriction.prop_onProperty().getDAMLValue().getLocalName());
                Out.println("toClass "
                  + restriction.prop_toClass().getDAMLValue());
                Out.println("hasClass "
                  + restriction.prop_hasClass().getDAMLValue());
                Out.println("hasClassQ "
                  + restriction.prop_hasClassQ().getDAMLValue());
                Out.println("cardinality "
                  + restriction.prop_cardinality().getDAMLValue());
                Out.println("cardinalityQ "
                  + restriction.prop_cardinalityQ().getDAMLValue());
              }
              String propName =
                restriction.prop_onProperty().getDAMLValue().getLocalName();
              if (propName == null)
                continue;
              String rangeName = null;
              if (restriction.prop_toClass().getDAMLValue() != null) {
                rangeName = restriction.prop_toClass().getDAMLValue().getLocalName();
              } else if (restriction.prop_hasClass().getDAMLValue() != null) {
                rangeName = restriction.prop_hasClass().getDAMLValue().getLocalName();
              } else if (restriction.prop_hasClassQ().getDAMLValue() != null) {
                rangeName = restriction.prop_hasClassQ().getDAMLValue().getLocalName();
              }


              gate.creole.ontology.Property theNewProperty = null;
              gate.creole.ontology.Property thePropDefinition =
                  (gate.creole.ontology.Property) propertiesMap.get(propName);
              if (thePropDefinition == null) {
                //check if this property is defined for a superclass
                thePropDefinition =
                  searchSuperClasses(propName, theClass, propertiesMap);
              }
              //we haven't seen this property defined for this class
              if (thePropDefinition == null) {
                Out.println("Warning: Ignoring restriction on property " + propName +
                  " because cannot find such property defined for this class");
              } else {
                //process the properties differently depending on their type
                if (thePropDefinition instanceof ObjectProperty) {
                  TClass rangeClass = this.getClassByName(rangeName);

                  if (rangeClass == null || !(rangeClass instanceof OClass))
                    rangeClass = (OClass)((ObjectProperty) thePropDefinition).getRange();

                  theNewProperty = this.addObjectProperty(
                                          thePropDefinition.getName(),
                                          kbClass,
                                          (OClass) rangeClass);
                } else {
                  theNewProperty = this.addDatatypeProperty(
                                          thePropDefinition.getName(),
                                          kbClass,
                                          rangeName);
                }
              }//if


            }//if restriction
          } //loop through the restrictions

        } //while loop through the classes

      } // if model.getloadsucessful

      /**debug*/
      if (DEBUG) {
        Out.println("Property definitions for the ontology");
        Set propertyDefs = this.getPropertyDefinitions();
        if (propertyDefs != null ) {
          Iterator iter = propertyDefs.iterator();
          while (iter.hasNext())
            Out.println( iter.next().toString());
        }

        Out.println("Classes: ");
        Iterator ic = this.getClasses().iterator();
        while (ic.hasNext()) {
          OClass cl = (OClass)ic.next();
          Out.println(" ");
          Out.println(" " +cl+ " [direct sub classes = "+
          cl.getSubClasses(OClass.DIRECT_CLOSURE).size()+"] "+
          "[transitive sub classes = "+
          cl.getSubClasses(OClass.TRANSITIVE_CLOSURE).size()+"]" +
           "[direct super classes = " +
           cl.getSuperClasses(OClass.DIRECT_CLOSURE).size() + " ] " +
           "[transitive super classes = " +
           cl.getSuperClasses(OClass.TRANSITIVE_CLOSURE).size() + "]");

          if (cl instanceof OClass) {
            OClass kbCl = (OClass) cl;
            if (kbCl.getProperties() == null)
              continue;
            Iterator ip = kbCl.getProperties().iterator();
            while (ip.hasNext()) {
              Out.println( ip.next().toString());
            }
          }
        }
      } // debug end


    } catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }

    this.setModified(false);

    return model;
  } // load()

  private gate.creole.ontology.Property addPropertyDefinition(
        DAMLProperty property, String propName, OClass kbClass,
        Map propertiesMap) throws RDFException{
    gate.creole.ontology.Property newProperty = null;
    PropertyAccessor propAcc = property.prop_range();
    if (property instanceof DAMLDatatypeProperty) {
      //do not read the ranges of the datatatype properties, because
      //they are tricky
      newProperty =
        new DatatypePropertyImpl(propName, kbClass, (String) null, this);
      propertiesMap.put(propName, newProperty);
    } else if (propAcc instanceof LiteralAccessor) {
      if (DEBUG) Out.println("Literal accessor");
      //add a DatatypeProperty
      if (((LiteralAccessor)propAcc).getValue() != null) {
        newProperty = new DatatypePropertyImpl(propName, kbClass,
            ((LiteralAccessor)propAcc).getValue().getString(), this);
        propertiesMap.put(propName, newProperty);
        if (DEBUG)
          Out.println(((LiteralAccessor)propAcc).getValue());
      }
    } else if (property instanceof DAMLObjectProperty) {
      if (DEBUG)
        Out.println(propAcc.getDAMLValue());
      if (propAcc.getDAMLValue() == null) {
        if (DEBUG)
          Out.println("Found a null value, adding as null");
        newProperty = new ObjectPropertyImpl(propName, kbClass, null, this);
        propertiesMap.put(propName, newProperty);
      } else {
        if (DEBUG)
          Out.println(propAcc.getDAMLValue().getLocalName());
        TClass rangeClass =
          this.getClassByName(propAcc.getDAMLValue().getLocalName());
        if (rangeClass == null || !(rangeClass instanceof OClass)) {
          newProperty = new ObjectPropertyImpl(propName, kbClass, null, this);
          propertiesMap.put(propName, newProperty);
        } else {
          newProperty =
            new ObjectPropertyImpl(propName, kbClass, (OClass) rangeClass, this);
          propertiesMap.put(propName, newProperty);
        }
      }
    }
    return newProperty;
  }

  private gate.creole.ontology.Property searchSuperClasses(String propName, DAMLClass theClass,
                                        Map propertiesMap)
                    throws RDFException{
    if (DEBUG)
      Out.println("searchSuperClasses called: propName" +
                  propName + "; className " + theClass.getLocalName());
    if (theClass == null)
      return null;
    if (propName == null)
      return null;

    gate.creole.ontology.Property theProperty =
        this.getPropertyDefinitionByName(propName);
    String propDomainName = null;
    //we have not yet loaded this property, so find it among the inherited ones in
    //the DAML model
    if (theProperty == null) {
      if (DEBUG) Out.println("Cannot find property definition" + propName);

      Iterator iter = theClass.getDefinedProperties(true);
      boolean propFound = false;
      while (iter.hasNext() && !propFound) {
        DAMLProperty property = (DAMLProperty) iter.next();
        if (property.getLocalName().equals(propName)
            && property.getDomainClasses().hasNext()) {
          propDomainName =
            ((DAMLClass) property.getDomainClasses().next()).getLocalName();
          propFound = true;
          if (DEBUG)
            Out.println("Domain is: " + propDomainName);
          TClass kbClass = this.getClassByName(propDomainName);
          if (kbClass != null && kbClass instanceof OClass) {
            theProperty =
              this.addPropertyDefinition(
                    property, propName, (OClass) kbClass, propertiesMap);
          }//if
        }//if
      }//while
      if (!propFound) {
        Out.println("Warning: Found restriction on property " + propName +
                    " for class " +
                    theClass.getLocalName() +
                    "which has undefined domain or its domain does not" +
                    "match the given class");
        Iterator propIter = model.listDAMLProperties();
        while (propIter.hasNext()) {
          DAMLProperty property = (DAMLProperty) propIter.next();
          if (property.getLocalName().equals(propName))
            theProperty =
              this.addPropertyDefinition(
                    property, propName, null, propertiesMap);
        }
      }
    } else {
      propDomainName = theProperty.getDomain().getName();
      boolean found = false;
      //search the superclasses of the given DAML class to find if the domain
      //of this property is a superclass of the given class
      Iterator cIter = theClass.getSuperClasses(true);
      while (cIter.hasNext() && !found) {
        if (propDomainName.equals( ((DAMLClass) cIter.next()).getLocalName()))
          found = true;
      }
      if (!found)
        theProperty = null;
    }
    return theProperty;

  }

  private boolean isRelationDefinedOnSuperClass(OClass kbClass, String domainName) {
    boolean result = false;
    Set superClasses = null;
    try {
      superClasses = kbClass.getSuperClasses(OClass.TRANSITIVE_CLOSURE);
    } catch (gate.creole.ontology.NoSuchClosureTypeException ex) {}
    if (superClasses == null || superClasses.isEmpty())
      return false;
    Iterator iter = superClasses.iterator();
    while (iter.hasNext()) {
      if (((OClass) iter.next()).getName().equals(domainName))
        return true;
    }
    return result;
  }
  private void addPropertyDefinitions(HashMap propertiesMap) {
    Iterator iter = propertiesMap.keySet().iterator();
    while (iter.hasNext()) {
      String propertyName = (String) iter.next();
      //we have already added this property definition to the ontology
      if (this.getPropertyDefinitionByName(propertyName) != null)
        continue;
      this.addPropertyDefinition(
          (gate.creole.ontology.Property) propertiesMap.get(propertyName));
    }
  }

  private void addRemainingProperties(HashMap propertiesMap) {
    if (propertiesMap == null || propertiesMap.isEmpty())
      return;

    Iterator iter = propertiesMap.keySet().iterator();
    while (iter.hasNext()) {
      String propertyName = (String) iter.next();
      gate.creole.ontology.Property theProperty =
          (gate.creole.ontology.Property) propertiesMap.get(propertyName);

      //process the properties differently depending on their type
      if (theProperty instanceof ObjectProperty) {
        this.addObjectProperty(theProperty.getName(),
                               theProperty.getDomain(),
                               (OClass)((ObjectProperty)theProperty).getRange());
      } else {
        this.addDatatypeProperty(theProperty.getName(),
                                 theProperty.getDomain(),
                                 (String)((DatatypeProperty)theProperty).getRange());
      }
    }

  }

  public void store() throws gate.creole.ResourceInstantiationException {
    DAMLModel theModel = storeAndGetModel();
    writeModel(theModel);
    System.out.println("No storage of instances and properties implemented yet!");
  }

  protected void writeModel(DAMLModel model) throws ResourceInstantiationException {
    try {
      /* store the model */
      Writer writer = new FileWriter(this.getURL().getFile());
      RDFWriter rdfWriter = model.getWriter("RDF/XML-ABBREV");
      rdfWriter.write(model, writer, this.getSourceURI());
    } catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }
  }


  protected DAMLModel storeAndGetModel()throws ResourceInstantiationException  {
      DAMLModel model = new DAMLModelImpl();

      try {
      /*ensure that all classes will be newly inferred
      later make this through more carefull
      handling of ontology.modified flag*/
      this.setModified(true);

      /* create ontology & get vocabulary */
      DAMLVocabulary voc = DAML_OIL.getInstance();
      com.hp.hpl.jena.daml.DAMLOntology onto =
          new DAMLOntologyImpl(
            this.getSourceURI(),
            this.getLabel(),
            model,
            voc);

      /* create properties necessary for classes & the ontology */

      com.hp.hpl.mesa.rdf.jena.model.Property propVersion =
          model.createProperty(voc.versionInfo().getURI());
      onto.addProperty(propVersion,this.getVersion());

      com.hp.hpl.mesa.rdf.jena.model.Property propLabel =
          model.createProperty(RDFS.label.getURI());
      onto.addProperty(propLabel,this.getLabel());

      com.hp.hpl.mesa.rdf.jena.model.Property propComment =
          model.createProperty(RDFS.comment.getURI());
      onto.addProperty(propComment,this.getComment());


      com.hp.hpl.mesa.rdf.jena.model.Property propSubClassOf =
          model.createProperty(RDFS.subClassOf.getURI());


      /* create classes */
      Iterator classes = this.getClasses().iterator();
      OClass clas;
      DAMLClass dclas;
      while (classes.hasNext())  {
        clas = (OClass) classes.next();

        dclas = model.createDAMLClass(clas.getURI());
        dclas.addProperty(propLabel,clas.getName());
        if (null != clas.getComment()) {
          dclas.addProperty(propComment,clas.getComment());
        }

        /* set super classes */
        Iterator sups = clas.getSuperClasses(clas.DIRECT_CLOSURE).iterator();
        OClass supClass;
        while (sups.hasNext()) {
          supClass = (OClass) sups.next();
          dclas.addProperty(propSubClassOf,supClass.getURI());
        } // while subs


      } //while classes



    } catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }

    this.setModified(false);
    return model;
  } // store


}