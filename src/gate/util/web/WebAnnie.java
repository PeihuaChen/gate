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
 * This class is designed to demonstrate ANNIE in a web context. It should be
 * called from either a servlet or a JSP.
 */
public class WebAnnie  {
    
    public static final String GATE_INIT_KEY = "gate.init";
    public static final String ANNIE_CONTROLLER_KEY = "annie.controller";

    /** The Corpus Pipeline application to contain ANNIE */
    private SerialAnalyserController annieController;
    
    private String filePath = "";

    /**
     * Initialise the ANNIE system. This creates a "corpus pipeline"
     * application that can be used to run sets of documents through
     * the extraction system.
     */
    private void initAnnie() throws GateException {
        
        // create a serial analyser controller to run ANNIE with
        annieController = (SerialAnalyserController)
            Factory.createResource("gate.creole.SerialAnalyserController",
                                   Factory.newFeatureMap(),
                                   Factory.newFeatureMap(),
                                   "ANNIE_" + Gate.genSym()
                                   );
        
        // Load tokenizer
        ProcessingResource tokeniser = (ProcessingResource)
            Factory.createResource("gate.creole.tokeniser.DefaultTokeniser",
                                   Factory.newFeatureMap());
        
        annieController.add(tokeniser);
        
        // Load sentence splitter
        ProcessingResource split = (ProcessingResource)
            Factory.createResource("gate.creole.splitter.SentenceSplitter",
                                   Factory.newFeatureMap());
        
        annieController.add(split);
        
        // Load POS tagger
        ProcessingResource postagger = (ProcessingResource)
            Factory.createResource("gate.creole.POSTagger",
                                   Factory.newFeatureMap());
        
        annieController.add(postagger);


        // Load Gazetteer -- this is a two step process
        FeatureMap gazetteerFeatures = Factory.newFeatureMap();
        gazetteerFeatures.put("encoding","ISO-8859-1");

        // Step one: Locate the gazetteer file
        try {
            URL gazetteerURL =
                new URL("jar:file:" + filePath +
                        "muse.jar!/muse/resources/gazetteer/lists.def");
            gazetteerFeatures.put("listsURL", gazetteerURL);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        
        // Step two: Load the gazetteer from the file
        ProcessingResource gazetteer = (ProcessingResource)
            Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",
                                   gazetteerFeatures);
        
        annieController.add(gazetteer);        

        // Load Grammar -- similar to gazetteer
        FeatureMap grammarFeatures = Factory.newFeatureMap();
        
        try {
            URL grammarURL =
                new URL("jar:file:" + filePath +
                        "muse.jar!/muse/resources/grammar/main/main.jape");
            grammarFeatures.put("grammarURL", grammarURL);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        
        ProcessingResource grammar = (ProcessingResource)
            Factory.createResource("gate.creole.ANNIETransducer",
                                   grammarFeatures);
        
        annieController.add(grammar);

        // Load Ortho Matcher
        ProcessingResource orthoMatcher = (ProcessingResource)
            Factory.createResource("gate.creole.orthomatcher.OrthoMatcher",
                                   Factory.newFeatureMap());
        
        annieController.add(orthoMatcher);

    } // initAnnie()
    
    /**
     * This method should be called from a servlet or JSP.
     * @param app The current servlet context, eg the JSP implicit variable "application"
     * @param url The url of the file to be analysed
     * @param annotations An array of annotations
     */
    public String process(ServletContext app, String url, String[] annotations)
        throws GateException, IOException {

        if (app.getAttribute(GATE_INIT_KEY) == null) {
            Gate.setLocalWebServer(false);
            Gate.setNetConnected(false);

            System.setProperty("java.protocol.handler.pkgs",
                               "gate.util.protocols");
            
            // Do the deed
            Gate.init();

            app.setAttribute(GATE_INIT_KEY, "true");
        }

        if (app.getAttribute(ANNIE_CONTROLLER_KEY) == null) {
            // initialise ANNIE (this may take several minutes)

            filePath = app.getInitParameter("muse.path");
            this.initAnnie();

            app.setAttribute(ANNIE_CONTROLLER_KEY, annieController);
        }
        else {
            annieController = (SerialAnalyserController) 
                app.getAttribute(ANNIE_CONTROLLER_KEY);
        }

        
        // create a GATE corpus and add a document from the URL specified
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

        if (annotations != null) {
            for (int i=0;i<annotations.length;i++) {
                annotTypesRequired.add(annotations[i]);
            }
            AnnotationSet selectedAnnotations =
                defaultAnnotSet.get(annotTypesRequired);
            return doc.toXml(selectedAnnotations, true);
        }
        else {
            return doc.toXml();
        }
     
    } // process
    
} // class WebAnnie
