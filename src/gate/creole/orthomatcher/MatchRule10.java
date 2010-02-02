package gate.creole.orthomatcher;

import java.util.HashMap;

import gate.Annotation;


/**
 * RULE #10: is one name the reverse of the other
 * reversing around prepositions only?
 * e.g. "Department of Defence" == "Defence Department"
 * Condition(s): case-sensitive match
 * Applied to: organisation annotations only
 */

public class MatchRule10 implements OrthoMatcherRule {

	OrthoMatcher orthmatcher;
		
	public MatchRule10(OrthoMatcher orthmatcher){
			this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		
		String token = null;
	    String previous_token = null;
	    String next_token = null;
	    boolean invoke_rule=false;

	    if (orthmatcher.tokensLongAnnot.size() >= 3
	            && orthmatcher.tokensShortAnnot.size() >= 2) {

	      // first get the tokens before and after the preposition
	      int i = 0;
	      for (; i< orthmatcher.tokensLongAnnot.size(); i++) {
	        token = (String)
	        ((Annotation) orthmatcher.tokensLongAnnot.get(i)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);
	        if (orthmatcher.prepos.containsKey(token)) {
	          invoke_rule=true;
	          break;
	        }//if
	        previous_token = token;
	      }//while

	      if (! invoke_rule)
	        return false;

	      if (i < orthmatcher.tokensLongAnnot.size()
	              && previous_token != null)
	        next_token= (String)
	        ((Annotation) orthmatcher.tokensLongAnnot.get(i++)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);
	      else return false;

	      String s21 = (String)
	      ((Annotation) orthmatcher.tokensShortAnnot.get(0)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);
	      String s22 = (String)
	      ((Annotation) orthmatcher.tokensShortAnnot.get(1)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);
	      // then compare (in reverse) with the first two tokens of s2
	      if (OrthoMatcherHelper.straightCompare(next_token,(String) s21,orthmatcher.caseSensitive)
	              && OrthoMatcherHelper.straightCompare(previous_token, s22,orthmatcher.caseSensitive))
	        return true ;
	    }//if (tokensLongAnnot.countTokens() >= 3
	    return false;
	}
	
  public String getId(){
    return "MatchRule10";
  }
}
