package gate.creole.orthomatcher;

import java.util.HashSet;
import java.util.Map;

/** RULE #1: If the two names are identical then they are the same
 * no longer used, because I do the check for same string via the
 * hash table of previous annotations
 * Condition(s): depend on case
 * Applied to: annotations other than names
 */
public class MatchRule1 implements OrthoMatcherRule{

	OrthoMatcher orthmatcher;
	
	public MatchRule1(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1,
	          String s2) {
		
	    boolean retVal = OrthoMatcherHelper.straightCompare(s1, s2, orthmatcher.caseSensitive);
	    //if straight compare didn't work, try a little extra logic
	    if (!retVal)
	      retVal = OrthoMatcherHelper.fuzzyMatch(orthmatcher.nicknameMap,s1, s2);

	    /*if (logResult && retVal && log.isDebugEnabled()) {
	      log.debug("rule1Name matched " + s1 + "(id: " + longAnnot.getId() + ") to "
	              + s2+ "(id: " + shortAnnot.getId() + ")");*/
	  
	    return retVal;
	}
	
  public String getId(){
    return "MatchRule1";
  }
}
