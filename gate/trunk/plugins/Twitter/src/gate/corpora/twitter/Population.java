/*
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
import gate.corpora.DocumentContentImpl;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;


public class Population  {


  public static void populateCorpus(final Corpus corpus, URL inputUrl, String encoding, List<String> contentKeys,
      List<String> featureKeys, int tweetsPerDoc) throws ResourceInstantiationException {
    try {
      InputStream input = inputUrl.openStream();
      List<String> lines = IOUtils.readLines(input, encoding);
      IOUtils.closeQuietly(input);
      
      // TODO: sort this out so it processes one at a time instead of reading the
      // whole hog into memory
      
      // For now, we assume the streaming API format (concatenated maps, not in a list)
      List<Tweet> tweets = TweetUtils.readTweetStrings(lines, contentKeys, featureKeys);
      
      int digits = (int) Math.ceil(Math.log10((double) tweets.size()));
      int tweetCounter = 0;
      Document document = newDocument(inputUrl, tweetCounter, digits);
      StringBuilder content = new StringBuilder();
      Map<PreAnnotation, Integer> annotanda = new HashMap<PreAnnotation, Integer>();
      
      for (Tweet tweet : tweets) {
        if ( (tweetCounter > 0) && (tweetsPerDoc > 0) && ((tweetCounter % tweetsPerDoc) == 0) ) {
          closeDocument(document, content, annotanda, corpus);
          document = newDocument(inputUrl, tweetCounter, digits);
          content = new StringBuilder();
          annotanda = new HashMap<PreAnnotation, Integer>();
        }

        int startOffset = content.length();
        content.append(tweet.getString());
        for (PreAnnotation preAnn : tweet.getAnnotations()) {
          annotanda.put(preAnn, startOffset);
        }
        content.append('\n');
      }
      
      if (content.length() > 0) {
        closeDocument(document, content, annotanda, corpus);
      }
      
      if(corpus.getDataStore() != null) {
        corpus.getDataStore().sync(corpus);
      }
      
    }
    catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }
  }


  private static Document newDocument(URL url, int counter, int digits) throws ResourceInstantiationException {
    Document document = Factory.newDocument("");
    String code = StringUtils.rightPad(Integer.toString(counter), digits, '0');
    String name = StringUtils.stripToEmpty(StringUtils.substring(url.getPath(), 1)) + "_" + code;
    document.setName(name);
    document.setSourceUrl(url);
    document.getFeatures().put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, TweetUtils.MIME_TYPE);
    return document;
  }
  
  
  private static void closeDocument(Document document, StringBuilder content, Map<PreAnnotation, Integer> annotanda, Corpus corpus) throws InvalidOffsetException {
    DocumentContent contentImpl = new DocumentContentImpl(content.toString());
    document.setContent(contentImpl);
    AnnotationSet originalMarkups = document.getAnnotations(Gate.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    for (PreAnnotation preAnn : annotanda.keySet()) {
      preAnn.toAnnotation(originalMarkups, annotanda.get(preAnn));
    }
    corpus.add(document);
    
    if (corpus.getLRPersistenceId() != null) {
      corpus.unloadDocument(document);
      Factory.deleteResource(document);
    }
  }


}
