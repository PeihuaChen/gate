/*
 * GazetteerList.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 02/2002
 *
 */

package gate.creole.gazetteer;

import java.util.*;
import java.io.*;
import java.net.*;
import com.ontotext.gate.exception.*;

/** implementation of a gazetteer list */
public class GazetteerList extends gate.creole.AbstractLanguageResource
implements Set {

  /** the url of this list */
  private URL url;
  private String encoding = "UTF-8";

  private Set entries = new HashSet();

  /** create a new gazetteer list */
  public GazetteerList() {
  }

  /** set the encoding of the list
   *  @param encod the encoding to be set */
  public void setEncoding(String encod) {
    encoding = encod;
  }

  /** get the encoding of the list
   *  @return the encoding of the list*/
  public String getEncoding() {
    return encoding;
  }

  /**
   * loads a gazetteer list
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void load() throws FileNotFoundException, IOException {
    if (null == url) {
      throw new URLNotSpecifiedException();
    }

    BufferedReader listReader;

    listReader = new BufferedReader(new InputStreamReader(
                            (url).openStream(), encoding));
    String line;
    while (null != (line = listReader.readLine())) {
      entries.add(line);
    } //while

    listReader.close();
  } // load ()

  /**
   * store the list to the specified url
   * @throws IOException
   */
  public void store() throws IOException{
    if (null == url) {
      throw new URLNotSpecifiedException();
    }

    File fileo = new File(url.getPath()+url.getFile());
    fileo.delete();
    BufferedWriter listWriter = new BufferedWriter(new FileWriter(fileo));
    Iterator iter = entries.iterator();
    while (iter.hasNext()) {
      listWriter.write(iter.next().toString());
      listWriter.newLine();
    }
    listWriter.close();
  } // store()

  public void setURL(URL theUrl) {
    url = theUrl;
  }

  public URL getURL() {
    return url;
  }

  /*---implementation of interface java.util.Set---*/

  public int size() {
    return entries.size();
  }

  public boolean isEmpty() {
    return (0 == entries.size());
  }

  public boolean contains(Object o) {
    return entries.contains(o);
  } // contains()

  /*it is not dangerous if the iterator is modified since there
  are no dependencies of entries to other members  */
  public Iterator iterator() {
    return entries.iterator();
  }

  public Object[] toArray() {
    return entries.toArray();
  }

  public Object[] toArray(Object[] a) {
    return toArray(a);
  }

  public boolean add(Object o) {
    boolean result = false;
    if (o instanceof String) {
      result = entries.add(o);
    }
    return result;
  } // add()

  public boolean remove(Object o) {
    return entries.remove(o);
  }

  public boolean containsAll(Collection c) {
    return entries.containsAll(c);
  }

  /**
   * add entire collection
   * @param c a collection to be addded
   * @return true if all the elements where Strings and all are sucessfully added
   */
  public boolean addAll(Collection c) {
    Iterator iter = c.iterator();
    Object o;
    boolean result = false;

    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof String) {
        result |= entries.add(o);
      }
    } // while
    return result;
  } // addAll()

  public boolean removeAll(Collection c) {
    return entries.removeAll(c);
  }

  public boolean retainAll(Collection c) {
    return entries.retainAll(c);
  }

  public void clear() {
    entries.clear();
  }

  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof GazetteerList) {
      result = true;
      GazetteerList list2 = (GazetteerList) o;
      result &= entries.equals(list2.entries);
    } // if
    return result;
  } // equals()

  /*---end of implementation of interface java.util.Set---*/

} // Class GazetteerList
