/*
 *  SyntaxTreeViewer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 20/09/2000
 *
 *  $Id$
 */

package gate.gui;

//java imports
import java.util.*;
import java.beans.*;
import java.net.URL;


//AWT imports - layouts and events
import java.awt.*;
import java.awt.event.*;

//SWING imports
import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;

//GATE imports
import gate.util.*;
import gate.*;
import gate.creole.*;


/**
  * The SyntaxTreeViewer is capable of showing and editing utterances (fancy
  * name for sentences) and the
  * attached syntax trees. It works by taking an utterance and all Token
  * annotations and constructs the text. Then it also gets all SyntaxTreeNode
  * annotations and builds and shows the syntac tree for that utterance. The
  * leaves of the tree are the tokens, which constitute the utterance.<P>
  *
  * It is possible to configure the annotation types that are used by the
  * viewer. The textAnnotationType property specifies the type
  * of annotation which is used to denote the utterance (sentence).
  * In GATE, the value of this property is not set directly, but is derived
  * from the VR configuration information from creole.xml (explained below).
  *
  * The treeNodeAnnotationType is the name of the
  * annotations which encode the SyntaxTreeNodes; default - SyntaxTreeNode.
  * To change when part of GATE, modify the <PARAMETER> setting of the
  * TreeViewer entry in creole.xml. Similarly, one can change which annotation
  * is used for chunking the utterance. By default, it is Token, which is also
  * specified in creole.xml as a parameter in the treeviewer entry.
  *
  * The component assumes that the annotations of type treeNodeAnnotationType have
  * features called: cat with a value String; consists which is a List either
  * empty or with annotation ids of the node's children; and optionally
  * text which contains
  * the text covered by this annotation. The component will work fine even
  * without the last feature. Still when it creates annotations,
  * these will have this feature added. <P>
  *
  *
  * Newly added tree nodes to the tree are added to the document
  * as annotations and deleted nodes are automatically deleted from the document
  * only after OK is chosen in the dialog. Cancel does not make any changes
  * permanent. <P>
  *
  * Configuring the viewer in GATE<P>
  * The viewer is configured in creole.xml. The default entry is:
  * <PRE>
  *   <RESOURCE>
  *     <NAME>Syntax tree viewer</NAME>
  *     <CLASS>gate.gui.SyntaxTreeViewer</CLASS>
  *     <!-- type values can be  "large" or "small"-->
  *     <GUI>
  *       <MAIN_VIEWER/>
  *       <ANNOTATION_TYPE_DISPLAYED>Sentence</ANNOTATION_TYPE_DISPLAYED>
  *       <PARAMETER NAME="treeNodeAnnotationType" DEFAULT="SyntaxTreeNode"
  *                  RUNTIME="false" OPTIONAL="true">java.lang.String
  *       </PARAMETER>
  *       <PARAMETER NAME="tokenType" DEFAULT="Token" RUNTIME="false"
  *                  OPTIONAL="true">java.lang.String
  *       </PARAMETER>
  *     </GUI>
  *   </RESOURCE>
  * </PRE>
  *
  * The categories that appear in the menu for manual annotation are determined
  * from SyntaxTreeViewerSchema.xml. If you want to change the default set,
  * you must edit this file and update your Gate jar accordingly (e.g., by
  * recompilation. This does not affect the categories of SyntaxTreeNode
  * annotations, which have been created automatically by some other process,
  * e.g., a parser PR.
  *
  * <P>
  * If used outside GATE,
  * in order to have appropriate behaviour always put this component inside a
  * scroll pane or something similar that provides scrollers.
  * Example code: <BREAK>
  * <PRE>
  *  JScrollPane scroller = new JScrollPane(syntaxTreeViewer1);
  *  scroller.setPreferredSize(syntaxTreeViewer1.getPreferredSize());
  *  frame.getContentPane().add(scroller, BorderLayout.CENTER);
  * </PRE>
  *
  *
  * The default way is to pass just one annotation of type textAnnotationType
  * which corresponds to the entire sentence or utterance to be annotated with
  * syntax tree information. Then the viewer automatically tokenises it
  * (by obtaining the relevant token annotations) and creates the leaves.<P>
  *
  * To create a new annotation, use setSpan, instead of setAnnotation.
  *
  * <P> In either
  * case, you must call setTarget first, because that'll provide the viewer
  * with the document's annotation set, from where it can obtain the token
  * annotations.
  * <P> If you intend to use the viewer outside GATE and do not understand
  * the API, e-mail gate@dcs.shef.ac.uk.
  */

