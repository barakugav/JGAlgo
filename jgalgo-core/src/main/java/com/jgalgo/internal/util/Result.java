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
package com.jgalgo.internal.util;

public final class Result<T, E> {

    private final Object result;
    private final boolean ok;

    private Result(Object result, boolean ok) {
        this.result = result;
        this.ok = ok;
    }

    public static <T, E> Result<T, E> ok(T result) {
        return new Result<>(result, true);
    }

    public static <T, E> Result<T, E> error(E error) {
        return new Result<>(error, false);
    }

    public boolean isOk() {
        return ok;
    }

    public boolean isErr() {
        return !ok;
    }

    public T unwrap() {
        if (!isOk())
            throw new IllegalStateException("Result is not ok");
        return (T) result;
    }

    public E unwrapError() {
        if (!isErr())
            throw new IllegalStateException("Result is ok");
        return (E) result;
    }

}
