package one.trifle.lurry

import groovy.transform.CompileStatic
import junit.framework.AssertionFailedError
import one.trifle.lurry.exception.LurryQueryException
import one.trifle.lurry.exception.LurrySqlException
import one.trifle.lurry.mapper.RowMapper
import one.trifle.lurry.model.Entity
import one.trifle.lurry.model.Query
import one.trifle.lurry.parser.Parser
import one.trifle.lurry.reader.Reader
import org.junit.Test

import javax.sql.DataSource

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.atLeast
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@CompileStatic
class GQueryTemplateTest {
    private Reader reader = mock(Reader)
    private Parser parser = mock(Parser)
    private DataSource source = mock(DataSource)

    @Test
    void simple() {
        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([new Entity(Person, [new Query("query", "sql")] as Query[])])

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        assertEquals("sql", template.getSql(Person, "query", [:]))
    }

    @Test(expected = LurryQueryException)
    void notFoundQuery() {
        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([new Entity(Person, [new Query("query", "sql")] as Query[])])

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        assertEquals("", template.getSql(Person, "test", [:]))
    }

    @Test
    void similarMultiple() {
        when(reader.iterator()).thenReturn([mock(InputStream), mock(InputStream)].iterator())
        when(parser.parse(any(InputStream)))
                .thenReturn([new Entity(Person, [new Query("query1", "sql1")] as Query[])])
                .thenReturn([new Entity(Person, [new Query("query2", "sql2")] as Query[])])

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        assertEquals("sql1", template.getSql(Person, "query1", [:]))
        assertEquals("sql2", template.getSql(Person, "query2", [:]))
    }

