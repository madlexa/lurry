/*
 * Copyright 2020 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.trifle.lurry.lang.interpreter

import one.trifle.lurry.lang.Token

class LFunction(private val params: List<Token>, private val body: (Map<String, Any?>) -> Any?) {
    fun call(vararg args: Any?): Any? {
        val vars = if (params.isEmpty()) {
            mapOf()
        } else {
            (params.indices).map { i -> params[i].value.toString() to args[i] }.toMap()
        }
        return body(vars)
    }
}
