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
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class LowestCommonAncestorStaticRMQTest extends TestBase {

	static int[][] randLcaQueries(IntGraph g, int root, int queriesNum, long seed) {
		Random rand = new Random(seed);
		int[][] queries = new int[queriesNum][3];

		int n = g.vertices().size();
		Int2IntMap parent = new Int2IntOpenHashMap(n);
		Int2IntMap depth = new Int2IntOpenHashMap(n);

		for (BfsIter it = BfsIter.newInstance(g, root); it.hasNext();) {
			int v = it.nextInt();
			int e = it.lastEdge();
			if (e == -1) {
				parent.put(v, -1);
				depth.put(v, 0);
			} else {
				int p = g.edgeEndpoint(e, v);
				parent.put(v, p);
				depth.put(v, depth.get(p) + 1);
			}
		}

		int[] vs = g.vertices().toIntArray();
		for (int query = 0; query < queriesNum; query++) {
			int u = vs[rand.nextInt(vs.length)];
			int v = vs[rand.nextInt(vs.length)];

			int uDepth = depth.get(u), vDepth = depth.get(v);
			/* assume v is deeper */
			int up = u, vp = v;
			if (uDepth > vDepth) {
				up = v;
				vp = u;
				int temp = uDepth;
				uDepth = vDepth;
				vDepth = temp;
			}
			/* iterate up to equal level */
			for (; uDepth < vDepth; vDepth--)
				vp = parent.get(vp);
			while (up != vp) {
				up = parent.get(up);
				vp = parent.get(vp);
			}
			int lca = up;

			queries[query][0] = u;
			queries[query][1] = v;
			queries[query][2] = lca;
		}
		return queries;
	}

	private static void testLCA(IntGraph g, Supplier<? extends LowestCommonAncestorStatic> builder, int[][] queries) {
		LowestCommonAncestorStatic lca = builder.get();
		LowestCommonAncestorStatic.DataStructure lcaDS = lca.preProcessTree(g, g.vertices().iterator().nextInt());

		for (int[] query : queries) {
			int u = query[0];
			int v = query[1];
			int expected = query[2];
			int actual = lcaDS.findLowestCommonAncestor(u, v);
			assertEquals(expected, actual, "<- [" + u + "," + v + "]");
		}
	}

	@Test
	public void testRandTrees() {
		final long seed = 0x16f0491558fa62f8L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(16, 16).repeat(128);
		tester.addPhase().withArgs(64, 64).repeat(64);
		tester.addPhase().withArgs(512, 512).repeat(16);
		tester.addPhase().withArgs(4096, 4096).repeat(4);
		tester.addPhase().withArgs(16384, 16384).repeat(1);
		tester.run((n, m) -> {
			IntGraph g = GraphsTestUtils.randTree(n, seedGen.nextSeed());
			int root = g.vertices().iterator().nextInt();
			int[][] queries = randLcaQueries(g, root, m, seedGen.nextSeed());
			testLCA(g, LowestCommonAncestorStaticRMQ::new, queries);
		});
	}

}