    @Test
    void othersMultiple() {
        when(reader.iterator()).thenReturn([mock(InputStream), mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("query11", "sql11")] as Query[]),
                new Entity(Company, [new Query("query21", "sql21")] as Query[])
        ]).thenReturn([
                new Entity(Company, [new Query("query22", "sql22")] as Query[]),
                new Entity(Person, [new Query("query12", "sql12")] as Query[])
        ])

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        assertEquals("sql11", template.getSql(Person, "query11", [test: "test"] as Map<String, Object>))
        assertEquals("sql12", template.getSql(Person, "query12", [test: "test"] as Map<String, Object>))
        assertEquals("sql21", template.getSql(Company, "query21", [test: "test"] as Map<String, Object>))
        assertEquals("sql22", template.getSql(Company, "query22", [test: "test"] as Map<String, Object>))
    }

    @Test
    void simpleQueryList() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("get", "SELECT * from persons WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeQuery(any(String))).thenReturn(rs)
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        List<Person> result = template.queryList(Person, "get", [id: 7] as Map<String, Object>, new RowMapper() {
            int i = 0

            @Override
            Person mapRow(ResultSet res, int rowNum) throws SQLException {
                assert res.is(rs)
                assert i < 2
                assert rowNum == (i++)
                new Person()
            }
        })
        assertEquals(2, result.size())
        verify(stmt, times(1)).executeQuery(eq("SELECT * from persons WHERE id = 7"))
        verify(conn, atLeast(1)).close()
        verify(stmt, atLeast(1)).close()
        verify(rs, atLeast(1)).close()
    }

    @Test(expected = LurrySqlException)
    void errorQueryList() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)
        RowMapper mapper = mock(RowMapper)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("get", "SELECT * from persons WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeQuery(any(String))).thenReturn(rs)
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false)
        when(mapper.mapRow(any(ResultSet), any(int))).thenReturn(null).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        template.queryList(Person, "get", [id: 7] as Map<String, Object>, mapper)
    }

    @Test
    void defaultMapperQueryList() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("get", "SELECT * from persons WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeQuery(any(String))).thenReturn(rs)
        when(rs.next()).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        try {
            template.queryList(Person, "get", [id: 7] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeQuery(eq("SELECT * from persons WHERE id = 7"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
            verify(rs, atLeast(1)).close()
        }
    }

    @Test
    void defaultMapMapperQueryList() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("get", "SELECT * from persons WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeQuery(any(String))).thenReturn(rs)
        when(rs.next()).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        try {
            template.queryMap(Person, "get", [id: 7] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeQuery(eq("SELECT * from persons WHERE id = 7"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
            verify(rs, atLeast(1)).close()
        }
    }

    @Test
    void insert() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("insert", "INSERT INTO persons (name) VALUES(\${name.escape()})")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenReturn(1)
        when(stmt.getGeneratedKeys()).thenReturn(rs)
        when(rs.next()).thenReturn(true)
        when(rs.getLong(eq(1))).thenReturn(7L)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)
        long id = template.insert(Person, "insert", [name: "Tester"] as Map<String, Object>)

        assertEquals(7l, id)
        verify(stmt, times(1)).executeUpdate(eq("INSERT INTO persons (name) VALUES('Tester')"))
        verify(conn, atLeast(1)).close()
        verify(stmt, atLeast(1)).close()
        verify(rs, atLeast(1)).close()
    }


    @Test
    void noInsert() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("insert", "INSERT INTO persons (name) VALUES(\${name.escape()})")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenReturn(0)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        try {
            template.insert(Person, "insert", [name: "Tester"] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeUpdate(eq("INSERT INTO persons (name) VALUES('Tester')"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
        }
    }

    @Test
    void errInsert() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)
        ResultSet rs = mock(ResultSet)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("insert", "INSERT INTO persons (name) VALUES(\${name.escape()})")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenReturn(1)
        when(stmt.getGeneratedKeys()).thenReturn(rs)
        when(rs.next()).thenReturn(false)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        try {
            template.insert(Person, "insert", [name: "Tester"] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeUpdate(eq("INSERT INTO persons (name) VALUES('Tester')"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
            verify(rs, atLeast(1)).close()
        }
    }

    @Test
    void exceptionInsert() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("insert", "INSERT INTO persons (name) VALUES(\${name.escape()})")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        try {
            template.insert(Person, "insert", [name: "Tester"] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeUpdate(eq("INSERT INTO persons (name) VALUES('Tester')"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
        }
    }

    @Test
    void update() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("update", "UPDATE persons SET name = \${name.escape()} WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenReturn(8)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        int cnt = template.update(Person, "update", [name: "Tester", id: 3L] as Map<String, Object>)

        assertEquals(8, cnt)
        verify(stmt, times(1)).executeUpdate(eq("UPDATE persons SET name = 'Tester' WHERE id = 3"))
        verify(conn, atLeast(1)).close()
        verify(stmt, atLeast(1)).close()
    }

    @Test
    void errUpdate() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("update", "UPDATE persons SET name = \${name.escape()} WHERE id = \$id")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        try {
            template.update(Person, "update", [name: "Tester", id: 3L] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeUpdate(eq("UPDATE persons SET name = 'Tester' WHERE id = 3"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
        }
    }

    @Test
    void delete() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("delete", "DELETE FROM persons WHERE name = \${name.escape()}")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenReturn(8)

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        int cnt = template.delete(Person, "delete", [name: "Tester"] as Map<String, Object>)

        assertEquals(8, cnt)
        verify(stmt, times(1)).executeUpdate(eq("DELETE FROM persons WHERE name = 'Tester'"))
        verify(conn, atLeast(1)).close()
        verify(stmt, atLeast(1)).close()
    }

    @Test
    void errDelete() {
        Connection conn = mock(Connection)
        Statement stmt = mock(Statement)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([
                new Entity(Person, [new Query("delete", "DELETE FROM persons WHERE name = \${name.escape()}")] as Query[]),
                new Entity(Person, [new Query("view", "test")] as Query[])
        ])
        when(source.connection).thenReturn(conn)
        when(conn.createStatement()).thenReturn(stmt)
        when(stmt.executeUpdate(any(String))).thenThrow(new SQLException())

        GQueryTemplate template = new GQueryTemplate(source, reader, parser)

        try {
            template.delete(Person, "delete", [name: "Tester"] as Map<String, Object>)
            throw new AssertionFailedError()
        } catch (LurrySqlException exc) {
            verify(stmt, times(1)).executeUpdate(eq("DELETE FROM persons WHERE name = 'Tester'"))
            verify(conn, atLeast(1)).close()
            verify(stmt, atLeast(1)).close()
        }
    }

    static class Person {}

    static class Company {}

}
