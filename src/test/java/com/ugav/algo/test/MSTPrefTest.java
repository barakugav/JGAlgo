package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.Graphs;
import com.ugav.algo.MST;
import com.ugav.algo.MSTBoruvka1926;
import com.ugav.algo.MSTFredmanTarjan1987;
import com.ugav.algo.MSTKargerKleinTarjan1995;
import com.ugav.algo.MSTKruskal1956;
import com.ugav.algo.MSTPrim1957;
import com.ugav.algo.MSTYao1976;
import com.ugav.algo.Pair;

public class MSTPrefTest extends TestUtils {

	@Test
	public static void randGraph() {
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
						Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
						GraphsTestUtils.assignRandWeightsIntPos(g);

						MST algo = builder.get();
						WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
						algo .calcMST(g, w);
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
		printTestStr("Performance result:\n");
		for (Pair<String, Long> time : times)
			printTestStr("\t" + time.e1 + ": " + time.e2 + "\n");
	}

}
