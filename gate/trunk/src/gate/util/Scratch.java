/*
 *  Scratch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 22/03/00
 *
 *  $Id$
 */


package gate.util;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;

import gate.*;
import gate.creole.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.w3c.www.mime.*;

/** A scratch pad for experimenting.
  */
public class Scratch
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  public static void main(String args[]) {
    Scratch oneOfMe = new Scratch();
    try{
      oneOfMe.doIt();
    } catch (Exception e) {
      e.printStackTrace(Out.getPrintWriter());
    }

  } // main

  public void testFinal(String s){

  }// testFinal()

  public void doIt() throws Exception {
    long startTime = System.currentTimeMillis();

    Out.prln("gate init");
    Gate.setLocalWebServer(false);
    Gate.setNetConnected(false);
    Gate.init();

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("creating resources");

    // a controller
    Controller c1 = (Controller) Factory.createResource(
      "gate.creole.SerialController",
      Factory.newFeatureMap()
    );
    c1.getFeatures().put("gate.NAME", "Scratch controller");

    //get a document
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
//    params.put("sourceUrl", new File("z:\\tmp\\zxc.txt").toURL());
    params.put("markupAware", "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    //create a default tokeniser
    params = Factory.newFeatureMap();
    params.put("rulesURL", "gate:/creole/tokeniser/DefaultTokeniser.rules");
    params.put("encoding", "UTF-8");
    params.put("document", doc);
    ProcessingResource tokeniser = (ProcessingResource) Factory.createResource(
      "gate.creole.tokeniser.DefaultTokeniser", params
    );

    //create a default gazetteer
    params = Factory.newFeatureMap();
    params.put("document", doc);
    params.put("listsURL", "gate:/creole/gazeteer/default/lists.def");
    ProcessingResource gaz = (ProcessingResource) Factory.createResource(
      "gate.creole.gazetteer.DefaultGazetteer", params
    );

    //create a default transducer
    params = Factory.newFeatureMap();
    params.put("document", doc);
    //params.put("grammarURL", new File("z:\\tmp\\main.jape").toURL());
    ProcessingResource trans = (ProcessingResource) Factory.createResource(
      "gate.creole.Transducer", params
    );

    // get the controller to encapsulate the tok and gaz
    c1.add(tokeniser);
    c1.add(gaz);
    c1.add(trans);

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("dumping state");

    // create a File to store the state in
    File stateFile = new File("z:\\tmp", "SerialisedGateState.gzsr");

    // dump the state into the new File
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
        new GZIPOutputStream(new FileOutputStream(stateFile))
      );
      oos.writeObject(new SessionState());
      oos.close();
    } catch(IOException e) {
      throw new GateException("Couldn't write to state file: " + e);
    }

    Out.prln(System.getProperty("user.home"));

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("reinstating");

    try {
      FileInputStream fis = new FileInputStream(stateFile);
      GZIPInputStream zis = new GZIPInputStream(fis);
      ObjectInputStream ois = new ObjectInputStream(zis);
      SessionState state = (SessionState) ois.readObject();
      ois.close();
    } catch(IOException e) {
      throw
        new GateException("Couldn't read file "+stateFile+": "+e);
    } catch(ClassNotFoundException ee) {
      throw
        new GateException("Couldn't find class: "+ee);
    }

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("done");
  } // doIt

  class SessionState implements Serializable {
    SessionState() {
      cr = Gate.getCreoleRegister();
      dsr = Gate.getDataStoreRegister();
    }

    CreoleRegister cr;

    DataStoreRegister dsr;

    // other state from Gate? and elsewhere?
  }

  /** Generate a random integer for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /** Random number generator */
  protected static Random randomiser = new Random();

} // class Scratch

