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
package com.jgalgo.alg.cover;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Bar Yehuda's vertex cover algorithm.
 *
 * <p>
 * Runs in linear time.
 *
 * <p>
 * Based on 'A linear-time approximation algorithm for the weighted vertex cover problem' by R Bar-Yehuda.
 *
 * @author Barak Ugav
 */
public class VertexCoverBarYehuda extends VertexCoverAbstract {

	/**
	 * Create a new Bar Yehuda's vertex cover algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link VertexCover#newInstance()} to get a default implementation for the {@link VertexCover}
	 * interface.
	 */
	public VertexCoverBarYehuda() {}

	@Override
	protected IntSet computeMinimumVertexCover(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		double[] sw = new double[n];
		for (int v : range(n))
			sw[v] = w.weight(v);

		Bitmap cover = new Bitmap(n);

		for (int e : range(g.edges().size())) {
			int u, v;
			if (cover.get(u = g.edgeSource(e)) || cover.get(v = g.edgeTarget(e)))
				continue;
			if (sw[u] <= sw[v]) {
				sw[v] -= sw[u];
				// sw[u] -= sw[u]; don't bother
				cover.set(u);
			} else {
				sw[u] -= sw[v];
				// sw[v] -= sw[v]; don't bother
				cover.set(v);
			}
		}
		return ImmutableIntArraySet.withBitmap(cover);
	}

}
