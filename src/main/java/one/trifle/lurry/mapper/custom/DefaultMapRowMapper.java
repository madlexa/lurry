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

import one.trifle.lurry.exception.LurrySqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation for default mapping rows to Map&lt;String, Object&gt;
 *
 * @author Aleksey Dobrynin
 */
public class DefaultMapRowMapper implements RowMapper<Map<String, Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMapRowMapper.class);

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        return IntStream.range(0, metaData.getColumnCount())
                .boxed()
                .collect(Collectors.toMap(it -> getColumnName(metaData, it),
                        it -> getColumnValue(rs, it)));
    }

    private String getColumnName(ResultSetMetaData metaData, int index) {
        try {
            return metaData.getColumnName(index + 1);
        } catch (SQLException exc) {
            LOGGER.error("error when get column name {}", index + 1, exc);
            throw new LurrySqlException("", exc);
        }
    }

    private Object getColumnValue(ResultSet rs, int index) {
        try {
            return rs.getObject(index + 1);
        } catch (SQLException exc) {
            LOGGER.error("error when get result set value {}", index + 1, exc);
            throw new LurrySqlException("", exc);
        }
    }
}
