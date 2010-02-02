package gate.creole.orthomatcher;

import java.util.HashMap;

/**
 * RULE #2: if the two names are listed as equivalent in the
 * lookup table (alias) then they match
 * Condition(s): -
 * Applied to: all name annotations
 */
public class MatchRule2 implements OrthoMatcherRule {

  OrthoMatcher orthmatcher;
	
	public MatchRule2(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {

	    if (orthmatcher.alias.containsKey(s1) && orthmatcher.alias.containsKey(s2)) {
	      if (orthmatcher.alias.get(s1).toString().equals(orthmatcher.alias.get(s2).toString())) {
	        /*if (log.isDebugEnabled())
	          log.debug("rule2 matched " + s1 + " to " + s2);*/
	        return true;
	      }
	    }

	    return false;
	  }
	
  public String getId(){
    return "MatchRule2";
  }
}
