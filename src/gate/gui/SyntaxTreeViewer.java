
//Title:        GATE
//Version:      $Id$
//Copyright:    Copyright (c) 2000
//Author:
//Company:      NLP Group, Univ. of Sheffield
//Description:

package gate.gui;

//java imports
import java.util.*;
import java.beans.*;

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
import java.net.URL;

/**
The SyntaxTreeViewer works by getting an annotation set of all annotations that
need to be displayed (both text and tree nodes) as an AnnotationSet property
called treeAnnotations. The types of annotations used by the viewer can be
configured although it also has default values. <P>

The textAnnotationType property specifies the type
of annotation which is used to get the text from (e.g. token, utterance);
default value - utterance. The treeNodeAnnotationType is the name of the
annotations which encode the SyntaxTreeNodes; default - SyntaxTreeNode. The
component assumes that the annotations of type treeNodeAnnotationType have
features called: cat with a value String; consists which is a Vector either empty
or with annotation ids of the node's children; and text which contains the text
covered by this annotation. The component will work fine even without the last
feature. Still when it creates annotations, these will have this feature added. <P>


Newly added tree nodes to the tree are automatically added to the document
as annotations. Deleted nodes are automatically deleted from the document
annotations too. <P>

In order to have appropriate behaviour always put this component inside a
scroll pane or something similar that provides scrollers. Example code: <BREAK>
<PRE>
    JScrollPane scroller = new JScrollPane(syntaxTreeViewer1);
    scroller.setPreferredSize(syntaxTreeViewer1.getPreferredSize());
		frame.getContentPane().add(scroller, BorderLayout.CENTER);
</PRE>

To get an idea how to use the component, look at the main function which is also
the test for this bean. <P>

*/


public class SyntaxTreeViewer extends JPanel
    implements Scrollable, ActionListener, MouseListener{

  //class members
  private boolean laidOut = false;  //whether to use any layout or not

  private int horizButtonGap = 5; //display all buttons x pixels apart horizontally
  private int vertButtonGap = 50; //display buttons at diff layers x pixels apart vertically
  private int extraButtonWidth = 10; //extra width in pixels to be added to each button
  private int maxUnitIncrement = 10; //number of pixels to be used as increment by scroller

  //GUI members
  BorderLayout borderLayout1 = new BorderLayout();
  JPopupMenu popup = new JPopupMenu(); //the right-click popup
  Color buttonBackground;
  Color selectedNodeColor = Color.red.darker();

  //the HashSet with the coordinates of the lines to draw
  HashSet lines = new HashSet();

  //The utterance to be annotated as a sentence. It's not used if the tree is passed
  //as annotations.
  private Annotation utterance;
  private int utteranceOffset = 0;
  //for internal use only. Set when the utterance is set.
  private String displayedString = "";

  //The name of the annotation type which is used to locate the
  //stereotype with the allowed categories
  //also when reading and creating annotations
  private String treeNodeAnnotationType = "SyntaxTreeNode";

  //The annotation name of the annotations used to extract the
  //text that appears at the leaves of the tree. For now the viewer
  //supports only one such annotation but might be an idea to extend it
  //so that it gets its text off many token annotations, which do not
  //need to be tokenised or off the syntax tree annotations themselves.
  private String textAnnotationType = "utterance";

  private HashMap leaves = new HashMap();  //all leaf nodes
  private HashMap nonTerminals = new HashMap(); //all non-terminal nodes
  private HashMap buttons = new HashMap(); //all buttons corresponding to any node
  private Vector selection = new Vector(); //all selected buttons
	private AnnotationSet treeAnnotations; //all annotations tp be displayed
  private Document document = null;
//the document to which the annotations belong

  private static BasicUnicodeButtonUI buttonUI = new BasicUnicodeButtonUI();

  private SyntaxTreeViewer() {   //override so we can't be constructed like that!
  }

  //CONSTRUCTORS
  public SyntaxTreeViewer(String annotType) {
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
    this.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent e) {
        this_propertyChange(e);
      }
    });

    buttonBackground = this.getBackground();

    //get all categories from stereotype
    fillCategoriesMenu();

    //initialise the popup menu

    //add popup to container
		this.add(popup);

  }

  public static void main(String[] args) throws Exception{
    Gate.init();
//    final String text = "This is a sentence. That is another one.";
    final String text = "\u0915\u0932\u094d\u0907\u0928\u0643\u0637\u0628 \u041a\u0430\u043b\u0438\u043d\u0430 Kalina";
    final Document doc = Transients.newDocument(text);

//  that works too but only use if you have the test file there.    
//    final Document doc = Transients.newDocument(new URL("file:///z:/temp/weird.txt"), "UTF-8");


    final SyntaxTreeViewer syntaxTreeViewer1 = new SyntaxTreeViewer("SyntaxTreeNode");
    //need to set the document here!!!!
     

		JFrame frame = new JFrame();

    //INITIALISE THE FRAME, ETC.
		frame.setEnabled(true);
		frame.setTitle("SyntaxTree Viewer");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		frame.getContentPane().add(syntaxTreeViewer1, BorderLayout.CENTER);
    //intercept the closing event to shut the application
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        AnnotationSet hs = doc.getAnnotations().get("SyntaxTreeNode");
        if (hs != null && hs.size() > 0) {
          int k = 0;
          for (Iterator i = hs.iterator(); i.hasNext(); k++) {
            System.out.println("Tree Annot " + k + ": ");
            System.out.println(i.next().toString());
          }
        } //if
        System.out.println("Exiting...");
        System.exit(0);
      }
    });

    //Put the bean in a scroll pane.
    JScrollPane scroller = new JScrollPane(syntaxTreeViewer1);
    scroller.setPreferredSize(syntaxTreeViewer1.getPreferredSize());
		frame.getContentPane().add(scroller, BorderLayout.CENTER);


    //DISPLAY FRAME
    frame.pack();
    frame.show();

    FeatureMap attrs = Transients.newFeatureMap();
    attrs.put("time", new Long(0));
    attrs.put("text", doc.getContent().toString());

    FeatureMap attrs1 = Transients.newFeatureMap();
    attrs1.put("cat", "N");
    attrs1.put("text", "This");
    attrs1.put("consists", new Vector());

    FeatureMap attrs2 = Transients.newFeatureMap();
    attrs2.put("cat", "V");
    attrs2.put("text", "is");
    attrs2.put("consists", new Vector());


    doc.getAnnotations().add( new Long(0), new Long(doc.getContent().toString().length()),
                              "utterance", attrs);
