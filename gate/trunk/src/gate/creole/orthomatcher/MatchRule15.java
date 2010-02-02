package gate.creole.orthomatcher;

import gate.Annotation;

/**
 * RULE #14: if the last token of one name
 * matches the second name
 * e.g. "Hamish Cunningham" == "Cunningham"
 * Condition(s): case-insensitive match
 * Applied to: all person annotations
 *
 * Don't need to nicknames here
 */
public class MatchRule15 implements OrthoMatcherRule {

  OrthoMatcher orthmatcher;
	
	public MatchRule15(OrthoMatcher orthmatcher){
			this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		
	//  if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin"))
	//  Out.prln("Rule 14 " + s1 + " and " + s2);
	    String s1_short = (String)
	    ((Annotation) orthmatcher.tokensLongAnnot.get(
	    		orthmatcher.tokensLongAnnot.size()-1)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME);
	//  Out.prln("Converted to " + s1_short);
	    if (orthmatcher.tokensLongAnnot.size()>1 && OrthoMatcherHelper.straightCompare(s1_short, s2,orthmatcher.caseSensitive)) {
	     /* if (log.isDebugEnabled())
	        log.debug("rule14 matched " + s1 + "(id: " + longAnnot.getId() + ") to "  + s2 
	                + "(id: " + shortAnnot.getId() + ")");*/
	      return true;
	    }

	    return false;
	}
	
  public String getId(){
    return "MatchRule15";
  }
}
