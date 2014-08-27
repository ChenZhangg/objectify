/*
 */

package com.googlecode.objectify.test;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.test.entity.Trivial;
import com.googlecode.objectify.test.util.TestBase;
import org.testng.annotations.Test;
import java.util.List;
import java.util.logging.Logger;
import static com.googlecode.objectify.test.util.TestObjectifyService.fact;
import static com.googlecode.objectify.test.util.TestObjectifyService.ofy;

/**
 * Tests of basic query operations
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class QueryProjectionTests extends TestBase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(QueryProjectionTests.class.getName());

	/** */
	@Test
	public void simpleProjectionWorks() throws Exception {
		fact().register(Trivial.class);

		Trivial triv = new Trivial(123L, "foo", 12);
		ofy().save().entity(triv).now();
		ofy().clear();

		List<Trivial> projected = ofy().load().type(Trivial.class).project("someString").list();
		assert projected.size() == 1;

		Trivial pt = projected.get(0);
		assert pt.getId() == triv.getId();
		assert pt.getSomeString().equals(triv.getSomeString());
		assert pt.getSomeNumber() == 0;	// default value
	}

	/** */
	@Test
	public void projectionDoesNotContaminateSession() throws Exception {
		fact().register(Trivial.class);

		Trivial triv = new Trivial(123L, "foo", 12);
		Key<Trivial> trivKey = ofy().save().entity(triv).now();
		ofy().clear();

		List<Trivial> projected = ofy().load().type(Trivial.class).project("someString").list();
		assert projected.size() == 1;
		assert !ofy().isLoaded(trivKey);

		Trivial fetched = ofy().load().key(trivKey).now();
		assert fetched.getSomeNumber() == triv.getSomeNumber();
	}

	@Entity
	private static class HasIndexedNumber {
		public @Id Long id;
		@Index
		public int number;
	}

	/** */
	@Test
	public void projectionOfNumbersWorks() throws Exception {
		fact().register(HasIndexedNumber.class);

		HasIndexedNumber hin = new HasIndexedNumber();
		hin.number = 5;

		ofy().save().entity(hin).now();
		ofy().clear();

		List<HasIndexedNumber> projected = ofy().load().type(HasIndexedNumber.class).project("number").list();
		assert projected.size() == 1;
		assert projected.get(0).number == hin.number;
	}
}