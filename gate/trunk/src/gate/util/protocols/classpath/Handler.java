package gate.util.protocols.classpath;

import java.net.*;
import java.io.*;

import gate.util.*;
import gate.*;

/**
 * The handler for the "classpath://" URLs.
 * All this class does is to transparently transform a "classpath://" URL into
 * an URL of the according type and forward all requests through it.
 */
public class Handler extends URLStreamHandler {

  protected URLConnection openConnection(URL u) throws java.io.IOException {
    URL actualURL = Handler.class.getResource(u.getPath());
    if(actualURL == null) throw new FileNotFoundException(u.toExternalForm());
    return actualURL.openConnection();
  }
}
