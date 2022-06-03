package com.ugav.algo.test;

import com.ugav.algo.TPMKomlos1985King1997Hagerup2009;

public class TPMKomlos1985King1997Hagerup2009Test extends TestUtils {

	@Test
	public static boolean testTPM() {
		return TPMTestUtils.testTPM(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public static boolean verifyMSTPositive() {
		return TPMTestUtils.verifyMSTPositive(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public static boolean verifyMSTNegative() {
		return TPMTestUtils.verifyMSTNegative(TPMKomlos1985King1997Hagerup2009::new);
	}

}
