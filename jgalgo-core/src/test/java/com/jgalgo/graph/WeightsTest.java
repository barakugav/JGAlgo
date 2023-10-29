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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

@SuppressWarnings("boxing")
public class WeightsTest extends TestBase {

	@Test
	public void testWeightsObjects() {
		final long seed = 0xe7d6f0afb01000baL;
		BiFunction<IntGraph, String, IWeights<Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), null, seed);
	}

	@Test
	public void testWeightsObjectsWithDefaultWeight() {
		final long seed = 0x71b6e5749ca06738L;
		Object defVal = new Object();
		BiFunction<IntGraph, String, IWeights<Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), defVal, seed);
	}

	@Test
	public void testWeightsBytes() {
		final long seed = 0x484dd5b050fbb881L;
		BiFunction<IntGraph, String, IWeights<Byte>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key, byte.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), (byte) 0, seed);
	}

	@Test
	public void testWeightsBytesWithDefaultWeight() {
		final long seed = 0xf14cb8abf6ed4f9fL;
		byte defVal = (byte) 0xc8;
		BiFunction<IntGraph, String, IWeights<Byte>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, byte.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), defVal, seed);
	}

	@Test
	public void testWeightsShorts() {
		final long seed = 0x2ee8e70daf324abfL;
		BiFunction<IntGraph, String, IWeights<Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), (short) 0, seed);
	}

	@Test
	public void testWeightsShortsWithDefaultWeight() {
		final long seed = 0x409d909a3947948fL;
		short defVal = 0x309b;
		BiFunction<IntGraph, String, IWeights<Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), defVal, seed);
	}

	@Test
	public void testWeightsInts() {
		final long seed = 0xc81e5634eed692fdL;
		BiFunction<IntGraph, String, IWeights<Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), 0, seed);
	}

	@Test
	public void testWeightsIntsWithDefaultWeight() {
		final long seed = 0x2024360951336d19L;
		int defVal = 0x3006b813;
		BiFunction<IntGraph, String, IWeights<Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), defVal, seed);
	}

	@Test
	public void testWeightsLongs() {
		final long seed = 0xa3183b96d809f3f0L;
		BiFunction<IntGraph, String, IWeights<Long>> edgeWeightsAdder = (g, key) -> g.addEdgesWeights(key, long.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), 0L, seed);
	}

	@Test
	public void testWeightsLongsWithDefaultWeight() {
		final long seed = 0x58fa7dd03fe7207eL;
		long defVal = 0x46c9069fec84a482L;
		BiFunction<IntGraph, String, IWeights<Long>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, long.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), defVal, seed);
	}

	@Test
	public void testWeightsFloats() {
		final long seed = 0x35801d78fc5ab86eL;
		BiFunction<IntGraph, String, IWeights<Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), 0f, seed);
	}

	@Test
	public void testWeightsFloatsWithDefaultWeight() {
		final long seed = 0xa9963dbfc0a4462bL;
		float defVal = Float.intBitsToFloat(0xe7f2aa88);
		BiFunction<IntGraph, String, IWeights<Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), defVal, seed);
	}

	@Test
	public void testWeightsDoubles() {
		final long seed = 0x32de9146baf98f13L;
		BiFunction<IntGraph, String, IWeights<Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), 0.0, seed);
	}

	@Test
	public void testWeightsDoublesWithDefaultWeight() {
		final long seed = 0x5698af4847f1349eL;
		double defVal = Double.longBitsToDouble(0x0de33f798dd2ec5aL);
		BiFunction<IntGraph, String, IWeights<Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), defVal, seed);
	}

	@Test
	public void testWeightsBools() {
		final long seed = 0x89698a740f504d87L;
		BiFunction<IntGraph, String, IWeights<Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), false, seed);
	}

	@Test
	public void testWeightsBoolsWithDefaultWeight() {
		final long seed = 0x98da961379cae813L;
		boolean defVal = true;
		BiFunction<IntGraph, String, IWeights<Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), defVal, seed);
	}

	@Test
	public void testWeightsChars() {
		final long seed = 0x8ede26a1638aef7dL;
		BiFunction<IntGraph, String, IWeights<Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), (char) 0, seed);
	}

	@Test
	public void testWeightsCharsWithDefaultWeight() {
		final long seed = 0x25b63aa72ff77460L;
		char defVal = (char) 0xa5fb;
		BiFunction<IntGraph, String, IWeights<Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), defVal, seed);
	}

	private static <T> void testWeights(BiFunction<IntGraph, String, IWeights<T>> edgeWeightsAdder,
			Supplier<T> weightFactory, T defaultWeight, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seed);
		final int n = 1024;
		final int m = 5000;

		for (boolean removeEdges : new boolean[] { false, true }) {
			IntGraph g;
			if (removeEdges) {
				g = GraphsTestUtils.randGraph(n, m, GraphArrayWithFixEdgesIDsTest.graphImpl(), seedGen.nextSeed());
			} else {
				g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());
			}

			String wKey = "edgeWeight";
			IWeights<T> weights = edgeWeightsAdder.apply(g, wKey);
			assertEquals(defaultWeight, weights.defaultWeightAsObj());

			int[] edges = g.edges().toIntArray();
			int edgesLen = edges.length;
			Int2ObjectMap<T> assignedEdges = new Int2ObjectOpenHashMap<>();
			assignedEdges.defaultReturnValue(defaultWeight);
			while (assignedEdges.size() < g.edges().size() * 3 / 4) {
				int op = rand.nextInt(10);

				if (op == 9 && removeEdges) {
					int eIdx = rand.nextInt(edgesLen);
					int e = edges[eIdx];
					g.removeEdge(e);
					assignedEdges.remove(e);
					edges[eIdx] = edges[--edgesLen];
					continue;
				}

				if (op % 2 == 0) {

					// Choose random unassigned edge
					int e;
					do {
						e = edges[rand.nextInt(edgesLen)];
					} while (assignedEdges.containsKey(e));

					// assigned to it a new value
					T val = weightFactory.get();
					weights.setAsObj(e, val);
					assignedEdges.put(e, val);

				} else {
					int e = edges[rand.nextInt(edgesLen)];
					T actual = weights.getAsObj(e);
					T expected = assignedEdges.get(e);
					assertEquals(expected, actual);
				}
			}

			int nonExistingEdge0;
			do {
				nonExistingEdge0 = rand.nextInt();
			} while (g.edges().contains(nonExistingEdge0));
			final int nonExistingEdge = nonExistingEdge0;
			assertThrows(IndexOutOfBoundsException.class, () -> weights.getAsObj(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> weights.getAsObj(nonExistingEdge));
			assertThrows(IndexOutOfBoundsException.class, () -> weights.setAsObj(-1, weightFactory.get()));
			assertThrows(IndexOutOfBoundsException.class, () -> weights.setAsObj(nonExistingEdge, weightFactory.get()));

			IWeights<T> weightsImmutable = g.immutableView().getEdgesWeights(wKey);
			for (int e : g.edges())
				assertEquals(weights.getAsObj(e), weightsImmutable.getAsObj(e));
			assertEquals(weights.defaultWeightAsObj(), weightsImmutable.defaultWeightAsObj());
			assertThrows(UnsupportedOperationException.class, () -> {
				IntIterator eit = g.edges().iterator();
				int e1 = eit.nextInt(), e2 = eit.nextInt();
				weightsImmutable.setAsObj(e1, weightsImmutable.getAsObj(e2));
			});

			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weights));
			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weightsImmutable));
			assertTrue(WeightsImpl.isEqual(g.edges(), weightsImmutable, weightsImmutable));
		}
	}

	private static Supplier<Object> weightFactoryObject() {
		return Object::new;
	}

	private static Supplier<Byte> weightFactoryByte(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Byte get() {
				return (byte) rand.nextInt();
			}
		};
	}

	private static Supplier<Short> weightFactoryShort(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Short get() {
				return (short) rand.nextInt();
			}
		};
	}

	private static Supplier<Integer> weightFactoryInt(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Integer get() {
				return rand.nextInt();
			}
		};
	}

	private static Supplier<Long> weightFactoryLong(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Long get() {
				return rand.nextLong();
			}
		};
	}

	private static Supplier<Float> weightFactoryFloat(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Float get() {
				return rand.nextFloat();
			}
		};
	}

	private static Supplier<Double> weightFactoryDouble(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Double get() {
				return rand.nextDouble();
			}
		};
	}

	private static Supplier<Boolean> weightFactoryBool(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Boolean get() {
				return rand.nextBoolean();
			}
		};
	}

	private static Supplier<Character> weightFactoryChar(long seed) {
		return new Supplier<>() {
			Random rand = new Random(seed);

			@Override
			public Character get() {
				return (char) rand.nextInt();
			}
		};
	}

}
