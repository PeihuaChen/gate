package com.ontotext.kim.model;

import java.io.IOException;

import com.ontotext.kim.client.model.PROTONUConstants;
import com.ontotext.kim.semanticrepository.SimpleTableListener;
import com.ontotext.kim.util.KimLogs;

public abstract class EntitiesQueryListener extends SimpleTableListener {

	private int loaded;
	private long startTime = 0;

	public EntitiesQueryListener() {
		super(4);
	}

	@Override
	public void startTableQueryResult() throws IOException {
		super.startTableQueryResult();
		this.loaded = 0;
		this.startTime = System.currentTimeMillis();
	}
	
	public void endTuple() throws IOException {
		try {
			String aliasLabel = row[0];
			String instUri    = row[1];
			String classUri   = row[2];
			String ignore     = row[3];
			//KimLogs.logNERC_GAZETTEER.info(aliasLabel + "\t" + instUri + "\t" + classUri + "\t" + ignore);        

			//ad-hoc check for noise
			// filters numbers [0..99]
			if (aliasLabel.length() <= 2) {
				return;
			}

			if (ignore == null && !classUri.equals(PROTONUConstants.CLASS_JOB_POSITION)) { // hack!!
				addEntity(instUri, classUri, aliasLabel);
				++loaded;
				
				// We log at the first, because sometimes the query runs slow and sometimes the query freezes.
				// If we know that we have received at least one result, then the query has not frozen.
				if (loaded == 1 || loaded % 10000 == 0) {
					long currentTime = System.currentTimeMillis();
					KimLogs.logSEMANTIC_REPOSITORY.info(
							String.format("Loaded %s aliases in %s seconds." , loaded, (currentTime - startTime)/1000) );
				}
			}
		}
		catch (Exception x) {
			KimLogs.logNERC_GAZETTEER.error("There has been an exception in endTuple()\n" +
					"dumping stack trace but continuing with the next tuples \n" +
					"Counters are cleared at finally{...}", x);
		}

	}

	@Override
	public void endTableQueryResult() throws IOException {		
		super.endTableQueryResult();
		long currentTime = System.currentTimeMillis();
		KimLogs.logSEMANTIC_REPOSITORY.info(
				String.format("Loaded %s aliases in %s seconds." , loaded, (currentTime - startTime)/1000) );
	}
	
	protected abstract void addEntity(String instUri, String classUri,
			String aliasLabel);

}
