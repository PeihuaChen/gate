/*
 * ColorGenerator.java
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
 * Valentin Tablan, 11/07/2000
 *
 * $Id$
 */
package gate.gui;

import java.awt.*;
import java.util.*;

public class ColorGenerator {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  public ColorGenerator() {
    for(int i = 0; i < 8; i++)availableSpacesList[i] = new LinkedList();
    ColorSpace usedCS = new ColorSpace(0,0,0,255);
    availableSpacesList[0].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                               usedCS.baseG,
                                               usedCS.baseB,
                                               usedCS.radius/2));
    availableSpacesList[1].addLast(new ColorSpace(usedCS.baseR,
                                               usedCS.baseG + usedCS.radius/2,
                                               usedCS.baseB,
                                               usedCS.radius/2));
    availableSpacesList[2].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                               usedCS.baseG + usedCS.radius/2,
                                               usedCS.baseB,
                                               usedCS.radius/2));

    availableSpacesList[3].addLast(new ColorSpace(usedCS.baseR,
                                               usedCS.baseG,
                                               usedCS.baseB + usedCS.radius/2,
                                               usedCS.radius/2));
    availableSpacesList[4].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                               usedCS.baseG,
                                               usedCS.baseB + usedCS.radius/2,
                                               usedCS.radius/2));
    availableSpacesList[5].addLast(new ColorSpace(usedCS.baseR,
                                               usedCS.baseG + usedCS.radius/2,
                                               usedCS.baseB + usedCS.radius/2,
                                               usedCS.radius/2));
/*
    availableSpacesList[6].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                               usedCS.baseG + usedCS.radius/2,
                                               usedCS.baseB + usedCS.radius/2,
                                               usedCS.radius/2));

  */
//    Color foo = getNextColor();
  }

  public Color getNextColor(){
    ColorSpace usedCS;
    listToRead = listToRead % 8;

    if(availableSpacesList[listToRead].isEmpty()){
      usedCS = (ColorSpace)usedSpacesList.removeFirst();
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR,
                                                 usedCS.baseG,
                                                 usedCS.baseB,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                                 usedCS.baseG,
                                                 usedCS.baseB,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR,
                                                 usedCS.baseG + usedCS.radius/2,
                                                 usedCS.baseB,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                                 usedCS.baseG + usedCS.radius/2,
                                                 usedCS.baseB,
                                                 usedCS.radius/2));

      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR,
                                                 usedCS.baseG,
                                                 usedCS.baseB + usedCS.radius/2,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                                 usedCS.baseG,
                                                 usedCS.baseB + usedCS.radius/2,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR,
                                                 usedCS.baseG + usedCS.radius/2,
                                                 usedCS.baseB + usedCS.radius/2,
                                                 usedCS.radius/2));
      availableSpacesList[listToRead].addLast(new ColorSpace(usedCS.baseR + usedCS.radius/2,
                                                 usedCS.baseG + usedCS.radius/2,
                                                 usedCS.baseB + usedCS.radius/2,
                                                 usedCS.radius/2));

    }
    usedCS = (ColorSpace)availableSpacesList[listToRead].removeFirst();
    Color res = new Color(usedCS.baseR + usedCS.radius/2,
                          usedCS.baseG + usedCS.radius/2,
                          usedCS.baseB + usedCS.radius/2);
    usedSpacesList.addLast(usedCS);
    listToRead++;
    return res;
  }

  class ColorSpace{
    ColorSpace(int r, int g, int b, int radius){
      baseR = r;
      baseG = g;
      baseB = b;
      this.radius = radius;
    }

    int baseR, baseG, baseB;
    int radius;
  }

  LinkedList[] availableSpacesList = new LinkedList[8];
  LinkedList usedSpacesList = new LinkedList();
  int listToRead = 0;
}