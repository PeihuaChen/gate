/*
	Scratch.java

	Hamish Cunningham, 22/03/00

	$Id$
*/


package gate.util;

import java.util.*;
import java.awt.datatransfer.*;

import gate.*;
import gate.jape.*;


/**
  * A scratch pad for experimenting.
  */
public class Scratch
{

  public static void main(String args[]) {
    FlavorMap sysFlavors = SystemFlavorMap.getDefaultFlavorMap();
    System.out.println(sysFlavors);

    Map sysFlavorsMap = sysFlavors.getNativesForFlavors(null);
    Iterator iter = sysFlavorsMap.entrySet().iterator();
    while(iter.hasNext()) {
      Object flavor = iter.next();
      System.out.println(flavor);
    }

    sysFlavorsMap = sysFlavors.getFlavorsForNatives(null);
    iter = sysFlavorsMap.entrySet().iterator();
    while(iter.hasNext()) {
      Object flavor = iter.next();
      System.out.println(flavor);
    }
    System.exit(0);
  } // main

  public int i;






} // class Scratch
