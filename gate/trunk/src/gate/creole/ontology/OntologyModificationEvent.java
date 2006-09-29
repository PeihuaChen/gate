package gate.creole.ontology;

import java.util.EventObject;

/**
 * @author niraj
 * OntologyModificationEvent
 */
public class OntologyModificationEvent extends EventObject {
	
	private Taxonomy source;
	private Object resource;
	private int eventType;
	
	public static final int ONTOLOGY_LABEL_CHANGED = 0;
	public static final int ONTOLOGY_COMMENT_CHANGED = 1;
	public static final int ONTOLOGY_ID_CHANGED = 2;
	public static final int ONTOLOGY_VERSION_CHANGED = 3;
	public static final int ONTOLOGY_DEFAULT_NAMESPACE_CHANGED = 4;
	public static final int ONTOLOGY_RESOURCE_ADDED = 5;
	public static final int ONTOLOGY_RESOURCE_REMOVED = 6;
	
	/**
	 * Constructor
	 * @param ontology
	 * @param resource
	 */
	public OntologyModificationEvent(Taxonomy taxonomy, Object resource, int eventType) {
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
