package gate.lexicon;

import gate.DataStore;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.LanguageResource;
import gate.Resource;
import gate.FeatureMap;
import java.io.Serializable;
import java.util.*;
import gate.creole.*;

public class OntoLexKBImpl extends AbstractLanguageResource
    implements OntoLexLR, Serializable {

  protected HashMap lexIdMap = new HashMap();
  protected HashMap conceptIdMap = new HashMap();
  protected Object lexKBIndentifier;
  protected Object ontologyIdentifier;
  static final long serialVersionUID = -6008345192731330593L;

  public OntoLexKBImpl() {
  }

  public List getConceptIds(Object lexId) {
    Object concepts = lexIdMap.get(lexId);
    if (concepts == null || !(concepts instanceof List))
      return null;
    return (List) concepts;
  }

  public List getLexIds(Object conceptId) {
    Object lexItems = conceptIdMap.get(conceptId);
    if (lexItems == null || !(lexItems instanceof List))
      return null;
    return (List) lexItems;
  }

  public Set getAllLexIds() {
    return lexIdMap.keySet();
  }

  public Set getAllConceptIds() {
    return conceptIdMap.keySet();
  }

  public void add(Object conceptId, Object lexId) {
    //first add it to the concept index
    Object lexItems = conceptIdMap.get(conceptId);
    List lexItemsList;
    if (lexItems == null) {
      lexItemsList = new ArrayList();
      lexItemsList.add(lexId);
      conceptIdMap.put(conceptId, lexItemsList);
    }
    else {
      lexItemsList = (List) lexItems;
      lexItemsList.add(lexId);
    }

    //then add it to the lexical index
    Object conceptItems = lexIdMap.get(lexId);
    List conceptItemsList;
    if (conceptItems == null) {
      conceptItemsList = new ArrayList();
      conceptItemsList.add(conceptId);
      lexIdMap.put(lexId, conceptItemsList);
    }
    else {
      conceptItemsList = (List) conceptItems;
      conceptItemsList.add(conceptId);
    }

  }

  public void removeByConcept(Object conceptId) {
    //first find the list of lexical Ids that are mapped to this concept ID
    //and delete the conceptId from their list
    Object lexIds = conceptIdMap.get(conceptId);
    if (lexIds == null || !(lexIds instanceof List))
      return;
    List lexIdList = (List) lexIds;
    for (int i=0; i < lexIdList.size(); i++) {
      Object lexId = lexIdList.get(i);
      List conceptList = getConceptIds(lexId);
      if (conceptList != null)
        conceptList.remove(conceptId);
    }//for

    //finally delete the conceptId from the conceptIdMap
    conceptIdMap.remove(conceptId);
  }

  public void removeByLexId(Object lexId) {
    //first find the list of concept Ids that are mapped to this lexical ID
    //and delete the lex Id from their list
    Object conceptIds = lexIdMap.get(lexId);
    if (conceptIds == null || !(conceptIds instanceof List))
      return;
    List conceptIdList = (List) conceptIds;
    for (int i=0; i < conceptIdList.size(); i++) {
      Object conceptId = conceptIdList.get(i);
      List lexList = getLexIds(conceptId);
      if (lexList != null)
        lexList.remove(lexId);
    }//for

    //finally delete the lexId from the lexIdMap
    lexIdMap.remove(lexId);
  }

  public void remove(Object conceptId, Object lexId) {
    Object conceptIds = lexIdMap.get(lexId);
    if (conceptIds != null && (conceptIds instanceof List))
      ((List) conceptIds).remove(conceptId);
    Object lexIds = conceptIdMap.get(conceptId);
    if (lexIds != null && (lexIds instanceof List))
      ((List) lexIds).remove(lexId);
  }

  public boolean isEmpty() {
    return conceptIdMap.isEmpty() && lexIdMap.isEmpty();
  }

  public void clear() {
    conceptIdMap.clear();
    lexIdMap.clear();
  }

  public Object getLexKBIdentifier() {
    return this.lexKBIndentifier;
  }

  public void setLexKBIdentifier(Object lexId) {
    this.lexKBIndentifier = lexId;
  }

  public Object getOntologyIdentifier() {
    return this.ontologyIdentifier;
  }

  public void setOntologyIdentifier(Object ontoId) {
    this.ontologyIdentifier = ontoId;
  }

  public Resource init() throws gate.creole.ResourceInstantiationException {
    if (this.ontologyIdentifier == null || this.lexKBIndentifier == null)
      throw new ResourceInstantiationException(
        "You must specify the identifiers of the ontology "
        + "and lexicon for the mapping");
    return super.init();
  }

}