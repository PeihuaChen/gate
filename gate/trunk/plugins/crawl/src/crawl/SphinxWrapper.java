package crawl;

import websphinx.*;

import gate.creole.*;
import gate.util.*;
import gate.corpora.*;
import gate.*;

import java.net.*;


public class SphinxWrapper extends Crawler{
    private Corpus corpus = null;
    private static int maxPages = -1;
    private static int count = 0;

    public void visit(Page p) {
	if ((maxPages != -1) && (count >= maxPages)) {
	    this.count = 0;
	    super.stop();
	    return;
	}

	String docName = p.toURL() + "_" + Gate.genSym();
	FeatureMap params = Factory.newFeatureMap();
	params.put(Document.DOCUMENT_URL_PARAMETER_NAME, p.toURL());
	try {
            Document doc = (Document) Factory.createResource(
							     DocumentImpl.class.getName(), params, null, docName
							     );
            corpus.add(doc);
	    System.out.println(count+" ["+p.getDepth()+"] "+p.toURL());
	    count++;
	}
	catch (ResourceInstantiationException e) {
            String nl = Strings.getNl();
            Err.prln(
		     "WARNING: could not intantiate document" + nl +
		     "  Document name was: " + docName + nl +
		     "  Exception was: " + e + nl + nl
		     );
	}
    }
    
    public boolean shouldVisit(Link l) {
	return super.shouldVisit(l);
    }
    
    public void setDepth(int depth) {
	super.setMaxDepth(depth);
    }

    public void setMaxPages(int max) {
	this.maxPages = max;
    }
    
    public int getMaxPages() {
	return maxPages;
    }
   

    public void setStart(String root) {
	//super.setDepthFirst(false);
	//super.setDomain(Crawler.SUBTREE);
	try {
	    URL url = new URL(root);
	    Link link = new Link(url);
	    //super.markVisited(link);
	    super.setRoot(link);
	}catch (MalformedURLException me) {
	    System.err.println("Malformed url "+root);
	    me.printStackTrace();
	}
    }

     public void setStart(URL root) {
	 Link link = new Link(root);
	 super.setRoot(link);
     }
    
    public void setCorpus(Corpus corpus) {
	this.corpus = corpus;
    }

    public void start() {
	super.run();
    }
   
  }