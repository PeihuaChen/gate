package gate.util.web;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.corpora.RepositioningInfo;
import gate.annotation.*;

import java.util.*;
import java.net.*;
import java.util.jar.*;
import java.io.IOException;

import javax.servlet.*;


public class WebCrimeReportAnalyser {

    public static final String SOCIS_CONTROLLER_KEY = "socis.controller";
    public static final String GATE_INIT_KEY = "gate.init";

    
    public String filePath = "";
    
    private SerialAnalyserController controller;

    public void initCrimeReportAnalyser() throws GateException {
        controller = (SerialAnalyserController)
            Factory.createResource("gate.creole.SerialAnalyserController",
                                   Factory.newFeatureMap(),
                                   Factory.newFeatureMap(),
                                   "Crime Report Analyser");

        ProcessingResource tokeniser = (ProcessingResource)
            Factory.createResource("gate.creole.tokeniser.DefaultTokeniser",
                                   Factory.newFeatureMap());

        controller.add(tokeniser);
        
        ProcessingResource split = (ProcessingResource)
            Factory.createResource("gate.creole.splitter.SentenceSplitter",
                                   Factory.newFeatureMap());
        
        controller.add(split);
        
        ProcessingResource postagger = (ProcessingResource)
            Factory.createResource("gate.creole.POSTagger",
                                   Factory.newFeatureMap());
        
        controller.add(postagger);
       
        /* ProcessingResource prechunking = (ProcessingResource)
            Factory.createResource("chunking.PreChunking",
                                   Factory.newFeatureMap());
        
        controller.add(prechunking);
             System.out.println("after tokeniser");
            System.out.println("Freemem: " + Runtime.getRuntime().freeMemory());
       
        FeatureMap fm = Factory.newFeatureMap();
        fm.put("inputASName","ChunkAnnotations");
        fm.put("outputASName","ChunkAnnotations");

        try {
            URL urlnp = new URL("jar:file:" + filePath + "files.jar!/resources/grammars/Chunk/mainNPChunk.jape");
            fm.put("grammarURL",urlnp);
        } catch(MalformedURLException e) {
            
            e.printStackTrace();
        }
        
        ProcessingResource npchunk = (ProcessingResource)
            Factory.createResource("gate.creole.ANNIETransducer",
                                   fm);
        
        controller.add(npchunk);
                
        FeatureMap fm1 = Factory.newFeatureMap();
        fm1.put("inputASName","ChunkAnnotations");
        fm1.put("outputASName","ChunkAnnotations");

        try {
            URL urlvp = new URL("jar:file:" + filePath + "files.jar!/resources/grammars/Chunk/mainNPChunk.jape");
            fm1.put("grammarURL",urlvp);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        
        ProcessingResource vpchunk = (ProcessingResource)
            Factory.createResource("gate.creole.ANNIETransducer",
                                   fm1);
        
        controller.add(vpchunk);*/
        
        FeatureMap fm_gaz = Factory.newFeatureMap();
        fm_gaz.put("encoding","ISO-8859-1");

        try {
            URL urlgaz = new URL("jar:file:" + filePath + "files.jar!/resources/gazetters/general/lists.def");
            fm_gaz.put("listsURL",urlgaz);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        
        ProcessingResource gazetteer = (ProcessingResource)
            Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",
                                   fm_gaz);
        
        controller.add(gazetteer);        

        FeatureMap fm_gra = Factory.newFeatureMap();
        
        try {
            URL urlgra = new URL("jar:file:" + filePath + "files.jar!/resources/grammars/NamedEntities/socismain.jape");
            fm_gra.put("grammarURL",urlgra);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        
        ProcessingResource grammar = (ProcessingResource)
            Factory.createResource("gate.creole.ANNIETransducer",
                                   fm_gra);
        
        controller.add(grammar);
        
    } // initIndexAnalyser()
    
    public String process(ServletContext app, String url, String[] annotations)
        throws GateException, IOException {

        long start;

        // Is this the first time a gate demo has been run? If so, 
        // initiali[s|z]e gate. It's a very heavy process, so only do
        // it once.

        if (app.getAttribute(GATE_INIT_KEY) == null) {
            Gate.setLocalWebServer(false);
            Gate.setNetConnected(false);

            System.setProperty("java.protocol.handler.pkgs",
                               "gate.util.protocols");
            
            // Do the deed
            Gate.init();

            app.setAttribute(GATE_INIT_KEY, "true");
        }

        // Now do the same for the SOCIS controller

        if (app.getAttribute(SOCIS_CONTROLLER_KEY) == null) {

            CreoleRegister reg = Gate.getCreoleRegister();

            filePath = app.getInitParameter("files.path");
            
            URL filesURL = new URL("jar:file:" + filePath + "files.jar!/");
            try {
                reg.registerDirectories(filesURL);
            } catch(GateException e) {
                System.out.println(e.getMessage());
            }
            
            initCrimeReportAnalyser();

            app.setAttribute(SOCIS_CONTROLLER_KEY, controller);
        }
        else {
            // The SOCIS demo has already run, so take the existing
            // controller from the application attribute hash
            
            controller = (SerialAnalyserController) 
                app.getAttribute(SOCIS_CONTROLLER_KEY);
        }

        Corpus corpus =
            (Corpus) Factory.createResource("gate.corpora.CorpusImpl");

        /* here the url specified by the user */
        URL textURL = new URL(url);
        
        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", textURL);
        params.put("preserveOriginalContent", new Boolean(true));
        params.put("collectRepositioningInfo", new Boolean(true));
        
        Document doc = (Document)
            Factory.createResource("gate.corpora.DocumentImpl",params);

        corpus.add(doc);
        
        controller.setCorpus(corpus);
        controller.execute();
        
        AnnotationSet defaultAnnotSet = doc.getAnnotations();
        AnnotationSet chunkAnnotSet = doc.getAnnotations("ChunkAnnotations");
        Set annotTypesRequired = new HashSet();
        Set chunkTypesRequired = new HashSet();

        for (int i=0;i<annotations.length;i++) {
            annotTypesRequired.add(annotations[i]);
        }

        /* socis stuff */
        /*annotTypesRequired.add("Location");
        annotTypesRequired.add("Time");
        annotTypesRequired.add("Organization");
        annotTypesRequired.add("Person");
        annotTypesRequired.add("Id_No");
        annotTypesRequired.add("Date");
        annotTypesRequired.add("Money");
        annotTypesRequired.add("Percent");
        annotTypesRequired.add("Conv_make");
        annotTypesRequired.add("Offence");
        annotTypesRequired.add("Age");
        annotTypesRequired.add("Drug");
        annotTypesRequired.add("Address"); */

        /* required chunks */
        /*
        chunkTypesRequired.add("NPCHUNK");
        chunkTypesRequired.add("VPCHUNK"); */
        
        AnnotationSet socis = defaultAnnotSet.get(annotTypesRequired);

        //AnnotationSet chunks = chunkAnnotSet.get(chunkTypesRequired);
        
        FeatureMap features = doc.getFeatures();
        String originalContent = (String)
            features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);

        RepositioningInfo info = (RepositioningInfo)
            features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);
        
