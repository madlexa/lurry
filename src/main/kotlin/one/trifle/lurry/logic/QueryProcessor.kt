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
package one.trifle.lurry.logic

import groovy.lang.Closure
import groovy.lang.GroovyRuntimeException
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import one.trifle.lurry.database.DatabaseType
import one.trifle.lurry.exception.LurrySqlException
import one.trifle.lurry.model.Query
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.slf4j.LoggerFactory
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/**
 * Processor for transform Lurry-Template to Sql String
 *
 * @author Aleksey Dobrynin
 */
class QueryProcessor(source: DataSource) {
    private val LOGGER = LoggerFactory.getLogger(QueryProcessor::class.java)

    private var type: DatabaseType = DatabaseType.DEFAULT
    private val cache = ConcurrentHashMap<Query, Template>()

    init {
        try {
            source.connection.use { conn ->
                if (conn == null) {
                    LOGGER.error("connection empty")
                    throw LurrySqlException("connection empty", null)
                }
                val metaData = conn.getMetaData()
                if (metaData == null) {
                    LOGGER.error("metaData empty")
                    throw LurrySqlException("metaData empty", null)
                }
                type = DatabaseType.of(metaData.getDatabaseProductName())
            }
        } catch (exc: SQLException) {
            LOGGER.error("get resource type error", exc)
            throw LurrySqlException("get resource type error", exc)
        }
    }

    private fun getMixed(): Class<*> {
        return type.mixed
    }

    /**
     * inject params into sql and exec specific code in template

     * @param query  object with sql-template
     * *
     * @param params data for inject
     * *
     * @return sql for execute
     */
    fun prepare(query: Query, params: Map<String, Any>): String {
        val vals = object : HashMap<String, Any>() {
            override fun containsKey(key: String): Boolean {
                return true
            }
        }
        vals.putAll(params)

        return DefaultGroovyMethods.use(QueryProcessor::class.java, getMixed(),
                object : Closure<String>(this, this) {
                    fun doCall(): String {
                        return get(query).make(vals).toString()
                    }
                })
    }

    /**
     * get cached template

     * @param query
     * *
     * @return GStringTemplate
     */
    private operator fun get(query: Query): Template {
        var template: Template? = cache[query]
        if (template != null) {
            return template
        }
        try {
            template = GStringTemplateEngine().createTemplate(query.sql)
            cache.put(query, template)
            return template
        } catch (exc: ClassNotFoundException) {
            LOGGER.error("compile sql template error:\n{}", query.sql, exc)
            throw LurrySqlException("compile sql template error", exc)
        } catch (exc: IOException) {
            LOGGER.error("compile sql template error:\n{}", query.sql, exc)
            throw LurrySqlException("compile sql template error", exc)
        } catch (exc: GroovyRuntimeException) {
            LOGGER.error("compile sql template error:\n{}", query.sql, exc)
            throw LurrySqlException("compile sql template error", exc)
        }
    }
}