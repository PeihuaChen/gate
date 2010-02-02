package gate.creole.orthomatcher;


import java.util.HashMap;

/** RULE #0: If the two names are listed in table of
 * spurius matches then they do NOT match
 * Condition(s): -
 * Applied to: all name annotations
 */
public class MatchRule0 implements OrthoMatcherRule {
 
    OrthoMatcher orthmatcher;
	
	  public MatchRule0(OrthoMatcher orthmatcher){
		   this.orthmatcher=orthmatcher;
	  }
	 
	  public boolean value(String _string1,String _string2){
		 
	      if (orthmatcher.spur_match.containsKey(_string1)
	            && orthmatcher.spur_match.containsKey(_string2) )
	      return
	      orthmatcher.spur_match.get(_string1).toString().equals(orthmatcher.spur_match.get(_string2).toString());

	    return false;
	  }
	  
	  public String getId(){
	    return "MatchRule0";
	  }
}
