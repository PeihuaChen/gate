/*
 * OntologyImpl.java
 * Copyright:    Copyright (c) 2002, OntoText Lab.
 * Company:      OntoText Lab.
 * borislav popov 03/2002 */
package com.ontotext.gate.ontology;

import gate.creole.*;
import gate.creole.ontology.*;
import gate.util.*;

import java.util.*;
import java.io.*;
import java.net.*;

import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.arp.*;

/** Provides load and store of ontologies from/to DAML+OIL repository
 *  @author borislav popov
 */
public class DAMLOntology extends TaxonomyImpl {

  /** DEBUG FLAG */
  private static final boolean DEBUG = false;

  /**loads from rdf/parse rdf. it helps if setURL is called apriori.*/
  public void load() throws ResourceInstantiationException{
    loadAndGetModel();
  }

  /**
   * Stores the ontology to <@link getURL()/> in daml
   */
  public void store()throws ResourceInstantiationException  {
    DAMLModel model = storeAndGetModel();
    writeModel(model);
  }

  /** Introduced a protected method that retuns the model, so it can be
   * used in a subclass to get more data before discarding it
   */
  protected DAMLModel loadAndGetModel() throws ResourceInstantiationException{

    DAMLModel model;

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
          TClass ocl = (TClass)cli.next();
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

      } // if model.getloadsucessful

      /**debug*/
      if (DEBUG) {
        Iterator ic = this.getClasses().iterator();
        while (ic.hasNext()) {
          TClass cl = (TClass)ic.next();
          System.out.println(""+cl+" [direct sub classes = "+
          cl.getSubClasses(TClass.DIRECT_CLOSURE).size()+"] "+
          "[transitive sub classes = "+
          cl.getSubClasses(TClass.TRANSITIVE_CLOSURE).size()+"]");
        }
      } // debug end


    } catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }

    this.setModified(false);

    return model;
  } // load()


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

      com.hp.hpl.mesa.rdf.jena.model.Property propVersion = model.createProperty(voc.versionInfo().getURI());
      onto.addProperty(propVersion,this.getVersion());

      com.hp.hpl.mesa.rdf.jena.model.Property propLabel = model.createProperty(RDFS.label.getURI());
      onto.addProperty(propLabel,this.getLabel());

      com.hp.hpl.mesa.rdf.jena.model.Property propComment = model.createProperty(RDFS.comment.getURI());
      onto.addProperty(propComment,this.getComment());


      com.hp.hpl.mesa.rdf.jena.model.Property propSubClassOf = model.createProperty(RDFS.subClassOf.getURI());


      /* create classes */
      Iterator classes = this.getClasses().iterator();
      TClass clas;
      DAMLClass dclas;
      while (classes.hasNext())  {
        clas = (TClass) classes.next();

        dclas = model.createDAMLClass(clas.getURI());
        dclas.addProperty(propLabel,clas.getName());
        if (null != clas.getComment()) {
          dclas.addProperty(propComment,clas.getComment());
        }

        /* set super classes */
        Iterator sups = clas.getSuperClasses(clas.DIRECT_CLOSURE).iterator();
        TClass supClass;
        while (sups.hasNext()) {
          supClass = (TClass) sups.next();
          dclas.addProperty(propSubClassOf,supClass.getURI());
        } // while subs


      } //while classes



    } catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }

    this.setModified(false);
    return model;
  } // store


} // class DAMLOntology