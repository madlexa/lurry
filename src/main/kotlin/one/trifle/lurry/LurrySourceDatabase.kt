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

import one.trifle.lurry.connection.DatabaseType
import one.trifle.lurry.connection.LurrySource
import one.trifle.lurry.connection.map
import one.trifle.lurry.connection.use
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class LurrySourceDatabase(private val source: DataSource) : LurrySource {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(LurrySourceDatabase::class.java)
    }

    override val type: DatabaseType = source.connection.use { conn ->
        val metaData = conn.metaData
        if (metaData == null) {
            LOGGER.error("metaData empty")
            throw LurrySqlException("metaData empty")
        }
        return@use DatabaseType.of(metaData.databaseProductName)
    } ?: DatabaseType.DEFAULT

    override fun execute(query: LQuery, params: Map<String, Any>) = source.connection.use { conn ->
        return@use conn.createStatement().use { stmt ->
            return@use stmt.executeQuery(query.sql(params, type.mixed)).map { columns, result ->
                return@map DatabaseRow(columns.map { field ->  field to result.getObject(field) }.toMap())
            }
        }
    } ?: emptyList()

    data class DatabaseRow(private val data: Map<String, Any>) : LurrySource.Row {
        override fun toMap(): Map<String, Any> = data
    }
}
