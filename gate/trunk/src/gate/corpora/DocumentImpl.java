/*
 *  DocumentImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;
import gate.gui.*;
import gate.event.*;

/** Represents the commonalities between all sorts of documents.
  *
  * <H2>Editing</H2>
  *
  * <P>
  * The DocumentImpl class implements the Document interface.
  * The DocumentContentImpl class models the textual or audio-visual
  * materials which are the source and content of Documents.
  * The AnnotationSetImpl class supplies annotations on Documents.
  *
  * <P>
  * Abbreviations:
  *
  * <UL>
  * <LI>
  * DC = DocumentContent
  * <LI>
  * D = Document
  * <LI>
  * AS = AnnotationSet
  * </UL>
  *
  * <P>
  * We add an edit method to each of these classes; for DC and AS
  * the methods are package private; D has the public method.
  *
  * <PRE>
  *   void edit(Long start, Long end, DocumentContent replacement)
  *   throws InvalidOffsetException;
  * </PRE>
  *
  * <P>
  * D receives edit requests and forwards them to DC and AS.
  * On DC, this method makes a change to the content - e.g. replacing
  * a String range from start to end with replacement. (Deletions
  * are catered for by having replacement = null.) D then calls
  * AS.edit on each of its annotation sets.
  *
  * <P>
  * On AS, edit calls replacement.size() (i.e. DC.size()) to
  * figure out how long the replacement is (0 for null). It then
  * considers annotations that terminate (start or end) in
  * the altered or deleted range as invalid; annotations that
  * terminate after the range have their offsets adjusted.
  * I.e.:
  * <UL>
  * <LI>
  * the nodes that pointed inside the old modified area are invalid now and
  * will be deleted along with the connected annotations;
  * <LI>
  * the nodes that are before the start of the modified area remain
  * untouched;
  * <LI>
  * the nodes that are after the end of the affected area will have the
  * offset changed according to the formula below.
  * </UL>
  *
  * <P>
  * A note re. AS and annotations: annotations no longer have
  * offsets as in the old model, they now have nodes, and nodes
  * have offsets.
  *
  * <P>
  * To implement AS.edit, we have several indices:
  * <PRE>
  *   HashMap annotsByStartNode, annotsByEndNode;
  * </PRE>
  * which map node ids to annotations;
  * <PRE>
  *   RBTreeMap nodesByOffset;
  * </PRE>
  * which maps offset to Nodes.
  *
  * <P>
  * When we get an edit request, we traverse that part of the
  * nodesByOffset tree representing the altered or deleted
  * range of the DC. For each node found, we delete any annotations
  * that terminate on the node, and then delete the node itself.
  * We then traverse the rest of the tree, changing the offset
  * on all remaining nodes by:
  * <PRE>
  *   newOffset =
  *     oldOffset -
  *     (
  *       (end - start) -                                     // size of mod
  *       ( (replacement == null) ? 0 : replacement.size() )  // size of repl
  *     );
  * </PRE>
  * Note that we use the same convention as e.g. java.lang.String: start
  * offsets are inclusive; end offsets are exclusive. I.e. for string "abcd"
  * range 1-3 = "bc". Examples, for a node with offset 4:
  * <PRE>
  * edit(1, 3, "BC");
  * newOffset = 4 - ( (3 - 1) - 2 ) = 4
  *
  * edit(1, 3, null);
  * newOffset = 4 - ( (3 - 1) - 0 ) = 2
  *
  * edit(1, 3, "BBCC");
  * newOffset = 4 - ( (3 - 1) - 4 ) = 6
  * </PRE>
  */
