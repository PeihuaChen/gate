package gate.creole;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.sql.*;
import java.util.*;

import gate.*;
import gate.util.OffsetComparator;

public class ExportAnnotations
    extends AbstractLanguageAnalyser
    implements ProcessingResource {

  /** Instance of the document */
  private Document document;

  /** Name of the annotationSet */
  private String annotationSetName;

  /** Database Instance Name */
  private String databaseInstanceURL;

  /** Database Instance Name */
  private String tableName;

  /** The date on which the job was posted */
  private String datePosted;

  /** The ID of the job */
  private String jobID;


  /**
   * Constructor
   */
  public ExportAnnotations() {

  }

  public Resource init() throws ResourceInstantiationException {
    return this;
  }

  /** Performs reinitialization of the ExportAnnotations PR */
  public void reinit() throws ResourceInstantiationException {
    init();
  }

  public void execute() throws ExecutionException {

    // check for the document parameter
    if(document == null) {
      throw new ExecutionException("No document provided to process");
    }

    // get the annotationSet name provided by the user, or otherwise use the
    // default method
    AnnotationSet inputAs = (annotationSetName == null ||
        annotationSetName.length() == 0) ?
        document.getAnnotations() :
        document.getAnnotations(annotationSetName);

        // so lets get the iterator
    List tokens = new ArrayList(inputAs.get());
    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(tokens, offsetComparator);
    Iterator iterator = tokens.iterator();

    boolean datePostedFound = false;
    boolean jobIDFound = false;
    // search for the jobID annotations
    while(iterator.hasNext() && (!datePostedFound || !jobIDFound)) {
      Annotation ann = (Annotation)(iterator.next());
      if(ann.getType().equals(ANNIEConstants.DATE_POSTED_ANNOTATION_TYPE)) {
        long startOffset = ann.getStartNode().getOffset().longValue();
        long endOffset = ann.getEndNode().getOffset().longValue();
        datePosted = document.getContent().toString().substring((int)(startOffset),(int)endOffset);
        datePostedFound = true;
      }

      if(ann.getType().equals(ANNIEConstants.JOB_ID_ANNOTATION_TYPE)) {
        long startOffset = ann.getStartNode().getOffset().longValue();
        long endOffset = ann.getEndNode().getOffset().longValue();
        jobID = document.getContent().toString().substring((int)(startOffset),(int)endOffset);
        jobIDFound = true;
      }
    }

    if(!datePostedFound) {
      datePosted = "not available";
    }

    if(!jobIDFound) {
      jobID = "not available";
    }

    iterator = tokens.iterator();

    // statement
    PreparedStatement statement = connect(databaseInstanceURL);

    // lets take one annotation at a time and export it
    while (iterator.hasNext()) {
      exportAnnotation((Annotation) iterator.next(), statement);
    }

    try {
      closeConnection(statement.getConnection());
    } catch(SQLException sqle) {
      throw new ExecutionException(sqle.getMessage());
    }
  }

  /**
   * This method simple closes the connection
   * @param writer
   * @throws ExecutionException
   */
  private void closeConnection(Connection conn) throws ExecutionException {
    try {
      conn.close();
    } catch(SQLException sqle) {
      throw new ExecutionException(sqle.getMessage());
    }
  }

  /**
   * this method gathers all the necessary information from the annotation
   * and finally send it to be written in the file
   */
  private void exportAnnotation(Annotation current, PreparedStatement statement)
      throws ExecutionException {

    // we need following information from the annotaion
    // the annotation type
    // the string value

    // annotation type
    if(current.getType().equals(ANNIEConstants.DATE_POSTED_ANNOTATION_TYPE) ||
       current.getType().equals(ANNIEConstants.JOB_ID_ANNOTATION_TYPE)) {

      // do nothing in this case because this are the two values which
      // have been already found previously
      return;
    }

    try {
      // find the annotation type
      statement.setString(1, current.getType());

      // string
      long startOffset = current.getStartNode().getOffset().longValue();
      long endOffset = current.getEndNode().getOffset().longValue();
      String string = document.getContent().toString().substring( (int) (
          startOffset), (int) (endOffset));
      statement.setString(2, string);

      // job_ID
      statement.setString(3, jobID);

      //date_posted
      statement.setString(4, datePosted);

      // finally write the record
      statement.executeUpdate();
    } catch(SQLException sqle) {
      throw new ExecutionException(sqle.getMessage());
    }

  }


  /** This method writes the header in the txt file */
  private PreparedStatement connect(String databaseInstanceURL)
      throws ExecutionException {

    // url
    String url = "jdbc:odbc:" + databaseInstanceURL;

    try {
      // load the driver
      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

      // Get the connnection
      Connection conn = DriverManager.getConnection(url);
      PreparedStatement statement = conn.prepareStatement(
          "INSERT INTO "+tableName+" VALUES ( ?, ?, ?, ? )");

      return statement;
    }
    catch (SQLException sqle) {
      throw new ExecutionException(sqle.getMessage());
    }
    catch (ClassNotFoundException cnfe) {
      throw new ExecutionException(cnfe.getMessage());
    }

  }

  /** Sets the document in PR */
  public void setDocument(Document document) {
    this.document = document;
  }

  /** Returns the document */
  public Document getDocument() {
    return this.document;
  }

  /** Sets the annotationSetName in PR, which should be exported */
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  /** Returns the annotationSetName */
  public String getAnnotationSetName() {
    return this.annotationSetName;
  }

  /** Sets the databaseInstanceName */
  public void setDatabaseInstanceURL(String databaseInstanceURL) {
    this.databaseInstanceURL = databaseInstanceURL;
  }

  /** Returns the databaseInstanceName */
  public String getDatabaseInstanceURL() {
    return this.databaseInstanceURL;
  }

  /** Sets the tableName */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /** gets the tableName */
  public String getTableName() {
    return this.tableName;
  }
}