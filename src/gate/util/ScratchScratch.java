package gate.util;

import java.util.*;
import java.io.*;

import gate.creole.tokeniser.*;

public class ScratchScratch {

  public ScratchScratch() {
  }

  public void doIt()throws Exception{
/*    for(int i = 0; i<= 255; i++){
      System.out.println("" + i + "\t'" + (char)i + "'\t" +
                         DefaultTokeniser.typesMnemonics[Character.getType((char)i)+128]);
    }
*/

  File tempFF = new File("d:/tmp/aaa/bbb/ccc/ddd/eee/fff/ggg/hhh/iii/jjj/kkk");
  tempFF.mkdirs();
  }
  public static void main(String[] args) {
    ScratchScratch scratchScratch = new ScratchScratch();
    try{
      scratchScratch.doIt();
    }catch(Exception e){
      e.printStackTrace(System.err);
    }

  }
}
