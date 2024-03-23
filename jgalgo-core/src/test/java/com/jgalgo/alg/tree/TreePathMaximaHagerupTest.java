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

package com.jgalgo.alg.tree;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class TreePathMaximaHagerupTest extends TestBase {

	@Test
	public void testTPM() {
		final long seed = 0x32cba050c3014810L;
		TreePathMaximaTestUtils.testTPM(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testTPMWithBitsLookupTable() {
		final long seed = 0xc45d80515d512726L;
		TreePathMaximaTestUtils.testTPM(withBitsLookupTable(), seed);
	}

	@Test
	public void testVerifyMSTPositive() {
		final long seed = 0x61820733d2eb1adaL;
		TreePathMaximaTestUtils.verifyMSTPositive(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTPositiveWithBitsLookupTable() {
		final long seed = 0x3c8c940744e2342dL;
		TreePathMaximaTestUtils.verifyMSTPositive(withBitsLookupTable(), seed);
	}

	@Test
	public void testVerifyMSTNegative() {
		final long seed = 0x3f6671898b7bc54cL;
		TreePathMaximaTestUtils.verifyMSTNegative(new TreePathMaximaHagerup(), seed);
	}

	@Test
	public void testVerifyMSTNegativeWithBitsLookupTable() {
		final long seed = 0x8b0cceccd638a612L;
		TreePathMaximaTestUtils.verifyMSTNegative(withBitsLookupTable(), seed);
	}

	private static TreePathMaximaHagerup withBitsLookupTable() {
		TreePathMaximaHagerup tmp = new TreePathMaximaHagerup();
		tmp.setBitsLookupTablesEnable(true);
		return tmp;
	}

	@Test
	public void nonTree() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		TreePathMaxima.IQueries queries = TreePathMaxima.IQueries.newInstance();
		queries.addQuery(0, 1);

		TreePathMaxima algo = new TreePathMaximaHagerup();
		assertThrows(IllegalArgumentException.class, () -> algo.computeHeaviestEdgeInTreePaths(g, null, queries));
	}

}
