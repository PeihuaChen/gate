package gate.lexicon;

import gate.util.*;
import gate.*;
import gate.creole.*;
import java.util.Iterator;

public class TestNLGLexiconPR extends AbstractProcessingResource {

  private NLGLexicon lexicon;
  private boolean printOnly = false;

  public TestNLGLexiconPR() {
  }

  public void setLexicon(NLGLexicon myLexicon) {
    lexicon = myLexicon;
  }

  public NLGLexicon getLexicon(){
    return lexicon;
  }

  public void setPrintOnly(Boolean isPrintOnly) {
    printOnly = isPrintOnly.booleanValue();
  }

  public Boolean getPrintOnly(){
    return new Boolean(printOnly);
  }

  public void execute() throws gate.creole.ExecutionException {
    if (lexicon == null)
      throw new ExecutionException("Lexicon not set");

    Out.prln(lexicon.getVersion());

    if (! printOnly) {
      lexicon.setVersion("2.0");
      MutableLexKBSynset newSynset = lexicon.addSynset();
      newSynset.setDefinition("my synset definition");
      newSynset.setPOS(NLGLexicon.POS_ADJECTIVE);
      Out.prln(newSynset.getDefinition());
      Out.prln(newSynset.getId());
      Out.prln(newSynset.getPOS());
    }

    Iterator iter = lexicon.getSynsets(NLGLexicon.POS_ADJECTIVE);
    while (iter.hasNext()) {
      LexKBSynset synset = (LexKBSynset) iter.next();
      Out.prln("definition: " + synset.getDefinition());
      Out.prln("id " + synset.getId());
      Out.prln("pos " + synset.getPOS());

    }

  }

}