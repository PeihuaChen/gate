package com.ontotext.kim.util.datastore;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.client.semanticrepository.QueryResultListener;
import com.ontotext.kim.client.semanticrepository.QueryResultListener.Feed;

/**
 * @author mnozchev
 *
 */
public class GazetteerListFeed implements Feed{

  private final File dictionaryPath;
  
  public GazetteerListFeed(File dictionaryPath) {
    this.dictionaryPath = dictionaryPath;
  }

  
  public void feedTo(QueryResultListener listener) throws KIMQueryException {
    
    try {
      listener.startTableQueryResult();
      // for all .def files in dictionary path
        // for all minor/major types with label lists
          // for all labels in labels lists
            //addEntity(minorType, majorType, label);
      listener.endTableQueryResult();
    }
    catch (IOException e) {
      throw new KIMQueryException(e);
    }
    
  }
  
  private void addEntity(QueryResultListener listener, String minorType, String majorType, String label) throws IOException {
    listener.startTuple();
    listener.tupleValue(new LiteralImpl(label));
    listener.tupleValue(new URIImpl("urn:"+minorType));
    listener.tupleValue(new URIImpl("urn:"+majorType));
    listener.endTuple();    
  }

}
