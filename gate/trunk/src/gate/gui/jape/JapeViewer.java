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
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

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

  private boolean updating = false;

  /** Transducer */
  private Transducer transducer;

  private JTree treePhases;

  Map<Integer, Style> colorMap = new HashMap<Integer, Style>();

  /** An Init method */
  public Resource init() {
    initGuiComponents();
    return this;
  }

  private void initGuiComponents() {
    setLayout(new BorderLayout());
    textArea = new JTextPane();
    textArea.setEditable(false);
    JScrollPane textScroll = new JScrollPane(textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(textScroll, BorderLayout.CENTER);

    treePhases = new JTree();
    JScrollPane treeScroll = new JScrollPane(treePhases,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(treeScroll, BorderLayout.WEST);
    treePhases.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
    treePhases.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if(updating) return;
        if(e.getPath().getLastPathComponent() == null) return;

        try {
          readJAPEFileContents(new URL(transducer.getGrammarURL(), e.getPath()
                  .getLastPathComponent()
                  + ".jape"));
        }
        catch(MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    });

    // if we want to set the jape to be monospaced (like most code editors) then do this...
    /*
     * MutableAttributeSet attrs = textArea.getInputAttributes();
     * StyleConstants.setFontFamily(attrs, "monospaced"); StyledDocument
     * doc = textArea.getStyledDocument(); doc.setCharacterAttributes(0,
     * doc.getLength() + 1, attrs, false);
     */

    Style style = textArea.addStyle("brackets", null);
    StyleConstants.setForeground(style, Color.red);
    colorMap.put(ParseCpslConstants.leftBrace, style);
    colorMap.put(ParseCpslConstants.rightBrace, style);
    colorMap.put(ParseCpslConstants.leftBracket, style);
    colorMap.put(ParseCpslConstants.rightBracket, style);
    colorMap.put(ParseCpslConstants.leftSquare, style);
    colorMap.put(ParseCpslConstants.rightSquare, style);

    style = textArea.addStyle("keywords", null);
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

    style = textArea.addStyle("strings", null);
    StyleConstants.setForeground(style, new Color(0, 128, 128));
    colorMap.put(ParseCpslConstants.string, style);

    style = textArea.addStyle("comments", null);
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
    if(!(target instanceof Transducer)) {
      throw new IllegalArgumentException(
              "The GATE jape viewer can only be used with a GATE jape transducer!\n"
                      + target.getClass().toString()
                      + " is not a GATE Jape Transducer!");
    }

    if(transducer != null) {
      transducer.removeProgressListener(this);
    }

    this.transducer = (Transducer)target;
    URL japeFileURL = transducer.getGrammarURL();
    String japePhaseName = japeFileURL.getFile();
    japePhaseName = japePhaseName.substring(japePhaseName.lastIndexOf("/") + 1,
            japePhaseName.length() - 5);
    treePhases.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(
            japePhaseName)));
    treePhases.setSelectionRow(0);
    // reading japeFile
    readJAPEFileContents(japeFileURL);
    ((Transducer)target).addProgressListener(this);
  }

  private void readJAPEFileContents(URL url) {
    if(treePhases.getLastSelectedPathComponent() == null) return;
    updating = true;

    try {
      if(url != null) {
        Reader japeReader = null;
        if(transducer.getEncoding() == null) {
          japeReader = new InputStreamReader(url.openStream());
        }
        else {
          japeReader = new InputStreamReader(url.openStream(), transducer
                  .getEncoding());
        }
        BufferedReader br = new BufferedReader(japeReader);
        String content = br.readLine();
        StringBuilder japeFileContents = new StringBuilder();
        java.util.List<Integer> lineOffsets = new ArrayList<Integer>();
        
        while(content != null) {
          lineOffsets.add(japeFileContents.length());
          japeFileContents.append(content).append("\n");
          content = br.readLine();
        }

        textArea.setEditable(true);
        textArea.setText(japeFileContents.toString());
        textArea.updateUI();
        textArea.setEditable(false);
        br.close();

        ParseCpslTokenManager tokenManager = new ParseCpslTokenManager(
                new SimpleCharStream(new StringReader(japeFileContents
                        .toString())));

        StyledDocument doc = textArea.getStyledDocument();

        ((DefaultMutableTreeNode)treePhases.getSelectionPath()
                .getLastPathComponent()).removeAllChildren();

        Token t;
        while((t = tokenManager.getNextToken()).kind != 0) {

          Token special = t.specialToken;
          while(special != null) {
            Style style = colorMap.get(special.kind);
            if(style != null) {
              int start = lineOffsets.get(special.beginLine - 1)
                      + special.beginColumn - 1;
              int end = lineOffsets.get(special.endLine - 1)
                      + special.endColumn - 1;
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

          if(t.kind == ParseCpslConstants.path) {
            ((DefaultMutableTreeNode)treePhases.getSelectionPath()
                    .getLastPathComponent()).add(new DefaultMutableTreeNode(t
                    .toString()));
          }
        }
      }
      else {
        textArea
                .setText("The JAPE Transducer was loaded from a serialised tranducer and the source is not available.");
      }
    }
    catch(IOException ioe) {
      throw new GateRuntimeException(ioe);
    }
    
    if(treePhases.getSelectionRows() != null
            && treePhases.getSelectionRows().length > 0)
      treePhases.expandRow(treePhases.getSelectionRows()[0]);

    updating = false;
  }

  public void processFinished() {
    setTarget(transducer);
  }

  public void progressChanged(int progress) {

  }
}
