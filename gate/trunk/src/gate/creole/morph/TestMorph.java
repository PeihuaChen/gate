package gate.creole.morph;

import junit.framework.*;
import javax.swing.JOptionPane;

/**
 * <p>Title: Gate2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Sheffield</p>
 * @author not attributable
 * @version 1.0
 */

public class TestMorph extends TestCase {

  private String verbFile;
  private String defaultRuleFile;
  private Interpret interpret;
  private int counter = 0;
  private int outOf = 0;

  public TestMorph(String dummy) {
    super(dummy);
  }

  protected void setUp() throws Exception {

    verbFile = "gate:/creole/morph/verb.dat";
    defaultRuleFile = "gate:/creole/morph/default.rul";

    interpret = new Interpret();

    // lets compile all the rules
    interpret.init(defaultRuleFile);
  }

  public static Test suite() {
    return new TestSuite(TestMorph.class);
  }

  public void testVerbs() throws Exception {

    ReadFile readVerbs = new ReadFile(verbFile);
    outOf = 0;
    counter = 0;

    if (readVerbs.read()) {

      // verb entries are in this format
      // going ==> go

      while (readVerbs.hasNext()) {

        String verb = readVerbs.getNext();
        // separate
        String [] words = verb.split(" ==> ");

        // the word to be tested
        words[0] = words[0].trim();

        // the base form from the word net
        words[1] = words[1].trim();

        String answer = interpret.runMorpher(words[0]);

        outOf++;
        if(!words[1].equals(answer)) {
          counter++;
        }
      }
      JOptionPane.showMessageDialog(null, counter+" out of "+outOf+" words didn't match");
    }
    else {
      System.out.println("Some Error reading verbFile");
      return;
    }
  }
}