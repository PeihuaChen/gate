package gate.util.web;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.gui.*;

import javax.servlet.*;

/**
 * This class illustrates how to use ANNIE as a sausage machine
 * in another application - put ingredients in one end (URLs pointing
 * to documents) and get sausages (e.g. Named Entities) out the
 * other end.
 * <P><B>NOTE:</B><BR>
 * For simplicity's sake, we don't do any exception handling.
 */
public class WebAnnie  {
    
    public static final String GATE_INIT_KEY = "gate.init";
    public static final String ANNIE_CONTROLLER_KEY = "annie.controller";

    /** The Corpus Pipeline application to contain ANNIE */
    private SerialAnalyserController annieController;
    
    /**
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    public void initAnnie() throws GateException {
        
        // create a serial analyser controller to run ANNIE with
        annieController = (SerialAnalyserController)
            Factory.createResource("gate.creole.SerialAnalyserController",
                                   Factory.newFeatureMap(),
                                   Factory.newFeatureMap(),
                                   "ANNIE_" + Gate.genSym()
                                   );

        /*
    "gate.creole.tokeniser.DefaultTokeniser",
    "gate.creole.gazetteer.DefaultGazetteer",
    "gate.creole.splitter.SentenceSplitter",
    "gate.creole.POSTagger",
    "gate.creole.ANNIETransducer",
    "gate.creole.orthomatcher.OrthoMatcher"
        */
        
        // load each PR as defined in ANNIEConstants
        for (int i = 0; i < ANNIEConstants.PR_NAMES.length; i++) {
            // use default parameters
            FeatureMap params = Factory.newFeatureMap(); 
            ProcessingResource pr = (ProcessingResource)
                Factory.createResource(ANNIEConstants.PR_NAMES[i], params);
            
            // add the PR to the pipeline controller
            annieController.add(pr);
        } // for each ANNIE PR
        
    } // initAnnie()
    
    /**
     * Run from the command-line, with a list of URLs as argument.
     * <P><B>NOTE:</B><BR>
     * This code will run with all the documents in memory - if you
     * want to unload each from memory after use, add code to store
     * the corpus in a DataStore.
     */
    public String process(ServletContext app, String url, String[] annotations)
        throws GateException, IOException {

        if (app.getAttribute(GATE_INIT_KEY) == null) {
            Gate.setLocalWebServer(false);
            Gate.setNetConnected(false);

            System.setProperty("java.protocol.handler.pkgs",
                               "gate.util.protocols");
            
            System.out.println("before gate init");
            System.out.println("Freemem: " + Runtime.getRuntime().freeMemory());

            // Do the deed
            Gate.init();
            System.out.println("after gate init");
            System.out.println("Freemem: " + Runtime.getRuntime().freeMemory());

            app.setAttribute(GATE_INIT_KEY, "true");
        }

        if (app.getAttribute(ANNIE_CONTROLLER_KEY) == null) {
            // initialise ANNIE (this may take several minutes)
            System.out.println("before annie init");
            System.out.println("Freemem: " + Runtime.getRuntime().freeMemory());
            this.initAnnie();
            System.out.println("after annie init");
            System.out.println("Freemem: " + Runtime.getRuntime().freeMemory());

            app.setAttribute(ANNIE_CONTROLLER_KEY, annieController);
        }
        else {
            annieController = (SerialAnalyserController) 
                app.getAttribute(ANNIE_CONTROLLER_KEY);
        }

        
        // create a GATE corpus and add a document for each command-line
        // argument
        Corpus corpus =
            (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
        URL u = new URL(url);
        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", u);

        Document doc = (Document)
            Factory.createResource("gate.corpora.DocumentImpl", params);
        corpus.add(doc);
        
        // tell the pipeline about the corpus and run it

        annieController.setCorpus(corpus);
        annieController.execute();
        
        // Get XML marked up document

        AnnotationSet defaultAnnotSet = doc.getAnnotations();
        Set annotTypesRequired = new HashSet();

        for (int i=0;i<annotations.length;i++) {
            annotTypesRequired.add(annotations[i]);
        }
        AnnotationSet peopleAndPlaces =
            defaultAnnotSet.get(annotTypesRequired);
        return doc.toXml(peopleAndPlaces, true);

    } // process
    
} // class WebAnnie
