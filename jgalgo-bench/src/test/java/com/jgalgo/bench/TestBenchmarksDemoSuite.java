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
