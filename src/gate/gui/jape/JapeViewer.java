package gate.gui.jape;

import gate.Resource;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractVisualResource;
import gate.creole.Transducer;
import gate.event.ProgressListener;
import gate.jape.parser.ParseCpslConstants;
import gate.jape.parser.ParseCpslTokenManager;
import gate.jape.parser.SimpleCharStream;
import gate.jape.parser.Token;
import gate.util.Files;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * @author niraj
 * @version 1.1
 */

public class JapeViewer extends AbstractVisualResource implements
                                                      ANNIEConstants,
                                                      ProgressListener {

  public JapeViewer() {
  }

  // GUI components
  /** The text display. */
  protected JTextPane textArea;

  /** Scroller used for the text diaplay */
  protected JScrollPane textScroll;

  /** The toolbar displayed on the top part of the component */
  protected JToolBar toolbar;

  /** Should this component behave as an editor as well as an viewer */
  private boolean editable = false;

  /** A field that holds the jape file name */
  private URL japeFileURL;

  /** a field that holds the jape file contents */
  private String japeFileContents;
  
  private boolean updating = false;

  /** Transducer */
  private Transducer transducer;
  
  private JComboBox cboPhases;

  // should probably be static

  Map<Integer, Style> colorMap = new HashMap<Integer, Style>();

  /** An Init method */
  public Resource init() {
    initGuiComponents();
    japeFileContents = new String();
    return this;
  }

  private void initGuiComponents() {
    setLayout(new BorderLayout());
    textArea = new JTextPane();
    textArea.setEditable(editable);
    textScroll =
            new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(textScroll, BorderLayout.CENTER);
    
    toolbar = new JToolBar();
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    
    JButton btnReload = new JButton("Reset");
    btnReload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        japeFileURL = transducer.getGrammarURL();
        readJAPEFileContents();
      }
    });
    toolbar.add(btnReload);
    
    cboPhases = new JComboBox();
    cboPhases.setPrototypeDisplayValue("The name of a phase");
    cboPhases.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ie) {
        if (updating) return;
        if (ie.getStateChange() != ItemEvent.SELECTED) return;
        
        try {
          japeFileURL = new URL(transducer.getGrammarURL(),ie.getItem()+".jape");
          System.out.println(japeFileURL);
          readJAPEFileContents();
        }
        catch (MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    });
    toolbar.addSeparator();
    toolbar.add(new JLabel("Phase:"));
    toolbar.add(cboPhases);

    add(toolbar, BorderLayout.NORTH);
    
    // if we want to set the jape to be monospaced then do this...
    /*
     * MutableAttributeSet attrs = textArea.getInputAttributes();
     * StyleConstants.setFontFamily(attrs, "monospaced"); StyledDocument doc =
     * textArea.getStyledDocument(); doc.setCharacterAttributes(0,
     * doc.getLength() + 1, attrs, false);
     */

    Style style = textArea.addStyle("Red", null);
    StyleConstants.setForeground(style, Color.red);
    colorMap.put(ParseCpslConstants.leftBrace, style);
    colorMap.put(ParseCpslConstants.rightBrace, style);
    colorMap.put(ParseCpslConstants.leftBracket, style);
    colorMap.put(ParseCpslConstants.rightBracket, style);
    colorMap.put(ParseCpslConstants.leftSquare, style);
    colorMap.put(ParseCpslConstants.rightSquare, style);

    style = textArea.addStyle("Blue", null);
    StyleConstants.setForeground(style, Color.blue);
    colorMap.put(ParseCpslConstants.rule, style);
    colorMap.put(ParseCpslConstants.priority, style);
    colorMap.put(ParseCpslConstants.macro, style);
    colorMap.put(ParseCpslConstants.bool, style);
    colorMap.put(ParseCpslConstants.phase, style);
    colorMap.put(ParseCpslConstants.input, style);
    colorMap.put(ParseCpslConstants.option, style);
    colorMap.put(ParseCpslConstants.multiphase, style);
    colorMap.put(ParseCpslConstants.phases, style);

    style = textArea.addStyle("Dark_Cyan", null);
    StyleConstants.setForeground(style, new Color(0, 128, 128));
    colorMap.put(ParseCpslConstants.string, style);

    style = textArea.addStyle("Green", null);
    StyleConstants.setForeground(style, new Color(0, 128, 0));
    colorMap.put(ParseCpslConstants.singleLineCStyleComment, style);
    colorMap.put(ParseCpslConstants.singleLineCpslStyleComment, style);
    colorMap.put(ParseCpslConstants.commentStart, style);
    colorMap.put(ParseCpslConstants.commentChars, style);
    colorMap.put(ParseCpslConstants.commentEnd, style);
    colorMap.put(ParseCpslConstants.phasesSingleLineCStyleComment, style);
    colorMap.put(ParseCpslConstants.phasesSingleLineCpslStyleComment, style);
    colorMap.put(ParseCpslConstants.phasesCommentStart, style);
    colorMap.put(ParseCpslConstants.phasesCommentChars, style);
    colorMap.put(ParseCpslConstants.phasesCommentEnd, style);
  }

  public void setTarget(Object target) {
    if(!(target instanceof Transducer)) { throw new IllegalArgumentException(
            "The GATE jape editor can only be used with a GATE jape transducer!\n"
                    + target.getClass().toString()
                    + " is not a GATE Jape Transducer!"); }

    if(transducer != null) {
      transducer.removeProgressListener(this);
    }

    this.transducer = (Transducer)target;
    japeFileURL = transducer.getGrammarURL();
    // reading japeFile
    readJAPEFileContents();
    ((Transducer)target).addProgressListener(this);
  }

  private void readJAPEFileContents() {
    updating = true;
    try {
      if(japeFileURL != null) {
        Reader japeReader = null;
        if(transducer.getEncoding() == null) {
          japeReader = new InputStreamReader(japeFileURL.openStream());
        } else {
          japeReader =
                  new InputStreamReader(japeFileURL.openStream(), transducer
                          .getEncoding());
        }
        BufferedReader br = new BufferedReader(japeReader);
        String content = br.readLine();
        japeFileContents = "";
        while(content != null) {
          japeFileContents += content + "\n";
          content = br.readLine();
        }

        textArea.setEditable(true);
        textArea.setText(japeFileContents);
        textArea.updateUI();
        textArea.setEditable(false);
        br.close();

        cboPhases.removeAllItems();
        System.out.println(cboPhases.getItemCount());
        String mainName = Files.fileFromURL(japeFileURL).getName();
        mainName = mainName.substring(0,mainName.lastIndexOf("."));
        cboPhases.addItem("");
        
        ParseCpslTokenManager tokenManager =
                new ParseCpslTokenManager(new SimpleCharStream(
                        new StringReader(japeFileContents)));

        StyledDocument doc = textArea.getStyledDocument();

        java.util.List<Integer> lineOffsets = new ArrayList<Integer>();

        lineOffsets.add(0);
        int startFrom = 0;
        int offset = 0;
        while((offset = japeFileContents.indexOf("\n", startFrom)) != -1) {
          lineOffsets.add(offset + 1);
          startFrom = offset + 1;
        }

        Token t;
        while((t = tokenManager.getNextToken()).kind != 0) {

          Token special = t.specialToken;
          while(special != null) {
            Style style = colorMap.get(special.kind);
            if(style != null) {
              int start =
                      lineOffsets.get(special.beginLine - 1)
                              + special.beginColumn - 1;
              int end =
                      lineOffsets.get(special.endLine - 1) + special.endColumn
                              - 1;
              doc.setCharacterAttributes(start, end - start + 1, style, true);
            }

            special = special.specialToken;
          }

          Style style = colorMap.get(t.kind);

          if(style != null) {
            int start = lineOffsets.get(t.beginLine - 1) + t.beginColumn - 1;
            int end = lineOffsets.get(t.endLine - 1) + t.endColumn - 1;
            doc.setCharacterAttributes(start, end - start + 1, style, true);
          }
          
          if (t.kind == ParseCpslConstants.path) {
            cboPhases.addItem(t.toString());
          }
        }
      } else {
        textArea
                .setText("The JAPE Transducer Object was loaded from a serialised tranducer and therefore cannot show any text!");
      }
    } catch(IOException ioe) {
      throw new GateRuntimeException(ioe);
    }
    updating = false;
  }

  public void processFinished() {
    japeFileURL = transducer.getGrammarURL();
    readJAPEFileContents();
  }

  public void progressChanged(int progress) {

  }
}
