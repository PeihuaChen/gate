package gate.util;

public class Tools {

  public Tools() {
  }
  static long sym=0;

  static public String gensym(String base){
    return base+sym++;
  }
} 