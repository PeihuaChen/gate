/*
 *  BootStrap.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Oana Hamza 14/Nov/2000
 *
 *  $Id$
 */
package gate.creole;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.beans.*;
import gnu.regexp.*;
import gate.util.*;
import java.util.jar.*;

/**
  * This class creates a resource (e.g.ProcessingResource, VisualResource or
  * Language Resource) with the information from the user and generates a
  * project in the directory provided by the user
  */

public class BootStrap {

  /** Where on the classpath the gate resources are to be found*/
  protected static String resourcePath = "gate/resources";

  /** the name of the resource of the template project from the gate resources*/
  protected String oldResource = "creole/templateproject";

  /** the name of jar resource*/
  protected String nameProject = "Template";

  /** a map from the variants of the names of the files and the
    * directories of the empty project to the variants of the names of the
    * files and the directories the new project
    */
  protected static Map names = null;

  /** the size of the buffer */
  private final static int BUFF_SIZE = 65000;

  /** a buffer in order to read an array of bytes */
  private byte buffer[] = null;

  /** a buffer in order to read an array of char */
  private char cbuffer[] = null;

  /** the current file created by the system */
  File newFile = null;

  /** determine the methods from the class that implements the resource*/
  ArrayList listMethodsResource = null;

  public BootStrap() {

    names = new HashMap();

    listMethodsResource = new ArrayList();

    buffer = new byte[BUFF_SIZE];

    cbuffer = new char[BUFF_SIZE];
  }

  /** replace with replacement in the text using regEx as a regular expression
    */
  public String regularExpressions ( String text, String replacement,
                                      String regEx) {
    String result = text;
    try {
      RE re = new RE(regEx);
      result = re.substituteAll( text,replacement);
    } catch (REException ree) {ree.printStackTrace();}
    return result;
  }

  /** Determines all the keys from the map "names" in the text and replaces them
    * with their values
    */
  public String changeKeyValue ( String text ) {

    Set keys = names.keySet();
    Iterator iteratorKeys = keys.iterator();
    while (iteratorKeys.hasNext()) {

      String key = (String) iteratorKeys.next();
      String value = (String)names.get(key);
      text = regularExpressions(text,value,key);
    } // while
    return text;
  } // changeKeyValue ( String text )

  /** return the text between the last dot and the end of input
    */
  public String findDot(String text) {

    // determine the position of the last "."
    int index = text.lastIndexOf(".");

    if (index != -1)
      text = text.substring(index+1,text.length());

    return text;
  }

  /** returns all the interfaces that it implements and the class that
    * it extends as a string
    */
  public String getInterfacesAndClass (String typeResource,
                                    Set listInterfaces) {

    // a map from the interfaces to classes which implement them
    Map interfaceAndImplementClass = new HashMap();

    // create the map in which the interfaces implement the resources such as
    // ProcessingResource, LanguageResource, VisualResource
    interfaceAndImplementClass.put(
      "ProcessingResource","AbstractProcessingResource");
    interfaceAndImplementClass.put(
      "LanguageResource","AbstractLanguageResource");
    interfaceAndImplementClass.put(
      "VisualResource","AbstractVisualResource");

    // add the class that it extends
    String interfacesAndClass = "";
    String abstractClass = "";

    abstractClass = (String)interfaceAndImplementClass.get(typeResource);
    interfacesAndClass = " extends " + abstractClass;

    // a map from all the methods from interfaces to the lists which contains
    // the features of every method
    Map allMethods = new HashMap();
    // add the interfaces that it implements
    if (listInterfaces!=null) {
        interfacesAndClass = interfacesAndClass+ "\n"+ "  implements";
        Iterator iter = listInterfaces.iterator();
        while (iter.hasNext()) {
          String name =(String)iter.next();
          try{
            Class currentClass = Class.forName("gate."+name);
            Method[] listMethods = currentClass.getMethods();

            for (int i=0;i<=listMethods.length-1;i++) {

              ArrayList features = new ArrayList();
              // add the type returned by the method
              features.add(0,listMethods[i].getReturnType());

              // add the types of the parameters of the method
              features.add(1,listMethods[i].getParameterTypes());

              // add the exceptions of the method
              features.add(2,listMethods[i].getExceptionTypes());

              String nameMethodInterface = listMethods[i].getName();
              allMethods.put(nameMethodInterface,features);
            }
          } catch (ClassNotFoundException cnfe){cnfe.printStackTrace();}

          interfacesAndClass = interfacesAndClass + " "+ name;
          if (iter.hasNext())
            interfacesAndClass = interfacesAndClass +",";
        }
    }
    boolean find= false;

    // methods from the class that extends the resource
    ArrayList methods = new ArrayList();
    try {
      Class currentClassExtend = Class.forName("gate.creole."+abstractClass);
      Method[] listMethodsClassExtend = currentClassExtend.getMethods();

      for (int i=0;i<=listMethodsClassExtend.length-1;i++) {
        String name = listMethodsClassExtend[i].getName();
        methods.add(name);
      }// for

      shapeMethod(methods,allMethods);

    } catch (ClassNotFoundException cnfe){cnfe.printStackTrace();}

    return interfacesAndClass;
  } // getInterfacesAndClass