public class SyntaxTreeViewer extends AbstractVisualResource
    implements  Scrollable, ActionListener, MouseListener,
                AnnotationVisualResource {

  // class members
  // whether to use any layout or not
  protected boolean laidOut = false;

  // display all buttons x pixels apart horizontally
  protected int horizButtonGap = 5;

  // display buttons at diff layers x pixels apart vertically
  protected int vertButtonGap = 50;

  // extra width in pixels to be added to each button
  protected int extraButtonWidth = 10;

  // number of pixels to be used as increment by scroller
  protected int maxUnitIncrement = 10;

  // GUI members
  BorderLayout borderLayout1 = new BorderLayout();
  JPopupMenu popup = new JPopupMenu(); //the right-click popup
  Color buttonBackground;
  Color selectedNodeColor = Color.red.darker();

  // the HashSet with the coordinates of the lines to draw
  HashSet lines = new HashSet();

  // The utterance to be annotated as a sentence. It's not used if the tree
  // is passed
  // as annotations.
  protected Annotation utterance;
  protected Long utteranceStartOffset = new Long(0);
  protected Long utteranceEndOffset = new Long(0);
  protected AnnotationSet currentSet = null;

  protected String tokenType = "Token";

  // for internal use only. Set when the utterance is set.
  protected String displayedString = "";

  // The name of the annotation type which is used to locate the
  // stereotype with the allowed categories
  // also when reading and creating annotations
  protected String treeNodeAnnotationType = "SyntaxTreeNode";

  // The annotation name of the annotations used to extract the
  // text that appears at the leaves of the tree. For now the viewer
  // supports only one such annotation but might be an idea to extend it
  // so that it gets its text off many token annotations, which do not
  // need to be tokenised or off the syntax tree annotations themselves.
  protected String textAnnotationType = "utterance";

  // all leaf nodes
  protected HashMap leaves = new HashMap();

  // all non-terminal nodes
  protected HashMap nonTerminals = new HashMap();

  // all buttons corresponding to any node
  protected HashMap buttons = new HashMap();

  // all selected buttons
  protected Vector selection = new Vector();

  // all annotations to be displayed
  protected AnnotationSet treeAnnotations;

  protected Document document = null;
  // the document to which the annotations belong

  //true when a new utterance annotation has been added
  //then if the user presses cancel, I need to delete it
  protected boolean utteranceAdded = false;


  public SyntaxTreeViewer() {
    try  {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }

  }

  //CONSTRUCTORS
  private SyntaxTreeViewer(String annotType) {

    treeNodeAnnotationType = annotType;
    try  {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  //METHODS
  private void jbInit() throws Exception {

    //check if we're using a layout; preferrably not
    if (laidOut)
      this.setLayout(borderLayout1);
    else
      this.setLayout(null);

    this.setPreferredSize(new Dimension (600, 400));
    this.setSize(600, 400);
    this.setBounds(0, 0, 600, 400);
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentShown(ComponentEvent e) {
        this_componentShown(e);
      }
      public void componentHidden(ComponentEvent e) {
        this_componentHidden(e);
      }
    });
    this.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent e) {
        this_propertyChange(e);
      }
    });

    buttonBackground = Color.red; //this.getBackground();

    //get all categories from stereotype
    fillCategoriesMenu();

    //initialise the popup menu

    //add popup to container
    this.add(popup);
  }// private void jbInit()

  // Methods required by AnnotationVisualResource

  /**
    * Called by the GUI when this viewer/editor has to initialise itself for a
    * specific annotation or text span.
    * @param target the object which will always be a {@link gate.AnnotationSet}
    */

  public void setTarget(Object target) {
    if (target == null) return;
    currentSet = (AnnotationSet) target;
    document = currentSet.getDocument();
  }

  /**
    * Used when the viewer/editor has to display/edit an existing annotation
    * @param ann the annotation to be displayed or edited. If ann is null then
    * the method simply returns
    */
  public void setAnnotation(Annotation ann){
    if (ann == null) return;

    utterance = ann;
    utteranceStartOffset = utterance.getStartNode().getOffset();
    utteranceEndOffset = utterance.getEndNode().getOffset();
    textAnnotationType = ann.getType();

    clearAll();
    utterances2Trees();
    annotations2Trees();
    this.setVisible(true);
    repaint();
  }

  /**
    * Used when the viewer has to create new annotations.
    * @param startOffset the start offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    * @param endOffset the end offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    */
  public void setSpan(Long startOffset, Long endOffset, String annotType){
    // If one of them is null, then simply return.
    if (startOffset == null || endOffset == null) return;
    if (document == null) return;

    try {
      Integer newId = currentSet.add( startOffset, endOffset, annotType,
                                Factory.newFeatureMap());
      utterance = currentSet.get(newId);
      utteranceAdded = true;
      textAnnotationType = annotType;
      setAnnotation(utterance);

    } catch (InvalidOffsetException ioe) {
      ioe.printStackTrace();
    }

  }

  /**
   * Called by the GUI when the user has pressed the "OK" button. This should
   * trigger the saving of the newly created annotation(s)
   */
  public void okAction() throws GateException{
    //Out.println("Visible coords" + this.getVisibleRect().toString());
    //Out.println("Size" + this.getBounds().toString());
    STreeNode.transferAnnotations(document, currentSet);

  } //okAction()

  /**
   * Called by the GUI when the user has pressed the "Cancel" button. This should
   * trigger the cleanup operation
   */
  public void cancelAction() throws GateException{
    //if we added a new utterance but user does not want it any more...
    if (utteranceAdded) {
      currentSet.remove(utterance); //delete it
      utteranceAdded = false;
    }
    //also cleanup the temporary annotation sets used by the viewer
    //to cache the added and deleted tree annotations
    STreeNode.undo(document);

  } //okAction()


  /**
    * Checks whether this viewer/editor can handle a specific annotation type.
    * @param annotationType represents the annotation type being questioned.If
    * it is <b>null</b> then the method will return false.
    * @return true if the SchemaAnnotationEditor can handle the annotationType
    * or false otherwise.
    */
  public boolean canDisplayAnnotationType(String annotationType){
    // Returns true only if the there is an AnnotationSchema with the same type
    // as annotationType.
    if (annotationType == null) return false;
    boolean found = false;

    java.util.List specificEditors = Gate.getCreoleRegister().
                                     getAnnotationVRs(annotationType);
    Iterator editorIter = specificEditors.iterator();
    while(editorIter.hasNext() && !found){
      String editorClass = (String)editorIter.next();

      Out.println(editorClass);
      if (editorClass.indexOf(this.getClass().getName()) > -1) {
        textAnnotationType = annotationType;
        found = true;
      }
    }

    return found;
  }// canDisplayAnnotationType();


