/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 25/10/2001
 *
 *  $Id$
 *
 */
package gate.util.persistence;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.event.*;
import gate.gui.MainFrame;
import gate.persist.PersistenceException;

import java.util.*;
import java.io.*;
import java.text.NumberFormat;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This class provides utility methods for saving resources through
 * serialisation via static methods.
 */
public class PersistenceManager {

  /**
   * A reference to an object; it uses the identity hashcode and the equals
   * defined by object identity.
   * These values will be used as keys in the
   * {link #existingPersitentReplacements} map.
   */
  static protected class ObjectHolder{
    ObjectHolder(Object target){
      this.target = target;
    }

    public int hashCode(){
      return System.identityHashCode(target);
    }

    public boolean equals(Object obj){
      if(obj instanceof ObjectHolder)
        return ((ObjectHolder)obj).target == this.target;
      else return false;
    }

    public Object getTarget(){
      return target;
    }

    private Object target;
  }//static class ObjectHolder{

  /**
   * This class is used as a marker for types that should NOT be serialised when
   * saving the state of a gate object.
   * Registering this type as the persistent equivalent for a specific class
   * (via {@link PersistenceManager#registerPersitentEquivalent(Class , Class)})
   * effectively stops all values of the specified type from being serialised.
   *
   * Maps that contain values that should not be serialised will have that entry
   * removed. In any other places where such values occur they will be replaced
   * by null after deserialisation.
   */
  public static class SlashDevSlashNull implements Persistence{
    /**
     * Does nothing
     */
    public void extractDataFromSource(Object source)throws PersistenceException{
    }

    /**
     * Returns null
     */
    public Object createObject()throws PersistenceException,
                                       ResourceInstantiationException{
      return null;
    }
    static final long serialVersionUID = -8665414981783519937L;
  }

  /**
   * URLs get upset when serialised and deserialised so we need to convert them
   * to strings for storage.
   * In the case of &quot;file:&quot; URLs the relative path to the persistence
   * file will actually be stored.
   */
  public static class URLHolder implements Persistence{
    /**
     * Populates this Persistence with the data that needs to be stored from the
     * original source object.
     */
    public void extractDataFromSource(Object source)throws PersistenceException{
      try{
        URL url = (URL)source;
        if(url.getProtocol().equals("file")){
          try{
            urlString = relativePathMarker +
                        getRelativePath(persistenceFile.toURL(), url);
          }catch(MalformedURLException mue){
            urlString = ((URL)source).toExternalForm();
          }
        }else{
          urlString = ((URL)source).toExternalForm();
        }
      }catch(ClassCastException cce){
        throw new PersistenceException(cce);
      }
    }

    /**
     * Creates a new object from the data contained. This new object is supposed
     * to be a copy for the original object used as source for data extraction.
     */
    public Object createObject()throws PersistenceException{
      try{
        if(urlString.startsWith(relativePathMarker)){
          URL context = persistenceFile.toURL();
          return new URL(context,
                         urlString.substring(relativePathMarker.length()));
        }else return new URL(urlString);
      }catch(MalformedURLException mue){
        throw new PersistenceException(mue);
      }
    }
    String urlString;
    /**
     * This string will be used to start the serialisation of URL that represent
     * relative paths.
     */
    private static final String relativePathMarker = "$relpath$";
    static final long serialVersionUID = 7943459208429026229L;
  }

  public static class ClassComparator implements Comparator{
    /**
     * Compares two {@link Class} values in terms of specificity; the more
     * specific class is said to be &quot;smaller&quot; than the more generic
     * one hence the {@link Object} class is the &quot;largest&quot; possible
     * class.
     * When two classes are not comparable (i.e. not assignable from each other)
     * in either direction a NotComparableException will be thrown.
     * both input objects should be Class values otherwise a
     * {@link ClassCastException} will be thrown.
     *
     */
    public int compare(Object o1, Object o2){
      Class c1 = (Class)o1;
      Class c2 = (Class)o2;

      if(c1.equals(c2)) return 0;
      if(c1.isAssignableFrom(c2)) return 1;
      if(c2.isAssignableFrom(c1)) return -1;
      throw new NotComparableException();
    }
  }

