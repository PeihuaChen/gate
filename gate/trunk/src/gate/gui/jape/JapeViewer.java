package gate.gui.jape;

import java.io.*;
import javax.swing.*;
import java.net.*;
import java.awt.*;

import gate.*;
import gate.creole.*;

/**
 * <p>Title: Gate2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Sheffield</p>
 * @author not attributable
 * @version 1.0
 */


public class JapeViewer extends AbstractVisualResource implements ANNIEConstants {

  public JapeViewer() {
  }

  //GUI components
  /** The text display.*/
  protected JTextArea textArea;

  /** Scroller used for the text diaplay*/
  protected JScrollPane textScroll;

  /** The toolbar displayed on the top part of the component */
  protected JToolBar toolbar;

  /**Should this component bahave as an editor as well as an viewer*/
  private boolean editable = false;

  /** A Button for saving the contents in a Jape file */
  private JButton saveButton;

  /** A Button for reverting contents */
  private JButton revertButton;

  /** A field that holds the jape file name */
  private URL japeFileURL;

  /** a field that holds the jape file contents */
  private String japeFileContents;

  /** Transducer */
  private Transducer transducer;

  /** An Init method */
  public Resource init() {
    initGuiComponents();
    japeFileContents = new String();
    return this;
  }

  private void initGuiComponents() {
    System.out.println("executing");
    setLayout(new BorderLayout());
    textArea = new JTextArea();
    textArea.setEditable(editable);
    textScroll = new JScrollPane(textArea,
                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(textScroll, BorderLayout.CENTER);
  }

  public void setTarget(Object target) {
    if(!(target instanceof  Transducer)) {
      throw new IllegalArgumentException(
        "The GATE jape editor can only be used with a GATE jape transducer!\n" +
        target.getClass().toString() + " is not a GATE Jape Transducer!");
    }

    this.transducer = (Transducer) target;
    //Transducer inst = (Transducer)((Gate.getCreoleRegister().getPrInstances("gate.creole.Transducer")).get(0));
    japeFileURL = transducer.getGrammarURL();
    // reading japeFile
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(japeFileURL.
          openStream()));
      String content = br.readLine();
      while(content != null) {
        japeFileContents += content + "\n";
        content = br.readLine();
      }
      textArea.setText(japeFileContents);
      br.close();
    } catch (IOException ioe) {
    }
  }
}