/*
 *  JSONTweetFormat.java
 *
 *  Copyright (c) 1995-2013, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 */
package gate.corpora;

import gate.AnnotationSet;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.GateConstants;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/** Document format for handling JSON tweets: either one 
 *  object {...} or a list [{tweet...}, {tweet...}, ...].
 */
@CreoleResource(name = "GATE JSON Tweet Document Format", isPrivate = true,
    autoinstances = {@AutoInstance(hidden = true)})

public class JSONTweetFormat extends TextualDocumentFormat {
  private static final long serialVersionUID = 6878020036304333918L;

  public static final String TEXT_ATTRIBUTE = "text";
  
  /** Default construction */
  public JSONTweetFormat() { super();}

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register ad hoc MIME-type
    // There is an application/json mime type, but I don't think
    // we want everything to be handled this way?
    MimeType mime = new MimeType("text","x-json-twitter");
    // Register the class handler for this MIME-type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(), this);
    // Register the mime type with string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file suffixes for this mime type
    suffixes2mimeTypeMap.put("json", mime);
    // Register magic numbers for this mime type
    //magic2mimeTypeMap.put("Subject:",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }
  
  @Override
  public void cleanup() {
    super.cleanup();
    
    MimeType mime = getMimeType();
    
    mimeString2ClassHandlerMap.remove(mime.getType()+ "/" + mime.getSubtype());
    mimeString2mimeTypeMap.remove(mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeTypeMap.remove("json");
  }

  @Override
  public void unpackMarkup(gate.Document doc) throws DocumentFormatException{
    if ( (doc == null) || (doc.getSourceUrl() == null && doc.getContent() == null) ) {
      throw new DocumentFormatException("GATE document is null or no content found. Nothing to parse!");
    }

    setNewLineProperty(doc);
    String jsonString = StringUtils.trimToEmpty(doc.getContent().toString());
    try {
      // Parse the String
      List<Tweet> tweets = readTweets(jsonString);
      
      // Put them all together to make the unpacked document content
      StringBuilder concatenation = new StringBuilder();
      for (Tweet tweet : tweets) {
        tweet.setStart(concatenation.length());
        concatenation.append(tweet.getString()).append("\n\n");
      }

      // Set new document content 
      DocumentContent newContent = new DocumentContentImpl(concatenation.toString());
      doc.edit(0L, doc.getContent().size(), newContent);

      AnnotationSet originalMarkups = doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
      // Create Original markups annotations for each tweet
      for (Tweet tweet : tweets) {
        originalMarkups.add(tweet.getStart(), tweet.getEnd(), "Tweet", tweet.getFeatures());
      }
    }
    catch (InvalidOffsetException e) {
      throw new DocumentFormatException(e);
    } 
    catch(IOException e) {
      throw new DocumentFormatException(e);
    }
  }
  
  
  
  private List<Tweet> readTweetList(String string) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Tweet> tweets = new ArrayList<Tweet>();
    ArrayNode jarray = (ArrayNode) mapper.readTree(string);
    for (JsonNode jnode : jarray) {
      tweets.add(new Tweet(jnode));
    }
    return tweets;
  }
  
  
  private List<Tweet> readTweets(String string) throws IOException {
    if (string.startsWith("[")) {
      return readTweetList(string);
    }

    // implied else
    return readTweetLines(string);
  }
  
  
  private List<Tweet>readTweetLines(String string) throws IOException {
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


class Tweet {
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
      if (key.equals("text")) {
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

}
