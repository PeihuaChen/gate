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
    if(other instanceof Scratch) return i == ((Scratch)other).i;
    else return false;
  } // equals

  public Scratch(int i){
    this.i = i;
  }

  public static void main(String args[]) {
    Map map1 = new HashMap();
    Map map2 = new HashMap();

    Collection col1 = new HashSet();
    Collection col2 = new HashSet();

    Scratch o1 = new Scratch(1);
    Scratch o2 = new Scratch(2);
    Scratch o3 = new Scratch(1);

    col1.add(o1);
    col1.add(o2);

    col2.add(o2);
    col2.add(o3);

    System.out.println(col1.containsAll(col2));
  } // main

  public int i;
} // class Scratch
