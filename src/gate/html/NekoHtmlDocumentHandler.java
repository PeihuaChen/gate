package gate.html;

import gate.Factory;
import gate.FeatureMap;
import gate.GateConstants;
import gate.corpora.DocumentContentImpl;
import gate.corpora.RepositioningInfo;
import gate.event.StatusListener;
import gate.html.HtmlDocumentHandler.CustomObject;
import gate.util.Err;
import gate.util.InvalidOffsetException;
import gate.util.Out;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTML;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class NekoHtmlDocumentHandler
                                    extends
                                      gate.xml.XmlPositionCorrectionHandler {
  private static final boolean DEBUG = false;

  /**
   * Constructor initialises all the private memeber data
   * 
   * @param aDocument The gate document that will be processed
   * @param anAnnotationSet The annotation set that will contain
   *          annotations resulted from the processing of the gate
   *          document
   * @param ignorableTags HTML tag names (lower case) whose text content
   *          should be ignored by this handler.
   */
  public NekoHtmlDocumentHandler(gate.Document aDocument,
          gate.AnnotationSet anAnnotationSet, Set<String> ignorableTags) {
    if(ignorableTags == null) {
      ignorableTags = new HashSet<String>();
    }
    if(DEBUG) {
      Out.println("Created NekoHtmlDocumentHandler.  ignorableTags = "
              + ignorableTags);
    }
    // init stack
    stack = new java.util.Stack<CustomObject>();

    // this string contains the plain text (the text without markup)
    tmpDocContent = new StringBuilder(aDocument.getContent().size().intValue());

    // colector is used later to transform all custom objects into
    // annotation objects
    colector = new LinkedList<CustomObject>();

    // the Gate document
    doc = aDocument;

    // init an annotation set for this gate document
    basicAS = anAnnotationSet;

    customObjectsId = 0;

    this.ignorableTags = ignorableTags;
  }// HtmlDocumentHandler

  /**
   * Called when the parser encounters the start of an HTML element.
   * Empty elements also trigger this method, followed immediately by an
   * {@link #endElement}.
   */
  @Override
  public void startElement(String uri, String localName, String qName,
          Attributes attributes) throws SAXException {
    // call characterActions
    if(readCharacterStatus) {
      readCharacterStatus = false;
      charactersAction(new String(contentBuffer).toCharArray(), 0,
              contentBuffer.length());
    }
    // localName = localName.toLowerCase();
    if(DEBUG) {
      Out.println("startElement: " + localName);
    }
    // Fire the status listener if the elements processed exceded the
    // rate
    if(0 == (++elements % ELEMENTS_RATE))
      fireStatusChangedEvent("Processed elements : " + elements);

    // Start of ignorable tag
    if(ignorableTags.contains(localName)) {
      ignorableTagLevels++;
      if(DEBUG) {
        Out.println("  ignorable tag: levels = " + ignorableTagLevels);
      }
    } // if

    // Construct a feature map from the attributes list
    FeatureMap fm = Factory.newFeatureMap();

    // Take all the attributes an put them into the feature map
    for(int i = 0; i < attributes.getLength(); i++) {
      if(DEBUG) {
        Out.println("  attribute: " + attributes.getLocalName(i) + " = "
                + attributes.getValue(i));
      }
      fm.put(attributes.getLocalName(i), attributes.getValue(i));
    }

    // Just analize the tag t and add some\n chars and spaces to the
    // tmpDocContent.The reason behind is that we need to have a
    // readable form
    // for the final document.
    customizeAppearanceOfDocumentWithStartTag(localName);

    // If until here the "tmpDocContent" ends with a NON whitespace
    // char,
    // then we add a space char before calculating the START index of
    // this
    // tag.
    // This is done in order not to concatenate the content of two
    // separate tags
    // and obtain a different NEW word.
    int tmpDocContentSize = tmpDocContent.length();
    if(tmpDocContentSize != 0
            && !Character.isWhitespace(tmpDocContent
                    .charAt(tmpDocContentSize - 1))) tmpDocContent.append(" ");

    // create the start index of the annotation
    Long startIndex = new Long(tmpDocContent.length());

    // initialy the start index is equal with the End index
    CustomObject obj = new CustomObject(localName, fm, startIndex, startIndex);

    // put it into the stack
    stack.push(obj);

  }

  /**
   * Called when the parser encounters character or CDATA content.
   * Characters may be reported in more than one chunk, so we gather all
   * contiguous chunks together and process them in one block.
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if(!readCharacterStatus) {
      contentBuffer = new StringBuilder(new String(ch, start, length));
    }
    else {
      contentBuffer.append(new String(ch, start, length));
    }
    readCharacterStatus = true;
  }

  /**
   * Called when all text between two tags has been processed.
   */
  public void charactersAction(char[] ch, int start, int length)
          throws SAXException {
    if(DEBUG) {
      Out.println("charactersAction: " + new String(ch, start, length));
    }
    // position correction
    super.characters(ch, start, length);

    // Skip ignorable tag content
    if(ignorableTagLevels > 0) {
      if(DEBUG) {
        Out.println("  inside ignorable tag, skipping");
      }
      return;
    }

    // create a string object based on the reported text
    String content = new String(ch, start, length);

    // remove the difference between JDK 1.3 and JDK 1.4
    String trimContent = content.trim();
    if(trimContent.length() == 0) {
      return;
    } // if

    int trimCorrection = content.indexOf(trimContent.charAt(0));
    content = trimContent;

    StringBuffer contentBuffer = new StringBuffer("");
    int tmpDocContentSize = tmpDocContent.length();
    boolean incrementStartIndex = false;
    // If the first char of the text just read "text[0]" is NOT
    // whitespace AND
    // the last char of the tmpDocContent[SIZE-1] is NOT whitespace then
    // concatenation "tmpDocContent + content" will result into a new
    // different
    // word... and we want to avoid that...
    if(tmpDocContentSize != 0
            && content.length() != 0
            && !Character.isWhitespace(content.charAt(0))
            && !Character.isWhitespace(tmpDocContent
                    .charAt(tmpDocContentSize - 1))) {

      contentBuffer.append(" ");
      incrementStartIndex = true;
    }// End if
    // update the document content

    // put the repositioning information
    if(reposInfo != null) {
      int extractedPos = tmpDocContent.length() + contentBuffer.length();
      addRepositioningInfo(content, (int)(getRealOffset() + trimCorrection),
              extractedPos);
    } // if

    contentBuffer.append(content);
    // calculate the End index for all the elements of the stack
    // the expression is : End index = Current doc length + text length
    Long end = new Long(tmpDocContent.length() + contentBuffer.length());

    CustomObject obj = null;
    // Iterate through stack to modify the End index of the existing
    // elements

    java.util.Iterator<CustomObject> anIterator = stack.iterator();
    while(anIterator.hasNext()) {
      // get the object and move to the next one
      obj = anIterator.next();
      if(incrementStartIndex && obj.getStart().equals(obj.getEnd())) {
        obj.setStart(new Long(obj.getStart().longValue() + 1));
      }// End if
      // sets its End index
      obj.setEnd(end);
    }// End while

    tmpDocContent.append(contentBuffer.toString());

  }

  /**
   * Called when the parser encounters the end of an HTML element.
   */
  @Override
  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    // call characterActions
    if(readCharacterStatus) {
      readCharacterStatus = false;
      charactersAction(new String(contentBuffer).toCharArray(), 0,
              contentBuffer.length());
    }

    // localName = localName.toLowerCase();
    if(DEBUG) {
      Out.println("endElement: " + localName);
    }

    // obj is for internal use
    CustomObject obj = null;

    // end of ignorable tag
    if(ignorableTags.contains(localName)) {
      ignorableTagLevels--;
      if(DEBUG) {
        Out.println("  end of ignorable tag.  levels = " + ignorableTagLevels);
      }
    } // if

    // If the stack is not empty then we get the object from the stack
    if(!stack.isEmpty()) {
      obj = (CustomObject)stack.pop();
      // Before adding it to the colector, we need to check if is an
      // emptyAndSpan one. See CustomObject's isEmptyAndSpan field.
      if(obj.getStart().equals(obj.getEnd())) {
        // The element had an end tag and its start was equal to its
        // end. Hence
        // it is anEmptyAndSpan one.
        obj.getFM().put("isEmptyAndSpan", "true");
      }// End iff
      // we add it to the colector
      colector.add(obj);
    }// End if

    // If element has text between, then customize its apearance
    if(obj != null && obj.getStart().longValue() != obj.getEnd().longValue())
    // Customize the appearance of the document
      customizeAppearanceOfDocumentWithEndTag(localName);
  }

  /**
   * Called when the parser reaches the end of the document. Here we
   * store the new content and construct the Original markups
   * annotations.
   */
  @Override
  public void endDocument() throws SAXException {
    if(DEBUG) {
      Out.println("endDocument");
    }
    CustomObject obj = null;
    // replace the old content with the new one
    doc.setContent(new DocumentContentImpl(tmpDocContent.toString()));

    // If basicAs is null then get the default annotation
    // set from this gate document
    if(basicAS == null)
      basicAS = doc
              .getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);

    // sort colector ascending on its id
    Collections.sort(colector);
    // iterate through colector and construct annotations
    while(!colector.isEmpty()) {
      obj = colector.getFirst();
      colector.remove(obj);
      // Construct an annotation from this obj
      try {
        basicAS.add(obj.getStart(), obj.getEnd(), obj.getElemName(), obj
                .getFM());
      }
      catch(InvalidOffsetException e) {
        Err.prln("Error creating an annot :" + obj + " Discarded...");
      }// end try
      // }// end if
    }// while

    // notify the listener about the total amount of elements that
    // has been processed
    fireStatusChangedEvent("Total elements : " + elements);
  }

  /**
   * Non-fatal error, print the stack trace but continue processing.
   */
  @Override
  public void error(SAXParseException e) throws SAXException {
    e.printStackTrace(Err.getPrintWriter());
  }

  /**
   * For given content the list with shrink position information is
   * searched and on the corresponding positions the correct
   * repositioning information is calculated and generated.
   */
  public void addRepositioningInfo(String content, int pos, int extractedPos) {
    int contentLength = content.length();

    // wrong way (without correction and analysing)
    // reposInfo.addPositionInfo(pos, contentLength, extractedPos,
    // contentLength);

    RepositioningInfo.PositionInfo pi = null;
    long startPos = pos;
    long correction = 0;
    long substituteStart;
    long remainingLen;
    long offsetInExtracted;

    for(int i = 0; i < ampCodingInfo.size(); ++i) {
      pi = (RepositioningInfo.PositionInfo)ampCodingInfo.get(i);
      substituteStart = pi.getOriginalPosition();

      if(substituteStart >= startPos) {
        if(substituteStart > pos + contentLength + correction) {
          break; // outside the current text
        } // if

        // should create two repositioning information records
        remainingLen = substituteStart - (startPos + correction);
        offsetInExtracted = startPos - pos;
        if(remainingLen > 0) {
          reposInfo.addPositionInfo(startPos + correction, remainingLen,
                  extractedPos + offsetInExtracted, remainingLen);
        } // if
        // record for shrank text
        reposInfo.addPositionInfo(substituteStart, pi.getOriginalLength(),
                extractedPos + offsetInExtracted + remainingLen, pi
                        .getCurrentLength());
        startPos = startPos + remainingLen + pi.getCurrentLength();
        correction += pi.getOriginalLength() - pi.getCurrentLength();
      } // if
    } // for

    // there is some text remaining for repositioning
    offsetInExtracted = startPos - pos;
    remainingLen = contentLength - offsetInExtracted;
    if(remainingLen > 0) {
      reposInfo.addPositionInfo(startPos + correction, remainingLen,
              extractedPos + offsetInExtracted, remainingLen);
    } // if
  } // addRepositioningInfo

  /**
   * This method analizes the tag t and adds some \n chars and spaces to
   * the tmpDocContent.The reason behind is that we need to have a
   * readable form for the final document. This method modifies the
   * content of tmpDocContent.
   * 
   * @param t the Html tag encounted by the HTML parser
   */
  protected void customizeAppearanceOfDocumentWithStartTag(String tagName) {
    boolean modification = false;
    if("p".equals(tagName)) {
      int tmpDocContentSize = tmpDocContent.length();
      if(tmpDocContentSize >= 2
              && '\n' != tmpDocContent.charAt(tmpDocContentSize - 2)) {
        tmpDocContent.append("\n");
        modification = true;
      }
    }// End if
    // if the HTML tag is BR then we add a new line character to the
    // document
    if("br".equals(tagName)) {
      tmpDocContent.append("\n");
      modification = true;
    }// End if
    if(modification == true) {
      Long end = new Long(tmpDocContent.length());
      java.util.Iterator<CustomObject> anIterator = stack.iterator();
      while(anIterator.hasNext()) {
        // get the object and move to the next one, and set its end
        // index
        anIterator.next().setEnd(end);
      }// End while
    }// End if
  }// customizeAppearanceOfDocumentWithStartTag

  /**
   * This method analizes the tag t and adds some \n chars and spaces to
   * the tmpDocContent.The reason behind is that we need to have a
   * readable form for the final document. This method modifies the
   * content of tmpDocContent.
   * 
   * @param t the Html tag encounted by the HTML parser
   */
  protected void customizeAppearanceOfDocumentWithEndTag(String tagName) {
    boolean modification = false;
    // if the HTML tag is BR then we add a new line character to the
    // document
    if(("p".equals(tagName)) || ("h1".equals(tagName))
            || ("h2".equals(tagName)) || ("h3".equals(tagName))
            || ("h4".equals(tagName)) || ("h5".equals(tagName))
            || ("h6".equals(tagName)) || ("tr".equals(tagName))
            || ("center".equals(tagName)) || ("li".equals(tagName))) {
      tmpDocContent.append("\n");
      modification = true;
    }

    if("title".equals(tagName)) {
      tmpDocContent.append("\n\n");
      modification = true;
    }// End if

    if(modification == true) {
      Long end = new Long(tmpDocContent.length());
      java.util.Iterator anIterator = stack.iterator();
      while(anIterator.hasNext()) {
        // get the object and move to the next one
        CustomObject obj = (CustomObject)anIterator.next();
        // sets its End index
        obj.setEnd(end);
      }// End while
    }// End if
  }// customizeAppearanceOfDocumentWithEndTag

  /** Keep the refference to this structure */
  private RepositioningInfo reposInfo = null;

  /** Keep the refference to this structure */
  private RepositioningInfo ampCodingInfo = null;

  /**
   * Set repositioning information structure refference. If you set this
   * refference to <B>null</B> information wouldn't be collected.
   */
  public void setRepositioningInfo(RepositioningInfo info) {
    reposInfo = info;
  } // setRepositioningInfo

  /** Return current RepositioningInfo object */
  public RepositioningInfo getRepositioningInfo() {
    return reposInfo;
  } // getRepositioningInfo

  /**
   * Set repositioning information structure refference for ampersand
   * coding. If you set this refference to <B>null</B> information
   * wouldn't be used.
   */
  public void setAmpCodingInfo(RepositioningInfo info) {
    ampCodingInfo = info;
  } // setRepositioningInfo

  /** Return current RepositioningInfo object for ampersand coding. */
  public RepositioningInfo getAmpCodingInfo() {
    return ampCodingInfo;
  } // getRepositioningInfo

  /**
   * The HTML tag names (lower case) whose text content should be
   * ignored completely by this handler. Typically this is just script
   * and style tags.
   */
  private Set<String> ignorableTags = null;

  /**
   * Set the set of tag names whose text content will be ignored.
   * 
   * @param newTags a set of lower-case tag names
   */
  public void setIgnorableTags(Set<String> newTags) {
    ignorableTags = newTags;
  }

  /**
   * Get the set of tag names whose content is ignored by this handler.
   */
  public Set<String> getIgnorableTags() {
    return ignorableTags;
  }

  // HtmlDocumentHandler member data

  // counter for the number of levels of ignorable tag we are inside.
  // For example, if we configured "ul" as an ignorable tag name then
  // this variable would have the following values:
  //
  // 0: <p>
  // 0: This is some text
  // 1: <ul>
  // 1: <li>
  // 1: some more text
  // 2: <ul> ...
  // 1: </ul>
  // 1: </li>
  // 0: </ul>
  //
  // this allows us to support nested ignorables
  int ignorableTagLevels = 0;

  // this constant indicates when to fire the status listener
  // this listener will add an overhead and we don't want a big overhead
  // this listener will be callled from ELEMENTS_RATE to ELEMENTS_RATE
  final static int ELEMENTS_RATE = 128;

  // the content of the HTML document, without any tag
  // for internal use
  private StringBuilder tmpDocContent = null;

  /**
   * This is used to capture all data within two tags before calling the
   * actual characters method
   */
  private StringBuilder contentBuffer = new StringBuilder("");

  /** This is a variable that shows if characters have been read */
  private boolean readCharacterStatus = false;

  // a stack used to remember elements and to keep the order
  private java.util.Stack<CustomObject> stack = null;

  // a gate document
  private gate.Document doc = null;

  // an annotation set used for creating annotation reffering the doc
  private gate.AnnotationSet basicAS;

  // listeners for status report
  protected List<StatusListener> myStatusListeners = new LinkedList<StatusListener>();

  // this reports the the number of elements that have beed processed so
  // far
  private int elements = 0;

  protected int customObjectsId = 0;

  public int getCustomObjectsId() {
    return customObjectsId;
  }

  // we need a colection to retain all the CustomObjects that will be
  // transformed into annotation over the gate document...
  // the transformation will take place inside onDocumentEnd() method
  private LinkedList<CustomObject> colector = null;

  // Inner class
  /**
   * The objects belonging to this class are used inside the stack. This
   * class is for internal needs
   */
  class CustomObject implements Comparable<CustomObject> {

    // constructor
    public CustomObject(String anElemName, FeatureMap aFm, Long aStart,
            Long anEnd) {
      elemName = anElemName;
      fm = aFm;
      start = aStart;
      end = anEnd;
      id = new Long(customObjectsId++);
    }// End CustomObject()

    // Methos implemented as required by Comparable interface
    public int compareTo(CustomObject obj) {
      return this.id.compareTo(obj.getId());
    }// compareTo();

    // accesor
    public String getElemName() {
      return elemName;
    }// getElemName()

    public FeatureMap getFM() {
      return fm;
    }// getFM()

    public Long getStart() {
      return start;
    }// getStart()

    public Long getEnd() {
      return end;
    }// getEnd()

    public Long getId() {
      return id;
    }

    // mutator
    public void setElemName(String anElemName) {
      elemName = anElemName;
    }// getElemName()

    public void setFM(FeatureMap aFm) {
      fm = aFm;
    }// setFM();

    public void setStart(Long aStart) {
      start = aStart;
    }// setStart();

    public void setEnd(Long anEnd) {
      end = anEnd;
    }// setEnd();

    // data fields
    private String elemName = null;

    private FeatureMap fm = null;

    private Long start = null;

    private Long end = null;

    private Long id = null;

  } // End inner class CustomObject

  // StatusReporter Implementation

  public void addStatusListener(StatusListener listener) {
    myStatusListeners.add(listener);
  }

  public void removeStatusListener(StatusListener listener) {
    myStatusListeners.remove(listener);
  }

  protected void fireStatusChangedEvent(String text) {
    Iterator<StatusListener> listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      listenersIter.next().statusChanged(text);
  }

}
