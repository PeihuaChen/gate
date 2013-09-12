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
import gate.creole.ResourceInstantiationException;
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;
import gate.corpora.*;
import java.io.IOException;
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

public class TweetUtils  {
  

  public static List<Tweet> readTweetList(String string) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    ArrayNode jarray = (ArrayNode) mapper.readTree(string);
    for (JsonNode jnode : jarray) {
      tweets.add(new Tweet(jnode));
    }
    return tweets;
  }
  
  
  public static List<Tweet> readTweets(String string) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string);
    }

    // implied else
    return readTweetLines(string);
  }
  
  
  public static List<Tweet>readTweetLines(String string) throws IOException {
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
  
  

}
