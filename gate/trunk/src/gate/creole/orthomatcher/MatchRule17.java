package gate.creole.orthomatcher;

/**
 * RULE #16: Conservative match rule
 * Require every token in one name to match the other except for tokens that are on a stop word list
 */
public class MatchRule17 implements OrthoMatcherRule {

    OrthoMatcher orthmatcher;
	
	public MatchRule17(OrthoMatcher orthmatcher){
			this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		//reversed execution of allNonStopTokensInOtherAnnot
		if (OrthoMatcherHelper.allNonStopTokensInOtherAnnot(orthmatcher.tokensLongAnnot, orthmatcher.tokensShortAnnot,orthmatcher.TOKEN_STRING_FEATURE_NAME,orthmatcher.caseSensitive)) {
		      return OrthoMatcherHelper.allNonStopTokensInOtherAnnot(orthmatcher.tokensShortAnnot, orthmatcher.tokensLongAnnot,orthmatcher.TOKEN_STRING_FEATURE_NAME,orthmatcher.caseSensitive);
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
