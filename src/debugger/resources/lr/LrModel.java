package debugger.resources.lr;

import gate.LanguageResource;
import gate.Document;
import gate.corpora.DocumentImpl;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Vladimir Karasev
 */

public class LrModel //extends DefaultMutableTreeNode
{
    LanguageResource lr;
    String storedContent;

    public LrModel(LanguageResource lr) {
        this.lr = lr;
        if (lr instanceof Document) {
            storedContent = ((Document) lr).getContent().toString();
        }
    }

    public String getText() {
        if (lr instanceof Document) {
            return ((Document) lr).getContent().toString();
        }
/*
        else if(lr instanceof CorpusImpl)
        {
            return ((CorpusImpl) lr).getDocumentNames().toString();
        }
*/
        return "";
    }

    public String getStoredContent() {
        return storedContent;
    }

    public void synchronize() {
        if (lr instanceof Document) {
            storedContent = ((Document) lr).getContent().toString();
        }
    }

//    public Enumeration children()
//    {
//        List c = new ArrayList(children);
//        Collections.sort(c, new Comparator()
//        {
//            public int compare(Object o1, Object o2)
//            {
//                String name1 = o1.toString();
//                String name2 = o2.toString();
//                return name1.compareToIgnoreCase(name2);
//            }
//        });
//        return new Vector(c).elements();
//    }

    public LanguageResource getLr() {
        return lr;
    }

    public boolean equals(Object obj) {
        if (obj instanceof LrModel) {
            return this.lr.equals(((LrModel) obj).lr);
        }
        return super.equals(obj);
    }

    public String toString() {
        return lr.getName();
    }

    public String getName() {
        return lr.getName();
    }
}
