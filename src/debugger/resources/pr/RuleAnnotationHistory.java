package debugger.resources.pr;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.annotation.AnnotationSetImpl;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */

public class RuleAnnotationHistory implements AnnotationSetListener {
    private ArrayList annSetsByDocument;

    public RuleAnnotationHistory() {
        this.annSetsByDocument = new ArrayList();
    }

    public Iterator getAllAnnotationSets() {
        return annSetsByDocument.iterator();
    }

    public AnnotationSet getAnnotationSet(Document document) {
        synchronized (annSetsByDocument) {
            for (Iterator iterator = annSetsByDocument.iterator(); iterator.hasNext();) {
                AnnotationSet annotationSet = (AnnotationSet) iterator.next();
                if (annotationSet.getDocument().equals(document)) {
                    return annotationSet;
                }
            }
            return null;
        }
    }

    public boolean addAnnotationSet(AnnotationSet annSet) {
        if (null == annSet) {
            return false;
        }
        AnnotationSet sourceAnnSet = getAnnotationSet(annSet.getDocument());
        boolean flag = true;
        if (null != sourceAnnSet) {
            for (Iterator annItr = annSet.iterator(); annItr.hasNext();) {
                Annotation ann = (Annotation) annItr.next();
                if (!sourceAnnSet.add(ann)) {
                    flag = false;
                }
            }
            return flag;
        } else {
            return annSetsByDocument.add(annSet);
        }
    }

    public boolean addAnnotation(Annotation annotation, Document document) {
        if (null != getAnnotationSet(document)) {
            return getAnnotationSet(document).add(annotation);
        } else {
            AnnotationSet annSet = new AnnotationSetImpl(document);
            annSet.add(annotation);
            return addAnnotationSet(annSet);
        }
    }

    public void annotationAdded(AnnotationSetEvent e) {
    }

    public void annotationRemoved(AnnotationSetEvent e) {
        AnnotationSet annSet = getAnnotationSet(e.getSourceDocument());
        if (null != annSet) {
            annSet.remove(e.getAnnotation());
        }
    }
}

