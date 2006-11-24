package gate.creole.gazetteer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * A node in a gazetteer list allowing an arbitary amount of features
 * to be added as metadata to an entry, e.g.:
 * </p>
 * <p>
 * With the separator set to '\t', if a gazetteer entry looked like this:
 * </p>
 * <pre>Vodaphone&#09;type=mobile phone company</pre> 
 * <p>
 * Then the GazetteerNode would consist of an entry "Vodaphone", with a featureMap
 * containing the key "type", mapped to "mobile phone company".
 *  </p>
 * @author JLy
 *
 */
public class GazetteerNode {
  /** The gazetteer entry */
  private String entry;
  
  /** The features associated to the entry. If there are no features for this entry, it is null */
  private Map featureMap = null;
  
  /** The separator used in a GazetteerNode string */
  private String separator;
    
  /**
   * Constructor. Uses the default separator.
   * 
   * @param entry the gazetteer entry
   * @param featureMap a map of name-value pairs
   */
  public GazetteerNode(String entry, Map featureMap) {
    this.entry = entry;
    this.featureMap = featureMap;
  }

  
  /**
   * Parses and create a gazetteer node from a string using no separator, i.e.
   * the whole node is considered as the string to match, and there are no
   * additional features.
   * 
   * @param node the gazetteer node to be parsed
   * @throws InvalidFormatException
   */
  public GazetteerNode(String node) {
    this(node,(String)null);
  }
  
  /**
   * Parses and create a gazetteer node from a string
   * 
   * @param node the gazetteer node to be parsed
   * @param separator the separator used in the gazetteer node string to delimit 
   * each name-value pair of features. If the separator is null, then the whole 
   * node will be used as the gazetteer entry
   * @throws InvalidFormatException
   */
  public GazetteerNode(String node, String separator) {
    this.separator = (separator != null && separator.length() == 0)? null : separator;
    int index_sep;
    if(this.separator == null || (index_sep = node.indexOf(this.separator)) == -1 ) {
      entry = node;
      // leave featureMap null
    } else {
      entry = node.substring(0, index_sep);
      String features = node.substring(index_sep + 1);
      featureMap = getFeatures(features);
    }
  }
  
  /**
   * Given a string of name-value pairs in the format "name=value", separated
   * by whatever this GazetteerNode's separator has been set to, convert it
   * to the equilivant map.
   * 
   * @param features a string in the format "name=value" separated by whatever
   * the separator has been set to.
   * @return a Map of the features
   * @throws InvalidFormatException
   */
  private Map getFeatures(String features) {
    
    if (separator == null)
      return null;
    
    // split the string into name-value pair strings
    ArrayList<String> tempPairs = new ArrayList<String>();

    int substr_begin = 0;
    int substr_end = features.indexOf(separator,substr_begin);
    while (substr_end != -1) {   
      tempPairs.add(features.substring(substr_begin,substr_end));
      substr_begin = substr_end + 1;
      substr_end = features.indexOf(separator,substr_begin); 
    }
    
    String lastPair = features.substring(substr_begin);

    if (lastPair.length() != 0) {
      tempPairs.add(lastPair);
    }
    
    String[] pairs = tempPairs.toArray(new String[tempPairs.size()]); 
        
    if (pairs.length == 0) {
      return null;
    }
    
    // extract the name and value from the pair strings and put in feature map
    Map<String,String> featureMap = new HashMap<String,String>(pairs.length);
    for(int i = 0; i < pairs.length; i++) {
      String pair = pairs[i];
      int sep = pair.indexOf('=');
      if(sep == -1) {
        throw new gate.util.GateRuntimeException("Correct format for gazetteer entry features is: [entry]([separator][featureName]=[featureValue])*");
      } else {
        String name = pair.substring(0, sep).trim();
        String value = pair.substring(sep + 1).trim();
        if(name.length() > 0 && value.length() > 0) {
          featureMap.put(name, value);
        }
      }
    }
    
    if (featureMap.size() == 0) {
      return null;
    }
    return featureMap;
  }

  /**
   * Converts a featureMap to separated name value pairs. Note: the string will begin
   * with the separator character.
   * 
   * @param featureMap
   * @return
   */
  public String featureMapToString(Map featureMap) {
    String str = "";
    for(Iterator it = featureMap.keySet().iterator(); it.hasNext();) {
      String key = (String)it.next();
      str += separator + key + "=" + featureMap.get(key);
    }
    return str;
  }

  

  /**
   * Gets the string representation of this node
   * 
   * @return the string representation of this node
   */
  public String toString() {
    if(featureMap == null || separator == null)
      return entry;
    else return entry + featureMapToString(featureMap);
  }

  /**
   * Checks this node vs another one for equality.
   * 
   * @param o another node
   * @return true if entry and weighting match.
   */
  public boolean equals(Object o) {
    boolean result = false;
    if(o instanceof GazetteerNode) {
      GazetteerNode node = (GazetteerNode)o;
      result = true;
      if(null != this.getEntry())
        result &= this.getEntry().equals(node.getEntry());
      if(null != this.getFeatureMap())
        result &= this.getFeatureMap().equals(node.getFeatureMap());
    }
    return result;
  }

  /**
   * @return the entry
   */
  public String getEntry() {
    return entry;
  }

  /**
   * @param entry
   *          the entry to set
   */
  public void setEntry(String entry) {
    this.entry = entry;
  }

  /**
   * @return the featureMap
   */
  public Map getFeatureMap() {
    return featureMap;
  }
  
  /**
   * @param featureMap the featureMap to set
   */
  public void setFeatureMap(Map featureMap) {
    this.featureMap = featureMap;
  }

  /**
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * @param separator the separator to set
   */
  public void setSeparator(String separator) {
    this.separator = separator;
  }

  
}
