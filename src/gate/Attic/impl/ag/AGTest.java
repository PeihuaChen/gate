package  gate.impl.ag;


import gate.util.*;


public class AGTest {

    public AGTest () {

    }


    public static void main (String[] args) {
        AGTest aGTest = new AGTest();
        aGTest.invokedStandalone = true;
        TextualDocumentImpl doc = new TextualDocumentImpl(Tools.gensym(), "Test");
        Long agId=Tools.gensym();
        gate.AnnotationGraph ag = doc.newAnnotationGraph(agId);

        try {
            ag.putNodeAt(Tools.gensym(), 0);
            ag.putNodeAt(Tools.gensym(), 4);
        }
        catch (gate.util.InvalidOffsetException e) {
            e.printStackTrace(System.err);
        }

        System.out.println(ag);
        ag.newAnnotation(Tools.gensym(), 0, 1, "char", "");
        ag.newAnnotation(Tools.gensym(), 1, 2, "char", "");
        ag.newAnnotation(Tools.gensym(), 2, 3, "char", "");
        ag.newAnnotation(Tools.gensym(), 3, 4, "char", "");
        gate.AnnotationGraph ag1 = doc.getAnnotationGraph(agId);
        System.out.println(ag1.getAnnotations("char"));
    }


    private boolean invokedStandalone = false;

}

