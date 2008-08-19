package gate.compound.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import gate.*;
import gate.compound.CompoundDocument;
import gate.creole.ResourceInstantiationException;

/**
 * Implemention of the CompoundDocument. Compound Document is a set of
 * one or more documents. It provides a more convenient way to group
 * documents and interpret them as a single document. It has a
 * capability to switch the focus among the different memebers of it.
 * 
 * @author niraj
 */
public class CompoundDocumentFromXml extends CompoundDocumentImpl {

  private static final long serialVersionUID = 8114328411647768889L;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    // set up the source URL and create the content
    if(sourceUrl == null) {
      throw new ResourceInstantiationException(
              "The sourceURL and document's content were null.");
    }

    CompoundDocument cd = null;
    try {
      StringBuilder xmlString = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(sourceUrl
              .openStream(), "utf-8"));
      String line = br.readLine();
      while(line != null) {
        xmlString.append("\n").append(line);
        line = br.readLine();
      }
      cd = AbstractCompoundDocument.fromXml(xmlString.toString());
      br.close();
    }
    catch(UnsupportedEncodingException uee) {
      throw new ResourceInstantiationException(uee);
    }
    catch(IOException ioe) {
      throw new ResourceInstantiationException(ioe);
    }
    return cd;
  } // init()

}
