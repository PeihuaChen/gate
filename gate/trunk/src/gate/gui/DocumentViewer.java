
//Title:        GATE
//Version:
//Copyright:    Copyright (c) 1999
//Author:       Hamish, Kalina, Christian, Valentin
//Company:      Univ Sheffield
//Description:

package gate.gui;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.text.Highlighter;

import gate.*;
import gate.gui.*;
import gate.util.*;


public class DocumentViewer extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane typeButtonsScroll = new JScrollPane();
  JScrollPane textScroll = new JScrollPane();
  JSplitPane centerSplit = new JSplitPane();
  JTextPane textPane = new JTextPane();
  Document document;
  SortedTable tableView;
  JScrollPane tableScroll = new JScrollPane();
  Box typesBox;
  Random randomGen = new Random();
  Object selectionTag = null;
  Highlighter.HighlightPainter selectionHP =
          new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(
          textPane.getSelectionColor());

  public DocumentViewer(Document doc) {
    document = doc;
    try  {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    typesBox = Box.createVerticalBox();
    this.setLayout(borderLayout1);
    centerSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    textPane.setEditorKit(new RawEditorKit());
    textPane.setEditable(false);
    textPane.setText(document.getContent().toString());
    tableView = new SortedTable();
    tableView.setTableModel(new AnnotationSetTableModel(document,
                                                   document.getAnnotations()));
    tableView.addMouseListener(new java.awt.event.MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        tableView_mousePressed(e);
      }
    });
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
/*
      public void componentShown(ComponentEvent e) {
        this_componentShown(e);
      }
*/
      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });
    typeButtonsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    typeButtonsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    tableScroll.getViewport().add(tableView, null);
    //create the types buttons
    typesBox.removeAll();
    Iterator typesIter = document.getAnnotations().getAllTypes().iterator();
    String currentType;
    int maxWidth = 0;
    LinkedList allButtons = new LinkedList();
    JButton typeButton, clearButton = new JButton();
    JLabel buttonLabel = new JLabel("Clear all");
    buttonLabel.setBackground(Color.black);
    buttonLabel.setForeground(Color.white);
//    buttonLabel.setBorder(LineBorder.createGrayLineBorder());
    buttonLabel.setOpaque(true);
    buttonLabel.setHorizontalAlignment(SwingConstants.CENTER);
    clearButton.add(buttonLabel,SwingConstants.CENTER);
    clearButton.setName("");
    clearButton.setBackground(Color.black);
    clearButton.setForeground(Color.white);
    clearButton.setHorizontalAlignment(JButton.CENTER);
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        typeButtonPressed("", null);
      }
    });
    ColorGenerator colGen = new ColorGenerator();
      if(maxWidth < clearButton.getPreferredSize().width)
        maxWidth = clearButton.getPreferredSize().width;
    while(typesIter.hasNext()){
      currentType = (String) typesIter.next();
      typeButton = new JButton();
      buttonLabel = new JLabel(currentType);
      buttonLabel.setBackground(Color.white);
      buttonLabel.setForeground(Color.black);
      buttonLabel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
      buttonLabel.setOpaque(true);
      buttonLabel.setHorizontalAlignment(SwingConstants.CENTER);
      typeButton.add(buttonLabel, SwingConstants.CENTER);
      typeButton.setName(currentType);
      typeButton.setForeground(Color.white);
      typeButton.setBackground(colGen.getNextColor());
      typeButton.setToolTipText(currentType);
      typeButton.setHorizontalAlignment(JButton.CENTER);
      typeButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(e.getSource() instanceof Container){
            JButton button = (JButton) e.getSource();
            Color col = button.getBackground();
            Color highlightCol = new Color(col.getRed(),
                                           col.getGreen(),
                                           col.getBlue(),
                                           128);
            typeButtonPressed(((JLabel)button.getComponent(0)).getText(),
                              highlightCol);
          }
        }
      });
      if(maxWidth < typeButton.getPreferredSize().width)
        maxWidth = typeButton.getPreferredSize().width;
      allButtons.add(typeButton);
    }
    Dimension bdim = new Dimension(maxWidth, clearButton.getPreferredSize().height);
    Dimension ldim = new Dimension(maxWidth -10, clearButton.getPreferredSize().height -4);
    clearButton.setMinimumSize(bdim);
    clearButton.setMaximumSize(bdim);
    ((JLabel)clearButton.getComponent(0)).setMinimumSize(ldim);
    ((JLabel)clearButton.getComponent(0)).setMaximumSize(ldim);

    typesBox.add(clearButton);
    Collections.sort(allButtons, new ButtonComparator());
    Iterator buttonsIter = allButtons.iterator();
    while(buttonsIter.hasNext()){
      typeButton = (JButton)buttonsIter.next();
      bdim = new Dimension(maxWidth, typeButton.getPreferredSize().height);
      ldim = new Dimension(maxWidth -10, typeButton.getPreferredSize().height -4);
      typeButton.setMinimumSize(bdim);
      typeButton.setMaximumSize(bdim);
      typeButton.setPreferredSize(bdim);
      ((JLabel)typeButton.getComponent(0)).setMinimumSize(ldim);
      ((JLabel)typeButton.getComponent(0)).setMaximumSize(ldim);
      typesBox.add(typeButton);
    }
    typeButtonsScroll.getViewport().add(typesBox, null);