  /**
   * Thrown by a comparator when the values provided for comparison are not
   * comparable.
   */
  public static class NotComparableException extends RuntimeException{
    public NotComparableException(String message){
      super(message);
    }
    public NotComparableException(){
    }
  }

  /**
   * Recursively traverses the provided object and replaces it and all its
   * contents with the appropiate persistent equivalent classes.
   *
   * @param the object to be analised and translated into a persistent
   * equivalent.
   * @return the persistent equivalent value for the provided target
   */
  static Serializable getPersistentRepresentation(Object target)
                      throws PersistenceException{
    if(target == null) return null;
    //first check we don't have it already
    Persistence res = (Persistence)existingPersitentReplacements.
                      get(new ObjectHolder(target));
    if(res != null) return res;

    Class type = target.getClass();
    Class newType = getMostSpecificPersistentType(type);
    if(newType == null){
      //no special handler
      if(target instanceof Serializable) return (Serializable)target;
      else throw new PersistenceException(
                     "Could not find a serialisable replacement for " + type);
    }

    //we have a new type; create the new object, populate and return it
    try{
      res = (Persistence)newType.newInstance();
    }catch(Exception e){
      throw new PersistenceException(e);
    }
    if(target instanceof NameBearer){
      StatusListener sListener = (StatusListener)MainFrame.getListeners().
                                 get("gate.event.StatusListener");
      if(sListener != null){
        sListener.statusChanged("Storing " + ((NameBearer)target).getName());
      }
    }
    res.extractDataFromSource(target);
    existingPersitentReplacements.put(new ObjectHolder(target), res);
    return res;
  }


  static Object getTransientRepresentation(Object target)
                      throws PersistenceException,
                             ResourceInstantiationException{

    if(target == null || target instanceof SlashDevSlashNull) return null;
    if(target instanceof Persistence){
      Object resultKey = new ObjectHolder(target);
      //check the cached values; maybe we have the result already
      Object result = existingTransientValues.get(resultKey);
      if(result != null) return result;

      //we didn't find the value: create it
      result = ((Persistence)target).createObject();
      existingTransientValues.put(resultKey, result);
      return result;
    }else return target;
  }


