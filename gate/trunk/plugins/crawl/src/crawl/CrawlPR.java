/*
 *  CrawlPR.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Google API and other sources subject to Google License. Please
 *  see http://www.google.com/apis/
 */

package crawl;

import gate.ProcessingResource;
import gate.Resource;
import gate.creole.*;
import gate.gui.MainFrame;
import gate.corpora.*;
import gate.util.*;
import gate.*;

import websphinx.*;

public class CrawlPR extends AbstractLanguageAnalyser
    implements ProcessingResource {

    private String root = null;
    private int depth = -1;
    private Corpus corpus = null;
    private Boolean dfs = null;
    private SphinxWrapper crawler;
    private String domain = null;
    private Corpus source = null;
    private int max = -1;

  /** Constructor of the class*/
  public CrawlPR() {
     
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    corpus = Factory.newCorpus("crawl");
    return super.init();
  }

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
   */
  public void reInit() throws ResourceInstantiationException {
    init();
  }

  /**
       * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {
    crawler = new SphinxWrapper();

    if (root == null && source == null) {
      throw new ExecutionException("Either root or source must be initialized");
    }
    if (depth == -1) {
      throw new ExecutionException("Limit is not initialized");
    }
    if (dfs == null) {
	throw new ExecutionException("dfs is not initialized");
    }
    if (domain == null) {
	throw new ExecutionException("domain type is not initialized.. Set to either SERVER/SUBTREE/WEB");
    }
    
    try {
	crawler.setCorpus(corpus);
	crawler.setDepth(depth);
	crawler.setDepthFirst(dfs.booleanValue());
	if (domain == "SUBTREE") {
	    crawler.setDomain(Crawler.SUBTREE);
	}else 
	    if (domain == "SERVER") {
		crawler.setDomain(Crawler.SERVER);
	    }else {
		crawler.setDomain(Crawler.WEB);
	    }
	if (max != -1) {
	    crawler.setMaxPages(max);
	}
	if (root != null && root !="") {
	    crawler.setStart(root);
	}
	else {
	    CorpusImpl roots = (CorpusImpl) source;
	    //System.out.println("using the corpus"+roots.getDocumentName(0));
	    Object rootArray[] = roots.toArray();
	    for (int i=0; i<rootArray.length; i++) {
		DocumentImpl doc = (DocumentImpl) rootArray[i];
		System.out.println("adding ... "+doc.getSourceUrl().toString()+"\n");
		crawler.setStart(doc.getSourceUrl());
	    }
	}
	//crawler.setDomain(crawlType);
	
	crawler.start();
	
    }
    catch (Exception e) {
	String nl = Strings.getNl();
	Err.prln(
		 "  Exception was: " + e + nl + nl
		 );
    }
  }
    public void setRoot(String root) {
	this.root = root;
    }
    
  public String getRoot() {
    return this.root;
  }

  public void setDepth(Integer limit) {
    this.depth = limit.intValue();
  }

  public Integer getDepth() {
    return new Integer(this.depth);
  }
    
    public void setDfs(Boolean dfs) {
	this.dfs = dfs;
    }
 
    public Boolean getDfs() {
	return this.dfs;
    }
    
    public void setDomain(String domain) {
	this.domain = domain;
    }
    
    public String getDomain() {
	return this.domain;
    }
    
    public void setSource(Corpus source) {
	this.source = source;
    }
    
    public Corpus getSource() {
	return this.source;
    }

    public void setMax(Integer max) {
	this.max = max.intValue();
    }
    
    public Integer getMax() {
	return new Integer(this.max);
    }

}