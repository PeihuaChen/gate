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
import org.w3c.www.mime.*;

/**
  * A scratch pad for experimenting.
  */
public class Scratch
{

  public static void main(String args[]) {
  /*
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
   */

   /*
   // Cristian scratch
   Map map = new HashMap();

   ExtendedMimeType mime = new ExtendedMimeType("text","xml");
   map.put(mime,"XML handler");
   map.put(new ExtendedMimeType("text","html"),"HTML handler");

   System.out.println(map.get(new ExtendedMimeType("text","xml")));
   */
  } // main

  public int i;

} // class Scratch

/*
class ExtendedMimeType extends MimeType{
  public ExtendedMimeType(String type, String subtype){super(type,subtype);}

  public boolean equals(ExtendedMimeType obj){
    if (this.toString().equals(obj.toString()))
          return true;
    return false;
  }

  public int hashCode(){
    System.out.println(this.toString () + " HASH code = " + this.toString ().hashCode());
    return this.toString ().hashCode();
  }
}
*/
