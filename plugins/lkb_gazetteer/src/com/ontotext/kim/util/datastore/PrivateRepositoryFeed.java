package com.ontotext.kim.util.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;

import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.client.semanticrepository.QueryResultListener;
import com.ontotext.kim.semanticrepository.TimedListener;
import com.ontotext.kim.semanticrepository.UnmanagedRepositoryFactory;

/**
 * @author mnozchev
 *
 */
public class PrivateRepositoryFeed implements QueryResultListener.Feed {

	private static final String SETTINGS_HASH_PROPERTY = "settingsHash";
	private static final String SNAPSHOT_PROPERTIES_FILENAME = "snapshot.properties";
	private final URL configFile;
	private final String query;
	private final int settingsHash;
	private final Logger log = Logger.getLogger(PrivateRepositoryFeed.class);
	private final File dictionaryPath;
	
	public PrivateRepositoryFeed(URL url, String query, int settingsHash) {
		this.configFile = url;
		this.query = query;
		this.settingsHash = settingsHash;
		dictionaryPath = new File(configFile.getFile()).getParentFile().getAbsoluteFile();
		
		if (!verifyHash(dictionaryPath, settingsHash)) {
			boolean deleteSuccesful = new File(dictionaryPath, "kim.trusted.entities.cache").delete();
			if (deleteSuccesful) {
				log.info("Cache is going to be refreshed due to a configuration change.");
			}
			else {
				log.warn("Cache needed to be refreshed due to a configuration change, but the system denied deleting it.");
			}
		}		
	}

	public void feedTo(QueryResultListener listener) throws KIMQueryException {
		UnmanagedRepositoryFactory factory = new UnmanagedRepositoryFactory();
		TimedListener timedListener = new TimedListener(true, listener, -1);
		Reader configReader = null;
		try {
			configReader = getConfigReader();
			Repository rep = factory.createRepository(configReader);
			rep.initialize();
			log.info("Initialized Sesame repository: " + (rep instanceof SailRepository ? ((SailRepository)rep).getSail().toString() : rep.toString()));
			try {				
				RepositoryConnection conn = rep.getConnection();				
				QueryResultListener.Feed dataFeed = new RepositoryFeed(conn, null, query);
				dataFeed.feedTo(timedListener);
				saveSettings(timedListener.getTimeTakenMS(), timedListener.getTuplesCnt());
			}
			finally {
				rep.shutDown();
			}
		}
		catch (Exception e) {
			throw new KIMQueryException(e);
		}
		finally {
			IOUtils.closeQuietly(configReader);
		}
		
	}

	private Reader getConfigReader() throws IOException {
		String configTemplate = FileUtils.readFileToString(new File(configFile.getFile()));
		configTemplate = configTemplate.replace("%relpath%", dictionaryPath.getAbsolutePath().replace('\\', '/'));
		return new StringReader(configTemplate);
	}

	private void saveSettings(Long timeTakenMS, int labelsCount) {
		Properties props = new Properties();
		props.put("snapshotDate", new SimpleDateFormat().format(new Date()));
		props.put("pluginVersion", getPackageVersion());
		props.put("labelsCount", String.valueOf(labelsCount));
		props.put("snapshotTimeTakenInSeconds", String.valueOf(timeTakenMS == null ? "" : timeTakenMS / 1000));
		props.put(SETTINGS_HASH_PROPERTY, String.valueOf(settingsHash));
		
		OutputStream settingsWriter = null;
		try {
			File settingsFile = new File(dictionaryPath, SNAPSHOT_PROPERTIES_FILENAME);
			settingsWriter = new FileOutputStream(settingsFile);
			props.store(settingsWriter, "Metadata about the last taken snapshot");
		}
		catch (IOException e) {
			log.warn("Could not save snapshot metadata: " + e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(settingsWriter);		
		}
	}

	private String getPackageVersion() {
		if (this.getClass().getPackage() == null)
			return "n/a";
		
		String res = this.getClass().getPackage().getImplementationVersion();
		return res == null ? "n/a" : res;
	}

	private boolean verifyHash(File dictionaryPath, int settingsHash) {
		Properties props = new Properties();
		File propsFile = new File(dictionaryPath, PrivateRepositoryFeed.SNAPSHOT_PROPERTIES_FILENAME);
		propsFile = propsFile.getAbsoluteFile();
		if (!propsFile.isFile()) {
			log.debug("Could not find " + propsFile);
			return true;
		}
		InputStream inStream = null;
		try {			
			inStream = new FileInputStream(propsFile);
			props.load(inStream);
			String oldHash = (String) props.get(PrivateRepositoryFeed.SETTINGS_HASH_PROPERTY);
			return settingsHash == NumberUtils.toInt(oldHash, settingsHash);
		} 
		catch (IOException e) {
			log.info("Could not read " + propsFile, e);
			// We fail the hash check only if we successfully read the old hash and found it 
			// to be different.
			return true;
		}
		finally {
			IOUtils.closeQuietly(inStream);
		}
	}	
}
