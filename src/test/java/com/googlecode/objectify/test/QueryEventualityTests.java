/*
 */

package com.googlecode.objectify.test;

import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.test.entity.Trivial;
import com.googlecode.objectify.test.util.TestBaseInconsistent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import static com.googlecode.objectify.test.util.TestObjectifyService.fact;
import static com.googlecode.objectify.test.util.TestObjectifyService.ofy;

/**
 * Tests of queries when they are eventual
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class QueryEventualityTests extends TestBaseInconsistent
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(QueryEventualityTests.class.getName());

	/** */
	Trivial triv1;
	Trivial triv2;
	List<Key<Trivial>> keys;

	/** */
	@BeforeMethod
	public void setUpExtra() {
		fact().register(Trivial.class);

		this.triv1 = new Trivial("foo1", 1);
		this.triv2 = new Trivial("foo2", 2);

		List<Trivial> trivs = new ArrayList<>();
		trivs.add(this.triv1);
		trivs.add(this.triv2);

		Map<Key<Trivial>, Trivial> result = ofy().save().entities(trivs).now();

		this.keys = new ArrayList<>(result.keySet());

		// This should apply the writes
		Query q = new Query("Trivial");
		PreparedQuery pq = ds().prepare(q);
		pq.asList(FetchOptions.Builder.withDefaults());

		// For some reason this doesn't.
		ofy().load().keys(keys).size();
	}

	/**
	 * Delete creates a negative cache result, so when the value comes back we should not insert a null
	 * but rather pretend the value does not exist.
	 */
	@Test
	public void deleteWorks() throws Exception {
		// Should be an unapplied write
		ofy().delete().entity(triv1).now();

		List<Trivial> found = ofy().load().type(Trivial.class).list();
		assert found.size() == 1;
		assert found.get(0).getId().equals(triv2.getId());
	}

	/**
	 * Delete creates a negative cache result, so when the value comes back we should not insert a null
	 * but rather pretend the value does not exist.
	 */
	@Test
	public void deleteAllWorks() throws Exception {
		ofy().delete().entities(triv1, triv2).now();

		List<Trivial> found = ofy().load().type(Trivial.class).list();
		assert found.isEmpty();
	}
}
