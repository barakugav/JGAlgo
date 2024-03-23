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
package com.jgalgo.alg.clique;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MaximalCliquesEnumeratorBronKerboschTest extends TestBase {

	private static MaximalCliquesEnumerator algo() {
		return new MaximalCliquesEnumeratorBronKerbosch();
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0xa95d5c493f644824L;
		MaximalCliquesEnumeratorTestUtils.testRandGraphs(algo(), seed);
	}

}
