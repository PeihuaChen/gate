package gate.util;

public class Tools {

  public Tools() {
  }
  static long sym=0;

  /*Returns a Long wich is unique during the current run.
  * Maybe we should use serializaton in order to save the state on System.exit...
  */
  static public synchronized Long gensym(){
    return new Long(sym++);
  }
}