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
package com.jgalgo.adapt.guava;

import static com.jgalgo.internal.util.Range.range;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import com.google.common.collect.Iterators;
import com.google.common.graph.EndpointPair;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

class GuavaAdapters {

	private GuavaAdapters() {}

	static <V> Set<V> successors(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int uIdx = viMap.idToIndex(node);
		IEdgeSet es = g.outEdges(uIdx);
		if (graph.isAllowParallelEdges()) {
			IntSet successors = new IntOpenHashSet();
			if (graph.isDirected()) {
				for (int e : es)
					successors.add(g.edgeTarget(e));
			} else {
				for (int e : es)
					successors.add(g.edgeEndpoint(e, uIdx));
			}
			return IndexIdMaps.indexToIdSet(successors, viMap);

		} else {
			return new AbstractSet<>() {
				@Override
				public int size() {
					return es.size();
				}

				@Override
				public boolean contains(Object o) {
					@SuppressWarnings("unchecked")
					int vIdx = viMap.idToIndexIfExist((V) o);
					return vIdx >= 0 && g.containsEdge(uIdx, vIdx);
				}

				@Override
				public Iterator<V> iterator() {
					return new Iterator<>() {
						IEdgeIter it = es.iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public V next() {
							it.nextInt();
							return viMap.indexToId(it.targetInt());
						}
					};
				}
			};
		}
	}

	static <V> Set<V> predecessors(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int vIdx = viMap.idToIndex(node);
		IEdgeSet es = g.inEdges(vIdx);
		if (graph.isAllowParallelEdges()) {
			IntSet successors = new IntOpenHashSet();
			if (graph.isDirected()) {
				for (int e : es)
					successors.add(g.edgeSource(e));
			} else {
				for (int e : es)
					successors.add(g.edgeEndpoint(e, vIdx));
			}
			return IndexIdMaps.indexToIdSet(successors, viMap);

		} else {
			return new AbstractSet<>() {
				@Override
				public int size() {
					return es.size();
				}

				@Override
				public boolean contains(Object o) {
					@SuppressWarnings("unchecked")
					int uIdx = viMap.idToIndexIfExist((V) o);
					return uIdx >= 0 && g.containsEdge(uIdx, vIdx);
				}

				@Override
				public Iterator<V> iterator() {
					return new Iterator<>() {
						IEdgeIter it = es.iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public V next() {
							it.nextInt();
							return viMap.indexToId(it.sourceInt());
						}
					};
				}
			};
		}
	}

	static <V> Set<EndpointPair<V>> edgesEndpoints(com.jgalgo.graph.Graph<V, ?> graph) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		return new AbstractSet<>() {

			@Override
			public int size() {
				return g.edges().size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean contains(Object o) {
				return o instanceof EndpointPair && hasEdgeConnecting(graph, (EndpointPair<V>) o);
			}

			@Override
			public Iterator<EndpointPair<V>> iterator() {
				IntFunction<EndpointPair<V>> mapper;
				if (g.isDirected()) {
					mapper = e -> {
						int uIdx = g.edgeSource(e), vIdx = g.edgeTarget(e);
						return EndpointPair.ordered(viMap.indexToId(uIdx), viMap.indexToId(vIdx));
					};
				} else {
					mapper = e -> {
						int uIdx = g.edgeSource(e), vIdx = g.edgeTarget(e);
						return EndpointPair.unordered(viMap.indexToId(uIdx), viMap.indexToId(vIdx));
					};
				}
				return IterTools.map(range(g.edges().size()).iterator(), mapper);
			}
		};
	}

	static <V> Set<V> adjacentNodes(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int uIdx = viMap.idToIndex(node);
		if (g.isDirected() || g.isAllowParallelEdges()) {
			IntSet adjacentNodes = new IntOpenHashSet();
			if (g.isDirected()) {
				for (int e : g.outEdges(uIdx))
					adjacentNodes.add(g.edgeTarget(e));
				for (int e : g.inEdges(uIdx))
					adjacentNodes.add(g.edgeSource(e));
			} else {
				for (int e : g.outEdges(uIdx))
					adjacentNodes.add(g.edgeEndpoint(e, uIdx));
				for (int e : g.inEdges(uIdx))
					adjacentNodes.add(g.edgeEndpoint(e, uIdx));

			}
			return IndexIdMaps.indexToIdSet(adjacentNodes, viMap);

		} else {
			IEdgeSet es = g.outEdges(uIdx);
			return new AbstractSet<>() {
				@Override
				public int size() {
					return es.size();
				}

				@Override
				public boolean contains(Object o) {
					@SuppressWarnings("unchecked")
					int vIdx = viMap.idToIndexIfExist((V) o);
					return vIdx >= 0 && g.containsEdge(uIdx, vIdx);
				}

				@Override
				public Iterator<V> iterator() {
					return new Iterator<>() {
						IEdgeIter it = es.iterator();

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public V next() {
							it.nextInt();
							return viMap.indexToId(it.targetInt());
						}
					};
				}
			};
		}
	}

