package com.ugav.algo.test;

import com.ugav.algo.TPMKomlos1985King1997Hagerup2009;

public class TPMKomlos1985King1997Hagerup2009Test extends TestUtils {

	@Test
	public static void testTPM() {
		TPMTestUtils.testTPM(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public static void verifyMSTPositive() {
		TPMTestUtils.verifyMSTPositive(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public static void verifyMSTNegative() {
		TPMTestUtils.verifyMSTNegative(TPMKomlos1985King1997Hagerup2009::new);
	}

}
