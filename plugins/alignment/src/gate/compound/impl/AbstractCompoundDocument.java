package gate.compound.impl;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import gate.AnnotationSet;
import gate.DataStore;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.compound.CompoundDocument;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.event.CreoleEvent;
import gate.event.DatastoreEvent;
import gate.event.DocumentListener;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;
import gate.util.Strings;

/**
 * This is an abstract implementation of the AbstractAlignedDocument
 * This class overrides the methods of DocumentImpl and provide
 * generic implementation of some of the methods of AlignedDocument
 * @author niraj
 */
public abstract class AbstractCompoundDocument extends DocumentImpl implements CompoundDocument {
 
	/** The encoding of the source of the document content */
	protected String encoding = null;

	/**
	 * If you set this flag to true the repositioning information for the
	 * document will be kept in the document feature. <br>
	 * Default value is false to avoid the unnecessary waste of time and memory
	 */
	protected Boolean collectRepositioningInfo = new Boolean(false);

	/**
	 * If you set this flag to true the original content of the document will be
	 * kept in the document feature. <br>
	 * Default value is false to avoid the unnecessary waste of memory
	 */
	protected Boolean preserveOriginalContent = new Boolean(false);

	/**
	 * Languages
	 */
	protected List<String> documentIDs;

	/** The source URL */
	protected URL sourceUrl;

	/**
	 * Curent Document
	 */
	protected Document currentDocument;

	/**
	 * Available documents
	 */
	protected Map<String, Document> documents;

	/** Freeze the serialization UID. */
	static final long serialVersionUID = -8456893608311510260L;

	private transient Vector<DocumentListener> documentListeners;

	/**
	 * The start of the range that the content comes from at the source URL (or
	 * null if none).
	 */
	protected Long sourceUrlStartOffset;

	/**
	 * The end of the range that the content comes from at the source URL (or
	 * null if none).
	 */
	protected Long sourceUrlEndOffset;

	/** Is the document markup-aware? */
	protected Boolean markupAware = new Boolean(false);

	/** Clear all the data members of the object. */
	public void cleanup() {
		Iterator<Document> iter = documents.values().iterator();
		while (iter.hasNext()) {
			Document doc = iter.next();
			doc.cleanup();
		}
	} // cleanup()

	/** Cover unpredictable Features creation */
	public FeatureMap getFeatures() {
		if (currentDocument == null) {
			if (features == null) {
				features = new SimpleFeatureMapImpl();
			}
			return features;
		} else {
			return currentDocument.getFeatures();
		}
	}

	/** Documents are identified by URLs */
	public URL getSourceUrl() {
		if (currentDocument == null) {
			return sourceUrl;
		}
		return currentDocument.getSourceUrl();
	}

	/** Set method for the document's URL */
	public void setSourceUrl(URL sourceUrl) {
		if (currentDocument == null) {
			this.sourceUrl = sourceUrl;
		} else {
			currentDocument.setSourceUrl(sourceUrl);
		}
	} // setSourceUrl

	/**
	 * Documents may be packed within files; in this case an optional pair of
	 * offsets refer to the location of the document.
	 */
	public Long[] getSourceUrlOffsets() {
		if (currentDocument == null) {
			return new Long[] { sourceUrlStartOffset, sourceUrlEndOffset };
		}
		return currentDocument.getSourceUrlOffsets();
	} // getSourceUrlOffsets

	/**
	 * Allow/disallow preserving of the original document content. If is <B>true</B>
	 * the original content will be retrieved from the DocumentContent object
	 * and preserved as document feature.
	 */
	public void setPreserveOriginalContent(Boolean b) {
		if (currentDocument == null) {
			this.preserveOriginalContent = b;
		} else {
			currentDocument.setPreserveOriginalContent(b);
		}
	} // setPreserveOriginalContent

	/**
	 * Get the preserving of content status of the Document.
	 * 
	 * @return whether the Document should preserve it's original content.
	 */
	public Boolean getPreserveOriginalContent() {
		if (currentDocument == null) {
			return preserveOriginalContent;
		} else {
			return currentDocument.getPreserveOriginalContent();
		}
	} // getPreserveOriginalContent

