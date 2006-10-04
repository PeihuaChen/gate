package gate.creole.ontology;

import java.util.EventObject;

/**
 * @author niraj OntologyModificationEvent
 */
public class OntologyModificationEvent extends EventObject {
  private Taxonomy source;

  private Object resource;

  private int eventType;

  public static final int LABEL_CHANGED_EVENT = 0;

  public static final int COMMENT_CHANGED_EVENT = 1;

  public static final int ID_CHANGED_EVENT = 2;

  public static final int VERSION_CHANGED_EVENT = 3;

  public static final int DEFAULT_NAMESPACE_CHANGED_EVENT = 4;

  public static final int ONTOLOGY_RESOURCE_ADDED = 5;

  public static final int ONTOLOGY_RESOURCE_REMOVED = 6;

  public static final int SUB_PROPERTY_ADDED_EVENT = 7;

  public static final int SUB_PROPERTY_REMOVED_EVENT = 8;

  public static final int SUPER_PROPERTY_ADDED_EVENT = 9;

  public static final int SUPER_PROPERTY_REMOVED_EVENT = 10;

  public static final int SUB_CLASS_ADDED_EVENT = 11;

  public static final int SUB_CLASS_REMOVED_EVENT = 12;

  public static final int SUPER_CLASS_ADDED_EVENT = 13;

  public static final int SUPER_CLASS_REMOVED_EVENT = 14;

  public static final int INVERSE_PROPERTY_EVENT = 15;

  public static final int DISJOINT_CLASS_EVENT = 16;

  public static final int SAME_AS_EVENT = 17;

  public static final int PROPERTY_VALUE_ADDED_EVENT = 18;

  public static final int PROPERTY_VALUE_REMOVED_EVENT = 19;

  public static final int NAME_CHANGED_EVENT = 20;

  public static final int URI_CHANGED_EVENT = 21;

  public static final int FUNCTIONAL_EVENT = 22;

  public static final int INVERSE_FUNCTIONAL_EVENT = 23;

  /**
   * Constructor
   * 
   * @param ontology
   * @param resource
   */
  public OntologyModificationEvent(Taxonomy taxonomy, Object resource,
          int eventType) {
    super(taxonomy);
    this.source = taxonomy;
    this.resource = resource;
    this.eventType = eventType;
  }

  public Taxonomy getSource() {
    return this.source;
  }

  public Object getResource() {
    return this.resource;
  }

  public int getEventType() {
    return this.eventType;
  }
}
