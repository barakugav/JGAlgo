package com.jgalgo.bench;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class TestBenchmarksDemoSuite {

	/*
	 * Runs all benchmarks without any warmup, for 0.1 seconds each, just to check
	 */

	 @Test
	 public void launchBenchmarksDemo() throws Exception {
		 String packageName = TestBenchmarksDemoSuite.class.getPackageName();
		 Options opt = new OptionsBuilder()
				 .include(packageName + ".*")
				 .warmupIterations(0)
				 .warmupTime(TimeValue.seconds(0))
				 .measurementIterations(1)
				 .measurementTime(TimeValue.microseconds(100))
				 .threads(1).forks(1)
				 .shouldFailOnError(true)
				 .shouldDoGC(true)
				 .build();
		 new Runner(opt).run();
	 }

}
