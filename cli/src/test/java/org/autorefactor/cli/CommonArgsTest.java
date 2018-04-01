package org.autorefactor.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommonArgsTest {

	@Test
	public void testPreparePatterns() {
		assertEquals(".*", CommonArgs.prepareIncludePattern(".*"));
		assertEquals(".*", CommonArgs.prepareIncludePattern(""));
		assertEquals(".*A.*", CommonArgs.prepareIncludePattern("A"));
		assertEquals("^A.*", CommonArgs.prepareIncludePattern("^A"));
		assertEquals(".*A$", CommonArgs.prepareIncludePattern("A$"));
	}

}
