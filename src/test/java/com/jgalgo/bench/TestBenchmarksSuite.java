package com.jgalgo.bench;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class TestBenchmarksSuite {

	@Test
	public void launchBenchmarks() throws Exception {
		String packageName = TestBenchmarksSuite.class.getPackageName();
		Options opt = new OptionsBuilder()
				.include(packageName + ".*")
				.threads(1).forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				.build();
		new Runner(opt).run();
	}

}