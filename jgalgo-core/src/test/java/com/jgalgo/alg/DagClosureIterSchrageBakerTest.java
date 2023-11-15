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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DagClosureIterSchrageBakerTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0xe085abe7fb5fb576L;
		testMinCuts(seed);
	}

	private static void testMinCuts(long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(3, 2).repeat(32);
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 18).repeat(8);
		tester.addPhase().withArgs(16, 32).repeat(8);
		tester.addPhase().withArgs(32, 46).repeat(4);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true)
					.parallelEdges(true).selfEdges(false).cycles(false).connected(false).build();

			testClosures(g.indexGraph());
		});
	}

	private static void testClosures(IndexGraph g) {
		DagClosureIterSchrageBaker algo = new DagClosureIterSchrageBaker();
		List<Bitmap> closures = new ObjectArrayList<>(algo.enumerateAllClosures(g));

		for (Bitmap closure : closures)
			for (int w : IPath.reachableVertices(g, closure.iterator()))
				assertTrue(closure.get(w));

		if (g.vertices().size() <= 16) {
			Set<IntSet> actual =
					closures.stream().map(bitmap -> new IntOpenHashSet(bitmap.iterator())).collect(Collectors.toSet());
			Set<IntSet> expected = new HashSet<>(findAllClosures(g));
			assertEquals(expected, actual);
		}
	}

	private static List<IntSet> findAllClosures(IndexGraph g) {
		final int n = g.vertices().size();
		IntList vertices = new IntArrayList(g.vertices());

		List<IntSet> closures = new ArrayList<>();
		IntSet closure = new IntOpenHashSet(n);
		subsetLoop: for (int bitmap = 1; bitmap < 1 << n; bitmap++) {
			closure.clear();
			for (int i = 0; i < n; i++)
				if ((bitmap & (1 << i)) != 0)
					closure.add(vertices.getInt(i));
			for (int w : IPath.reachableVertices(g, closure.iterator()))
				if (!closure.contains(w))
					continue subsetLoop;
			closures.add(new IntOpenHashSet(closure));
		}
		return closures;
	}

}