public class DocumentImpl
extends AbstractLanguageResource implements Document, CreoleListener, DatastoreListener {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** If you set this flag to true the original content of the document will
   *  be kept in the document feature. <br>
   *  Default value is false to avoid the unnecessary waste of memory */
  private Boolean preserveOriginalContent = new Boolean(false);

  /** If you set this flag to true the repositioning information for
   *  the document will be kept in the document feature. <br>
   *  Default value is false to avoid the unnecessary waste of time and memory
   */
  private Boolean collectRepositioningInfo = new Boolean(false);

  /**
   * This is a variable which contains the latest crossed over annotation
   * found during export with preserving format, i.e., toXml(annotations)
   * method.
   */
  private Annotation crossedOverAnnotation = null;

  /** Default construction. Content left empty. */
  public DocumentImpl() {
    content = new DocumentContentImpl();
  } // default construction

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    // set up the source URL and create the content
    if(sourceUrl == null) {
      if(stringContent == null) {
        throw new ResourceInstantiationException(
          "The sourceURL and document's content were null."
        );
      }

      content = new DocumentContentImpl(stringContent);
      getFeatures().put("gate.SourceURL", "created from String");
    } else {
      try {

        content = new DocumentContentImpl(
          sourceUrl, encoding, sourceUrlStartOffset, sourceUrlEndOffset);
        getFeatures().put("gate.SourceURL", sourceUrl.toExternalForm());
      } catch(IOException e) {
        throw new ResourceInstantiationException("DocumentImpl.init: " + e);
      }

      if(preserveOriginalContent.booleanValue() && content != null) {
        String originalContent = new String(
          ((DocumentContentImpl) content).getOriginalContent());
        getFeatures().put(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME,
                      originalContent);
      } // if
    }

    // set up a DocumentFormat if markup unpacking required
    if(getMarkupAware().booleanValue()) {
      DocumentFormat docFormat =
        DocumentFormat.getDocumentFormat(this, sourceUrl);
      try {
        if(docFormat != null){
          StatusListener sListener = (StatusListener)
                                      gate.gui.MainFrame.getListeners().
                                      get("gate.event.StatusListener");
          if(sListener != null) docFormat.addStatusListener(sListener);

          // set the flag if true and if the document format support collecting
          docFormat.setShouldCollectRepositioning(collectRepositioningInfo);

          if(docFormat.getShouldCollectRepositioning().booleanValue()) {
            // unpack with collectiong of repositioning information
            RepositioningInfo info = new RepositioningInfo();

            String origContent = (String) getFeatures().get(
                GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);

            RepositioningInfo ampCodingInfo = new RepositioningInfo();
            if(origContent != null) {
              boolean shouldCorrectCR = docFormat instanceof XmlDocumentFormat;
              collectInformationForAmpCodding(origContent, ampCodingInfo,
                                              shouldCorrectCR);
              if(docFormat instanceof HtmlDocumentFormat) {
                collectInformationForWS(origContent, ampCodingInfo);
              } // if
            } // if

            docFormat.unpackMarkup(this, info, ampCodingInfo);

            if(origContent != null
                && docFormat instanceof XmlDocumentFormat) {
              // CRLF correction of RepositioningInfo
              correctRepositioningForCRLFInXML(origContent, info);
            } // if

            getFeatures().put(
                GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME, info);
          }
          else {
            // normal old fashioned unpack
            docFormat.unpackMarkup(this);
          }
          docFormat.removeStatusListener(sListener);
       } //if format != null
      } catch(DocumentFormatException e) {
        throw new ResourceInstantiationException(
          "Couldn't unpack markup in document " + sourceUrl.toExternalForm() +
          " " + e
        );
      }
    } // if markup aware

    return this;
  } // init()

  /**
   * Correct repositioning information for substitution of "\r\n" with "\n"
   */
  private void correctRepositioningForCRLFInXML(String content,
                                            RepositioningInfo info) {
    int index = -1;

    do {
      index = content.indexOf("\r\n", index+1);
      if(index != -1) {
        info.correctInformationOriginalMove(index, 1);
      } // if
    } while(index != -1);
  } // correctRepositioningForCRLF

  /**
   * Collect information for substitution of "&xxx;" with "y"
   *
   * It couldn't be collected a position information about
   * some unicode and &-coded symbols during parsing. The parser "hide" the
   * information about the position of such kind of parsed text.
   * So, there is minimal chance to have &-coded symbol inside the covered by
   * repositioning records area. The new record should be created for every
   * coded symbol outside the existing records.
   * <BR>
   * If <code>shouldCorrectCR</code> flag is <code>true</code> the correction
   * for CRLF substitution is performed.
   */
  private void collectInformationForAmpCodding(String content,
                                            RepositioningInfo info,
                                            boolean shouldCorrectCR) {

    if(content == null || info == null) return;

    int ampIndex = -1;
    int semiIndex;

    do {
      ampIndex = content.indexOf('&', ampIndex+1);
      if(ampIndex != -1) {
        semiIndex = content.indexOf(';', ampIndex+1);
        // have semicolon and it is near enough for amp codding
        if(semiIndex != -1 && (semiIndex-ampIndex) < 8) {
          info.addPositionInfo(ampIndex, semiIndex-ampIndex+1, 0, 1);
        }
        else {
          // no semicolon or it is too far
          // analyse for amp codding without semicolon
          int maxEnd = Math.min(ampIndex+8, content.length());
          String ampCandidate = content.substring(ampIndex, maxEnd);
          int ampCodingSize = analyseAmpCodding(ampCandidate);

          if(ampCodingSize != -1) {
            info.addPositionInfo(ampIndex, ampCodingSize, 0, 1);
          } // if

        } // if - semicolon found
      } // if - ampersand found
    } while (ampIndex != -1);

    // correct the collected information to adjust it's positions
    // with reported by the parser
    int index = -1;

    if(shouldCorrectCR) {
      do {
        index = content.indexOf("\r\n", index+1);
        if(index != -1) {
          info.correctInformationOriginalMove(index, -1);
        } // if
      } while(index != -1);
    } // if
  } // collectInformationForAmpCodding

  /**
   * This function compute size of the ampersand codded sequence when
   * semicolin is not present.
   */
  private int analyseAmpCodding(String content) {
    int result = -1;

    try {
      char ch = content.charAt(1);

      switch(ch) {
        case 'l' : // &lt
        case 'L' : // &lt
          if(content.charAt(2) == 't' || content.charAt(2) == 'T') {
            result = 3;
          } // if
          break;
        case 'g' : // &gt
        case 'G' : // &gt
          if(content.charAt(2) == 't' || content.charAt(2) == 'T') {
            result = 3;
          } // if
          break;
        case 'a' : // &amp
        case 'A' : // &amp
          if(content.substring(2, 4).equalsIgnoreCase("mp")) {
            result = 4;
          } // if
          break;
        case 'q' : // &quot
        case 'Q' : // &quot
          if(content.substring(2, 5).equalsIgnoreCase("uot")) {
            result = 5;
          } // if
          break;
        case '#' : // #number (example &#145, &#x4C38)
          int endIndex = 2;
          boolean hexCoded = false;
          if(content.charAt(2) == 'x' || content.charAt(2) == 'X') {
            // Hex codding
            ++endIndex;
            hexCoded = true;
          } // if

          while (endIndex < 8
                  && isNumber(content.charAt(endIndex), hexCoded) ) {
            ++endIndex;
          } // while
          result = endIndex;
          break;
      } // switch
    } catch (StringIndexOutOfBoundsException ex) {
      // do nothing
    } // catch

    return result;
  } // analyseAmpCodding

  /** Check for numeric range. If hex is true the A..F range is included */
  private boolean isNumber(char ch, boolean hex) {
    if(ch >= '0' && ch <= '9') return true;

    if(hex) {
      if(ch >= 'A' && ch <= 'F') return true;
      if(ch >= 'a' && ch <= 'f') return true;
    } // if

    return false;
  } // isNumber

  /** HTML parser perform substitution of multiple whitespaces (WS) with
   *  a single WS. To create correct repositioning information structure we
   *  should keep the information for such multiple WS.
   *  <BR>
   *  The criteria for WS is <code>(ch <= ' ')</code>.
   */
  private void collectInformationForWS(String content, RepositioningInfo info) {

    if(content == null || info == null) return;

    // analyse the content and correct the repositioning information
    char ch;
    int startWS, endWS;

    startWS = endWS = -1;
    int contentLength = content.length();

    for(int i=0; i<contentLength; ++i) {
      ch = content.charAt(i);

      // is whitespace
      if(ch <= ' ') {
        if(startWS == -1) {
          startWS = i;
        } // if
        endWS = i;
      }
      else {
        if(endWS - startWS > 0) {
          // put the repositioning information about the WS substitution
          info.addPositionInfo(
            (long)startWS, (long)(endWS - startWS + 1), 0, 1);
        } // if
        // clear positions
        startWS = endWS = -1;
      }// if
    } // for
  } // collectInformationForWS

  /** Clear all the data members of the object. */
  public void cleanup() {

    defaultAnnots = null;
    if ( (namedAnnotSets != null) && (!namedAnnotSets.isEmpty()))
        namedAnnotSets.clear();
    if (DEBUG) Out.prln("Document cleanup called");
    if (this.lrPersistentId != null)
      Gate.getCreoleRegister().removeCreoleListener(this);
    if(this.getDataStore() != null)
      this.getDataStore().removeDatastoreListener(this);
  } // cleanup()


  /** Documents are identified by URLs */
  public URL getSourceUrl() { return sourceUrl; }

  /** Set method for the document's URL */
  public void setSourceUrl(URL sourceUrl) {
    this.sourceUrl = sourceUrl;
  } // setSourceUrl

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceUrlOffsets() {
    Long[] sourceUrlOffsets = new Long[2];
    sourceUrlOffsets[0] = sourceUrlStartOffset;
    sourceUrlOffsets[1] = sourceUrlEndOffset;
    return sourceUrlOffsets;
  } // getSourceUrlOffsets

  /**
   * Allow/disallow preserving of the original document content.
   * If is <B>true</B> the original content will be retrieved from
   * the DocumentContent object and preserved as document feature.
   */
  public void setPreserveOriginalContent(Boolean b) {
    preserveOriginalContent = b;
  } // setPreserveOriginalContent

  /** Get the preserving of content status of the Document.
   *
   *  @return whether the Document should preserve it's original content.
   */
  public Boolean getPreserveOriginalContent() {
    return preserveOriginalContent;
  } // getPreserveOriginalContent

  /**
   *  Allow/disallow collecting of repositioning information.
   *  If is <B>true</B> information will be retrieved and preserved
   *  as document feature.<BR>
   *  Preserving of repositioning information give the possibilities
   *  for converting of coordinates between the original document content and
   *  extracted from the document text.
   */
  public void setCollectRepositioningInfo(Boolean b) {
    collectRepositioningInfo = b;
  } // setCollectRepositioningInfo

  /** Get the collectiong and preserving of repositioning information
   *  for the Document. <BR>
   *  Preserving of repositioning information give the possibilities
   *  for converting of coordinates between the original document content and
   *  extracted from the document text.
   *
   *  @return whether the Document should collect and preserve information.
   */
  public Boolean getCollectRepositioningInfo() {
    return collectRepositioningInfo;
  } // getCollectRepositioningInfo

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method gets the
    * start offset.
    */
  public Long getSourceUrlStartOffset() { return sourceUrlStartOffset; }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * start offset.
    */
  public void setSourceUrlStartOffset(Long sourceUrlStartOffset) {
    this.sourceUrlStartOffset = sourceUrlStartOffset;
  } // setSourceUrlStartOffset

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method gets the
    * end offset.
    */
  public Long getSourceUrlEndOffset() { return sourceUrlEndOffset; }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * end offset.
    */
  public void setSourceUrlEndOffset(Long sourceUrlEndOffset) {
    this.sourceUrlEndOffset = sourceUrlEndOffset;
  } // setSourceUrlStartOffset

  /** The content of the document: a String for text; MPEG for video; etc. */
  public DocumentContent getContent() { return content; }

  /** Set method for the document content */
  public void setContent(DocumentContent content) { this.content = content; }

  /** Get the encoding of the document content source */
  public String getEncoding() { return encoding; }

  /** Set the encoding of the document content source */
  public void setEncoding(String encoding) { this.encoding = encoding; }

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() {
    if(defaultAnnots == null){
      defaultAnnots = new AnnotationSetImpl(this);
      fireAnnotationSetAdded(new DocumentEvent(
           this, DocumentEvent.ANNOTATION_SET_ADDED, null));
    }//if
    return defaultAnnots;
  } // getAnnotations()

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    * If the provided name is null then it returns the default annotation set.
    */
  public AnnotationSet getAnnotations(String name) {
    if(name == null) return getAnnotations();
    if(namedAnnotSets == null)
      namedAnnotSets = new HashMap();
    AnnotationSet namedSet = (AnnotationSet) namedAnnotSets.get(name);

    if(namedSet == null) {
      namedSet = new AnnotationSetImpl(this, name);
      namedAnnotSets.put(name, namedSet);

      DocumentEvent evt = new DocumentEvent(
        this, DocumentEvent.ANNOTATION_SET_ADDED, name
      );
      fireAnnotationSetAdded(evt);
    }
    return namedSet;
  } // getAnnotations(name)

  /** Make the document markup-aware. This will trigger the creation
   *  of a DocumentFormat object at Document initialisation time; the
   *  DocumentFormat object will unpack the markup in the Document and
   *  add it as annotations. Documents are <B>not</B> markup-aware by default.
   *
   *  @param b markup awareness status.
   */
  public void setMarkupAware(Boolean newMarkupAware) {
      this.markupAware = newMarkupAware;
  }

  /** Get the markup awareness status of the Document.
   *  <B>Documents are markup-aware by default.</B>
   *  @return whether the Document is markup aware.
   */
  public Boolean getMarkupAware() { return markupAware; }

  /** Returns an XML document aming to preserve the original markups(
    * the original markup will be in the same place and format as it was
    * before processing the document) and include (if possible)
    * the annotations specified in the aSourceAnnotationSet.
    * It is equivalent to toXml(aSourceAnnotationSet, true).
    */
  public String toXml(Set aSourceAnnotationSet){
    return toXml(aSourceAnnotationSet, true);
  }

  /** Returns an XML document aming to preserve the original markups(
    * the original markup will be in the same place and format as it was
    * before processing the document) and include (if possible)
    * the annotations specified in the aSourceAnnotationSet.
    * <b>Warning:</b> Annotations from the aSourceAnnotationSet will be lost
    * if they will cause a crosed over situation.
    * @param aSourceAnnotationSet is an annotation set containing all the
    * annotations that will be combined with the original marup set. If the
    * param is <code>null</code> it will only dump the original markups.
    * @param includeFeatures is a boolean that controls whether the annotation
    * features should be included or not. If false, only the annotation type
    * is included in the tag.
    * @return a string representing an XML document containing the original
    * markup + dumped annotations form the aSourceAnnotationSet
    */
  public String toXml(Set aSourceAnnotationSet, boolean includeFeatures){

    if(hasOriginalContentFeatures()) {
      return saveAnnotationSetAsXmlInOrig(aSourceAnnotationSet,includeFeatures);
    } // if

    AnnotationSet originalMarkupsAnnotSet =
            this.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);

    // Create a dumping annotation set on the document. It will be used for
    // dumping annotations...
    AnnotationSet dumpingSet = new AnnotationSetImpl((Document) this);

    // This set will be constructed inside this method. If is not empty, the
    // annotation contained will be lost.
    if (!dumpingSet.isEmpty()){
      Out.prln("WARNING: The dumping annotation set was not empty."+
      "All annotation it contained were lost.");
      dumpingSet.clear();
    }// End if

    StatusListener sListener = (StatusListener)
                               gate.gui.MainFrame.getListeners().
                               get("gate.event.StatusListener");
    // Construct the dumping set in that way that all annotations will verify
    // the condition that there are not annotations which are crossed.
    // First add all annotation from the original markups
    if(sListener != null)
      sListener.statusChanged("Constructing the dumping annotation set.");
    dumpingSet.addAll(originalMarkupsAnnotSet);
    // Then take all the annotations from aSourceAnnotationSet and verify if
    // they can be inserted safely into the dumpingSet. Where not possible,
    // report.
    if (aSourceAnnotationSet != null){
      Iterator iter = aSourceAnnotationSet.iterator();
      while (iter.hasNext()){
        Annotation currentAnnot = (Annotation) iter.next();
        if(insertsSafety(dumpingSet,currentAnnot)){
          dumpingSet.add(currentAnnot);
        }else if (crossedOverAnnotation != null){
          try {
            Out.prln("Warning: Annotations were found to violate the " +
            "crossed over condition: \n" +
            "1. [" +
            getContent().getContent(
                           crossedOverAnnotation.getStartNode().getOffset(),
                           crossedOverAnnotation.getEndNode().getOffset()) +
            " (" + crossedOverAnnotation.getType() + ": " +
            crossedOverAnnotation.getStartNode().getOffset() +
            ";" + crossedOverAnnotation.getEndNode().getOffset() +
            ")]\n" +
            "2. [" +
            getContent().getContent(
                           currentAnnot.getStartNode().getOffset(),
                           currentAnnot.getEndNode().getOffset()) +
            " (" + currentAnnot.getType() + ": " +
            currentAnnot.getStartNode().getOffset() +
            ";" + currentAnnot.getEndNode().getOffset() +
            ")]\nThe second one will be discarded.\n"  );
          } catch (gate.util.InvalidOffsetException ex) {
            throw new GateRuntimeException(ex.getMessage());
          }
        }// End if
      }// End while
    }// End if

    // The dumpingSet is ready to be exported as XML
    // Here we go.
    if(sListener != null) sListener.statusChanged("Dumping annotations as XML");
    StringBuffer xmlDoc = new StringBuffer(
          DOC_SIZE_MULTIPLICATION_FACTOR*(this.getContent().size().intValue()));
    // Add xml header
