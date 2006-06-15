/*
 *  ProbabilityPR.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 */

package probability;

import gate.ProcessingResource;
import gate.Resource;
import gate.creole.*;
import gate.gui.MainFrame;
import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.annotation.*;

import java.util.*;

public class ProbabilityPR extends AbstractLanguageAnalyser
    implements ProcessingResource {
    
    private Corpus corpus = null;
    
    private TreeMap personCount = new TreeMap();
    private int totalPersons = 0;
    private String annotationType;

     /** Constructor of the class*/
    public ProbabilityPR() {
    }
    
    /** Initialise this resource, and return it. */
    public Resource init() throws ResourceInstantiationException {
	return super.init();
    }
    /**
     * Reinitialises the processing resource. After calling this method the
     * resource should be in the state it is after calling init.
     * If the resource depends on external resources (such as rules files) then
     * the resource will re-read those resources. If the data used to create
     * the resource has changed since the resource has been created then the
     * resource will change too after calling reInit().
     */
    public void reInit() throws ResourceInstantiationException {
	init();
	personCount = new TreeMap();
	totalPersons = 0;
    }


    public void execute() throws ExecutionException {
	personCount = new TreeMap();
	totalPersons = 0;
	if (corpus == null) {
	    throw new ExecutionException("Corpus is not initialized");
	}
	if (annotationType == null) {
	    throw new ExecutionException("Annotation Type is not initialized");
	}
	CorpusImpl roots = (CorpusImpl) corpus;
	Object rootArray[] = roots.toArray();
	for (int i=0; i<rootArray.length; i++) {
	    DocumentImpl doc = (DocumentImpl) rootArray[i];
	    //System.out.println("doc = "+doc.getSourceUrl().toString());
	    //get the names of all sets
	    Map namedSets = doc.getNamedAnnotationSets();
	    //nothing left to do if there are no named sets
	    if (namedSets == null || namedSets.isEmpty())
		continue;
	    
	    //loop through the sets and delete them all unless they're original markups
	    List setNames = new ArrayList(namedSets.keySet());
	    Iterator iter = setNames.iterator();
	    String setName;
	    
	    //System.out.println("annotations = "+doc.getAnnotations(null));
	    AnnotationSetImpl annSet = (AnnotationSetImpl)doc.getAnnotations(null);
	    //System.out.println("annotations = "+annSet.get("Person").toString());
	    AnnotationSet personSet = annSet.get(annotationType);
	    
	    //AnnotationSet personSet = annSet.get("Location");
	    
	    //nothing left to do if there are no named sets
	    if (personSet == null || personSet.isEmpty())
		continue;
	    
	    Annotation currAnnot;
	    Iterator it = personSet.iterator();
	    while (it.hasNext()) {
		currAnnot = (Annotation) it.next();
		int start = currAnnot.getStartNode().getOffset().intValue();
		int end = currAnnot.getEndNode().getOffset().intValue();
		String text = doc.getContent().toString().substring(start,end).toLowerCase();
		totalPersons ++;
		if (personCount.containsKey(text)) {
		    //System.out.println("contains "+text);
		    Integer count = (Integer)personCount.get(text);
		    int c= count.intValue();
		    personCount.remove(text);
		    personCount.put(text,new Integer(++c));
		} else {
		    personCount.put(text,new Integer(1));
		}
		//System.out.println(text+"\n______");
	    } // while
	}
	//System.out.println("done till here");
	//System.out.println("size ="+personCount.size());
	//System.out.println("keyset = "+personCount.keySet());
	
	// Sort hashtable. 
	Vector v = new Vector(personCount.keySet()); 
	//Collections.sort(v);
	Collections.sort(v,new Comparator() {
		public int compare(Object o1, Object o2) {
		    Integer int1 = (Integer) personCount.get(o1);
		    Integer int2 = (Integer) personCount.get(o2);
		    return (int2.intValue()-int1.intValue());
		}
	    }
			 
			 ); 
	
	// Display (sorted) hashtable. 
	for (Enumeration e = v.elements(); e.hasMoreElements();) { 
	    String key = (String)e.nextElement(); 
	    Integer val = (Integer)personCount.get(key); 
	    System.out.println(key + "\t" + (float)val.intValue()/(float)totalPersons); 
	} 
	
	/*Object key;
	  for (Iterator iterate = personCount.keySet().iterator(); iterate.hasNext(); ) {
	  key = iterate.next();
	  System.out.println(key + " => " + personCount.get(key));
	  } */
	
    }
    
    public void setCorpus(Corpus corpus) {
	this.corpus = corpus;
    }
    
    public Corpus getCorpus() {
	return this.corpus;
    }   

    public void setAnnotationType(String type) {
	this.annotationType = type;
    }
    
    public String getAnnotationType() {
	return this.annotationType;
    }
}