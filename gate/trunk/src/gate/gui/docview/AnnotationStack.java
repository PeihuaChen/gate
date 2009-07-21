/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 7 July 2009
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.Node;
import gate.FeatureMap;
import gate.annotation.NodeImpl;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import java.util.*;
import java.awt.*;

/**
 * Stack of annotations in a JPanel.
 * <br><br>
 * To use, respect this order:<br><code>
 * AnnotationStack stackPanel = new AnnotationStack(...);<br>
 * stackPanel.set...(...);<br>
 * stackPanel.clearAllRows();<br>
 * stackPanel.addRow(...);<br>
 * stackPanel.addAnnotation(...);<br>
 * stackPanel.drawStack();</code>
 */
public class AnnotationStack extends JPanel {

  public AnnotationStack() {
    super();
    init();
  }

  public AnnotationStack(int maxColumns, int maxValueLength) {
    super();
    this.maxColumns = maxColumns;
    this.maxValueLength = maxValueLength;
    init();
  }

  void init() {
    setLayout(new GridBagLayout());
    setOpaque(true);
    setBackground(Color.WHITE);
    stackRows = new ArrayList<StackRow>();
    textMouseListener = new StackMouseListener();
    headerMouseListener = new StackMouseListener();
    annotationMouseListener = new StackMouseListener();
  }

  /**
   * Add a row to the annotation stack.
   *
   * @param type annotation type
   * @param feature feature name or empty string if not to display
   * @param header text for the first column
   * @param lastColumnButton button at the end of the column; may be null
   * @param crop how to crop the text for the annotation if too long, one of
   *   {@link #CROP_START}, {@link #CROP_MIDDLE} or {@link #CROP_END}
   */
  public void addRow(String type, String feature, String header,
                     JButton lastColumnButton, int crop) {
    stackRows.add(new StackRow(type, feature, header, lastColumnButton, crop));
  }

  /**
   * Add an annotation to the current stack row.
   *
   * @param startOffset document offset where starts the annotation
   * @param endOffset document offset where ends the annotation
   * @param type annotation type
   * @param features annotation features map
   */
  public void addAnnotation(int startOffset, int endOffset,
                            String type, FeatureMap features) {
    stackRows.get(stackRows.size()-1).addAnnotation(
      StackAnnotation.createAnnotation(startOffset, endOffset, type, features));
  }

  /**
   * Add an annotation to the current stack row.
   *
   * @param annotation annotation to add to the current stack row
   */
  public void addAnnotation(gate.Annotation annotation) {
    stackRows.get(stackRows.size()-1).addAnnotation(
      StackAnnotation.createAnnotation(annotation));
  }

  /**
   * Clear all rows in the stack. To be called before adding the first row.
   */
  public void clearAllRows() {
    stackRows.clear();
  }