	/**
	 * Allow/disallow collecting of repositioning information. If is <B>true</B>
	 * information will be retrieved and preserved as document feature.<BR>
	 * Preserving of repositioning information give the possibilities for
	 * converting of coordinates between the original document content and
	 * extracted from the document text.
	 */
	public void setCollectRepositioningInfo(Boolean b) {
		if (currentDocument == null) {
			collectRepositioningInfo = b;
		} else {
			currentDocument.setCollectRepositioningInfo(b);
		}
	} // setCollectRepositioningInfo

	/**
	 * Get the collectiong and preserving of repositioning information for the
	 * Document. <BR>
	 * Preserving of repositioning information give the possibilities for
	 * converting of coordinates between the original document content and
	 * extracted from the document text.
	 * 
	 * @return whether the Document should collect and preserve information.
	 */
	public Boolean getCollectRepositioningInfo() {
		if (currentDocument == null) {
			return collectRepositioningInfo;
		} else {
			return currentDocument.getCollectRepositioningInfo();
		}
	} // getCollectRepositioningInfo

	/**
	 * Documents may be packed within files; in this case an optional pair of
	 * offsets refer to the location of the document. This method gets the start
	 * offset.
	 */
	public Long getSourceUrlStartOffset() {
		if (currentDocument == null) {
			return sourceUrlStartOffset;
		} else {
			return currentDocument.getSourceUrlStartOffset();
		}
	}

	/**
	 * Documents may be packed within files; in this case an optional pair of
	 * offsets refer to the location of the document. This method sets the start
	 * offset.
	 */
	public void setSourceUrlStartOffset(Long sourceUrlStartOffset) {
		if (currentDocument == null) {
			this.sourceUrlStartOffset = sourceUrlStartOffset;
		} else {
			currentDocument.setSourceUrlStartOffset(sourceUrlStartOffset);
		}

	} // setSourceUrlStartOffset

	/**
	 * Documents may be packed within files; in this case an optional pair of
	 * offsets refer to the location of the document. This method gets the end
	 * offset.
	 */
	public Long getSourceUrlEndOffset() {
		if (currentDocument == null) {
			return sourceUrlEndOffset;
		} else {
			return currentDocument.getSourceUrlEndOffset();
		}
	}

	/**
	 * Documents may be packed within files; in this case an optional pair of
	 * offsets refer to the location of the document. This method sets the end
	 * offset.
	 */
	public void setSourceUrlEndOffset(Long sourceUrlEndOffset) {
		if (currentDocument == null) {
			this.sourceUrlEndOffset = sourceUrlEndOffset;
		} else {
			currentDocument.setSourceUrlEndOffset(sourceUrlEndOffset);
		}
	} // setSourceUrlStartOffset

	/** The content of the document: a String for text; MPEG for video; etc. */
	public DocumentContent getContent() {
		if (currentDocument == null) {
//			throw new RuntimeException(
//					"CompoundDocumentImpl does not contain any text but its member does!"
//							+ " Please use the setDocument(String documentID) to set a specific document!");
			return new DocumentContentImpl("");
		} else {	
			return currentDocument.getContent();
		}
	}

