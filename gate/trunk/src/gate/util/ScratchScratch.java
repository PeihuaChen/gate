package gate.util;

import java.util.*;

public class ScratchScratch {

  public ScratchScratch() {
  }

  public void doIt(){
  /*
    System.out.println("New line " + Character.isSpaceChar('\n'));
    System.out.println("New line " + Character.isWhitespace('\n'));
    for(char c = '\u0020'; c <= '\u00ff'; c++)
      System.out.println(c + " " + Character.getType(c));
      */

    String unu1 = "unu";
    String unu2 = "unu";
    Map map = new HashMap();
    map.put(unu1, new Byte((byte)1));
    map.put("doi", new Byte((byte)2));

System.out.println("Unu: " + map.get(unu2));
System.out.println("Doi: " + map.get("doi"));
  }
  public static void main(String[] args) {
    ScratchScratch scratchScratch = new ScratchScratch();
    scratchScratch.doIt();

  }
}
