/*
 * GateIMDescriptor.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, October 2000
 *
 * $Id$
 */
package guk.im;

import java.util.Locale;
import java.awt.*;
import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.io.*;
import java.util.*;

import guk.*;


/**
 * Provides a way for the Gate input method to be discovered by the system.
 *
 * @see java.awt.im
 * @see java.awt.im.spi
 */
public class GateIMDescriptor implements InputMethodDescriptor {

  /**
   * Default constructor.
   */
  public GateIMDescriptor() {
    try{
      InputStream is = GateIM.class.getResourceAsStream(GateIM.getIMBase() + "im.list");
	    if (is==null) throw new IllegalArgumentException(
              "Failed to retrieve resource 'im.list'. Please reset classpath.");
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line = br.readLine();
      StringTokenizer st;
      String filename, language, country, variant;
      supportedLocales = new HashMap();
      while(line != null){
        //skip comments and empty lines
        if(line.startsWith("#") || line.startsWith("//") ||
           line.length() == 0 ){
          line = br.readLine();
          continue;
        }
        language = country = variant = null;
        st = new StringTokenizer(line, "\t", false);
        if(st.hasMoreTokens()){
          //get the file
          filename = st.nextToken();
          if(st.hasMoreTokens()){
            //get the language
            language = st.nextToken();
            if(st.hasMoreElements()){
              //get the country
              country = st.nextToken();
              if(country.equals("--")) country = "";
              if(st.hasMoreElements()){
                //get the variant
                variant = st.nextToken();
                supportedLocales.put(new Locale(language,country,variant),
                                     filename);
              }else{
                //no variant
                supportedLocales.put(new Locale(language,country), filename);
              }
            }else{
              //no country
              throw new IllegalArgumentException(
                "Invalid input methods definition file!\n");
            }
          }else{
            //no language
            throw new IllegalArgumentException(
                "Invalid input methods definition file!\n");
          }
        }
        line = br.readLine();
      }
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

  /**
   * Gets an Array with the locales supported by the Gate input method.
   *
   * @exception AWTException
   */
  public Locale[] getAvailableLocales() throws AWTException {
    java.util.List locales = new ArrayList(supportedLocales.keySet());
    Collections.sort(locales, new Comparator(){
      /**
       * Comparison method used for sorting the available locales.
       *
       * @param a
       * @param b
       */
      public int compare(Object a, Object b){
        if(a instanceof Locale && b instanceof Locale){
          Locale l1 = (Locale) a;
          Locale l2 = (Locale) b;
          return l1.getDisplayLanguage().compareTo(l2.getDisplayLanguage());
        }else throw new ClassCastException();
      }
    });
    return (Locale[])locales.toArray(new Locale[0]);
  }

  /**
   * Is the available locales list dynamic. Always returns <tt>false</tt>;
   *
   */
  public boolean hasDynamicLocaleList() {
    return false;
  }

  /**
   * Returns the display name for the input method for a given locale.
   *
   * @param inputLocale the locale for which the display name is sought
   * @param displayLanguage the current locale to be used for displaying the
   *     name
   */
  public String getInputMethodDisplayName(Locale inputLocale,
                                          Locale displayLanguage) {
    if(inputLocale == null) return "Gate Unicode Input Methods";
    return inputLocale.getDisplayName(inputLocale);
  }

  /**
   * Provides an icon for the gate input method.
   *
   * @param inputLocale
   */
  public Image getInputMethodIcon(Locale inputLocale) {
    //not yet!
    return null;
  }

  /**
   * Creates a new {@link GateIM} object and returns a handle.
   *
   * @exception Exception
   */
  public InputMethod createInputMethod() throws Exception {
    return new GateIM(supportedLocales);
  }

/*  static public void main(String[] args){
    try{
      GateIMDescriptor gd = new GateIMDescriptor();
      InputMethod im = gd.createInputMethod();
//      im.setLocale(new Locale("ar","","Windows"));
      //try all locales
      Locale[] locales = gd.getAvailableLocales();
      for(int i=0; i < locales.length; i++) im.setLocale(locales[i]);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
*/
  /**
   * The available locales. Maps from locale to filename.
   *
   */
  Map supportedLocales;
}
