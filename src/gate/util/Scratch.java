/*
	Scratch.java

	Hamish Cunningham, 22/03/00

	$Id$
*/


package gate.util;

import java.util.*;
import gate.*;
import gate.jape.*;


/**
  * A scratch pad for experimenting.
  */
public class Scratch
{

  public boolean equals(Object other) {
    System.out.println("Scratch.equals, i = " + i);

    if(other instanceof Scratch)
      return i == ((Scratch)other).i;
    else
      return false;
  } // equals

  public Scratch(int i){
    this.i = i;
  }

  public static void main(String args[]) {
    Map map1 = new HashMap();
    Map map2 = new HashMap();

    Collection col1 = new HashSet();
    Collection col2 = new HashSet();

    Scratch o1 = new Scratch(1);
    Scratch o2 = new Scratch(2);
    Scratch o3 = new Scratch(1);
    Scratch o4 = new Scratch(4);

    col1.add(o1);
    col1.add(o2);
    col1.add(o3);
    col1.add(o4);

    col2.add(o1);
    col2.add(o3);
    col2.add(o4);
    col2.add(new Scratch(5));   // it figures out that col1.containsAll(col2)
    				// is false without calling equals...

    System.out.println(col1.containsAll(col2));
  } // main

  public int i;




public void thing(Document doc, LeftHandSide lhs)
throws InvalidOffsetException {

    AnnotationSet wholeAnnots = lhs.getBoundAnnots("whole");

    if(wholeAnnots != null && wholeAnnots.size() != 0) {
      // RHS assignment block

      FeatureMap features = Transients.newFeatureMap();

      { // need a block for the existing annot set
        AnnotationSet numberExistingAnnots = lhs.getBoundAnnots("number");
        AnnotationSet existingAnnots = numberExistingAnnots.get("Token");
        Iterator iter = existingAnnots.iterator();
        while(iter.hasNext()) {
          Object existingFeatureValue =
            ((Annotation) iter.next()).getFeatures().get("string");
          if(existingFeatureValue != null) {
            features.put("amount", existingFeatureValue);
            break;
          }
        }
      } // block for existing annots

      doc.getAnnotations().add(
        wholeAnnots.firstNode().getOffset(),     // shouldn't need getOffset
        wholeAnnots.lastNode().getOffset(),
        "Name", features
      );

      // end of RHS assignment block
    }
    
    if(wholeAnnots != null && wholeAnnots.size() != 0) {
      // RHS assignment block

      FeatureMap features = Transients.newFeatureMap();

      features.put("kind", "KiloAmount");

      doc.getAnnotations().add(
        wholeAnnots.firstNode(), wholeAnnots.lastNode(), "Name", features
      );

      // end of RHS assignment block
    }

/*
package japeactionclasses;
import gate.*; import java.io.*; import gate.jape.*;
import gate.annotation.*; import gate.util.*;
public class OneKiloAmountActionClass2 implements java.io.Serializable, RhsAction {
  public void doit(Document doc, LeftHandSide lhs) {
    AnnotationSet wholeAnnots = lhs.getBoundAnnots("whole");
    if(wholeAnnots != null && wholeAnnots.size() != 0) {
      // RHS assignment block
      FeatureMap attrs = null;
      Annotation annot = null;
      spans = new JdmSpanSequence();
      attrs = new JdmAttributeSequence();
      annot = new JdmAnnotation("Name", spans, attrs);
      spans.append(new JdmSpan(wholeAnnots.getLeftmostStart(), wholeAnnots.getRightmostEnd()));
      Object val = null;
      { // need a block for the existing annot set
        JdmAnnotationSet numberExistingAnnots = lhs.getBoundAnnots("number");
        JdmAnnotationSet existingAnnots =
        numberExistingAnnots.selectAnnotations("Token", new JdmAttributeSequence());
        for(int i=0; i<numberExistingAnnots.length(); i++) {
          JdmAttribute a = numberExistingAnnots.nth(i).getAttribute("string");
          if(a != null)
            try{attrs.append(new JdmAttribute(a));}
            catch(JdmException e) { }
        } // for
      } // block for existing annots
      doc.addAnnotation(annot);      // end of RHS assignment block
    }
    if(wholeAnnots != null && wholeAnnots.size() != 0) {
      // RHS assignment block
      FeatureMap attrs = null;
      Annotation annot = null;
      spans = new JdmSpanSequence();
      attrs = new JdmAttributeSequence();
      annot = new JdmAnnotation("Name", spans, attrs);
      spans.append(new JdmSpan(wholeAnnots.getLeftmostStart(), wholeAnnots.getRightmostEnd()));
      Object val = null;
      val = new String("KiloAmount");
      try{attrs.append(new JdmAttribute("kind", val));}
      catch(JdmException e) { }
      doc.addAnnotation(annot);      // end of RHS assignment block
    }
  }
}
*/

} // thing




} // class Scratch
