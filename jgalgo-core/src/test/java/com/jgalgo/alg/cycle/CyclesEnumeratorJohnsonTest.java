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

package com.jgalgo.alg.cycle;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CyclesEnumeratorJohnsonTest extends TestBase {

	@Test
	public void testSimpleGraph() {
		CyclesEnumeratorTestUtils.testSimpleGraph(new CyclesEnumeratorJohnson());
	}

	@Test
	public void testRandGraphs() {
		final long seed = 0x51f9f9bde92eef18L;
		CyclesEnumeratorTestUtils.testRandGraphs(new CyclesEnumeratorJohnson(), seed);
	}

	@Test
	public void noParallelEdges() {
		IntGraph g = IntGraphFactory.directed().allowParallelEdges().newGraph();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		CyclesEnumerator algo = new CyclesEnumeratorJohnson();
		assertNotNull(algo.cyclesIter(g));

		g.addEdge(2, 0);
		assertThrows(IllegalArgumentException.class, () -> new ObjectArrayList<>(algo.cyclesIter(g)));
	}

}