        Annotation currAnnot;
        SortedAnnotationList sortedAnnotationsNamedEntities =
            new SortedAnnotationList();
        
        // The AnnotationSet socis can be null if no annotations have
        // been found
        if (socis != null) {
            Iterator it = socis.iterator();
            while(it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotationsNamedEntities.addSortedExclusive(currAnnot);
            }
        }
        
        AnnotationSet uniqueNamedEntities =
            new AnnotationSetImpl((Document) null);
        
        uniqueNamedEntities.addAll(sortedAnnotationsNamedEntities);
        
        SortedAnnotationList sortedAnnotationsChunks =
            new SortedAnnotationList();
        
        /*it = chunks.iterator();
        while(it.hasNext()) {
            currAnnot = (Annotation) it.next();
            sortedAnnotationsChunks.addSortedExclusive(currAnnot);
            } //while
        
        AnnotationSet uniqueChunks = new AnnotationSetImpl((Document) null);
        
        uniqueChunks.addAll(sortedAnnotationsChunks); */

        String xmlDocumentNamedEntities = doc.toXml(uniqueNamedEntities, true);
        //String xmlDocumentChunks = doc.toXml(uniqueChunks,true);
        return xmlDocumentNamedEntities;
        
    }
    
    public static class SortedAnnotationList extends Vector {
        
        public SortedAnnotationList() {
            super();
        }
        public boolean addSortedExclusive(Annotation annot) {
            Annotation currAnnot = null;
            for(int i=0; i<size() ; ++i) {
                currAnnot = (Annotation) get(i);
                if(annot.overlaps(currAnnot)) {
                    return false;
                    
                } //if
                
            } //for
            long annotStart = annot.getStartNode().getOffset().longValue();
            long currStart;
            for (int i=0; i < size(); ++i) {
                currAnnot = (Annotation) get(i);
                currStart = currAnnot.getStartNode().getOffset().longValue();
                if(annotStart < currStart) {
                    insertElementAt(annot, i);
                    return true;
                    
                } //if
                
            } //for
            
            int size = size();
            insertElementAt(annot, size);
            return true;
        } //addSortedExclusive
        
    } //SortedAnnotationList
    
}

