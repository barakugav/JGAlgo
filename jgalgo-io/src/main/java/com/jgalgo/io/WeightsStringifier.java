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

import java.util.Objects;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsByte;
import com.jgalgo.graph.WeightsChar;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsFloat;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.graph.WeightsLong;
import com.jgalgo.graph.WeightsObj;
import com.jgalgo.graph.WeightsShort;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectFunction;
import it.unimi.dsi.fastutil.chars.Char2ObjectFunction;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;

interface WeightsStringifier<K> {

	String weightStr(K id);

	static <K> WeightsStringifier<K> newInstance(Weights<K, ?> weights) {
		return new WeightsStringifier.Builder().build(weights);
	}

	static class Builder {

		private Byte2ObjectFunction<String> byteStringifier = String::valueOf;
		private Short2ObjectFunction<String> shortStringifier = String::valueOf;
		private Int2ObjectFunction<String> intStringifier = String::valueOf;
		private Long2ObjectFunction<String> longStringifier = String::valueOf;
		private Float2ObjectFunction<String> floatStringifier = String::valueOf;
		private Double2ObjectFunction<String> doubleStringifier = String::valueOf;
		private Boolean2ObjectFunction<String> boolStringifier = String::valueOf;
		private Char2ObjectFunction<String> charStringifier = String::valueOf;
		private Object2ObjectFunction<Object, String> objectStringifier = String::valueOf;

		void setByteStringifier(Byte2ObjectFunction<String> byteStringifier) {
			this.byteStringifier = Objects.requireNonNull(byteStringifier);
		}

		void setShortStringifier(Short2ObjectFunction<String> shortStringifier) {
			this.shortStringifier = Objects.requireNonNull(shortStringifier);
		}

		void setIntStringifier(Int2ObjectFunction<String> intStringifier) {
			this.intStringifier = Objects.requireNonNull(intStringifier);
		}

		void setLongStringifier(Long2ObjectFunction<String> longStringifier) {
			this.longStringifier = Objects.requireNonNull(longStringifier);
		}

		void setFloatStringifier(Float2ObjectFunction<String> floatStringifier) {
			this.floatStringifier = Objects.requireNonNull(floatStringifier);
		}

		void setDoubleStringifier(Double2ObjectFunction<String> doubleStringifier) {
			this.doubleStringifier = Objects.requireNonNull(doubleStringifier);
		}

		void setBoolStringifier(Boolean2ObjectFunction<String> boolStringifier) {
			this.boolStringifier = Objects.requireNonNull(boolStringifier);
		}

		void setCharStringifier(Char2ObjectFunction<String> charStringifier) {
			this.charStringifier = Objects.requireNonNull(charStringifier);
		}

		void setObjectStringifier(Object2ObjectFunction<Object, String> objectStringifier) {
			this.objectStringifier = Objects.requireNonNull(objectStringifier);
		}

		@SuppressWarnings("unchecked")
		<K> WeightsStringifier<K> build(Weights<K, ?> weights) {
			if (weights instanceof WeightsByte) {
				WeightsByte<K> ws = (WeightsByte<K>) weights;
				Byte2ObjectFunction<String> stringifier = byteStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsShort) {
				WeightsShort<K> ws = (WeightsShort<K>) weights;
				Short2ObjectFunction<String> stringifier = shortStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsInt) {
				WeightsInt<K> ws = (WeightsInt<K>) weights;
				Int2ObjectFunction<String> stringifier = intStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsLong) {
				WeightsLong<K> ws = (WeightsLong<K>) weights;
				Long2ObjectFunction<String> stringifier = longStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsFloat) {
				WeightsFloat<K> ws = (WeightsFloat<K>) weights;
				Float2ObjectFunction<String> stringifier = floatStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsDouble) {
				WeightsDouble<K> ws = (WeightsDouble<K>) weights;
				Double2ObjectFunction<String> stringifier = doubleStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsBool) {
				WeightsBool<K> ws = (WeightsBool<K>) weights;
				Boolean2ObjectFunction<String> stringifier = boolStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else if (weights instanceof WeightsChar) {
				WeightsChar<K> ws = (WeightsChar<K>) weights;
				Char2ObjectFunction<String> stringifier = charStringifier;
				return elm -> stringifier.get(ws.get(elm));

			} else {
				WeightsObj<K, ?> ws = (WeightsObj<K, ?>) weights;
				Object2ObjectFunction<Object, String> stringifier = objectStringifier;
				return elm -> stringifier.get(ws.get(elm));
			}
		}

	}

}
