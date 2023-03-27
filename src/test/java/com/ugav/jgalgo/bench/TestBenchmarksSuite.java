package com.ugav.jgalgo.bench;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class TestBenchmarksSuite {

	@Test
	public void launchBenchmarks() throws Exception {
		String packageName = TestBenchmarksSuite.class.getPackageName();
		Options opt = new OptionsBuilder()
				.include(packageName + ".*")
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(1))
				.warmupIterations(2)
				.measurementTime(TimeValue.seconds(1))
				.measurementIterations(2)
				.threads(1).forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				.build();
		new Runner(opt).run();
	}

}