package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.TPMKomlos1985King1997Hagerup2009;

public class TPMKomlos1985King1997Hagerup2009Test extends TestUtils {

	@Test
	public void testTPM() {
		TPMTestUtils.testTPM(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public void testVerifyMSTPositive() {
		TPMTestUtils.verifyMSTPositive(TPMKomlos1985King1997Hagerup2009::new);
	}

	@Test
	public void testVerifyMSTNegative() {
		TPMTestUtils.verifyMSTNegative(TPMKomlos1985King1997Hagerup2009::new);
	}

}
