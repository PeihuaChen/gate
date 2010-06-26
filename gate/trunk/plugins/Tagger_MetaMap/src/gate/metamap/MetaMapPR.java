/*
 *  MetaMapPR.java
 *
 *
 * Copyright (c) 2010, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  philipgooch, 20/2/2010
 */
package gate.metamap;

import gov.nih.nlm.nls.metamap.*;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.util.*;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.util.*;
import java.io.*;

// TextNormalizer code from phramer.org
// Allows compilation under both Java 5 and Java 6
import info.olteanu.utils.*;
import info.olteanu.interfaces.StringFilter;

import java.util.regex.Pattern;

/** 
 * This class is the implementation of the resource METAMAP.
 */
@CreoleResource(name = "MetaMap Annotator",
helpURL = "http://gate.ac.uk/userguide/sec:misc-creole:metamap",
comment = "This plugin uses the MetaMap Java API to send GATE document content to MetaMap skrmedpostctl server and PrologBeans mmserver instances running on the given machine/port")
public class MetaMapPR extends AbstractLanguageAnalyser
        implements ProcessingResource, Serializable {

    private String inputASName;
    private String outputASName;
    private String outputASType;
    private ArrayList<String> inputASTypes;
    private OutputMode outputMode;
    private Boolean annotatePhrases;
    private String metaMapOptions;
    private ArrayList<String> excludeSemanticTypes;
    private ArrayList<String> restrictSemanticTypes;
    private Long scoreThreshold;

    private Integer mmServerPort;
    private String mmServerHost;
    private Integer mmServerTimeout;
    private Boolean useNegEx;
    
    @Override
    public Resource init() throws ResourceInstantiationException {

        // check required parameters are set
        if (outputMode == null) {
            throw new ResourceInstantiationException("outputMode parameter must be set");
        }
        
        if (outputASType == null || outputASType.trim().length() == 0) {
            throw new ResourceInstantiationException("outputASType parameter must be set");
        }
        if (mmServerHost == null || mmServerHost.trim().length() == 0) {
            throw new ResourceInstantiationException("mmServerHost parameter must be set");
        }
        if (mmServerPort == null || !(mmServerPort instanceof Integer)) {
            throw new ResourceInstantiationException("mmServerPort parameter must be set");
        }
        if (mmServerTimeout == null || !(mmServerTimeout instanceof Integer)) {
            throw new ResourceInstantiationException("mmServerTimeout parameter must be set");
        }
        return this;
    }

    @Override
    public void execute() throws ExecutionException {
        // If no document provided to process throw an exception
        if (document == null) {
            fireProcessFinished();
            throw new ExecutionException("No document to process!");
        }


        // Need to check whether scoreThreshold is null and if not, whether it is a Long
        if (scoreThreshold == null || !(scoreThreshold instanceof Long)) {
            fireProcessFinished();
            throw new ExecutionException("Invalid score threshold value!");
        }

        Long lngInitialOffset = Long.valueOf(0);

        if (inputASTypes != null) {
            AnnotationSet inputAS = (inputASName == null || inputASName.trim().length() == 0) ? document.getAnnotations() : document.getAnnotations(inputASName);
           
            for (String inputAnn : inputASTypes) {
                Iterator<Annotation> itr = inputAS.get(inputAnn).iterator();
                while (itr.hasNext()) {

                    Annotation ann = itr.next();
                    String annContent = "";
                    
                    try {
                        lngInitialOffset = ann.getStartNode().getOffset();
                        annContent = document.getContent().getContent(lngInitialOffset,
                                ann.getEndNode().getOffset()).toString();
                    } catch (InvalidOffsetException ioe) {
                        // this should never happen
                        fireProcessFinished();
                        throw new ExecutionException(ioe);
                    }
                    try {
                        this.processWithMetaMap(annContent, lngInitialOffset);
                    } catch (Exception e) {
                        fireProcessFinished();
                        throw new ExecutionException(e);
                    }
                }
            }
        } else {
            String docText = document.getContent().toString();
            try {
                this.processWithMetaMap(docText, lngInitialOffset);
            } catch (Exception e) {
                fireProcessFinished();
                throw new ExecutionException(e);
            }
        }

        fireProcessFinished();

    }

    /**
     * 
     * @param phrase
     * @throws Exception
     */
    public void processPhrase(PCM pcm, Long lngInitialOffset) throws Exception {
        AnnotationSet outputAs = (outputASName == null || outputASName.trim().length() == 0) ? document.getAnnotations() : document.getAnnotations(outputASName);

        Phrase phrase = pcm.getPhrase();
        Position pos = phrase.getPosition();

        int intStartPos = pos.getX();
        int intEndPos = pos.getY() + intStartPos;

        FeatureMap fm = Factory.newFeatureMap();
        fm.put("Type", "Phrase");
        
        Long lngStartPos = new Long((long) intStartPos + lngInitialOffset.longValue());
        Long lngEndPos = new Long((long) intEndPos + lngInitialOffset.longValue());
        try {
            outputAs.add(lngStartPos, lngEndPos, outputASType, fm);
        } catch (InvalidOffsetException ie) {
            throw ie;
        }
    }

    
    /**
     *
     * @param eVList
     * @throws Exception
     */
    public void processEvents(List<Ev> eVList, List<Negation> negList, String type, Long lngInitialOffset) throws Exception {
        AnnotationSet outputAs = (outputASName == null || outputASName.trim().length() == 0) ? document.getAnnotations() : document.getAnnotations(outputASName);


        for (Ev mapEv : eVList) {
            FeatureMap fm = Factory.newFeatureMap();
            
            List<Position> lstSpans = mapEv.getPositionalInfo();

            int numSpans = lstSpans.size();

            int intStartPos = lstSpans.get(0).getX();
            int intEndPos = lstSpans.get(numSpans - 1).getX() + lstSpans.get(numSpans - 1).getY();

            int intScore = Math.abs(mapEv.getScore());

            boolean blnIncludeType = true;
            // don't include mapping if the semantic type is in the exclusion list
            if (excludeSemanticTypes != null) {
                for (String s : mapEv.getSemanticTypes()) {
                    if (excludeSemanticTypes.contains(s)) {
                        blnIncludeType = false;
                        break;
                    }
                }
            }

            // only include mapping if the semantic type is in the inclusion list
            if (restrictSemanticTypes != null) {
                blnIncludeType = false;
                for (String s : mapEv.getSemanticTypes()) {
                    if (restrictSemanticTypes.contains(s)) {
                        blnIncludeType = true;
                        break;
                    }
                }
            }
            // blnIncludeType = true;
            if (blnIncludeType && intScore >= scoreThreshold) {
                if (negList != null && negList.size() > 0) {
                    // see if there is a NegEx match at the same position as Event match
                    // if so, add an annotation feature for the NexEx type and trigger
                    for (Negation ne : negList) {
                        List<Position> p = ne.getConceptPositionList();
                        
                        int intNegStartPos = p.get(0).getX();
                        int negSpans = p.size();

                        int intNegEndPos = p.get(negSpans - 1).getX() + p.get(negSpans - 1).getY();

                        if (intNegStartPos == intStartPos && intNegEndPos == intEndPos) {
                            fm.put("NegExType", ne.getType());
                            fm.put("NegExTrigger", ne.getTrigger());
                            break;
                        }
                    }
                }
                fm.put("Type", type);
                fm.put("Score", mapEv.getScore());
                fm.put("ConceptId", mapEv.getConceptId());
                fm.put("ConceptName", mapEv.getConceptName());
                fm.put("PreferredName", mapEv.getPreferredName());
                fm.put("SemanticTypes", mapEv.getSemanticTypes());
                fm.put("Sources", mapEv.getSources());

                Long lngStartPos = new Long((long)intStartPos + lngInitialOffset.longValue());
                Long lngEndPos = new Long((long)intEndPos + lngInitialOffset.longValue());
                try {
                    outputAs.add(lngStartPos, lngEndPos, outputASType, fm);
                } catch (InvalidOffsetException ie) {
                    throw ie;
                }
            }
        }
    }

    /**
     *
     * @param pcm
     * @throws Exception
     */
    public void processMappings(PCM pcm, List<Negation> negList, Long lngInitialOffset) throws Exception {
        for (gov.nih.nlm.nls.metamap.Map map : pcm.getMappings()) {
            processEvents(map.getEvList(), negList, "Mapping", lngInitialOffset);
        }
    }

    /**
     *
     * @param pcm
     * @throws Exception
     */
    public void processCandidates(PCM pcm, List<Negation> negList, Long lngInitialOffset) throws Exception {
        processEvents(pcm.getCandidates(), negList, "Candidate", lngInitialOffset);
    }

    /**
     *
     * @param result
     * @throws Exception
     */
    public void processUtterances(Result result, Long lngInitialOffset) throws Exception {

        List<Negation> negList = null;

        if (useNegEx) {
             negList = result.getNegations();
        }

        for (Utterance utterance : result.getUtteranceList()) {

            for (PCM pcm : utterance.getPCMList()) {
                int numPCMMappings = pcm.getMappings().size();

                if (outputMode.equals(OutputMode.CandidatesAndMappings) || outputMode.equals(OutputMode.CandidatesOnly)) {
                    this.processCandidates(pcm, negList, lngInitialOffset);
                }
                if (outputMode.equals(OutputMode.CandidatesAndMappings) || outputMode.equals(OutputMode.MappingsOnly)) {
                    this.processMappings(pcm, negList, lngInitialOffset);
                }

                // only annotate phrases if they contain MetaMap mappings
                if (annotatePhrases && numPCMMappings > 0) {
                    this.processPhrase(pcm, lngInitialOffset);
                }
            }
        }
    }

    

    /**
     *
     * @param text - text to be annotated
     * @throws Exception
     */
    public void processWithMetaMap(String text, Long lngInitialOffset) throws Exception {
        MetaMapApi api = new MetaMapApiImpl(mmServerHost, mmServerPort.intValue(), mmServerTimeout.intValue());

        List<String> mmOptions = new ArrayList<String>();

        mmOptions.add(this.getMetaMapOptions());

        if (mmOptions.size() > 0) {
            api.setOptions(mmOptions);
        }

        Result result = null;

        String asciiText = filterNonAscii(normalizeString(text));

        result = api.processString(asciiText);

        if (result != null) {
            this.processUtterances(result, lngInitialOffset);
        } else {
            throw new Exception("NULL result instance! ");
        }
    }

    /**
     *
     * @param str
     * @return Normalized version of str with accented characters replaced by unaccented version and
     * with diacritics removed. E.g. Ö -> O
     */
    public String normalizeString(String str) throws ClassNotFoundException {
        // TextNormalizer code from phramer.org
        // Allows compilation under both Java 5 and Java 6
        StringFilter stringFilter = TextNormalizer.getNormalizationStringFilter();
        String nfdNormalizedString = stringFilter.filter(str);
        
        // Normalizer is Java 6 only
        // String nfdNormalizedString = java.text.Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     *
     * @param text
     * @return ASCII encoding of text with non ASCII characters replaced by ?
     * @throws UnsupportedEncodingException
     */
    public String filterNonAscii(String text) throws UnsupportedEncodingException {
        String aText;
        byte[] b = text.getBytes("US-ASCII");
        aText = new String(b, "US-ASCII");
        return aText;
    }

    // PR parameters
    @Optional
    @RunTime
    @CreoleParameter(comment = "Input Annotation Set Name")
    public void setInputASName(String inputASName) {
        this.inputASName = inputASName;
    }

    public String getInputASName() {
        return inputASName;
    }

    @Optional
    @RunTime
    @CreoleParameter(comment = "Output Annotation Set Name")
    public void setOutputASName(String outputASName) {
        this.outputASName = outputASName;
    }

    public String getOutputASName() {
        return outputASName;
    }

    @CreoleParameter(defaultValue = "MetaMap",
    comment = "Name for the MetaMap Annotation types")
    public void setOutputASType(String outputASType) {
        this.outputASType = outputASType;
    }

    public String getOutputASType() {
        return outputASType;
    }

    @Optional
    @RunTime
    @CreoleParameter(comment = "Only send the content of the given Annotations in the input Annotation Set to MetaMap")
    public void setInputASTypes(ArrayList<String> inputASTypes) {
        this.inputASTypes = inputASTypes;
    }

    public ArrayList<String> getInputASTypes() {
        return inputASTypes;
    }


    @RunTime
    @CreoleParameter(defaultValue = "MappingsOnly",
    comment = "Output only final mappings, only candidate terms, or both")
    public void setOutputMode(OutputMode outputMode) {
        this.outputMode = outputMode;
    }

    public OutputMode getOutputMode() {
        return outputMode;
    }

    @RunTime
    @CreoleParameter(defaultValue = "false",
    comment = "Output MetaMap phrase-level annotations?")
    public void setAnnotatePhrases(Boolean annotatePhrases) {
        this.annotatePhrases = annotatePhrases;
    }

    public Boolean getAnnotatePhrases() {
        return annotatePhrases;
    }

    @Optional
    @CreoleParameter(comment = "Exclude the following semantic types from the output (equivalent to -k option in MetaMap)")
    public void setExcludeSemanticTypes(ArrayList<String> excludeSemanticTypes) {
        this.excludeSemanticTypes = excludeSemanticTypes;
    }

    public ArrayList<String> getExcludeSemanticTypes() {
        return excludeSemanticTypes;
    }

    @Optional
    @CreoleParameter(comment = "Restrict output to the following semantic types (equivalent to -J option in MetaMap)")
    public void setRestrictSemanticTypes(ArrayList<String> restrictSemanticTypes) {
        this.restrictSemanticTypes = restrictSemanticTypes;
    }

    public ArrayList<String> getRestrictSemanticTypes() {
        return restrictSemanticTypes;
    }

    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "500",
    comment = "Omit Candidates and Mappings with a score less than this number (equivalent to -r option in MetaMap)")
    public void setScoreThreshold(Long scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public Long getScoreThreshold() {
        return scoreThreshold;
    }

    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "-Xt",
    comment = "MetaMap runtime options")
    public void setMetaMapOptions(String metaMapOptions) {
        this.metaMapOptions = metaMapOptions;
    }

    public String getMetaMapOptions() {
        return metaMapOptions;
    }



    @CreoleParameter(defaultValue = "150000",
    comment = "Time in milliseconds to wait for Prolog server before timing out")
    public void setMmServerTimeout(Integer timeout) {
        this.mmServerTimeout = timeout;
    }

    public Integer getMmServerTimeout() {
        return mmServerTimeout;
    }

    
    @CreoleParameter(defaultValue = "8066",
    comment = "MetaMap mmserver port number")
    public void setMmServerPort(Integer port) {
        this.mmServerPort = port;
    }

    public Integer getMmServerPort() {
        return mmServerPort;
    }

    @CreoleParameter(defaultValue = "localhost",
    comment = "MetaMap mmserver host name or IP address")
    public void setMmServerHost(String host) {
        this.mmServerHost = host;
    }

    public String getMmServerHost() {
        return mmServerHost;
    }


    @RunTime
    @CreoleParameter(defaultValue = "false",
    comment = "Output NegEx negation annotations and features?")
    public void setUseNegEx(Boolean useNegEx) {
        this.useNegEx = useNegEx;
    }

    public Boolean getUseNegEx() {
        return useNegEx;
    }
} // class MetaMapPR

