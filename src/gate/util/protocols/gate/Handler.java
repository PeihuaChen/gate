package gate.util.protocols.gate;

import java.net.*;
import java.io.*;

import gate.util.*;

/**
 * The handler for the "gate://" URLs.
 * All this class does is to transparently transform a "gate://" URL into
 * an URL of the according type and forward all requests through it.
 */
public class Handler extends URLStreamHandler {

  protected URLConnection openConnection(URL u) throws java.io.IOException {
    URL actualURL = Handler.class.getResource(
                      Files.getResourcePath() + u.getPath()
                    );
    if(actualURL == null) throw new FileNotFoundException(u.toExternalForm());
    return actualURL.openConnection();
  }
}