/*  public static void main(String[] args) throws Exception {
    Gate.init();
    // final String text = "This is a sentence. That is another one.";
    final String text = "\u0915\u0932\u094d\u0907\u0928\u0643\u0637\u0628 \u041a\u0430\u043b\u0438\u043d\u0430 Kalina";
    final Document doc = Factory.newDocument(text);

    // that works too but only use if you have the test file there.
    // final Document doc = Factory.newDocument(
    //                        new URL("file:///z:/temp/weird.txt"), "UTF-8");


    final SyntaxTreeViewer syntaxTreeViewer1 =
      new SyntaxTreeViewer("SyntaxTreeNode");
    //syntaxTreeViewer1.setUnicodeSupportEnabled(true);
    //need to set the document here!!!!


    JFrame frame = new JFrame();

    //INITIALISE THE FRAME, ETC.
    frame.setEnabled(true);
    frame.setTitle("SyntaxTree Viewer");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    // frame.getContentPane().add(syntaxTreeViewer1, BorderLayout.CENTER);
    // intercept the closing event to shut the application
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        AnnotationSet hs = doc.getAnnotations().get("SyntaxTreeNode");
        if (hs != null && hs.size() > 0) {
          int k = 0;
          for (Iterator i = hs.iterator(); i.hasNext(); k++) {
            Out.println("Tree Annot " + k + ": ");
            Out.println(i.next().toString());
          }
        } //if
        Out.println("Exiting...");
        //System.exit(0);
      }
    });

    //Put the bean in a scroll pane.
    JScrollPane scroller = new JScrollPane(syntaxTreeViewer1);
    scroller.setPreferredSize(syntaxTreeViewer1.getPreferredSize());
    frame.getContentPane().add(scroller, BorderLayout.CENTER);

    //DISPLAY FRAME
    frame.pack();
    frame.show();

    FeatureMap attrs = Factory.newFeatureMap();
    attrs.put("time", new Long(0));
    attrs.put("text", doc.getContent().toString());
*/
    /*
    FeatureMap attrs1 = Factory.newFeatureMap();
    attrs1.put("cat", "N");
    attrs1.put("text", "This");
    attrs1.put("consists", new Vector());

    FeatureMap attrs2 = Factory.newFeatureMap();
    attrs2.put("cat", "V");
    attrs2.put("text", "is");
    attrs2.put("consists", new Vector());
    */

/*
    doc.getAnnotations().add( new Long(0), new Long(
                      doc.getContent().toString().length()),"utterance", attrs);
*/
    /* Integer id1 = doc.getAnnotations().add(new Long(0), new Long(4),
                              "SyntaxTreeNode", attrs1);
    Integer id2 = doc.getAnnotations().add(new Long(5), new Long(7),
                              "SyntaxTreeNode", attrs2);

    FeatureMap attrs3 = Factory.newFeatureMap();
    attrs3.put("cat", "VP");
    attrs3.put("text", "This is");
    Vector consists = new Vector();
    consists.add(id1);
    consists.add(id2);
    attrs3.put("consists", consists);
    doc.getAnnotations().add(new Long(0), new Long(7),
                                                  "SyntaxTreeNode", attrs3);
    */

