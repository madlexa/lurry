package one.trifle.lurry.mapper.custom

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

/**
 * An implementation for default mapping rows
 * this implementation map sql-result field to pojo property by name
 * one by one.
 *
 * @author Aleksey Dobrynin
 */
class DefaultRowMapper<T> implements RowMapper<T> {
    private final Class<T> clazz

    /**
     * Constructor for defoult mapping
     *
     * @param clazz result class for return mapping result
     */
    DefaultRowMapper(Class<T> clazz) {
        this.clazz = clazz
    }

    @Override
    T mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.metaData
        T obj = clazz.newInstance()
        MetaClass meta = obj.getMetaClass()
        (1..metaData.columnCount).collectEntries {
            [(metaData.getColumnName((int) it)): rs.getObject((int) it)]
        }.each {
            if (obj.hasProperty(it.key as String)) {
                obj[it.key as String] = it.value?.asType(meta.getMetaProperty(it.key as String).type)
            }
        }
        return obj
    }
}
