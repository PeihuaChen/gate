package gate.creole.orthomatcher;

import gate.Annotation;

/**
 * RULE #13: do multi-word names match except for
 * one token e.g.
 * "Second Force Recon Company" == "Force Recon Company"
 * Note that this rule has NOT been used in LaSIE's 1.5
 * namematcher
 * Restrictions: - remove cdg first
 *               - shortest name should be 2 words or more
 *               - if N is the number of tokens of the longest
 *                 name, then N-1 tokens should be matched
 * Condition(s): case-sensitive match
 * Applied to: organisation or person annotations only
 */
public class MatchRule14 implements OrthoMatcherRule {

  OrthoMatcher orthmatcher;
	
	public MatchRule14(OrthoMatcher orthmatcher){
			this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
	    int matched_tokens = 0, mismatches = 0;;

	    // if names < 2 words then rule is invalid
	    if (orthmatcher.tokensLongAnnot.size() < 3 || orthmatcher.tokensShortAnnot.size() < 2) return false;

	//  if (s1.equalsIgnoreCase("chin") || s2.equalsIgnoreCase("chin")) {
	//  Out.prln("Rule 13: Matching tokens" + tokensLongAnnot);
	//  Out.prln("with tokens " + tokensShortAnnot);
	//  }

	    // now do the matching
	    for (int i=0,j= 0; i < orthmatcher.tokensShortAnnot.size() && mismatches < 2; i++) {

//	    Out.prln("i = " + i);
//	    Out.prln("j = " + j);
	      if ( ((Annotation) orthmatcher.tokensLongAnnot.get(j)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME).equals(
	              ((Annotation) orthmatcher.tokensShortAnnot.get(i)).getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME)) ) {
	        matched_tokens++;
	        j++;
	      } else
	        mismatches++;
	    } // for

	    if (matched_tokens >= orthmatcher.tokensLongAnnot.size()-1)
	      return true;

	    return false;
	}
	
  public String getId(){
    return "MatchRule14";
  }
}
