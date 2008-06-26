/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  SearchAndAnnotatePanel.java
 *
 *  Thomas Heitz, Nov 21, 2007
 *
 *  $Id: SchemaAnnotationEditor.java 9221 2007-11-14 17:46:37Z valyt $
 */


package gate.gui.annedit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.event.*;
import gate.gui.MainFrame;
import gate.util.*;

/**
 * Build a GUI for searching and annotating annotations in a text.
 * It needs to be called from a gate.gui.docview.AnnotationEditor.
 * 
 * <p>Here is an example how to add it to a JPanel panel.
 *
 * <pre>
 * SearchAndAnnotatePanel searchPanel =
 *           new SearchAndAnnotatePanel(panel.getBackground(), this, window);
 * panel.add(searchPanel);
 * </pre>
 */
public class SearchAndAnnotatePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  /**
   * The annotation editor that use this search and annotate panel.
   */
  private AnnotationEditor annotationEditor;

  /**
   * Window that contains the annotation editor.
   */
  private Window annotationEditorWindow;

  /**
   * Listener for updating the list of searched annotations.
   */
  protected AnnotationSetListener annotationSetListener;
  
  /**
   * The box used to host the search pane.
   */
  protected Box searchBox;

  /**
   * The pane containing the UI for search and annotate functionality.
   */
  protected JPanel searchPane;
  
  /**
   * Text field for searching
   */
  protected JTextField searchTextField;
  
  /**
   * Checkbox for enabling RegEx searching.
   */
  protected JCheckBox searchRegExpChk;
  
  /**
   * Help button that gives predefined regular expressions.
   */
  protected JButton helpRegExpButton;

  /**
   * Checkbox for enabling case sensitive searching.
   */
  protected JCheckBox searchCaseSensChk;
  
  /**
   * Checkbox for enabling whole word searching.
   */
  protected JCheckBox searchWholeWordsChk;

  /**
   * Checkbox for enabling whole word searching.
   */
  protected JCheckBox searchHighlightsChk;

  /**
   * Checkbox for showing the search UI.
   */
  protected JCheckBox searchEnabledCheck;

  /**
   * Shared instance of the matcher.
   */
  protected Matcher matcher;

  protected FindFirstAction findFirstAction;
  
  protected FindPreviousAction findPreviousAction;

  protected FindNextAction findNextAction;
  
  protected AnnotateMatchAction annotateMatchAction;
  
  protected AnnotateAllMatchesAction annotateAllMatchesAction;
  
  protected UndoAnnotateAllMatchesAction undoAnnotateAllMatchesAction;

  protected int nextMatchStartsFrom;
  
  protected String content;

  /**
   * Start and end index of the all the matches. 
   */
  protected LinkedList<Vector<Integer>> matchedIndexes;
  
  /**
   * List of annotations ID that have been created
   * by the AnnotateAllMatchesAction. 
   */
  protected LinkedList<Annotation> annotateAllAnnotationsID;
  
  protected SmallButton firstSmallButton;

  protected SmallButton annotateAllMatchesSmallButton;
  


  public SearchAndAnnotatePanel(Color color,
          AnnotationEditor annotationEditor, Window window) {

    this.annotationEditor = annotationEditor;
    annotationEditorWindow = window;

    initGui(color);

    // initially searchBox is collapsed
    searchBox.remove(searchPane);
    searchCaseSensChk.setVisible(false);
    searchRegExpChk.setVisible(false);
    searchWholeWordsChk.setVisible(false);
    searchHighlightsChk.setVisible(false);

    // if the user never gives the focus to the textPane then
    // there will never be any selection in it so we force it
    getOwner().getTextComponent().requestFocusInWindow();

    initListeners();
    
    content = getOwner().getDocument().getContent().toString();
  }

  /**
   * Build the GUI with JPanels and Boxes.
   *
   * @param color Color of the background.
   * _                    _        _          _         _
   * V Search & Annotate |_| Case |_| Regexp |_| Whole |_| Highlights
   *  _______________________________________________
   * |V_Searched_Expression__________________________| |?|
   * 
   * |First| |Prev.| |Next| |Annotate| |Ann. all next|
   */
  protected void initGui(Color color) {

    JPanel mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBackground(color);
    mainPane.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));

    setLayout(new BorderLayout());
    add(mainPane, BorderLayout.CENTER);

    searchBox = Box.createVerticalBox();
    String aTitle = "Open Search & Annotate tool";
    JLabel label = new JLabel(aTitle);
    searchBox.setMinimumSize(
      new Dimension(label.getPreferredSize().width, 0));    
    searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel firstLinePane = new JPanel();
    firstLinePane.setAlignmentX(Component.LEFT_ALIGNMENT);
    firstLinePane.setAlignmentY(Component.TOP_ALIGNMENT);
    firstLinePane.setLayout(new BoxLayout(firstLinePane, BoxLayout.Y_AXIS));
    firstLinePane.setBackground(color);
    Box hBox = Box.createHorizontalBox();
      searchEnabledCheck = new JCheckBox(aTitle, MainFrame.getIcon("closed"), false);
      searchEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      searchEnabledCheck.setBackground(color);
      searchEnabledCheck.setToolTipText("<html>Allows to search for an "
        +"expression and<br>annotate one or all the matches.</html>");
    hBox.add(searchEnabledCheck);
    hBox.add(Box.createHorizontalStrut(5));
      searchCaseSensChk = new JCheckBox("Case", true);
      searchCaseSensChk.setToolTipText("Case sensitive search.");
      searchCaseSensChk.setBackground(color);
    hBox.add(searchCaseSensChk);
    hBox.add(Box.createHorizontalStrut(5));
      searchRegExpChk = new JCheckBox("Regexp", false);
      searchRegExpChk.setToolTipText("Regular expression search.");
      searchRegExpChk.setBackground(color);
    hBox.add(searchRegExpChk);
    hBox.add(Box.createHorizontalStrut(5));
      searchWholeWordsChk = new JCheckBox("Whole", false);
      searchWholeWordsChk.setBackground(color);
      searchWholeWordsChk.setToolTipText("Whole word search.");
    hBox.add(searchWholeWordsChk);
    hBox.add(Box.createHorizontalStrut(5));
      searchHighlightsChk = new JCheckBox("Highlights", false);
      searchHighlightsChk.setToolTipText(
        "Restrict the search on the highlighted annotations.");
      searchHighlightsChk.setBackground(color);
    hBox.add(searchHighlightsChk);
    hBox.add(Box.createHorizontalGlue());
    firstLinePane.add(hBox);
    searchBox.add(firstLinePane);

    searchPane = new JPanel();
    searchPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchPane.setAlignmentY(Component.TOP_ALIGNMENT);
    searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.Y_AXIS));
    searchPane.setBackground(color);
      hBox = Box.createHorizontalBox();
      hBox.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
      hBox.add(Box.createHorizontalStrut(5));
        searchTextField = new JTextField(10);
        searchTextField.setToolTipText("Enter an expression to search for.");
        //disallow vertical expansion
        searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
            searchTextField.getPreferredSize().height));
        searchTextField.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            findFirstAction.actionPerformed(null);
          }
        });
      hBox.add(searchTextField);
      hBox.add(Box.createHorizontalStrut(2));
      helpRegExpButton = new JButton("?");
      helpRegExpButton.setMargin(new Insets(0, 2, 0, 2));
      helpRegExpButton.setToolTipText("Predefined search expressions.");
      helpRegExpButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            String[] values1 = {
              "Number",
              "Person"
            };
            String[] values2 = {
              "Any character",
              "The beginning of a line",
              "The end of a line",
              "All letters",
              "Letter uppercase",
              "Letter lowercase",
              "Letter titlecase",
              "Letter modifier",
              "Letter other",
              "All Numbers",
              "Number decimal digit",
              "Number letter",
              "Number other",
              "All punctuations",
              "Punctuation connector",
              "Punctuation dash",
              "Punctuation open",
              "Punctuation close",
              "Punctuation initial quote",
              "Punctuation final quote",
              "Punctuation other",
              "All symbols",
              "Symbol math",
              "Symbol currency",
              "Symbol modifier",
              "Symbol other",
              "All separators",
              "Separator space",
              "Separator line",
              "Separator paragraph",
              "All Marks",
              "Mark nonspacing",
              "Mark spacing combining",
              "Mark enclosing",
              "All others",
              "Other control",
              "Other format",
              "Other surrogate",
              "Other private use",
              "Other not assigned",
              "Any character except Category",
              "Category1 and/or Category2",
              "Category1 and Category2"
            };
            String[] values3 = {
              "Either the selection or X",
              "Once or not at all",
              "Zero or more times",
              "One or more times",
              "Capturing group",
              "Non-capturing group"
            };
            JPanel vspace1 = new JPanel();
            vspace1.setSize(0, 5);
            final JList list1 = new JList(values1);
            list1.setVisibleRowCount(Math.min(10, values1.length));
            list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp1 = new JScrollPane(list1);
            final JButton b1 = new JButton("Replace search expression");
            b1.setEnabled(false);
            JPanel vspace2 = new JPanel();
            vspace2.setSize(0, 5);
            final JList list2 = new JList(values2);
            list2.setVisibleRowCount(Math.min(10, values2.length));
            list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp2 = new JScrollPane(list2);
            final JButton b2 = new JButton("Insert at the caret position");
            b2.setEnabled(false);
            JPanel vspace3 = new JPanel();
            vspace3.setSize(0, 5);
            final JList list3 = new JList(values3);
            list3.setVisibleRowCount(Math.min(10, values3.length));
            list3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp3 = new JScrollPane(list3);
            final JButton b3 = new JButton("Modify the selection");
            b3.setEnabled(false);
            if (searchTextField.getSelectedText() == null) {
              list3.setEnabled(false);
            }
            Object[] messageObjects = {
              "Choose a predefined search:",
              vspace1, jsp1, b1, vspace2, jsp2, b2, vspace3, jsp3, b3
            };
            String options[] = {"Cancel"};
            final JOptionPane optionPane = new JOptionPane(
              messageObjects,
              JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
              null, options, "Cancel");
            b1.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list1.getSelectedValue() != null) {
                  optionPane.setValue(list1.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list1.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list1.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list1.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list1.getSelectedValue() != null) {
                  b1.setEnabled(true);
                } else {
                  b1.setEnabled(false);
                }
              }
            });
            b2.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list2.getSelectedValue() != null) {
                  optionPane.setValue(list2.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list2.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list2.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list2.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list2.getSelectedValue() != null) {
                  b2.setEnabled(true);
                } else {
                  b2.setEnabled(false);
                }
              }
            });
            b3.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list3.getSelectedValue() != null) {
                  optionPane.setValue(list3.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list3.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list3.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list3.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list3.getSelectedValue() != null) {
                  b3.setEnabled(true);
                } else {
                  b3.setEnabled(false);
                }
              }
            });
            JDialog optionDialog = optionPane.createDialog(
              gate.gui.MainFrame.getInstance(), "GATE");
            optionDialog.setVisible(true);
            Object selectedValue = optionPane.getValue();
            if (selectedValue == null
             || !(selectedValue instanceof String)
             || selectedValue.equals("Cancel")) {
              return;
            } else {
              searchCaseSensChk.setSelected(true);
              searchRegExpChk.setSelected(true);
              searchWholeWordsChk.setSelected(false);
            }
            int p = searchTextField.getCaretPosition();
            int s1 = searchTextField.getSelectionStart();
            int s2 = searchTextField.getSelectionEnd();
            try {
            if (selectedValue.equals("Number")) {
              searchTextField.setText("\\b[\\p{N}][\\p{N},.]*\\b");
            } else if (selectedValue.equals("Person")) {
              searchTextField.setText("\\p{Lu}\\p{L}+, \\p{Lu}\\.(?: \\p{Lu}\\.)*");
            } else if (selectedValue.equals("Either the selection or X")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")|(?:X)", null);
            } else if (selectedValue.equals("Once or not at all")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")?", null);
            } else if (selectedValue.equals("Zero or more times")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")*", null);
            } else if (selectedValue.equals("One or more times")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")+", null);
            } else if (selectedValue.equals("Capturing group")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")", null);
            } else if (selectedValue.equals("Non-capturing group")) {
              searchTextField.getDocument().insertString(s1, "(?:", null);
              searchTextField.getDocument().insertString(s2+3, ")", null);
            } else if (selectedValue.equals("Any character")) {
              searchTextField.getDocument().insertString(p, ".", null);
            } else if (selectedValue.equals("The beginning of a line")) {
              searchTextField.getDocument().insertString(p, "^", null);
            } else if (selectedValue.equals("The end of a line")) {
              searchTextField.getDocument().insertString(p, "$", null);
            } else if (selectedValue.equals("Any character except Category")) {
              searchTextField.getDocument().insertString(p, "\\P{Category}", null);
            } else if (selectedValue.equals("Category1 and/or Category2")) {
              searchTextField.getDocument().insertString(p, "[\\p{Category1}\\p{Category2}]", null);
            } else if (selectedValue.equals("Category1 and Category2")) {
              searchTextField.getDocument().insertString(p, "[\\p{Category1}&&\\p{Category2}]", null);
            } else if (selectedValue.equals("All letters")) {
              searchTextField.getDocument().insertString(p, "\\p{L}", null);
            } else if (selectedValue.equals("Letter uppercase")) {
              searchTextField.getDocument().insertString(p, "\\p{Lu}", null);
            } else if (selectedValue.equals("Letter lowercase")) {
              searchTextField.getDocument().insertString(p, "\\p{Ll}", null);
            } else if (selectedValue.equals("Letter titlecase")) {
              searchTextField.getDocument().insertString(p, "\\p{Lt}", null);
            } else if (selectedValue.equals("Letter modifier")) {
              searchTextField.getDocument().insertString(p, "\\p{Lm}", null);
            } else if (selectedValue.equals("Letter other")) {
              searchTextField.getDocument().insertString(p, "\\p{Lo}", null);
            } else if (selectedValue.equals("All Marks")) {
              searchTextField.getDocument().insertString(p, "\\p{M}", null);
            } else if (selectedValue.equals("Mark nonspacing")) {
              searchTextField.getDocument().insertString(p, "\\p{Mn}", null);
            } else if (selectedValue.equals("Mark spacing combining")) {
              searchTextField.getDocument().insertString(p, "\\p{Mc}", null);
            } else if (selectedValue.equals("Mark enclosing")) {
              searchTextField.getDocument().insertString(p, "\\p{Me}", null);
            } else if (selectedValue.equals("All Numbers")) {
              searchTextField.getDocument().insertString(p, "\\p{N}", null);
            } else if (selectedValue.equals("Number decimal digit")) {
              searchTextField.getDocument().insertString(p, "\\p{Nd}", null);
            } else if (selectedValue.equals("Number letter")) {
              searchTextField.getDocument().insertString(p, "\\p{Nl}", null);
            } else if (selectedValue.equals("Number other")) {
              searchTextField.getDocument().insertString(p, "\\p{No}", null);
            } else if (selectedValue.equals("All separators")) {
              searchTextField.getDocument().insertString(p, "\\p{Z}", null);
            } else if (selectedValue.equals("Separator space")) {
              searchTextField.getDocument().insertString(p, "\\p{Zs}", null);
            } else if (selectedValue.equals("Separator line")) {
              searchTextField.getDocument().insertString(p, "\\p{Zl}", null);
            } else if (selectedValue.equals("Separator paragraph")) {
              searchTextField.getDocument().insertString(p, "\\p{Zp}", null);
            } else if (selectedValue.equals("All others")) {
              searchTextField.getDocument().insertString(p, "\\p{C}", null);
            } else if (selectedValue.equals("Other control")) {
              searchTextField.getDocument().insertString(p, "\\p{Cc}", null);
            } else if (selectedValue.equals("Other format")) {
              searchTextField.getDocument().insertString(p, "\\p{Cf}", null);
            } else if (selectedValue.equals("Other surrogate")) {
              searchTextField.getDocument().insertString(p, "\\p{Cs}", null);
            } else if (selectedValue.equals("Other private use")) {
              searchTextField.getDocument().insertString(p, "\\p{Co}", null);
            } else if (selectedValue.equals("Other not assigned")) {
              searchTextField.getDocument().insertString(p, "\\p{Cn}", null);
            } else if (selectedValue.equals("All punctuations")) {
              searchTextField.getDocument().insertString(p, "\\p{P}", null);
            } else if (selectedValue.equals("Punctuation connector")) {
              searchTextField.getDocument().insertString(p, "\\p{Pc}", null);
            } else if (selectedValue.equals("Punctuation dash")) {
              searchTextField.getDocument().insertString(p, "\\p{Pd}", null);
            } else if (selectedValue.equals("Punctuation open")) {
              searchTextField.getDocument().insertString(p, "\\p{Ps}", null);
            } else if (selectedValue.equals("Punctuation close")) {
              searchTextField.getDocument().insertString(p, "\\p{Pe}", null);
            } else if (selectedValue.equals("Punctuation initial quote")) {
              searchTextField.getDocument().insertString(p, "\\p{Pi}", null);
            } else if (selectedValue.equals("Punctuation final quote")) {
              searchTextField.getDocument().insertString(p, "\\p{Pf}", null);
            } else if (selectedValue.equals("Punctuation other")) {
              searchTextField.getDocument().insertString(p, "\\p{Po}", null);
            } else if (selectedValue.equals("All symbols")) {
              searchTextField.getDocument().insertString(p, "\\p{S}", null);
            } else if (selectedValue.equals("Symbol math")) {
              searchTextField.getDocument().insertString(p, "\\p{Sm}", null);
            } else if (selectedValue.equals("Symbol currency")) {
              searchTextField.getDocument().insertString(p, "\\p{Sc}", null);
            } else if (selectedValue.equals("Symbol modifier")) {
              searchTextField.getDocument().insertString(p, "\\p{Sk}", null);
            } else if (selectedValue.equals("Symbol other")) {
              searchTextField.getDocument().insertString(p, "\\p{So}", null);
            }
            } catch (BadLocationException e) {
              // should never happend
              throw new LuckyException(e);
            }
          }
        });
      hBox.add(helpRegExpButton);
      hBox.add(Box.createHorizontalGlue());
    searchPane.add(hBox);
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
        findFirstAction = new FindFirstAction();
        firstSmallButton = new SmallButton(findFirstAction);
      hBox.add(firstSmallButton);
      hBox.add(Box.createHorizontalStrut(5));
        findPreviousAction = new FindPreviousAction();
        findPreviousAction.setEnabled(false);
      hBox.add(new SmallButton(findPreviousAction));
      hBox.add(Box.createHorizontalStrut(5));
        findNextAction = new FindNextAction();
        findNextAction.setEnabled(false);
      hBox.add(new SmallButton(findNextAction));
      hBox.add(Box.createHorizontalStrut(5));
        annotateMatchAction = new AnnotateMatchAction();
        annotateMatchAction.setEnabled(false);
      hBox.add(new SmallButton(annotateMatchAction));
      hBox.add(Box.createHorizontalStrut(5));
        annotateAllMatchesAction = new AnnotateAllMatchesAction();
        undoAnnotateAllMatchesAction = new UndoAnnotateAllMatchesAction();
        annotateAllMatchesSmallButton =
          new SmallButton(annotateAllMatchesAction);
        annotateAllMatchesAction.setEnabled(false);
        undoAnnotateAllMatchesAction.setEnabled(false);
      hBox.add(annotateAllMatchesSmallButton);
      hBox.add(Box.createHorizontalStrut(5));
    searchPane.add(hBox);
    searchBox.add(searchPane);

    mainPane.add(searchBox);
  }

  protected void initListeners() {

    searchEnabledCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (searchEnabledCheck.isSelected()) {
          //add the search box if not already there
          if (!searchBox.isAncestorOf(searchPane)) {
            //if empty, initialise the search field to the text of the current
            //annotation
            String searchText = searchTextField.getText(); 
            if (searchText == null || searchText.trim().length() == 0) {
              if (annotationEditor.getAnnotationCurrentlyEdited() != null
                && getOwner() != null) {
                String annText = getOwner().getDocument().getContent().
                    toString().substring(
                      annotationEditor.getAnnotationCurrentlyEdited()
                        .getStartNode().getOffset().intValue(),
                      annotationEditor.getAnnotationCurrentlyEdited()
                        .getEndNode().getOffset().intValue());
                searchTextField.setText(annText);
              }
            }
            searchBox.add(searchPane);
          }
          searchEnabledCheck.setText("");
          searchCaseSensChk.setVisible(true);
          searchRegExpChk.setVisible(true);
          searchWholeWordsChk.setVisible(true);
          searchHighlightsChk.setVisible(true);
          searchTextField.requestFocusInWindow();
          searchTextField.selectAll();
          annotationEditorWindow.pack();
          annotationEditor.setPinnedMode(true);

        } else {
          if(searchBox.isAncestorOf(searchPane)){
            searchEnabledCheck.setText("Open Search & Annotate tool");
            searchBox.remove(searchPane);
            searchCaseSensChk.setVisible(false);
            searchRegExpChk.setVisible(false);
            searchWholeWordsChk.setVisible(false);
            searchHighlightsChk.setVisible(false);
            annotationEditorWindow.pack();
          }
        }
      }
    });

    this.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        // put the selection of the document into the search text field
        if (getOwner().getTextComponent().getSelectedText() != null) {
          searchTextField.setText(getOwner().getTextComponent().getSelectedText());
        }
      }
      public void ancestorRemoved(AncestorEvent event) {
        // if the editor window is closed
        enableActions(false);
      }
      public void ancestorMoved(AncestorEvent event) {
        // do nothing
      }
    });

    searchTextField.getDocument().addDocumentListener(new DocumentListener(){
      public void changedUpdate(DocumentEvent e) {
        enableActions(false);
      }
      public void insertUpdate(DocumentEvent e) {
        enableActions(false);
      }
      public void removeUpdate(DocumentEvent e) {
        enableActions(false);
      }
    });

    searchCaseSensChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

    searchRegExpChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

    searchWholeWordsChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

      searchHighlightsChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

  }

  private void enableActions(boolean state){
    findPreviousAction.setEnabled(state);
    findNextAction.setEnabled(state);
    annotateMatchAction.setEnabled(state);
    annotateAllMatchesAction.setEnabled(state);
    if (annotateAllMatchesSmallButton.getAction()
            .equals(undoAnnotateAllMatchesAction)) {
      annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
    }
  }

  private boolean isAnnotationEditorReady() {
    if (!annotationEditor.editingFinished()
     || getOwner() == null
     || annotationEditor.getAnnotationCurrentlyEdited() == null
     || annotationEditor.getAnnotationSetCurrentlyEdited() == null) {
      JOptionPane.showMessageDialog(annotationEditorWindow,
        "Please set all required features first.", "GATE",
        JOptionPane.INFORMATION_MESSAGE);
      return false;
    } else {
      return true; 
    }
  }

  protected class FindFirstAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public FindFirstAction(){
      super("First");
      super.putValue(SHORT_DESCRIPTION, "Finds the first occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_F);
    }

    public void actionPerformed(ActionEvent evt){
      if (!isAnnotationEditorReady()) { return; }
      annotationEditor.setPinnedMode(true);
      annotationEditor.setEnableEditing(false);
      String patternText = searchTextField.getText();
      Pattern pattern = null;

      try {
        String prefixPattern = searchWholeWordsChk.isSelected() ? "\\b":"";
        prefixPattern += searchRegExpChk.isSelected() ? "":"\\Q";
        String suffixPattern = searchRegExpChk.isSelected() ? "":"\\E";
        suffixPattern += searchWholeWordsChk.isSelected() ? "\\b":"";
        patternText = prefixPattern + patternText + suffixPattern;
        // TODO: Pattern.UNICODE_CASE prevent insensitive case to work
        // for Java 1.5 but works with Java 1.6
        pattern = searchCaseSensChk.isSelected() ?
                  Pattern.compile(patternText) :
                  Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);

      } catch(PatternSyntaxException e) {
        // FIXME: put this error dialog in front of the editor dialog
        // when the dialog is a JWindow
//        annotationEditorWindow.setAlwaysOnTop(false);
//          annotationEditorWindow.toBack();
        JOptionPane errorOptionsPane = new JOptionPane(
                "Invalid pattern!\n" + e.toString(),
                JOptionPane.ERROR_MESSAGE);
        JDialog errorDialog = errorOptionsPane
                .createDialog(annotationEditorWindow, "GATE");
//          errorDialog.setAlwaysOnTop(true);
        errorDialog.setVisible(true);
//          errorDialog.toFront();
        return;
      }

      matcher = pattern.matcher(content);
      boolean found = false;
      int start = -1;
      int end = -1;
      nextMatchStartsFrom = 0;
      while (matcher.find(nextMatchStartsFrom) && !found) {
        start = (matcher.groupCount()>0)?matcher.start(1):matcher.start();
        end = (matcher.groupCount()>0)?matcher.end(1):matcher.end();
        found = false;
        if (searchHighlightsChk.isSelected()) {
          javax.swing.text.Highlighter.Highlight[] highlights =
            getOwner().getTextComponent().getHighlighter().getHighlights();
          for (javax.swing.text.Highlighter.Highlight h : highlights) {
            if (h.getStartOffset() <= start && h.getEndOffset() >= end) {
              found = true;
              break;
            }
          }
        } else {
          found = true;
        }
        nextMatchStartsFrom = end;
      }

      if (found) {
        findNextAction.setEnabled(true);
        annotateMatchAction.setEnabled(true);
        annotateAllMatchesAction.setEnabled(false);
        matchedIndexes = new LinkedList<Vector<Integer>>();
        Vector<Integer> v = new Vector<Integer>(2);
        v.add(new Integer(start));
        v.add(new Integer(end));
        matchedIndexes.add(v);
        getOwner().getTextComponent().select(start, end);
        annotationEditor.placeDialog(start, end);

      } else {
        // no match found
        findNextAction.setEnabled(false);
        annotateMatchAction.setEnabled(false);
      }
      findPreviousAction.setEnabled(false);
    }
  }
  
  protected class FindPreviousAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public FindPreviousAction() {
      super("Prev.");
      super.putValue(SHORT_DESCRIPTION, "Finds the previous occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    }

    public void actionPerformed(ActionEvent evt) {
      if (!isAnnotationEditorReady()) { return; }
      annotationEditor.setEnableEditing(false);
      // the first time we invoke previous action we want to go two
      // previous matches back not just one
      matchedIndexes.removeLast();

      Vector<Integer> v;
      if (matchedIndexes.size() == 1) {
        // no more previous annotation, disable the action
        findPreviousAction.setEnabled(false);
      }
      v = matchedIndexes.getLast();
      int start = (v.firstElement()).intValue();
      int end = (v.lastElement()).intValue();
      getOwner().getTextComponent().select(start, end);
      annotationEditor.placeDialog(start, end);
      // reset the matcher for the next FindNextAction
      nextMatchStartsFrom = start;
      findNextAction.setEnabled(true);
      annotateMatchAction.setEnabled(true);
    }
  }

  protected class FindNextAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public FindNextAction(){
      super("Next");
      super.putValue(SHORT_DESCRIPTION, "Finds the next occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    }

    public void actionPerformed(ActionEvent evt){
      if (!isAnnotationEditorReady()) { return; }
      annotationEditor.setEnableEditing(false);
      boolean found = false;
      int start = -1;
      int end = -1;
      nextMatchStartsFrom = getOwner().getTextComponent().getCaretPosition();

      while (matcher.find(nextMatchStartsFrom) && !found) {
        start = (matcher.groupCount()>0)?matcher.start(1):matcher.start();
        end = (matcher.groupCount()>0)?matcher.end(1):matcher.end();
        found = false;
        if (searchHighlightsChk.isSelected()) {
          javax.swing.text.Highlighter.Highlight[] highlights =
            getOwner().getTextComponent().getHighlighter().getHighlights();
          for (javax.swing.text.Highlighter.Highlight h : highlights) {
            if (h.getStartOffset() <= start && h.getEndOffset() >= end) {
              found = true;
              break;
            }
          }
        } else {
          found = true;
        }
        nextMatchStartsFrom = end;
      }

      if (found) {
        Vector<Integer> v = new Vector<Integer>(2);
        v.add(new Integer(start));
        v.add(new Integer(end));
        matchedIndexes.add(v);
        getOwner().getTextComponent().select(start, end);
        annotationEditor.placeDialog(start, end);
        findPreviousAction.setEnabled(true);
      } else {
        //no more matches possible
        findNextAction.setEnabled(false);
        annotateMatchAction.setEnabled(false);
      }
    }
  }
  
  protected class AnnotateMatchAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public AnnotateMatchAction(){
      super("Annotate");
      super.putValue(SHORT_DESCRIPTION, "Annotates the current match.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_A);
    }
    
    public void actionPerformed(ActionEvent evt){
      if (!isAnnotationEditorReady()) { return; }
      int start = getOwner().getTextComponent().getSelectionStart();
      int end = getOwner().getTextComponent().getSelectionEnd();
      FeatureMap features = Factory.newFeatureMap();
      if(annotationEditor.getAnnotationCurrentlyEdited().getFeatures() != null) 
        features.putAll(annotationEditor.getAnnotationCurrentlyEdited().getFeatures());
      try {
        Integer id = annotationEditor.getAnnotationSetCurrentlyEdited().add(
          new Long(start), new Long(end), 
          annotationEditor.getAnnotationCurrentlyEdited().getType(), features);
        Annotation newAnn =
          annotationEditor.getAnnotationSetCurrentlyEdited().get(id);
        getOwner().getTextComponent().select(end, end);
        annotationEditor.editAnnotation(newAnn,
           annotationEditor.getAnnotationSetCurrentlyEdited());
        annotateAllMatchesAction.setEnabled(true);
        if (annotateAllMatchesSmallButton.getAction()
              .equals(undoAnnotateAllMatchesAction)) {
          annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
        }
      }
      catch(InvalidOffsetException e) {
        //the offsets here should always be valid.
        throw new LuckyException(e);
      }
    }
  }
  
  protected class AnnotateAllMatchesAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public AnnotateAllMatchesAction(){
      super("Ann. all next");
      super.putValue(SHORT_DESCRIPTION, "Annotates all the following matches.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_L);
    }

    public void actionPerformed(ActionEvent evt){
      if (!isAnnotationEditorReady()) { return; }
      annotateAllAnnotationsID = new LinkedList<Annotation>();
      boolean found = false;
      int start = -1;
      int end = -1;
      nextMatchStartsFrom =
        getOwner().getTextComponent().getCaretPosition();
 
      do {
      found = false;
      while (matcher.find(nextMatchStartsFrom) && !found) {
        start = (matcher.groupCount()>0)?matcher.start(1):matcher.start();
        end = (matcher.groupCount()>0)?matcher.end(1):matcher.end();
        if (searchHighlightsChk.isSelected()) {
          javax.swing.text.Highlighter.Highlight[] highlights =
            getOwner().getTextComponent().getHighlighter().getHighlights();
          for (javax.swing.text.Highlighter.Highlight h : highlights) {
            if (h.getStartOffset() <= start && h.getEndOffset() >= end) {
              found = true;
              break;
            }
          }
        } else {
          found = true;
        }
        nextMatchStartsFrom = end;
      }
      if (found) { annotateCurrentMatch(start, end); }
      } while (found && !matcher.hitEnd());

      annotateAllMatchesSmallButton.setAction(undoAnnotateAllMatchesAction);
      undoAnnotateAllMatchesAction.setEnabled(true);
    }

    private void annotateCurrentMatch(int start, int end){
        FeatureMap features = Factory.newFeatureMap();
        features.put("safe.regex", "true");
        if(annotationEditor.getAnnotationCurrentlyEdited().getFeatures() != null) 
          features.putAll(annotationEditor.getAnnotationCurrentlyEdited().getFeatures());
        try {
          Integer id = annotationEditor.getAnnotationSetCurrentlyEdited().add(
            new Long(start), new Long(end), 
            annotationEditor.getAnnotationCurrentlyEdited().getType(),
            features);
          Annotation newAnn =
            annotationEditor.getAnnotationSetCurrentlyEdited().get(id);
          annotateAllAnnotationsID.add(newAnn);
        }
        catch(InvalidOffsetException e) {
          //the offsets here should always be valid.
          throw new LuckyException(e);
        }
    }
  }
  
  /**
   * Remove the annotations added by the last action that annotate all matches.
   */
  protected class UndoAnnotateAllMatchesAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public UndoAnnotateAllMatchesAction(){
      super("Undo");
      super.putValue(SHORT_DESCRIPTION, "Undo previous annotate all action.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    }
    
    public void actionPerformed(ActionEvent evt){

      Iterator<Annotation> it = annotateAllAnnotationsID.iterator();
      while (it.hasNext()) {
        annotationEditor.getAnnotationSetCurrentlyEdited().remove(it.next());
      }

      // just hide the editor to avoid editing null annotation
      annotationEditorWindow.setVisible(false);
      annotationEditor.setPinnedMode(false);

      annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
      annotateAllMatchesAction.setEnabled(false);
    }
  }

  /**
   * A smaller JButton with less margins.
   */
  protected class SmallButton extends JButton{

    private static final long serialVersionUID = 1L;

    public SmallButton(Action a) {
      super(a);
      setMargin(new Insets(0, 2, 0, 2));
    }
  }

  /**
   * @return the owner
   */
  public AnnotationEditorOwner getOwner() {
    return annotationEditor.getOwner();
  }

}
