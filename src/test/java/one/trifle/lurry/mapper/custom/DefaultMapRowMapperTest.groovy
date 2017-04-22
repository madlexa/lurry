package one.trifle.lurry.mapper.custom

import org.junit.Test

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * @author Aleksey Dobrynin
 */
class DefaultMapRowMapperTest {
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

        Map<String, Object> map = new DefaultMapRowMapper().mapRow(rs)
        assertEquals(7, map.id)
        assertEquals("test", map.name)
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

        Map<String, Object> map = new DefaultMapRowMapper().mapRow(rs)
        assertEquals(7, map.id)
        assertEquals("test", map.name)
        assertEquals("fake", map.fake)
    }

    @Test(expected = SQLException)
    void notField() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(3)
        when(metaData.getColumnName(any(int))).thenThrow(new SQLException())

        ResultSet rs = mock(ResultSet)
        when(rs.getMetaData()).thenReturn(metaData)

        new DefaultMapRowMapper().mapRow(rs)
    }

    @Test(expected = SQLException)
    void notObject() {
        ResultSetMetaData metaData = mock(ResultSetMetaData)
        when(metaData.columnCount).thenReturn(3)
        when(metaData.getColumnName(eq(1))).thenReturn("id")

        ResultSet rs = mock(ResultSet)
        when(rs.getObject(any(int))).thenThrow(new SQLException())

        when(rs.getMetaData()).thenReturn(metaData)

        new DefaultMapRowMapper().mapRow(rs)
    }

}
