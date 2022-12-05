package com.ugav.algo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AppTest {

	@Test
	public void tests_main() {
		assertTrue(TestRunner.getInstance().runTests());
	}
}
