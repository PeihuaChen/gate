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
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;
import gate.corpora.JSONTweetFormat;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;


// JSON API
// http://json-lib.sourceforge.net/apidocs/jdk15/index.html
// Jackson API
// http://wiki.fasterxml.com/JacksonHome

// Standard: RFC 4627
// https://tools.ietf.org/html/rfc4627


public class Tweet {
  private String string;
  private FeatureMap features;
  private long start;
  
  public int getLength() {
    return this.string.length();
  }

  public String getString() {
    return this.string;
  }
  
  public FeatureMap getFeatures() {
    return this.features;
  }
  
  public void setStart(long start) {
    this.start = start;
  }
  
  public long getStart() {
    return this.start;
  }
  
  public long getEnd() {
    return this.start + this.string.length();
  }

  
  public Tweet(JsonNode json) {
    string = "";
    Iterator<String> keys = json.fieldNames();
    features = Factory.newFeatureMap();

    while (keys.hasNext()) {
      String key = keys.next();
      if (key.equals(JSONTweetFormat.TEXT_ATTRIBUTE)) {
        string = StringEscapeUtils.unescapeHtml(json.get(key).asText());
      }
      else {
        features.put(key.toString(), process(json.get(key)));
      }
    }
  }
  
  
  public Tweet() {
    string = "";
    features = Factory.newFeatureMap();
  }

  
  private Object process(JsonNode node) {
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
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator<String> keys = node.fieldNames();
      while (keys.hasNext()) {
        String key = keys.next();
        map.put(key, process(node.get(key)));
      }
      return map;
    }

    return node.toString();
  }
  
  
  //public DocumentImpl toDocument(List<String> keepFeatures, FeatureMap contentItems) {
  //}
  
  
}
