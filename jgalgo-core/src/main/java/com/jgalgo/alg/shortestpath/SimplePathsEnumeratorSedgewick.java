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
package com.jgalgo.alg.shortestpath;

import java.util.Iterator;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.Fastutil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Sedgewick's simple paths enumerator implementation.
 *
 * <p>
 * The algorithm is a variant of DFS, that mark visited vertices, but unmark them when the DFS backtracks.
 *
 * <p>
 * Based on 'Algorithms in c, part 5: graph algorithms' by Robert Sedgewick.
 *
 * @author Barak Ugav
 */
public class SimplePathsEnumeratorSedgewick extends SimplePathsEnumeratorAbstract {

	/**
	 * Create a algorithm for enumerating over all simple paths between a source and target vertices.
	 *
	 * <p>
	 * Please prefer using {@link SimplePathsEnumerator#newInstance()} to get a default implementation for the
	 * {@link SimplePathsEnumerator} interface.
	 */
	public SimplePathsEnumeratorSedgewick() {}

	@Override
	protected Iterator<IPath> simplePathsIter(IndexGraph g, int source, int target) {
		if (source == target)
			return ObjectIterators.singleton(IPath.valueOf(g, source, target, Fastutil.list()));
		return new Iterator<>() {

			final int n = g.vertices().size();
			Bitmap visited = new Bitmap(n);
			IEdgeIter[] edgeIter = new IEdgeIter[n];
			IntArrayList path = new IntArrayList();
			int depth = 0;
			{
				visited.set(source);
				edgeIter[depth] = g.outEdges(source).iterator();
				advance(source);
			}

			private void advance(int u) {
				dfs: for (;;) {
					for (IEdgeIter eit = edgeIter[depth]; eit.hasNext();) {
						int e = eit.nextInt();
						assert u == eit.sourceInt();
						int v = eit.targetInt();
						if (visited.get(v))
							continue;
						path.add(e);
						if (v == target)
							return;
						visited.set(v);
						depth++;
						edgeIter[depth] = g.outEdges(v).iterator();
						u = v;
						continue dfs;
					}
					depth--;
					if (depth < 0)
						return;
					visited.clear(u);
					int lastEdge = path.popInt();
					u = g.edgeEndpoint(lastEdge, u);
				}
			}

			@Override
			public boolean hasNext() {
				return depth >= 0;
			}

			@Override
			public IPath next() {
				Assertions.hasNext(this);
				IPath ret = IPath.valueOf(g, source, target, new IntArrayList(path));

				/* remove last edge to target */
				int lastEdge = path.popInt();
				/* find next path */
				advance(g.edgeEndpoint(lastEdge, target));

				return ret;
			}
		};
	}

}
