/*TextualDocumentImpl.java
*@author Valentin Tablan
*24.01.2000
*/
package  gate.impl.ag;


import  gate.*;
import gate.util.*;


public class TextualDocumentImpl
    extends gate.impl.ag.DocumentImpl
    implements gate.TextualDocument
{

    /**Constructor.
    *@param id the id for the new TextualDocument;
    *content the initial content for the document.
    */
    public TextualDocumentImpl (Long id, String content) {
        super(id);
        this.content = new String(content); //make a copy to be on the safe side...
    }


    /** The contents of the document */
    public String getCurrentContent () {
        return  "";
    }


    /** The contents overspanned by a given annotation
    *@param a the annotation for wich the content should be returned.
    */
    public String getContentOf (Annotation a) {
        gate.Node start = a.getStartNode();
        gate.Node end = a.getEndNode();

        try {
            return  (String)getContent(start.getOffset().doubleValue(), end.getOffset().doubleValue());
        }
        catch (gate.util.InvalidOffsetException e) {
            e.printStackTrace(System.err);
        }

        return  null;
    }

    /*The length of the document's content.
    */
    public double getLength () {
        return  content.length();
    }

    /*The content of the entire document.
    *The result will be a String.
    */
    public Object getContent () {
        return  content;
    }

    /*Returns the content of a given span.
    *@param startIndex the starting offset for the span;
    *@param endIndex the ending offset for the span.
    */
    public Object getContent (double startIndex, double endIndex)
        throws gate.util.InvalidOffsetException
    {
        if ((((startIndex*10)%10) > 0)) throw (
          new gate.util.InvalidOffsetException(
            "Offset is not an integer value: " + startIndex +
            ". Textual documents only accept integer offsets!")
          );
        if ((((endIndex*10)%10) > 0)) throw  (
          new gate.util.InvalidOffsetException(
            "Offset is not an integer value: " + endIndex +
            ". Textual documents only accept integer offsets!")
          );
        int sidx = (int)startIndex;
        int eidx = (int)endIndex;
        if (eidx > content.length()) throw  (
          new gate.util.InvalidOffsetException(
            "Offset out of bounds: " + eidx + ">" + content.length())
          );
        return  content.substring(sidx, eidx);
    }


    private String content;

}