  /**
   * Finds the most specific persistent replacement type for a given class.
   * Look for a type that has a registered persistent equivalent starting from
   * the provided class continuing with its superclass and implemented
   * interfaces and their superclasses and implemented interfaces and so on
   * until a type is found.
   * Classes are considered to be more specific than interfaces and in
   * situations of ambiguity the most specific types are considered to be the
   * ones that don't belong to either java or GATE followed by the ones  that
   * belong to GATE and followed by the ones that belong to java.
   *
   * E.g. if there are registered persitent types for {@link gate.Resource} and
   * for {@link gate.LanguageResource} than such a request for a
   * {@link gate.Document} will yield the registered type for
   * {@link gate.LanguageResource}.
   */
  protected static Class getMostSpecificPersistentType(Class type){
    //this list will contain all the types we need to expand to superclass +
    //implemented interfaces. We start with the provided type and work our way
    //up the ISA hierarchy
    List expansionSet = new ArrayList();
    expansionSet.add(type);

    //algorithm:
    //1) check the current expansion set
    //2) expand the expansion set

    //at each expansion stage we'll have a class and three lists of interfaces:
    //the user defined ones; the GATE ones and the java ones.
    List userInterfaces = new ArrayList();
    List gateInterfaces = new ArrayList();
    List javaInterfaces = new ArrayList();
    while(!expansionSet.isEmpty()){
      //1) check the current set
      Iterator typesIter = expansionSet.iterator();
      while(typesIter.hasNext()){
        Class result = (Class)persistentReplacementTypes.get(typesIter.next());
        if(result != null){
          return result;
        }
      }
      //2) expand the current expansion set;
      //the expanded expansion set will need to be ordered according to the
      //rules (class >> interface; user interf >> gate interf >> java interf)

      //at each point we only have at most one class
      if(type != null) type = type.getSuperclass();


      userInterfaces.clear();
      gateInterfaces.clear();
      javaInterfaces.clear();

      typesIter = expansionSet.iterator();
      while(typesIter.hasNext()){
        Class aType = (Class)typesIter.next();
        Class[] interfaces = aType.getInterfaces();
        //distribute them according to their type
        for(int i = 0; i < interfaces.length; i++){
          Class anIterf = interfaces[i];
          String interfType = anIterf.getName();
          if(interfType.startsWith("java")){
            javaInterfaces.add(anIterf);
          }else if(interfType.startsWith("gate")){
            gateInterfaces.add(anIterf);
          }else userInterfaces.add(anIterf);
        }
      }

      expansionSet.clear();
      if(type != null) expansionSet.add(type);
      expansionSet.addAll(userInterfaces);
      expansionSet.addAll(gateInterfaces);
      expansionSet.addAll(javaInterfaces);
    }
    //we got out the while loop without finding anything; return null;
    return null;

//    SortedSet possibleTypesSet = new TreeSet(classComparator);
//
//    Iterator typesIter = persistentReplacementTypes.keySet().iterator();
//    //we store all the types that could not be analysed
//    List lostTypes = new ArrayList();
//    while(typesIter.hasNext()){
//      Class aType = (Class)typesIter.next();
//      if(aType.isAssignableFrom(type)){
//        try{
//          possibleTypesSet.add(aType);
//        }catch(NotComparableException nce){
//          lostTypes.add(aType);
//        }
//      }
//    }
//
//    if(possibleTypesSet.isEmpty())  return null;
//
//    Class resultKey = (Class)possibleTypesSet.first();
//    Class result = (Class) persistentReplacementTypes.get(resultKey);
//
//    //check whether we lost anything important
//    typesIter = lostTypes.iterator();
//    while(typesIter.hasNext()){
//      Class aType = (Class)typesIter.next();
//      try{
//        if(classComparator.compare(aType, resultKey) < 0){
//          Err.prln("Found at least two incompatible most specific types for " +
//          type.getName() + ":\n " +
//          aType.toString() + " was discarded in favour of" + result.getName() +
//          ".\nSome of your saved results may have been lost!");
//        }
//      }catch(NotComparableException nce){
//        Err.prln("Found at least two incompatible most specific types for " +
//        type.getName() + ":\n " +
//        aType.toString() + " was discarded in favour of" + result.getName() +
//        ".\nSome of your saved results may have been lost!");
//      }
//    }
//
//    return result;
  }

  /**
   * Calculates the relative path for a file: URL starting from a given context
   * which is also a file: URL.
   * @param context the URL to be used as context.
   * @param target the URL for which the relative path is computed.
   * @return a String value representing the relative path. Constructing a URL
   * from the context URL and the relative path should result in the target URL.
   */
  public static String getRelativePath(URL context, URL target){
    if(context.getProtocol().equals("file") &&
       target.getProtocol().equals("file")){

      //normalise the two file URLS
      try{
        context = new File(context.getPath()).toURL();
      }catch(MalformedURLException mue){
        throw new GateRuntimeException("Could not normalise the file URL:\n"+
                                       context + "\nThe problem was:\n" +mue);
      }
      try{
        target = new File(target.getPath()).toURL();
      }catch(MalformedURLException mue){
        throw new GateRuntimeException("Could not normalise the file URL:\n"+
                                       target + "\nThe problem was:\n" +mue);
      }
      List targetPathComponents = new ArrayList();
      File aFile = new File(target.getPath()).getParentFile();
      while(aFile != null){
        targetPathComponents.add(0, aFile);
        aFile = aFile.getParentFile();
      }
      List contextPathComponents = new ArrayList();
      aFile = new File(context.getPath()).getParentFile();
      while(aFile != null){
        contextPathComponents.add(0, aFile);
        aFile = aFile.getParentFile();
      }
      //the two lists can have 0..n common elements (0 when the files are
      //on separate roots
      int commonPathElements = 0;
      while(commonPathElements < targetPathComponents.size() &&
            commonPathElements < contextPathComponents.size() &&
            targetPathComponents.get(commonPathElements).
            equals(contextPathComponents.get(commonPathElements)))
        commonPathElements++;
      //construct the string for the relative URL
      String relativePath = "";
      for(int i = commonPathElements;
          i < contextPathComponents.size(); i++){
        if(relativePath.length() == 0) relativePath += "..";
        else relativePath += "/..";
      }
      for(int i = commonPathElements; i < targetPathComponents.size(); i++){
        String aDirName = ((File)targetPathComponents.get(i)).getName();
        if(aDirName.length() == 0){
          aDirName = ((File)targetPathComponents.get(i)).getAbsolutePath();
          if(aDirName.endsWith(File.separator)){
            aDirName = aDirName.substring(0, aDirName.length() -
                                             File.separator.length());
          }
        }
//Out.prln("Adding \"" + aDirName + "\" name for " + targetPathComponents.get(i));
        if(relativePath.length() == 0){
          relativePath += aDirName;
        }else{
          relativePath += "/" + aDirName;
        }
      }
      //we have the directory; add the file name
      if(relativePath.length() == 0){
        relativePath += new File(target.getPath()).getName();
      }else{
        relativePath += "/" + new File(target.getPath()).getName();
      }

      return relativePath;
    }else{
      throw new GateRuntimeException("Both the target and the context URLs " +
                                     "need to be \"file:\" URLs!");
    }
  }

