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

package com.jgalgo.alg.span;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class MinimumSpanningTreeBoruvkaTest extends TestBase {

	@Test
	public void testRandGraph() {
		final long seed = 0x9bb8032ff5628f22L;
		MinimumSpanningTreeTestUtils.testRandGraph(new MinimumSpanningTreeBoruvka(), seed);
	}

	@Test
	public void directedNotSupported() {
		MinimumSpanningTreeTestUtils.directedNotSupported(new MinimumSpanningTreeBoruvka());
	}

}
