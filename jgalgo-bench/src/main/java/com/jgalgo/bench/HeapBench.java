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

package com.jgalgo.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.jgalgo.Heap;
import com.jgalgo.HeapBinary;
import com.jgalgo.HeapBinomial;
import com.jgalgo.HeapFibonacci;
import com.jgalgo.HeapPairing;
import com.jgalgo.HeapReference;
import com.jgalgo.RedBlackTree;
import com.jgalgo.SplayTree;
import com.jgalgo.bench.TestUtils.SeedGenerator;

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

	private void benchHeap(Heap.Builder heapBuilder, Blackhole blackhole) {
		Heap<Integer> heap = heapBuilder.build();

		List<Op> sequence = sequences.get(graphIdx.getAndUpdate(i -> (i + 1) % sequencesNum));
		for (Op op : sequence) {
			if (op instanceof Op.Insert) {
				HeapReference<Integer> ref = heap.insert(((Op.Insert) op).x);
				blackhole.consume(ref);

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
		benchHeap(HeapBinary::new, blackhole);
	}

	@Benchmark
	public void Pairing(Blackhole blackhole) {
		benchHeap(HeapPairing::new, blackhole);
	}

	@Benchmark
	public void Fibonacci(Blackhole blackhole) {
		benchHeap(HeapFibonacci::new, blackhole);
	}

	@Benchmark
	public void Binomial(Blackhole blackhole) {
		benchHeap(HeapBinomial::new, blackhole);
	}

	@Benchmark
	public void RedBlackTree(Blackhole blackhole) {
		benchHeap(RedBlackTree::new, blackhole);
	}

	@Benchmark
	public void SplayTree(Blackhole blackhole) {
		benchHeap(SplayTree::new, blackhole);
	}

}