/*
    HashSet set = new HashSet();
    set.add("utterance");
    set.add("SyntaxTreeNode");
    AnnotationSet annots = doc.getAnnotations().get(set);
    syntaxTreeViewer1.setTreeAnnotations(annots);

  }// public static void main
*/

  protected void paintComponent(Graphics g) {
    super.paintComponent( g);
    drawLines(g);
  }// protected void paintComponent(Graphics g)


  private void drawLines(Graphics g) {

    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      Coordinates coords = (Coordinates) i.next();

      g.drawLine( coords.getX1(),
                  coords.getY1(),
                  coords.getX2(),
                  coords.getY2());
    }// for
  }// private void drawLines(Graphics g)

  public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
  }// public Dimension getPreferredScrollableViewportSize()

  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
    return maxUnitIncrement;
  }// public int getScrollableUnitIncrement

  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width - maxUnitIncrement;
    else
        return visibleRect.height - maxUnitIncrement;
  }// public int getScrollableBlockIncrement

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }// public boolean getScrollableTracksViewportWidth()

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  void this_propertyChange(PropertyChangeEvent e) {

    //we have a new utterance to display and annotate
    if (e.getPropertyName().equals("utterance")) {
      clearAll();
      utterances2Trees();
    }

  } //this_propertyChange

  /**
    * Clear all buttons and tree nodes created because component is being
    * re-initialised. Not sure it works perfectly.
    */
  private void clearAll() {
    lines.clear();
    this.removeAll();
    buttons.clear();
    leaves.clear();
    nonTerminals.clear();
  }

  /**
    * Converts the annotations into treeNodes
    */
  private void annotations2Trees() {
    if (document == null) return;

    HashMap processed = new HashMap(); //for all processed annotations

    //first get all tree nodes in this set, then restrict them by offset
    AnnotationSet tempSet = currentSet.get(treeNodeAnnotationType);
    if (tempSet == null || tempSet.isEmpty())
      return;
    treeAnnotations = tempSet.get(utterance.getStartNode().getOffset(),
                                  utterance.getEndNode().getOffset());
    if (treeAnnotations == null || treeAnnotations.isEmpty())
      return;

    // sort them from left to right first
    // Should work as
    // annotation implements Comparable
    LinkedList nodeAnnots = new LinkedList(treeAnnotations);
    Collections.sort(nodeAnnots);

    Vector childrenButtons = new Vector();
    String oldParent = "";

    //find all annotations with no children
    Iterator i = nodeAnnots.iterator();
    while (i.hasNext()) {
      Annotation annot = (Annotation) i.next();

      java.util.List children = (java.util.List) annot.getFeatures().get("consists");
      //check if it's a leaf
      if (children == null ||
          children.isEmpty())
        {

        STreeNode leaf = findLeaf(annot.getStartNode(), annot.getEndNode());
        if (leaf == null) {//not found
          Out.println("Can't find my leaf node for annotation: " + annot);
        }

        JButton button = (JButton) buttons.get(new Integer(leaf.getID()));
        selection.clear();
        selection.add(button);

        //then create the non-terminal with the category
        STreeNode node = new STreeNode(annot);
        node.add(leaf);
        node.setLevel(1);
        node.setUserObject(annot.getFeatures().get("cat"));
        nonTerminals.put(new Integer(node.getID()), node);
        JButton parentButton = createCentralButton(node);
        addLines(node);

        //finally add to the processed annotations
        processed.put(annot.getId(), parentButton);

      } //if

    } //loop through children

    //loop through the rest of the nodes
    Iterator i1 = nodeAnnots.iterator();
    while (i1.hasNext()) {
      Annotation annotNode = (Annotation) i1.next();
      if (processed.containsKey(annotNode.getId()))
        continue;
      processChildrenAnnots(annotNode, processed);
    } //process all higher nodes

    selection.clear();

    this.scrollRectToVisible(new
      Rectangle(0, (int) getHeight()- (int) getVisibleRect().getHeight(),
        (int) getVisibleRect().getWidth(), (int) getVisibleRect().getHeight()));
  } //annotations2Trees

  private JButton processChildrenAnnots(Annotation annot, HashMap processed) {
    selection.clear();
    Vector childrenButtons = new Vector();
    java.util.List children = (java.util.List) annot.getFeatures().get("consists");

    for (Iterator i = children.iterator(); i.hasNext(); ) {
      Integer childId = (Integer) i.next();
      Annotation child = treeAnnotations.get(childId);
      JButton childButton;

      if (processed.containsKey(child.getId()))
        childButton = (JButton) processed.get(child.getId());
      else
        childButton = processChildrenAnnots(child, processed);

      childrenButtons.add(childButton);
    }

    selection = (Vector) childrenButtons.clone();
    STreeNode parent = createParentNode(
                          (String) annot.getFeatures().get("cat"),
                          annot);
    nonTerminals.put(new Integer(parent.getID()), parent);
    JButton parentButton = createCentralButton(parent);
    addLines(parent);

    processed.put(annot.getId(), parentButton);
    selection.clear();
    return parentButton;
  }// private JButton processChildrenAnnots

  private STreeNode findLeaf(Node start, Node end) {
    for (Iterator i = leaves.values().iterator(); i.hasNext(); ) {
      STreeNode node = (STreeNode) i.next();
      if (node.getStart() == start.getOffset().intValue() &&
          node.getEnd() == end.getOffset().intValue()
         )
        return node;
    }

    return null;
  }//private STreeNode findLeaf(Node start, Node end)


  /**
    * Converts the given utterances into a set of leaf nodes for annotation
    */
  private void utterances2Trees() {

    if (! utterance.getType().equals(textAnnotationType)) {
      Out.println("Can't display annotations other than the specified type:" +
                                                            textAnnotationType);
      return;
    }

    // set the utterance offset correctly.
    // All substring calculations depend on that.
    utteranceStartOffset = utterance.getStartNode().getOffset();
    utteranceEndOffset = utterance.getEndNode().getOffset();

    try {
      displayedString = currentSet.getDocument().getContent().getContent(
                        utteranceStartOffset, utteranceEndOffset).toString();
    } catch (InvalidOffsetException ioe) {
      ioe.printStackTrace();
    }

    AnnotationSet allTokens = currentSet.get(utteranceStartOffset,
                                          utteranceEndOffset);
    if (allTokens == null || allTokens.isEmpty()) {
      Out.println("TreeViewer warning: No annotations of type " + tokenType +
                  "so cannot show or edit the text and the tree annotations.");
      return;
    }

    AnnotationSet tokens = allTokens.get(tokenType);

    //if no tokens return
    //needs improving maybe, just show one solid utterance
    if (tokens == null || tokens.isEmpty()){
      Out.println("TreeViewer warning: No annotations of type " + tokenType +
                  "so cannot show or edit the text and the tree annotations.");
      return;
    }

    Insets insets = this.getInsets();
    // the starting X position for the buttons
    int buttonX = insets.left;

    // the starting Y position
    int buttonY = this.getHeight() - 20 - insets.bottom;

    //We need to go through the nodes this way and get the f***ing tokens
    //coz there is no way to sort them by startOffset. The compareTo method
    //only uses the Ids, highly useful!
    Node startNode = tokens.firstNode();
    Node endNode = tokens.nextNode(startNode);

    //loop through the tokens
    while (startNode != null && endNode != null) {
      AnnotationSet nextTokenSet = tokens.get(startNode.getOffset());

      if (nextTokenSet == null || nextTokenSet.isEmpty())
        break;

      Annotation tokenAnnot = (Annotation) nextTokenSet.iterator().next();
      Long tokenBegin = tokenAnnot.getStartNode().getOffset();
      Long tokenEnd = tokenAnnot.getEndNode().getOffset();

      String tokenText = "";
      try {
        tokenText = document.getContent().getContent(
                        tokenBegin, tokenEnd).toString();
      } catch (InvalidOffsetException ioe) {
        ioe.printStackTrace();
      }

      // create the leaf node
      STreeNode node =
        new STreeNode(tokenBegin.longValue(), tokenEnd.longValue());

      // make it a leaf
      node.setAllowsChildren(false);

      // set the text
      node.setUserObject(tokenText);
      node.setLevel(0);

      // add to hash table of leaves
      leaves.put(new Integer(node.getID()), node);

      // create the corresponding button
      buttonX = createButton4Node(node, buttonX, buttonY);

      //advance to the next node
      startNode = tokenAnnot.getEndNode();
      endNode = tokens.nextNode(startNode);
    } //while


/*
    //This old piece of code was used to tokenise, instead of relying on
    // annotations. Can re-instate if someone shows me the need for it.

    long currentOffset = utteranceStartOffset.longValue();

    StrTokeniser strTok =
        new StrTokeniser(displayedString,
                        " \r\n\t");

    Insets insets = this.getInsets();
    // the starting X position for the buttons
    int buttonX = insets.left;

    // the starting Y position
    int buttonY = this.getHeight() - 20 - insets.bottom;

    while (strTok.hasMoreTokens()) {
      String word = strTok.nextToken();
//      Out.println("To display:" + word);

      // create the leaf node
      STreeNode node =
        new STreeNode(currentOffset, currentOffset + word.length());

      // make it a leaf
      node.setAllowsChildren(false);

      // set the text
      node.setUserObject(word);
      node.setLevel(0);

      // add to hash table of leaves
      leaves.put(new Integer(node.getID()), node);

      // create the corresponding button
      buttonX = createButton4Node(node, buttonX, buttonY);

      currentOffset += word.length()+1;  //// +1 to include the delimiter too
    }
*/

    this.setSize(buttonX, buttonY + 20 + insets.bottom);
    // this.resize(buttonX, buttonY + 20 + insets.bottom);
    this.setPreferredSize(this.getSize());

  } // utterance2Trees

  /**
    * Returns the X position where another button can start if necessary.
    * To be used to layout only the leaf buttons. All others must be created
    * central to their children using createCentralButton.
    */
  private int createButton4Node(STreeNode node, int buttonX, int buttonY) {

    JButton button = new JButton((String) node.getUserObject());
    button.setBorderPainted(false);

    FontMetrics fm = button.getFontMetrics(button.getFont());

    int buttonWidth,
        buttonHeight;

    // Out.print
    //  ("Button width " + b1.getWidth() + "Button height " + b1.getHeight());

    buttonWidth = fm.stringWidth(button.getText())
                  + button.getMargin().left + button.getMargin().right
                  + extraButtonWidth;
    buttonHeight = fm.getHeight() + button.getMargin().top +
                      button.getMargin().bottom;
    buttonY = buttonY - buttonHeight;

//     Out.print("New Button X " + buttonX +
//        "New Button Y" + buttonY);

    button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    button.addActionListener(this);
    button.addMouseListener(this);
    button.setActionCommand("" + node.getID());
    button.setVisible(true);
    button.enable();

    this.add(button);
    buttons.put(new Integer(node.getID()), button);

    buttonX += buttonWidth + horizButtonGap;
    return buttonX;

  }// private int createButton4Node(STreeNode node, int buttonX, int buttonY)

  private JButton createCentralButton(STreeNode newNode) {

    FocusButton button = new FocusButton((String) newNode.getUserObject());
    button.setBorderPainted(false);

    FontMetrics fm = button.getFontMetrics(button.getFont());

    int buttonWidth,
        buttonHeight,
        buttonX = 0,
        buttonY =0;

    // Out.print("Button width " + b1.getWidth() + ";
    //    Button height " + b1.getHeight());

    buttonWidth = fm.stringWidth(button.getText())
                  + button.getMargin().left + button.getMargin().right
                  + extraButtonWidth;
    buttonHeight = fm.getHeight() + button.getMargin().top +
                      button.getMargin().bottom;

    int left = this.getWidth(), right =0 , top = this.getHeight();

    // determine the left, right, top
    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      JButton childButton = (JButton) i.next();

      if (left > childButton.getX())
        left = childButton.getX();
      if (childButton.getX() + childButton.getWidth() > right)
        right = childButton.getX() + childButton.getWidth();
      if (childButton.getY() < top)
        top = childButton.getY();
    }

    buttonX = (left + right) /2 - buttonWidth/2;
    buttonY = top - vertButtonGap;
    // Out.println("Button's Y is" + buttonY);

    // Out.print("New Button width " + buttonWidth + ";
    //    New Button height " + buttonHeight);
    button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    button.addActionListener(this);
    button.addMouseListener(this);
    // button.registerKeyboardAction(this,
    //													"delete",
    //                           KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
    //                           WHEN_FOCUSED);

    button.setActionCommand("" + newNode.getID());

    this.add(button);
    // add to hashmap of buttons
    buttons.put(new Integer(newNode.getID()), button);

    // check if we need to resize the panel
    if (buttonY < 0) {
      this.setSize(this.getWidth(), this.getHeight() + 5* (- buttonY));
      this.setPreferredSize(this.getSize());
      shiftButtonsDown(5* (-buttonY));
    }

    return button;
  }// private JButton createCentralButton(STreeNode newNode)

  private void shiftButtonsDown(int offset) {
    for (Iterator i = buttons.values().iterator(); i.hasNext(); ) {
      JButton button = (JButton) i.next();
      button.setBounds(		button.getX(),
                          button.getY() + offset,
                          button.getWidth(),
                          button.getHeight());
    } // for loop through buttons

    for (Iterator k = lines.iterator(); k.hasNext(); ) {
      Coordinates coords = (Coordinates) k.next();
      coords.setY1(coords.getY1() + offset);
      coords.setY2(coords.getY2() + offset);
    }
  }// private void shiftButtonsDown(int offset)

  public void actionPerformed(ActionEvent e) {

    //check for the popup menu items
    if (e.getSource() instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) e.getSource();

      // check if we're annotating a leaf
      // the popup label is set to leaves when the popup has been
      // constructed in showRightClickPopup
      if (popup.getLabel().equals("leaves")) {
        Integer id = new Integer(e.getActionCommand());

        clearSelection();
        JButton button = (JButton) buttons.get(id);
        selection.add(button);

        STreeNode leaf = (STreeNode) leaves.get(id);

        // create parent with the same span as leaf
        // using createParentNode here is not a good idea coz it works only
        // for creating parents of non-terminal nodes, not leaves
        STreeNode parent = new STreeNode(leaf.getStart(), leaf.getEnd());
        parent.setLevel(leaf.getLevel()+1); //levels increase from bottom to top
        parent.add(leaf);

        // set the text
        parent.setUserObject(menuItem.getText());

        // last create the annotation; should always come last!
        parent.createAnnotation(  document,
                                  treeNodeAnnotationType,
                                  displayedString,
                                  utteranceStartOffset.longValue());
        nonTerminals.put(new Integer(parent.getID()), parent);

        // create new button positioned centrally above the leaf
        createCentralButton(parent);

        // add the necessary lines for drawing
        addLines(parent);

        selection.clear();

        // repaint the picture!
        this.repaint();
      } // finished processing leaves
      else if (popup.getLabel().equals("non-terminal")) {
        // the action command is set to the id under which
        // the button can be found
        Integer id = new Integer(e.getActionCommand());

        //locate button from buttons hashMap and add to selection
        JButton button = (JButton) buttons.get(id);
        selection.add(button);

        //create the new parent
        STreeNode parent = createParentNode(menuItem.getText());

        //add to nonTerminals HashMap
        nonTerminals.put(new Integer(parent.getID()), parent);

        //create new button positioned centrally above the leaf
        createCentralButton(parent);

        //add the necessary lines for drawing
        addLines(parent);

        clearSelection();

        //repaint the picture!
        this.repaint();

      } //check for non-terminals

    } //if statement for MenuItems


  }// public void actionPerformed(ActionEvent e)

  public void mouseClicked(MouseEvent e) {

    if (! (e.getSource() instanceof JButton))
      return;

    JButton source = (JButton) e.getSource();

    //check if CTRL or Shift is pressed and if not, clear the selection
    if ((! (e.isControlDown() || e.isShiftDown()))
         && SwingUtilities.isLeftMouseButton(e))
      clearSelection();

    //and select the current node
    if (SwingUtilities.isLeftMouseButton(e))
    //if (e.getModifiers() == e.BUTTON1_MASK)
      selectNode(e);


    //only repspond to right-clicks here by displaying the popup
    if (SwingUtilities.isRightMouseButton(e)) {
      //if button not in focus, grad the focus and select it!
      if ( source.getBackground() != selectedNodeColor ) {
        source.grabFocus();
        source.doClick();
        selectNode(e);
      }
      //Out.println(e.getComponent().getClass() + " right-clicked!");
      showRightClickPopup(e);
    } //end of right-click processing

  }// public void mouseClicked(MouseEvent e)

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  } // createButton4Node


  private void showRightClickPopup(MouseEvent e) {

    //that'll always work coz we checked it in MouseClicked.
    JButton source = (JButton) e.getSource();
    Integer id = new Integer(source.getActionCommand());

    //check if it's a leaf and if so, offer the leaf annotation dialog
    Object obj = leaves.get(id);
    if (obj != null) {
      STreeNode leaf = (STreeNode) obj;
      //do nothing if it already has a parent
      if (leaf.getParent() != null) {
        clearSelection();
        JOptionPane.showMessageDialog(this, "Node already annotated. To delete the existing annotation,select it and press <DEL>.",
                                      "Syntax Tree Viewer message",
                                      JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      //reset the popup and set it's heading accordingly
      popup.setLabel("leaves");
      setMenuCommands(popup, ""+id);

      popup.pack();
      popup.show(source, e.getX(), e.getY());
    } else { //we have a non-terminal node

      //check if it has been annotated already
      if ( ((STreeNode) nonTerminals.get(id)).getParent() != null) {
        clearSelection();
        JOptionPane.showMessageDialog(this, "Node already annotated. To delete"+
                          " the existing annotation, select it and press <DEL>.",
                          "Syntax Tree Viewer message",
                          JOptionPane.INFORMATION_MESSAGE);
        return;  //and do nothing if so!
      }

      popup.setLabel("non-terminal");
      setMenuCommands(popup, ""+id);

      popup.pack();
      popup.show(source, e.getX(), e.getY());

    }

  } //showRightClickPopup

  private void addLines(STreeNode newNode) {

    JButton newButton = (JButton) buttons.get(new Integer(newNode.getID()));
    int nbX = newButton.getX() + newButton.getWidth()/2;
    int nbY = newButton.getY() + newButton.getHeight();

    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      JButton selButton = (JButton) i.next();

      //I create it a rect but it will in fact be used as x1, y1, x2, y2 for the
      //draw line. see drawLines.
      Coordinates coords = new Coordinates(
                                nbX,
                                nbY,
                                selButton.getX() + selButton.getWidth()/2,
                                selButton.getY());

      lines.add(coords);
    }

  } // addLines

  private void clearSelection() {
    for (Enumeration enum = selection.elements(); enum.hasMoreElements(); ) {
      JButton selButton = (JButton) enum.nextElement();
      selButton.setBackground(buttonBackground);
    }

    selection.clear();

  } //clearSlection


  private void fillCategoriesMenu() {
    boolean found = false;

    //fetch the valid categories from the stereotype
    CreoleRegister creoleReg = Gate.getCreoleRegister();
    java.util.List currentAnnotationSchemaList =
                      creoleReg.getLrInstances("gate.creole.AnnotationSchema");
    if (currentAnnotationSchemaList.isEmpty()) return;

    Iterator iter = currentAnnotationSchemaList.iterator();
    while (iter.hasNext()){
      AnnotationSchema annotSchema = (AnnotationSchema) iter.next();
      //we have found the right schema
      if (treeNodeAnnotationType.equals(annotSchema.getAnnotationName())) {
        found = true;
        FeatureSchema categories = annotSchema.getFeatureSchema("cat");
        //iterate through all categories
        for (Iterator i =
                categories.getPermissibleValues().iterator(); i.hasNext(); ) {

          JMenuItem menuItem = new JMenuItem( (String) i.next() );
          menuItem.addActionListener(this);
          popup.add(menuItem);
        } //for

      } //if
    }// while

    //if we don't have a schema, issue a warning
    if (! found)
      Out.println("Warning: You need to define an annotation schema for " +
                  treeNodeAnnotationType +
                  " in order to be able to add such annotations.");

  } // fillCategoriesMenu

  /**
    * Sets the action commands of all menu items to the specified command
    */
  private void setMenuCommands(JPopupMenu menu, String command) {
    for (int i = 0; i < menu.getComponentCount() ; i++) {
      JMenuItem item = (JMenuItem) menu.getComponent(i);
      item.setActionCommand(command);
    }

  } // setMenuCommands

  /**
    * Create a parent node for all selected non-terminal nodes
    */
  protected STreeNode createParentNode(String text) {
    STreeNode  parentNode = new STreeNode();

    long begin =  2147483647, end = 0, level= -1;
    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      JButton button = (JButton) i.next();
      Integer id = new Integer(button.getActionCommand());

      STreeNode child = (STreeNode) nonTerminals.get(id);

      if (begin > child.getStart())
        begin = child.getStart();
      if (end < child.getEnd())
        end = child.getEnd();
      if (level < child.getLevel())
        level = child.getLevel();

      parentNode.add(child);

    } //for

    parentNode.setLevel(level+1);
    parentNode.setStart(begin);
    parentNode.setEnd(end);
    parentNode.setUserObject(text);
    parentNode.createAnnotation(document,
                                treeNodeAnnotationType,
                                displayedString,
                                utteranceStartOffset.longValue());


    return parentNode;
  }

  /**
    * Create a parent node for all selected non-terminal nodes
    */
  protected STreeNode createParentNode(String text, Annotation annot) {
    STreeNode  parentNode = new STreeNode(annot);

    long level = -1;
    for (Iterator i = selection.iterator(); i.hasNext(); ) {
      JButton button = (JButton) i.next();
      Integer id = new Integer(button.getActionCommand());

      STreeNode child = (STreeNode) nonTerminals.get(id);

      if (level < child.getLevel())
        level = child.getLevel();

      parentNode.add(child);
    } //for

    parentNode.setLevel(level+1);
    parentNode.setUserObject(text);

    return parentNode;
  }


  void selectNode(MouseEvent e) {
    // try finding the node that's annotated, i.e., the selected button
    if (e.getSource() instanceof JButton) {
      JButton source = (JButton) e.getSource();

        selection.add(source);
        buttonBackground = source.getBackground();
        source.setBackground(selectedNodeColor);
    }
  }

  // remove that node from the syntax tree
  void removeNode(JButton button) {

    Integer id = new Integer(button.getActionCommand());
    STreeNode node = (STreeNode) nonTerminals.get(id);
    nonTerminals.remove(node);
    node.removeAnnotation(document);

    //fix the STreeNodes involved
    resetChildren(node);
    removeNodesAbove(node);

    //remove button from everywhere
    buttons.remove(button);
    button.setVisible(false);
    this.remove(button);

    //recalculate all lines
    recalculateLines();

    //make sure we clear the selection
    selection.clear();
    repaint();
  }

  //set parent node to null for all children of the given node
  private void resetChildren(STreeNode node) {
    for (Enumeration e = node.children(); e.hasMoreElements(); )
      ((STreeNode) e.nextElement()).setParent(null);

    node.disconnectChildren();
  }

  private void removeNodesAbove(STreeNode node) {
    STreeNode parent = (STreeNode) node.getParent();

    while (parent != null) {
      Integer id = new Integer(parent.getID());
      parent.removeAnnotation(document);
      if (parent.isNodeChild(node))
        parent.remove(node);
      parent.disconnectChildren();

      nonTerminals.remove(id);

      JButton button = (JButton) buttons.get(id);
      this.remove(button);
      buttons.remove(id);

      parent = (STreeNode) parent.getParent();
    }
  }

  private void recalculateLines() {
    lines.clear();
    //go through all non-terminals and recalculate their lines to their children
    for (Iterator i = nonTerminals.values().iterator(); i.hasNext(); )
      recalculateLines((STreeNode) i.next());

  }

  /**
    * recalculates all lines from that node to all its children
    */
  private void recalculateLines(STreeNode node) {
    Integer id = new Integer(node.getID());
    JButton button = (JButton) buttons.get(id);

    int bX = button.getX() + button.getWidth()/2;
    int bY = button.getY() + button.getHeight();

    for (Enumeration e = node.children(); e.hasMoreElements(); ) {
      STreeNode subNode = (STreeNode) e.nextElement();
      Integer sid = new Integer(subNode.getID());
      JButton subButton = (JButton) buttons.get(sid);

      Coordinates coords = new Coordinates(
                                bX,
                                bY,
                                subButton.getX() + subButton.getWidth()/2,
                                subButton.getY());

      lines.add(coords);
    }

  }