//    xmlDoc.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

    // If the annotation set contains this "GatePreserveFormat"
    // type, then this is removed because it will be added in the saving
    // process. The reason of this removal is that if the loaded document
    // was previously loaded from a GatePreserveFormat then we
    // don't want to create lots of annotation for this type. This annotation
    // type should be always the root element of a XML preserving format
    // GATE document.
    FeatureMap docFeatures = this.getFeatures();
    String mimeTypeStr = null;
//    addGatePreserveFormatTag = false;
    if (  docFeatures != null &&
          null != (mimeTypeStr=(String)docFeatures.get("MimeType")) &&
          (
            "text/html".equalsIgnoreCase(mimeTypeStr) ||
            "text/xml".equalsIgnoreCase(mimeTypeStr) ||
            "text/sgml".equalsIgnoreCase(mimeTypeStr)
           )
       ){
          /* don't add the root tag */
    }else{
      // Add the root start element
//      xmlDoc.append("<GatePreserveFormat"+
//                    " xmlns:gate=\"http://www.gate.ac.uk\"" +
//                    " gate:annotMaxId=\"" +
//                    getNextAnnotationId() +
//                    "\">");
//      addGatePreserveFormatTag = true;
    }// End if

    xmlDoc.append(saveAnnotationSetAsXml(dumpingSet, includeFeatures));

