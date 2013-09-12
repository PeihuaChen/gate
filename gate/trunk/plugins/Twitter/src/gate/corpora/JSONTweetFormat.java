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
 *  
 *  $Id$
 */
package gate.corpora;

import gate.*;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;
import gate.corpora.twitter.*;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang.StringUtils;


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
      List<Tweet> tweets = TweetUtils.readTweets(jsonString);
      
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

}