/*
  // discontinued from use,done automatically instead, when the utterance is set

  public void setTreeAnnotations(AnnotationSet newTreeAnnotations) {
    AnnotationSet  oldTreeAnnotations = treeAnnotations;
    treeAnnotations = newTreeAnnotations;
    firePropertyChange("treeAnnotations", oldTreeAnnotations,
                          newTreeAnnotations);
  }
*/

  public void setTreeNodeAnnotationType(String newTreeNodeAnnotationType) {
    treeNodeAnnotationType = newTreeNodeAnnotationType;
  }

  public void setTokenType(String newTokenType) {
    if (newTokenType != null && ! newTokenType.equals(""))
      tokenType = newTokenType;
  }

  public String getTokenType() {
    return tokenType;
  }

  void this_componentShown(ComponentEvent e) {
    Out.println("Tree Viewer shown");
  }

  void this_componentHidden(ComponentEvent e) {
    Out.println("Tree Viewer closes");
  }

/*
  //None of this works, damn!!!

  public void setVisible(boolean b) {
    if (!b && this.isVisible())
      Out.println("Tree Viewer closes");

    super.setVisible( b);
  }
  public void hide() {
    Out.println("Tree Viewer closes");
    super.hide();
  }
*/


}// class SyntaxTreeViewer


