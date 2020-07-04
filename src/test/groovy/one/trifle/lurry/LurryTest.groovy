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
package one.trifle.lurry

import groovy.transform.CompileStatic
import one.trifle.lurry.connection.DatabaseType
import one.trifle.lurry.connection.LurrySource
import org.jetbrains.annotations.NotNull
import org.junit.Test

import static junit.framework.TestCase.assertEquals

@CompileStatic
class LurryTest {
    @Test
    void "simple api"() {
        // INIT
        def source = new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
            @Override List<LurrySource.Row> execute(@NotNull LQuery query, @NotNull Map<String, ?> params) { [] }
        }
        def lurry = new Lurry(source)
        def query = new LQuery("")
        def params = [:]

        // EXEC
        def results = lurry.<Person> query(query, params)

        // CHECK
        assertEquals(results, [])
    }

    static class Person {}
}