package com.ontotext.kim.gate;

import gate.AnnotationSet;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class SesameEnrichment extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 3650L;
	
	private RepositoryConnection conn;
	private Repository rep;
	private String server;
	private String repositoryId;
	private String inputASName;
	private Set<String> annTypes = new HashSet<String>(Arrays.asList("Lookup"));
	private boolean deleteOnNoRelations = true;
	
	private String query = 
		"SELECT ?Person WHERE { " +
		"?Person <http://dbpedia.org/ontology/birthplace> ?BirthPlace . " +
		"?BirthPlace <http://www.geonames.org/ontology#parentFeature> <%s> . " +
		"?Person a <http://sw.opencyc.org/2008/06/10/concept/en/Entertainer> .} LIMIT 100";
	
	private static final Logger log = Logger.getLogger(SesameEnrichment.class);
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			rep = new HTTPRepository(server, repositoryId);
			conn = rep.getConnection();
		}
		catch (RepositoryException e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
	}

	@Override
	public void execute() throws ExecutionException {		
		AnnotationSet input = document.getAnnotations(inputASName);
		Set<gate.Annotation> deathRow = new HashSet<gate.Annotation>();
		
		for (gate.Annotation ann : input.get(annTypes)) {
			Object instFeature = ann.getFeatures().get("inst");
			if (!(instFeature instanceof String))
				continue;
			String instQuery = String.format(query, instFeature);
			try {
				TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, instQuery);
				StringBuilder outputData = new StringBuilder();
				TupleQueryResult tqr = tq.evaluate();	
				if (!tqr.hasNext() && deleteOnNoRelations) {
					deathRow.add(ann);
				}
				try {
					while (tqr.hasNext()) {
						BindingSet bs = tqr.next();
						for (Object val : bs)
							outputData.append(((Binding)val).getValue().stringValue()).append(",");						
					}
				}
				finally {
					tqr.close();
				}
				if (outputData.length() > 0) {
					ann.getFeatures().put("connections", outputData.toString());
				}
			} catch (MalformedQueryException e) {
				log.warn(String.format("Created invalid query [%s] for entity [%s] (in brackets). Parser reported: %s", instQuery, instFeature, e.getMessage()));
			} catch (Exception e) {
				log.warn(String.format("Error executing query [%s] for entity [%s] (in brackets)", instQuery, instFeature), e);
			}			
		}
		
		if (deleteOnNoRelations) {
			input.removeAll(deathRow);
		}
		
	}
	
	@Override
	public void reInit() throws ResourceInstantiationException {
		cleanup();
		init();
	}
	
	@Override
	public synchronized void cleanup() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
			if (rep != null) {
				rep.shutDown();
				rep = null;
			}
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getInputASName() {
		return inputASName;
	}

	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}
	public void setAnnotationTypes(List<String> annTypes) {
		this.annTypes = new HashSet<String>(annTypes);
	}

	public List<String> getAnnotationTypes() {
		return new ArrayList(annTypes);
	}

	public void setQuery(String query) {
		this.query = query.replace("\\", "");
	}

	public String getQuery() {
		return query;
	}

	public void setDeleteOnNoRelations(Boolean deleteOnNoRelations) {
		if (deleteOnNoRelations != null)
			this.deleteOnNoRelations = deleteOnNoRelations;
	}

	public Boolean getDeleteOnNoRelations() {
		return deleteOnNoRelations;
	}	
	
	public String getVersion() {
		return this.getClass().getPackage().getImplementationVersion();		
	}
	
	public void setVersion(String v) {
		
	}
}