class FocusButton extends JButton {

  public FocusButton(String text) {
    super(text);
  }

  public FocusButton() {
    super();
  }

  public FocusButton(Icon icon) {
    super(icon);
  }

  public FocusButton(String text, Icon icon) {
    super(text, icon);
  }// public FocusButton

  public boolean isManagingFocus() {
    return true;
  }// public boolean isManagingFocus()

  public void processComponentKeyEvent(KeyEvent e) {
    super.processComponentKeyEvent(e);

    //I need that cause I get all events here, so I only want to process
    //when it's a release event. The reason is that for keys like <DEL>
    //key_typed never happens
    if (e.getID() != KeyEvent.KEY_RELEASED)
      return;

    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
      SyntaxTreeViewer viewer = (SyntaxTreeViewer) ((JButton) e.getSource()).getParent();
      viewer.removeNode((JButton) e.getSource());
    }
  }// public void processComponentKeyEvent(KeyEvent e)

} // class SyntaxTreeViewer

// $Log$
// Revision 1.19  2001/08/08 14:39:00  kalina
// Made the dialog to size itself maximum as much as the screen, coz was
// getting too big without that.
//
// Some documentation on Tree Viewer and some small changes to utterance2trees()
// to make it order the tokens correctly by offset
//
// Revision 1.18  2001/08/07 19:03:05  kalina
// Made the tree viewer use Token annotations to break the sentence for annotation
//
// Revision 1.17  2001/08/07 17:01:32  kalina
// Changed the AVR implementing classes in line with the updated AVR
// API (cancelAction() and setSpan new parameter).
//
// Also updated the TreeViewer, so now it can be used to edit and view
// Sentence annotations and the SyntaxTreeNodes associated with them.
// So if you have trees, it'll show them, if not, it'll help you build them.
//
// Revision 1.16  2001/04/09 10:36:36  oana
// a few changes in the code style
//
// Revision 1.14  2000/12/04 12:29:29  valyt
// Done some work on the visual resources
// Added the smart XJTable
//
// Revision 1.13  2000/11/08 16:35:00  hamish
// formatting
//
// Revision 1.12  2000/10/26 10:45:26  oana
// Modified in the code style
//
// Revision 1.11  2000/10/24 10:10:18  valyt
// Fixed the deprecation warning in gate/gui/SyntaxTreeViewer.java
//
// Revision 1.10  2000/10/18 13:26:47  hamish
// Factory.createResource now working, with a utility method that uses reflection (via java.beans.Introspector) to set properties on a resource from the
//     parameter list fed to createResource.
//     resources may now have both an interface and a class; they are indexed by interface type; the class is used to instantiate them
//     moved createResource from CR to Factory
//     removed Transients; use Factory instead
//
// Revision 1.9  2000/10/16 16:44:32  oana
// Changed the comment of DEBUG variable
//
// Revision 1.8  2000/10/10 15:36:35  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.7  2000/10/10 09:49:57  valyt
// Fixed the Annotation test
//
// Revision 1.6  2000/10/02 12:34:06  valyt
// Added the UnicodeEnabled switch on gate.util.Tools
//
// Revision 1.5  2000/09/28 14:26:09  kalina
// Added even more documentation (is this me?!) and allowed several tokens to be
// passed instead of a whole utterance/sentence for annotation. Needs good testing this
// but will do it when somebody tries using this functionality.
//
// Revision 1.4  2000/09/28 13:16:12  kalina
// Added some documentation
//
// Revision 1.3  2000/09/21 14:23:45  kalina
// Fixed some small bug in main(). To test just run the component itself.
//
// Revision 1.2  2000/09/21 14:17:27  kalina
// Added Unicode support
//
// Revision 1.1  2000/09/20 17:03:37  kalina
// Added the tree viewer from the prototype. It works now with the new annotation API.
