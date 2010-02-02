package gate.creole.orthomatcher;

/**
 * RULE #16: Conservative match rule
 * Require every token in one name to match the other except for tokens that are on a stop word list
 */
public class MatchRule17 implements OrthoMatcherRule {

    OrthoMatcher orthomatcher;
	
	public MatchRule17(OrthoMatcher orthmatcher){
			this.orthomatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		//reversed execution of allNonStopTokensInOtherAnnot
		if (OrthoMatcherHelper.allNonStopTokensInOtherAnnot(orthomatcher.tokensLongAnnot, orthomatcher.tokensShortAnnot,orthomatcher.TOKEN_STRING_FEATURE_NAME,orthomatcher.caseSensitive)) {
		      return OrthoMatcherHelper.allNonStopTokensInOtherAnnot(orthomatcher.tokensShortAnnot, orthomatcher.tokensLongAnnot,orthomatcher.TOKEN_STRING_FEATURE_NAME,orthomatcher.caseSensitive);
		    }
		  else 
		  {
		      return false;
		  }
	}
	
  public String getId(){
    return "MatchRule17";
  }
}
