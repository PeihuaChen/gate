package gate.impl.ag;

import gate.Annotation;

public class TextualDocument extends gate.impl.ag.Document implements gate.TextualDocument {
/*<Valy>the contents of this object should actually be a chunk of the document's text.
Should find a way to represent the borders of the contained text relative to the full document.
*/
  public TextualDocument(String id,String content){
    super(id);
    this.content=new String(content);//make a copy to be on the safe side...
  }

  /** The contents of the document */
  public String getCurrentContent() { return ""; }

  /** The contents of a particular span */
  public String getContentOf(Annotation a) {
    gate.Node start=a.getStartNode();
    gate.Node end=a.getEndNode();
    try{
      return (String)getContent(start.getOffset().doubleValue(),end.getOffset().doubleValue());
    }catch(gate.util.InvalidOffsetException e){
      e.printStackTrace(System.err);
    }
    return  null;
  }

  public double getLength(){
    return content.length();
  }
  public Object getContent(){
    return content;
  }

  public Object getContent(double startIndex, double endIndex)throws gate.util.InvalidOffsetException{
    if((((startIndex*10)%10)>0) )
      throw(new gate.util.InvalidOffsetException("Offset is not an integer value: "
                                                  +startIndex
                                                  +". Textual documents only accept integer offsets!"));
    if((((endIndex*10)%10)>0) )
      throw(new gate.util.InvalidOffsetException("Offset is not an integer value: "
                                                  +endIndex
                                                  +". Textual documents only accept integer offsets!"));
    int sidx=(int)startIndex;
    int eidx=(int)endIndex;
    if (eidx > content.length())throw(new gate.util.InvalidOffsetException("Offset out of bounds: "
                                                  +eidx+">"
                                                  +content.length()));
    return content.substring(sidx,eidx);
  }

  private String content;
}