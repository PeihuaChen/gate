/**
 * (c) Copyright Ontotext Lab, Sirma Group Corp 2004
 */

package com.ontotext.gate.gazetteer;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.gazetteer.AbstractGazetteer;
import gate.creole.gazetteer.GazetteerException;
import gate.creole.gazetteer.GazetteerList;
import gate.creole.gazetteer.GazetteerNode;
import gate.creole.gazetteer.LinearDefinition;
import gate.creole.gazetteer.LinearNode;
import gate.creole.gazetteer.Lookup;
import gate.creole.gazetteer.MappingNode;
import gate.util.InvalidOffsetException;
import gate.util.LuckyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HashGazetteer extends AbstractGazetteer {
  private static final long serialVersionUID = -4603155688378104052L;

  private ArrayList<Lookup> categoryList;

  private Map<LinearNode, GazetteerList> listsByNode;

  private Map<String, List<Lookup>> mapsList[];

  private int mapsListSize;

  private AnnotationSet annotationSet;

  @SuppressWarnings("unchecked")
  public HashGazetteer() {
    categoryList = null;
    mapsList = new HashMap[1000];
    mapsListSize = 0;
  }

  @SuppressWarnings("unchecked")
  public Resource init() throws ResourceInstantiationException {
    if(listsURL == null)
      throw new ResourceInstantiationException(
              "No URL provided for gazetteer creation!");
    try {
      definition = new LinearDefinition();
      definition.setURL(listsURL);
      definition.load();
      int i = definition.size();
      listsByNode = definition.loadLists();
      mapsListSize = mapsList.length;
      categoryList = new ArrayList<Lookup>(i + 1);
      Iterator<LinearNode> iterator = definition.iterator();
      int j = 0;
      LinearNode linearnode;
      for(; iterator.hasNext(); readList(linearnode)) {
        linearnode = (LinearNode)iterator.next();
        fireStatusChanged("Reading " + linearnode.toString());
        fireProgressChanged((++j * 100) / i);
      }

      fireProcessFinished();
    }
    catch(Exception exception) {
      throw new ResourceInstantiationException(exception);
    }
    return this;
  }

  public void execute() throws ExecutionException {
    if(document == null) throw new ExecutionException("Document is null!");

    if(annotationSetName == null || annotationSetName.equals(""))
      annotationSet = document.getAnnotations();
    else annotationSet = document.getAnnotations(annotationSetName);

    String s = document.getContent().toString() + " ";

    int i = s.length();
    int j = 0;
    int k = 0;

    StringBuffer stringbuffer = new StringBuffer();
    boolean prevIsSymbol = false;
    boolean prevIsDigit = false;
    boolean prevIsLetter = false;

    // TODO what does this do, as it is only ever set to false
    boolean flag11 = false;

    String s3 = "";
    int i1 = 0;
    int j1 = 0;

    for(int l1 = 0; l1 < i; l1++) {
      char c = s.charAt(l1);
      boolean currIsWhitespace = Character.isWhitespace(c);
      if(currIsWhitespace && stringbuffer.length() == 0) {
        j++;
        prevIsLetter = prevIsDigit = prevIsSymbol = flag11 = false;
        continue;
      }
      if(currIsWhitespace && prevIsSymbol && stringbuffer.length() == 1) {
        j += 2;
        prevIsLetter = prevIsDigit = prevIsSymbol = flag11 = false;
        stringbuffer.delete(0, stringbuffer.length());
        continue;
      }
      boolean currIsLetter = Character.isLetter(c);
      boolean currIsDigit = Character.isDigit(c);
      boolean currIsSymbol = !currIsWhitespace && !currIsLetter && !currIsDigit;
      boolean currIsLowerCase = Character.isLowerCase(c);
      if(k <= j
              && (currIsWhitespace || currIsSymbol || flag11
                      && !currIsLowerCase || !prevIsLetter && currIsLetter))
        k = l1;
      boolean flag13 = prevIsLetter
              && (currIsDigit || currIsSymbol || currIsWhitespace)
              || prevIsLetter && currIsLetter && flag11 && !currIsLowerCase
              || prevIsDigit
              && (currIsLetter || currIsSymbol || currIsWhitespace)
              || prevIsSymbol;
      if(l1 == i - 1) flag13 = true;
      if(flag13) {
        boolean flag16 = !currIsSymbol && !currIsDigit;
        if(l1 == i - 1) flag16 = true;
        String s2 = normalizeWhitespace(stringbuffer.toString());
        int k1 = s2.length();
        flag16 &= k1 - j1 > 1;
        j1 = k1;
        if(i1 != j || !s2.equals(s3)) {
          int l = s2.length();
          if(l > 0) {
            boolean flag14 = annotate(s2, j, l1, l);
            if(flag14) {
              s3 = s2;
              i1 = j;
            }
            if(!flag14 && flag16 || i - 1 == l1) {
              if(k <= j) k = l1;
              j = k;
              l1 = k - 1;
              stringbuffer.delete(0, stringbuffer.length());
              continue;
            }
          }
        }
      }
      stringbuffer.append(c);
      prevIsDigit = currIsDigit;
      prevIsLetter = currIsLetter;
      prevIsSymbol = currIsSymbol;
    }

    fireProcessFinished();
    fireStatusChanged("Hash Gazetteer processing finished!");
  }

  public boolean add(String s, Lookup lookup1) {
    if(!super.caseSensitive.booleanValue()) {
      String s1 = s.toUpperCase();
      if(!s1.equals(s)) add(s1, lookup1);
    }
    String s2 = removeTrailingSymbols(s);
    if(!s2.equals(s)) add(s2, lookup1);
    String s3 = s + " ";

    List<Lookup> arraylist = null;
    int j = 0;
    s3.trim();
    j = s3.length();

    boolean prevIsLetter = false;
    boolean prevIsDigit = false;
    boolean prevIsLowercase = false;

    String s4 = "";
    Map<String, List<Lookup>> hashmap = null;
    for(int k = 0; k < j; k++) {
      char c = s3.charAt(k);
      boolean currIsWhitespace = Character.isWhitespace(c);
      boolean currIsDigit = Character.isDigit(c);
      boolean currIsLetter = Character.isLetter(c);
      boolean currIsSymbol = !currIsWhitespace && !currIsDigit && !currIsLetter;
      boolean currIsLowercase = Character.isLowerCase(c);
      boolean flag18 = prevIsLetter
              && (currIsDigit || currIsSymbol || currIsWhitespace)
              || prevIsLetter && currIsLetter && prevIsLowercase
              && !currIsLowercase || prevIsDigit
              && (currIsLetter || currIsSymbol || currIsWhitespace);
      if(k + 1 == j) flag18 = true;
      if(flag18) {
        s4 = normalizeWhitespace(s3.substring(0, k));
        int i = s4.length();
        if(mapsList[i] == null) {
          hashmap = new HashMap<String, List<Lookup>>();
          mapsList[i] = hashmap;
        }
        else {
          hashmap = (Map<String, List<Lookup>>)mapsList[i];
        }
        if(!hashmap.containsKey(s4)) hashmap.put(s4, null);
      }
      prevIsDigit = currIsDigit;
      prevIsLetter = currIsLetter;

      prevIsLowercase = currIsLowercase;

    }

    arraylist = hashmap.get(s4);
    if(null == arraylist) {
      arraylist = new ArrayList<Lookup>(1);
      arraylist.add(lookup1);
    }
    else if(!arraylist.contains(lookup1)) arraylist.add(lookup1);
    hashmap.put(s4, arraylist);
    return true;
  }

  public Set<Lookup> lookup(String s) {
    Set<Lookup> set = null;
    String s1 = normalizeWhitespace(s);
    int i = s1.length();
    if(mapsListSize < i) return set;
    Map<String, List<Lookup>> hashmap = (HashMap<String, List<Lookup>>)mapsList[i];
    if(hashmap == null) {
      return set;
    }
    else {
      Set<Lookup> hashset = new HashSet<Lookup>(hashmap.get(s1));
      return hashset;
    }
  }

  private boolean annotate(String s, int i, int j, int k) {
    // boolean flag1 = false;
    if(k >= mapsListSize) return false;
    Map<String, List<Lookup>> hashmap = mapsList[k];
    if(hashmap == null) return false;
    if(!hashmap.containsKey(s)) return false;
    List<Lookup> arraylist = hashmap.get(s);

    // TODO shouldn't this return false if arraylist is null?

    if(null != arraylist) {
      for(Iterator<Lookup> iterator = arraylist.iterator(); iterator.hasNext();) {
        Lookup lookup1 = (Lookup)iterator.next();
        FeatureMap featuremap = Factory.newFeatureMap();
        featuremap.put("majorType", lookup1.majorType);
        if(null != lookup1.oClass && null != lookup1.ontology) {
          featuremap.put("class", lookup1.oClass);
          featuremap.put("ontology", lookup1.ontology);
        }
        if(null != lookup1.minorType) {
          featuremap.put("minorType", lookup1.minorType);
          if(null != lookup1.languages)
            featuremap.put("language", lookup1.languages);
        }
        try {
          annotationSet.add(new Long(i), new Long(j), "Lookup", featuremap);
        }
        catch(InvalidOffsetException invalidoffsetexception) {
          throw new LuckyException(invalidoffsetexception.toString());
        }
      }

    }

    return true;
  }

  /**
   * Removes a string from the gazetteer
   * 
   * @param s the item to remove
   * @return true if the operation was successful
   */
  public boolean remove(String s) {

    String s1 = a(s, true);
    int i = s1.length();
    if(i > mapsListSize) return false;
    Map<String, List<Lookup>> hashmap = mapsList[i];
    if(hashmap == null) return false;
    if(hashmap.containsKey(s1)) {
      hashmap.remove(s1);
      return true;
    }
    return false;
  }

  /**
   * Works backwards through the String parameter removing each
   * character until it encounters a letter, digit, or whitespace at
   * which point it returns the truncated string.
   * 
   * @param s the String you wish to remove trailing symbols from
   * @return the truncated String that now ends in a letter, digit, or
   *         whitespace character
   */
  private String removeTrailingSymbols(String s) {
    for(int i = s.length() - 1; i >= 0; i--) {
      char c = s.charAt(i);
      if(!Character.isLetter(c) && !Character.isDigit(c)
              && !Character.isWhitespace(c))
        s = s.substring(0, i);
      else return s;
    }

    return s;
  }

  /**
   * Normalizes the whitespace within the String instance by replacing
   * any sequence of one or more whitespace characters with a single
   * space. Not that any leading/trailing whitespace is also removed.
   * 
   * @param s the String to normalize
   * @return the normalized String
   */
  private String normalizeWhitespace(String s) {

    // this seems to be the same as String.replaceAll("\\s+", " ")

    StringBuffer stringbuffer = new StringBuffer();
    s = s.trim();
    char ac[] = s.toCharArray();
    int i = s.length();
    boolean prevWasWhitespace = false;
    for(int j = 0; j < i; j++) {
      char c = ac[j];

      boolean currIsWhitespace = Character.isWhitespace(c);

      if(currIsWhitespace && !prevWasWhitespace)
        stringbuffer.append(' ');
      else if(!currIsWhitespace) stringbuffer.append(c);

      prevWasWhitespace = currIsWhitespace;
    }

    return stringbuffer.toString();
  }

  private String a(String s, boolean flag) {
    StringBuffer stringbuffer = new StringBuffer();
    boolean flag1 = true;
    s = s.trim();
    char ac[] = s.toCharArray();
    int i = s.length();
    if(i <= 1) return s;
    char c = ac[0];
    stringbuffer.append(c);
    boolean flag2 = true;
    boolean prevIsLetter = Character.isLetter(c);
    boolean prevNotLetterOrDigit = !Character.isLetterOrDigit(c);

    boolean flag10 = true;
    char c2 = 'p';

    for(int j = 1; j < i; j++) {
      char c1 = ac[j];
      boolean currNotLetterOrDigit = !Character.isLetterOrDigit(c1);
      boolean currIsWhitespace = Character.isWhitespace(c1);
      boolean currIsLetter = Character.isLetter(c1);
      boolean currIsDigit = Character.isDigit(c1);
      if(j > 0 && flag2) {
        if(prevNotLetterOrDigit && currIsWhitespace) continue;
        flag2 = prevIsLetter && currNotLetterOrDigit || prevNotLetterOrDigit
                && currIsLetter;
        if(currNotLetterOrDigit) {
          if(c2 == 'p') c2 = c1;
          flag2 = flag10 = c2 == c1;
        }
        if(j > 2 && !flag2 && stringbuffer.length() > 0) {
          char c3 = stringbuffer.charAt(stringbuffer.length() - 1);
          stringbuffer.deleteCharAt(stringbuffer.length() - 1);
          stringbuffer.append(Character.toLowerCase(c3));
        }
      }
      if(currIsLetter || currIsDigit) {
        if(flag && currIsLetter) flag1 &= Character.isUpperCase(c1);
        if(!flag10) c1 = Character.toLowerCase(c1);
        stringbuffer.append(c1);
      }
      else if(!flag2) flag10 = false;
      prevIsLetter = currIsLetter;
      prevNotLetterOrDigit = currNotLetterOrDigit;
    }

    String s1 = stringbuffer.toString();
    if(flag && flag1) s1 = s1.toUpperCase();
    return s1;
  }

  private void readList(LinearNode linearnode) throws GazetteerException {

    if(linearnode == null)
      throw new GazetteerException("LinearNode node is null");

    GazetteerList gazetteerlist = (GazetteerList)listsByNode.get(linearnode);
    if(gazetteerlist == null)
      throw new GazetteerException("gazetteer list not found by node");

    String s = linearnode.getList();
    String majorType = linearnode.getMajorType();
    String minorType = linearnode.getMinorType();
    String language = linearnode.getLanguage();

    Lookup lookup1 = new Lookup(s, majorType, minorType, language);

    if(mappingDefinition != null) {
      MappingNode mappingnode = mappingDefinition.getNodeByList(s);
      if(null != mappingnode) {
        lookup1.oClass = mappingnode.getClassID();
        lookup1.ontology = mappingnode.getOntologyID();
      }
    }

    lookup1.list = s;
    categoryList.add(lookup1);

    @SuppressWarnings("unchecked")
    Iterator<GazetteerNode> iterator = gazetteerlist.iterator();
    String s6 = null;

    for(; iterator.hasNext(); add(s6, lookup1)) {
      String s4 = iterator.next().toString();
      s4.trim();
      int i = s4.length();
      for(int j = 0; j < i; j++) {
        if(j + 1 != i && !Character.isWhitespace(s4.charAt(j))) continue;
        if(j + 1 == i) j = i;
        s6 = s4.substring(0, j).trim();
      }
    }
  }
}
