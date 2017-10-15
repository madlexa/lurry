/*
 * Copyright 2017 Aleksey Dobrynin
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
package one.trifle.lurry

import one.trifle.lurry.connection.LurrySource

/**
 * Main class of lurry library for execute query and map to object
 *
 * @param source database connection source
 *
 * @author Aleksey Dobrynin
 */
class Lurry(private val source: LurrySource) {
    /**
     * execute query with params
     *
     * @param query executed query
     * @param params template placeholders
     *
     * @return result list with objects
     */
    fun <T> query(query: LQuery, params: Map<String, Any>): List<T> {
        return emptyList()
    }
}