  /** create the form for the methods from the class that create the resource
    * @listMethodExtend is the list with all methods from the class that extends
    *  the resource
    * @listInterfacesMethod is the list with all methods from the interfaces
    * that implement the resource
    */
  public void shapeMethod (ArrayList listMethodExtend,Map listInterfacesMethod){
    // determine all the methods from the interfaces which are not among the
    // methods from the class that extends the resource
    Set keys = listInterfacesMethod.keySet();
    Iterator iteratorKeys = keys.iterator();
    while (iteratorKeys.hasNext()) {
      String nameMethod = (String)(iteratorKeys.next());
      if (listMethodExtend.contains(nameMethod) == false) {

        ArrayList currentFeature = (ArrayList)(
                                          listInterfacesMethod.get(nameMethod));
        // the value which the method returns
        Class valReturn = (Class)(currentFeature.get(0));
        // the types of the parameters of the method
        Class[] valTypes = (Class[])(currentFeature.get(1));
        // the exceptions of the method
        Class[] valException = (Class[])(currentFeature.get(2));

        // the form of the method
        String typeReturn = findDot(valReturn.getName());
        String declaration = "public "+ typeReturn +" "+
                             nameMethod +"(";
        // parameters
        if (valTypes.length == 0)
          declaration = declaration+")";
        else
          for (int i=0;i<valTypes.length;i++) {
            declaration = declaration + findDot(valTypes[i].getName()) +
                            " variable"+ i;

            if (i==valTypes.length-1)
              declaration = declaration + ")";
            else
              declaration = declaration + ", ";

          } // for

        // exceptions
        if (valException.length == 0) {
          if (typeReturn.compareTo("void") !=0 )
           declaration = declaration + "{ " + "return "+
                            typeReturn.toLowerCase()+ "; }" + "\n" + "\n"+
                            "protected "+ typeReturn + " " +
                            typeReturn.toLowerCase() +";";
          else
           declaration = declaration+" {}" ;
        } // if
        else {
          declaration = declaration + " throws ";
          for (int i=0;i<valException.length;i++) {

            declaration = declaration + findDot(valException[i].getName());

            if (i == valException.length-1) {
              if (typeReturn.compareTo("void") !=0 )
                declaration = declaration + "{ " + "return "+
                          typeReturn.toLowerCase()+ "; }" + "\n"+"\n" +
                          "protected " + typeReturn +" " +
                          typeReturn.toLowerCase() +";";
              else
                declaration = declaration+" {}" ;
            } else
              declaration = declaration + ", ";

          } // for
        } // else

        // add the form of the method
        listMethodsResource.add(declaration);
      } // if
    } // while
  } // shapeMethod

  /** Modifies the content of the file from gate/resources/pathResourceFile
    * using the information (e.g.name of the resource, name of the
    * class which implements the resource)provided by the user and copies it
    * in currentFile. This procedure also adds the interfaces that it implements
    * and the class that it extends in the class which creates the resource
    * @content is the content of the current file from the
    *  template project
    * @interfaces is the class which extends and the interfaces which
    *  implement the class that it implements the resource
    * @expr helps to determine the class which implements the resource
    */
  public String addContent(String content,String expr,String interfaces) {

    String newContent = changeKeyValue(content);

    REMatch aMatch = null;

    try {
      RE regExpr = new RE(expr);

      aMatch = regExpr.getMatch(newContent);

      if (aMatch!= null) {

        int finalIndex = aMatch.getEndIndex();

        // get the new content of the current file
        String finalContent = newContent.substring(
                                              finalIndex+2,newContent.length());
        String nextLetter = newContent.substring(finalIndex,finalIndex+2);

        newContent = newContent.substring(0,finalIndex)+ interfaces+nextLetter
                      +"\n";
        Iterator iterator = listMethodsResource.iterator();
        while (iterator.hasNext()) {
          String method = (String)iterator.next();
          newContent = newContent + "\n" + method+ "\n";
        }
        newContent = newContent + finalContent;
      }

    } catch (REException ree) {
      ree.printStackTrace();
    }
    return newContent;
  } // addContent

