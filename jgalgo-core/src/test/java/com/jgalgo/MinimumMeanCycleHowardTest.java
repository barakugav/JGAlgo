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

package com.jgalgo;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumMeanCycleHowardTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x6968128e5b6c70dfL;
		MinimumMeanCycleTestUtils.testRandGraphs(new MinimumMeanCycleHoward(), seed);
	}

	@Test
	public void testRandGraphsSimilarWeights() {
		final long seed = 0xc84b8622042a4d23L;
		MinimumMeanCycleTestUtils.testRandGraphsSimilarWeights(new MinimumMeanCycleHoward(), seed);
	}

}
