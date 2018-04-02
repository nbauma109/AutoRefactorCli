package org.autorefactor.cli;

import static org.junit.Assert.*;

import org.autorefactor.cli.dd.DDMin.Result;
import org.junit.Test;

public class JavaMinimizerTest {
	private static final AutoRefactor.TargetTest REPRODUCED = new AutoRefactor.TargetTest() {
		@Override
		public Result apply(String code) {
			return Result.Reproduced;
		}
	};

	@Test
	public void testRemoveLeadingWhiteSpace() {
		doTestRemoveLeadingWhiteSpace("abc\n", " \tabc\n");
		doTestRemoveLeadingWhiteSpace("abc\ndef\n", " \tabc\n\t\tdef\n");
	}

	private void doTestRemoveLeadingWhiteSpace(String expected, String in) {
		assertEquals(expected, AutoRefactor.removeLeadingWhitespace(in));
	}

	@Test
	public void testRemoveTrailingWhiteSpace() {
		doTestRemoveTrailingWhiteSpace("abc\n", "abc \n");
		doTestRemoveTrailingWhiteSpace("abc\ndef\n", "abc \ndef\t \t\n");
	}

	private void doTestRemoveTrailingWhiteSpace(String expected, String in) {
		assertEquals(expected, AutoRefactor.removeTrailingWhitespace(in));
	}
	
	@Test
	public void testReplacements() {
		assertEquals("{return 1;}", AutoRefactor.tryReplacements("{return a;}", null, REPRODUCED));
		assertEquals("{Object s =1;}", AutoRefactor.tryReplacements("{String s = \"asdf\";}", null, REPRODUCED));
		assertEquals("a(Object...q) {}", AutoRefactor.tryReplacements("a(Object...reference) {}", null, REPRODUCED));
	}

	@Test
	public void testSplitLines() {
		assertEquals("@hello()\nworld()", AutoRefactor.splitLines("@hello()world()", REPRODUCED));
	}
}
