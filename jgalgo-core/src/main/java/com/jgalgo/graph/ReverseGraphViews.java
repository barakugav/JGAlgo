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
package com.jgalgo.graph;

import java.util.AbstractSet;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class ReverseGraphViews {

	private ReverseGraphViews() {}

	private static interface ReverseGraph<V, E> {
		Graph<V, E> graph();
	}

	private static class ObjReverseGraph<V, E> extends GraphViews.GraphView<V, E> implements ReverseGraph<V, E> {

		ObjReverseGraph(Graph<V, E> g) {
			super(g);
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return reverseEdgeSet(graph().inEdges(source));
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return reverseEdgeSet(graph().outEdges(target));
		}

		@Override
		public E getEdge(V source, V target) {
			return graph().getEdge(target, source);
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			return reverseEdgeSet(graph().getEdges(target, source));
		}

		@Override
		public void addEdge(V source, V target, E edge) {
			graph().addEdge(target, source, edge);
		}

		@Override
		public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
			graph().addEdges(reverseEdgeSet(edges));
		}

		@Override
		public void removeInEdgesOf(V vertex) {
			graph().removeOutEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(V vertex) {
			graph().removeInEdgesOf(vertex);
		}

		@Override
		public void moveEdge(E edge, V newSource, V newTarget) {
			graph().moveEdge(edge, newTarget, newSource);
		}

		@Override
		public V edgeSource(E edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public V edgeTarget(E edge) {
			return graph().edgeSource(edge);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().reverseView();
		}
	}

	private abstract static class ReverseIntGraphBase extends GraphViews.IntGraphViewBase
			implements ReverseGraph<Integer, Integer> {

		ReverseIntGraphBase(IntGraph g) {
			super(g);
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new ReversedIEdgeSet(graph().inEdges(source));
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new ReversedIEdgeSet(graph().outEdges(target));
		}

		@Override
		public int getEdge(int source, int target) {
			return graph().getEdge(target, source);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			return new ReversedIEdgeSet(graph().getEdges(target, source));
		}

		@Override
		public int addEdge(int source, int target) {
			return graph().addEdge(target, source);
		}

		@Override
		public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
			graph().addEdges(reverseEdgeSet(edges));
		}

		@Override
		public void removeInEdgesOf(int vertex) {
			graph().removeOutEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(int vertex) {
			graph().removeInEdgesOf(vertex);
		}

		@Override
		public void moveEdge(int edge, int newSource, int newTarget) {
			graph().moveEdge(edge, newTarget, newSource);
		}

		@Override
		public int edgeSource(int edge) {
			return graph().edgeTarget(edge);
		}

		@Override
		public int edgeTarget(int edge) {
			return graph().edgeSource(edge);
		}
	}

	private static class ReverseIntGraph extends ReverseIntGraphBase {

		ReverseIntGraph(IntGraph g) {
			super(g);
		}

		@Override
		public void addVertex(int vertex) {
			graph().addVertex(vertex);
		}

		@Override
		public void renameVertex(int vertex, int newId) {
			graph().renameVertex(vertex, newId);
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			graph().addEdge(target, source, edge);
		}

		@Override
		public void renameEdge(int edge, int newId) {
			graph().renameEdge(edge, newId);
		}

		@Override
		public IdBuilderInt vertexBuilder() {
			return graph().vertexBuilder();
		}

		@Override
		public IdBuilderInt edgeBuilder() {
			return graph().edgeBuilder();
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().reverseView();
		}

		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph().indexGraphVerticesMap();
		}

		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph().indexGraphEdgesMap();
		}
	}

	private static class ReverseIndexGraph extends ReverseIntGraphBase implements IndexGraph {

		ReverseIndexGraph(IndexGraph g) {
			super(g);
		}

		@Override
		public IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		public IntSet addEdgesReassignIds(IEdgeSet edges) {
			return graph().addEdgesReassignIds(new ReversedIEdgeSet(edges));
		}

		@Override
		public void addVertexRemoveListener(IndexRemoveListener listener) {
			graph().addVertexRemoveListener(listener);
		}

		@Override
		public void removeVertexRemoveListener(IndexRemoveListener listener) {
			graph().removeVertexRemoveListener(listener);
		}

		@Override
		public void addEdgeRemoveListener(IndexRemoveListener listener) {
			graph().addEdgeRemoveListener(listener);
		}

		@Override
		public void removeEdgeRemoveListener(IndexRemoveListener listener) {
			graph().removeEdgeRemoveListener(listener);
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphVerticesMap() {
			return graph().indexGraphVerticesMap();
		}

		@Deprecated
		@Override
		public IndexIntIdMap indexGraphEdgesMap() {
			return graph().indexGraphEdgesMap();
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> EdgeSet<V, E> reverseEdgeSet(EdgeSet<V, E> set) {
		if (set instanceof IEdgeSet) {
			return (EdgeSet<V, E>) new ReversedIEdgeSet((IEdgeSet) set);
		} else {
			return new ReversedEdgeSet<>(set);
		}
	}

	private static class ReversedEdgeSet<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

		private final EdgeSet<V, E> set;

		ReversedEdgeSet(EdgeSet<V, E> set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(Object edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public boolean remove(Object edge) {
			return set.remove(edge);
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new ReversedEdgeIter<>(set.iterator());
		}
	}

	private static class ReversedIEdgeSet extends AbstractIntSet implements IEdgeSet {

		private final IEdgeSet set;

		ReversedIEdgeSet(IEdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@Override
		public boolean contains(int edge) {
			return set.contains(edge);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public boolean remove(int edge) {
			return set.remove(edge);
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public IEdgeIter iterator() {
			return new ReversedIEdgeIter(set.iterator());
		}
	}

	private static class ReversedEdgeIter<V, E> extends GraphViews.EdgeIterView<V, E> {
		ReversedEdgeIter(EdgeIter<V, E> it) {
			super(it);
		}

		@Override
		public V source() {
			return it.target();
		}

		@Override
		public V target() {
			return it.source();
		}
	}

	private static class ReversedIEdgeIter extends GraphViews.IEdgeIterView {
		ReversedIEdgeIter(IEdgeIter it) {
			super(it);
		}

		@Override
		public int sourceInt() {
			return it.targetInt();
		}

		@Override
		public int targetInt() {
			return it.sourceInt();
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> Graph<V, E> of(Graph<V, E> g) {
		if (g instanceof ReverseGraph)
			return ((ReverseGraph<V, E>) g).graph();
		if (g instanceof IndexGraph)
			return (Graph<V, E>) new ReverseIndexGraph((IndexGraph) g);
		if (g instanceof IntGraph)
			return (Graph<V, E>) new ReverseIntGraph((IntGraph) g);
		return new ObjReverseGraph<>(g);
	}

}
