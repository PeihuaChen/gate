/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 09/11/2001
 *
 *  $Id$
 */
package gate.util;

import java.util.HashMap;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.*;
/**
 * A map that stores values as strings and provides support for converting some
 * frequently used types to and from string
 */
public class OptionsMap extends HashMap {

  /**
   * Converts the value to string using its toString() method and then stores it
   */
  public Object put(Object key, Object value){
    if(value instanceof Font){
      Font font = (Font)value;
      String family = font.getFamily();
      int size = font.getSize();
      boolean italic = font.isItalic();
      boolean bold = font.isBold();
      value = family + "#" + size + "#" + italic + "#" + bold;
    }

    Object res = super.put(key, value.toString());
    return res;
  }

  /**
   * If the object stored under key is an Integer then returns its value
   * otherwise returns null;
   */
  public Integer getInt(Object key){
    String stringValue = getString(key);
    Integer value = null;
    try{
      value = Integer.decode(stringValue);
    }catch(Exception e){};
    return value;
  }

  /**
   * If the object stored under key is a Boolean then returns its value
   * otherwise returns null;
   */
  public Boolean getBoolean(Object key){
    String stringValue = getString(key);
    Boolean value = null;
    try{
      value = Boolean.valueOf(stringValue);
    }catch(Exception e){};
    return value;
  }

  /**
   * If the object stored under key is a String then returns its value
   * otherwise returns null;
   */
  public String getString(Object key){
    String stringValue = null;
    try{
      stringValue = (String)get(key);
    }catch(Exception e){};
    return stringValue;
  }

  /**
   * If the object stored under key is a String then returns its value
   * otherwise returns null;
   */
  public Font getFont(Object key){
    String stringValue = null;
    try{
      stringValue = (String)get(key);
    }catch(Exception e){};
    if(stringValue == null) return null;
    StringTokenizer strTok = new StringTokenizer(stringValue, "#", false);
    String family = strTok.nextToken();
    int size = Integer.parseInt(strTok.nextToken());
    boolean italic = Boolean.valueOf(strTok.nextToken()).booleanValue();
    boolean bold = Boolean.valueOf(strTok.nextToken()).booleanValue();

    Map fontAttrs = new HashMap();
    fontAttrs.put(TextAttribute.FAMILY, family);
    fontAttrs.put(TextAttribute.SIZE, new Float(size));
    if(bold) fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    else fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
    if(italic) fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    else fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);

    return new Font(fontAttrs);
  }



}