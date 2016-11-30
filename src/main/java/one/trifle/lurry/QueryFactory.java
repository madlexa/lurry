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

import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import one.trifle.lurry.parser.Parser;
import one.trifle.lurry.reader.Reader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author Aleksey Dobrynin
 */
public class QueryFactory {

    private final Reader reader;
    private final Parser parser;
    private Map<String, Query> cache = null;
    private QueryProcessor processor = new QueryProcessor();

    public QueryFactory(Reader reader, Parser parser) {
        this.reader = reader;
        this.parser = parser;
    }

    private void init() {
        synchronized (this) {
            if (cache == null) {
                cache = new HashMap<>();
                for (InputStream source : reader) {
                    for (Entity entity : parser.parse(source)) {
                        for (Query query : entity.getQueries()) {
                            cache.put(getCacheKey(entity.getName(), query.getName()), query);
                        }
                    }
                }
            }
        }
    }

    public String get(String entity, String query, Map<String, Object> params) {
        if (cache == null) {
            init();
        }
        Query data = cache.get(getCacheKey(entity, query));
        if (data == null) {
            return "";
        }
        return processor.exec(data, params);
    }

    private String getCacheKey(String entity, String query) {
        return entity + "_" + query;
    }
}
