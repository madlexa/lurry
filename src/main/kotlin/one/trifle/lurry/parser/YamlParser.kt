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
package one.trifle.lurry.parser

import one.trifle.lurry.exception.LurryParseFormatException
import one.trifle.lurry.mapper.LurryMapper
import one.trifle.lurry.model.Entity
import one.trifle.lurry.model.Query
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.InputStream

/**
 * implementation {@code Parser} is used to convert yaml to lurry format
 * <p>
 * yaml example:
 * <pre>
 * com.mysite.Person:
 *     get:
 *         sql: |-
 *             SELECT p.ID, p.login, c.ID AS cID, c.value
 *             FROM persons p
 *             LEFT JOIN contacts c ON c.personID = p.ID
 *             WHERE ${id ? "p.ID = $id" : ''}
 *                   ${login ? "p.login = ${login.escape()} " : ""}
 *         mapper:
 *             version: 1.0
 *             unique: |-
 *                 main: ID
 *                 contact: cID
 *             import: |-
 *                 one.trifle.lurry.model.Person
 *                 one.trifle.lurry.model.Contact
 *             mapping: |-
 *                 main: new Person(id: row.ID, login: row.login, contacts: [])
 *                 contact:
 *                     #main.contacts << new Contact(id: row.cID, value: row.value)
 *     delete: |-
 *         DELETE FROM persons
 *         WHERE ${id ? 'ID = ' + id : ''}
 *               ${login ? "login = ${login.escape()}" : ''}
 * com.mysite.Company:
 *     get:
 *         sql: SELECT * FROM companies WHERE ID = $id
 *         mapper: my.package.MyMapper
 *     delete: DELETE FROM companies WHERE ID = $id
 * </pre>
 *
 * @author Aleksey Dobrynin
 */
class YamlParser : Parser {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(YamlParser::class.java)
    }

    override fun parse(source: InputStream): List<Entity> {
        LOGGER.trace("start parse yaml source")
        try {
            return Yaml().loadAs(source, Map::class.java).entries.map({ entry ->
                Entity(Class.forName(entry.key as String),
                        (entry.value as Map<*, *>).entries
                                .map { query(it) }
                                .toTypedArray())
            }).toList()
        } catch (exc: YAMLException) {
            LOGGER.error("yaml parse error", exc)
            throw LurryParseFormatException("yaml parse error", exc)
        }
    }

    fun query(entry: Map.Entry<*, *>): Query {
        val value = entry.value
        val query = Query(entry.key as String, when (value) {
            is String -> value
            is Map<*, *> -> value["sql"] as String
            else -> throw LurryParseFormatException("i dont know type = [" + entry.key + "]", null)
        })
        if (value is Map<*, *>) {
            val mapper = value["mapper"]
            when (mapper) {
                is String -> query.mapper = Class.forName(mapper) as Class<out LurryMapper>
                is Map<*, *> -> TODO()
                else -> throw LurryParseFormatException("i dont know type = [" + entry.key + "]", null)
            }
        }
        return query
    }
}