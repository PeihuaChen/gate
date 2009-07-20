package gate.opencalais;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Serializable;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.util.InvalidOffsetException;
import gate.corpora.DocumentContentImpl;

/**
 * A semantic annotator using OpenCalais
 */
public class OpenCalais extends AbstractLanguageAnalyser implements
                                                    ProcessingResource,
                                                    Serializable {
  /**
   * The URL of the OpenCalais REST service
   * 
   */
  private URL openCalaisURL;

  public void setOpenCalaisURL(URL newValue) {
    openCalaisURL = newValue;
  }

  public URL getOpenCalaisURL() {
    return openCalaisURL;
  }

  /**
   * The URL of the OpenCalais configuraiton file
   * 
   */
  private URL paramsXMLURL;
 
  private String paramsXMLString;

  public void setParamsXMLURL(URL newValue) {
    paramsXMLURL = newValue;
  }

  public URL getParamsXMLURL() {
    return paramsXMLURL;
  }

  /**
   * The license key for OpenCalais
   * 
   */
  private String licenseID;

  public void setLicenseID(String newValue) {
    licenseID = newValue;
  }

  public String getLicenseID() {
    return licenseID;
  }


  /**
   * Create the semantic annotator
   */
  public Resource init() throws ResourceInstantiationException {
    // sanity check parameters
    if(openCalaisURL == null) { throw new ResourceInstantiationException(
            "OpenCalais URL must be specified"); }

    if(licenseID == null || licenseID.equals("")) { throw new ResourceInstantiationException(
            "OpenCalais license ID must be specified"); }

    // Read the params file
    try {
      File paramsFile = new File(paramsXMLURL.toURI());
      BufferedReader bufReader = new BufferedReader(new FileReader(paramsFile));
      String line = bufReader.readLine();
      StringBuffer strBuf = new StringBuffer();
      while(line != null) {
	strBuf.append(line);
        line = bufReader.readLine();
      }

      paramsXMLString = strBuf.toString();

    } catch(FileNotFoundException fnfe) {   
      throw new ResourceInstantiationException(
              "paramsXML file" + paramsXMLURL  + "does not exist");
    } catch(IOException ioe) {
      throw new ResourceInstantiationException(
              "Problem reading paramsXML file" + paramsXMLURL);
    } catch(URISyntaxException use) {
      throw new ResourceInstantiationException(
              "Problem reading params file URI: " + paramsXMLURL);
    }

    return this;
  }

  public void execute() throws ExecutionException {
    Document doc = getDocument();

    // Get the text out of the document
    String docText = ((DocumentContentImpl) doc.getContent()).toString();

    // Post it to OpenCalais
    
    BufferedReader results = null;
    try {
       results = new BufferedReader(post(docText));
    }catch (IOException ioe) {
      throw new ExecutionException(
              "Problem reading params file URI: " + paramsXMLURL);
    }




    // Parse the output into GATE annotations
    // TODO
    try{
      String nextLine = results.readLine();
      while(nextLine != null) {
        System.out.println(nextLine);
        nextLine = results.readLine();
      }
    } catch (IOException ioe) {
      throw new ExecutionException(
              "Problem reading output from OpenCalais");
    }
    
  }
  
  /**
   * Post a request to OpenCalais
   */
    private Reader post(String text) throws IOException, UnsupportedEncodingException {
	StringBuilder sb = new StringBuilder(text.length() + 1024);
	sb.append("licenseID=").append(encode(licenseID));
	sb.append("&content=").append(encode(text));
	sb.append("&paramsXML=").append(encode(paramsXMLString));
	URLConnection connection = openCalaisURL.openConnection();
	connection.addRequestProperty("Content-Type",
			"application/x-www-form-urlencoded");
	connection.addRequestProperty("Content-Length", Integer.toString(sb.length()));
	connection.setDoOutput(true);
	OutputStream out = connection.getOutputStream();
	OutputStreamWriter writer = new OutputStreamWriter(out);
	writer.write(sb.toString());
	writer.flush();
	return new InputStreamReader(connection.getInputStream());
  }


  /** URL encode a string */
  private String encode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(s, "UTF-8");
  }

}
