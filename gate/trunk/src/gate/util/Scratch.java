/* 
	Scratch.java

	Hamish Cunningham, 22/03/00

	$Id$
*/


package gate.util;

import java.util.*;


/**
  * A scratch pad for experimenting.
  */
public class Scratch
{
  public boolean equals(Object other) {
    System.out.println("Scratch.equals");
    return super.equals(other);
  } // equals

  public static void main(String args[]) {
    Map map1 = new HashMap();
    Map map2 = new HashMap();

    Scratch o1 = new Scratch();
    Scratch o2 = new Scratch();
    Scratch o3 = new Scratch();

    map1.put("o1", o1);
    map1.put("o2", o2);
    map1.put("o3", o3);

    map2.put("o1", o1);
    map2.put("o2", o2);
    map2.put("o3", o3);

    System.out.println(
      "map1.values().containsAll(map2.values()): " +
      map1.values().containsAll(map2.values())
    );

    map1.remove("o1");
    System.out.println(
      "map1.values().containsAll(map2.values()): " +
      map1.values().containsAll(map2.values())
    );

  } // main

} // class Scratch