	/** Set method for the document content */
	public void setContent(DocumentContent content) {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not contain any text but its member does!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			currentDocument.setContent(content);
		}
	}

	/** Get the encoding of the document content source */
	public String getEncoding() {
		if (currentDocument == null) {
			return this.encoding;
		} else {
			return ((DocumentImpl) currentDocument).getEncoding();
		}
	}

	/** Set the encoding of the document content source */
	public void setEncoding(String encoding) {
		if (currentDocument == null) {
			this.encoding = encoding;
		} else {
			((DocumentImpl) currentDocument).setEncoding(encoding);
		}
	}

	/**
	 * Get the default set of annotations. The set is created if it doesn't
	 * exist yet.
	 */
	public AnnotationSet getAnnotations() {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any annotationSet but its member does!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.getAnnotations();
		}
	} // getAnnotations()

	/**
	 * Get a named set of annotations. Creates a new set if one with this name
	 * doesn't exist yet. If the provided name is null then it returns the
	 * default annotation set.
	 */
	public AnnotationSet getAnnotations(String name) {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any annotationSet but its member does!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.getAnnotations(name);
		}
	} // getAnnotations(name)

	/**
	 * Make the document markup-aware. This will trigger the creation of a
	 * DocumentFormat object at Document initialisation time; the DocumentFormat
	 * object will unpack the markup in the Document and add it as annotations.
	 * Documents are <B>not</B> markup-aware by default.
	 * 
	 * @param newMarkupAware
	 *            markup awareness status.
	 */
	public void setMarkupAware(Boolean newMarkupAware) {
		if (currentDocument == null) {
			this.markupAware = newMarkupAware;
		} else {
			currentDocument.setMarkupAware(newMarkupAware);
		}
	}

	/**
	 * Get the markup awareness status of the Document. <B>Documents are
	 * markup-aware by default.</B>
	 * 
	 * @return whether the Document is markup aware.
	 */
	public Boolean getMarkupAware() {
		if (currentDocument == null) {
			return this.markupAware;
		} else {
			return currentDocument.getMarkupAware();
		}
	}

	/**
	 * Returns an XML document aming to preserve the original markups( the
	 * original markup will be in the same place and format as it was before
	 * processing the document) and include (if possible) the annotations
	 * specified in the aSourceAnnotationSet. It is equivalent to
	 * toXml(aSourceAnnotationSet, true).
	 */
	public String toXml(Set aSourceAnnotationSet) {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any content to convert it to XML but its members have!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.toXml(aSourceAnnotationSet);
		}
	}

	/**
	 * Returns an XML document aming to preserve the original markups( the
	 * original markup will be in the same place and format as it was before
	 * processing the document) and include (if possible) the annotations
	 * specified in the aSourceAnnotationSet. <b>Warning:</b> Annotations from
	 * the aSourceAnnotationSet will be lost if they will cause a crosed over
	 * situation.
	 * 
	 * @param aSourceAnnotationSet
	 *            is an annotation set containing all the annotations that will
	 *            be combined with the original marup set. If the param is
	 *            <code>null</code> it will only dump the original markups.
	 * @param includeFeatures
	 *            is a boolean that controls whether the annotation features
	 *            should be included or not. If false, only the annotation type
	 *            is included in the tag.
	 * @return a string representing an XML document containing the original
	 *         markup + dumped annotations form the aSourceAnnotationSet
	 */
	public String toXml(Set aSourceAnnotationSet, boolean includeFeatures) {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any content to convert it to XML but its members have!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.toXml(aSourceAnnotationSet, includeFeatures);
		}
	}// End toXml()

	/**
	 * Returns a GateXml document that is a custom XML format for wich there is
	 * a reader inside GATE called gate.xml.GateFormatXmlHandler. What it does
	 * is to serialize a GATE document in an XML format.
	 * 
	 * @return a string representing a Gate Xml document.
	 */
	public String toXml() {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any content to convert it to XML but its members have!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.toXml();
		}
	}// toXml

	/**
	 * Returns a map with the named annotation sets. It returns
	 * <code>null</code> if no named annotaton set exists.
	 */
	public Map getNamedAnnotationSets() {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any annotationSets!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.getNamedAnnotationSets();
		}
	} // getNamedAnnotationSets

	/**
	 * Returns a set of all named annotation sets in existence
	 */
	public Set getAnnotationSetNames() {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any annotationSets!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			return currentDocument.getAnnotationSetNames();
		}
	}

	/**
	 * Removes one of the named annotation sets. Note that the default
	 * annotation set cannot be removed.
	 * 
	 * @param name
	 *            the name of the annotation set to be removed
	 */
	public void removeAnnotationSet(String name) {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any annotationSets!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			currentDocument.removeAnnotationSet(name);
		}
	}

	/** Propagate edit changes to the document content and annotations. */
	public void edit(Long start, Long end, DocumentContent replacement)
			throws InvalidOffsetException {
		if (currentDocument == null) {
			throw new RuntimeException(
					"CompoundDocumentImpl does not have any content!"
							+ " Please use the setDocument(String documentID) to set a specific document!");
		} else {
			currentDocument.edit(start, end, replacement);
		}
	} // edit(start,end,replacement)

	/** Ordering based on URL.toString() and the URL offsets (if any) */
	public int compareTo(Object o) throws ClassCastException {
		CompoundDocument other = (CompoundDocument) o;
		return toString().compareTo(other.toString());
	} // compareTo

	/** String respresentation */
	public String toString() {
		if (currentDocument == null) {
			String n = Strings.getNl();
			StringBuffer s = new StringBuffer("CompoundDocumentImpl: " + n);
			s.append("  encoding:" + encoding + n);
			s.append("  features:" + features + n);
			s.append("  markupAware:" + markupAware + n);
			s.append("  sourceUrl:" + sourceUrl + n);
			s.append(n);
			return s.toString();
		} else {
			return currentDocument.toString();
		}
	} // toString

	public void removeDocument(String documentID) {
		Document doc = (Document) documents.get(documentID);
		if(doc == null) return;
		Factory.deleteResource(doc);
	}
	
	
	public synchronized void removeDocumentListener(DocumentListener l) {
		if (currentDocument != null) {
			currentDocument.removeDocumentListener(l);
		} else {
			if (documentListeners != null && documentListeners.contains(l)) {
				Vector v = (Vector) documentListeners.clone();
				v.removeElement(l);
				documentListeners = v;
			}
		}
	}

	public synchronized void addDocumentListener(DocumentListener l) {
		if (currentDocument != null) {
			currentDocument.addDocumentListener(l);
		} else {
			Vector v = documentListeners == null ? new Vector<DocumentListener>(2)
					: (Vector) documentListeners.clone();
			if (!v.contains(l)) {
				v.addElement(l);
				documentListeners = v;
			}
		}
	}

	public void resourceLoaded(CreoleEvent e) {
	}

	public void resourceUnloaded(CreoleEvent e) {
	}

	public void datastoreOpened(CreoleEvent e) {
	}

	public void datastoreCreated(CreoleEvent e) {
	}

	public void resourceRenamed(Resource resource, String oldName,
			String newName) {
	}

	private void deleteAllDocs() {
		Set keys = documents.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Document doc = (Document) documents.get(key);
			Factory.deleteResource(doc);
		}
	}

	public void datastoreClosed(CreoleEvent e) {
		if (!e.getDatastore().equals(this.getDataStore()))
			return;
		// we also remove other documents
		deleteAllDocs();
		// close this lr, since it cannot stay open when the DS it comes from
		// is closed
		Factory.deleteResource(this);
	}

	public void setLRPersistenceId(Object lrID) {
		super.setLRPersistenceId(lrID);
		// make persistent documents listen to the creole register
		// for events about their DS
		Gate.getCreoleRegister().addCreoleListener(this);
	}

	public void resourceAdopted(DatastoreEvent evt) {
	}

	public void resourceDeleted(DatastoreEvent evt) {
		if (!evt.getSource().equals(this.getDataStore()))
			return;
		// if an open document is deleted from a DS, then
		// it must close itself immediately, as is no longer valid
		if (evt.getResourceID().equals(this.getLRPersistenceId())) {
			deleteAllDocs();
			Factory.deleteResource(this);
		}
	}

	public void resourceWritten(DatastoreEvent evt) {
	}

	public void setDataStore(DataStore dataStore)
			throws gate.persist.PersistenceException {
		super.setDataStore(dataStore);
		if (this.dataStore != null)
			this.dataStore.addDatastoreListener(this);
	}

	public Document getCurrentDocument() {
		return currentDocument;
	}

	public Document getDocument(String documentID) {
		Object obj = documents.get(documentID);
		if (obj == null) {
			return this;
		} else {
			return (Document) obj;
		}
	}

	public void setCurrentDocument(Document aDocument) {
		this.currentDocument = aDocument;
	}

	public void setCurrentDocument(String documentID) {
		Object obj = documents.get(documentID);
		if (obj == null) {
			currentDocument = null;
		} else {
			currentDocument = (Document) obj;
		}
	}

	public Map getDocuments() {
		return documents;
	}

	public List<String> getDocumentIDs() {
		return documentIDs;
	}

	public void setDocumentIDs(List<String> docIDs) {
		this.documentIDs = docIDs;
	}
}
