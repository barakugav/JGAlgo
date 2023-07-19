/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.internal.data;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class RMQStaticLookupTableTest extends TestBase {

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0xc7d2ec9ae1d4efd0L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 1; n <= 256; n++)
			RMQStaticUtils.testRMQ(new RMQStaticLookupTable(), n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testRegular16384() {
		final long seed = 0xa9873a72958dd0b6L;
		RMQStaticUtils.testRMQ(new RMQStaticLookupTable(), 16384, 4096, seed);
	}
}
