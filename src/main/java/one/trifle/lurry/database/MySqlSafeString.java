/*
 * Copyright 2016 Aleksey Dobrynin
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
package one.trifle.lurry.database;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Specific mixed class for MySql special for inject in query template methods
 *
 * @author Aleksey Dobrynin
 */
public class MySqlSafeString extends DefaultSafeString {
    /**
     * escape special characters to remove sql injection
     * replace ' -&gt; '', \ -&gt; \\ and quote string
     *
     * @param str sql string
     * @return escaping string
     */
    public static String escape(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder to = new StringBuilder("'");
        for (char symbol : str.toCharArray()) {
            if (symbol == '\'' || symbol == '\\') {
                to.append(symbol);
            }
            to.append(symbol);
        }
        return to.append("'").toString();
    }

    /**
     * method for inline string array in sql
     * this method quote and escape array elements
     * and remove null elements
     * ["test1", "test2\", null, "'test3'"] -&gt; 'test1','test2\\','''test3'''
     *
     * @param list java array string
     * @return sql string
     */
    public static String join(String[] list) {
        return Arrays.stream(list)
                .filter(Objects::nonNull)
                .map(MySqlSafeString::escape)
                .collect(Collectors.joining(","));
    }
}
