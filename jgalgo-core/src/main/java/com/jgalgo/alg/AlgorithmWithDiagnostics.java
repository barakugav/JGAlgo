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
package com.jgalgo.alg;

interface AlgorithmWithDiagnostics {

	/**
	 * <b>[TL;DR Don't call me!]</b> Get a diagnostic value.
	 * <p>
	 * The algorithm may collect diagnostic data on its running performance and expose these values. The collected
	 * diagnostics types are not exposed as 'public' because they are not part of the API and may change in the future.
	 * <p>
	 * These diagnostics are mainly for debug and benchmark purposes.
	 *
	 * @param  key the diagnostic key
	 * @return     the diagnostic value
	 */
	default Object getDiagnostic(String key) {
		return null;
	}

}
