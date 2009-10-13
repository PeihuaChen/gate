package com.ontotext.kim.test;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.query.QueryResultCounter;
import com.ontotext.kim.util.datastore.PrivateRepositoryFeed;

public class PrivateRepositoryFeedTest extends TestCase {

	public void testWithLLD() throws IOException, KIMQueryException {
		URL configReader = this.getClass().getClassLoader().getResource("config.ttl");
		String query = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("query.txt"));
		PrivateRepositoryFeed feed = new PrivateRepositoryFeed(configReader, query);
		QueryResultCounter counter = new QueryResultCounter();
		feed.feedTo(counter);
		assertEquals(100, counter.getCount());
	}
}
