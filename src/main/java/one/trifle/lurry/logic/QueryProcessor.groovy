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
package one.trifle.lurry.logic

import groovy.text.GStringTemplateEngine
import groovy.text.Template
import one.trifle.lurry.database.DatabaseType
import one.trifle.lurry.model.Query

import javax.sql.DataSource

import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

/**
 * Processor for transform Lurry-Template to Sql String
 *
 * @author Aleksey Dobrynin
 */
class QueryProcessor {

    private DatabaseType type
    private final DataSource source;
    private Map<Query, Template> cache = new ConcurrentHashMap<>()

    /**
     * recommend single QueryProcessor for one DataSource
     *
     * @param source not null for prepare query and escape strings in queries
     */
    QueryProcessor(DataSource source) {
        this.source = source
    }

    private Class getMixed() {
        if (type == null) {
            Connection conn = source?.connection
            try {
                type = DatabaseType.of(conn?.metaData?.databaseProductName)
            } finally {
                conn?.close()
            }
        }
        type.mixed
    }

    /**
     * inject params into sql and exec specific code in template
     *
     * @param query object with sql-template
     * @param params data for inject
     * @return sql for execute
     */
    String prepare(Query query, Map<String, Object> params) {
        Map<String, Object> vals = new HashMap<String, Object>() {
            @Override
            boolean containsKey(Object key) { true }
        }
        vals.putAll(params)
        String result = ""
        use(getMixed()) {
            result = get(query).make(vals).toString()
        }
        return result
    }

    /**
     * get cached template
     *
     * @param query
     * @return GStringTemplate
     */
    private Template get(Query query) {
        Template template = cache[query]
        if (!template) {
            template = new GStringTemplateEngine().createTemplate(query.sql)
            cache[query] = template
        }
        template
    }
}
