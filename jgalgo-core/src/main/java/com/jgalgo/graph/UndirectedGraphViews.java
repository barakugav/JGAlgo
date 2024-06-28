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
import java.util.Collection;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

class UndirectedGraphViews {

	private UndirectedGraphViews() {}

	private static final class UndirectedView<V, E> extends GraphViews.GraphView<V, E> {

		UndirectedView(Graph<V, E> g) {
			super(g);
			assert g.isDirected();
		}

		@Override
		public EdgeSet<V, E> outEdges(V source) {
			return new EdgeSetOut(source);
		}

		@Override
		public EdgeSet<V, E> inEdges(V target) {
			return new EdgeSetIn(target);
		}

		@Override
		public E getEdge(V source, V target) {
			E e = graph().getEdge(source, target);
			return e != null ? e : graph().getEdge(target, source);
		}

		@Override
		public EdgeSet<V, E> getEdges(V source, V target) {
			if (source.equals(target))
				return graph().getEdges(source, target);
			return new EdgeSetSourceTarget<>(graph(), source, target);
		}

		@Override
		public void removeInEdgesOf(V vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(V vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public IndexGraph indexGraph() {
			return graph().indexGraph().undirectedView();
		}

		@Override
		public boolean isDirected() {
			return false;
		}

		@Override
		public boolean isAllowParallelEdges() {
			/*
			 * We do not enforce that (u,v) and (v,u) both exists in the original graph. Although this function return
			 * true, the original graph may no support parallel edges. See {@link Graph#undirectedView()}.
			 */
			return true;
		}

		private abstract static class EdgeSetBase<V, E> extends AbstractSet<E> implements EdgeSet<V, E> {

			final EdgeSet<V, E> out;
			final EdgeSet<V, E> in;

			EdgeSetBase(EdgeSet<V, E> out, EdgeSet<V, E> in) {
				this.out = out;
				this.in = in;
			}

			@Override
			public boolean contains(Object o) {
				return out.contains(o) || in.contains(o);
			}

			@Override
			public boolean remove(Object o) {
				return out.remove(o) || in.remove(o);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				changed |= out.removeAll(c);
				changed |= in.removeAll(c);
				return changed;
			}

			@Override
			public void clear() {
				out.clear();
				in.clear();
			}

			@Override
			public int size() {
				return (int) ObjectIterables.size(this);
			}

			@Override
			public boolean isEmpty() {
				return !iterator().hasNext();
			}
		}

		private abstract static class EdgeSetOutOrInBase<V, E> extends EdgeSetBase<V, E> {

			final V vertex;

			EdgeSetOutOrInBase(Graph<V, E> g, V vertex) {
				super(g.outEdges(vertex), g.inEdges(vertex));
				this.vertex = vertex;
			}
		}

		private abstract class EdgeIterOutOrInBase implements EdgeIter<V, E>, ObjectIterator<E> {

			private EdgeIter<V, E> outIt, inIt;
			final V vertex;
			V endpoint;

			EdgeIterOutOrInBase(V vertex) {
				outIt = graph().outEdges(vertex).iterator();
				inIt = graph().inEdges(vertex).iterator();
				this.vertex = vertex;
				advance();
			}

			private void advance() {
				if (outIt != null) {
					if (outIt.hasNext())
						return;
					outIt = null;
				}
				for (; inIt.hasNext(); inIt.next()) {
					E e = inIt.peekNext();
					/* we skip self edges in the in-edges iterator */
					if (!vertex.equals(graph().edgeSource(e)))
						return;
				}
				inIt = null;
			}

			@Override
			public boolean hasNext() {
				return inIt != null;
			}

			@Override
			public E next() {
				Assertions.hasNext(this);
				E e;
				if (outIt != null) {
					e = outIt.next();
					endpoint = outIt.target();
				} else {
					e = inIt.next();
					endpoint = inIt.source();
				}
				advance();
				return e;
			}

			@Override
			public E peekNext() {
				Assertions.hasNext(this);
				return outIt != null ? outIt.peekNext() : inIt.peekNext();
			}

			@Override
			public int skip(int n) {
				int skipped = 0;
				if (outIt != null) {
					skipped = JGAlgoUtils.objIterSkip(outIt, n);
					advance();
					n -= skipped;
					if (n == 0)
						return skipped;
				}
				return skipped + ObjectIterator.super.skip(n);
			}
		}

		private class EdgeSetOut extends EdgeSetOutOrInBase<V, E> {
			EdgeSetOut(V source) {
				super(graph(), source);
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIterOut(vertex);
			}
		}

		private class EdgeIterOut extends EdgeIterOutOrInBase {
			EdgeIterOut(V source) {
				super(source);
			}

			@Override
			public V source() {
				return vertex;
			}

			@Override
			public V target() {
				return endpoint;
			}
		}

		private class EdgeSetIn extends EdgeSetOutOrInBase<V, E> {
			EdgeSetIn(V target) {
				super(graph(), target);
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIterIn(vertex);
			}
		}

		private class EdgeIterIn extends EdgeIterOutOrInBase {
			EdgeIterIn(V target) {
				super(target);
			}

			@Override
			public V source() {
				return endpoint;
			}

			@Override
			public V target() {
				return vertex;
			}
		}

		private static class EdgeSetSourceTarget<V, E> extends EdgeSetBase<V, E> {

			private final V source, target;

			EdgeSetSourceTarget(Graph<V, E> g, V source, V target) {
				super(g.getEdges(source, target), g.getEdges(target, source));
				this.source = source;
				this.target = target;
			}

			@Override
			public int size() {
				return out.size() + in.size();
			}

			@Override
			public EdgeIter<V, E> iterator() {
				return new EdgeIterSourceTarget<>(source, target, out, in);
			}
		}

		private static class EdgeIterSourceTarget<V, E> implements EdgeIter<V, E>, ObjectIterator<E> {

			private final V source, target;
			private final EdgeIter<V, E> stIt;
			private final EdgeIter<V, E> tsIt;
			private EdgeIter<V, E> it;

			EdgeIterSourceTarget(V source, V target, EdgeSet<V, E> stSet, EdgeSet<V, E> tsSet) {
				this.source = source;
				this.target = target;
				stIt = stSet.iterator();
				tsIt = tsSet.iterator();
				it = stIt;
				advance();
			}

			private void advance() {
				if (it.hasNext())
					return;
				if (it == stIt && tsIt.hasNext()) {
					it = tsIt;
				} else {
					it = null;
				}
			}

			@Override
			public boolean hasNext() {
				return it != null;
			}

			@Override
			public E next() {
				Assertions.hasNext(this);
				E e = it.next();
				advance();
				return e;
			}

			@Override
			public E peekNext() {
				Assertions.hasNext(this);
				return it.peekNext();
			}

			@Override
			public V source() {
				return source;
			}

			@Override
			public V target() {
				return target;
			}

			@Override
			public int skip(int n) {
				if (it == null) {
					if (n < 0)
						throw new IllegalArgumentException("Argument must be nonnegative: " + n);
					return 0;
				}
				int skipped = JGAlgoUtils.objIterSkip(it, n);
				n -= skipped;
				if (n == 0) {
					advance();
					return skipped;
				}
				skipped += JGAlgoUtils.objIterSkip(tsIt, n);
				it = tsIt.hasNext() ? tsIt : null;
				return skipped;
			}
		}
	}

	private abstract static class UndirectedViewIntBase extends GraphViews.IntGraphViewBase {

		UndirectedViewIntBase(IntGraph g) {
			super(g);
			assert g.isDirected();
		}

		@Override
		public IEdgeSet outEdges(int source) {
			return new EdgeSetOut(source);
		}

		@Override
		public IEdgeSet inEdges(int target) {
			return new EdgeSetIn(target);
		}

		@Override
		public int getEdge(int source, int target) {
			int e = graph().getEdge(source, target);
			return e >= 0 ? e : graph().getEdge(target, source);
		}

		@Override
		public IEdgeSet getEdges(int source, int target) {
			if (source == target)
				return graph().getEdges(source, target);
			return new EdgeSetSourceTarget(graph(), source, target);
		}

		@Override
		public void removeInEdgesOf(int vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public void removeOutEdgesOf(int vertex) {
			graph().removeEdgesOf(vertex);
		}

		@Override
		public boolean isDirected() {
			return false;
		}

		@Override
		public boolean isAllowParallelEdges() {
			/*
			 * We do not enforce that (u,v) and (v,u) both exists in the original graph. Although this function return
			 * true, the original graph may no support parallel edges. See {@link Graph#undirectedView()}.
			 */
			return true;
		}

		private abstract static class EdgeSetBase extends AbstractIntSet implements IEdgeSet {

			final IEdgeSet out;
			final IEdgeSet in;

			EdgeSetBase(IEdgeSet out, IEdgeSet in) {
				this.out = out;
				this.in = in;
			}

			@Override
			public boolean contains(int o) {
				return out.contains(o) || in.contains(o);
			}

			@Override
			public boolean remove(int o) {
				return out.remove(o) || in.remove(o);
			}

			@Override
			public boolean removeAll(IntCollection c) {
				boolean changed = false;
				changed |= out.removeAll(c);
				changed |= in.removeAll(c);
				return changed;
			}

			@Override
			public void clear() {
				out.clear();
				in.clear();
			}
		}

		private abstract static class EdgeSetOutOrInBase extends EdgeSetBase {

			final int vertex;

			EdgeSetOutOrInBase(IntGraph g, int vertex) {
				super(g.outEdges(vertex), g.inEdges(vertex));
				this.vertex = vertex;
			}

			@Override
			public int size() {
				return (int) ObjectIterables.size(this);
			}
		}

		private abstract class EdgeIterOutOrInBase implements IEdgeIter {

			private IEdgeIter outIt, inIt;
			final int vertex;
			int endpoint = -1;

			EdgeIterOutOrInBase(int vertex) {
				outIt = graph().outEdges(vertex).iterator();
				inIt = graph().inEdges(vertex).iterator();
				this.vertex = vertex;
				advance();
			}

			private void advance() {
				if (outIt != null) {
					if (outIt.hasNext())
						return;
					outIt = null;
				}
				for (; inIt.hasNext(); inIt.nextInt()) {
					int e = inIt.peekNextInt();
					/* we skip self edges in the in-edges iterator */
					if (vertex != graph().edgeSource(e))
						return;
				}
				inIt = null;
			}

			@Override
			public boolean hasNext() {
				return inIt != null;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int e;
				if (outIt != null) {
					e = outIt.nextInt();
					endpoint = outIt.targetInt();
				} else {
					e = inIt.nextInt();
					endpoint = inIt.sourceInt();
				}
				advance();
				return e;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return outIt != null ? outIt.peekNextInt() : inIt.peekNextInt();
			}

			@Override
			public int skip(int n) {
				endpoint = -1;
				int skipped = 0;
				if (outIt != null) {
					skipped = JGAlgoUtils.objIterSkip(outIt, n);
					advance();
					n -= skipped;
					if (n == 0)
						return skipped;
				}
				return skipped + IEdgeIter.super.skip(n);
			}
		}

		private class EdgeSetOut extends EdgeSetOutOrInBase {
			EdgeSetOut(int source) {
				super(graph(), source);
			}

			@Override
			public IEdgeIter iterator() {
				return new EdgeIterOut(vertex);
			}
		}

		private class EdgeIterOut extends EdgeIterOutOrInBase {
			EdgeIterOut(int source) {
				super(source);
			}

			@Override
			public int sourceInt() {
				return vertex;
			}

			@Override
			public int targetInt() {
				return endpoint;
			}
		}

		private class EdgeSetIn extends EdgeSetOutOrInBase {
			EdgeSetIn(int target) {
				super(graph(), target);
			}

			@Override
			public IEdgeIter iterator() {
				return new EdgeIterIn(vertex);
			}
		}

		private class EdgeIterIn extends EdgeIterOutOrInBase {
			EdgeIterIn(int target) {
				super(target);
			}

			@Override
			public int sourceInt() {
				return endpoint;
			}

			@Override
			public int targetInt() {
				return vertex;
			}
		}

		private static class EdgeSetSourceTarget extends EdgeSetBase {

			private final int source, target;

			EdgeSetSourceTarget(IntGraph g, int source, int target) {
				super(g.getEdges(source, target), g.getEdges(target, source));
				this.source = source;
				this.target = target;
			}

			@Override
			public int size() {
				return out.size() + in.size();
			}

			@Override
			public IEdgeIter iterator() {
				return new EdgeIterSourceTarget(source, target, out, in);
			}
		}

		private static class EdgeIterSourceTarget implements IEdgeIter {

			private final int source, target;
			private final IEdgeIter stIt;
			private final IEdgeIter tsIt;
			private IEdgeIter it;

			EdgeIterSourceTarget(int source, int target, IEdgeSet stSet, IEdgeSet tsSet) {
				this.source = source;
				this.target = target;
				stIt = stSet.iterator();
				tsIt = tsSet.iterator();
				it = stIt;
				advance();
			}

			private void advance() {
				if (it.hasNext())
					return;
				if (it == stIt && tsIt.hasNext()) {
					it = tsIt;
				} else {
					it = null;
				}
			}

			@Override
			public boolean hasNext() {
				return it != null;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				int e = it.nextInt();
				advance();
				return e;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return it.peekNextInt();
			}

			@Override
			public int sourceInt() {
				return source;
			}

			@Override
			public int targetInt() {
				return target;
			}

			@Override
			public int skip(int n) {
				if (it == null) {
					if (n < 0)
						throw new IllegalArgumentException("Argument must be nonnegative: " + n);
					return 0;
				}
				int skipped = it.skip(n);
				n -= skipped;
				if (n == 0) {
					advance();
					return skipped;
				}
				skipped += tsIt.skip(n);
				it = tsIt.hasNext() ? tsIt : null;
				return skipped;
			}
		}
	}

	private static final class UndirectedViewInt extends UndirectedViewIntBase {

		UndirectedViewInt(IntGraph g) {
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
			graph().addEdge(source, target, edge);
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
			return graph().indexGraph().undirectedView();
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

	private static final class UndirectedViewIndex extends UndirectedViewIntBase implements IndexGraph {

		UndirectedViewIndex(IndexGraph g) {
			super(g);
		}

		@Override
		public IndexGraph graph() {
			return (IndexGraph) super.graph();
		}

		@Override
		public IntSet addEdgesReassignIds(IEdgeSet edges) {
			return graph().addEdgesReassignIds(edges);
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
	static <V, E> Graph<V, E> of(Graph<V, E> g) {
		if (!g.isDirected()) {
			return g;
		} else if (g instanceof IndexGraph) {
			return (Graph<V, E>) new UndirectedViewIndex((IndexGraph) g);
		} else if (g instanceof IntGraph) {
			return (Graph<V, E>) new UndirectedViewInt((IntGraph) g);
		} else {
			return new UndirectedView<>(g);
		}
	}

}
