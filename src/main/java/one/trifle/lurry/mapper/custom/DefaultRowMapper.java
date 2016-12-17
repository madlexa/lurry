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
package one.trifle.lurry.mapper.custom;

import one.trifle.lurry.exception.LurryMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation for default mapping rows
 * this implementation map sql-result field to pojo property by name
 * one by one.
 *
 * @author Aleksey Dobrynin
 */
public class DefaultRowMapper<T> implements RowMapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRowMapper.class);

    private final Class<T> clazz;

    /**
     * Constructor for defoult mapping
     *
     * @param clazz result class for return mapping result
     */
    public DefaultRowMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            T obj = clazz.newInstance();

            int size = metaData.getColumnCount();
            Map<String, Object> values = new HashMap<>(size, 1);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                values.put(metaData.getColumnName(i), rs.getObject(i));
            }

            for (PropertyDescriptor propertyDescriptor :
                    Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method method = propertyDescriptor.getWriteMethod();
                String name = propertyDescriptor.getName();
                if (method == null) {
                    continue;
                }
                if (values.containsKey(name)) {
                    method.invoke(obj, values.get(name));
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IntrospectionException | IllegalArgumentException exc) {
            LOGGER.error("mapping error", exc);
            throw new LurryMappingException("mapping error", exc);
        }
    }
}
