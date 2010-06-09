package org.knallgrau.utils.textcat;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import org.knallgrau.utils.textcat.TextCategorizer;


@CreoleResource(name = "TextCat PR", 
        comment = "Recognizes the document language using TextCat. Possible" +
        "languages: german, english, french, spanish, italian, swedish, polish, dutch, " +
        "norwegian, finnish, albanian, slovakian, slovenian, danish, hungarian.")		
public class LanguageIdentifier 
  extends gate.creole.AbstractLanguageAnalyser 
  implements ControllerAwarePR     {

  private static final long serialVersionUID = 5831213212185693826L;
  @SuppressWarnings("unused")
  private static final String __SVNID = "$Id$";
  
  private TextCategorizer guesser; 
  
  public LanguageIdentifier init() throws ResourceInstantiationException {
    return this;
  }
  
  
  public void controllerExecutionStarted(Controller c)
    throws ExecutionException {
    guesser = new TextCategorizer();
  }


	/**
	 * Based on the document content, recognizes the language and adds a document feature.
	 */
	public void execute() throws ExecutionException {
		if(document == null || document.getFeatures() == null)
			return;
		
//		String language = (String) document.getFeatures().get(languageFeatureName);
//		if(language != null && language.length() > 0)
//			return;
		
		if(languageFeatureName == null || "".equals(languageFeatureName))
			languageFeatureName = "language";

		/* Default situation: classify the whole document and save the
		 * result as a document feature.		 */
		if ( (annotationType == null) || (annotationType.length() == 0) )  {
		  String text = document.getContent().toString();
		  String category = guesser.categorize(text);
		  document.getFeatures().put(languageFeatureName, category);
		}
		
		/* New option: classify the text underlying each annotation
		 * (specified by AS and type) and save the result as
		 * an annotation feature.		 */
		else {
		  AnnotationSet annotations = document.getAnnotations(annotationSetName).get(annotationType);
		  for (Annotation annotation : annotations) {
		    String text = Utils.stringFor(document, annotation);
		    String category = guesser.categorize(text);
		    annotation.getFeatures().put(languageFeatureName, category);
		  }
		}
		
	}
	
	
	public void reInit() throws ResourceInstantiationException { }
	
	
	/*  CREOLE PARAMETERS */
	
	@RunTime
	@Optional
	@CreoleParameter(comment = "Name of the document feature which will be used for language.",
	        defaultValue = "LANGUAGE")
	public void setLanguageFeatureName(String languageFeatureName) {
		this.languageFeatureName = languageFeatureName;
	}

  public String getLanguageFeatureName() {
    return languageFeatureName;
  }

  private String languageFeatureName;
  
	
	@RunTime
	@Optional
	@CreoleParameter(comment = "Type of annotations to classify; if null or empty, this PR will classify whole documents.",
	        defaultValue = "")
	public void setAnnotationType(String atype) {
	  this.annotationType = atype;
	}
	
	public String getAnnotationType() {
	  return this.annotationType;
	}
	
	private String annotationType;
	
	@RunTime
	@Optional
	@CreoleParameter(comment = "name of input/output annotation set (not used for whole-document categorization)")
  public void setAnnotationSetName(String inputASName) {
    this.annotationSetName = inputASName;
  }

  public String getAnnotationSetName() {
    return annotationSetName;
  }

  private String annotationSetName;

	
	/*  MISC. INHERITED STUFF */
	
  public void controllerExecutionAborted(Controller c, Throwable t)
          throws ExecutionException {
    // NOTHING
  }

  public void controllerExecutionFinished(Controller c)
          throws ExecutionException {
    // NOTHING
  }


} // class LanguageIdentifier

