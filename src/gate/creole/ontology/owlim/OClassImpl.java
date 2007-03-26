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
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Implementation of the OClass interface
 * @author niraj
 * 
 */
public class OClassImpl extends OResourceImpl implements OClass {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort 
   */
  public OClassImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIMServiceImpl owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort); 
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#addSubClass(gate.creole.ontology.OClass)
   */
  public void addSubClass(OClass subClass) {
    try {
      owlim.addSubClass(this.repositoryID, this.uri.toString(), subClass
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this, OConstants.SUB_CLASS_ADDED_EVENT);
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#addSuperClass(gate.creole.ontology.OClass)
   */
  public void addSuperClass(OClass superClass) {
    try {
      owlim.addSuperClass(this.repositoryID, superClass.getURI().toString(),
              this.uri.toString());
      ontology.fireOntologyModificationEvent(this, OConstants.SUPER_CLASS_ADDED_EVENT);
    } catch(RemoteException re) {
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
      owlim.removeSubClass(this.repositoryID, this.uri.toString(), subClass
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this, OConstants.SUB_CLASS_REMOVED_EVENT);
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OClass#removeSuperClass(gate.creole.ontology.OClass)
   */
  public void removeSuperClass(OClass superClass) {
    try {
      owlim.removeSuperClass(this.repositoryID, superClass.getURI().toString(),
              this.uri.toString());
      ontology.fireOntologyModificationEvent(this, OConstants.SUPER_CLASS_REMOVED_EVENT);
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
      owlim.setEquivalentClassAs(this.repositoryID, this.uri.toString(),
              theClass.getURI().toString());
      ontology.fireOntologyModificationEvent(this, OConstants.EQUIVALENT_CLASS_EVENT);
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
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
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * Gets the super classes, and returns them in an array list where on each
   * index there is a collection of the super classes at distance - the index.
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
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * Gets the sub classes, and returns them in an array list where on each
   * index there is a collection of the sub classes at distance - the index.
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
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

}