  /** create the map with variants of the names... */
  public Map createNames (String nameResource, String nameClass) {

    // determine the name of the current user and the current day
    Calendar calendar = Calendar.getInstance();
    int month = calendar.get(calendar.MONTH)+1;
    int year = calendar.get(calendar.YEAR);
    int day = calendar.get(calendar.DAY_OF_MONTH);
    String date = day+"/"+month+"/"+year;
    String user = System.getProperty("user.name");

    // the a map with the variants of names and the current date
    // and the current user
    names.put(nameProject,nameResource);
    names.put(nameProject.toUpperCase(),nameResource.toUpperCase());
    names.put(nameProject.toLowerCase(),nameResource.toLowerCase());
    names.put("___CLASSNAME___",nameClass);
    names.put("___DATE___",date);
    names.put("___AUTHOR___",user);

    return names;
  }

  /**  Creates the resource and dumps out a project structure using the
    *  structure from gate/resource/meter.jar and the information provided by
    *  the user
    * @nameResource is the name of the new resource
    * @typeResource is the type of the resource (e.g.ProcessingResource,
    *  LanguageResource or VisualResource)
    * @nameClass is the name of the class which implements the resource
    * @listInterfaces is the set of the interfaces that implements the resource
    * @pathNewProject is the path where it will be the new resource
    */
  public void createResource( String nameResource,String typeResource,
                              String nameClass, Set listInterfaces,
                              String pathNewProject)
  {
      createNames(nameResource,nameClass);

      // determine the interfaces that the resource implements and the class
      // that it extends
      String interfacesAndClass = getInterfacesAndClass (typeResource,
                                                  listInterfaces);
      try {
      // take the content of the file with the structure of the template project
      InputStream inputStream = Files.getGateResourceAsStream(oldResource +"/"+
                                "file-list.properties");

      Properties properties = new Properties();

      properties.load(inputStream);

      Enumeration keyProperties = properties.propertyNames();

      // goes through all the files from the template project
      while (keyProperties.hasMoreElements()) {

        String valKey = (String)keyProperties.nextElement();

        String valueKey = properties.getProperty(valKey);


        int indexEnd = valueKey.indexOf(",");
        String newValueKey = valueKey;

        while ((indexEnd != -1)||(newValueKey.compareTo("")!=0)) {
          String nameFile = "";

          if (indexEnd != -1) {
            nameFile = newValueKey.substring(0,indexEnd);
          } else {
            nameFile = newValueKey;
            newValueKey = "";
          }

          // the new path of the current file from template project
          String newPathFile = changeKeyValue(pathNewProject+"/"+nameFile);

          if (valKey.compareTo("dir") == 0) {
            // the current directory is created
            newFile = new File(newPathFile);
            newFile.mkdir();

          } // if
          else {
            // the extension of the current file
            String extension = newPathFile.substring(newPathFile.length()-4,
                                                        newPathFile.length());

            InputStream currentInputStream =
                Files.getGateResourceAsStream(oldResource+"/"+nameFile);

            newFile = new File(newPathFile);

            if (extension.compareTo(".jar")!=0) {

              // the content of the current file is copied on the disk
              try {
                // the current file for writing characters
                FileWriter fileWriter = new FileWriter(newFile);

                InputStreamReader inputStreamReader = new InputStreamReader (
                                                        currentInputStream);

                int  charRead = 0;
                String text = null;

                while(
                (charRead = inputStreamReader.read(cbuffer,0,BUFF_SIZE)) != -1){

                  text = new String (cbuffer,0,charRead);

                  String expr = "public class " + names.get("___CLASSNAME___");
                  String newText = addContent(text,expr,interfacesAndClass);
                  fileWriter.write(newText ,0,newText.length());

                } // while

                inputStreamReader.close();

                // close the input stream
                currentInputStream.close();

                // close the file for writing
                fileWriter.close();

              } catch (IOException ioe) {
                ioe.printStackTrace();
              }

            } // if
            else { // the current file is a jar

              // the current file for writing bytes
              FileOutputStream fileOutputStream = null;
              fileOutputStream = new FileOutputStream (newFile);
              int  bytesRead = 0;
              while(
                (bytesRead = currentInputStream.read(buffer,0,BUFF_SIZE)) != -1)

                fileOutputStream.write(buffer ,0,bytesRead);

                currentInputStream.close();

                fileOutputStream.close();
            } // else

          } // else
          if (indexEnd != -1)
           newValueKey = newValueKey.substring(indexEnd+1,newValueKey.length());

          indexEnd = newValueKey.indexOf(",");
        } // while
      }// while

    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  } // modify

} // class BootStrap
