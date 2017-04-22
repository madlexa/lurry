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
package one.trifle.lurry.mapper.custom

import one.trifle.lurry.exception.LurryMappingException
import one.trifle.lurry.mapper.MapperVersion
import org.slf4j.LoggerFactory
import java.beans.IntrospectionException
import java.beans.Introspector
import java.lang.reflect.InvocationTargetException
import java.sql.ResultSet

/**
 * An implementation for default mapping rows
 * this implementation map sql-result field to pojo property by name
 * one by one.

 * @author Aleksey Dobrynin
 */
class DefaultRowMapper<T>(private val clazz: Class<T>) : RowMapper<T> {
    override fun mapRow(rs: ResultSet): T {
        try {
            val metaData = rs.metaData
            val obj = clazz.newInstance()

            val values = (1..metaData.columnCount)
                    .map { columnNum -> metaData.getColumnName(columnNum) to rs.getObject(columnNum) }
                    .toMap()

            for (propertyDescriptor in Introspector.getBeanInfo(clazz).propertyDescriptors) {
                val method = propertyDescriptor.writeMethod
                val name = propertyDescriptor.name
                if (method == null) {
                    continue
                }
                if (values.containsKey(name)) {
                    method.invoke(obj, values[name])
                }
            }
            return obj
        } catch (exc: InstantiationException) {
            LOGGER.error("mapping error", exc)
            throw LurryMappingException("mapping error", exc)
        } catch (exc: IllegalAccessException) {
            LOGGER.error("mapping error", exc)
            throw LurryMappingException("mapping error", exc)
        } catch (exc: InvocationTargetException) {
            LOGGER.error("mapping error", exc)
            throw LurryMappingException("mapping error", exc)
        } catch (exc: IntrospectionException) {
            LOGGER.error("mapping error", exc)
            throw LurryMappingException("mapping error", exc)
        } catch (exc: IllegalArgumentException) {
            LOGGER.error("mapping error", exc)
            throw LurryMappingException("mapping error", exc)
        }

    }

    override fun getVersion(): MapperVersion {
        return MapperVersion.MAP
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRowMapper::class.java)
    }
}
