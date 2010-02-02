package gate.creole.orthomatcher;

import gate.Annotation;
import gate.creole.ExecutionException;
import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrthoMatcherHelper {
	
	  public static boolean straightCompare(String s1,
	          String s2,
	          boolean matchCase) {

	    boolean matched = false;
	    if (!matchCase)
	      matched = s1.equalsIgnoreCase(s2);
	    else matched =  s1.equals(s2) ;
	//  kalina: do not remove, nice for debug
	//  if (matched && (s2.startsWith("Kenneth") || s1.startsWith("Kenneth")))
	//  Out.prln("Rule1: Matched " + s1 + "and " + s2);
	    return matched;
	  }
	  
	  public static boolean fuzzyMatch (Map<String,HashSet<String>> nicknameMap,String s1, String s2) {
		    String s1Lower = s1.toLowerCase();
		    String s2Lower = s2.toLowerCase();
		    if (s1Lower.equals(s2Lower)) {
		      return true;
		    }
		    // System.out.println("Now comparing " + s1 + " | " + s2) ;
		    Set<String> formalNameSet = nicknameMap.get(s1Lower);
		    if (formalNameSet != null) {
		      if (formalNameSet.contains(s2Lower)) {
		        return true;
		      }
		    }
		    formalNameSet = nicknameMap.get(s2Lower);
		    if (formalNameSet != null) {
		      if (formalNameSet.contains(s1Lower)) {
		        return true;
		      }
		    }
		    return false;
		  }
	  
	  /**
	   * Returns true if only one of s1 and s2 is a single character and the two strings match on that
	   * initial
	   * 
	   * @param s1  
	   * @param s2
	   * @return
	   */
	  public static boolean initialMatch(String s1, String s2) {
	    return (((s1.length() == 1) ^ (s2.length() == 1) ) && (s1.charAt(0) == s2.charAt(0)));
	  }
	  
	  /**
	   * @return true if all of the tokens in firstName are either found in second name or are stop words
	   */
	  public static boolean allNonStopTokensInOtherAnnot(ArrayList<Annotation> firstName,ArrayList<Annotation> secondName,String TOKEN_STRING_FEATURE_NAME,boolean caseSensitive) {
	    for (Annotation a : firstName) {
	      if (!a.getFeatures().containsKey("ortho_stop")) {
	        String aString = (String) a.getFeatures().get(TOKEN_STRING_FEATURE_NAME);
	        boolean foundAMatchInSecond = false;
	        for (Annotation b: secondName) {
	          if (OrthoMatcherHelper.straightCompare(aString,(String) b.getFeatures().get(TOKEN_STRING_FEATURE_NAME),caseSensitive)) {
	            foundAMatchInSecond = true;
	            break;
	          }
	        }
	        if (!foundAMatchInSecond) {
	          return false;
	        }
	      }
	    }
	    return true;
	  }
	  
	  public static String getStringForAnnotation(Annotation a, gate.Document d) throws ExecutionException {
		    String annotString = getStringForSpan(a.getStartNode().getOffset(),a.getEndNode().getOffset(), d);
		    // now do the reg. exp. substitutions
		    annotString = annotString.replaceAll("\\s+", " ");

		    return annotString;
		  }

		static private String getStringForSpan(Long start, Long end,gate.Document d) throws ExecutionException {
		    try {
		      return d.getContent().getContent(start, end).toString();
		    }
		    catch (InvalidOffsetException e) {
		      //log.error("Weird offset exception in getStringForSpan", e);
		      throw new ExecutionException(e);
		    }
	  }
		 
	  public static boolean ExecuteDisjunction(Map<Integer,OrthoMatcherRule> allrules, int[] executeRules,String longName,String shortName, boolean mr[]) {
		  
		  boolean result=false;
		  
		  for (int i = 0; i < executeRules.length; i = i + 1) {
		    
		    boolean current=allrules.get(executeRules[i]).value(longName, shortName);
		    mr[executeRules[i]]=current;
			  result=result || current;
		  }
		  
		  return result;
	  }
}
