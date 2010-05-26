/*
 *  CrawlPR.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 */
package crawl;

import websphinx.*;
import java.util.*;
import gate.creole.*;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.corpora.*;
import gate.*;
import java.net.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.lang.StringUtils;


public class SphinxWrapper extends Crawler{

  private static final long serialVersionUID = -6524027714398026402L;
  @SuppressWarnings("unused")
  private static final String __SVNID = "$Id";

  private Corpus corpus = null;
  private static int maxPages = -1;
  private static int maxKeep  = -1;
  private static int countFetched = 0;
  private static int countKept    = 0;
  private static boolean ignoreKeywords;
  private static boolean caseSensitiveKeywords;
  private static boolean convertXmlTypes;
  private static List<String> keywords;

  
  public void setKeywords(List<String> newKeywords, boolean caseSensitive) {
    keywords = newKeywords;
    ignoreKeywords = (keywords == null) || keywords.isEmpty();
    caseSensitiveKeywords = caseSensitive;
  }
  
  public void setConvertXmlTypes(boolean convert) {
    convertXmlTypes = convert;
  }
  
  
  @SuppressWarnings("unchecked")
  public void visit(Page p) {
    if ( ( (maxPages != -1) && (countFetched >= maxPages) ) ||
         ( (maxKeep != -1) && (countKept >= maxKeep) ) )    {
      resetCounter();
      syncIfNecessary();
      
      super.stop();
      return;
    }

    countFetched++;
    String url = p.toURL();
    FeatureMap params = Factory.newFeatureMap();
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME, url);

    String docName = url;
    if (url.length() > 40) {
      docName = StringUtils.substring(url, url.length() - 40);
    }
    docName = docName.replaceAll("[^\\p{ASCII}]", "_") + "_" + Gate.genSym();

    /* Take advantage of the MIME type from the server when
     * constructing the GATE document.      */
    String contentTypeStr = p.getContentType();
    String originalMimeType = null;

    if (contentTypeStr != null) {
      try {
        ContentType contentType = new ContentType(contentTypeStr);
        String mimeType = contentType.getBaseType();
        String encoding = contentType.getParameter("charset");

        if (mimeType != null) {
          if (convertXmlTypes) {
            originalMimeType = mimeType;
            mimeType = convertMimeType(mimeType);
            if (! originalMimeType.equals(mimeType)) {
              System.out.println("   convert " + originalMimeType + " -> " + mimeType);
            }
          }
          params.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, mimeType);
        }
        
        if (encoding != null) {
          params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, encoding);
          
        }
      } catch(ParseException e) {
        e.printStackTrace();
      }
    }


    try {
      Document doc = (Document) Factory.createResource(
              DocumentImpl.class.getName(), params, null, docName);
      
      if (ignoreKeywords || CrawlPR.containsAnyKeyword(doc, keywords, caseSensitiveKeywords)) {
        /* Use the Last-Modified HTTP header if available.  */
        long lastModified = p.getLastModified();
        Date date;
        if (lastModified > 0L) {
          date = new Date(lastModified);
        }
        else {
          date = new Date();
        }
        doc.getFeatures().put("Date", date);
        
        if (originalMimeType != null) {
          doc.getFeatures().put("originalMimeType", originalMimeType);
        }
        
        corpus.add(doc);
        
        if (corpus.getLRPersistenceId() != null) {
          corpus.unloadDocument(doc);
          Factory.deleteResource(doc);
        }
        
        countKept++;
        System.out.println("Keep " + countKept + " / " + countFetched + 
                " [" + p.getDepth() + "] " + p.toURL());
      }
      
      else {
        System.out.println("Drop " + countKept + " / " + countFetched + 
                " [" + p.getDepth() + "] " + p.toURL());
        Factory.deleteResource(doc);
      }

    }
    catch (ResourceInstantiationException e) {
      System.err.println("WARNING: could not intantiate document " + docName);
      e.printStackTrace();
      System.out.println("Drop " + countKept + " / " + countFetched + 
              " [" + p.getDepth() + "] " + p.toURL());
    }
  }

  public boolean shouldVisit(Link l) {
    return super.shouldVisit(l);
  }

  public void setDepth(int depth) {
    super.setMaxDepth(depth);
  }

  public void setMaxPages(int max) {
    maxPages = max;
  }
  
  public void setMaxKeep(int max) {
    maxKeep = max;
  }

  public int getMaxPages() {
    return maxPages;
  }
  
  public int getMaxKeep() {
    return maxKeep;
  }


  protected void addStartLink(String root) {
    try {
      URL url = new URL(root);
      Link link = new Link(url);
      System.out.println("Adding seed URL  " + url.toString());
      super.addRoot(link);
    }
    catch (MalformedURLException me) {
      System.err.println("Malformed url "+root);
      me.printStackTrace();
    }
  }

  protected void addStartLink(URL url) {
    Link link = new Link(url);
    System.out.println("Adding seed URL  " + url.toString());
    super.addRoot(link);
  }
  

  public void setCorpus(Corpus corpus) {
    this.corpus = corpus;
  }


  /* yes: application/rss+xml.xml
   * no:  image/svg+xml.xml
   */
  private String convertMimeType(String originalType) {
    String result = originalType;
    if (originalType.endsWith("xml")
            && (originalType.startsWith("application") || originalType.startsWith("application") )
    ) {
      result = "text/xml";
    }
    return result;
  }
  
  
  public void start() {
    super.run();
  }
  
  protected void resetCounter() {
    countFetched = 0;
    countKept = 0;
  }
  
  protected void interrupt()  {
    super.stop();
    syncIfNecessary();
  }

  private void syncIfNecessary() {
    if (corpus.getLRPersistenceId() != null) {
      try {
        corpus.sync();
      }
      catch(PersistenceException e) {
        e.printStackTrace();
      }
      catch(SecurityException e) {
        e.printStackTrace();
      }
    }
  }

  
  
}