  public static void saveObjectToFile(Object obj, File file)
                     throws PersistenceException, IOException {
    ProgressListener pListener = (ProgressListener)MainFrame.getListeners()
                                 .get("gate.event.ProgressListener");
    StatusListener sListener = (gate.event.StatusListener)
                               MainFrame.getListeners().
                               get("gate.event.StatusListener");
    long startTime = System.currentTimeMillis();
    if(pListener != null) pListener.progressChanged(0);
    ObjectOutputStream oos = null;
    persistenceFile = file;
    try{
      //insure a clean start
      existingPersitentReplacements.clear();
      existingPersitentReplacements.clear();

      oos = new ObjectOutputStream(new FileOutputStream(file));

      //always write the list of creole URLs first
      List urlList = new ArrayList(Gate.getCreoleRegister().getDirectories());
      Object persistentList = getPersistentRepresentation(urlList);
      oos.writeObject(persistentList);

      //now write the object
      Object persistentObject = getPersistentRepresentation(obj);
      oos.writeObject(persistentObject);
    }finally{
      persistenceFile = null;
      if(oos != null){
        oos.flush();
        oos.close();
      }
      long endTime = System.currentTimeMillis();
      if(sListener != null) sListener.statusChanged(
          "Storing completed in " +
          NumberFormat.getInstance().format(
          (double)(endTime - startTime) / 1000) + " seconds");
      if(pListener != null) pListener.processFinished();
    }
  }

  public static Object loadObjectFromFile(File file)
                     throws PersistenceException, IOException,
                            ResourceInstantiationException {
    exceptionOccured = false;
    ProgressListener pListener = (ProgressListener)MainFrame.getListeners().
                                 get("gate.event.ProgressListener");
    StatusListener sListener = (gate.event.StatusListener)
                                MainFrame.getListeners()
                                .get("gate.event.StatusListener");
    if(pListener != null) pListener.progressChanged(0);
    long startTime = System.currentTimeMillis();
    persistenceFile = file;
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
    Object res = null;
    try{
      //first read the list of creole URLs
      Iterator urlIter = ((Collection)
                          getTransientRepresentation(ois.readObject())).
                          iterator();
      while(urlIter.hasNext()){
        URL anUrl = (URL)urlIter.next();
        try{
          if(!Gate.getCreoleRegister().getDirectories().contains(anUrl))
            Gate.getCreoleRegister().registerDirectories(anUrl);
        }catch(GateException ge){
          Err.prln("Could not reload creole directory " +
                   anUrl.toExternalForm());
        }
      }
      //now we can read the saved object
      res = ois.readObject();
      ois.close();

      //ensure a fresh start
      existingTransientValues.clear();
      res = getTransientRepresentation(res);
      existingTransientValues.clear();
      long endTime = System.currentTimeMillis();
              if(sListener != null) sListener.statusChanged(
                  "Loading completed in " +
                  NumberFormat.getInstance().format(
                  (double)(endTime - startTime) / 1000) + " seconds");
              if(pListener != null) pListener.processFinished();
      if(exceptionOccured){
        throw new PersistenceException("There were errors!\n" +
                                       "See messages for details...");
      }
      return res;
    }catch(ResourceInstantiationException rie){
      if(sListener != null) sListener.statusChanged("Loading failed!");
      if(pListener != null) pListener.processFinished();
      throw rie;
    }catch(Exception ex){
      if(sListener != null) sListener.statusChanged("Loading failed!");
      if(pListener != null) pListener.processFinished();
      throw new PersistenceException(ex);
    }finally{
      persistenceFile = null;
    }
  }


