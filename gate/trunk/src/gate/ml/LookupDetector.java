package gate.ml;

import java.util.*;

import weka.core.*;


import gate.*;
import gate.util.*;
import gate.creole.ANNIEConstants;
/**
 * Detects lookup major and minor types and their location.
 * This attribute detector is used to detect both lookup types (a nominal
 * attribute) and their location (a numerical one).
 * A sequence of calls to {@link #getAttribute()} will return alternatively
 * the two types of attributes.
 */
public class LookupDetector extends AbstractAttributeExtractor{

  public LookupDetector() {
  }

  public Attribute getAttribute() {
    Attribute attribute = null;
    String attributeNameBase = "Lookup-" + ((int)attributesReturned / 2 + 1);
    if(attributesReturned % 2 == 0){
      //even value -> Lookup type
      FastVector values = new FastVector(LOOKUP_TYPES.length);
      for(int i = 0; i < LOOKUP_TYPES.length; i++)
        values.addElement(LOOKUP_TYPES[i]);
      attribute = new Attribute(attributeNameBase,
                                values);
    }else{
      //odd value ->lookup position
      attribute = new Attribute(attributeNameBase + " (position)");
    }
    attributesReturned++;
    return attribute;
  }


  public Object getAttributeValue(Object data) {
    if(data == lastAnnotationInstance){
      if(lastLookupPosition != -1){
        //this is a second question for the same annotation instance and the
        //same lookup -> return lookup position
        Object returnValue = lastLookupPosition == -2 ? null :
                             new Double(lastLookupPosition);
        lastLookupPosition = -1;
        return returnValue;
      }
    }else{
      //new annotation instance
      lookupsReturned = 0;
    }

    //if we reached this point we need to return the lookup type

    //the data is an annotation in this case.
    Annotation ann = (Annotation)data;
    Long endOffset = ann.getEndNode().getOffset();
    Long nextOffset = ann.getStartNode().getOffset();
    int skippedLookups = 0;
    int skippedTokens = 0;
    while(nextOffset != null &&
          nextOffset.compareTo(endOffset) < 0){
      //advance offset skipping all Lookups found until the one that needs
      //returning
      Set startingAnnots = dataCollector.getStartingAnnotations(nextOffset);
      if(startingAnnots != null && (!startingAnnots.isEmpty())){
        //first count skipped tokens
        Iterator annIter = startingAnnots.iterator();
        while(annIter.hasNext()){
          Annotation annotation = (Annotation)annIter.next();
          if(annotation.getType().equals(ANNIEConstants.TOKEN_ANNOTATION_TYPE)){
            skippedTokens++;
          }
        }

        annIter = startingAnnots.iterator();
        while(annIter.hasNext()){
          Annotation annotation = (Annotation)annIter.next();
          if(annotation.getType().equals(ANNIEConstants.LOOKUP_ANNOTATION_TYPE)){
            skippedLookups++;
            if(skippedLookups == (lookupsReturned + 1)){
              //the lookup we just skipped was never returned before
              //it needs to be returned now
              String lookupType = (String)annotation.getFeatures().
                            get(ANNIEConstants.LOOKUP_MAJOR_TYPE_FEATURE_NAME);
              String minorType = (String)annotation.getFeatures().
                            get(ANNIEConstants.LOOKUP_MINOR_TYPE_FEATURE_NAME);
              if(minorType != null) lookupType += ":" + minorType;

              //save the last annotation instance we examined
              lastAnnotationInstance = ann;
              //save the location for the last lookup found
              lastLookupPosition = skippedTokens;
              lookupsReturned ++;
              if(LOOKUP_TYPES_LIST.contains(lookupType)){
                return lookupType;
              }else{
                Out.prln("Warning: unknown lookup type: " + lookupType);
                return null;
              }
            }
          }
        }
      }
      nextOffset = dataCollector.nextOffset(nextOffset);
    }
    //no more lookups
    lastLookupPosition = -2;
    lastAnnotationInstance = ann;
    return null;
  }


  /**
   * This attribute detector is used to detect both lookup types (a nominal
   * attribute) and their location (a numerical one).
   * A sequence of calls to {@link #getAttribute()} will return alternatively
   * the two types of attributes.
   * This value is used to determine what attribute will be returned based on
   * its parity.
   */
  protected int attributesReturned = 0;

  /**
   * This attribute detector can be used repeatedly to get the values for more
   * than one lookup annotations inside the annotation instance under scrutiny.
   * This value will mark the number of lookups returned for the current target
   * entity in order to avoid returning the same value twice.
   */
  protected int lookupsReturned = 0;

  protected Annotation lastAnnotationInstance = null;

  protected int lastLookupPosition = -1;

  protected static final String[] LOOKUP_TYPES;
  protected static final List LOOKUP_TYPES_LIST;

  static{
    LOOKUP_TYPES = new String[]{
    "sport", "stop", "organization", "location:city", "organization:company",
    "location:country_abbrev", "country_adj", "location:country",
    "currency_unit:pre_amount", "currency_unit:post_amount", "date_key",
    "date_unit", "date:day", "organization:departmen", "facility_key_ext",
    "facility_key", "facility:building", "date:festival", "govern_key",
    "organization:government", "greeting", "time:hour", "ident_key:pre",
    "jobtitle", "loc_general_key", "loc_key:post", "loc_key:pre",
    "location:relig", "date:month", "location:region", "cdg",
    "organization:newspaper", "number", "date:ordinal", "organization",
    "org_base", "org_key:cap", "org_key", "org_pre", "spur",
    "person_first:ambig", "person_ending", "person_first:female", "person_full",
    "person_first:male", "person_full:relig", "person_full:sci", "phone_prefix",
    "location:province", "location:racecourse", "spur_ident", "address:street",
    "surname:prefix", "organization:team", "time:ampm", "time_modifier",
    "time_unit", "time:zone", "title:female", "title:civilian", "title:male",
    "title:military", "title:police", "organization:company", "year"};

    LOOKUP_TYPES_LIST = Arrays.asList(LOOKUP_TYPES);
  }

}