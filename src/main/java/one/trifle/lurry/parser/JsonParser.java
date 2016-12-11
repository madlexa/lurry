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
package one.trifle.lurry.parser;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import one.trifle.lurry.exception.LurryParseFormatException;
import one.trifle.lurry.exception.LurryPermissionException;
import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * implementation {@code Parser} is used to convert json to lurry format
 * <p>
 * json example:
 * <pre>
 * [{
 *     "name": "com.mysite.Person",
 *     "queries": [
 *         {
 *             "name": "get",
 *             "sql": "SELECT * FROM persons WHERE ${id ? 'ID = ' + id : ''} ${login ? 'login = ' + login : ''}"
 *         },{
 *             "name": "delete",
 *             "sql": "DELETE FROM persons WHERE ${id ? 'ID = ' + id : ''} ${login ? 'login = ' + login : ''}"
 *         }
 *     ]
 * },{
 *     "name": "com.mysite.Company",
 *     "queries": [
 *         {
 *             "name": "get",
 *             "sql": "SELECT * FROM companies WHERE ID = $id"
 *         },{
 *             "name": "delete",
 *             "sql": "DELETE FROM companies WHERE ID = $id"
 *         }
 *     ]
 * }]
 * </pre>
 *
 * @author Aleksey Dobrynin
 */
public class JsonParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);

    @Override
    public List<Entity> parse(InputStream source) {
        LOGGER.trace("start parse json source");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = source.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return new Mapper(result.toString("UTF-8")).map();
        } catch (IOException exc) {
            LOGGER.trace("json parse error", exc);
            throw new LurryPermissionException("json parse error", exc);
        } catch (JsonSyntaxException | ClassNotFoundException exc) {
            LOGGER.trace("json parse error", exc);
            throw new LurryParseFormatException("json parse error", exc);
        }
    }

    private final class Mapper {
        private final String json;

        Mapper(String json) {
            this.json = json;
        }

        @SuppressWarnings("unchecked")
        List<Entity> map() throws ClassNotFoundException {
            Map[] values = new Gson().fromJson(json, Map[].class);
            List<Entity> result = new ArrayList<>(values.length);
            for (Map value : values) {
                List<Map> queries = (List<Map>) value.get("queries");
                Entity entity = new Entity(Class.forName(value.get("name").toString()), new Query[queries.size()]);
                for (int i = 0; i < queries.size(); i++) {
                    entity.getQueries()[i] = new Query(queries.get(i).get("name").toString(),
                            (String) queries.get(i).get("sql"));
                }
                result.add(entity);
            }
            return result;
        }
    }
}