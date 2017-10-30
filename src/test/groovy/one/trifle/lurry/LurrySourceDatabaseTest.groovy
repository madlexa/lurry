package one.trifle.lurry

import one.trifle.lurry.connection.DatabaseType
import org.junit.Test

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData

import static junit.framework.TestCase.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

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

    @Test(expected = LurrySqlException)
    void "init null metaData"() {
        // INIT
        def source = mock(DataSource)
        def connection = mock(Connection)
        when(connection.metaData).thenReturn(null)
        when(source.connection).thenReturn(connection)

        // EXEC
        def result = new LurrySourceDatabase(source)
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