/*    Integer id1 = doc.getAnnotations().add(new Long(0), new Long(4),
                              "SyntaxTreeNode", attrs1);
    Integer id2 = doc.getAnnotations().add(new Long(5), new Long(7),
                              "SyntaxTreeNode", attrs2);

    FeatureMap attrs3 = Transients.newFeatureMap();
    attrs3.put("cat", "VP");
    attrs3.put("text", "This is");
    Vector consists = new Vector();
    consists.add(id1);
    consists.add(id2);
    attrs3.put("consists", consists);
    doc.getAnnotations().add(new Long(0), new Long(7), "SyntaxTreeNode", attrs3);
*/
    HashSet set = new HashSet();
    set.add("utterance");
    set.add("SyntaxTreeNode");
    AnnotationSet annots = doc.getAnnotations().get(set);

    syntaxTreeViewer1.setTreeAnnotations(annots);

  }

  protected void paintComponent(Graphics g) {
    super.paintComponent( g);

    drawLines(g);
  }


  private void drawLines(Graphics g) {

    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      Coordinates coords = (Coordinates) i.next();

      g.drawLine( coords.getX1(),
                  coords.getY1(),
                  coords.getX2(),
                  coords.getY2());
    }
  }

  public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return maxUnitIncrement;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width - maxUnitIncrement;
    else
        return visibleRect.height - maxUnitIncrement;
  }

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }



  void this_propertyChange(PropertyChangeEvent e) {
    //we have a new utterance to display and annotate
    if (e.getPropertyName().equals("utterance")) {
      clearAll();
      utterances2Trees();
    }
    if (e.getPropertyName().equals("treeAnnotations")) {
    	clearAll();
      document = treeAnnotations.getDocument();
      annotations2Trees();
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
  * Converts the annotations into treeNodes and sets the displayedString correctly.
  */
  private void annotations2Trees() {
  	HashMap processed = new HashMap(); //for all processed annotations

    AnnotationSet utterances =
    	treeAnnotations.get(textAnnotationType);

		if (utterances.size() > 1)
    	System.out.println("Tree Viewer can't display more than one utterance/sentence at a time! Using only first annotation");
    else if (utterances.size() == 0) {
      System.out.println("No annotations of type " + textAnnotationType + " passed so can't display anything!");
      return;
    }

    //we have our utterance now
    utterance = (Annotation) utterances.iterator().next();

    //let's process that into leaves
    utterances2Trees();

    //sort them from left to right first
    AnnotationSet nodeAnnotations = treeAnnotations.get(treeNodeAnnotationType);
    if (nodeAnnotations == null || nodeAnnotations.isEmpty())
      return;

    //now sort the annotation set. Should work as annotation implements Comparable
    LinkedList nodeAnnots = new LinkedList(nodeAnnotations);
    Collections.sort(nodeAnnots);

    Vector childrenButtons = new Vector();
    String oldParent = "";

		//find all annotations with no children
    Iterator i = nodeAnnots.iterator();
    while (i.hasNext()) {
    	Annotation annot = (Annotation) i.next();

      Vector children = (Vector) annot.getFeatures().get("consists");
      //check if it's a leaf
      if (children == null ||
      		children.isEmpty())
        {

        STreeNode leaf = findLeaf(annot.getStartNode(), annot.getEndNode());
        if (leaf == null) {//not found
        	System.out.println("Can't find my leaf node for annotation: " + annot);
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
    Vector children = (Vector) annot.getFeatures().get("consists");

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
  }

  private STreeNode findLeaf(Node start, Node end) {
  	for (Iterator i = leaves.values().iterator(); i.hasNext(); ) {
    	STreeNode node = (STreeNode) i.next();
      if (node.getStart() == start.getOffset().intValue() &&
          node.getEnd() == end.getOffset().intValue()
         )
      	return node;
    }

    return null;
  }


  /**
  * Converts the given utterances into a set of leaf nodes for annotation
  */
  private void utterances2Trees() {

  	if (! utterance.getType().equals(textAnnotationType)) {
			System.out.println("Can't display annotations other than the specified type:" + textAnnotationType);
      return;
    }

    displayedString = (String) utterance.getFeatures().get("text");

    //set the utterance offset correctly. All substring calculations depend on that.
    utteranceOffset = utterance.getStartNode().getOffset().intValue();
    int currentOffset = utteranceOffset;
    StrTokeniser strTok =
        new StrTokeniser((String) utterance.getFeatures().get("text"),
                        " \r\n\t");

    Insets insets = this.getInsets();
    int buttonX = insets.left;   //the starting X position for the buttons
    int buttonY = this.getHeight() - 20 - insets.bottom; //the starting Y position

    while (strTok.hasMoreTokens()) {
      String word = strTok.nextToken();

      //create the leaf node
      STreeNode node = new STreeNode(currentOffset, currentOffset + word.length());
      node.setAllowsChildren(false); //make it a leaf
      node.setUserObject(word); //set the text
      node.setLevel(0);
      leaves.put(new Integer(node.getID()), node); //add to hash table of leaves

      //create the corresponding button
      buttonX = createButton4Node(node, buttonX, buttonY);

      currentOffset += word.length()+1;  //// +1 to include the delimiter too

    }

    this.setSize(buttonX, buttonY + 20 + insets.bottom);
//    this.resize(buttonX, buttonY + 20 + insets.bottom);
    this.setPreferredSize(this.getSize());

  } //utterance2Trees


  /**
   * Returns the X position where another button can start if necessary.
      To be used to layout only the leaf buttons. All others must be created
      central to their children using createCentralButton.
  */
  private int createButton4Node(STreeNode node, int buttonX, int buttonY) {

    JButton button = new JButton((String) node.getUserObject());
    button.setBorderPainted(false);
    button.setUI(buttonUI);

    FontMetrics fm = button.getFontMetrics(button.getFont());

    int buttonWidth,
        buttonHeight;

//    System.out.print("Button width " + b1.getWidth() + "; Button height " + b1.getHeight());

    buttonWidth = fm.stringWidth(button.getText())
                  + button.getMargin().left + button.getMargin().right
                  + extraButtonWidth;
    buttonHeight = fm.getHeight() + button.getMargin().top + button.getMargin().bottom;
    buttonY = buttonY - buttonHeight;

//    System.out.print("New Button width " + buttonWidth + "; New Button height " + buttonHeight);
    button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    button.addActionListener(this);
    button.addMouseListener(this);
    button.setActionCommand("" + node.getID());

    this.add(button);
    buttons.put(new Integer(node.getID()), button);

    buttonX += buttonWidth + horizButtonGap;
    return buttonX;

  }

  private JButton createCentralButton(STreeNode newNode) {

    FocusButton button = new FocusButton((String) newNode.getUserObject());
    button.setBorderPainted(false);

    FontMetrics fm = button.getFontMetrics(button.getFont());

    int buttonWidth,
        buttonHeight,
        buttonX = 0,
        buttonY =0;

//    System.out.print("Button width " + b1.getWidth() + "; Button height " + b1.getHeight());

    buttonWidth = fm.stringWidth(button.getText())
                  + button.getMargin().left + button.getMargin().right
                  + extraButtonWidth;
    buttonHeight = fm.getHeight() + button.getMargin().top + button.getMargin().bottom;

    int left = this.getWidth(), right =0 , top = this.getHeight();
    //determine the left, right, top
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
//    System.out.println("Button's Y is" + buttonY);

//    System.out.print("New Button width " + buttonWidth + "; New Button height " + buttonHeight);
    button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    button.addActionListener(this);
    button.addMouseListener(this);
//    button.registerKeyboardAction(this,
//    															"delete",
//                                  KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
//                                  WHEN_FOCUSED);

    button.setActionCommand("" + newNode.getID());

    this.add(button);
    buttons.put(new Integer(newNode.getID()), button);   //add to hashmap of buttons

    //check if we need to resize the panel
    if (buttonY < 0) {
    	this.setSize(this.getWidth(), this.getHeight() + 5* (- buttonY));
      this.setPreferredSize(this.getSize());
      shiftButtonsDown(5* (-buttonY));
    }

		return button;
  }

  private void shiftButtonsDown(int offset) {
		for (Iterator i = buttons.values().iterator(); i.hasNext(); ) {
    	JButton button = (JButton) i.next();
			button.setBounds(		button.getX(),
      										button.getY() + offset,
                          button.getWidth(),
                          button.getHeight());
    } //for loop through buttons

    for (Iterator k = lines.iterator(); k.hasNext(); ) {
    	Coordinates coords = (Coordinates) k.next();
      coords.setY1(coords.getY1() + offset);
      coords.setY2(coords.getY2() + offset);
    }
  }

  public void actionPerformed(ActionEvent e) {

		//check for the popup menu items
    if (e.getSource() instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) e.getSource();

      //check if we're annotating a leaf
      //the popup label is set to leaves when the popup has been
      //constructed in showRightClickPopup
      if (popup.getLabel().equals("leaves")) {
        Integer id = new Integer(e.getActionCommand());

        clearSelection();
        JButton button = (JButton) buttons.get(id);
        selection.add(button);

        STreeNode leaf = (STreeNode) leaves.get(id);

        //create parent with the same span as leaf
        //using createParentNode here is not a good idea coz it works only
        //for creating parents of non-terminal nodes, not leaves
        STreeNode parent = new STreeNode(leaf.getStart(), leaf.getEnd());
        parent.setLevel(leaf.getLevel()+1); //levels increase from bottom to top
        parent.add(leaf);
        parent.setUserObject(menuItem.getText()); //set the text
        //last create the annotation; should always come last!
        parent.createAnnotation(  document,
                                  treeNodeAnnotationType,
        													displayedString,
                                  utteranceOffset);
        nonTerminals.put(new Integer(parent.getID()), parent);

        //create new button positioned centrally above the leaf
        createCentralButton(parent);

        //add the necessary lines for drawing
        addLines(parent);

        selection.clear();

        //repaint the picture!
        this.repaint();
      } //finished processing leaves
      else if (popup.getLabel().equals("non-terminal")) {
        //the action command is set to the id under which the button can be found
        Integer id = new Integer(e.getActionCommand());

        //locate button from buttons hashMap and add to selection
        JButton button = (JButton) buttons.get(id);
        selection.add(button);

        STreeNode parent = createParentNode(menuItem.getText()); //create the new parent
        nonTerminals.put(new Integer(parent.getID()), parent); //add to nonTerminals HashMap

        //create new button positioned centrally above the leaf
        createCentralButton(parent);

        //add the necessary lines for drawing
        addLines(parent);

        clearSelection();

        //repaint the picture!
        this.repaint();

      } //check for non-terminals

    } //if statement for MenuItems


  }

  public void mouseClicked(MouseEvent e) {

    if (! (e.getSource() instanceof JButton))
   		return;

    JButton source = (JButton) e.getSource();

		//check if CTRL is pressed and if not, clear the selection
    if (! e.isControlDown() && SwingUtilities.isLeftMouseButton(e))
//		if (! e.isControlDown() && e.getModifiers() == e.BUTTON1_MASK)
      clearSelection();
		//and select the current node
    if (SwingUtilities.isLeftMouseButton(e))
//		if (e.getModifiers() == e.BUTTON1_MASK)
	   	selectNode(e);


    //only repspond to right-clicks here by displaying the popup
    if (SwingUtilities.isRightMouseButton(e)) {
//    if (e.isPopupTrigger() || e.getModifiers() == e.BUTTON3_MASK) {
    	//if button not in focus, grad the focus and select it!
    	if ( source.getBackground() != selectedNodeColor ) {
        source.grabFocus();
        source.doClick();
        selectNode(e);
      }
//        System.out.println(e.getComponent().getClass() + " right-clicked!");
      showRightClickPopup(e);
    } //end of right-click processing

  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  } //createButton4Node


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
      	JOptionPane.showMessageDialog(this, "Node already annotated. To delete the existing annotation, select it and press <DEL>.",
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
      	JOptionPane.showMessageDialog(this, "Node already annotated. To delete the existing annotation, select it and press <DEL>.",
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

  }//addLines

  private void clearSelection() {
    for (Enumeration enum = selection.elements(); enum.hasMoreElements(); ) {
      JButton selButton = (JButton) enum.nextElement();
      selButton.setBackground(buttonBackground);
    }

    selection.clear();

  } //clearSlection


  private void fillCategoriesMenu() {


/* !!! This is how we do it when we have annotation stereotypes
       For now we just supply the stuff hand-coded and change it
       when stereotypes become available

    //fetch the valid categories from the stereotype
    Map stereotypes = AnnotationStereotype.getStereotypes();
    if (stereotypes == null || stereotypes.isEmpty())
      return;
      //get the right stereotype first
    AnnotationStereotype nodeStereotype =
            (AnnotationStereotype) stereotypes.get(treeNodeAnnotationType);
    if (nodeStereotype == null)
      return;

    AttributeStereotype categories = nodeStereotype.getAttributeStereotype("cat");
    //iterate through all categories
    for (Iterator i = categories.getPermissibleValues().iterator(); i.hasNext(); ) {

      JMenuItem menuItem = new JMenuItem( (String) i.next() );
      menuItem.addActionListener(this);
      popup.add(menuItem);

    }

*/
    JMenuItem menuItem = new JMenuItem("S");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("NP");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("DET");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("VP");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("N");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("V");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("Adj");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("Prep");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("Conj");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("Adv");
    menuItem.addActionListener(this);
    popup.add(menuItem);

    menuItem = new JMenuItem("PropN");
    menuItem.addActionListener(this);
    popup.add(menuItem);

  }//fillCategoriesMenu

  /**
   * Sets the action commands of all menu items to the specified command
  */
  private void setMenuCommands(JPopupMenu menu, String command) {
    for (int i = 0; i < menu.getComponentCount() ; i++) {
      JMenuItem item = (JMenuItem) menu.getComponentAtIndex(i);
      item.setActionCommand(command);
    }

  } //setMenuCommands

  /**
  * Create a parent node for all selected non-terminal nodes
  */
  protected STreeNode createParentNode(String text) {
    STreeNode  parentNode = new STreeNode();

    int begin =  2147483647, end = 0, level= -1;
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
                                utteranceOffset);


    return parentNode;
  }

  /**
  * Create a parent node for all selected non-terminal nodes
  */
  protected STreeNode createParentNode(String text, Annotation annot) {
    STreeNode  parentNode = new STreeNode(annot);

    int level = -1;
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
    //try finding the node that's annotated, i.e., the selected button
    if (e.getSource() instanceof JButton) {
      JButton source = (JButton) e.getSource();

        selection.add(source);
        buttonBackground = source.getBackground();
        source.setBackground(selectedNodeColor);
    }
  }

  //remove that node from the syntax tree
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

    recalculateLines();  //recalculate all lines
    selection.clear(); //make sure we clear the selection
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

	public void setTreeAnnotations(AnnotationSet newTreeAnnotations) {
		AnnotationSet  oldTreeAnnotations = treeAnnotations;
		treeAnnotations = newTreeAnnotations;
		firePropertyChange("treeAnnotations", oldTreeAnnotations, newTreeAnnotations);
	}

  public void setTreeNodeAnnotationType(String newTreeNodeAnnotationType) {
    treeNodeAnnotationType = newTreeNodeAnnotationType;
  }

  public void setTextAnnotationType(String newTextAnnotationType) {
    textAnnotationType = newTextAnnotationType;
  }







}


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
  }

	public boolean isManagingFocus() {
  	return true;
  }

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
  }

} //FocusButton

// $Log$
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

