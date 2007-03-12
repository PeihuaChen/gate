/*
 *  URI.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: URI.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

/**
 * Each resource has a unique URI in the Ontology. Each URI has a
 * namespace and a name which all together makes a qualified URI. e.g.
 * namespace: http://gate.ac.uk, aResourceName: Person , URI =
 * http://gate.ac.uk#Person. It is possible to have a URI for an
 * anonymouse resource, in which case the namespace is set to an empty
 * string and a flag (isAnonymouseResource) is set to true.
 * 
 * @author niraj
 * 
 */
public class URI {

  /**
   * Namespace for this URI (in current version - a value before the
   * last occurance of '#' or '/')
   */
  protected String namespace;

  /**
   * A Resource name (in current version - a value after the last
   * occurance of '#' or '/')
   */
  protected String aResourceName;

  /**
   * String representation of the URI
   */
  protected String uri;

  /**
   * Denotes whether the OResource this URI belongs to is an anonymous
   * or not.
   */
  protected boolean isAnonymousResource;

  /**
   * Constructor
   * 
   * @param uri
   * @param isAnonymousResource
   * @throws InvalidURIException
   */
  public URI(String uri, boolean isAnonymousResource)
          throws InvalidURIException {
    this.isAnonymousResource = isAnonymousResource;
    if(!this.isAnonymousResource) {
      int index = uri.lastIndexOf('#');
      if(index < 0) {
        index = uri.lastIndexOf('/');
        if(index < 0) throw new InvalidURIException("Invalid URI :" + uri);
        if(index + 2 > uri.length())
          throw new InvalidURIException("Invalid URI :" + uri);
        this.uri = uri;
        this.namespace = "";
        this.aResourceName = uri.substring(index + 1, uri.length());
      }
      else {
        this.uri = uri;
        this.namespace = uri.substring(0, index + 1);
        this.aResourceName = uri.substring(index + 1, uri.length());
      }
    }
    else {
      this.uri = uri;
      this.namespace = "";
      this.aResourceName = "[" + uri + "]";
    }
  }

  /**
   * Retrieves the name space part from the URI. In this implementation
   * it retrieves the string that appears before the last occurance of
   * '#' or '/'.
   * 
   * @return
   */
  public String getNameSpace() {
    return this.namespace;
  }

  /**
   * Retrieves the resource name from the given URI. In this
   * implementation it retrieves the string that appears after the last
   * occurance of '#' or '/'.
   * 
   * @return
   */
  public String getResourceName() {
    return this.aResourceName;
  }

  /**
   * Returns the string representation of the uri. In case of anonymous
   * class, it returns the '[' + resourcename + ']'.
   */
  public String toString() {
    return this.uri;
  }

  /**
   * Indicates whether the URI refers to an anonymous resource
   * 
   * @return
   */
  public boolean isAnonymousResource() {
    return this.isAnonymousResource;
  }
}
