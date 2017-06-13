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
package one.trifle.lurry.database

/**
 * Mixed class special for inject in query template methods
 *
 * @author Aleksey Dobrynin
 */
object DefaultSafeString {
    /**
     * escape special characters to remove sql injection
     * replace ' -&gt; '' and quote string
     *
     * @param str sql string
     * @return escaping string
     */
    @JvmStatic fun escape(str: String?): String? {
        if (str == null) {
            return null
        }
        val to = StringBuilder("'")
        for (symbol in str) {
            if (symbol == '\'') {
                to.append(symbol)
            }
            to.append(symbol)
        }
        return to.append("'").toString()
    }

    /**
     * method for inline string array in sql
     * this method quote and escape array elements
     * and remove null elements
     * ["test1", "test2", null, "'test3'"] -&gt; 'test1','test2','''test3'''
     *
     * @param list java array string
     * @return sql string
     */
    @JvmStatic fun join(list: Array<String?>): String = list
            .filter { str -> str != null }
            .map { str -> escape(str) }
            .joinToString(",")

    /**
     * method for inline numbers array in sql
     * this method without quote array elements and without escape
     * and remove null elements
     * [1, 2, null, 3, 4] -&gt; 1,2,3,4

     * @param list java array number
     * *
     * @return sql string
     */
    @JvmStatic fun join(list: Array<Number?>): String = list
            .filter { num -> num != null }
            .map(Number?::toString)
            .joinToString(",")
}
