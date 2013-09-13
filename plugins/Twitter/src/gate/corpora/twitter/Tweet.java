/*
 *  Tweet.java
 *
 *  Copyright (c) 1995-2013, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  $Id$
 */
package gate.corpora.twitter;

import gate.*;
import gate.util.*;
import gate.corpora.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import com.fasterxml.jackson.databind.JsonNode;


// JSON API
// http://json-lib.sourceforge.net/apidocs/jdk15/index.html
// Jackson API
// http://wiki.fasterxml.com/JacksonHome

// Standard: RFC 4627
// https://tools.ietf.org/html/rfc4627


public class Tweet {
  private String string;
  private long start;
  private Set<PreAnnotation> annotations;
  
  public static String PATH_SEPARATOR = ":";
  
  
  public Set<PreAnnotation> getAnnotations() {
    return this.annotations;
  }
  
  public int getLength() {
    return this.string.length();
  }

  public String getString() {
    return this.string;
  }
  
  public long getStart() {
    return this.start;
  }
  
  public long getEnd() {
    return this.start + this.string.length();
  }


  /**
   * Used by the JSONTWeetFormat; content with only the text, & all the 
   * JSON stuff in the annotation features.
   */
  public Tweet(JsonNode json, String annotationType) {
    string = "";
    Iterator<String> keys = json.fieldNames();
    FeatureMap features = Factory.newFeatureMap();
    annotations = new HashSet<PreAnnotation>();

    while (keys.hasNext()) {
      String key = keys.next();
      features.put(key.toString(), process(json.get(key)));

      if (key.equals(JSONTweetFormat.TEXT_ATTRIBUTE)) {
        string = StringEscapeUtils.unescapeHtml(json.get(key).asText());
      }
    }
    
    annotations.add(new PreAnnotation(0L, string.length(), annotationType, features));
  }
  
  
  /** Empty constructor for an empty result. 
   */
  public Tweet() {
    string = "";
    annotations = new HashSet<PreAnnotation>();
  }

  
  
  private static Object process(JsonNode node) {
    /* JSON types: number, string, boolean, array, object (dict/map),
     * null.  All map keys are strings.
     */

    if (node.isBoolean()) {
      return node.asBoolean();
    }
    if (node.isDouble()) {
      return node.asDouble();
    }
    if (node.isInt()) {
      return node.asInt();
    }
    if (node.isTextual()) {
      return node.asText();
    }
      
    if (node.isNull()) {
      return null;
    }
    
    if (node.isArray()) {
      List<Object> list = new ArrayList<Object>();
      for (JsonNode item : node) {
        list.add(process(item));
      }
      return list;
    }

    if (node.isObject()) {
      FeatureMap map = Factory.newFeatureMap();
      Iterator<String> keys = node.fieldNames();
      while (keys.hasNext()) {
        String key = keys.next();
        map.put(key, process(node.get(key)));
      }
      return map;
    }

    return node.toString();
  }
  
  

//  public Document toDocument(List<String> keepFeatures, FeatureMap contentItems) throws GateException {
//    FeatureMap parameters = Factory.newFeatureMap();
//    parameters.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, "");
//    Document doc = (Document) Factory.createResource(DocumentImpl.class.getName(), parameters);
//    //doc.setSourceUrl(sourceUrl);
//    
//    // this is wrong: we need various strings with content annotations over them
//    DocumentContent newContent= new DocumentContentImpl(this.getString());
//    doc.setContent(newContent);
//    AnnotationSet originalMarkups = doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
//
//    originalMarkups.add(0L, newContent.size(), JSONTweetFormat.TWEET_ANNOTATION_TYPE, Factory.newFeatureMap());
//
//    // TODO: copy all the keepFeatures
//    
//    return doc;
//  }

  
  
}
