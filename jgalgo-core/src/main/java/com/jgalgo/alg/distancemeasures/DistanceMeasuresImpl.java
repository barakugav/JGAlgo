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
package com.jgalgo.alg.distancemeasures;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;
import com.jgalgo.alg.shortestpath.ShortestPathAllPairs;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

class DistanceMeasuresImpl {

	private DistanceMeasuresImpl() {}

	static class IndexImpl implements IDistanceMeasures {

		private final IndexGraph g;
		private final ShortestPathAllPairs.IResult sp;

		private double radius;
		private boolean radiusComputed;
		private double diameter;
		private boolean diameterComputed;
		private double[] eccentricity;
		private IntSet center;
		private IntSet periphery;

		IndexImpl(IndexGraph g, ShortestPathAllPairs.IResult sp) {
			this.g = Objects.requireNonNull(g);
			this.sp = Objects.requireNonNull(sp);
		}

		@Override
		public double radius() {
			if (!radiusComputed) {
				computeEccentricity();
				radius = Arrays.stream(eccentricity).min().orElse(Double.POSITIVE_INFINITY);
				radiusComputed = true;
			}
			return radius;
		}

		@Override
		public double diameter() {
			if (!diameterComputed) {
				computeEccentricity();
				diameter = Arrays.stream(eccentricity).max().orElse(Double.POSITIVE_INFINITY);
				diameterComputed = true;
			}
			return diameter;
		}

		@Override
		public double eccentricity(int v) {
			computeEccentricity();
			Assertions.checkVertex(v, g.vertices().size());
			return eccentricity[v];
		}

		@Override
		public IntSet center() {
			if (center == null) {
				double radius = radius();
				double eps = Double.isFinite(radius) ? radius * 1e-8 : 0;
				final int n = g.vertices().size();
				int[] centerArr = range(n).filter(v -> eccentricity[v] <= radius + eps).toArray();
				center = ImmutableIntArraySet.withBitmap(centerArr, n);
			}
			return center;
		}

		@Override
		public IntSet periphery() {
			if (periphery == null) {
				double diameter = diameter();
				double eps = Double.isFinite(diameter) ? diameter * 1e-8 : 0;
				final int n = g.vertices().size();
				int[] peripheryArr = range(n).filter(v -> eccentricity[v] >= diameter - eps).toArray();
				periphery = ImmutableIntArraySet.withBitmap(peripheryArr, n);
			}
			return periphery;
		}

		private void computeEccentricity() {
			if (eccentricity != null)
				return;
			final int n = g.vertices().size();
			IntToDoubleFunction uEccentricity =
					u -> range(n).mapToDouble(v -> sp.distance(u, v)).max().orElse(Double.POSITIVE_INFINITY);
			IntStream stream = range(n).intStream();
			if (JGAlgoConfigImpl.ParallelByDefault)
				stream = stream.parallel();
			eccentricity = stream.mapToDouble(uEccentricity).toArray();
		}
	}

	private static class ObjMeasuresFromIndexMeasures<V, E> implements DistanceMeasures<V, E> {

		private final IDistanceMeasures indexMeasures;
		private final IndexIdMap<V> viMap;

		ObjMeasuresFromIndexMeasures(Graph<V, E> g, IDistanceMeasures indexMeasures) {
			this.indexMeasures = Objects.requireNonNull(indexMeasures);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double radius() {
			return indexMeasures.radius();
		}

		@Override
		public double diameter() {
			return indexMeasures.diameter();
		}

		@Override
		public double eccentricity(V v) {
			return indexMeasures.eccentricity(viMap.idToIndex(v));
		}

		@Override
		public Set<V> center() {
			return IndexIdMaps.indexToIdSet(indexMeasures.center(), viMap);
		}

		@Override
		public Set<V> periphery() {
			return IndexIdMaps.indexToIdSet(indexMeasures.periphery(), viMap);
		}
	}

	private static class IntMeasuresFromIndexMeasures implements IDistanceMeasures {

		private final IDistanceMeasures indexMeasures;
		private final IndexIntIdMap viMap;

		IntMeasuresFromIndexMeasures(IntGraph g, IDistanceMeasures indexMeasures) {
			this.indexMeasures = Objects.requireNonNull(indexMeasures);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double radius() {
			return indexMeasures.radius();
		}

		@Override
		public double diameter() {
			return indexMeasures.diameter();
		}

		@Override
		public double eccentricity(int v) {
			return indexMeasures.eccentricity(viMap.idToIndex(v));
		}

		@Override
		public IntSet center() {
			return IndexIdMaps.indexToIdSet(indexMeasures.center(), viMap);
		}

		@Override
		public IntSet periphery() {
			return IndexIdMaps.indexToIdSet(indexMeasures.periphery(), viMap);
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> DistanceMeasures<V, E> measuresFromIndexMeasures(Graph<V, E> g, IDistanceMeasures indexMeasures) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (DistanceMeasures<V, E>) new IntMeasuresFromIndexMeasures((IntGraph) g, indexMeasures);
		} else {
			return new ObjMeasuresFromIndexMeasures<>(g, indexMeasures);
		}
	}

}
