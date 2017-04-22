package one.trifle.lurry.mapper.custom

import one.trifle.lurry.exception.LurryMappingException
import org.junit.Test

import java.sql.ResultSet
import java.sql.ResultSetMetaData

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * @author Aleksey Dobrynin
 */
class DefaultRowMapperTest {
    @Test
    void simple() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(2)
        when(metaData.getColumnName(eq(1))).thenReturn("id")
        when(metaData.getColumnName(eq(2))).thenReturn("name")

        ResultSet rs = mock(ResultSet)
        when(rs.getMetaData()).thenReturn(metaData)
        when(rs.getObject(eq(1))).thenReturn(7)
        when(rs.getObject(eq(2))).thenReturn("test")

        RowMapper mapper = new DefaultRowMapper<Person>(Person)
        Person person = mapper.mapRow(rs)
        assertEquals(7, person.id)
        assertEquals("test", person.name)
    }

    @Test
    void moreFields() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(3)
        when(metaData.getColumnName(eq(1))).thenReturn("id")
        when(metaData.getColumnName(eq(2))).thenReturn("name")
        when(metaData.getColumnName(eq(3))).thenReturn("fake")

        ResultSet rs = mock(ResultSet)
        when(rs.getMetaData()).thenReturn(metaData)
        when(rs.getObject(eq(1))).thenReturn(7)
        when(rs.getObject(eq(2))).thenReturn("test")
        when(rs.getObject(eq(3))).thenReturn("fake")

        RowMapper mapper = new DefaultRowMapper<Person>(Person)
        Person person = mapper.mapRow(rs)
        assertEquals(7, person.id)
        assertEquals("test", person.name)
    }

    @Test
    void lessFields() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(1)
        when(metaData.getColumnName(eq(1))).thenReturn("name")

        ResultSet rs = mock(ResultSet)
        when(rs.getMetaData()).thenReturn(metaData)
        when(rs.getObject(eq(1))).thenReturn("test")

        RowMapper mapper = new DefaultRowMapper<Person>(Person)
        Person person = mapper.mapRow(rs)
        assertEquals(null, person.id)
        assertEquals("test", person.name)
    }

    @Test(expected = LurryMappingException)
    void exceptionType() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(1)
        when(metaData.getColumnName(eq(1))).thenReturn("id")

        ResultSet rs = mock(ResultSet)
        when(rs.getMetaData()).thenReturn(metaData)
        when(rs.getObject(eq(1))).thenReturn("test")

        RowMapper mapper = new DefaultRowMapper<Person>(Person)
        mapper.mapRow(rs)
    }

    static class Person {
        private Integer id
        private String name

        Integer getId() {
            return id
        }

        void setId(Integer id) {
            this.id = id
        }

        String getName() {
            return name
        }

        void setName(String name) {
            this.name = name
        }
    }
}
