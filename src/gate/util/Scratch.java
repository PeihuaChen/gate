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
/*
import java.io.IOException;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.w3c.tidy.Tidy;
*/

/**
  * A scratch pad for experimenting.
  */
public class Scratch
{

  //*
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
   //*/

   // Cristian scratch



  /*

        Test16 t1 = new Test16("url", "outXMlFile", "errorFile", true);
        Test16 t2 = new Test16(args[3], args[4], args[5], false);
        Thread th1 = new Thread(t1);
        Thread th2 = new Thread(t2);

        th1.start();
        th2.start();

  */
  /*
   Map map = new HashMap();

   ExtendedMimeType mime = new ExtendedMimeType("text","xml");
   map.put(mime,"XML handler");
   map.put(new ExtendedMimeType("text","html"),"HTML handler");

   System.out.println(map.get(new ExtendedMimeType("text","xml")));
  */
  } // main

  //public int i;

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

/*
public class Test16 implements Runnable {

    private String url;
    private String outFileName;
    private String errOutFileName;
    private boolean xmlOut;

    public Test16(String url, String outFileName,
                  String errOutFileName, boolean xmlOut)
    {
        this.url = url;
        this.outFileName = outFileName;
        this.errOutFileName = errOutFileName;
        this.xmlOut = xmlOut;
    }

    public void run()
    {
        URL u;
        BufferedInputStream in;
        FileOutputStream out;
        Tidy tidy = new Tidy();

        tidy.setXmlOut(xmlOut);
        try {
            tidy.setErrout(new PrintWriter(new FileWriter(errOutFileName), true));
            u = new URL(url);
            in = new BufferedInputStream(u.openStream());
            out = new FileOutputStream(outFileName);
            tidy.parse(in, out);
        }
        catch ( IOException e ) {
            System.out.println( this.toString() + e.toString() );
        }
    }
}
*/