//    if (addGatePreserveFormatTag){
//      // Add the root end element
//      xmlDoc.append("</GatePreserveFormat>");
//    }// End if
    if(sListener != null) sListener.statusChanged("Done.");
    return xmlDoc.toString();
  }//End toXml()

  /** This method verifies if aSourceAnnotation can ve inserted safety into the
    * aTargetAnnotSet. Safety means that it doesn't violate the crossed over
    * contition with any annotation from the aTargetAnnotSet.
    * @param aTargetAnnotSet the annotation set to include the aSourceAnnotation
    * @param aSourceAnnotation the annotation to be inserted into the
    * aTargetAnnotSet
    * @return true if the annotation inserts safety, or false otherwise.
    */
  private boolean insertsSafety(AnnotationSet aTargetAnnotSet,
                                                Annotation aSourceAnnotation){

    if (aTargetAnnotSet == null || aSourceAnnotation == null) {
      this.crossedOverAnnotation = null;
      return false;
    }
    if (aSourceAnnotation.getStartNode() == null ||
        aSourceAnnotation.getStartNode().getOffset()== null) {
      this.crossedOverAnnotation = null;
      return false;
    }
    if (aSourceAnnotation.getEndNode() == null ||
        aSourceAnnotation.getEndNode().getOffset()== null) {
      this.crossedOverAnnotation = null;
      return false;
    }

    // Get the start and end offsets
    Long start = aSourceAnnotation.getStartNode().getOffset();
    Long end =   aSourceAnnotation.getEndNode().getOffset();
    // Read aSourceAnnotation offsets long
    long s2 = start.longValue();
    long e2 = end.longValue();

    // Obtain a set with all annotations annotations that overlap
    // totaly or partially with the interval defined by the two provided offsets
    AnnotationSet as = aTargetAnnotSet.get(start,end);

    // Investigate all the annotations from as to see if there is one that
    // comes in conflict with aSourceAnnotation
    Iterator it = as.iterator();
    while(it.hasNext()){
      Annotation ann = (Annotation) it.next();
      // Read ann offsets
      long s1 = ann.getStartNode().getOffset().longValue();
      long e1 = ann.getEndNode().getOffset().longValue();

      if (s1<s2 && s2<e1 && e1<e2) {
        this.crossedOverAnnotation = ann;
        return false;
      }
      if (s2<s1 && s1<e2 && e2<e1) {
        this.crossedOverAnnotation = ann;
        return false;
      }
    }// End while
    return true;
  }// insertsSafety()

  /** This method saves all the annotations from aDumpAnnotSet and combines
    * them with the document content.
    * @param aDumpAnnotationSet is a GATE annotation set prepared to be used
    * on the raw text from document content. If aDumpAnnotSet is <b>null<b>
    * then an empty string will be returned.
    * @param includeFeatures is a boolean, which controls whether the annotation
    * features and gate ID are included or not.
    * @return The XML document obtained from raw text + the information from
    * the dump annotation set.
    */
  private String saveAnnotationSetAsXml(AnnotationSet aDumpAnnotSet,
                                        boolean includeFeatures){
    String content = null;
    if (this.getContent()== null)
      content = new String("");
    else
      content = this.getContent().toString();
    StringBuffer docContStrBuff = filterNonXmlChars(new StringBuffer(content));
    if (aDumpAnnotSet == null)   return docContStrBuff.toString();

    TreeMap offsets2CharsMap = new TreeMap();
    if (this.getContent().size().longValue() != 0){
      // Fill the offsets2CharsMap with all the indices where
      // special chars appear
      buildEntityMapFromString(content,offsets2CharsMap);
    }//End if
    // The saving alghorithm is as follows:
    ///////////////////////////////////////////
    // Construct a set of annot with all IDs in asc order.
    // All annotations that end at that offset swap their place in descending
    // order. For each node write all the tags from left to right.

    // Construct the node set
    TreeSet offsets = new TreeSet();
    Iterator iter = aDumpAnnotSet.iterator();
    while (iter.hasNext()){
      Annotation annot = (Annotation) iter.next();
      offsets.add(annot.getStartNode().getOffset());
      offsets.add(annot.getEndNode().getOffset());
    }// End while
    isRootTag = false;
    // ofsets is sorted in ascending order.
    // Iterate this set in descending order and remove an offset at each
    // iteration
    while (!offsets.isEmpty()){
      Long offset = (Long)offsets.last();
      // Remove the offset from the set
      offsets.remove(offset);
      // Now, use it.
      // Returns a list with annotations that needs to be serialized in that
      // offset.
      List annotations = getAnnotationsForOffset(aDumpAnnotSet,offset);
      // Attention: the annotation are serialized from left to right
      StringBuffer tmpBuff = new StringBuffer("");
      Stack stack = new Stack();
      // Iterate through all these annotations and serialize them
      Iterator it = annotations.iterator();
      while(it.hasNext()){
        Annotation a = (Annotation) it.next();
        it.remove();
        // Test if a Ends at offset
        if ( offset.equals(a.getEndNode().getOffset()) ){
          // Test if a Starts at offset
          if ( offset.equals(a.getStartNode().getOffset()) ){
            // Here, the annotation a Starts and Ends at the offset
            if ( null != a.getFeatures().get("isEmptyAndSpan") &&
                 "true".equals((String)a.getFeatures().get("isEmptyAndSpan"))){

              // Assert: annotation a with start == end and isEmptyAndSpan
              if (offsets.isEmpty() && "".equals(tmpBuff.toString())){
                // a is the doc's root tag to be written
                // The annotations are serialized from left to right.
                // The first annot in the last offset is the ROOT one
                isRootTag = true;
              }// End if
              tmpBuff.append(writeStartTag(a, includeFeatures));
              stack.push(a);
            }else{
              // Assert annotation a with start == end and an empty tag
              tmpBuff.append(writeEmptyTag(a));
              // The annotation is removed from dumped set
              aDumpAnnotSet.remove(a);
            }// End if
          }else{
            // Here the annotation a Ends at the offset.
            // In this case empty the stack and write the end tag
            if (!stack.isEmpty()){
              while(!stack.isEmpty()){
                Annotation a1 = (Annotation)stack.pop();
                tmpBuff.append(writeEndTag(a1));
              }// End while
            }// End if
            tmpBuff.append(writeEndTag(a));
          }// End if
        }else{
          // The annotation a does NOT end at the offset. Let's see if it starts
          // at the offset
          if ( offset.equals(a.getStartNode().getOffset()) ){
            // The annotation a starts at the offset.
            // In this case empty the stack and write the end tag
            if (!stack.isEmpty()){
              while(!stack.isEmpty()){
                Annotation a1 = (Annotation)stack.pop();
                tmpBuff.append(writeEndTag(a1));
              }// End while
            }// End if
            if (offsets.isEmpty() && "".equals(tmpBuff.toString())){
              // a is the last tag to be written
              // The annotations are serialized from left to right.
              // The first annot in the last offset is the ROOT one.
              isRootTag = true;
            }// End if
            tmpBuff.append(writeStartTag(a, includeFeatures));
            // The annotation is removed from dumped set
            aDumpAnnotSet.remove(a);
          }// End if ( offset.equals(a.getStartNode().getOffset()) )
        }// End if ( offset.equals(a.getEndNode().getOffset()) )
      }// End while(it.hasNext()){

      // In this case empty the stack and write the end tag
      if (!stack.isEmpty()){
        while(!stack.isEmpty()){
          Annotation a1 = (Annotation)stack.pop();
          tmpBuff.append(writeEndTag(a1));
        }// End while
      }// End if

      // Before inserting tmpBuff into docContStrBuff we need to check
      // if there are chars to be replaced and if there are, they would be
      // replaced.
      if (!offsets2CharsMap.isEmpty()){
        Integer offsChar = (Integer) offsets2CharsMap.lastKey();
        while( !offsets2CharsMap.isEmpty() &&
                       offsChar.intValue() >= offset.intValue()){
          // Replace the char at offsChar with its corresponding entity form
          // the entitiesMap.
          docContStrBuff.replace(offsChar.intValue(),offsChar.intValue()+1,
          (String)entitiesMap.get((Character)offsets2CharsMap.get(offsChar)));
          // Discard the offsChar after it was used.
          offsets2CharsMap.remove(offsChar);
          // Investigate next offsChar
          if (!offsets2CharsMap.isEmpty())
            offsChar = (Integer) offsets2CharsMap.lastKey();
        }// End while
      }// End if
      // Insert tmpBuff to the location where it belongs in docContStrBuff
      docContStrBuff.insert(offset.intValue(),tmpBuff.toString());
    }// End while(!offsets.isEmpty())
    // Need to replace the entities in the remaining text, if there is any text
    // So, if there are any more items in offsets2CharsMap they need to be
    // replaced
    while (!offsets2CharsMap.isEmpty()){
      Integer offsChar = (Integer) offsets2CharsMap.lastKey();
      // Replace the char with its entity
      docContStrBuff.replace(offsChar.intValue(),offsChar.intValue()+1,
      (String)entitiesMap.get((Character)offsets2CharsMap.get(offsChar)));
      // remove the offset from the map
      offsets2CharsMap.remove(offsChar);
    }// End while
    return docContStrBuff.toString();
  }// saveAnnotationSetAsXml()

  /**
   *  Return true only if the document has features for original content and
   *  repositioning information.
   */
  private boolean hasOriginalContentFeatures() {
    FeatureMap features = getFeatures();
    boolean result = false;

    result =
    (features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME) != null)
      &&
    (features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME)
      != null);

    return result;
  } // hasOriginalContentFeatures

  /** This method saves all the annotations from aDumpAnnotSet and combines
    * them with the original document content, if preserved as feature.
    * @param aDumpAnnotationSet is a GATE annotation set prepared to be used
    * on the raw text from document content. If aDumpAnnotSet is <b>null<b>
    * then an empty string will be returned.
    * @param includeFeatures is a boolean, which controls whether the annotation
    * features and gate ID are included or not.
    * @return The XML document obtained from raw text + the information from
    * the dump annotation set.
    */
  private String saveAnnotationSetAsXmlInOrig(Set aSourceAnnotationSet,
                                        boolean includeFeatures){
    StringBuffer docContStrBuff;

    String origContent;

    origContent =
     (String)features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
    if(origContent == null) {
      origContent = "";
    } // if

    long originalContentSize = origContent.length();

    RepositioningInfo repositioning = (RepositioningInfo)
      getFeatures().get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

    docContStrBuff = new StringBuffer(origContent);
    if (aSourceAnnotationSet == null) return docContStrBuff.toString();

    StatusListener sListener = (StatusListener)
                               gate.gui.MainFrame.getListeners().
                               get("gate.event.StatusListener");

    AnnotationSet originalMarkupsAnnotSet =
            this.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    // Create a dumping annotation set on the document. It will be used for
    // dumping annotations...
    AnnotationSet dumpingSet = new AnnotationSetImpl((Document) this);
    if(sListener != null)
      sListener.statusChanged("Constructing the dumping annotation set.");
    // Then take all the annotations from aSourceAnnotationSet and verify if
    // they can be inserted safely into the dumpingSet. Where not possible,
    // report.
    if (aSourceAnnotationSet != null){
      Iterator iter = aSourceAnnotationSet.iterator();
      Annotation currentAnnot;
      while (iter.hasNext()){
        currentAnnot = (Annotation) iter.next();
        if(insertsSafety(originalMarkupsAnnotSet, currentAnnot)
            && insertsSafety(dumpingSet, currentAnnot)){
          dumpingSet.add(currentAnnot);
        }else{
          Out.prln("Warning: Annotation with ID=" + currentAnnot.getId() +
          ", startOffset=" + currentAnnot.getStartNode().getOffset() +
          ", endOffset=" + currentAnnot.getEndNode().getOffset() +
          ", type=" + currentAnnot.getType()+ " was found to violate the" +
          " crossed over condition. It will be discarded");
        }// End if
      }// End while
    }// End if

    // The dumpingSet is ready to be exported as XML
    // Here we go.
    if(sListener != null) sListener.statusChanged("Dumping annotations as XML");

    ///////////////////////////////////////////
    // Construct a set of annot with all IDs in asc order.
    // All annotations that end at that offset swap their place in descending
    // order. For each node write all the tags from left to right.

    // Construct the node set
    TreeSet offsets = new TreeSet();
    Iterator iter = aSourceAnnotationSet.iterator();
    while (iter.hasNext()){
      Annotation annot = (Annotation) iter.next();
      offsets.add(annot.getStartNode().getOffset());
      offsets.add(annot.getEndNode().getOffset());
    }// End while
    isRootTag = false;

    // ofsets is sorted in ascending order.
    // Iterate this set in descending order and remove an offset at each
    // iteration
    while (!offsets.isEmpty()){
      Long offset = (Long)offsets.last();
      // Remove the offset from the set
      offsets.remove(offset);
      // Now, use it.
      // Returns a list with annotations that needs to be serialized in that
      // offset.
      List annotations = getAnnotationsForOffset(aSourceAnnotationSet,offset);
      // Attention: the annotation are serialized from left to right
      StringBuffer tmpBuff = new StringBuffer("");
      Stack stack = new Stack();
      // Iterate through all these annotations and serialize them
      Iterator it = annotations.iterator();
      Annotation a = null;
      while(it.hasNext()) {
        a = (Annotation) it.next();
        it.remove();
        // Test if a Ends at offset
        if ( offset.equals(a.getEndNode().getOffset()) ){
          // Test if a Starts at offset
          if ( offset.equals(a.getStartNode().getOffset()) ){
            // Here, the annotation a Starts and Ends at the offset
            if ( null != a.getFeatures().get("isEmptyAndSpan") &&
                 "true".equals((String)a.getFeatures().get("isEmptyAndSpan"))){

              // Assert: annotation a with start == end and isEmptyAndSpan
              tmpBuff.append(writeStartTag(a, includeFeatures, false));
              stack.push(a);
            }else{
              // Assert annotation a with start == end and an empty tag
              tmpBuff.append(writeEmptyTag(a, false));
              // The annotation is removed from dumped set
              aSourceAnnotationSet.remove(a);
            }// End if
          }else{
            // Here the annotation a Ends at the offset.
            // In this case empty the stack and write the end tag
            while(!stack.isEmpty()){
              Annotation a1 = (Annotation)stack.pop();
              tmpBuff.append(writeEndTag(a1));
            }// End while
            tmpBuff.append(writeEndTag(a));
          }// End if
        }else{
          // The annotation a does NOT end at the offset. Let's see if it starts
          // at the offset
          if ( offset.equals(a.getStartNode().getOffset()) ){
            // The annotation a starts at the offset.
            // In this case empty the stack and write the end tag
            while(!stack.isEmpty()){
              Annotation a1 = (Annotation)stack.pop();
              tmpBuff.append(writeEndTag(a1));
            }// End while

            tmpBuff.append(writeStartTag(a, includeFeatures, false));
            // The annotation is removed from dumped set
            aSourceAnnotationSet.remove(a);
          }// End if ( offset.equals(a.getStartNode().getOffset()) )
        }// End if ( offset.equals(a.getEndNode().getOffset()) )
      }// End while(it.hasNext()){

      // In this case empty the stack and write the end tag
      while(!stack.isEmpty()){
        Annotation a1 = (Annotation)stack.pop();
        tmpBuff.append(writeEndTag(a1));
      }// End while

      long originalPosition = -1;
      boolean backPositioning =
        a != null && offset.equals(a.getEndNode().getOffset());
      if ( backPositioning ) {
        // end of the annotation correction
        originalPosition =
          repositioning.getOriginalPos(offset.intValue(), true);
      } // if

      if(originalPosition == -1) {
        originalPosition = repositioning.getOriginalPos(offset.intValue());
      } // if

      // Insert tmpBuff to the location where it belongs in docContStrBuff
      if(originalPosition != -1 && originalPosition <= originalContentSize ) {
        docContStrBuff.insert((int) originalPosition, tmpBuff.toString());
      }
      else {
        Out.prln("Error in the repositioning. The offset ("+offset.intValue()
        +") could not be positioned in the original document. \n"
        +"Calculated position is: "+originalPosition
        +" placed back: "+backPositioning);
      } // if

    }// End while(!offsets.isEmpty())

    return docContStrBuff.toString();
  } // saveAnnotationSetAsXml()

  /** This method returns a list with annotations ordered that way that
    * they can be serialized from left to right, at the offset. If one of the
    * params is null then an empty list will be returned.
    * @param aDumpAnnotSet is a set containing all annotations that will be
    * dumped.
    * @param offset represent the offset at witch the annotation must start
    * AND/OR end.
    * @return a list with those annotations that need to be serialized.
    */
  private List getAnnotationsForOffset(Set aDumpAnnotSet, Long offset){
    List annotationList = new LinkedList();
    if (aDumpAnnotSet == null || offset == null) return annotationList;
    Set annotThatStartAtOffset = new TreeSet(
                          new AnnotationComparator(ORDER_ON_END_OFFSET,DESC));
    Set annotThatEndAtOffset = new TreeSet(
                          new AnnotationComparator(ORDER_ON_START_OFFSET,DESC));
    Set annotThatStartAndEndAtOffset = new TreeSet(
                          new AnnotationComparator(ORDER_ON_ANNOT_ID,ASC));

    // Fill these tree lists with annotation tat start, end or start and
    // end at the offset.
    Iterator iter = aDumpAnnotSet.iterator();
    while(iter.hasNext()){
      Annotation ann = (Annotation) iter.next();
      if (offset.equals(ann.getStartNode().getOffset())){
        if (offset.equals(ann.getEndNode().getOffset()))
          annotThatStartAndEndAtOffset.add(ann);
        else
          annotThatStartAtOffset.add(ann);
      }else{
        if (offset.equals(ann.getEndNode().getOffset()))
          annotThatEndAtOffset.add(ann);
      }// End if
    }// End while
    annotationList.addAll(annotThatEndAtOffset);
    annotThatEndAtOffset = null;
    annotationList.addAll(annotThatStartAtOffset);
    annotThatStartAtOffset = null;
    iter = annotThatStartAndEndAtOffset.iterator();
    while(iter.hasNext()){
      Annotation ann = (Annotation) iter.next();
      Iterator it = annotationList.iterator();
      boolean breaked = false;
      while (it.hasNext()){
        Annotation annFromList = (Annotation) it.next();
        if (annFromList.getId().intValue() > ann.getId().intValue()){
          annotationList.add(annotationList.indexOf(annFromList),ann);
          breaked = true;
          break;
        }// End if
      }// End while
      if (!breaked)
        annotationList.add(ann);
      iter.remove();
    }// End while
    return annotationList;
  }// getAnnotationsForOffset()

  private String writeStartTag(Annotation annot, boolean includeFeatures){
    return writeStartTag(annot, includeFeatures, true);
  } // writeStartTag

  /** Returns a string representing a start tag based on the input annot*/
  private String writeStartTag(Annotation annot, boolean includeFeatures,
                                boolean includeNamespace){
    AnnotationSet originalMarkupsAnnotSet =
            this.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);

    StringBuffer strBuff = new StringBuffer("");
    if (annot == null) return strBuff.toString();
