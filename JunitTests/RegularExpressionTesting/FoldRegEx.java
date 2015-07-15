package RegularExpressionTesting;

import static org.junit.Assert.*;

import org.junit.Test;

import columncontents.Log;

public class FoldRegEx {

	@Test
	public void logHeaderRegExTest() {
		Log l = Log.getInstance();
		assertTrue(l.headerMatch("LOG") != null);
		assertTrue(l.headerMatch("log") != null);
		assertTrue(l.headerMatch(" LOG(D/V)") != null);
		assertTrue(l.headerMatch("log(D/V)") != null);
		
		assertTrue(l.headerMatch("OrthoLog") == null);
	}

}
