package com.jgalgo;

import org.junit.jupiter.api.Test;

public class TreePathMaximaHagerupTest extends TestBase {

	@Test
	public void testTPM() {
		final long seed = 0x32cba050c3014810L;
		TreePathMaximaTestUtils.testTPM(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testTPMWithBitsLookupTable() {
		final long seed = 0xc45d80515d512726L;
		TreePathMaximaTestUtils.testTPM(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTPositive() {
		final long seed = 0x61820733d2eb1adaL;
		TreePathMaximaTestUtils.verifyMSTPositive(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTPositiveWithBitsLookupTable() {
		final long seed = 0x3c8c940744e2342dL;
		TreePathMaximaTestUtils.verifyMSTPositive(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTNegative() {
		final long seed = 0x3f6671898b7bc54cL;
		TreePathMaximaTestUtils.verifyMSTNegative(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTNegativeWithBitsLookupTable() {
		final long seed = 0x8b0cceccd638a612L;
		TreePathMaximaTestUtils.verifyMSTNegative(new TreePathMaximaHagerup(), seed);
	}

}
