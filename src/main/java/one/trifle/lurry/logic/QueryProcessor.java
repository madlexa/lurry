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
package one.trifle.lurry.logic;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import one.trifle.lurry.database.DatabaseType;
import one.trifle.lurry.exception.LurryIllegalArgumentException;
import one.trifle.lurry.exception.LurrySqlException;
import one.trifle.lurry.model.Query;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor for transform Lurry-Template to Sql String
 *
 * @author Aleksey Dobrynin
 */
public class QueryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryProcessor.class);

    private final DataSource source;
    private DatabaseType type;
    private Map<Query, Template> cache = new ConcurrentHashMap<>();

    /**
     * recommend single QueryProcessor for one DataSource
     *
     * @param source not null for prepare query and escape strings in queries
     */
    public QueryProcessor(DataSource source) {
        if (source == null) {
            LOGGER.error("database source cannot be null");
            throw new LurryIllegalArgumentException("database source cannot be null", null);
        }
        this.source = source;
    }

    private Class getMixed() {
        if (type == null) {
            try (Connection conn = source.getConnection()) {
                if (conn == null) {
                    LOGGER.error("connection empty");
                    throw new LurrySqlException("connection empty", null);
                }
                DatabaseMetaData metaData = conn.getMetaData();
                if (metaData == null) {
                    LOGGER.error("metaData empty");
                    throw new LurrySqlException("metaData empty", null);
                }
                type = DatabaseType.of(metaData.getDatabaseProductName());
            } catch (SQLException exc) {
                LOGGER.error("get resource type error", exc);
                throw new LurrySqlException("get resource type error", exc);
            }
        }
        return type.getMixed();
    }

    /**
     * inject params into sql and exec specific code in template
     *
     * @param query  object with sql-template
     * @param params data for inject
     * @return sql for execute
     */
    public String prepare(Query query, Map<String, Object> params) {
        Map<String, Object> vals = new HashMap<String, Object>() {
            @Override
            public boolean containsKey(Object key) {
                return true;
            }
        };
        vals.putAll(params);

        return DefaultGroovyMethods.use(QueryProcessor.class, getMixed(),
                new Closure<String>(this, this) {
                    public String doCall() {
                        return get(query).make(vals).toString();
                    }
                });
    }

    /**
     * get cached template
     *
     * @param query
     * @return GStringTemplate
     */
    private Template get(Query query) {
        Template template = cache.get(query);
        if (template == null) {
            try {
                template = new GStringTemplateEngine().createTemplate(query.getSql());
                cache.put(query, template);
            } catch (ClassNotFoundException | IOException | GroovyRuntimeException exc) {
                LOGGER.error("compile sql template error:\n{}", query.getSql(), exc);
                throw new LurrySqlException("compile sql template error", exc);
            }
        }
        return template;
    }
}