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
import gate.creole.*;


/** implementation of a gazetteer list */
public class GazetteerList extends gate.creole.AbstractLanguageResource
implements List {

  /** the url of this list */
  private URL url;
  private String encoding = "UTF-8";

  /** flag indicating whether the list has been modified after loading/storing */
  private boolean isModified = false;

  private List entries = new ArrayList();

  /** create a new gazetteer list */
  public GazetteerList() {
  }

  /** @return true if the list has been modified after load/store  */
  public boolean isModified() {
    return isModified;
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
   * @throws ResourceInstantiationException
   */
  public void load() throws ResourceInstantiationException {
    try {
      if (null == url) {
        throw new ResourceInstantiationException("URL not specified (null).");
      }

      BufferedReader listReader;

      listReader = new BufferedReader(new InputStreamReader(
                              (url).openStream(), encoding));
      String line;
      while (null != (line = listReader.readLine())) {
        entries.add(line);
      } //while

      listReader.close();
    } catch (Exception x) {
      throw new ResourceInstantiationException(x);
    }
    isModified = false;
  } // load ()

  /**
   * store the list to the specified url
   * @throws ResourceInstantiationException
   */
  public void store() throws ResourceInstantiationException{
    try {
      if (null == url) {
        throw new ResourceInstantiationException("URL not specified (null)");
      }

      URL tempUrl = url;
      if (-1 != url.getProtocol().indexOf("gate")) {
        tempUrl = gate.util.protocols.gate.Handler.class.getResource(
                      gate.util.Files.getResourcePath() + url.getPath()
                    );
      } // if gate:path url

      File fileo = new File(tempUrl.getFile());

      fileo.delete();
      BufferedWriter listWriter = new BufferedWriter(new FileWriter(fileo));
      Iterator iter = entries.iterator();
      while (iter.hasNext()) {
        listWriter.write(iter.next().toString());
        listWriter.newLine();
      }
      listWriter.close();
    } catch (Exception x) {
      throw new ResourceInstantiationException(x);
    }
    isModified = false;
  } // store()

  public void setURL(URL theUrl) {
    url = theUrl;
    isModified = true;
  }

  public URL getURL() {
    return url;
  }

/*--------------implementation of java.util.List--------------------*/
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
    isModified |= result;
    return result;
  } // add()

  public boolean remove(Object o) {
    boolean result = entries.remove(o);
    isModified |= result;
    return result;
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
    isModified |= result;

    return result;
  } // addAll(Collection)

  public boolean addAll(int index, Collection c) {
    boolean result = entries.addAll(index,c);
    isModified |= result;
    return result;
  } //addAll(int,Collection)


  public boolean removeAll(Collection c) {
    boolean result = entries.removeAll(c);
    isModified |= result;
    return result;
  }

  public boolean retainAll(Collection c) {
    boolean result = entries.retainAll(c);
    isModified |= result;
    return result;
  }

  public void clear() {
    if (0 < entries.size())
      isModified = true;
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



  public Object get(int index) {
    return entries.get(index);
  }

  public Object set(int index, Object element) {
    isModified=true;
    return entries.set(index,element);
  }

  public void add(int index, Object element) {
    isModified = true;
    entries.add(index,element);
  }

  public Object remove(int index) {
    int size = entries.size();
    Object result = entries.remove(index);
    isModified |= (size!=entries.size());
    return result;
  }

  public int indexOf(Object o) {
    return entries.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return entries.lastIndexOf(o);
  }

  public ListIterator listIterator() {
    return entries.listIterator();
  }

  public ListIterator listIterator(int index) {
    return entries.listIterator(index);
  }

  public List subList(int fromIndex, int toIndex) {
    return entries.subList(fromIndex,toIndex);
  }


  /** dumps all the entries to one string */
  public String toString() {
    StringBuffer result = new StringBuffer();
    String entry = null;
    for ( int i = 0 ; i < entries.size() ; i++) {
      entry = ((String)entries.get(i)).trim();
      if (entry.length()>0) {
        result.append(entry);
        result.append("\n");
      }// if
    }// for
    return result.toString();
  }//toString()

  /** updates the content of the gaz list with the given parameter
   *  @param newContent the new content of the gazetteer list */
  public void updateContent(String newContent) {
    BufferedReader listReader;


    listReader = new BufferedReader(new StringReader(newContent));
    String line;
    List tempEntries = new ArrayList();
    try {
      while (null != (line = listReader.readLine())) {
        tempEntries.add(line);
      } //while
      listReader.close();
    } catch (IOException x) {
      /**should never be thrown*/
      throw new gate.util.LuckyException("IOException :"+x.getMessage());
    }

    isModified = !tempEntries.equals(entries);
    clear();
    entries = tempEntries;

  } // updateContent(String)

} // Class GazetteerList
