package gate.creole.tokeniser.chinesetokeniser;

import java.io.*;
import java.util.*;

/**
 * <p>Title: Segmenter.java</p>
 * <p>Description: This class segments the Chinese Text by adding extra spaces
 * </p>
 * <p>Company: University Of Sheffield</p>
 * @author Erik E. Peterson - modified by Niraj Aswani
 * @see <a href="http://www.mandarintools.com/segmenter.html">source</a>
 */
public class Segmenter {
  //private Hashtable zhwords;
  private TreeMap zhwords;
  private TreeSet csurname, cforeign, cnumbers, cnotname;

  private boolean debug;

  // Char form
  public final static int TRAD = 0;
  public final static int SIMP = 1;
  public final static int BOTH = 2;

  // by niraj
  private ArrayList marks;

  // Charform is TRAD, SIMP or BOTH
  public Segmenter(int charform, boolean loadwordfile) {
    debug = false;

    int count = 0;

    int treelevel;

    csurname = new TreeSet();
    cforeign = new TreeSet();
    cnumbers = new TreeSet();
    cnotname = new TreeSet();

    if (charform == SIMP) {
      loadset(cnumbers,
              "gate:/creole/tokeniser/chinesetokeniser/snumbers_u8.txt");
      loadset(cforeign,
              "gate:/creole/tokeniser/chinesetokeniser/sforeign_u8.txt");
      loadset(csurname,
              "gate:/creole/tokeniser/chinesetokeniser/ssurname_u8.txt");
      loadset(cnotname,
              "gate:/creole/tokeniser/chinesetokeniser/snotname_u8.txt");
    }
    else if (charform == TRAD) {
      loadset(cnumbers,
              "gate:/creole/tokeniser/chinesetokeniser/tnumbers_u8.txt");
      loadset(cforeign,
              "gate:/creole/tokeniser/chinesetokeniser/tforeign_u8.txt");
      loadset(csurname,
              "gate:/creole/tokeniser/chinesetokeniser/tsurname_u8.txt");
      loadset(cnotname,
              "gate:/creole/tokeniser/chinesetokeniser/tnotname_u8.txt");
    }
    else { // BOTH
      loadset(cnumbers,
              "gate:/creole/tokeniser/chinesetokeniser/snumbers_u8.txt");
      loadset(cforeign,
              "gate:/creole/tokeniser/chinesetokeniser/sforeign_u8.txt");
      loadset(csurname,
              "gate:/creole/tokeniser/chinesetokeniser/ssurname_u8.txt");
      loadset(cnotname,
              "gate:/creole/tokeniser/chinesetokeniser/snotname_u8.txt");
      loadset(cnumbers,
              "gate:/creole/tokeniser/chinesetokeniser/tnumbers_u8.txt");
      loadset(cforeign,
              "gate:/creole/tokeniser/chinesetokeniser/tforeign_u8.txt");
      loadset(csurname,
              "gate:/creole/tokeniser/chinesetokeniser/tsurname_u8.txt");
      loadset(cnotname,
              "gate:/creole/tokeniser/chinesetokeniser/tnotname_u8.txt");
    }

    zhwords = new TreeMap();

    if (!loadwordfile) {
      return;
    }

    String newword = null;
    try {
      InputStream worddata = null;
      if (charform == SIMP) {
        worddata = new java.net.URL(
            "gate:/creole/tokeniser/chinesetokeniser/simplexu8.txt").openStream();
      }
      else if (charform == TRAD) {
        worddata = new java.net.URL(
            "gate:/creole/tokeniser/chinesetokeniser/tradlexu8.txt").openStream();
      }
      else if (charform == BOTH) {
        worddata = new java.net.URL(
            "gate:/creole/tokeniser/chinesetokeniser/bothlexu8.txt").openStream();
      }
      BufferedReader in = new BufferedReader(new InputStreamReader(worddata,
          "UTF8"));
      while ( (newword = in.readLine()) != null) {
        if ( (newword.indexOf("#") == -1) && (newword.length() < 5)) {

          zhwords.put(newword.intern(), "1");

          if (newword.length() == 3) {
            if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
              zhwords.put(newword.substring(0, 2).intern(), "2");
            }
          }

          if (newword.length() == 4) {
            if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
              zhwords.put(newword.substring(0, 2).intern(), "2");
            }
            if (zhwords.containsKey(newword.substring(0, 3).intern()) == false) {
              zhwords.put(newword.substring(0, 3).intern(), "2");
            }

          }

          //if (count++ % 20000 == 0) { System.err.println(count); }
        }
      }
      in.close();

    }
    catch (IOException e) {
      //System.err.println("IOException: "+e);
    }

  }

  /** Load a set of character data */
  private void loadset(TreeSet targetset, String sourcefile) {
    String dataline;
    try {
      InputStream setdata = new java.net.URL(sourcefile).openStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(setdata,
          "UTF-8"));
      while ( (dataline = in.readLine()) != null) {
        if ( (dataline.indexOf("#") > -1) || (dataline.length() == 0)) {
          continue;
        }
        targetset.add(dataline.intern());
      }
      in.close();
    }
    catch (Exception e) {
      //System.err.println("Exception loading data file" + sourcefile + " " + e);
    }

  }

  public boolean isNumber(String testword) {
    boolean result = true;
    for (int i = 0; i < testword.length(); i++) {
      if (cnumbers.contains(testword.substring(i, i + 1).intern()) == false) {
        result = false;
        break;
      }
    }

    return result;
  }

  public boolean isAllForeign(String testword) {
    boolean result = true;
    for (int i = 0; i < testword.length(); i++) {
      if (cforeign.contains(testword.substring(i, i + 1).intern()) == false) {
        result = false;
        break;
      }
    }

    return result;
  }

  public boolean isNotCJK(String testword) {
    boolean result = true;
    for (int i = 0; i < testword.length(); i++) {
      if (Character.UnicodeBlock.of(testword.charAt(i)) ==
          Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
        result = false;
        break;
      }
    }

    return result;
  }

  public String stemWord(String word) {
    String[] prefix = new String[] {
        "\u7b2c", "\u526f", "\u4e0d"};
    String[] suffix = new String[] {
        "\u4e86", "\u7684", "\u5730", "\u4e0b", "\u4e0a", "\u4e2d", "\u91cc",
        "\u5230", "\u5185", "\u5916", "\u4eec"};
    String[] infix = new String[] {
        "\u5f97", "\u4e0d"};
    int i;

    StringBuffer unstemmed = new StringBuffer(word);

    for (i = 0; i < prefix.length; i++) {
      if (unstemmed.substring(0, 1).equals(prefix[i]) == true &&
          (zhwords.get(unstemmed.substring(1, unstemmed.length()).intern()) != null ||
           unstemmed.length() == 2)) {
        unstemmed.deleteCharAt(0);
        return unstemmed.toString();
      }
    }

    for (i = 0; i < suffix.length; i++) {
      if (unstemmed.substring(unstemmed.length() - 1, unstemmed.length()).
          equals(suffix[i]) == true &&
          (zhwords.get(unstemmed.substring(0, unstemmed.length() - 1).intern()) != null ||
           unstemmed.length() == 2)) {
        unstemmed.deleteCharAt(unstemmed.length() - 1);
        return unstemmed.toString();
      }
    }

    for (i = 0; i < infix.length; i++) {
      if (unstemmed.length() == 3 && unstemmed.substring(1, 2).equals(infix[i]) == true &&
          zhwords.get(new String(unstemmed.substring(0, 1) +
                                 unstemmed.substring(2, 3)).intern()) != null) {
        unstemmed.deleteCharAt(1);
        return unstemmed.toString();
      }
    }

    return unstemmed.toString();
  }

  // here we will keep track of where we add the spaces in the original
  // document and we will store all these marks in the array called *marks*
  public String segmentLine(String cline, String separator) {
    StringBuffer currentword = new StringBuffer();
    StringBuffer outline = new StringBuffer();
    int i, clength;
    char currentchar;
    separator = " ";

    clength = cline.length();
    int[][] offsets = new int[clength][2];
    marks = new ArrayList(); // addition by Niraj

    for (i = 0; i < clength; i++) {
      currentchar = cline.charAt(i);
      if (Character.UnicodeBlock.of(currentchar) ==
          Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
          isNumber(cline.substring(i, i + 1)) == true) {
        // Character in CJK block
        if (currentword.length() == 0) { // start looking for next word
          //System.err.println("current word length 0");
          if (i > 0 && (Character.isWhitespace(cline.charAt(i - 1)) == false)) {

            marks.add(new Long(i + marks.size())); //addition by Niraj
            outline.append(separator);
          }
          currentword.append(currentchar);

        }
        else {
          if (zhwords.containsKey(new String(currentword.toString() +
                                             currentchar).intern()) == true &&
              ( (String) (zhwords.get(new String(currentword.toString() +
                                                 currentchar).intern()))).
              equals("1") == true) {
            // word is in lexicon
            currentword.append(currentchar);

          }
          else if (isAllForeign(currentword.toString()) &&
                   cforeign.contains(new String(new char[] {currentchar}).
                                     intern()) &&
                   i + 2 < clength &&
                   (zhwords.containsKey(cline.substring(i, i + 2).intern()) == false)) {
            // Possible a transliteration of a foreign name
            currentword.append(currentchar);

          }
          else if (isNumber(currentword.toString()) &&
                   cnumbers.contains(new String(new char[] {currentchar}).
                                     intern())
                   /* && (i + 2 < clength) &&
               (zhwords.containsKey(cline.substring(i, i+2).intern()) == false) */) {
            // Put all consecutive number characters together
            currentword.append(currentchar);

          }
          else if ( (zhwords.containsKey(new String(currentword.toString() +
              currentchar).intern())) &&
                   ( ( (String) (zhwords.get(new String(currentword.toString() +
              currentchar).intern()))).equals("2") == true) &&
                   i + 1 < clength &&
                   (zhwords.containsKey(new String(currentword.toString() +
              currentchar +
              cline.charAt(i + 1)).intern()) == true)) {

            // Starts a word in the lexicon
            currentword.append(currentchar);

          }
          else { // Start anew

            outline.append(currentword.toString());
            if (Character.isWhitespace(currentchar) == false) {
              // addition by Niraj
              marks.add(new Long(i + marks.size()));
              // end of addition
              outline.append(separator);
            }
            currentword.setLength(0);
            currentword.append(currentchar);
          }
        }

      }
      else { // Not chinese character
        //System.err.println("not cjk");
        if (currentword.length() > 0) {
          outline.append(currentword.toString());
          if (Character.isWhitespace(currentchar) == false) {
            // addition by Niraj
            marks.add(new Long(i + marks.size()));
            // end of addition
            outline.append(separator);
          }
          currentword.setLength(0);
        }
        outline.append(currentchar);
      }
    }

    outline.append(currentword.toString());

    return outline.toString();
    //return offsets;
  }

  public void addword(String newword) {
    zhwords.put(newword.intern(), "1");

    if (newword.length() == 3) {
      if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
        zhwords.put(newword.substring(0, 2).intern(), "2");
      }
    }

    if (newword.length() == 4) {
      if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
        zhwords.put(newword.substring(0, 2).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 3).intern()) == false) {
        zhwords.put(newword.substring(0, 3).intern(), "2");
      }

    }

    if (newword.length() == 5) {
      if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
        zhwords.put(newword.substring(0, 2).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 3).intern()) == false) {
        zhwords.put(newword.substring(0, 3).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 4).intern()) == false) {
        zhwords.put(newword.substring(0, 4).intern(), "2");
      }
    }

    if (newword.length() == 6) {
      if (zhwords.containsKey(newword.substring(0, 2).intern()) == false) {
        zhwords.put(newword.substring(0, 2).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 3).intern()) == false) {
        zhwords.put(newword.substring(0, 3).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 4).intern()) == false) {
        zhwords.put(newword.substring(0, 4).intern(), "2");
      }
      if (zhwords.containsKey(newword.substring(0, 5).intern()) == false) {
        zhwords.put(newword.substring(0, 5).intern(), "2");
      }
    }

  }

  /**
   * This method returns the marks where the spaces were added by the segmenter
   */
  // addition by Niraj
  public ArrayList getMarks() {
    return marks;
  }

  public String segmentData(String fileContents, String encoding) {
    byte[] gbbytes;
    String segstring = "";
    boolean debug = false;

    try {
      segstring = segmentLine(fileContents, " ");
      if (debug) {
        gbbytes = segstring.getBytes(encoding);
      }
    }
    catch (Exception e) {
      //System.err.println("Exception " + e.toString());
    }

    return segstring;
  }
}