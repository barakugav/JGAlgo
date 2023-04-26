package com.jgalgo;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntStack;

class ConnectivityAlgorithmImpl implements ConnectivityAlgorithm {

	private IntStack stack1;
	private IntStack stack2;
	private int[] arr1 = IntArrays.EMPTY_ARRAY;
	private int[] arr2 = IntArrays.EMPTY_ARRAY;
	private EdgeIter[] arr3 = MemoryReuse.EmptyEdgeIterArr;

	@Override
	public ConnectivityAlgorithm.Result computeConnectivityComponents(Graph g) {
		if (g instanceof DiGraph) {
			int n = g.vertices().size();
			stack1 = MemoryReuse.ensureAllocated(stack1, IntArrayList::new);
			stack2 = MemoryReuse.ensureAllocated(stack2, IntArrayList::new);
			arr1 = MemoryReuse.ensureLength(arr1, n);
			arr2 = MemoryReuse.ensureLength(arr2, n);
			arr3 = MemoryReuse.ensureLength(arr3, n);
			return new WorkerDirected(stack1, stack2, arr1, arr2, arr3).computeCC((DiGraph) g);

		} else {
			stack1 = MemoryReuse.ensureAllocated(stack1, IntArrayList::new);
			return new WorkerUndirected(stack1).computeCC((UGraph) g);
		}
	}

	private static class WorkerUndirected {
		private final IntStack stack;

		WorkerUndirected(IntStack stack) {
			assert stack.isEmpty();
			this.stack = stack;
		}

		ConnectivityAlgorithm.Result computeCC(UGraph g) {
			int n = g.vertices().size();
			int[] comp = new int[n];
			Arrays.fill(comp, -1);
			int compNum = 0;

			for (int root = 0; root < n; root++) {
				if (comp[root] != -1)
					continue;

				final int compIdx = compNum++;
				stack.push(root);
				comp[root] = compIdx;

				while (!stack.isEmpty()) {
					int u = stack.popInt();

					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						eit.nextInt();
						int v = eit.v();
						if (comp[v] != -1) {
							assert comp[v] == compIdx;
							continue;
						}
						comp[v] = compIdx;
						stack.push(v);
					}
				}
			}
			return new Result(compNum, comp);
		}
	}

	private static class WorkerDirected {
		private final IntStack s;
		private final IntStack p;
		private final int[] dfsPath;
		private final int[] c;
		private final EdgeIter[] edges;

		WorkerDirected(IntStack stack1, IntStack stack2, int[] arrN1, int[] arrN2, EdgeIter[] arrN3) {
			assert stack1.isEmpty();
			assert stack2.isEmpty();
			this.s = stack1;
			this.p = stack2;
			this.dfsPath = arrN1;
			this.c = arrN2;
			this.edges = arrN3;
		}

		ConnectivityAlgorithm.Result computeCC(DiGraph g) {
			// implementation of Tarjan's strongly connected components algorithm
			// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

			int n = g.vertices().size();
			int[] comp = new int[n];
			Arrays.fill(comp, -1);
			int compNum = 0;

			Arrays.fill(c, 0);
			int cNext = 1;

			for (int root = 0; root < n; root++) {
				if (comp[root] != -1)
					continue;
				dfsPath[0] = root;
				edges[0] = g.edgesOut(root);
				c[root] = cNext++;
				s.push(root);
				p.push(root);

				dfs: for (int depth = 0;;) {
					for (EdgeIter eit = edges[depth]; eit.hasNext();) {
						eit.nextInt();
						int v = eit.v();
						if (c[v] == 0) {
							c[v] = cNext++;
							s.push(v);
							p.push(v);

							dfsPath[++depth] = v;
							edges[depth] = g.edgesOut(v);
							continue dfs;
						} else if (comp[v] == -1)
							while (c[p.topInt()] > c[v])
								p.popInt();
					}
					int u = dfsPath[depth];
					if (p.topInt() == u) {
						int v;
						do {
							v = s.popInt();
							comp[v] = compNum;
						} while (v != u);
						compNum++;
						p.popInt();
					}

					edges[depth] = null;
					if (depth-- == 0)
						break;
				}
			}
			return new Result(compNum, comp);
		}
	}

	private static class Result implements ConnectivityAlgorithm.Result {
		private final int ccNum;
		private final int[] vertexToCC;

		private Result(int ccNum, int[] vertexToCC) {
			this.ccNum = ccNum;
			this.vertexToCC = vertexToCC;
		}

		@Override
		public int getVertexCc(int v) {
			return vertexToCC[v];
		}

		@Override
		public int getNumberOfCC() {
			return ccNum;
		}

		@Override
		public String toString() {
			return Arrays.toString(vertexToCC);
		}
	}

	static class Builder implements ConnectivityAlgorithm.Builder {
		@Override
		public ConnectivityAlgorithm build() {
			return new ConnectivityAlgorithmImpl();
		}
	}

}
