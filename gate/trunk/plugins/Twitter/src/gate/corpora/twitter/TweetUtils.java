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
  

  public static List<Tweet> readTweetList(String string, String annotationType) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    ArrayNode jarray = (ArrayNode) mapper.readTree(string);
    for (JsonNode jnode : jarray) {
      tweets.add(new Tweet(jnode, annotationType));
    }
    return tweets;
  }
  
  
  public static List<Tweet> readTweets(String string, String annotationType) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string, annotationType);
    }

    // implied else
    return readTweetLines(string, annotationType);
  }
  
  
  public static List<Tweet>readTweetLines(String string, String annotationType) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    List<Tweet> tweets = new ArrayList<Tweet>();
    
    // just not null, so we can use it in the loop 
    String[] lines = string.split("[\\n\\r]+");
    for (String line : lines) {
      if (line.length() > 0) {
        JsonNode jnode = mapper.readTree(line);
        tweets.add(new Tweet(jnode, annotationType));
      }
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
  

  

}
