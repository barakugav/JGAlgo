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

import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntIterables;

abstract class IndexGraphBase extends GraphBase implements IndexGraphImpl {

	private final boolean isDirected;
	private final boolean isAllowSelfEdges;
	private final boolean isAllowParallelEdges;

	IndexGraphBase(IndexGraphBase.Capabilities capabilities) {
		this.isDirected = capabilities.isDirected;
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
	}

	static class Capabilities {
		private Capabilities(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			this.isDirected = isDirected;
			this.isAllowSelfEdges = isAllowSelfEdges;
			this.isAllowParallelEdges = isAllowParallelEdges;
		}

		static Capabilities of(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			return new Capabilities(isDirected, isAllowSelfEdges, isAllowParallelEdges);
		}

		private final boolean isDirected;
		private final boolean isAllowSelfEdges;
		private final boolean isAllowParallelEdges;

	}

	@Override
	public boolean isDirected() {
		return isDirected;
	}

	@Override
	public boolean isAllowSelfEdges() {
		return isAllowSelfEdges;
	}

	@Override
	public boolean isAllowParallelEdges() {
		return isAllowParallelEdges;
	}

	@Override
	public EdgeSet getEdges(int source, int target) {
		Assertions.Graphs.checkVertex(source, vertices().size());
		Assertions.Graphs.checkVertex(target, vertices().size());
		return isDirected() ? new EdgeSetSourceTargetDirected(source, target)
				: new EdgeSetSourceTargetUndirected(source, target);
	}

	private abstract class EdgeSetSourceTarget extends EdgeSetAbstract {

		final int source, target;

		EdgeSetSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void clear() {
			for (EdgeIter it = iterator(); it.hasNext();) {
				it.nextInt();
				it.remove();
			}
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterSourceTarget(source, target);
		}
	}

	private class EdgeSetSourceTargetUndirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetUndirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			int s = edgeSource(edge), t = edgeTarget(edge);
			return (source == s && target == t) || (source == t && target == s);
		}

		@Override
		public int size() {
			return (int) IntIterables.size(this);
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}
	}

	private class EdgeSetSourceTargetDirected extends EdgeSetSourceTarget {
		EdgeSetSourceTargetDirected(int source, int target) {
			super(source, target);
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge) && target == edgeTarget(edge);
		}

		@Override
		public int size() {
			return (int) IntIterables.size(this);
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}
	}

	private class EdgeIterSourceTarget implements EdgeIter {

		private final int source, target;
		private final EdgeIter it;
		private int nextEdge = -1;

		EdgeIterSourceTarget(int source, int target) {
			this.source = source;
			this.target = target;
			it = outEdges(source).iterator();
			advance();
		}

		private void advance() {
			while (it.hasNext()) {
				int e = it.nextInt();
				if (it.target() == target) {
					nextEdge = e;
					return;
				}
			}
			nextEdge = -1;
		}

		@Override
		public boolean hasNext() {
			return nextEdge != -1;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int ret = nextEdge;
			advance();
			return ret;
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return nextEdge;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			return target;
		}
	}

}
