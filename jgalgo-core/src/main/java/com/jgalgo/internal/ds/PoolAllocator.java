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
package com.jgalgo.internal.ds;

import static com.jgalgo.internal.util.Range.range;

public class PoolAllocator {

    private final int[] freeEntries;
    private int freeEntriesNum;

    public PoolAllocator(int n) {
        freeEntries = new int[n];
        for (int i : range(n))
            freeEntries[i] = n - i - 1;
        freeEntriesNum = n;
    }

    public int alloc() {
        if (freeEntriesNum == 0)
            throw new IllegalStateException("No free entries left");
        return freeEntries[--freeEntriesNum];
    }

    public void free(int index) {
        freeEntries[freeEntriesNum++] = index;
    }

}
