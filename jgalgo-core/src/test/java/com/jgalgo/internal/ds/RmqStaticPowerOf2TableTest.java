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

package com.jgalgo.internal.ds;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class RmqStaticPowerOf2TableTest extends TestBase {

	@Test
	public void testRegularNRange64to256() {
		final long seed = 0x95ef040f1c1d0dcfL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		for (int n = 64; n <= 256; n++)
			RmqStaticUtils.testRMQ(new RmqStaticPowerOf2Table(), n, 1024, seedGen.nextSeed());
	}

	@Test
	public void testRegular65536() {
		final long seed = 0x4505769d28250811L;
		RmqStaticUtils.testRMQ65536(new RmqStaticPowerOf2Table(), seed);
	}

}
