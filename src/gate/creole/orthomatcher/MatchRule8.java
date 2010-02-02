package gate.creole.orthomatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import gate.Annotation;

/**
 * RULE #7: if one of the tokens in one of the
 * names is in the list of separators eg. "&"
 * then check if the token before the separator
 * matches the other name
 * e.g. "R.H. Macy & Co." == "Macy"
 * Condition(s): case-sensitive match
 * Applied to: organisation annotations only
 */
public class MatchRule8 implements OrthoMatcherRule {

  OrthoMatcher orthmatcher;
	
	public MatchRule8(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		
		//don't try it unless the second string is just one token
	    if (orthmatcher.tokensShortAnnot.size() != 1)
	      return false;

	    String previous_token = null;

	    for (int i = 0;  i < orthmatcher.tokensLongAnnot.size(); i++ ) {
	      if (orthmatcher.connector.containsKey( ((Annotation) orthmatcher.tokensLongAnnot.get(i)
	      ).getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME) )) {
	        previous_token = (String) ((Annotation) orthmatcher.tokensLongAnnot.get(i-1)
	        ).getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME);

	        break;
	      }
	    }

	    //now match previous_token with other name
	    if (previous_token != null) {
//	    if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
//	    Out.prln("Rule7");
	      return OrthoMatcherHelper.straightCompare(previous_token,s2,orthmatcher.caseSensitive);

	    }
	    return false;

	}
	
  public String getId(){
    return "MatchRule8";
  }
}