  /**
   * Sets the persistent equivalent type to be used to (re)store a given type
   * of transient objects.
   * @param transientType the type that will be replaced during serialisation
   * operations
   * @param persistentType the type used to replace objects of transient type
   * when serialising; this type needs to extend {@link Persistence}.
   * @return the persitent type that was used before this mapping if such
   * existed.
   */
  public static Class registerPersitentEquivalent(Class transientType,
                                          Class persistentType)
               throws PersistenceException{
    if(!Persistence.class.isAssignableFrom(persistentType)){
      throw new PersistenceException(
        "Persistent equivalent types have to implement " +
        Persistence.class.getName() + "!\n" +
        persistentType.getName() + " does not!");
    }
    return (Class)persistentReplacementTypes.put(transientType, persistentType);
  }


  /**
   * A dictionary mapping from java type (Class) to the type (Class) that can
   * be used to store persistent data for the input type.
   */
  private static Map persistentReplacementTypes;

  /**
   * Stores the persistent replacements created during a transaction in order to
   * avoid creating two different persistent copies for the same object.
   * The keys used are {@link ObjectHolder}s that contain the transient values
   * being converted to persistent equivalents.
   */
  private static Map existingPersitentReplacements;

  /**
   * Stores the transient values obtained from persistent replacements during a
   * transaction in order to avoid creating two different transient copies for
   * the same persistent replacement.
   * The keys used are {@link ObjectHolder}s that hold persistent equivalents.
   * The values are the transient values created by the persisten equivalents.
   */
  private static Map existingTransientValues;

  private static ClassComparator classComparator = new ClassComparator();

  /**
   * This flag is set to true when an exception occurs. It is used in order to
   * allow error reporting without interrupting the current operation.
   */
  static boolean exceptionOccured = false;

  /**
   * The file currently used to write/read the persisten representation.
   * Will only have a non-null value during storing and restorin operations.
   */
  static File persistenceFile;

  static{
    persistentReplacementTypes = new HashMap();
    try{
      //VRs don't get saved, ....sorry guys :)
      registerPersitentEquivalent(VisualResource.class,
                                  SlashDevSlashNull.class);

      registerPersitentEquivalent(URL.class, URLHolder.class);

      registerPersitentEquivalent(Map.class, MapPersistence.class);
      registerPersitentEquivalent(Collection.class,
                                  CollectionPersistence.class);

      registerPersitentEquivalent(ProcessingResource.class,
                                  PRPersistence.class);

      registerPersitentEquivalent(DataStore.class,
                                  DSPersistence.class);

      registerPersitentEquivalent(LanguageResource.class,
                                  LRPersistence.class);

      registerPersitentEquivalent(Corpus.class,
                                  CorpusPersistence.class);

      registerPersitentEquivalent(Controller.class,
                                  ControllerPersistence.class);

      registerPersitentEquivalent(ConditionalController.class,
                                  ConditionalControllerPersistence.class);

      registerPersitentEquivalent(LanguageAnalyser.class,
                                  LanguageAnalyserPersistence.class);

      registerPersitentEquivalent(SerialAnalyserController.class,
                                  SerialAnalyserControllerPersistence.class);

      registerPersitentEquivalent(gate.persist.JDBCDataStore.class,
                                  JDBCDSPersistence.class);
      registerPersitentEquivalent(gate.creole.AnalyserRunningStrategy.class,
                                  AnalyserRunningStrategyPersistence.class);
    }catch(PersistenceException pe){
      //builtins shouldn't raise this
      pe.printStackTrace();
    }
    existingPersitentReplacements = new HashMap();
    existingTransientValues = new HashMap();
  }
}