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

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class BenchUtils {

	static Map<String, String> parseArgsStr(String s) {
		String[] strs = s.split(" ");
		Map<String, String> args = new Object2ObjectArrayMap<>(strs.length);
		for (String arg : strs) {
			int idx = arg.indexOf('=');
			String key = arg.substring(0, idx);
			String value = arg.substring(idx + 1);
			args.put(key, value);
		}
		return args;
	}

}
