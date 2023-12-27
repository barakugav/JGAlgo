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

package com.jgalgo.bench.util;

import static com.jgalgo.internal.util.Range.range;
import java.util.Random;
import java.util.function.Function;
import com.jgalgo.gen.BarabasiAlbertGraphGenerator;
import com.jgalgo.gen.GnmBipartiteGraphGenerator;
import com.jgalgo.gen.GnmGraphGenerator;
import com.jgalgo.gen.GnpGraphGenerator;
import com.jgalgo.gen.RecursiveMatrixGraphGenerator;
import com.jgalgo.gen.UniformTreeGenerator;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IdBuilderInt;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;

public class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {}

	public static IntGraph randGraph(int n, int m, boolean directed, long seed) {
		return GraphsTestUtils.randGraph(n, m, directed, true, true, seed);
	}

	public static IntGraph randGraph(int n, int m, boolean directed, boolean selfEdges, boolean parallelEdges,
			long seed) {
		return (IntGraph) new GnmGraphGenerator<>(IntGraphFactory.undirected())
				.seed(seed)
				.directed(directed)
				.selfEdges(selfEdges)
				.parallelEdges(parallelEdges)
				.vertices(range(n))
				.edges(m, IdBuilderInt.defaultBuilder())
				.generateMutable();
	}

	public static IntGraph randBipartiteGraph(int n1, int n2, int m, boolean directed, long seed) {
		return randBipartiteGraph(n1, n2, m, directed, true, seed);
	}

	public static IntGraph randBipartiteGraph(int n1, int n2, int m, boolean directed, boolean parallelEdges,
			long seed) {
		GnmBipartiteGraphGenerator<Integer, Integer> gen =
				new GnmBipartiteGraphGenerator<>(IntGraphFactory.undirected());
		if (directed) {
			gen.directedAll();
		} else {
			gen.undirected();
		}
		return (IntGraph) gen
				.seed(seed)
				.vertices(range(n1), range(n1, n1 + n2))
				.edges(m, IdBuilderInt.defaultBuilder())
				.parallelEdges(parallelEdges)
				.generateMutable();
	}

	public static IntGraph randTree(int n, long seed) {
		return (IntGraph) new UniformTreeGenerator<>(IntGraphFactory.undirected())
				.seed(seed)
				.vertices(range(n))
				.edges(IdBuilderInt.defaultBuilder())
				.generateMutable();
	}

	public static Function<Boolean, IntGraph> defaultGraphImpl() {
		return directed -> IntGraphFactory.newInstance(directed.booleanValue()).newGraph();
	}

	public static IWeightsDouble assignRandWeights(IntGraph g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	public static IWeightsDouble assignRandWeights(IntGraph g, double minWeight, double maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		IWeightsDouble weight = g.addEdgesWeights("weight", double.class);
		for (int e : g.edges())
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		return weight;
	}

	public static IWeightsInt assignRandWeightsIntPos(IntGraph g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static IWeightsInt assignRandWeightsIntNeg(IntGraph g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	public static IWeightsInt assignRandWeightsInt(IntGraph g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		IWeightsInt weight = IWeights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges())
			weight.set(e, rand.nextInt(maxWeight - minWeight) + minWeight);
		return weight;
	}

	public static IntGraph randomGraphGnp(int n, boolean directed, long seed) {
		return new GnpGraphGenerator<>(IntGraphFactory.undirected())
				.seed(seed)
				.directed(directed)
				.vertices(range(n))
				.edges(IdBuilderInt.defaultBuilder())
				.generate()
				.indexGraph();
	}

	public static IntGraph randomGraphBarabasiAlbert(int n, boolean directed, long seed) {
		return new BarabasiAlbertGraphGenerator<>(IntGraphFactory.undirected())
				.seed(seed)
				.directed(directed)
				.vertices(range(n))
				.edges(IdBuilderInt.defaultBuilder())
				.generate()
				.indexGraph();
	}

	public static IntGraph randomGraphRecursiveMatrix(int n, int m, boolean directed, long seed) {
		return new RecursiveMatrixGraphGenerator<>(IntGraphFactory.undirected())
				.seed(seed)
				.directed(directed)
				.vertices(range(n))
				.edges(m, IdBuilderInt.defaultBuilder())
				.generate()
				.indexGraph();
	}

}