  /**
   * Draw the annotation stack in a JPanel with a GridBagLayout.
   */
  public void drawStack() {

    // clear the panel
    removeAll();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;

    /**********************
     * First row of text *
     *********************/

    gbc.gridwidth = 1;
    gbc.insets = new java.awt.Insets(10, 10, 10, 10);
    JLabel labelTitle = new JLabel("Context");
    labelTitle.setOpaque(true);
    labelTitle.setBackground(Color.WHITE);
    labelTitle.setBorder(new CompoundBorder(
      new EtchedBorder(EtchedBorder.LOWERED,
        new Color(250, 250, 250), new Color(250, 250, 250).darker()),
      new EmptyBorder(new Insets(0, 2, 0, 2))));
    labelTitle.setToolTipText("Expression and its context.");
    add(labelTitle, gbc);
    gbc.insets = new java.awt.Insets(10, 0, 10, 0);

    int startContextOffset = contextSize;
    int endContextOffset = contextSize + endOffset - startOffset;
    boolean textTooLong = (endOffset - startOffset) > maxColumns;
    int upperBound = text.length() - (maxColumns/2);

    // for each character
    for (int charNum = 0; charNum < text.length(); charNum++) {

      gbc.gridx = charNum + 1;
      if (textTooLong) {
        if (charNum == maxColumns/2) {
          // add ellipsis dots in case of a too long text displayed
          add(new JLabel("..."), gbc);
          // skip the middle part of the text if too long
          charNum = upperBound + 1;
          continue;
        } else if (charNum > upperBound) {
          gbc.gridx -= upperBound - (maxColumns/2) + 1;
        }
      }

      // set the text and color of the feature value
      JLabel label = new JLabel(text.substring(charNum, charNum+1));
      if (charNum >= startContextOffset && charNum < endContextOffset) {
        // this part is matched by the pattern, color it
        label.setBackground(new Color(240, 201, 184));
      } else {
        // this part is the context, no color
        label.setBackground(Color.WHITE);
      }
      label.setOpaque(true);

      // get the word from which belongs the current character charNum
      int start = text.lastIndexOf(" ", charNum);
      int end = text.indexOf(" ", charNum);
      String word = text.substring(
        (start == -1) ? 0 : start,
        (end == -1) ? text.length() : end);
      // add a mouse listener that modify the query
      label.addMouseListener(textMouseListener.createListener(word));
      add(label, gbc);
    }

      /************************************
       * Subsequent rows with annotations *
       ************************************/

    // for each row to display
    for (StackRow stackRow : stackRows) {
      String type = stackRow.getType();
      String feature = stackRow.getFeature();

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 1;
      gbc.insets = new Insets(0, 0, 3, 0);

      // add the header of the row
      JLabel annotationTypeAndFeature = new JLabel();
      String typeAndFeature = type + (feature.equals("") ? "" : ".") + feature;
      annotationTypeAndFeature.setText(stackRow.getHeader().equals("") ?
        typeAndFeature : stackRow.getHeader());
      annotationTypeAndFeature.setOpaque(true);
      annotationTypeAndFeature.setBackground(Color.WHITE);
      annotationTypeAndFeature.setBorder(new CompoundBorder(
        new EtchedBorder(EtchedBorder.LOWERED,
          new Color(250, 250, 250), new Color(250, 250, 250).darker()),
        new EmptyBorder(new Insets(0, 2, 0, 2))));
      if (feature.equals("")) {
        annotationTypeAndFeature.addMouseListener(
          headerMouseListener.createListener(type));
      } else {
        annotationTypeAndFeature.addMouseListener(
          headerMouseListener.createListener(type, feature));
      }
      gbc.insets = new java.awt.Insets(0, 10, 3, 10);
      add(annotationTypeAndFeature, gbc);
      gbc.insets = new java.awt.Insets(0, 0, 3, 0);

      // add all annotations for this row
      HashMap<Integer,TreeSet<Integer>> gridSet =
        new HashMap<Integer,TreeSet<Integer>>();
      int gridyMax = gbc.gridy;
      for(StackAnnotation ann : stackRow.getAnnotations()) {
        gbc.gridx = ann.getStartNode().getOffset().intValue()
                  - startOffset + contextSize + 1;
        gbc.gridwidth = ann.getEndNode().getOffset().intValue()
                      - ann.getStartNode().getOffset().intValue();
        if(textTooLong) {
          if(gbc.gridx > (upperBound + 1)) {
            // x starts after the hidden middle part
            gbc.gridx -= upperBound - (maxColumns / 2) + 1;
          }
          else if(gbc.gridx > (maxColumns / 2)) {
            // x starts in the hidden middle part
            if(gbc.gridx + gbc.gridwidth <= (upperBound + 3)) {
              // x ends in the hidden middle part
              continue; // skip the middle part of the text
            }
            else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - gbc.gridx + 2;
              gbc.gridx = (maxColumns / 2) + 2;
            }
          }
          else {
            // x starts before the hidden middle part
            if(gbc.gridx + gbc.gridwidth < (maxColumns / 2)) {
              // x ends before the hidden middle part
              // do nothing
            }
            else if(gbc.gridx + gbc.gridwidth < upperBound) {
              // x ends in the hidden middle part
              gbc.gridwidth = (maxColumns / 2) - gbc.gridx + 1;
            }
            else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - (maxColumns / 2);
            }
          }
        }
        if(gbc.gridwidth == 0) {
          gbc.gridwidth = 1;
        }

        JLabel label = new JLabel();
        String value = (String) ann.getFeatures().get(feature);
        if (value == null) { value = " "; }
        if(value.length() > maxValueLength) {
          // show the full text in the tooltip
          label.setToolTipText((value.length() > 500) ?
            "<html><textarea rows=\"30\" cols=\"40\" readonly=\"readonly\">"
            + value.replaceAll("(.{50,60})\\b", "$1\n") + "</textarea></html>" :
            ((value.length() > 100) ?
              "<html><table width=\"500\" border=\"0\" cellspacing=\"0\">"
                + "<tr><td>" + value.replaceAll("\n", "<br>")
                + "</td></tr></table></html>" :
              value));
          if(stackRow.getCrop() == CROP_START) {
            value = "..." + value.substring(value.length() - maxValueLength - 1);
          }
          else if(stackRow.getCrop() == CROP_END) {
            value = value.substring(0, maxValueLength - 2) + "...";
          }
          else {// cut in the middle
            value = value.substring(0, maxValueLength / 2) + "..."
              + value.substring(value.length() - (maxValueLength / 2));
          }
        }
        label.setText(value);
        label.setBackground(getAnnotationTypeColor(ann.getType()));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        label.setOpaque(true);
        if(feature.equals("")) {
          label.addMouseListener(annotationMouseListener
            .createListener(type, String.valueOf(ann.getId())));
          // show the feature values in the tooltip
          String width = (ann.getFeatures().toString().length() > 100) ?
            "500" : "100%";
          String toolTip = "<html><table width=\"" + width
            + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\">";
          Color color = (Color) UIManager.get("ToolTip.background");
          float[] hsb = Color.RGBtoHSB(
            color.getRed(), color.getGreen(), color.getBlue(), null);
          color = Color.getHSBColor(hsb[0], hsb[1],
            Math.max(0f, hsb[2] - hsb[2]*0.075f)); // darken the color
          String hexColor = Integer.toHexString(color.getRed()) +
            Integer.toHexString(color.getGreen()) +
            Integer.toHexString(color.getBlue());
          boolean odd = false; // alternate background color every other row
          for(Map.Entry<Object, Object> map : ann.getFeatures().entrySet()) {
            toolTip +="<tr align=\"left\""
              + (odd?" bgcolor=\"#"+hexColor+"\"":"")+"><td><strong>"
              + map.getKey() + "</strong></td><td>"
              + ((((String)map.getValue()).length() > 500) ?
              "<textarea rows=\"20\" cols=\"40\" cellspacing=\"0\">"
                + ((String)map.getValue()).replaceAll("(.{50,60})\\b", "$1\n")
                + "</textarea>" :
              ((String)map.getValue()).replaceAll("\n", "<br>"))
              + "</td></tr>";
            odd = !odd;
          }
          label.setToolTipText(toolTip + "</table></html>");

        } else {
          label.addMouseListener(annotationMouseListener.createListener(
            type, feature, (String) ann.getFeatures().get(feature),
            String.valueOf(ann.getId())));
        }
        // find the first empty row span for this annotation
        int oldGridy = gbc.gridy;
        for(int y = oldGridy; y <= (gridyMax + 1); y++) {
          // for each cell of this row where spans the annotation
          boolean xSpanIsEmpty = true;
          for(int x = gbc.gridx;
              (x < (gbc.gridx + gbc.gridwidth)) && xSpanIsEmpty; x++) {
            xSpanIsEmpty = !(gridSet.containsKey(x)
              && gridSet.get(x).contains(y));
          }
          if(xSpanIsEmpty) {
            gbc.gridy = y;
            break;
          }
        }
        // save the column x and row y of the current value
        TreeSet<Integer> ts;
        for(int x = gbc.gridx; x < (gbc.gridx + gbc.gridwidth); x++) {
          ts = gridSet.get(x);
          if(ts == null) {
            ts = new TreeSet<Integer>();
          }
          ts.add(gbc.gridy);
          gridSet.put(x, ts);
        }
        add(label, gbc);
        gridyMax = Math.max(gridyMax, gbc.gridy);
        gbc.gridy = oldGridy;
      }

