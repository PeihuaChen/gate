/*
 *  TweetUtils.java
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

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;
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

public class TweetUtils  {
  
  public static final String PATH_SEPARATOR = ":";
  
  

  public static List<Tweet> readTweets(String string) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string, null, null);
    }
  
    // implied else
    return readTweetLines(string, null, null);
  }


  public static List<Tweet> readTweets(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string, contentKeys, featureKeys);
    }

    // implied else
    return readTweetLines(string, contentKeys, featureKeys);
  }
  
  
  public static List<Tweet>readTweetLines(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    
    // just not null, so we can use it in the loop 
    String[] lines = string.split("[\\n\\r]+");
    for (String line : lines) {
      if (line.length() > 0) {
        JsonNode jnode = mapper.readTree(line);
        tweets.add(new Tweet(jnode));
      }
    }
    
    return tweets;
  }
  
  
  
  public static List<Tweet> readTweetList(String string, List<String> contentKeys, List<String> featureKeys) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    ArrayNode jarray = (ArrayNode) mapper.readTree(string);
    for (JsonNode jnode : jarray) {
      tweets.add(new Tweet(jnode));
    }
    return tweets;
  }


  public static FeatureMap filterFeatures(FeatureMap source, Collection<String> keep) {
    FeatureMap result = Factory.newFeatureMap();
    for (Object key : source.keySet()) {
      if (keep.contains(key.toString())) {
        result.put(key, source.get(key));
      }
    }
    return result;
  }
  
  
  public static FeatureMap flatten(FeatureMap features, String separator) {
    return flatten(features, "", separator);
  }
  
  
  private static FeatureMap flatten(Map<?, ?> map, String prefix, String separator) {
    FeatureMap flattened = Factory.newFeatureMap();

    for (Object key : map.keySet()) {
      String flatKey = prefix + key.toString();
      Object value = map.keySet();
      if (value instanceof Map) {
        flattened.putAll(flatten((Map<?, ?>) value, flatKey + separator, separator));
      }
      else {
        flattened.put(flatKey, value);
      }
    }
    return flattened;
  }
  
  
  public static Object process(JsonNode node) {
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

  

  public static FeatureMap process(JsonNode node, List<String> keepers) {
    FeatureMap all = (FeatureMap) process(node);
    FeatureMap found = Factory.newFeatureMap();
    for (String keeper : keepers) {
      String[] keySequence = StringUtils.split(keeper, PATH_SEPARATOR);
      Object value = explore(all, keySequence);
      if (value != null) {
        found.put(keeper, value);
      }
    }
    return found;
  }
  
  
  // TODO: carry out the recursion on the JSON object instead of converting it to
  // a FeatureMap first  
  
  private static Object explore(FeatureMap map, String[] keySequence) {
    if (keySequence.length < 1) {
      return null;
    }
    
    if (map.containsKey(keySequence[0])) {
      Object value = map.get(keySequence[0]); 
      if (keySequence.length == 1) {
        return value;
      }
      else if (value instanceof FeatureMap){
        String[] remainingKeys = (String[]) ArrayUtils.subarray(keySequence, 1, keySequence.length);
        return explore((FeatureMap) value, remainingKeys);
      }
    }
    
    return null;
  }
  

}
