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
import java.util.*;

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

  /** determine the methods from the class that implements the resource*/
  protected ArrayList listMethodsResource = null;

  /** the packages used by the class which creates the resources */
  protected Set allPackages = null;

  protected Map fields = null;

  public BootStrap() {

    names = new HashMap();

    listMethodsResource = new ArrayList();

    buffer = new byte[BUFF_SIZE];

    cbuffer = new char[BUFF_SIZE];

    allPackages = new HashSet();

    fields = new HashMap();
  }

  /** replace with replacement in the text using regEx as a regular expression
    */
  public String regularExpressions ( String text, String replacement,
                                      String regEx) throws REException{
    String result = text;
    RE re = new RE(regEx);
    result = re.substituteAll( text,replacement);
    return result;
  }

  /** Determines all the keys from the map "names" in the text and replaces them
    * with their values
    */
  public String changeKeyValue ( String text )throws REException {

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
  public String determineTypePackage(String text) {

    // determine the position of the last "."
    int index = text.lastIndexOf(".");
    int ind = text.lastIndexOf(";");
    String type = new String();
    String namePackage = new String();

    if (index != -1){
      // determine the package and add to the list of packages
      if (ind != -1) {
        type = text.substring(index+1,text.length()-1)+"[]";
        namePackage = (text.substring(2,index))+".*";
      }
      else {
        namePackage = (text.substring(0,index))+".*";
        type = text.substring(index+1,text.length());
      }
      // add the name of the package
      if ((!allPackages.contains(namePackage))&&
                              (namePackage.compareTo("java.lang.*")!=0))
        allPackages.add(namePackage);

    } else {type = text;}

    return type;
  }

  /** returns all the interfaces that it implements and the class that
    * it extends as a string
    */
  public String getInterfacesAndClass (String typeResource, Set listInterfaces)
                                    throws ClassNotFoundException {

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
        int lastDot = name.lastIndexOf(".");
        Class currentClass;

        // determine the packages
        if (lastDot != -1) {
          String namePackage =  name.substring(0,lastDot);
          currentClass = Class.forName(name);
          name = name.substring(lastDot+1,name.length());
          // add the name of package in the list
          if ((!allPackages.contains(namePackage))&&
                            (namePackage.compareTo("java.lang.*")!=0))
            allPackages.add(namePackage +".*");
        } else {
          currentClass = Class.forName("gate."+name);
          if (!allPackages.contains("gate.*"))
            allPackages.add("gate.*");
        }
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

        interfacesAndClass = interfacesAndClass + " "+ name;
        if (iter.hasNext())
          interfacesAndClass = interfacesAndClass +",";
      }
    }
    boolean find= false;

    // methods from the class that extends the resource
    ArrayList methods = new ArrayList();
    Class currentClassExtend = Class.forName("gate.creole."+abstractClass);
    Method[] listMethodsClassExtend = currentClassExtend.getMethods();

    for (int i=0;i<=listMethodsClassExtend.length-1;i++) {
      String name = listMethodsClassExtend[i].getName();
      methods.add(name);
    }// for

    shapeMethod(methods,allMethods);

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
    // methods of the class that extends the resource
    Set keys = listInterfacesMethod.keySet();
    Iterator iteratorKeys = keys.iterator();
    int j = 0;
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
        Out.prln("val "+valReturn.getName());
        String typeReturn = determineTypePackage(valReturn.getName());


        String declaration = "public "+ typeReturn +" "+
                             nameMethod +"(";
        // parameters
        if (valTypes.length == 0)
          declaration = declaration+")";
        else
          for (int i=0;i<valTypes.length;i++) {
            declaration = declaration +
                            determineTypePackage(valTypes[i].getName()) +
                            " parameter"+ i;

            if (i==valTypes.length-1)
              declaration = declaration + ")";
            else
              declaration = declaration + ", ";

          } // for

        // exceptions

        if (valException.length == 0) {
          if (typeReturn.compareTo("void") !=0 ){
            if (typeReturn.indexOf("[]") == -1)
              declaration = declaration + "{ " + "return "+
                            typeReturn.toLowerCase()+ j + "; }";
            else
              declaration = declaration + "{ " + "return "+
                            typeReturn.toLowerCase().substring(
                            0,typeReturn.length()-2)+ j + "[]; }";

            fields.put(new Integer(j),typeReturn);
            j =j+1;
          }
          else {declaration = declaration+" {}" ;}
        } // if
        else {
          declaration = declaration + "\n"+ "                throws ";
          for (int i=0;i<valException.length;i++) {

            declaration = declaration + determineTypePackage(
                                                    valException[i].getName());

            if (i == valException.length-1) {
              if (typeReturn.compareTo("void") !=0 ){
                if (typeReturn.indexOf("[]") == -1)
                  declaration = declaration + "{ " + "return "+
                          typeReturn.toLowerCase()+ j+"; }";
                else
                  declaration = declaration + "{ " + "return "+
                            typeReturn.toLowerCase().substring(
                            0,typeReturn.length()-2)+ j + "[]; }";

                fields.put(new Integer(j),typeReturn);
                j=j+1;
              }
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
  public String addContent(String content,String expr,String interfaces)
                          throws REException{

    String newContent = changeKeyValue(content);

    REMatch aMatch = null;

    RE regExpr = new RE(expr);

    aMatch = regExpr.getMatch(newContent);

    if (aMatch!= null) {

      int finalIndex = aMatch.getEndIndex();

      // get the new content of the current file
      String finalContent = newContent.substring(
                                            finalIndex+2,newContent.length());
      String nextLetter = newContent.substring(finalIndex,finalIndex+2);

      newContent = newContent.substring(0,finalIndex)+
                                                interfaces+nextLetter+"\n";

      Iterator iterator = listMethodsResource.iterator();
      while (iterator.hasNext()) {
        String method = (String)iterator.next();
        newContent = newContent + "\n" + method+"\n";
      }
      Iterator iter = fields.keySet().iterator();
      int i=0;
      while (iter.hasNext()) {
        Integer index = (Integer)iter.next();
        String type = (String)fields.get(index);
        newContent = newContent + "\n" + "protected " + type +" " +
                          type.toLowerCase() + index.toString() +";";
        i+=1;
      }
      newContent = newContent + finalContent;
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

  /** determine all the packages */
  public String namesPackages (Set listPackages) {
    Iterator iterator = listPackages.iterator();
    String packages = new String();
    while (iterator.hasNext()) {
      packages = packages + "\n" + "import "+ iterator.next()+";";
    }// while
    return packages;
  }

  /**  Creates the resource and dumps out a project structure using the
    *  structure from gate/resource/creole/templateproject/Template and the
    *  information provided by the user
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
                              throws
                              IOException,ClassNotFoundException, REException {

    createNames(nameResource,nameClass);

    // determine the interfaces that the resource implements and the class
    // that it extends
    String interfacesAndClass = getInterfacesAndClass (typeResource,
                                                  listInterfaces);

    // all the packages from the class which creates the resource
    String packages = namesPackages(allPackages);

    // the current file created by the system
    File newFile = null;

    // take the content of the file with the structure of the template project
    InputStream inputStream = Files.getGateResourceAsStream(oldResource +"/"+
                              "file-list.properties");

    Properties properties = new Properties();

    // put all the files and directories
    properties.load(inputStream);

    // close the input stream
    inputStream.close();

    Enumeration keyProperties = properties.propertyNames();

    // goes through all the files from the template project
    while (keyProperties.hasMoreElements()) {

      String key = (String)keyProperties.nextElement();

      String valueKey = properties.getProperty(key);

      StringTokenizer token = new StringTokenizer(valueKey,",");
      while (token.hasMoreTokens()) {
        String nameFile = (String)token.nextToken();

        // the new path of the current file from template project
        String newPathFile = changeKeyValue(pathNewProject+"/"+nameFile);

        if (key.compareTo("dir") == 0) {
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
          if (extension.compareTo(".jav") == 0)
            newFile = new File(newPathFile+"a");
          else newFile = new File(newPathFile);

          if (extension.compareTo(".jar")!=0) {

            // the content of the current file is copied on the disk

            // the current file for writing characters

            FileWriter fileWriter = new FileWriter(newFile);

            InputStreamReader inputStreamReader = new InputStreamReader (
                                                    currentInputStream);

            int  charRead = 0;
            String text = null;

            while(
            (charRead = inputStreamReader.read(cbuffer,0,BUFF_SIZE)) != -1){

              text = new String (cbuffer,0,charRead);

              String expr1 = "public class " + names.get("___CLASSNAME___");
              String expr2 = "import ___PACKAGE___.*;";
              String newText;

              if (packages.length() == 0){
                newText = regularExpressions(text,"",expr2);
              }
              else {
                newText = regularExpressions(text,packages,expr2);
              }

              newText = addContent(newText,expr1,interfacesAndClass);
              fileWriter.write(newText ,0,newText.length());

            } // while

            inputStreamReader.close();

            // close the input stream
            currentInputStream.close();

            // close the file for writing
            fileWriter.close();

          } // if
        } // else
      } // while
    }// while
  } // modify
} // class BootStrap