	static <V> Set<EndpointPair<V>> incidentEdges(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int uIdx = viMap.idToIndex(node);
		if (g.isDirected()) {
			IEdgeSet outEs = g.outEdges(uIdx);
			IEdgeSet inEs = g.inEdges(uIdx);
			return new AbstractSet<>() {
				@Override
				public int size() {
					int s = outEs.size() + inEs.size();
					if (g.isAllowSelfEdges())
						for (int e : outEs)
							if (g.edgeSource(e) == g.edgeTarget(e))
								s--;
					return s;
				}

				public boolean isEmpty() {
					return outEs.isEmpty() && inEs.isEmpty();
				}

				@Override
				public boolean contains(Object o) {
					if (!(o instanceof EndpointPair))
						return false;
					@SuppressWarnings("unchecked")
					EndpointPair<V> ep = (EndpointPair<V>) o;
					int vIdx = viMap.idToIndexIfExist(ep.nodeU());
					if (vIdx < 0)
						return false;
					int wIdx = viMap.idToIndexIfExist(ep.nodeV());
					if (wIdx < 0)
						return false;
					if (uIdx == vIdx) {
						return g.containsEdge(uIdx, wIdx);
					} else if (uIdx == wIdx) {
						return g.containsEdge(vIdx, uIdx);
					} else {
						return false;
					}
				}

				@Override
				public Iterator<EndpointPair<V>> iterator() {
					IntFunction<EndpointPair<V>> outMapper =
							e -> EndpointPair.ordered(node, viMap.indexToId(g.edgeTarget(e)));
					Iterator<EndpointPair<V>> outEdges = IterTools.map(outEs.iterator(), outMapper);
					IntFunction<EndpointPair<V>> inMapper =
							e -> EndpointPair.ordered(viMap.indexToId(g.edgeSource(e)), node);
					Iterator<EndpointPair<V>> inEdges = IterTools.map(inEs.iterator(), inMapper);
					if (g.isAllowSelfEdges())
						inEdges = Iterators.filter(inEdges, ep -> ep.nodeU() != ep.nodeV());
					return Iterators.concat(outEdges, inEdges);
				}
			};

		} else {
			IEdgeSet es = g.outEdges(uIdx);
			return new AbstractSet<>() {
				@Override
				public int size() {
					return es.size();
				}

				@Override
				public boolean contains(Object o) {
					if (!(o instanceof EndpointPair))
						return false;
					@SuppressWarnings("unchecked")
					EndpointPair<V> ep = (EndpointPair<V>) o;
					int vIdx = viMap.idToIndexIfExist(ep.nodeU());
					if (vIdx < 0)
						return false;
					int wIdx = viMap.idToIndexIfExist(ep.nodeV());
					if (wIdx < 0)
						return false;
					if (uIdx == vIdx) {
						return g.containsEdge(uIdx, wIdx);
					} else if (uIdx == wIdx) {
						return g.containsEdge(vIdx, uIdx);
					} else {
						return false;
					}
				}

				@Override
				public Iterator<EndpointPair<V>> iterator() {
					IntFunction<EndpointPair<V>> mapper =
							e -> EndpointPair.unordered(node, viMap.indexToId(g.edgeEndpoint(e, uIdx)));
					return IterTools.map(es.iterator(), mapper);
				}
			};
		}
	}

	static <V> int degree(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		IndexGraph g = graph.indexGraph();
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int vIdx = viMap.idToIndex(node);
		if (g.isDirected())
			return g.outEdges(vIdx).size() + g.inEdges(vIdx).size();
		if (!g.isAllowSelfEdges())
			return g.outEdges(vIdx).size();
		/* self edges are counted twice in Guava graphs */
		int degree = g.outEdges(vIdx).size();
		for (int edge : g.outEdges(vIdx))
			if (g.edgeSource(edge) == g.edgeTarget(edge))
				degree++;
		return degree;
	}

	static <V> int outDegree(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		return graph.isDirected() ? graph.outEdges(node).size() : degree(graph, node);
	}

	static <V> int inDegree(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		return graph.isDirected() ? graph.inEdges(node).size() : degree(graph, node);
	}

	static <V> boolean hasEdgeConnecting(com.jgalgo.graph.Graph<V, ?> graph, EndpointPair<V> endpoints) {
		return getEdge(graph, endpoints) != null;
	}

	static <V> boolean hasEdgeConnecting(com.jgalgo.graph.Graph<V, ?> graph, V nodeU, V nodeV) {
		return getEdge(graph, nodeU, nodeV) != null;
	}

	static <V, E> E getEdge(com.jgalgo.graph.Graph<V, E> graph, EndpointPair<V> endpoints) {
		if (graph.isDirected() && !endpoints.isOrdered())
			return null;
		return getEdge(graph, endpoints.nodeU(), endpoints.nodeV());
	}

	static <V, E> E getEdge(com.jgalgo.graph.Graph<V, E> graph, V nodeU, V nodeV) {
		IndexIdMap<V> viMap = graph.indexGraphVerticesMap();
		int uIdx = viMap.idToIndexIfExist(nodeU);
		if (uIdx < 0)
			return null;
		int vIdx = viMap.idToIndexIfExist(nodeV);
		if (vIdx < 0)
			return null;
		IndexGraph g = graph.indexGraph();
		int eIdx = g.getEdge(uIdx, vIdx);
		return graph.indexGraphEdgesMap().indexToIdIfExist(eIdx);
	}

	static <V> boolean addNode(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		if (graph.vertices().contains(Objects.requireNonNull(node)))
			return false;
		graph.addVertex(node);
		return true;
	}

	static <V> boolean removeNode(com.jgalgo.graph.Graph<V, ?> graph, V node) {
		if (!graph.vertices().contains(Objects.requireNonNull(node)))
			return false;
		graph.removeVertex(node);
		return true;
	}

}
