/*
 *  IaaMain.java
 * 
 *  Yaoyong Li 15/03/2008
 *
 *  $Id: IaaMain.java, v 1.0 2008-03-15 12:58:16 +0000 yaoyong $
 */
package gate.iaaplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.AnnotationMerging;
import gate.util.ContingencyTable;
import gate.util.FMeasure;
import gate.util.IaaCalculation;
import gate.util.InvalidOffsetException;

/**
 * 
 * Compute the inter-annotator agreements (IAAs). Currently only f-measures
 * are computed as IAA. But other measures, such as Cohen's Kappa, can
 * be computed easily if needed.
 *
 */

public class IaaMain extends AbstractLanguageAnalyser implements
		ProcessingResource {
	/** Annotation sets for merging in one document. */
	private String annSetsForIaa;

	/** Specifying the annotation types and features for merging. */
	private String annTypesAndFeats;
  /** Specify the verbosity level for IAA results outputs. */
  private String verbosity;
  private int verbo;
  /** The overall contingency table. */
  private ContingencyTable contingencyOverall;
  /** The contingency table for each pair of annotator. */
  private ContingencyTable[] contingencyTables=null;
  /** The overall F-measure for each type. */
  private FMeasure[]fMeasureOverall;
  /** Fmeaures for each pair of annotator and each label. */
  private HashMap<String,FMeasure> fMeasuresPairwiseLabel=null;
  /** Using the labels or not for one type for IAA computation */
  private boolean isUsingLabel;
  /** Fmeaures for each pair of annotator and over all labels for each type. */
  private FMeasure[][]fMeasuresPairwise=null;
  
  /** The overall F-measure for all types. */
  public FMeasure fMeasureOverallTypes;
 
  /** Average the results from each documents. */
  
  /** All the types and features from all documents in corpus. */
  HashMap<String,String>allTypeFeats = null;

	/** Initialise this resource, and return it. */
	public gate.Resource init() throws ResourceInstantiationException {
    allTypeFeats = new HashMap<String,String>();
		return this;
	} // init()

	/**
	 * Run the resource.
	 * 
	 * @throws ExecutionException
	 */
	public void execute() throws ExecutionException {
    int positionDoc = corpus.indexOf(document);
    if(positionDoc == 0) {
      allTypeFeats.clear();
      isUsingLabel = false;
      verbo = Integer.parseInt(verbosity);
    }
		// get the annotation sets for computing the IAA
		String termSeparator = new String(";");
		// Get all the existing annotation sets from the current document
		Set annsExisting = document.getAnnotationSetNames();
		String[] annsArray = null;
		if (annSetsForIaa == null || annSetsForIaa.trim().length() == 0) {
			// if there is no annotation specified, compare all the annotation
			// sets in the document
			// count how many annotation sets the document has
			int num = 0;
			for (Object obj : annsExisting) {
				if (obj != null && obj.toString().trim().length() > 0)
					++num;
			}
			annsArray = new String[num];
			num = 0;
			List<String> annsE = new Vector<String>(annsExisting);
			Collections.sort(annsE);
			for (Object obj : annsE) {
				if (obj != null && obj.toString().trim().length() > 0)
					annsArray[num++] = obj.toString();
			}
		} else {
			annSetsForIaa = annSetsForIaa.trim();
			annsArray = annSetsForIaa.split(termSeparator);
		}
		int numAnns = annsArray.length;
    if(verbo>0) System.out.println("annotation sets:");
		for (int i = 0; i < numAnns; ++i) {
			annsArray[i] = annsArray[i].trim();
      if(verbo>0) System.out.println("*"+annsArray[i]+"*");
    }

		// Check if each annotation set for merging exist in the current
		// document
		for (int i = 0; i < numAnns; ++i)
			if (!annsExisting.contains(annsArray[i]))
				throw new ExecutionException("The annotation set"
						+ annsArray[i]
						+ "for merging doesn't exist in current document "
						+ document.getName());
		// Collect the annotation types from annotation sets for merging
    //map from annotation type to feature
		HashMap<String, String> annsTypes = new HashMap<String, String>();
		if (this.annTypesAndFeats == null
				|| this.annTypesAndFeats.trim().length() == 0) {
      //if not specifed the annotation type and features
			for (int i = 0; i < numAnns; ++i) {
				Set types = document.getAnnotations(annsArray[i]).getAllTypes();
				for (Object obj : types)
					if (!annsTypes.containsKey(obj))
						annsTypes.put(obj.toString(), "");
			}
    }	else {
			String[] annTs = this.annTypesAndFeats.split(termSeparator);
			for (int i = 0; i < annTs.length; ++i) {
				annTs[i] = annTs[i].trim();
				if (annTs[i].contains("->")) {
					String ty = annTs[i].substring(0, annTs[i].indexOf("->"));
					String tf = annTs[i].substring(annTs[i].indexOf("->") + 2);
					annsTypes.put(ty.trim(), tf.trim());
				} else
					annsTypes.put(annTs[i], "");
			}
		}
    
    //Put the types and features into the map for all
    for(String t:annsTypes.keySet()) {
      allTypeFeats.put(t,annsTypes.get(t));
    }
    
    //Get the avaraged f-measures for all documents in the corpus
    //initialise the averaged measures
    if(positionDoc == 0) {
      int num1 = annsArray.length*(annsArray.length-1)/2;
      int numTypes = annsTypes.keySet().size();
      fMeasuresPairwise = new FMeasure[numTypes][num1];
      fMeasureOverall = new FMeasure[numTypes];
      for(int j=0; j<numTypes; ++j) {
        for(int i=0; i<num1; ++i) 
          fMeasuresPairwise[j][i] = new FMeasure();
        fMeasureOverall[j] = new FMeasure();
      }
      fMeasureOverallTypes = new FMeasure();
      fMeasuresPairwiseLabel = new HashMap<String,FMeasure>();
    }
    
    //Compute the IAA for each annotation type and feature
    //Get all the annotation sets
    AnnotationSet [] annSAll = new AnnotationSet[annsArray.length];
    for(int i=0; i<annSAll.length; ++i)
      annSAll[i] = document.getAnnotations(annsArray[i]);
    
    if(verbo>0) System.out.println("For the document " +document.getName());
    
    //Sort the types names
    Vector<String>typeNames = new Vector<String>(annsTypes.keySet());
    Collections.sort(typeNames);
    
    for(int i=0; i<typeNames.size(); ++i) {
      String typeN = typeNames.get(i);
      if(verbo>0) System.out.println("For the annotation type *"+typeN+"*");
      IaaCalculation iaaC = null;
      AnnotationSet[][] annSs= new AnnotationSet[1][annsArray.length];
      for(int j=0; j<annSs[0].length; ++j)
        annSs[0][j] = annSAll[j].get(typeN);
      String [] labels=null;
      if(annsTypes.get(typeN) != null && annsTypes.get(typeN) != "") {
        String nameF = annsTypes.get(typeN);
        ArrayList<String>labelList = IaaCalculation.collectLabels(annSs,nameF);
        Collections.sort(labelList);
        labels = new String[labelList.size()];
        for(int j=0; j<labelList.size(); ++j)
          labels[j] = labelList.get(j);
        iaaC = new IaaCalculation(typeN, nameF, labels, annSs, verbo);
        
        isUsingLabel = true;
        
        if(verbo>0) System.out.println("Annotation feature=*"+nameF+"*");
      } else {
        iaaC = new IaaCalculation(typeN, annSs, verbo);
      }
      
      iaaC.pairwiseIaaFmeasure();
      //if(verbo>0) System.out.println("For the annotation type *"+typeN+"*");
      iaaC.printResultsPairwiseFmeasures();
      //sum the fmeasure of all documents
      fMeasureOverall[i].add(iaaC.fMeasureOverall);
      
      for(int j=0; j<fMeasuresPairwise[0].length; ++j)
        fMeasuresPairwise[i][j].add(iaaC.fMeasuresPairwise[j]);
      
      //add the fmeasure for each sub-type label
      if(annsTypes.get(typeN) != null && annsTypes.get(typeN) != "") {
        for(int i1=0; i1<labels.length; ++i1) {
          int num11 = 0;
          for(int i11 = 0; i11 <annsArray.length; ++i11)
            for(int j11 = i11 + 1; j11 <annsArray.length; ++j11) {
              String key = typeN.concat("->"+labels[i1]);
              key = "("+annsArray[i11]+","+annsArray[j11]+"):"+key;
              if(!fMeasuresPairwiseLabel.containsKey(key))
                fMeasuresPairwiseLabel.put(key, new FMeasure());
              fMeasuresPairwiseLabel.get(key).add(iaaC.fMeasuresPairwiseLabel[num11][i1]);
              ++num11;
            }
          }
      }
      //iaaC.pairwiseIaaKappa();
      //iaaC.printResultsPairwiseIaa(); 
    }
    
    //Print out the overall results
    if(positionDoc == corpus.size()-1) {
      if(verbo>0) System.out.println("\nFor each pair of annotators, each type and each label:");
      ArrayList<String>keyList = new ArrayList(fMeasuresPairwiseLabel.keySet());
      Collections.sort(keyList);
      int numDoc = corpus.size();
      int numTypes = annsTypes.keySet().size();
      if(verbo>0) System.out.println("\nMacro averaged over "+numDoc+" documents:");
      System.out.print("for each type:");
      for(int i=0; i<numTypes; ++i) {
        String typeN = typeNames.get(i);
        if(verbo>0) System.out.println("Annotation type *"+ typeN+"*");
        fMeasureOverall[i].macroAverage(numDoc); 
        for(int j=0; j<fMeasuresPairwise[0].length; ++j)
          fMeasuresPairwise[i][j].macroAverage(numDoc);
        if(verbo>0) System.out.println("For each pair of annotators");
        int num11=0;
        for(int i1=0; i1<annsArray.length; ++i1)
          for(int j=i1+1; j<annsArray.length; ++j) {
            if(verbo>0) System.out.println("For pair ("+annsArray[i1]+","+annsArray[j]+"): "+
              fMeasuresPairwise[i][num11].printResults());
            ++num11;
          }
        if(verbo>1) {
          isUsingLabel = false;
          if(annsTypes.get(typeN) != null && annsTypes.get(typeN) != "")
            isUsingLabel = true;
            
          if(isUsingLabel) {
            
            for(int i1=0; i1<keyList.size(); ++i1) {
              String key = keyList.get(i1);
              if(key.contains("):"+typeN+"->")) {
                fMeasuresPairwiseLabel.get(key).macroAverage(numDoc);
                String pairAnns = key.substring(0,key.indexOf("):")+1);
                String typeAnn = key.substring(key.indexOf("):")+2, key.indexOf("->"));
                String labelAnn = key.substring(key.indexOf("->")+2);
                System.out.println("pairAnns="+pairAnns+", type="+typeAnn+", label="+labelAnn+": "
                +fMeasuresPairwiseLabel.get(key).printResults());
              }
            }
          }
        }
        if(verbo>0) System.out.println("Overall pairs: "+fMeasureOverall[i].printResults());
        fMeasureOverallTypes.add(fMeasureOverall[i]);
      }
      fMeasureOverallTypes.macroAverage(numTypes);
      if(verbo>0) System.out.println("Overall pairs and types: "+  fMeasureOverallTypes.printResults());
      
      if(verbo>0) System.out.println("\nMicro averaged over "+numDoc+" documents:");
      if(verbo>0) System.out.println("For each pair of annotators");
      for(int i=0; i<numTypes; ++i) {
        String typeN = typeNames.get(i);
        if(verbo>0) System.out.println("Annotation type *"+ typeN+"*");
        int num11=0;
        for(int i1=0; i1<annsArray.length; ++i1)
        for(int j=i1+1; j<annsArray.length; ++j) {
          fMeasuresPairwise[i][num11].computeFmeasure();
          fMeasuresPairwise[i][num11].computeFmeasureLenient();
          if(verbo>0) System.out.println("For pair ("+annsArray[i1]+","+annsArray[j]+"): "+
          fMeasuresPairwise[i][num11].printResults());
          ++num11;
        }
        fMeasureOverall[i].computeFmeasure();
        fMeasureOverall[i].computeFmeasureLenient();
        if(verbo>0) System.out.println("Overall pairs: "+fMeasureOverall[i].printResults());
        if(verbo>1) {
          isUsingLabel = false;
          if(annsTypes.get(typeN) != null && annsTypes.get(typeN) != "")
            isUsingLabel = true;
          if(isUsingLabel) {
            System.out.println("\nFor each pair of annotators, each type and each label:");
            for(int i1=0; i1<keyList.size(); ++i1) {
              String key = keyList.get(i1);
              if(key.contains("):"+typeN+"->")) {
                fMeasuresPairwiseLabel.get(key).computeFmeasure();
                fMeasuresPairwiseLabel.get(key).computeFmeasureLenient();
                String pairAnns = key.substring(0,key.indexOf("):")+1);
                String typeAnn = key.substring(key.indexOf("):")+2, key.indexOf("->"));
                String labelAnn = key.substring(key.indexOf("->")+2);
                System.out.println("pairAnns="+pairAnns+", type="+typeAnn+", label="+labelAnn+": "
                  +fMeasuresPairwiseLabel.get(key).printResults());
              }
            }
          }
        }
      }
      fMeasureOverallTypes.computeFmeasure();
      fMeasureOverallTypes.computeFmeasureLenient();
      if(verbo>0) System.out.println("Overall pairs and types: "+  fMeasureOverallTypes.printResults());
    }
  }
  
	public void setAnnSetsForIaa(String annSetSeq) {
		this.annSetsForIaa = annSetSeq;
	}

	public String getAnnSetsForIaa() {
		return this.annSetsForIaa;
	}

	public void setAnnTypesAndFeats(String annTypeSeq) {
		this.annTypesAndFeats = annTypeSeq;
	}

	public String getAnnTypesAndFeats() {
		return this.annTypesAndFeats;
	}
  
  public void setVerbosity(String v) {
    this.verbosity = v;
  }

  public String getVerbosity() {
    return this.verbosity;
  }
  

}
