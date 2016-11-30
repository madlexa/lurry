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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public List<Entity> parse(InputStream source) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = source.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return new Mapper(result.toString("UTF-8")).map();
        } catch (IOException exc) {
            throw new LurryPermissionException("json parse error", exc);
        } catch (JsonSyntaxException exc) {
            throw new LurryParseFormatException("json parse error", exc);
        }
    }

    private final class Mapper {
        private final String json;

        Mapper(String json) {
            this.json = json;
        }

        List<Entity> map() throws IOException {
            return Arrays.asList(new Gson().fromJson(json, Entity[].class));
        }
    }
}
