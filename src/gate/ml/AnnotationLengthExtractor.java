/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 17 June 2002
 *
 *  $Id$
 */
package gate.ml;

import java.util.*;

import weka.core.*;

import gate.*;
import gate.util.*;
import gate.creole.ANNIEConstants;


public class AnnotationLengthExtractor extends AbstractAttributeExtractor {

  public AnnotationLengthExtractor() {
  }

  public Attribute getAttribute() {
    return new Attribute("Annotation length");
  }


  public Object getAttributeValue(Object data) {
    //the data is an annotation in this case.
    Annotation ann = (Annotation)data;
    Long endOffset = ann.getEndNode().getOffset();
    Long nextOffset = ann.getStartNode().getOffset();
    int tokensCnt = 0;
    while(nextOffset != null &&
          nextOffset.compareTo(endOffset) < 0){
      //advance offset counting all tokens found
      Set startingAnnots = dataCollector.getStartingAnnotations(nextOffset);
      if(startingAnnots != null && (!startingAnnots.isEmpty())){
        Iterator annIter = startingAnnots.iterator();
        while(annIter.hasNext()){
          Annotation annotation = (Annotation)annIter.next();
          if(annotation.getType().equals(ANNIEConstants.TOKEN_ANNOTATION_TYPE)){
            tokensCnt++;
          }
        }
      }
      nextOffset = dataCollector.nextOffset(nextOffset);
    }
    return new Double(tokensCnt);
  }
}