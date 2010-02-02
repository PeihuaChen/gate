package gate.creole.orthomatcher;


import java.util.HashMap;

/** RULE #0: If the two names are listed in table of
 * spurius matches then they do NOT match
 * Condition(s): -
 * Applied to: all name annotations
 */
public class MatchRule0 implements OrthoMatcherRule {
 
    OrthoMatcher orthomatcher;
	
	  public MatchRule0(OrthoMatcher orthmatcher){
		   this.orthomatcher=orthmatcher;
	  }
	 
	  public boolean value(String _string1,String _string2){
		 
	      if (orthomatcher.spur_match.containsKey(_string1)
	            && orthomatcher.spur_match.containsKey(_string2) )
	      return
	      orthomatcher.spur_match.get(_string1).toString().equals(orthomatcher.spur_match.get(_string2).toString());

	    return false;
	  }
	  
	  public String getId(){
	    return "MatchRule0";
	  }
}
