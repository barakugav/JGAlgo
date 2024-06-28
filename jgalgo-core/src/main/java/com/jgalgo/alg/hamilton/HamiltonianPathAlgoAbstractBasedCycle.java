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
package com.jgalgo.alg.hamilton;

import static com.jgalgo.internal.util.Range.range;
import java.util.Collections;
import java.util.Iterator;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Abstract class for computing Hamiltonian cycles/paths in graphs, based on Hamiltonian cycle algo.
 *
 * <p>
 * The {@link HamiltonianPathAlgo} interface expose a large number of methods of different variants of the Hamiltonian
 * problem. This abstract class implements some of these methods by reducing to a single Hamiltonian cycle problem,
 * {@link #hamiltonianCyclesIter(IndexGraph)}, which is left to the subclasses to implement.
 *
 * @author Barak Ugav
 */
public abstract class HamiltonianPathAlgoAbstractBasedCycle extends HamiltonianPathAlgoAbstract {

	/**
	 * Default constructor.
	 */
	public HamiltonianPathAlgoAbstractBasedCycle() {}

	@Override
	protected Iterator<IPath> hamiltonianPathsIter(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		if (n == 0)
			return Collections.emptyIterator();
		if (n == 1)
			return ObjectIterators.singleton(IPath.valueOf(g, 0, 0, Fastutil.list()));

		/*
		 * We find all Hamiltonian paths using a reduction to the problem of finding all Hamiltonian cycles. We create a
		 * graph, g0, with all the vertices and edges of the original graph, and we add another auxillary vertex x
		 * connected to all other vertices. If the graph is directed, x is connected to each vertex with two edges, one
		 * in each direction. If the graph is undirected, x is connected to each vertex with a single edge. We compute
		 * the Hamiltonian cycles in the new graph, and we map each such cycle to an Hamiltonian path by deleting x from
		 * the cycle.
		 */

		IndexGraphBuilder b = IndexGraphBuilder.newInstance(g.isDirected());
		b.ensureVertexCapacity(n + 1);
		b.ensureEdgeCapacity(m + n * (g.isDirected() ? 2 : 1));
		b.addVertices(g.vertices());
		b.addEdges(EdgeSet.allOf(g));
		final int x = b.addVertexInt();
		if (g.isDirected()) {
			for (int v : range(n)) {
				b.addEdge(x, v);
				b.addEdge(v, x);
			}
		} else {
			for (int v : range(n))
				b.addEdge(x, v);
		}
		IndexGraph g0 = b.build();

		Iterator<IPath> cyclesIter = hamiltonianCyclesIter(g0);
		return IterTools.map(cyclesIter, cycle -> {
			IntList path = new IntArrayList(n - 1);
			int xIdx = cycle.vertices().indexOf(x);
			assert xIdx >= 0;
			IntList cEdges = cycle.edges();
			if (xIdx < cEdges.size() - 1)
				path.addAll(cEdges.subList(xIdx + 1, cEdges.size() - (xIdx == 0 ? 1 : 0)));
			if (xIdx > 1)
				path.addAll(cEdges.subList(0, xIdx - 1));

			int source = g0.edgeEndpoint(/* xToSourceEdge */ cEdges.getInt(xIdx), x);
			int target = g0.edgeEndpoint(/* targetToXEdge */ cEdges.getInt(xIdx > 0 ? xIdx - 1 : cEdges.size() - 1), x);
			return IPath.valueOf(g, source, target, path);
		});
	}

	@Override
	protected Iterator<IPath> hamiltonianPathsIter(IndexGraph g, int source, int target) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		Assertions.checkVertex(source, n);
		Assertions.checkVertex(target, n);
		if (n == 1)
			return ObjectIterators.singleton(IPath.valueOf(g, 0, 0, Fastutil.list()));

		/* if source and target are the same vertex, we actually want an Hamiltonian cycle */
		if (source == target) {
			Iterator<IPath> cyclesIter = hamiltonianCyclesIter(g);
			return IterTools.map(cyclesIter, cycle -> {
				IntList path = new IntArrayList(n);
				int srcIdx = cycle.vertices().indexOf(source);
				assert srcIdx >= 0;
				path.addAll(cycle.edges().subList(srcIdx, cycle.edges().size()));
				path.addAll(cycle.edges().subList(0, srcIdx));
				return IPath.valueOf(g, source, target, path);
			});
		}

		/*
		 * We find all Hamiltonian paths between the given source and target vertices using a reduction to the problem
		 * of finding all Hamiltonian cycles. We create a graph, g0, with all the vertices and edges of the original
		 * graph, and we add another auxillary vertex x, and two additional edges (x, source) (target, x). We compute
		 * the Hamiltonian cycles in the new graph, and we map each such cycle to an Hamiltonian path between the source
		 * and target by deleting the edges (x, source) (target, x), which are require in any Hamiltonian cycle in g0,
		 * from the computed cycle.
		 */

		IndexGraphBuilder b = IndexGraphBuilder.newInstance(g.isDirected());
		b.ensureVertexCapacity(n + 1);
		b.ensureEdgeCapacity(m + 2);
		b.addVertices(g.vertices());
		b.addEdges(EdgeSet.allOf(g));
		final int x = b.addVertexInt();
		b.addEdge(x, source);
		b.addEdge(target, x);
		IndexGraph g0 = b.build();

		Iterator<IPath> cyclesIter = hamiltonianCyclesIter(g0);
		return IterTools.map(cyclesIter, cycle -> {
			IntArrayList path = new IntArrayList(n - 1);
			int xIdx = cycle.vertices().indexOf(x);
			assert xIdx >= 0;

			IntList cEdges = cycle.edges();
			if (xIdx < cEdges.size() - 1)
				path.addAll(cEdges.subList(xIdx + 1, cEdges.size() - (xIdx == 0 ? 1 : 0)));
			if (xIdx > 1)
				path.addAll(cEdges.subList(0, xIdx - 1));

			if (g.isDirected()) {
				assert source == g0.edgeTarget(/* xToSourceEdge */ cEdges.getInt(xIdx));
				assert target == g0
						.edgeSource(/* targetToXEdge */ cEdges.getInt(xIdx > 0 ? xIdx - 1 : cEdges.size() - 1));
			} else {
				int edgeAfterX = cEdges.getInt(xIdx);
				int vertexAfterX = g0.edgeEndpoint(edgeAfterX, x);

				if (source != vertexAfterX) {
					assert target == vertexAfterX;
					IntArrays.reverse(path.elements(), 0, path.size());
				}
				assert (source == vertexAfterX ? target : source) == g0
						.edgeEndpoint(/* targetToXEdge */ cEdges.getInt(xIdx > 0 ? xIdx - 1 : cEdges.size() - 1), x);
			}

			return IPath.valueOf(g, source, target, path);
		});
	}

}
