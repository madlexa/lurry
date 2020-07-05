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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockito.Mockito.mock


import static org.mockito.Mockito.when

@CompileStatic
class LurrySourceDatabaseTest {
    @Test
    void "init null connection"() {
        // INIT
        def source = mock(DataSource)
        when(source.connection).thenReturn(null)

        // EXEC
        def result = new LurrySourceDatabase(source)

        // CHECK
        assertEquals(result.type, DatabaseType.DEFAULT)
    }

    @Test
    void "init null metaData"() {
        // INIT
        def source = mock(DataSource)
        def connection = mock(Connection)
        when(connection.metaData).thenReturn(null)
        when(source.connection).thenReturn(connection)

        // EXEC
        Assertions.assertThrows(LurrySqlException.class, {
            def result = new LurrySourceDatabase(source)
        })
    }

    @Test
    void "init mysql driver"() {
        // INIT
        def source = mock(DataSource)
        def connection = mock(Connection)
        def metadata = mock(DatabaseMetaData)
        when(metadata.databaseProductName).thenReturn("com.mysql.jdbc.Driver")
        when(connection.metaData).thenReturn(metadata)
        when(source.connection).thenReturn(connection)

        // EXEC
        def result = new LurrySourceDatabase(source)

        // CHECK
        assertEquals(result.type, DatabaseType.MYSQL)
    }

    @Test
    void "init mysql name"() {
        // INIT
        def source = mock(DataSource)
        def connection = mock(Connection)
        def metadata = mock(DatabaseMetaData)
        when(metadata.databaseProductName).thenReturn("MySQL")
        when(connection.metaData).thenReturn(metadata)
        when(source.connection).thenReturn(connection)

        // EXEC
        def result = new LurrySourceDatabase(source)

        // CHECK
        assertEquals(result.type, DatabaseType.MYSQL)
    }

}