//    if (!addGatePreserveFormatTag && isRootTag){
      if (isRootTag){
      //the features are included either if desired or if that's an annotation
      //from the original markup of the document. We don't want for example to
      //spoil all links in an HTML file!
      if (includeFeatures) {
        strBuff.append("<");
        strBuff.append(annot.getType());
        strBuff.append(" ");
        if(includeNamespace) {
          strBuff.append(" xmlns:gate=\"http://www.gate.ac.uk\"");
          strBuff.append(" gate:");
        }
        strBuff.append("gateId=\"");
        strBuff.append(annot.getId());
        strBuff.append("\"");
        strBuff.append(" ");
        if(includeNamespace) {
          strBuff.append("gate:");
        }
        strBuff.append("annotMaxId=\"");
        strBuff.append(getNextAnnotationId());
        strBuff.append("\"");
        strBuff.append(writeFeatures(annot.getFeatures(), includeNamespace));
        strBuff.append(">");
      }
      else if (originalMarkupsAnnotSet.contains(annot)) {
          strBuff.append("<");
          strBuff.append(annot.getType());
          strBuff.append(writeFeatures(annot.getFeatures(), includeNamespace));
          strBuff.append(">");
        }
      else {
        strBuff.append("<");
        strBuff.append(annot.getType());
        strBuff.append(">");
      }
      // Once the root tag was writen then there will be no other Root tag
      isRootTag = false;
    }else{
      //the features are included either if desired or if that's an annotation
      //from the original markup of the document. We don't want for example to
      //spoil all links in an HTML file!
      if (includeFeatures) {
        strBuff.append("<");
        strBuff.append(annot.getType());
        strBuff.append(" ");
        if(includeNamespace) {
          strBuff.append("gate:");
        } // if includeNamespaces
        strBuff.append("gateId=\"");
        strBuff.append(annot.getId());
        strBuff.append("\"");
        strBuff.append(writeFeatures(annot.getFeatures(), includeNamespace));
        strBuff.append(">");
      }
      else if (originalMarkupsAnnotSet.contains(annot)) {
        strBuff.append("<");
        strBuff.append(annot.getType());
        strBuff.append(writeFeatures(annot.getFeatures(), includeNamespace));
        strBuff.append(">");
      }
      else {
        strBuff.append("<");
        strBuff.append(annot.getType());
        strBuff.append(">");
      }
    }// End if
    return strBuff.toString();
  }// writeStartTag()

  /** This method takes aScanString and searches for those chars from
    * entitiesMap that appear in the string. A tree map(offset2Char) is filled
    * using as key the offsets where those Chars appear and the Char.
    * If one of the params is null the method simply returns.
    */
  private void buildEntityMapFromString(String aScanString, TreeMap aMapToFill){
    if (aScanString == null || aMapToFill == null) return;
    if (entitiesMap == null || entitiesMap.isEmpty()){
      Err.prln("WARNING: Entities map was not initialised !");
      return;
    }// End if
    // Fill the Map with the offsets of the special chars
    Iterator entitiesMapIterator = entitiesMap.keySet().iterator();
    while(entitiesMapIterator.hasNext()){
      Character c = (Character) entitiesMapIterator.next();
      int fromIndex = 0;
      while (-1 != fromIndex){
        fromIndex = aScanString.indexOf(c.charValue(),fromIndex);
        if (-1 != fromIndex){
          aMapToFill.put(new Integer(fromIndex),c);
          fromIndex ++;
        }// End if
      }// End while
    }// End while
  }//buildEntityMapFromString();

  private String writeEmptyTag(Annotation annot){
    return writeEmptyTag(annot, true);
  } // writeEmptyTag

  /** Returns a string representing an empty tag based on the input annot*/
  private String writeEmptyTag(Annotation annot, boolean includeNamespace){
    StringBuffer strBuff = new StringBuffer("");
    if (annot == null) return strBuff.toString();

    strBuff.append("<");
    strBuff.append(annot.getType());

    AnnotationSet originalMarkupsAnnotSet =
            this.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    if (! originalMarkupsAnnotSet.contains(annot)) {
      strBuff.append(" gateId=\"");
      strBuff.append(annot.getId());
      strBuff.append("\"");
    }
    strBuff.append(writeFeatures(annot.getFeatures(),includeNamespace));
    strBuff.append("/>");

    return strBuff.toString();
  }// writeEmptyTag()

  /** Returns a string representing an end tag based on the input annot*/
  private String writeEndTag(Annotation annot){
    StringBuffer strBuff = new StringBuffer("");
    if (annot == null) return strBuff.toString();
/*
    if (annot.getType().indexOf(" ") != -1)
      Out.prln("Warning: Truncating end tag to first word for annot type \""
      +annot.getType()+ "\". ");
*/
    strBuff.append("</"+annot.getType()+">");
    return strBuff.toString();
  }// writeEndTag()

  /** Returns a string representing a FeatureMap serialized as XML attributes*/
  private String writeFeatures(FeatureMap feat, boolean includeNamespace){
    StringBuffer strBuff = new StringBuffer("");
    if (feat == null) return strBuff.toString();
    Iterator it = feat.keySet().iterator();
    while (it.hasNext()){
      Object key = it.next();
      Object value = feat.get(key);
      if ( (key != null) && (value != null) ){
        // Eliminate a feature inserted at reading time and which help to
        // take some decissions at saving time
        if ("isEmptyAndSpan".equals(key.toString()))
          continue;
        if( !(String.class.isAssignableFrom(key.getClass()) ||
              Number.class.isAssignableFrom(key.getClass()))){

            Out.prln("Warning:Found a feature NAME("+key+") that doesn't came"+
                             " from String or Number.(feature discarded)");
            continue;
        }// End if
        if ( !(String.class.isAssignableFrom(value.getClass()) ||
               Number.class.isAssignableFrom(value.getClass()) ||
               java.util.Collection.class.isAssignableFrom(value.getClass()))){

            Out.prln("Warning:Found a feature VALUE("+value+") that doesn't came"+
                       " from String, Number or Collection.(feature discarded)");
            continue;
        }// End if
        if ("matches".equals(key)) {
          strBuff.append(" ");
          if(includeNamespace) {
            strBuff.append("gate:");
          }
          strBuff.append(key);
          strBuff.append("=\"");
        }
        else {
          strBuff.append(" ");
          strBuff.append(key);
          strBuff.append("=\"");
        }
        if (java.util.Collection.class.isAssignableFrom(value.getClass())){
          Iterator valueIter = ((Collection)value).iterator();
          while(valueIter.hasNext()){
            Object item = valueIter.next();
            if (!(String.class.isAssignableFrom(item.getClass()) ||
                  Number.class.isAssignableFrom(item.getClass())))
                  continue;
            strBuff.append(item);
            strBuff.append(";");
          }// End while
          if (strBuff.charAt(strBuff.length()-1) == ';')
            strBuff.deleteCharAt(strBuff.length()-1);
        }else{
          strBuff.append(value);
        }// End if
        strBuff.append("\"");
      }// End if
    }// End while
    return strBuff.toString();
  }// writeFeatures()

  /** Returns a GateXml document that is a custom XML format for wich there is
    * a reader inside GATE called gate.xml.GateFormatXmlHandler.
    * What it does is to serialize a GATE document in an XML format.
    * @return a string representing a Gate Xml document. If saved in a file,this
    * string must be written using the UTF-8 encoding because the first line
    * in the generated xml document is <?xml version="1.0" encoding="UTF-8" ?>
    */
  public String toXml(){
    // Initialize the xmlContent with 3 time the size of the current document.
    // This is because of the tags size. This measure is made to increase the
    // performance of StringBuffer.
    StringBuffer xmlContent = new StringBuffer(
         DOC_SIZE_MULTIPLICATION_FACTOR*(getContent().size().intValue()));
    // Add xml header
    xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
    // Add the root element
    xmlContent.append("<GateDocument>\n");
    xmlContent.append("<!-- The document's features-->\n\n");
    xmlContent.append("<GateDocumentFeatures>\n");

    xmlContent.append(featuresToXml(this.getFeatures()));
    xmlContent.append("</GateDocumentFeatures>\n");
    xmlContent.append("<!-- The document content area with serialized"+
                      " nodes -->\n\n");
    // Add plain text element
    xmlContent.append("<TextWithNodes>");
    xmlContent.append(textWithNodes(this.getContent().toString()));
    xmlContent.append("</TextWithNodes>\n");
    // Serialize as XML all document's annotation sets
    // Serialize the default AnnotationSet
    StatusListener sListener = (StatusListener)
                               gate.gui.MainFrame.getListeners().
                               get("gate.event.StatusListener");
    if(sListener != null)
      sListener.statusChanged("Saving the default annotation set ");
    xmlContent.append("<!-- The default annotation set -->\n\n");
    xmlContent.append(annotationSetToXml(this.getAnnotations()));
    // Serialize all others AnnotationSets
    // namedAnnotSets is a Map containing all other named Annotation Sets.
    if (namedAnnotSets != null){
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext()){
        AnnotationSet annotSet = (AnnotationSet) iter.next();
        xmlContent.append("<!-- Named annotation set -->\n\n");
        // Serialize it as XML
        if(sListener != null) sListener.statusChanged("Saving " +
                                                      annotSet.getName()+
                                                      " annotation set ");
        xmlContent.append(annotationSetToXml(annotSet));
      }// End while
    }// End if
    // Add the end of GateDocument
    xmlContent.append("</GateDocument>");
    if(sListener != null) sListener.statusChanged("Done !");
    // return the XmlGateDocument
    return xmlContent.toString();
  }// toXml

  /** This method filters any non XML char
    * see: http://www.w3c.org/TR/2000/REC-xml-20001006#charsets
    * All non XML chars will be replaced with 0x20 (space char) This assures
    * that the next time the document is loaded there won't be any problems.
    * @param aStrBuffer represents the input String that is filtred. If the
    * aStrBuffer is null then an empty string will be returend
    * @return the "purified" StringBuffer version of the aStrBuffer
    */
  private StringBuffer filterNonXmlChars(StringBuffer aStrBuffer){
    if (aStrBuffer == null) return new StringBuffer("");
    String space = new String(" ");
    for (int i=aStrBuffer.length()-1;i>=0; i--){
      if (!isXmlChar(aStrBuffer.charAt(i)))
        aStrBuffer.replace(i,i+1,space);
    }// End for
    return aStrBuffer;
  }// filterNonXmlChars()

  /** This method decide if a char is a valid XML one or not
    * @param ch the char to be tested
    * @return true if is a valid XML char and fals if is not.
    */
  public static boolean isXmlChar(char ch){
    if (ch == 0x9 || ch == 0xA || ch ==0xD) return true;
    if ((0x20 <= ch) && (ch <= 0xD7FF)) return true;
    if ((0xE000 <= ch) && (ch <= 0xFFFD)) return true;
    if ((0x10000 <= ch) && (ch <= 0x10FFFF)) return true;
    return false;
  }// End isXmlChar()

  /** This method saves a FeatureMap as XML elements.
    * @ param aFeatureMap the feature map that has to be saved as XML.
    * @ return a String like this: <Feature><Name>...</Name>
    * <Value>...</Value></Feature><Feature>...</Feature>
    */
  private String featuresToXml(FeatureMap aFeatureMap){
    StringBuffer str = new StringBuffer("");

    if (aFeatureMap == null) return str.toString();

    Set keySet = aFeatureMap.keySet();
    Iterator keyIterator = keySet.iterator();
    while(keyIterator.hasNext()){
      Object key = keyIterator.next();
      Object value = aFeatureMap.get(key);
      if ((key != null) && (value != null)){
        String keyClassName = null;
        String keyItemClassName = null;
        String valueClassName = null;
        String valueItemClassName = null;
        String key2String = key.toString();
        String value2String = value.toString();

        Object item = null;
        // Test key if it is String, Number or Collection
        if (key instanceof java.lang.String ||
            key instanceof java.lang.Number ||
            key instanceof java.util.Collection)
          keyClassName = key.getClass().getName();

        // Test value if it is String, Number or Collection
        if (value instanceof java.lang.String ||
            value instanceof java.lang.Number ||
            value instanceof java.util.Collection)
          valueClassName = value.getClass().getName();

        // Features and values that are not Strings, Numbers or collections
        // will be discarded.
        if (keyClassName == null || valueClassName == null) continue;

        // If key is collection serialize the colection in a specific format
        if (key instanceof java.util.Collection){
          StringBuffer keyStrBuff = new StringBuffer("");
          Iterator iter = ((Collection) key).iterator();
          if (iter.hasNext()){
            item = iter.next();
            if (item instanceof java.lang.Number)
              keyItemClassName = item.getClass().getName();
            else
              keyItemClassName = String.class.getName();
            keyStrBuff.append(item.toString());
          }// End if
          while (iter.hasNext()){
            item = iter.next();
            keyStrBuff.append(";" + item.toString());
          }// End while
          key2String = keyStrBuff.toString();
        }// End if
        // If key is collection serialize the colection in a specific format
        if (value instanceof java.util.Collection){
          StringBuffer valueStrBuff = new StringBuffer("");
          Iterator iter = ((Collection) value).iterator();
          if (iter.hasNext()){
            item = iter.next();
            if (item instanceof java.lang.Number)
              valueItemClassName = item.getClass().getName();
            else
              valueItemClassName = String.class.getName();
            valueStrBuff.append(item.toString());
          }// End if
          while (iter.hasNext()){
            item = iter.next();
            valueStrBuff.append(";" + item.toString());
          }// End while
          value2String = valueStrBuff.toString();
        }// End if
        str.append("<Feature>\n  <Name");
        if (keyClassName != null)
          str.append(" className=\""+keyClassName+"\"");
        if (keyItemClassName != null)
          str.append(" itemClassName=\""+keyItemClassName+"\"");
        str.append(">");
        str.append(filterNonXmlChars(replaceCharsWithEntities(key2String)));
        str.append("</Name>\n  <Value");
        if (valueClassName != null)
          str.append(" className=\"" + valueClassName + "\"");
        if (valueItemClassName != null)
          str.append(" itemClassName=\"" + valueItemClassName + "\"");
        str.append(">");
        str.append(filterNonXmlChars(replaceCharsWithEntities(value2String)));
        str.append("</Value>\n</Feature>\n");
      }// End if
    }// end While
    return str.toString();
  }//featuresToXml

  /** This method replace all chars that appears in the anInputString and also
    * that are in the entitiesMap with their corresponding entity
    * @param anInputString the string analyzed. If it is null then returns the
    *  empty string
    * @return a string representing the input string with chars replaced with
    *  entities
    */
  private StringBuffer replaceCharsWithEntities(String anInputString){
    if (anInputString == null) return new StringBuffer("");
    StringBuffer strBuff = new StringBuffer(anInputString);
    for (int i=strBuff.length()-1; i>=0; i--){
      Character ch = new Character(strBuff.charAt(i));
      if (entitiesMap.keySet().contains(ch)){
        strBuff.replace(i,i+1,(String) entitiesMap.get(ch));
      }// End if
    }// End for
    return strBuff;
  }//replaceCharsWithEntities()

  /** This method creates Node XML elements and inserts them at the
    * corresponding offset inside the text. Nodes are created from the default
    * annotation set, as well as from all existing named annotation sets.
    * @param aText The text representing the document's plain text.
    * @return The text with empty <Node id="NodeId"/> elements.
    */
  private String textWithNodes(String aText){
    if (aText == null) return new String("");
    StringBuffer textWithNodes = filterNonXmlChars(new StringBuffer(aText));

    // Construct a map from offsets to Chars
    TreeMap offsets2CharsMap = new TreeMap();
    if (aText.length()!= 0){
      // Fill the offsets2CharsMap with all the indices where special chars appear
      buildEntityMapFromString(aText,offsets2CharsMap);
    }//End if
    // Construct the offsetsSet for all nodes belonging to this document
    TreeSet offsetsSet = new TreeSet();
    Iterator annotSetIter = this.getAnnotations().iterator();
    while (annotSetIter.hasNext()){
      Annotation annot = (Annotation) annotSetIter.next();
      offsetsSet.add(annot.getStartNode().getOffset());
      offsetsSet.add(annot.getEndNode().getOffset());
    }// end While
    // Get the nodes from all other named annotation sets.
    if (namedAnnotSets != null){
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext()){
        AnnotationSet annotSet = (AnnotationSet) iter.next();
        Iterator iter2 = annotSet.iterator();
        while(iter2.hasNext()){
          Annotation annotTmp = (Annotation) iter2.next();
          offsetsSet.add(annotTmp.getStartNode().getOffset());
          offsetsSet.add(annotTmp.getEndNode().getOffset());
        }// End while
      }// End while
    }// End if
    // offsetsSet is ordered in ascending order because the structure
    // is a TreeSet

    if (offsetsSet.isEmpty()){
      return replaceCharsWithEntities(aText).toString();
    }// End if
    // Iterate through all nodes from anAnnotSet and transform them to
    // XML elements. Then insert those elements at the node's offset into the
    // textWithNodes .
    while (!offsetsSet.isEmpty()){
      Long offset = (Long) offsetsSet.last();
      // Eliminate the offset from the list in order to create more memory space
      offsetsSet.remove(offset);
      // Use offset
      int offsetValue = offset.intValue();
      String strNode = "<Node id=\"" + offsetValue + "\"/>";
      // Before inserting this string into the textWithNodes, check to see if
      // there are any chars to be replaced with their corresponding entities
      if (!offsets2CharsMap.isEmpty()){
        Integer offsChar = (Integer) offsets2CharsMap.lastKey();
        while( !offsets2CharsMap.isEmpty() &&
                       offsChar.intValue() >= offset.intValue()){
          // Replace the char at offsChar with its corresponding entity form
          // the entitiesMap.
          textWithNodes.replace(offsChar.intValue(),offsChar.intValue()+1,
          (String)entitiesMap.get((Character)offsets2CharsMap.get(offsChar)));
          // Discard the offsChar after it was used because this offset will
          // never appear again
          offsets2CharsMap.remove(offsChar);
          // Investigate next offsChar
          if (!offsets2CharsMap.isEmpty())
            offsChar = (Integer) offsets2CharsMap.lastKey();
        }// End while
      }// End if
      // Now it is safe to insert the node
      textWithNodes.insert(offsetValue,strNode);
    }// end while
    // Need to replace the entities in the remaining text, if there is any text
    // So, if there are any more items in offsets2CharsMap they need to be
    // replaced
    while (!offsets2CharsMap.isEmpty()){
      Integer offsChar = (Integer) offsets2CharsMap.lastKey();
      // Replace the char with its entity
      textWithNodes.replace(offsChar.intValue(),offsChar.intValue()+1,
      (String)entitiesMap.get((Character)offsets2CharsMap.get(offsChar)));
      // remove the offset from the map
      offsets2CharsMap.remove(offsChar);
    }// End while
    return textWithNodes.toString();
  }//textWithNodes()

  /** This method saves an AnnotationSet as XML.
    * @param anAnnotationSet The annotation set that has to be saved as XML.
    * @return a String like this: <AnnotationSet> <Annotation>....
    * </AnnotationSet>
    */
  private String annotationSetToXml(AnnotationSet anAnnotationSet){
    StringBuffer str = new StringBuffer("");

    if (anAnnotationSet == null){
      str.append("<AnnotationSet>\n");
      str.append("</AnnotationSet>\n");
      return str.toString();
    }// End if
    if (anAnnotationSet.getName() == null)
      str.append("<AnnotationSet>\n");
    else str.append("<AnnotationSet Name=\"" + anAnnotationSet.getName()+
                                                                    "\" >\n");
    // Iterate through AnnotationSet and save each Annotation as XML
    Iterator iterator = anAnnotationSet.iterator();
    while (iterator.hasNext()){
      Annotation annot = (Annotation) iterator.next();
      str.append("<Annotation " + "Type=\"" + annot.getType() +
                  "\" StartNode=\"" + annot.getStartNode().getOffset() +
                   "\" EndNode=\"" + annot.getEndNode().getOffset() + "\">\n");
      str.append(featuresToXml(annot.getFeatures()));
      str.append("</Annotation>\n");
    }// End while

    str.append("</AnnotationSet>\n");
    return str.toString();
  }// annotationSetToXml

  /** Returns a map with the named annotation sets. It returns <code>null</code>
   *  if no named annotaton set exists. */
  public Map getNamedAnnotationSets() {
    return namedAnnotSets;
  } // getNamedAnnotationSets

  /**
   * Removes one of the named annotation sets.
   * Note that the default annotation set cannot be removed.
   * @param name the name of the annotation set to be removed
   */
  public void removeAnnotationSet(String name){
    Object removed = namedAnnotSets.remove(name);
    if(removed != null){
      fireAnnotationSetRemoved(
        new DocumentEvent(this, DocumentEvent.ANNOTATION_SET_REMOVED, name));
    }
  }

  /** Propagate edit changes to the document content and annotations. */
  public void edit(Long start, Long end, DocumentContent replacement)
    throws InvalidOffsetException
  {
    if(! isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    if(content != null)
      ((DocumentContentImpl) content).edit(start, end, replacement);

    if(defaultAnnots != null)
      ((AnnotationSetImpl) defaultAnnots).edit(start, end, replacement);

    if(namedAnnotSets != null) {
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext())
        ((AnnotationSetImpl) iter.next()).edit(start, end, replacement);
    }

  } // edit(start,end,replacement)

  /** Check that an offset is valid, i.e. it is non-null, greater than
    * or equal to 0 and less than the size of the document content.
    */
  public boolean isValidOffset(Long offset) {
    if(offset == null)
      return false;

    long o = offset.longValue();
    if(o > getContent().size().longValue() || o < 0)
      return false;

    return true;
  } // isValidOffset

  /** Check that both start and end are valid offsets and that
    * they constitute a valid offset range, i.e. start is greater
    * than or equal to long.
    */
  public boolean isValidOffsetRange(Long start, Long end) {
    return
      isValidOffset(start) && isValidOffset(end) &&
      start.longValue() <= end.longValue();
  } // isValidOffsetRange(start,end)

  /** Sets the nextAnnotationId */
  public void setNextAnnotationId(int aNextAnnotationId){
    nextAnnotationId = aNextAnnotationId;
  }// setNextAnnotationId();

  /** Generate and return the next annotation ID */
  public Integer getNextAnnotationId() {
    return new Integer(nextAnnotationId++);
  } // getNextAnnotationId

  /** Generate and return the next node ID */
  public Integer getNextNodeId() { return new Integer(nextNodeId++); }

  /** Ordering based on URL.toString() and the URL offsets (if any) */
  public int compareTo(Object o) throws ClassCastException {
    DocumentImpl other = (DocumentImpl) o;
    return getOrderingString().compareTo(other.getOrderingString());
  } // compareTo

  /** Utility method to produce a string for comparison in ordering.
    * String is based on the source URL and offsets.
    */
  protected String getOrderingString() {
    if(sourceUrl == null) return toString();

    StringBuffer orderingString = new StringBuffer(sourceUrl.toString());
    if(sourceUrlStartOffset != null && sourceUrlEndOffset != null) {
      orderingString.append(sourceUrlStartOffset.toString());
      orderingString.append(sourceUrlEndOffset.toString());
    }

    return orderingString.toString();
  } // getOrderingString()

  /** The id of the next new annotation */
  protected int nextAnnotationId = 0;

  /** The id of the next new node */
  protected int nextNodeId = 0;
  /** The source URL */
  protected URL sourceUrl;

  /** The document's URL name. */

  /** The content of the document */
  protected DocumentContent content;

  /** The encoding of the source of the document content */
  protected String encoding = "UTF-8";

  // Data needed in toXml(AnnotationSet) methos

  /** This field indicates whether or not to add the tag
    * called GatePreserveFormat to the document. HTML, XML, SGML docs won't
    * have this tag added
    */
