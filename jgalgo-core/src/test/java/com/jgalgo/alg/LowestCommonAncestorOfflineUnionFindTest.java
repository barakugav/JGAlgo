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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.LowestCommonAncestorStaticRMQTest.Query;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestUtils.PhasedTester;
import com.jgalgo.internal.util.TestUtils.SeedGenerator;

public class LowestCommonAncestorOfflineUnionFindTest {

	private static <V, E> void testLCA(Graph<V, E> g, V root, Supplier<? extends LowestCommonAncestorOffline> builder,
			Query<V>[] queries) {
		LowestCommonAncestorOffline lca = builder.get();
		LowestCommonAncestorOffline.Queries<V, E> qs = LowestCommonAncestorOffline.Queries.newInstance();
		for (int q = 0; q < queries.length; q++)
			qs.addQuery(queries[q].u, queries[q].v);
		LowestCommonAncestorOffline.Result<V, E> lcaDS = lca.findLCAs(g, root, qs);

		for (int q = 0; q < queries.length; q++) {
			V expected = queries[q].lca;
			V actual = lcaDS.getLca(q);
			assertEquals(expected, actual, "<- [" + queries[q].u + "," + queries[q].v + "]");
		}
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xd2ed7577f43a2461L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.addPhase().withArgs(512, 512).repeat(16);
		tester.addPhase().withArgs(4096, 4096).repeat(4);
		tester.addPhase().withArgs(16384, 16384).repeat(1);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			Integer root = g.vertices().iterator().next();
			Query<Integer>[] queries = LowestCommonAncestorStaticRMQTest.randLcaQueries(g, root, m, seedGen.nextSeed());
			testLCA(g, root, LowestCommonAncestorOfflineUnionFind::new, queries);
		});
	}

}