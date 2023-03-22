package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.ugav.algo.Graph.WeightFunctionInt;

public class MSTPrefTest extends TestUtils {

	// TODO pref test shouldn't be unit test
	@Test
	public void testRandGraph() {
		perfCompare(List.of(
				Pair.of("MSTBoruvka1926", MSTBoruvka1926::new),
				Pair.of("MSTFredmanTarjan1987", MSTFredmanTarjan1987::new),
				Pair.of("MSTKruskal1956", MSTKruskal1956::new),
				Pair.of("MSTPrim1957", MSTPrim1957::new),
				Pair.of("MSTYao1976", MSTYao1976::new),
				Pair.of("MSTKargerKleinTarjan1995", () -> new MSTKargerKleinTarjan1995(nextRandSeed()))),
				(Supplier<? extends MST> builder) -> {
					List<Phase> phases = List.of(phase(1, 0, 0), phase(1280, 16, 32), phase(640, 64, 128), phase(320, 128, 256),
							phase(80, 1024, 4096), phase(20, 4096, 16384));
					runTestMultiple(phases, (testIter, args) -> {
						int n = args[0], m = args[1];
						Graph g = GraphsTestUtils.randGraph(n, m);
						GraphsTestUtils.assignRandWeightsIntPos(g);
						WeightFunctionInt w = g.edgesWeight("weight");

						MST algo = builder.get();
						algo.calcMST(g, w);
					});
				});
	}

	@SuppressWarnings("boxing")
	private static <A> void perfCompare(Collection<Pair<String, A>> algs, Consumer<A> bench) {
		if (algs.isEmpty())
			return;

		List<Pair<String, Long>> times = new ArrayList<>(algs.size());
		for (Pair<String, A> alg : algs) {
			long begin = System.currentTimeMillis();
			bench.accept(alg.e2);
			long end = System.currentTimeMillis();
			times.add(Pair.of(alg.e1, end - begin));
		}

		times.sort((p1, p2) -> Long.compare(p1.e2, p2.e2));
		System.out.println("Performance result:");
		for (Pair<String, Long> time : times)
			System.out.println("\t" + time.e1 + ": " + time.e2);
	}

}
