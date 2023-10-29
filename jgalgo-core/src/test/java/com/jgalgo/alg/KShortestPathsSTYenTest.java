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
package com.jgalgo.alg;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class KShortestPathsSTYenTest extends TestBase {

	@Test
	public void randGraphUndirected() {
		final long seed = 0xaa039caa5dd2c1c3L;
		KShortestPathsSTTestUtils.randGraphs(new KShortestPathsSTYen(), false, seed);
	}

	@Test
	public void randGraphDirected() {
		final long seed = 0x4c41ddd4454cf5f8L;
		KShortestPathsSTTestUtils.randGraphs(new KShortestPathsSTYen(), true, seed);
	}

}
