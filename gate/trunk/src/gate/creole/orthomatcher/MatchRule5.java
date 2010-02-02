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

	OrthoMatcher orthomatcher;
	
	public MatchRule5(OrthoMatcher orthmatcher){
		this.orthomatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		boolean allTokensMatch = true;
//	    if (s1.equals("wilson")) {
//	      log.debug("MR4 Name: Matching" + tokensLongAnnot + " with " + tokensShortAnnot);
//	      log.debug("MR4 Name: Matching " + s1 + " with " + s2);
//	    }  
	    if (orthomatcher.tokensLongAnnot.size() == 0 || orthomatcher.tokensShortAnnot.size() == 0) {
	      orthomatcher.log.debug("Rule 5 rejecting " + s1 + " and " + s2 + " because one doesn't have any tokens");
	      return false;
	    }
	    Iterator tokensLongAnnotIter = orthomatcher.tokensLongAnnot.iterator();
	    Iterator tokensShortAnnotIter = orthomatcher.tokensShortAnnot.iterator();
	    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
	      Annotation token = (Annotation) tokensLongAnnotIter.next();
	      if (((String)token.getFeatures().get(orthomatcher.TOKEN_KIND_FEATURE_NAME)).equals(orthomatcher.PUNCTUATION_VALUE))
	        continue;
	      if (! OrthoMatcherHelper.fuzzyMatch(orthomatcher.nicknameMap,(String)(((Annotation) tokensShortAnnotIter.next()).
	              getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME)),
	              (String) token.getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME))) {
	        allTokensMatch = false;
	        break;
	      }
	    }
	    if (allTokensMatch && orthomatcher.log.isDebugEnabled()) {
	      orthomatcher.log.debug("rule 5 matched " + s1 + "(id: " + orthomatcher.longAnnot.getId() + ", offset: " + orthomatcher.longAnnot.getStartNode().getOffset() + ") to " + 
	                                    s2+  "(id: " + orthomatcher.shortAnnot.getId() + ", offset: " + orthomatcher.shortAnnot.getStartNode().getOffset() + ")");
	    }       
	    return allTokensMatch;
	}
	
  public String getId(){
    return "MatchRule5";
  }
}
