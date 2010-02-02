package gate.creole.orthomatcher;

import gate.Annotation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * RULE #4Name: Does all the non-punctuation tokens from the long string match the corresponding tokens 
 * in the short string?  
 * This basically identifies cases where the two strings match token for token, excluding punctuation
 * Applied to: person annotations
 *
 * Modified by Andrew Borthwick, Spock Networks:  Allowed for nickname match
 */
public class MatchRule5 implements OrthoMatcherRule {

	OrthoMatcher orthmatcher;
	
	public MatchRule5(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		boolean allTokensMatch = true;
//	    if (s1.equals("wilson")) {
//	      log.debug("MR4 Name: Matching" + tokensLongAnnot + " with " + tokensShortAnnot);
//	      log.debug("MR4 Name: Matching " + s1 + " with " + s2);
//	    }  
	    /*if (OrthoMatcher.tokensLongAnnot.size() == 0 || OrthoMatcher.tokensShortAnnot.size() == 0) {
	      log.debug("Rule4n rejecting " + s1 + " and " + s2 + " because one doesn't have any tokens");
	      return false;
	    }*/
	    Iterator tokensLongAnnotIter = orthmatcher.tokensLongAnnot.iterator();
	    Iterator tokensShortAnnotIter = orthmatcher.tokensShortAnnot.iterator();
	    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
	      Annotation token = (Annotation) tokensLongAnnotIter.next();
	      if (((String)token.getFeatures().get(orthmatcher.TOKEN_KIND_FEATURE_NAME)).equals(orthmatcher.PUNCTUATION_VALUE))
	        continue;
	      if (! OrthoMatcherHelper.fuzzyMatch(orthmatcher.nicknameMap,(String)(((Annotation) tokensShortAnnotIter.next()).
	              getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME)),
	              (String) token.getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME))) {
	        allTokensMatch = false;
	        break;
	      }
	    }
	    /*if (allTokensMatch && log.isDebugEnabled())
	      log.debug("rule4n matched " + s1 + "(id: " + longAnnot.getId() + ", offset: " + longAnnot.getStartNode().getOffset() + ") to " + 
	                                    s2+  "(id: " + shortAnnot.getId() + ", offset: " + shortAnnot.getStartNode().getOffset() + ")");
	                                    */
	    return allTokensMatch;
	}
	
  public String getId(){
    return "MatchRule5";
  }
}
