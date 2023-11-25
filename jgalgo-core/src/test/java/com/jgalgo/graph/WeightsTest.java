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
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

@SuppressWarnings("boxing")
public class WeightsTest extends TestBase {

	@Test
	public void testWeightsObjects() {
		final long seed = 0xe7d6f0afb01000baL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), null, seed);
	}

	@Test
	public void testWeightsObjectsWithDefaultWeight() {
		final long seed = 0x71b6e5749ca06738L;
		Object defVal = new Object();
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Object>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, Object.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryObject(), defVal, seed);
	}

	@Test
	public void testWeightsBytes() {
		final long seed = 0x484dd5b050fbb881L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Byte>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, byte.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), (byte) 0, seed);
	}

	@Test
	public void testWeightsBytesWithDefaultWeight() {
		final long seed = 0xf14cb8abf6ed4f9fL;
		byte defVal = (byte) 0xc8;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Byte>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, byte.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryByte(seed), defVal, seed);
	}

	@Test
	public void testWeightsShorts() {
		final long seed = 0x2ee8e70daf324abfL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), (short) 0, seed);
	}

	@Test
	public void testWeightsShortsWithDefaultWeight() {
		final long seed = 0x409d909a3947948fL;
		short defVal = 0x309b;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Short>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, short.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryShort(seed), defVal, seed);
	}

	@Test
	public void testWeightsInts() {
		final long seed = 0xc81e5634eed692fdL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), 0, seed);
	}

	@Test
	public void testWeightsIntsWithDefaultWeight() {
		final long seed = 0x2024360951336d19L;
		int defVal = 0x3006b813;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Integer>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, int.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryInt(seed), defVal, seed);
	}

	@Test
	public void testWeightsLongs() {
		final long seed = 0xa3183b96d809f3f0L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Long>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, long.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), 0L, seed);
	}

	@Test
	public void testWeightsLongsWithDefaultWeight() {
		final long seed = 0x58fa7dd03fe7207eL;
		long defVal = 0x46c9069fec84a482L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Long>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, long.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryLong(seed), defVal, seed);
	}

	@Test
	public void testWeightsFloats() {
		final long seed = 0x35801d78fc5ab86eL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), 0f, seed);
	}

	@Test
	public void testWeightsFloatsWithDefaultWeight() {
		final long seed = 0xa9963dbfc0a4462bL;
		float defVal = Float.intBitsToFloat(0xe7f2aa88);
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Float>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, float.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryFloat(seed), defVal, seed);
	}

	@Test
	public void testWeightsDoubles() {
		final long seed = 0x32de9146baf98f13L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), 0.0, seed);
	}

	@Test
	public void testWeightsDoublesWithDefaultWeight() {
		final long seed = 0x5698af4847f1349eL;
		double defVal = Double.longBitsToDouble(0x0de33f798dd2ec5aL);
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Double>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, double.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryDouble(seed), defVal, seed);
	}

	@Test
	public void testWeightsBools() {
		final long seed = 0x89698a740f504d87L;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), false, seed);
	}

	@Test
	public void testWeightsBoolsWithDefaultWeight() {
		final long seed = 0x98da961379cae813L;
		boolean defVal = true;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Boolean>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, boolean.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryBool(seed), defVal, seed);
	}

	@Test
	public void testWeightsChars() {
		final long seed = 0x8ede26a1638aef7dL;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), (char) 0, seed);
	}

	@Test
	public void testWeightsCharsWithDefaultWeight() {
		final long seed = 0x25b63aa72ff77460L;
		char defVal = (char) 0xa5fb;
		BiFunction<Graph<Integer, Integer>, String, Weights<Integer, Character>> edgeWeightsAdder =
				(g, key) -> g.addEdgesWeights(key, char.class, defVal);
		WeightsTest.testWeights(edgeWeightsAdder, weightFactoryChar(seed), defVal, seed);
	}

	private static <T> void testWeights(
			BiFunction<Graph<Integer, Integer>, String, Weights<Integer, T>> edgeWeightsAdder,
			Supplier<T> weightFactory, T defaultWeight, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		final Random rand = new Random(seed);
		final int n = 1024;
		final int m = 5000;

		foreachBoolConfig((intGraph, removeEdges) -> {
			Graph<Integer, Integer> g = new RandomGraphBuilder(seedGen.nextSeed()).graphImpl(intGraph).n(n).m(m)
					.directed(false).parallelEdges(false).selfEdges(true).cycles(true).connected(false).build();

			String wKey = "edgeWeight";
			Weights<Integer, T> weights = edgeWeightsAdder.apply(g, wKey);
			assertEquals(defaultWeight, weights.defaultWeightAsObj());

			Int2ObjectMap<T> assignedEdges = new Int2ObjectOpenHashMap<>();
			assignedEdges.defaultReturnValue(defaultWeight);
			while (assignedEdges.size() < g.edges().size() * 3 / 4) {
				int op = rand.nextInt(10);

				if (op == 9 && removeEdges) {
					int e = rand.nextBoolean() ? Graphs.randEdge(g, rand)
							: g.indexGraphEdgesMap().indexToId(g.edges().size() - 1);
					g.removeEdge(e);
					assignedEdges.remove(e);
					continue;
				}

				if (op % 2 == 0) {

					// Choose random unassigned edge
					int e;
					do {
						e = Graphs.randEdge(g, rand);
					} while (assignedEdges.containsKey(e));

					// assigned to it a new value
					T val = weightFactory.get();
					weights.setAsObj(e, val);
					assignedEdges.put(e, val);

				} else {
					int e = Graphs.randEdge(g, rand);
					T actual = weights.getAsObj(e);
					T expected = assignedEdges.get(e);
					assertEquals(expected, actual);
					if (weights instanceof WeightFunction) {
						@SuppressWarnings("unchecked")
						WeightFunction<Integer> weights0 = ((WeightFunction<Integer>) weights);

						int e2;
						do {
							e2 = Graphs.randEdge(g, rand);
						} while (assignedEdges.containsKey(e2));

						assertEquals(((Number) weights.getAsObj(e)).doubleValue(), weights0.weight(e));
						if (weights instanceof WeightFunctionInt)
							assertEquals(((Number) weights.getAsObj(e)).intValue(),
									((WeightFunctionInt<Integer>) weights0).weightInt(e));
						assertEquals(Double.compare(weights0.weight(e), weights0.weight(e2)), weights0.compare(e, e2));
					}

				}
			}

			int nonExistingEdge0;
			do {
				nonExistingEdge0 = rand.nextInt();
			} while (g.edges().contains(nonExistingEdge0));
			final int nonExistingEdge = nonExistingEdge0;
			assertThrows(NoSuchEdgeException.class, () -> weights.getAsObj(-1));
			assertThrows(NoSuchEdgeException.class, () -> weights.getAsObj(nonExistingEdge));
			assertThrows(NoSuchEdgeException.class, () -> weights.setAsObj(-1, weightFactory.get()));
			assertThrows(NoSuchEdgeException.class, () -> weights.setAsObj(nonExistingEdge, weightFactory.get()));

			Weights<Integer, T> weightsImmutable = g.immutableView().getEdgesWeights(wKey);
			for (int e : g.edges())
				assertEquals(weights.getAsObj(e), weightsImmutable.getAsObj(e));
			assertEquals(weights.defaultWeightAsObj(), weightsImmutable.defaultWeightAsObj());
			assertThrows(UnsupportedOperationException.class, () -> {
				Iterator<Integer> eit = g.edges().iterator();
				Integer e1 = eit.next(), e2 = eit.next();
				weightsImmutable.setAsObj(e1, weightsImmutable.getAsObj(e2));
			});

			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weights));
			assertTrue(WeightsImpl.isEqual(g.edges(), weights, weightsImmutable));
			assertTrue(WeightsImpl.isEqual(g.edges(), weightsImmutable, weightsImmutable));

			IndexIdMap<Integer> eiMap = g.indexGraphEdgesMap();
			IWeights<T> indexWeights = IndexIdMaps.idToIndexWeights(weights, eiMap);
			Weights<Integer, T> weights2 = unknownImplementationWrap(weights);
			IWeights<T> indexWeights2 = IndexIdMaps.idToIndexWeights(weights2, eiMap);
			assertEquals(weights.defaultWeightAsObj(), indexWeights.defaultWeightAsObj());
			assertEquals(weights.defaultWeightAsObj(), indexWeights2.defaultWeightAsObj());
			for (int e : g.edges()) {
				switch (rand.nextInt(3)) {
					case 0:
						weights.setAsObj(e, weightFactory.get());
						break;
					case 1:
						indexWeights.setAsObj(eiMap.idToIndex(e), weightFactory.get());
						break;
					case 2:
						indexWeights2.setAsObj(eiMap.idToIndex(e), weightFactory.get());
						break;
					default:
						break;
				}
			}
			for (int e : g.edges()) {
				assertEquals(weights.getAsObj(e), indexWeights.getAsObj(eiMap.idToIndex(e)));
				assertEquals(weights.getAsObj(e), indexWeights2.getAsObj(eiMap.idToIndex(e)));
			}

			/* clear graph, should clear weights as well */
			g.clear();
		});
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

	@SuppressWarnings("unchecked")
	private static <K, T> Weights<K, T> unknownImplementationWrap(Weights<K, T> w) {
		if (w instanceof IWeightsObj) {
			IWeightsObj<T> w0 = (IWeightsObj<T>) w;
			return (WeightsObj<K, T>) new IWeightsObj<T>() {
				@Override
				public T get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, T weight) {
					w0.set(element, weight);
				}

				@Override
				public T defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsByte) {
			IWeightsByte w0 = (IWeightsByte) w;
			return (Weights<K, T>) new IWeightsByte() {
				@Override
				public byte get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, byte weight) {
					w0.set(element, weight);
				}

				@Override
				public byte defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsShort) {
			IWeightsShort w0 = (IWeightsShort) w;
			return (Weights<K, T>) new IWeightsShort() {
				@Override
				public short get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, short weight) {
					w0.set(element, weight);
				}

				@Override
				public short defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsInt) {
			IWeightsInt w0 = (IWeightsInt) w;
			return (Weights<K, T>) new IWeightsInt() {
				@Override
				public int get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, int weight) {
					w0.set(element, weight);
				}

				@Override
				public int defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsLong) {
			IWeightsLong w0 = (IWeightsLong) w;
			return (Weights<K, T>) new IWeightsLong() {
				@Override
				public long get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, long weight) {
					w0.set(element, weight);
				}

				@Override
				public long defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsFloat) {
			IWeightsFloat w0 = (IWeightsFloat) w;
			return (Weights<K, T>) new IWeightsFloat() {
				@Override
				public float get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, float weight) {
					w0.set(element, weight);
				}

				@Override
				public float defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsDouble) {
			IWeightsDouble w0 = (IWeightsDouble) w;
			return (Weights<K, T>) new IWeightsDouble() {
				@Override
				public double get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, double weight) {
					w0.set(element, weight);
				}

				@Override
				public double defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsBool) {
			IWeightsBool w0 = (IWeightsBool) w;
			return (Weights<K, T>) new IWeightsBool() {
				@Override
				public boolean get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, boolean weight) {
					w0.set(element, weight);
				}

				@Override
				public boolean defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof IWeightsChar) {
			IWeightsChar w0 = (IWeightsChar) w;
			return (Weights<K, T>) new IWeightsChar() {
				@Override
				public char get(int element) {
					return w0.get(element);
				}

				@Override
				public void set(int element, char weight) {
					w0.set(element, weight);
				}

				@Override
				public char defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsObj) {
			WeightsObj<K, T> w0 = (WeightsObj<K, T>) w;
			return new WeightsObj<>() {
				@Override
				public T get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, T weight) {
					w0.set(element, weight);
				}

				@Override
				public T defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsByte) {
			WeightsByte<K> w0 = (WeightsByte<K>) w;
			return (Weights<K, T>) new WeightsByte<K>() {
				@Override
				public byte get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, byte weight) {
					w0.set(element, weight);
				}

				@Override
				public byte defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsShort) {
			WeightsShort<K> w0 = (WeightsShort<K>) w;
			return (Weights<K, T>) new WeightsShort<K>() {
				@Override
				public short get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, short weight) {
					w0.set(element, weight);
				}

				@Override
				public short defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsInt) {
			WeightsInt<K> w0 = (WeightsInt<K>) w;
			return (Weights<K, T>) new WeightsInt<K>() {
				@Override
				public int get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, int weight) {
					w0.set(element, weight);
				}

				@Override
				public int defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsLong) {
			WeightsLong<K> w0 = (WeightsLong<K>) w;
			return (Weights<K, T>) new WeightsLong<K>() {
				@Override
				public long get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, long weight) {
					w0.set(element, weight);
				}

				@Override
				public long defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsFloat) {
			WeightsFloat<K> w0 = (WeightsFloat<K>) w;
			return (Weights<K, T>) new WeightsFloat<K>() {
				@Override
				public float get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, float weight) {
					w0.set(element, weight);
				}

				@Override
				public float defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsDouble) {
			WeightsDouble<K> w0 = (WeightsDouble<K>) w;
			return (Weights<K, T>) new WeightsDouble<K>() {
				@Override
				public double get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, double weight) {
					w0.set(element, weight);
				}

				@Override
				public double defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsBool) {
			WeightsBool<K> w0 = (WeightsBool<K>) w;
			return (Weights<K, T>) new WeightsBool<K>() {
				@Override
				public boolean get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, boolean weight) {
					w0.set(element, weight);
				}

				@Override
				public boolean defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else if (w instanceof WeightsChar) {
			WeightsChar<K> w0 = (WeightsChar<K>) w;
			return (Weights<K, T>) new WeightsChar<K>() {
				@Override
				public char get(K element) {
					return w0.get(element);
				}

				@Override
				public void set(K element, char weight) {
					w0.set(element, weight);
				}

				@Override
				public char defaultWeight() {
					return w0.defaultWeight();
				}
			};
		} else {
			throw new AssertionError("unknown weights type: " + w.getClass().getName());
		}
	}

}
