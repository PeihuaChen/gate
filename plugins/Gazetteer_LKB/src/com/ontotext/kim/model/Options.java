package com.ontotext.kim.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParser;

import com.ontotext.kim.KIMConstants;

/**
 * Options reader for the LKB Gazetteer
 * 
 * @author mnozchev
 */
public class Options {

  private final Map<String, String> opts;
  private final static Logger log = Logger.getLogger(Options.class);
  
  public Options(Map<String, String> opts) {
    this.opts = opts;
  }
  
  /**
   * Loads the configuration of the dictionary from a "config.ttl" Turtle RDF
   * 
   * @param dictionaryPath the dictionary path, not null
   * @return an Options instance, no null
   */
  public static Options load(File dictionaryPath) {
    Map<String, String> opts = new HashMap<String, String>();
    Reader inp = null;
    try {
      inp = new FileReader(new File(dictionaryPath, getConfigFileName()));
      RDFParser parser = new TurtleParser();
      Graph statements = new GraphImpl();
      parser.setRDFHandler(new StatementCollector(statements));      
      parser.parse(inp, "http://www.ontotext.com/lkb_gazetteer#");
      Iterator<Statement> it = statements.match(new URIImpl("http://www.ontotext.com/lkb_gazetteer#DictionaryConfiguration"), null, null);
      while (it.hasNext()) {
        Statement st = it.next();
        opts.put(st.getPredicate().getLocalName().toLowerCase(), st.getObject().stringValue());
      }
    }
    catch(IOException e) {
      log.error("Could not read config file from " + new File(dictionaryPath, getConfigFileName()).getAbsolutePath(), e);
    }
    catch(RDFParseException e) {
      log.error("Config file accessible but was not a valid Turtle RDF.", e);
    }
    catch(RDFHandlerException e) {
      log.error("Unexpected error when reading the configuration RDF.", e);
    }
    finally {
      IOUtils.closeQuietly(inp);
    }
    return new Options(Collections.unmodifiableMap(opts));
  }

  public static String getConfigFileName() {    
    return "config.ttl";
  }

  public boolean isCacheEnabled() {    
    return "enabled".equalsIgnoreCase(opts.get("caching"));
  }

  public String getCaseSensitivity() {    
    String res = opts.get("casesensitivity");
    return res != null ? res : KIMConstants.CASE_INSENSITIV;
  }
  
  public Map<String, String> getMap() {
    return opts; // unmodifiable
  }
}
