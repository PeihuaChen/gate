package gate.creole.orthomatcher;

import java.util.ArrayList;

import gate.Annotation;

/**
 * RULE #3: adding a possessive at the end
 * of one name causes a match
 * e.g. "Standard and Poor" == "Standard and Poor's"
 * and also "Standard and Poor" == "Standard's"
 * Condition(s): case-insensitive match
 * Applied to: all name annotations
 */
public class MatchRule3 implements OrthoMatcherRule {

	  OrthoMatcher orthmatcher;
		
		public MatchRule3(OrthoMatcher orthmatcher){
			this.orthmatcher=orthmatcher;
		}
	
	public boolean value(String s1,  String s2) { //short string

		if (s2.endsWith("'s") || s2.endsWith("'")
	            ||(s1.endsWith("'s")|| s1.endsWith("'"))) {

	      String s2_poss = null;

	      if (!s2.endsWith("'s")) s2_poss = s2.concat("'s");
	      else s2_poss = s2.concat("'");

	      if (s2_poss != null && OrthoMatcherHelper.straightCompare(s1, s2_poss,orthmatcher.caseSensitive)) {
	        /*if (log.isDebugEnabled())
	          log.debug("rule3 matched " + s1 + " to " + s2);*/
	        return true;
	      }

	      // now check the second case i.e. "Standard and Poor" == "Standard's"
	      String token = (String)
	      ((Annotation) orthmatcher.tokensLongAnnot.get(0)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);

	      if (!token.endsWith("'s")) s2_poss = token.concat("'s");
	      else s2_poss = token.concat("'");

	      if (s2_poss != null && OrthoMatcherHelper.straightCompare(s2_poss,s2,orthmatcher.caseSensitive)) {
	        /*if (log.isDebugEnabled())
	          log.debug("rule3 matched " + s1 + " to " + s2);*/
	        return true;
	      }

	    } // if (s2.endsWith("'s")
	    return false;
	}
	
  public String getId(){
    return "MatchRule3";
  }
}
