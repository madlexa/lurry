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

import one.trifle.lurry.mapper.MapperVersion
import org.slf4j.LoggerFactory
import java.sql.ResultSet

/**
 * An implementation for default mapping rows to Map&lt;String, Object&gt;

 * @author Aleksey Dobrynin
 */
class DefaultMapRowMapper : RowMapper<Map<String, *>> {

    override fun mapRow(rs: ResultSet): Map<String, *> {
        val metaData = rs.metaData
        return (1..metaData.columnCount)
                .map { metaData.getColumnName(it) to rs.getObject(it) }
                .toMap()
    }

    override fun getVersion(): MapperVersion {
        return MapperVersion.DEFAULT
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultMapRowMapper::class.java)
    }
}
