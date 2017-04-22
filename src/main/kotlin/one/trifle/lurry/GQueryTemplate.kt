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
package one.trifle.lurry

import one.trifle.lurry.exception.LurryQueryException
import one.trifle.lurry.exception.LurrySqlException
import one.trifle.lurry.logic.QueryProcessor
import one.trifle.lurry.mapper.custom.DefaultMapRowMapper
import one.trifle.lurry.mapper.custom.RowMapper
import one.trifle.lurry.model.Query
import one.trifle.lurry.parser.Parser
import one.trifle.lurry.reader.Reader
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource
import kotlin.collections.HashMap

/**
 * Main class of lurry library for execute query and map to object
 * recommend single QueryFactory for one DataSource
 *
 * @param source for escape string and execute query
 * @param reader reader implementation
 * @param parser parser implementation
 *
 * @author Aleksey Dobrynin
 */
class GQueryTemplate(private val source: DataSource, reader: Reader, parser: Parser) {
    private val processor = QueryProcessor(source)
    private val cache = HashMap<String, Query>()

    init {
        LOGGER.trace("start init data")
        for (source in reader) {
            LOGGER.trace("read source")
            for ((name, queries) in parser.parse(source)) {
                LOGGER.trace("read entity {}", name)
                for (query in queries) {
                    cache[getCacheKey(name.simpleName, query.name)] = query
                }
            }
        }

        LOGGER.trace("finish init data")
    }

    private fun getQuery(entity: String, queryName: String): Query {
        val query = cache[getCacheKey(entity, queryName)]
        if (query != null) {
            return query
        }
        throw LurryQueryException("not found query [entity='${entity}', queryName=${queryName}]", null)
    }

    internal fun getSql(entity: String, queryName: String, params: Map<String, Any>): String {
        return getSql(getQuery(entity, queryName), params)
    }

    private fun getSql(query: Query, params: Map<String, Any>): String {
        LOGGER.trace("start prepare sql")
        return processor.prepare(query, params)
    }

    /**
     * Method find query with sql-template by entity and queryName
     * after inject params and execute sql
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @param mapper    instance for map row to java object
     * @param <T>       result class after mapping
     * @return unbounded list, after transform mapping
    </T> */
    fun <T> queryList(entity: String, queryName: String, params: Map<String, Any>, mapper: RowMapper<T>): List<T> {
        val sql = getSql(entity, queryName, params)
        LOGGER.trace("execute: sql = {}", sql)
        val result = ArrayList<T>()
        try {
            source.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        var position = 0
                        while (rs.next()) {
                            LOGGER.trace("entity = '{}', query = '{}'\nmap: row #{}", entity, queryName, position++)
                            result.add(mapper.mapRow(rs))
                        }
                    }
                }
            }
        } catch (exc: SQLException) {
            LOGGER.error("execute query: entity = '{}', query = '{}'", entity, queryName, exc)
            throw LurrySqlException("Error: execute query", exc)
        }

        return result
    }

    /**
     * Method find query with sql-template by entity and queryName
     * after inject params and execute sql. Use DefaultRowMapper
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @param <T>       result class after mapping
     * @return unbounded list, after transform mapping
    </T> */
    fun <T> queryList(entity: String, queryName: String, params: Map<String, Any>): List<T> {
//        return queryList<Any>(entity, queryName, params, DefaultRowMapper<Any>(null!!)) // TODO
        return emptyList()
    }

    /**
     * Method find query with sql-template by entity and queryName
     * after inject params and execute sql. Use [DefaultMapRowMapper]
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @return unbounded list with map key value
     */
    fun queryMap(entity: String, queryName: String, params: Map<String, Any>): List<Map<String, *>> {
        return queryList(entity, queryName, params, DefaultMapRowMapper())
    }

    /**
     * Insert method find query with sql-template by entity and queryName
     * after inject params and execute sql.
     *
     * @param entity    find class
     * @param queryName name from source
     * @param params    params for inject in template
     * @return inserted id
     */
    fun insert(entity: String, queryName: String, params: Map<String, Any>): Long? {
        val sql = getSql(entity, queryName, params)
        LOGGER.trace("insert: sql = {}", sql)
        try {
            source.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    val rows = stmt.executeUpdate(sql)
                    if (rows == 0) {
                        LOGGER.error("created {} fail, no rows inserted", entity)
                        throw LurrySqlException("Creating row failed, no rows affected.", null)
                    }

                    stmt.generatedKeys.use { rs ->
                        if (rs.next()) {
                            return rs.getLong(1)
                        }
                    }
                }
            }
        } catch (exc: SQLException) {
            LOGGER.error("execute query: entity = '{}', query = '{}'", entity, queryName, exc)
            throw LurrySqlException("Error: execute query", exc)
        }

        LOGGER.error("created {} fail, no rows inserted", entity)
        throw LurrySqlException("created row fail, no rows inserted.", null)
    }

    /**
     * Update method find query with sql-template by entity and queryName
     * after inject params and execute sql.
     *
     * @param entity    find class
     * @param queryName name from source
     * @param params    params for inject in template
     * @return rows affected
     */
    fun update(entity: String, queryName: String, params: Map<String, Any>): Int {
        val sql = getSql(entity, queryName, params)
        LOGGER.trace("update: sql = {}", sql)
        try {
            source.connection.use { conn -> conn.createStatement().use { stmt -> return stmt.executeUpdate(sql) } }
        } catch (exc: SQLException) {
            LOGGER.error("execute query: entity = '{}', query = '{}'", entity, queryName, exc)
            throw LurrySqlException("Error: execute query", exc)
        }
    }

    /**
     * Delete method find query with sql-template by entity and queryName
     * after inject params and execute sql.
     *
     * @param entity    find class
     * @param queryName name from source
     * @param params    params for inject in template
     * @return rows affected
     */
    fun delete(entity: String, queryName: String, params: Map<String, Any>): Int {
        val sql = getSql(entity, queryName, params)
        LOGGER.trace("delete: sql = {}", sql)
        try {
            source.connection.use { conn -> conn.createStatement().use { stmt -> return stmt.executeUpdate(sql) } }
        } catch (exc: SQLException) {
            LOGGER.error("execute query: entity = '{}', query = '{}'", entity, queryName, exc)
            throw LurrySqlException("Error: execute query", exc)
        }
    }

    private fun getCacheKey(entity: String, query: String): String {
        return "${entity}_${query}"
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GQueryTemplate::class.java)
    }
}
