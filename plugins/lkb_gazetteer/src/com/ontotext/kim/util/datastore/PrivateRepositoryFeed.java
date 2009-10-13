package com.ontotext.kim.util.datastore;

import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.client.semanticrepository.QueryResultListener;
import com.ontotext.kim.semanticrepository.UnmanagedRepositoryFactory;

/**
 * @author mnozchev
 *
 */
public class PrivateRepositoryFeed implements QueryResultListener.Feed {

	private final URL configFile;
	private final String query;
	
	public PrivateRepositoryFeed(URL url, String query) {
		this.configFile = url;
		this.query = query;
	}

	public void feedTo(QueryResultListener listener) throws KIMQueryException {
		UnmanagedRepositoryFactory factory = new UnmanagedRepositoryFactory();
		try {
			Repository rep = factory.createRepository(new FileReader(configFile.getFile()));
			rep.initialize();
			try {
				RepositoryConnection conn = rep.getConnection();
				QueryResultListener.Feed dataFeed = new RepositoryFeed(conn, null, query);
				dataFeed.feedTo(listener);
			}
			finally {
				rep.shutDown();
			}
		}
		catch (Exception e) {
			throw new KIMQueryException(e);
		}
		
	}

}
