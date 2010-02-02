package gate.creole.orthomatcher;

import gate.Annotation;


/**
 * RULE #5: if the 1st token of one name
 * matches the second name
 * e.g. "Pepsi Cola" == "Pepsi"
 * Condition(s): case-insensitive match
 * Applied to: all name annotations
 *
 * Note that we don't want to use nicknames here because you don't use nicknames for last names
 */
public class MatchRule6 implements OrthoMatcherRule {

  OrthoMatcher orthmatcher;
	
	public MatchRule6(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		if (orthmatcher.tokensLongAnnot.size()> 1 &&
	            ((Annotation) orthmatcher.tokensLongAnnot.get(0)).getFeatures().get("kind").equals("number"))
	      return false;

	    //  if (s1.startsWith("Patrick") || s2.startsWith("Patrick")) {
	    //  Out.prln("Rule 5: " + s1 + "and " + s2);
	    //  }

	    //require that when matching person names, the shorter one to be of length 1
	    //for the rule to apply. In other words, avoid matching Peter Smith and
	    //Peter Kline, because they share a Peter token.
	    if ( (orthmatcher.shortAnnot.getType().equals(orthmatcher.personType)
	            || orthmatcher.longAnnot.getType().equals(orthmatcher.personType)
	    )
	    &&
	    orthmatcher.tokensShortAnnot.size()>1
	    )
	      return false;

	    if (orthmatcher.tokensLongAnnot.size()<=1)
	      return false;
	    if (((Annotation) orthmatcher.tokensShortAnnot.get(0)).getFeatures().containsKey("ortho_stop")) {
	      return false;
	    }
	    boolean result = OrthoMatcherHelper.straightCompare((String)
	            ((Annotation) orthmatcher.tokensLongAnnot.get(0)
	            ).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME),
	            s2,
	            orthmatcher.caseSensitive);
	    /*if (result && log.isDebugEnabled())
	      log.debug("rule5 matched " + s1 + " to " + s2);*/
	    return result;
	}
	
  public String getId(){
    return "MatchRule6";
  }
}