/*
    typeButtonsScroll.setPreferredSize(
      new Dimension(maxWidth +
                    typeButtonsScroll.getVerticalScrollBar().getWidth() + 5,
                    typeButtonsScroll.getSize().height));
*/
    typeButtonsScroll.setPreferredSize(
      new Dimension(maxWidth +
                    (new JScrollBar()).getPreferredSize().width + 6,
                    typeButtonsScroll.getSize().height));

    centerSplit.add(textScroll, JSplitPane.TOP);
    centerSplit.add(tableScroll, JSplitPane.BOTTOM);
    textScroll.getViewport().add(textPane, null);
    RepaintManager repaintManager = RepaintManager.currentManager(textPane);
//    repaintManager.setDoubleBufferingEnabled(false);
//    textPane.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
    this.add(typeButtonsScroll, BorderLayout.EAST);

    this.add(centerSplit, BorderLayout.CENTER);
  }


  void typeButtonPressed(String type, Color col){
    if(type.equals("")){
      textPane.getHighlighter().removeAllHighlights();
      tableView.clearSelection();
      selectionTag = null;
    }else{
      AnnotationSet as = document.getAnnotations().get(type);
      Iterator annIter = as.iterator();
      gate.Annotation currentAnn;
      int start, end;
      try{
        Highlighter.HighlightPainter hp =
          new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(col);
        while(annIter.hasNext()){
          currentAnn = (gate.Annotation)annIter.next();
          start = currentAnn.getStartNode().getOffset().intValue();
          end = currentAnn.getEndNode().getOffset().intValue();
          textPane.getHighlighter().addHighlight(start, end, hp);
        }
      }catch(javax.swing.text.BadLocationException ble){
        ble.printStackTrace(System.err);
      }
      for(int i = 0; i < tableView.getRowCount(); i++)
        if(tableView.getModel().getValueAt(i, 2).equals(type))
          tableView.addRowSelectionInterval(i,i);
    }

//System.out.println(type);
  }

  void this_componentResized(ComponentEvent e) {
    if(textPane.getHeight() + 10 > centerSplit.getHeight() /2)
      centerSplit.setDividerLocation(0.5);
    else centerSplit.setDividerLocation(textPane.getHeight() + 10);

  }

  class ButtonComparator implements Comparator{
    public int compare(Object one, Object two){
      return ((JButton)one).getName().compareTo(((JButton)two).getName());
    }
  }

  void tableView_mousePressed(MouseEvent e) {
    try{
      int start = ((Long)tableView.getModel().getValueAt(
                    tableView.rowAtPoint(e.getPoint()), 0)).intValue();
      int end = ((Long)tableView.getModel().getValueAt(
                    tableView.rowAtPoint(e.getPoint()), 1)).intValue();

      //bring the selected element in the center of the scroll pane viewport
      Rectangle rect = textPane.modelToView(start);
      double x = rect.getLocation().getX() -
          (textScroll.getViewport().getSize().getWidth() - rect.getWidth())/2;
      double y = rect.getLocation().getY() -
          (textScroll.getViewport().getSize().getHeight() - rect.getHeight())/2;

      long lx = Math.round(Math.min(x, textPane.getSize().getWidth() -
          textScroll.getViewport().getSize().getWidth()));
      long ly = Math.round(Math.min(y, textPane.getSize().getHeight() -
          textScroll.getViewport().getSize().getHeight()));
      if(lx < 0) lx = 0;
      if(ly < 0) ly =0;
      textScroll.getViewport().setViewPosition(new Point((int)lx,(int)ly));
      //remove the previous selection highlight
      if(selectionTag != null)
        textPane.getHighlighter().removeHighlight(selectionTag);
      //highlight the selected element
      selectionTag =
        textPane.getHighlighter().addHighlight(start, end, selectionHP);
    }catch(javax.swing.text.BadLocationException ble){
      ble.printStackTrace(System.err);
    }
  }
}
