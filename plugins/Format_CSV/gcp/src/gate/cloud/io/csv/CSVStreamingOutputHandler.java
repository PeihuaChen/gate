package gate.cloud.io.csv;

import static gate.cloud.io.IOConstants.PARAM_ENCODING;
import static gate.cloud.io.IOConstants.PARAM_FILE_EXTENSION;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.cloud.batch.DocumentID;
import gate.cloud.io.json.JSONStreamingOutputHandler;
import gate.util.GateException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVStreamingOutputHandler extends JSONStreamingOutputHandler {
  
  public static final String PARAM_SEPARATOR_CHARACTER = "separator";
  public static final String PARAM_QUOTE_CHARACTER = "quote";  
  public static final String PARAM_COLUMNS = "columns";
  public static final String PARAM_ANNOTATION_SET_NAME = "annotationSetName";
  public static final String PARAM_ANNOTATION_TYPE = "annotationType";
  
  
  private static final Logger logger = Logger
      .getLogger(CSVStreamingOutputHandler.class);
  
  protected String encoding;

  protected char separatorChar;

  protected char quoteChar;
  
  protected String annotationSetName, annotationType;
  
  protected String[] columns;
  
  @Override
  protected void configImpl(Map<String, String> configData) throws IOException,
          GateException {

    if(!configData.containsKey(PARAM_FILE_EXTENSION)) {
      configData.put(PARAM_FILE_EXTENSION, ".csv");
    }
    
    super.configImpl(configData);
    
    encoding = configData.get(PARAM_ENCODING);
    separatorChar = configData.get(PARAM_SEPARATOR_CHARACTER).charAt(0);
    quoteChar = configData.get(PARAM_QUOTE_CHARACTER).charAt(0); 
    
    columns = configData.get(PARAM_COLUMNS).split(",");
    
    annotationSetName = configData.get(PARAM_ANNOTATION_SET_NAME);
    annotationType = configData.get(PARAM_ANNOTATION_TYPE);
  }
  
  @Override
  protected void outputDocumentImpl(Document document, DocumentID documentId)
    throws IOException, GateException {

    //TODO move to a thread local to save recreating each time?
    CSVWriter csvOut = new CSVWriter(new OutputStreamWriter(getFileOutputStream(documentId),encoding),separatorChar,quoteChar);
    
    String[] data = new String[columns.length];
    
    if (annotationType == null || annotationType.trim().equals("")) {
      for (int i = 0 ; i < columns.length ; ++i) {
        data[i] = (String)getValue(columns[i], document, null);
      }
      csvOut.writeNext(data);
    } else {
      
      List<Annotation> sorted = Utils.inDocumentOrder(document.getAnnotations(annotationSetName).get(annotationType));
      for (Annotation annotation : sorted) {
        for (int i = 0 ; i < columns.length ; ++i) {
          data[i] = (String)getValue(columns[i], document, annotation);
        }
        csvOut.writeNext(data);
      }
    }        
    
    csvOut.flush();
    
    //baos.get().write('\n');
    byte[] result = baos.get().toByteArray();
    
    csvOut.close();
    
    baos.get().reset();
    try {
      results.put(result);
    } catch(InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
  
  private Object getValue(String key, Document document, Annotation within) {
    
    String[] parts = key.split("\\.");
    
    if (parts.length > 2) {      
      logger.log(Level.WARN, "Invalid key: "+key);
      return null;
    }
    
    if (key.startsWith(".")) {
      return document.getFeatures().get(parts[1]);
    } else {
      AnnotationSet annots = document.getAnnotations(annotationSetName).get(parts[0]);
      
      if (within != null) {
        annots = Utils.getContainedAnnotations(annots, within);
      }
      
      if (annots.size() == 0) return null;
      
      Annotation annotation = Utils.inDocumentOrder(annots).get(0);
      
      if (parts.length == 1)
        return Utils.stringFor(document, annotation);
      
      return annotation.getFeatures().get(parts[1]);
    }
  }
}