      // add a button at the end of the row
      gbc.gridwidth = 1;
      if (stackRow.getLastColumnButton() != null) {
        // last cell of the row
        gbc.gridx = Math.min(text.length(), maxColumns) + 1;
        gbc.insets = new Insets(0, 10, 3, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        add(stackRow.getLastColumnButton(), gbc);
        gbc.insets = new Insets(0, 0, 3, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
      }

      // set the new gridy to the maximum row we put a value
      gbc.gridy = gridyMax;
    }

    if (lastRowButton != null) {
      // add a configuration button on the last row
      gbc.insets = new java.awt.Insets(0, 10, 0, 10);
      gbc.gridx = 0;
      gbc.gridy++;
      add(lastRowButton, gbc);
    }

    // add an empty cell that takes all remaining space to
    // align the visible cells at the top-left corner
    gbc.gridy++;
    gbc.gridx = maxColumns+1;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    add(new JLabel(""), gbc);

    validate();
    updateUI();
  }

  /**
   * This method uses the java.util.prefs.Preferences and get the color
   * for particular annotationType.. This color could have been saved by
   * the AnnotationSetsView
   *
   * @param annotationType name of the annotation type
   * @return the color saved in the GATE preferences
   */
  Color getAnnotationTypeColor(String annotationType) {
    java.util.prefs.Preferences prefRoot = null;
    try {
      prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class
              .forName("gate.gui.docview.AnnotationSetsView"));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    int rgba = (annotationType != null)?prefRoot.getInt(annotationType, -1):-1;
    Color colour;
    if(rgba == -1) {
      // initialise and save
      gate.swing.ColorGenerator colorGenerator = new gate.swing.ColorGenerator();
      float components[] = colorGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0], components[1], components[2], 0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      if (annotationType != null) {
        prefRoot.putInt(annotationType, rgba);
      }
    }
    else {
      colour = new Color(rgba, true);
    }
    return colour;
  }

  /**
   * Extension of a MouseInputAdapter that adds a method
   * to create new Listeners from it.<br>
   * You must overriden the createListener method.
   * <br>
   * There is 3 cases for the parameters of createListener:
   * <ul>
   * <li>first line of text -> createListener(word)
   * <li>first column (header) -> createListener(type),
   *   createListener(type, feature)
   * <li>annotation -> createListener(type, annotationId),
   *   createListener(type, feature, value, annotationId)
   * </ul>
   */
  public static class StackMouseListener extends MouseInputAdapter {
    public MouseInputAdapter createListener(String... parameters) {
      return null;
    }
  }

  /**
   * Annotation that doesn't belong to an annotation set
   * and with id always equal to -1.<br>
   * Allows to create an annotation without document, nodes, annotation set,
   * and keep compatibility with gate.Annotation.
   * <br>
   * This class is only for AnnotationStack internal use
   * as it won't work with most of the methods that use gate.Annotation.
   */
  static class StackAnnotation extends gate.annotation.AnnotationImpl {
    StackAnnotation(Integer id, Node start, Node end, String type,
                         FeatureMap features) {
      super(id, start, end, type, features);
    }
    static StackAnnotation createAnnotation(int startOffset,
                  int endOffset, String type, FeatureMap features) {
      Node startNode = new NodeImpl(-1, (long) startOffset);
      Node endNode = new NodeImpl(-1, (long) endOffset);
      return new StackAnnotation(-1, startNode, endNode, type, features);
    }
    static StackAnnotation createAnnotation(gate.Annotation annotation) {
      return new StackAnnotation(annotation.getId(), annotation.getStartNode(),
        annotation.getEndNode(), annotation.getType(), annotation.getFeatures());
    }
  }

  /**
   * A row of annotations in the stack.
   */
  class StackRow {
    StackRow(String type, String feature, String header,
             JButton lastColumnButton, int crop) {
      this.type = type;
      this.feature = feature;
      this.annotations = new HashSet<StackAnnotation>();
      this.header = header;
      this.lastColumnButton = lastColumnButton;
      this.crop = crop;
    }

    public String getType() {
      return type;
    }
    public String getFeature() {
      return feature;
    }
    public Set<StackAnnotation> getAnnotations() {
      return annotations;
    }
    public String getHeader() {
      return header;
    }
    public JButton getLastColumnButton() {
      return lastColumnButton;
    }
    public int getCrop() {
      return crop;
    }
    public void addAnnotation(StackAnnotation annotation) {
      annotations.add(annotation);
    }

    String type;
    String feature;
    Set<StackAnnotation> annotations;
    String header;
    JButton lastColumnButton;
    int crop;
  }

  public void setLastRowButton(JButton lastRowButton) {
    this.lastRowButton = lastRowButton;
  }

  /** @param text first line of text that contains the main expression
   *  and its context */
  public void setText(String text) {
    this.text = text;
  }

  /** @param startOffset document offset where starts the main expression */
  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }

  /** @param endOffset document offset where ends the main expression */
  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }

  /** @param contextSize size of the context to display in characters */
  public void setContextSize(int contextSize) {
    this.contextSize = contextSize;
  }

  /** @param expressionTooltip optional tooltip for the main expression */
  public void setExpressionTooltip(String expressionTooltip) {
    this.expressionTooltip = expressionTooltip;
  }

  /** @param textMouseListener optional listener for the first line of text */
  public void setTextMouseListener(StackMouseListener textMouseListener) {
    this.textMouseListener = textMouseListener;
  }

  /** @param headerMouseListener optional listener for the first column */
  public void setHeaderMouseListener(StackMouseListener headerMouseListener) {
    this.headerMouseListener = headerMouseListener;
  }

  /** @param annotationMouseListener optional listener for the annotations */
  public void setAnnotationMouseListener(StackMouseListener annotationMouseListener) {
    this.annotationMouseListener = annotationMouseListener;
  }

  /** rows of annotations that are displayed in the stack*/
  ArrayList<StackRow> stackRows;
  /** maximum number of columns to display in the match,
   *  i.e. maximum number of characters */
  int maxColumns = 150;
  /** maximum length of a feature value displayed */
  int maxValueLength = 30;
  JButton lastRowButton;
  String text = "";
  int startOffset = 0;
  int endOffset = 0;
  int contextSize = 0;
  String expressionTooltip = "";
  StackMouseListener textMouseListener;
  StackMouseListener headerMouseListener;
  StackMouseListener annotationMouseListener;
  public final static int CROP_START = 0;
  public final static int CROP_MIDDLE = 1;
  public final static int CROP_END = 2;
}
