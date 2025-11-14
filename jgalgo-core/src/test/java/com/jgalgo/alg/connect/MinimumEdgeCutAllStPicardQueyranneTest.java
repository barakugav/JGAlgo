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
package com.jgalgo.alg.connect;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.Path;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.alg.flow.MaximumFlowTestUtils;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MinimumEdgeCutAllStPicardQueyranneTest extends TestBase {

	@Test
	public void directed() {
		final long seed = 0xc787bb5a521199f5L;
		testMinCuts(new MinimumEdgeCutAllStPicardQueyranne(), true, seed);
	}

	@Test
	public void undirected() {
		final long seed = 0x491c179eb3311751L;
		testMinCuts(new MinimumEdgeCutAllStPicardQueyranne(), false, seed);
	}

	private static void testMinCuts(MinimumEdgeCutAllSt algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 6).repeat(32);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.addPhase().withArgs(64, 128).repeat(16);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightsInt<Integer> w = g.addEdgesWeights("weight", int.class);
			for (Integer e : g.edges())
				w.set(e, rand.nextInt(16384));

			Pair<Integer, Integer> sourceSink = MaximumFlowTestUtils.chooseSourceSink(g, rand);
			testAllMinCuts(g, w, sourceSink.first(), sourceSink.second(), algo);
		});
	}

	private static <V, E> void testAllMinCuts(Graph<V, E> g, WeightFunction<E> w, V source, V sink,
			MinimumEdgeCutAllSt algo) {
		List<VertexBiPartition<V, E>> cuts = algo.allMinimumCuts(g, w, source, sink);

		if (cuts.isEmpty()) {
			assertNull(Path.findPath(g, source, sink));
			return;
		}
		double minCutWeight = WeightFunction.weightSum(w, cuts.get(0).crossEdges());
		for (VertexBiPartition<V, E> cut : cuts)
			assertEquals(minCutWeight, WeightFunction.weightSum(w, cut.crossEdges()), 1e-6);

		if (g.vertices().size() <= 16) {
			Set<Set<V>> actual = cuts.stream().map(VertexBiPartition::leftVertices).collect(Collectors.toSet());
			Set<Set<V>> expected = new HashSet<>(findAllMinimumStCuts(g, w, source, sink));
			assertEquals(expected, actual);
		}
	}

	private static <V, E> List<Set<V>> findAllMinimumStCuts(Graph<V, E> g, WeightFunction<E> w, V source, V sink) {
		List<V> vertices = new ArrayList<>(g.vertices());
		vertices.remove(source);
		vertices.remove(sink);

		ToDoubleFunction<Set<V>> cutWeight = cut -> {
			double weight = 0;
			for (V u : cut) {
				for (EdgeIter<V, E> eit = g.outEdges(u).iterator(); eit.hasNext();) {
					E e = eit.next();
					V v = eit.target();
					if (!cut.contains(v))
						weight += w.weight(e);
				}
			}
			return weight;
		};
		List<ObjectDoublePair<Set<V>>> cuts = SubSets.stream(vertices).map(vs -> {
			Set<V> cut = new ObjectOpenHashSet<>(vs);
			cut.add(source);
			return ObjectDoublePair.of(cut, cutWeight.applyAsDouble(cut));
		}).collect(toList());
		Set<V> naiveSet = Set.of(source);
		cuts.add(ObjectDoublePair.of(naiveSet, cutWeight.applyAsDouble(naiveSet)));

		cuts.sort((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()));
		double minCutWeight = cuts.get(0).rightDouble();
		final double eps = 1e-4;
		return cuts
				.stream()
				.filter(p -> p.secondDouble() <= minCutWeight + eps)
				.map(ObjectDoublePair::first)
				.collect(Collectors.toList());
	}

	@Test
	public void testBuilderDefaultImpl() {
		MinimumEdgeCutAllSt algo = MinimumEdgeCutAllSt.newInstance();
		assertEquals(MinimumEdgeCutAllStPicardQueyranne.class, algo.getClass());
	}

}
