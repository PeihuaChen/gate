package gate.impl.ag;

public class AGTest {

  public AGTest() {
  }

  public static void main(String[] args) {
    AGTest aGTest = new AGTest();
    aGTest.invokedStandalone = true;

    TextualDocument doc=new TextualDocument("doc1","Test");
    gate.AnnotationGraph ag=doc.newAnnotationGraph("ag1");
    try{
      ag.putNodeAt("First",0);
      ag.putNodeAt("Last",4);
    }catch(gate.util.InvalidOffsetException e){
      e.printStackTrace(System.err);
    }
    System.out.println(ag);
    ag.newAnnotation("A",0,1,"char","");
    ag.newAnnotation("B",1,2,"char","");
    ag.newAnnotation("C",2,3,"char","");
    ag.newAnnotation("D",3,4,"char","");

    gate.AnnotationGraph ag1=doc.getAnnotationGraph("ag1");
    System.out.println(ag1.getAnnotations("char"));
  }
  private boolean invokedStandalone = false;
}