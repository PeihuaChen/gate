/*
 *  OClassImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OClassImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Implementation of the OClass interface
 * 
 * @author niraj
 * 
 */
public class OClassImpl extends OResourceImpl implements OClass {
  /**
   * Constructor
   * 
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public OClassImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#addSubClass(gate.creole.ontology.OClass)
   */
  public void addSubClass(OClass subClass) {
    try {
      // lets first check if the current class is a subclass of the
      // subClass. If so,
      // we don't allow this.
      if(this == subClass) {
        Utils
                .warning("addSubClass(subClass) : The super and sub classes are same.");
        return;
      }

      if(this.isSubClassOf(subClass, OConstants.TRANSITIVE_CLOSURE)) {
        Utils.warning(subClass.getURI().toString() + " is a super class of "
                + this.getURI().toString());
        return;
      }
      
      if(this.isSuperClassOf(subClass, OConstants.DIRECT_CLOSURE)) {
        Utils.warning(subClass.getURI().toString() + " is already a sub class of "
                + this.getURI().toString());
        return;
      }

      owlim.addSubClass(this.repositoryID, this.uri.toString(), subClass
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.SUB_CLASS_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#removeSubClass(gate.creole.ontology.OClass)
   */
  public void removeSubClass(OClass subClass) {
    try {

      if(this == subClass) {
        Utils.warning("addSubClass(subClass) : The super and sub classes are same.");
        return;
      }

      if(!subClass.isSubClassOf(this, OConstants.DIRECT_CLOSURE)) {
        Utils.warning(subClass.getURI().toString()
                + " is not a direct subclass of " + this.getURI().toString());
        return;
      }

      owlim.removeSubClass(this.repositoryID, this.uri.toString(), subClass.getURI().toString());
      
//      // here we need to find out all its instances
//      // and remove inconsistant or invalid property values
//      Set<OInstance> instances = ontology.getOInstances(subClass, OConstants.TRANSITIVE_CLOSURE);
//      for(OInstance anInst : instances) {
//        // ok so we need to find out all set properties on this instance
//        Set<RDFProperty> setProps = anInst.getAllSetProperties();
//        
//        // and find out all applicable properties
//        Set<RDFProperty> applicableProps = new HashSet<RDFProperty>();
//        Set<OClass> instOClasses = anInst.getOClasses(OConstants.DIRECT_CLOSURE);
//        for(OClass aClass : instOClasses) {
//          System.out.println(aClass.getURI().toString());
//          applicableProps.addAll(aClass.getPropertiesWithResourceAsDomain());
//        }
//        
//        System.out.println("Applicable Props : ");
//        for(RDFProperty rProp : applicableProps) {
//          System.out.println("\t"+rProp.getURI().toString());
//        }
//        
//        System.out.println("Set Props : ");
//        // and now for each property in setProps, check if it is not in applicableProps, remove it from the inst
//        for(RDFProperty rProp : setProps) {
//            System.out.println("\t"+rProp.getURI().toString());
//          if(!applicableProps.contains(rProp)) {
//              if(rProp instanceof AnnotationProperty) {
//                anInst.removeAnnotationPropertyValues((AnnotationProperty) rProp);
//              } else if(rProp instanceof DatatypeProperty) {
//                anInst.removeDatatypePropertyValues((DatatypeProperty) rProp);
//              } else if(rProp instanceof ObjectProperty) {
//                anInst.removeObjectPropertyValues((ObjectProperty) rProp); 
//              } else {
//                anInst.removeRDFPropertyValues(rProp);
//              }
//           }
//        }
//      }
      
      ontology.fireOntologyModificationEvent(this,
              OConstants.SUB_CLASS_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#getSubClasses(byte)
   */
  public Set<OClass> getSubClasses(byte closure) {
    try {
      ResourceInfo[] subClasses = owlim.getSubClasses(this.repositoryID,
              this.uri.toString(), closure);
      Set<OClass> oClasses = new HashSet<OClass>();
      for(int i = 0; i < subClasses.length; i++) {
        oClasses.add(Utils
                .createOClass(this.repositoryID, this.ontology, this.owlim,
                        subClasses[i].getUri(), subClasses[i].isAnonymous()));
      }
      return oClasses;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#getSuperClasses(byte)
   */
  public Set<OClass> getSuperClasses(byte closure) {
    try {
      ResourceInfo[] superClasses = owlim.getSuperClasses(this.repositoryID,
              this.uri.toString(), closure);
      Set<OClass> oClasses = new HashSet<OClass>();
      for(int i = 0; i < superClasses.length; i++) {
        oClasses.add(Utils.createOClass(this.repositoryID, this.ontology,
                this.owlim, superClasses[i].getUri(), superClasses[i]
                        .isAnonymous()));
      }
      return oClasses;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#isSuperClassOf(gate.creole.ontology.OClass,
   *      byte)
   */
  public boolean isSuperClassOf(OClass aClass, byte closure) {
    try {
      return owlim.isSuperClassOf(this.repositoryID, this.uri.toString(),
              aClass.getURI().toString(), closure);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#isSubClassOf(gate.creole.ontology.OClass,
   *      byte)
   */
  public boolean isSubClassOf(OClass aClass, byte closure) {
    try {
      return owlim.isSubClassOf(this.repositoryID, aClass.getURI().toString(),
              this.uri.toString(), closure);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#isTopClass()
   */
  public boolean isTopClass() {
    try {
      return owlim.isTopClass(this.repositoryID, this.uri.toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#setSameClassAs(gate.creole.ontology.OClass)
   */
  public void setEquivalentClassAs(OClass theClass) {
    try {
      // lets first check if the current class is a subclass of the
      // subClass. If so,
      // we don't allow this.
      if(this == theClass) {
        Utils
                .warning("setEquivalentClassAs(theClass) : Both the source and the argument classes refer to the same instance of class");
        return;
      }

      owlim.setEquivalentClassAs(this.repositoryID, this.uri.toString(),
              theClass.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.EQUIVALENT_CLASS_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#getSameClasses()
   */
  public Set<OClass> getEquivalentClasses() {
    try {
      ResourceInfo[] eqClasses = owlim.getEquivalentClasses(this.repositoryID,
              this.uri.toString());
      Set<OClass> oClasses = new HashSet<OClass>();
      for(int i = 0; i < eqClasses.length; i++) {
        oClasses.add(Utils.createOClass(this.repositoryID, this.ontology,
                this.owlim, eqClasses[i].getUri(), eqClasses[i].isAnonymous()));
      }
      return oClasses;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#isSameClassAs(gate.creole.ontology.OClass)
   */
  public boolean isEquivalentClassAs(OClass aClass) {
    try {
      return owlim.isEquivalentClassAs(this.repositoryID, this.uri.toString(),
              aClass.getURI().toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * Gets the super classes, and returns them in an array list where on
   * each index there is a collection of the super classes at distance -
   * the index.
   */
  public ArrayList<Set<OClass>> getSuperClassesVSDistance() {
    try {
      ArrayList<Set<OClass>> result = new ArrayList<Set<OClass>>();
      Set<OClass> set;
      int level = 0;
      OClass c;
      Set<OClass> levelSet = new HashSet<OClass>();
      levelSet.add(this);
      boolean rollon = (0 < owlim.getSuperClasses(this.repositoryID, this.uri
              .toString(), OConstants.DIRECT_CLOSURE).length);
      while(rollon) {
        set = new HashSet<OClass>();
        Iterator<OClass> li = levelSet.iterator();
        while(li.hasNext()) {
          c = li.next();
          set.addAll(c.getSuperClasses(OConstants.DIRECT_CLOSURE));
        }
        if(0 < set.size()) {
          result.add(level++, set);
        }
        levelSet = set;
        rollon = 0 < levelSet.size();
      }
      return result;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * Gets the sub classes, and returns them in an array list where on
   * each index there is a collection of the sub classes at distance -
   * the index.
   */
  public ArrayList<Set<OClass>> getSubClassesVsDistance() {
    try {
      ArrayList<Set<OClass>> result = new ArrayList<Set<OClass>>();
      Set<OClass> set;
      int level = 0;
      OClass c;
      Set<OClass> levelSet = new HashSet<OClass>();
      levelSet.add(this);
      boolean rollon = (0 < owlim.getSubClasses(this.repositoryID, this.uri
              .toString(), OConstants.DIRECT_CLOSURE).length);
      while(rollon) {
        set = new HashSet<OClass>();
        Iterator<OClass> li = levelSet.iterator();
        while(li.hasNext()) {
          c = li.next();
          set.addAll(c.getSubClasses(OConstants.DIRECT_CLOSURE));
        }
        if(0 < set.size()) {
          result.add(level++, set);
        }
        levelSet = set;
        rollon = 0 < levelSet.size();
      }
      return result;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

}
