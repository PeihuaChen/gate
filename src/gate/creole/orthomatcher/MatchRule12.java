package gate.creole.orthomatcher;

import gate.Annotation;

/**
 * RULE #12: do the first and last tokens of one name
 * match the first and last tokens of the other?
 * Condition(s): case-sensitive match
 * Applied to: organisation annotations only
 */
public class MatchRule12 implements OrthoMatcherRule {

    OrthoMatcher orthomatcher;
	
	public MatchRule12(OrthoMatcher orthmatcher){
			this.orthomatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {

	    // first do the easy case e.g. "Pan American" == "Pan Am"

	    if (orthomatcher.tokensLongAnnot.size()>1 && orthomatcher.tokensShortAnnot.size()>1) {
//	    Out.prln("Rule 12");

	      // get first and last tokens of s1 & s2
	      String s1_first = (String)
	      ((Annotation) orthomatcher.tokensLongAnnot.get(0)).getFeatures().get(orthomatcher.TOKEN_STRING_FEATURE_NAME);
	      String s2_first = (String)
	      ((Annotation) orthomatcher.tokensShortAnnot.get(0)).getFeatures().get(orthomatcher.TOKEN_STRING_FEATURE_NAME);

	      if (!OrthoMatcherHelper.straightCompare(s1_first,s2_first,orthomatcher.caseSensitive))
	        return false;

	      String s1_last = (String)
	      ((Annotation) orthomatcher.tokensLongAnnot.get(orthomatcher.tokensLongAnnot.size()-1)).getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME);
	      String s2_last = (String)
	      ((Annotation) orthomatcher.tokensShortAnnot.get(orthomatcher.tokensShortAnnot.size()-1)).getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME);

	      boolean retVal =  OrthoMatcherHelper.straightCompare(s1_last,s2_last,orthomatcher.caseSensitive);
	      if (retVal && orthomatcher.log.isDebugEnabled()) {
	        orthomatcher.log.debug("rule 12 matched " + s1 + "(id: " + orthomatcher.longAnnot.getId() + ") to "
	                + s2+ "(id: " + orthomatcher.shortAnnot.getId() + ")");
	      }
	      return retVal;
	      
	    } // if (tokensLongAnnot.countTokens()>1
	    return false;
	}
	
  public String getId(){
    return "MatchRule12";
  }
}