//  private boolean addGatePreserveFormatTag = false;

  /** This field indicates if an annotation is the doc's root tag.
    * It is needed when adding the namespace information
    */
  private boolean isRootTag = false;

  /** This field is used when creating StringBuffers for toXml() methods.
    * The size of the StringBuffer will be docDonctent.size() multiplied by this
    * value. It is aimed to improve the performance of StringBuffer
    */
  private final int DOC_SIZE_MULTIPLICATION_FACTOR = 1;

  /** Constant used in the inner class AnnotationComparator to order
    * annotations on their start offset
    */
  private final int ORDER_ON_START_OFFSET = 0;
  /** Constant used in the inner class AnnotationComparator to order
    * annotations on their end offset
    */
  private final int ORDER_ON_END_OFFSET = 1;
  /** Constant used in the inner class AnnotationComparator to order
    * annotations on their ID
    */
  private final int ORDER_ON_ANNOT_ID = 2;
  /** Constant used in the inner class AnnotationComparator to order
    * annotations ascending
    */
  private final int ASC = 3;
  /** Constant used in the inner class AnnotationComparator to order
    * annotations descending
    */
  private final int DESC = -3;

  /** A map initialized in init() containing entities that needs to be
    * replaced in strings
    */
  private static Map entitiesMap = null;
  // Initialize the entities map use when saving as xml
  static{
    entitiesMap = new HashMap();
    entitiesMap.put(new Character('<'),"&lt;");
    entitiesMap.put(new Character('>'),"&gt;");
    entitiesMap.put(new Character('&'),"&amp;");
    entitiesMap.put(new Character('\''),"&apos;");
    entitiesMap.put(new Character('"'),"&quot;");
    entitiesMap.put(new Character((char)160),"&#160;");
    entitiesMap.put(new Character((char)169),"&#169;");
  }//static

  /** The range that the content comes from at the source URL
    * (or null if none).
    */
  //protected Long[] sourceUrlOffsets;

  /** The start of the range that the content comes from at the source URL
    * (or null if none).
    */
  protected Long sourceUrlStartOffset;

  /** The end of the range that the content comes from at the source URL
    * (or null if none).
    */
  protected Long sourceUrlEndOffset;

  /** The default annotation set */
  protected AnnotationSet defaultAnnots;

  /** Named sets of annotations */
  protected Map namedAnnotSets;

  /**
   * A property of the document that will be set when the user
   * wants to create the document from a string, as opposed to from
   * a URL.
   */
  private String stringContent;

  /**
   * The stringContent of a document is
   * a property of the document that will be set when the user
   * wants to create the document from a string, as opposed to from
   * a URL.
   * <B>Use the <TT>getContent</TT> method instead to get the actual document
   * content.</B>
   */
  public String getStringContent() { return stringContent; }

  /**
   * The stringContent of a document is
   * a property of the document that will be set when the user
   * wants to create the document from a string, as opposed to from
   * a URL.
   * <B>Use the <TT>setContent</TT> method instead to update the actual
   * document content.</B>
   */
  public void setStringContent(String stringContent) {
    this.stringContent = stringContent;
  } // set StringContent

  /** Is the document markup-aware? */
  protected Boolean markupAware = new Boolean(false);

  /** Check: test 2 objects for equality */
  protected boolean check(Object a, Object b) {
    if( (a == null || b == null) )
      return a == b;

    return a.equals(b);
  } // check(a,b)

  /** Equals */
  public boolean equals(Object other) {
    if(other == null ||
       !(other instanceof DocumentImpl))return false;
    DocumentImpl doc = (DocumentImpl) other;

// PENDING EQUALS IMPLS
    if(! check(content, doc.content)) return false;
    if(! check(defaultAnnots, doc.defaultAnnots)) return false;
    if(! check(encoding, doc.encoding)) return false;
    if(! check(features, doc.features)) return false;
    if(!markupAware.equals(doc.markupAware)) return false;
    if(! check(namedAnnotSets, doc.namedAnnotSets)) return false;
    if(nextAnnotationId != doc.nextAnnotationId) return false;
    if(nextNodeId != doc.nextNodeId) return false;
    if(! check(sourceUrl, doc.sourceUrl)) return false;
    if(! check(sourceUrlStartOffset, doc.sourceUrlStartOffset)) return false;
    if(! check(sourceUrlEndOffset, doc.sourceUrlEndOffset)) return false;

    return true;
  } // equals

  /** Hash code */
  public int hashCode() {
    int code = getContent().hashCode();
    int memberCode = (defaultAnnots == null) ? 0 : defaultAnnots.hashCode();
    code += memberCode;
    memberCode = (encoding == null) ? 0 : encoding.hashCode();
    code += memberCode;
    memberCode = (features == null) ? 0 : features.hashCode();
    code += memberCode;
    code += (markupAware.booleanValue()) ? 0 : 1;
    memberCode = (namedAnnotSets == null) ? 0 : namedAnnotSets.hashCode();
    code += memberCode;
    code += nextAnnotationId;
    code += nextNodeId;
    memberCode = (sourceUrl == null) ? 0 : sourceUrl.hashCode();
    code += memberCode;
    memberCode =
      (sourceUrlStartOffset == null) ? 0 : sourceUrlStartOffset.hashCode();
    code += memberCode;
    memberCode =
      (sourceUrlEndOffset == null) ? 0 : sourceUrlEndOffset.hashCode();
    code += memberCode;
    return code;
  } // hashcode

  /** String respresentation */
  public String toString() {
    String n = Strings.getNl();
    StringBuffer s = new StringBuffer("DocumentImpl: " + n);
    s.append("  content:" + content + n);
    s.append("  defaultAnnots:" + defaultAnnots + n);
    s.append("  encoding:" + encoding + n);
    s.append("  features:" + features + n);
    s.append("  markupAware:" + markupAware + n);
    s.append("  namedAnnotSets:" + namedAnnotSets + n);
    s.append("  nextAnnotationId:" + nextAnnotationId + n);
    s.append("  nextNodeId:" + nextNodeId + n);
    s.append("  sourceUrl:" + sourceUrl + n);
    s.append("  sourceUrlStartOffset:" + sourceUrlStartOffset + n);
    s.append("  sourceUrlEndOffset:" + sourceUrlEndOffset + n);
    s.append(n);

    return s.toString();
  } // toString

   /** Freeze the serialization UID. */
  static final long serialVersionUID = -8456893608311510260L;

  /** Inner class needed to compare annotations*/
  class AnnotationComparator implements java.util.Comparator {
    int orderOn = -1;
    int orderType = ASC;
    /** Constructs a comparator according to one of three sorter types:
      * ORDER_ON_ANNOT_TYPE, ORDER_ON_END_OFFSET, ORDER_ON_START_OFFSET
      */
      public AnnotationComparator(int anOrderOn, int anOrderType){
        orderOn = anOrderOn;
        orderType = anOrderType;
      }// AnnotationComparator()

      /**This method must be implemented according to Comparator interface */
      public int compare(Object o1, Object o2){
        Annotation a1 = (Annotation) o1;
        Annotation a2 = (Annotation) o2;
        // ORDER_ON_START_OFFSET ?
        if (orderOn == ORDER_ON_START_OFFSET){
          int result = a1.getStartNode().getOffset().compareTo(
                                                a2.getStartNode().getOffset());
          if (orderType == ASC){
            // ASC
            // If they are equal then their ID will decide.
            if (result == 0)
              return a1.getId().compareTo(a2.getId());
            return result;
          }else{
            // DESC
            if (result == 0)
              return - (a1.getId().compareTo(a2.getId()));
            return -result;
          }// End if (orderType == ASC)
        }// End if (orderOn == ORDER_ON_START_OFFSET)

        // ORDER_ON_END_OFFSET ?
        if (orderOn == ORDER_ON_END_OFFSET){
          int result = a1.getEndNode().getOffset().compareTo(
                                                a2.getEndNode().getOffset());
          if (orderType == ASC){
            // ASC
            // If they are equal then their ID will decide.
            if (result == 0)
              return - (a1.getId().compareTo(a2.getId()));
            return result;
          }else{
            // DESC
            // If they are equal then their ID will decide.
            if (result == 0)
              return a1.getId().compareTo(a2.getId());
            return - result;
          }// End if (orderType == ASC)
        }// End if (orderOn == ORDER_ON_END_OFFSET)

        // ORDER_ON_ANNOT_ID ?
        if (orderOn == ORDER_ON_ANNOT_ID){
          if (orderType == ASC)
            return a1.getId().compareTo(a2.getId());
          else
            return -(a1.getId().compareTo(a2.getId()));
        }// End if
        return 0;
      }//compare()
  } // End inner class AnnotationComparator


  private transient Vector documentListeners;
  private transient Vector gateListeners;

  public synchronized void removeDocumentListener(DocumentListener l) {
    if (documentListeners != null && documentListeners.contains(l)) {
      Vector v = (Vector) documentListeners.clone();
      v.removeElement(l);
      documentListeners = v;
    }
  }
  public synchronized void addDocumentListener(DocumentListener l) {
    Vector v = documentListeners == null ? new Vector(2) : (Vector) documentListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      documentListeners = v;
    }
  }
  protected void fireAnnotationSetAdded(DocumentEvent e) {
    if (documentListeners != null) {
      Vector listeners = documentListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DocumentListener) listeners.elementAt(i)).annotationSetAdded(e);
      }
    }
  }
  protected void fireAnnotationSetRemoved(DocumentEvent e) {
    if (documentListeners != null) {
      Vector listeners = documentListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DocumentListener) listeners.elementAt(i)).annotationSetRemoved(e);
      }
    }
  }
  public void resourceLoaded(CreoleEvent e) {
  }
  public void resourceUnloaded(CreoleEvent e) {
  }
  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
  }
  public void datastoreClosed(CreoleEvent e) {
    if (! e.getDatastore().equals(this.getDataStore()))
      return;
    //close this lr, since it cannot stay open when the DS it comes from
    //is closed
    Factory.deleteResource(this);
  }
  public void setLRPersistenceId(Object lrID) {
    super.setLRPersistenceId( lrID);
    //make persistent documents listen to the creole register
    //for events about their DS
    Gate.getCreoleRegister().addCreoleListener(this);
  }
  public void resourceAdopted(DatastoreEvent evt) {
  }
  public void resourceDeleted(DatastoreEvent evt) {
    if(! evt.getSource().equals(this.getDataStore()))
      return;
    //if an open document is deleted from a DS, then
    //it must close itself immediately, as is no longer valid
    if(evt.getResourceID().equals(this.getLRPersistenceId()))
      Factory.deleteResource(this);
  }
  public void resourceWritten(DatastoreEvent evt) {
  }
  public void setDataStore(DataStore dataStore) throws gate.persist.PersistenceException {
    super.setDataStore( dataStore);
    if (this.dataStore != null)
      this.dataStore.addDatastoreListener(this);
  }

} // class DocumentImpl
