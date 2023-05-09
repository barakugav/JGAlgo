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

package com.jgalgo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import com.jgalgo.TestUtils.SeedGenerator;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
@State(Scope.Benchmark)
public class HeapBench {

	@Param({ "N=64 M=256", "N=512 M=4096", "N=4096 M=16384" })
	public String args;
	private List<List<Op>> sequences;
	private final int sequencesNum = 31;
	private final AtomicInteger graphIdx = new AtomicInteger();

	@Setup(Level.Iteration)
	public void setup() {
		Map<String, String> argsMap = BenchUtils.parseArgsStr(args);
		int initialSize = Integer.parseInt(argsMap.get("N"));
		int opsNum = Integer.parseInt(argsMap.get("M"));

		final SeedGenerator seedGen = new SeedGenerator(0x88da246e71ef3dacL);
		Random rand = new Random(seedGen.nextSeed());
		sequences = new ArrayList<>(sequencesNum);
		for (int gIdx = 0; gIdx < sequencesNum; gIdx++) {
			List<Op> sequence = new ArrayList<>(initialSize + opsNum);
			for (int i = 0; i < initialSize; i++)
				sequence.add(new Op.Insert(rand.nextInt()));
			for (int i = 0; i < opsNum; i++) {
				int r = rand.nextInt(5);
				if (r < 2)
					sequence.add(new Op.Insert(rand.nextInt()));
				else if (r < 4)
					sequence.add(new Op.FindMin());
				else
					sequence.add(new Op.ExtractMin());
			}
			sequences.add(sequence);
		}
	}

	private static class Op {
		private static class Insert extends Op {
			final int x;

			Insert(int x) {
				this.x = x;
			}
		}
		private static class FindMin extends Op {
		}
		private static class ExtractMin extends Op {
		}
	}

	private void benchHeap(Heap.Builder<Integer> heapBuilder, Blackhole blackhole) {
		Heap<Integer> heap = heapBuilder.build();

		List<Op> sequence = sequences.get(graphIdx.getAndUpdate(i -> (i + 1) % sequencesNum));
		for (Op op : sequence) {
			if (op instanceof Op.Insert) {
				heap.insert(((Op.Insert) op).x);

			} else if (op instanceof Op.FindMin) {
				if (heap.isEmpty())
					continue;
				Integer min = heap.findMin();
				blackhole.consume(min);

			} else if (op instanceof Op.ExtractMin) {
				if (heap.isEmpty())
					continue;
				Integer min = heap.extractMin();
				blackhole.consume(min);

			} else {
				throw new IllegalArgumentException("unknown op: " + op);
			}
		}
	}

	@Benchmark
	public void Binary(Blackhole blackhole) {
		benchHeap(Heap.newBuilder().elementsTypePrimitive(int.class), blackhole);
	}

	@Benchmark
	public void Pairing(Blackhole blackhole) {
		benchHeap(heapBuilder(HeapReferenceable.newBuilder().setOption("impl", "HeapPairing")), blackhole);
	}

	@Benchmark
	public void Fibonacci(Blackhole blackhole) {
		benchHeap(heapBuilder(HeapReferenceable.newBuilder().setOption("impl", "HeapFibonacci")), blackhole);
	}

	@Benchmark
	public void Binomial(Blackhole blackhole) {
		benchHeap(heapBuilder(HeapReferenceable.newBuilder().setOption("impl", "HeapBinomial")), blackhole);
	}

	@Benchmark
	public void RedBlackTree(Blackhole blackhole) {
		benchHeap(heapBuilder(basicRefBuilder(RedBlackTree::new)), blackhole);
	}

	@Benchmark
	public void SplayTree(Blackhole blackhole) {
		benchHeap(heapBuilder(basicRefBuilder(SplayTree::new)), blackhole);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Heap.Builder<Integer> heapBuilder(HeapReferenceable.Builder builder) {
		return new Heap.Builder<>() {
			@Override
			public Heap build(Comparator cmp) {
				return builder.keysTypePrimitive(int.class).valuesTypeVoid().build(cmp).asHeap();
			}

			@Override
			public Heap.Builder elementsTypeObj() {
				return this;
			}

			@Override
			public Heap.Builder elementsTypePrimitive(Class primitiveType) {
				return this;
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static HeapReferenceable.Builder<Integer, Void> basicRefBuilder(
			Function<Comparator<Object>, ? extends HeapReferenceable<Integer, Void>> builder) {
		return new HeapReferenceable.Builder<>() {

			@Override
			public HeapReferenceable build(Comparator cmp) {
				return builder.apply(cmp);
			}

			@Override
			public HeapReferenceable.Builder keysTypeObj() {
				return this;
			}

			@Override
			public HeapReferenceable.Builder keysTypePrimitive(Class primitiveType) {
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypeObj() {
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypePrimitive(Class primitiveType) {
				return this;
			}

			@Override
			public HeapReferenceable.Builder valuesTypeVoid() {
				return this;
			}
		};
	}

}
