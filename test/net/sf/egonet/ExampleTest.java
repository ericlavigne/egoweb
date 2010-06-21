package net.sf.egonet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExampleTest {

	private Integer x,y;
	
	@Before
	public void setUp() throws Exception {
		x = 1;
		y = 1;
	}

	@After
	public void tearDown() throws Exception {
		x = null;
		y = null;
	}

	@Test
	public void exampleTest() {
		assertTrue(x.equals(y));
	}
}
