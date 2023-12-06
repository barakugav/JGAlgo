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
package com.jgalgo.gen;

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Generate a uniform random tree.
 *
 * <p>
 * The generator uses the Prufer sequence method to generate a uniform random tree. The Prufer sequence is a sequence of
 * {@code n-2} integers sampled uniformly in the range {@code [0,n)}, where {@code n} is the number of vertices. The
 * sequence is than converted to a tree using the algorithm described in the paper "An Optimal Algorithm for Prufer
 * Codes" by Xiaodong Wang, Lei Wang and Yingjie Wu. The algorithm runs in linear time.
 *
 * <p>
 * The generator generate undirected graphs only. If zero vertices are set, an empty graph is generated.
 *
 * <p>
 * For deterministic behavior, set the seed of the generator using {@link #setSeed(long)}.
 *
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public class UniformTreeGenerator<V, E> implements GraphGenerator<V, E> {

	private final boolean intGraph;
	private List<V> vertices;
	private BiFunction<V, V, E> edgeBuilder;
	private Random rand = new Random();

	private UniformTreeGenerator(boolean intGraph) {
		this.intGraph = intGraph;
	}

	/**
	 * Creates a new uniform tree generator.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @return     a new uniform tree generator
	 */
	public static <V, E> UniformTreeGenerator<V, E> newInstance() {
		return new UniformTreeGenerator<>(false);
	}

	/**
	 * Creates a new uniform tree generator for {@link IntGraph}.
	 *
	 * @return a new uniform tree generator for {@link IntGraph}
	 */
	public static UniformTreeGenerator<Integer, Integer> newIntInstance() {
		return new UniformTreeGenerator<>(true);
	}

	/**
	 * Set the vertices of the generated graph(s).
	 *
	 * <p>
	 * If the generator is used to generate multiple graphs, the same vertex set will be used for all of them.
	 *
	 * @param vertices the vertices of the generated graph(s)
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(Collection<V> vertices) {
		if (intGraph) {
			this.vertices = (List<V>) new IntArrayList(IntAdapters.asIntCollection((Collection<Integer>) vertices));
		} else {
			this.vertices = new ObjectArrayList<>(vertices);
		}
	}

	/**
	 * Set the vertices set of the generated graph(s) from a supplier.
	 *
	 * <p>
	 * The supplier will be called exactly {@code verticesNum} times, and the same set of vertices created will be used
	 * for multiple graphs if {@link #generate()} is called multiple times.
	 *
	 * @param verticesNum    the number of vertices
	 * @param vertexSupplier the supplier of vertices
	 */
	@SuppressWarnings("unchecked")
	public void setVertices(int verticesNum, Supplier<V> vertexSupplier) {
		if (intGraph) {
			IntList vertices = new IntArrayList(verticesNum);
			IntSupplier vSupplier = IntAdapters.asIntSupplier((Supplier<Integer>) vertexSupplier);
			for (int i = 0; i < verticesNum; i++)
				vertices.add(vSupplier.getAsInt());
			this.vertices = (List<V>) vertices;
		} else {
			List<V> vertices = new ObjectArrayList<>(verticesNum);
			for (int i = 0; i < verticesNum; i++)
				vertices.add(vertexSupplier.get());
			this.vertices = vertices;
		}
	}

	/**
	 * Set the edge supplier of the generated graph(s).
	 *
	 * <p>
	 * The supplier will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
	 *
	 * @param edgeSupplier the edge supplier
	 */
	public void setEdges(Supplier<E> edgeSupplier) {
		Objects.requireNonNull(edgeSupplier);
		setEdges((u, v) -> edgeSupplier.get());
	}

	/**
	 * Set the edge builder function of the generated graph(s).
	 *
	 * <p>
	 * The function will be called for any edge created, for any graph generated. This behavior is different from
	 * {@link #setVertices(int, Supplier)}, where the supplier is used to generate a set of vertices which is reused for
	 * any generated graph.
	 *
	 * @param edgeBuilder the edge builder function
	 */
	public void setEdges(BiFunction<V, V, E> edgeBuilder) {
		this.edgeBuilder = Objects.requireNonNull(edgeBuilder);
	}

	/**
	 * Set the seed of the random number generator used to generate the graph(s).
	 *
	 * <p>
	 * By default, a random seed is used. For deterministic behavior, set the seed of the generator.
	 *
	 * @param seed the seed of the random number generator
	 */
	public void setSeed(long seed) {
		rand = new Random(seed);
	}

	@Override
	public GraphBuilder<V, E> generateIntoBuilder() {
		if (vertices == null)
			throw new IllegalStateException("Vertices not set");
		if (edgeBuilder == null)
			throw new IllegalStateException("Edge supplier not set");

		@SuppressWarnings("unchecked")
		GraphFactory<V, E> factory =
				intGraph ? ((GraphFactory<V, E>) IntGraphFactory.newUndirected()) : GraphFactory.newUndirected();
		GraphBuilder<V, E> g = factory.newBuilder();

		final int n = vertices.size();
		if (n == 0)
			return g;
		for (V v : vertices)
			g.addVertex(v);
		if (n == 1)
			return g;

		/* generate a random Prufer code of length n-2 */
		int[] pruferCode = new int[n - 2];
		for (int i = 0; i < n - 2; i++)
			pruferCode[i] = rand.nextInt(n);

		/* 'decode' the Prufer code to a tree of size n */
		int[] degree = new int[n];
		for (int c : pruferCode)
			degree[c]++;
		int index, uIdx;
		V u = vertices.get(uIdx = index = range(n).filter(i -> degree[i] == 0).findFirst().getAsInt());
		Bitmap hasParent = new Bitmap(n);
		for (int vIdx : pruferCode) {
			V v = vertices.get(vIdx);
			g.addEdge(u, v, edgeBuilder.apply(u, v));
			hasParent.set(uIdx);

			degree[vIdx]--;
			if (vIdx < index && degree[vIdx] == 0) {
				u = v;
				uIdx = vIdx;
			} else {
				u = vertices.get(uIdx = index = range(index + 1, n).filter(i -> degree[i] == 0).findFirst().getAsInt());
			}
		}

		/* After the above loop, there should be two vertices with no parent. Connect them */
		assert Bitmap.fromPredicate(n, v -> !hasParent.get(v)).cardinality() == 2;
		int root1Idx = hasParent.nextClearBit(0);
		int root2Idx = hasParent.nextClearBit(root1Idx + 1);
		V root1 = vertices.get(root1Idx);
		V root2 = vertices.get(root2Idx);
		g.addEdge(root1, root2, edgeBuilder.apply(root1, root2));

		return g;
	}

}
