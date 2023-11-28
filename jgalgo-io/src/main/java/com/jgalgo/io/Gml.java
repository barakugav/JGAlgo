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
package com.jgalgo.io;

class Gml {

	private Gml() {}

	static boolean isKeyCharPrefix(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
	}

	static boolean isKeyChar(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
	}

	static boolean isNumSign(char c) {
		return c == '+' || c == '-';
	}

	static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	static void checkValidWeightsKey(String key) {
		if (key == null || key.isEmpty())
			throw new IllegalArgumentException("invalid key: '" + key + "'");
		if (!isKeyCharPrefix(key.charAt(0)))
			throw new IllegalArgumentException("invalid key: '" + key + "'");
		for (int i = 1; i < key.length(); i++)
			if (!isKeyChar(key.charAt(i)))
				throw new IllegalArgumentException("invalid key: '" + key + "'");
	}

}
