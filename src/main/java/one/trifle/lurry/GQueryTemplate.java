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
package one.trifle.lurry;

import one.trifle.lurry.exception.LurryQueryException;
import one.trifle.lurry.exception.LurrySqlException;
import one.trifle.lurry.logic.QueryProcessor;
import one.trifle.lurry.mapper.DefaultMapRowMapper;
import one.trifle.lurry.mapper.DefaultRowMapper;
import one.trifle.lurry.mapper.RowMapper;
import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import one.trifle.lurry.parser.Parser;
import one.trifle.lurry.reader.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class of lurry library for execute query and map to object
 *
 * @author Aleksey Dobrynin
 */
public class GQueryTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GQueryTemplate.class);
    private final Reader reader;
    private final Parser parser;
    private final DataSource source;
    private final QueryProcessor processor;
    private Map<String, Query> cache = null;

    /**
     * recommend single QueryFactory for one DataSource
     *
     * @param source for escape string and execute query
     * @param reader reader implementation
     * @param parser parser implementation
     */
    public GQueryTemplate(DataSource source, Reader reader, Parser parser) {
        this.reader = reader;
        this.parser = parser;
        this.source = source;
        this.processor = new QueryProcessor(source);
    }

    private void init() {
        synchronized (this) {
            if (cache == null) {
                LOGGER.trace("start init data");
                cache = new HashMap<>();
                for (InputStream source : reader) {
                    LOGGER.trace("read source");
                    for (Entity entity : parser.parse(source)) {
                        LOGGER.trace("read entity {}", entity.getName().getName());
                        for (Query query : entity.getQueries()) {
                            cache.put(getCacheKey(entity.getName(), query.getName()), query);
                        }
                    }
                }
                LOGGER.trace("finish init data");
            }
        }
    }

    private Query getQuery(Class entity, String queryName) {
        if (cache == null) {
            init();
        }
        return cache.get(getCacheKey(entity, queryName));
    }

    String getSql(Class entity, String queryName, Map<String, Object> params) {
        return getSql(getQuery(entity, queryName), params);
    }

    private String getSql(Query query, Map<String, Object> params) {
        LOGGER.trace("start prepare sql");
        if (query == null) {
            throw new LurryQueryException("not found sql", null);
        }
        return processor.prepare(query, params);
    }

    /**
     * Method fined query with sql-template by entity and queryName
     * after inject params and execute sql
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @param mapper    instance for map row to java object
     * @param <T>       result class after mapping
     * @return unbounded list, after transform mapping
     */
    public <T> List<T> queryList(Class entity, String queryName, Map<String, Object> params, RowMapper<T> mapper) {
        String sql = getSql(entity, queryName, params);
        LOGGER.trace("execute: sql = {}", sql);
        List<T> result = new ArrayList<>();
        try (Connection conn = source.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int position = 0;
            while (rs.next()) {
                LOGGER.trace("entity = '{}', query = '{}'\nmap: row #{}", entity.getName(), queryName, position);
                result.add(mapper.mapRow(rs, position++));
            }
        } catch (SQLException exc) {
            LOGGER.error("execute query: entity = '{}', query = '{}'", entity.getName(), queryName, exc);
            throw new LurrySqlException("Error: execute query", exc);
        }
        return result;
    }

    /**
     * Method fined query with sql-template by entity and queryName
     * after inject params and execute sql. Use {@link DefaultRowMapper}
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @param <T>       result class after mapping
     * @return unbounded list, after transform mapping
     */
    public <T> List<T> queryList(Class<T> entity, String queryName, Map<String, Object> params) {
        return queryList(entity, queryName, params, new DefaultRowMapper<>(entity));
    }

    /**
     * Method fined query with sql-template by entity and queryName
     * after inject params and execute sql. Use {@link DefaultMapRowMapper}
     *
     * @param entity    result class
     * @param queryName name from source
     * @param params    params for inject in template
     * @return unbounded list with map key value
     */
    public List<Map<String, Object>> queryMap(Class entity, String queryName, Map<String, Object> params) {
        return queryList(entity, queryName, params, new DefaultMapRowMapper());
    }

    private String getCacheKey(Class entity, String query) {
        return entity.getName() + "_" + query;
    }
}
