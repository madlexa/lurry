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

/**
 * Processor for transform Lurry-Template to Sql String
 *
 * @author Aleksey Dobrynin
 */
data class LQuery(private val entity: String, private val name: String) {

    /**
     * generate sql from template
     *
     * @param params placeholders
     *
     * @return correct sql string
     */
    fun sql(params: Map<String, Any>): String {
        TODO()
    }
}