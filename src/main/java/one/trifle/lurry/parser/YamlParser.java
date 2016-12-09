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

import one.trifle.lurry.exception.LurryParseFormatException;
import one.trifle.lurry.exception.LurryPermissionException;
import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * implementation {@code Parser} is used to convert yaml to lurry format
 * <p>
 * yaml example:
 * <pre>
 * com.mysite.Person:
 *     get: |-
 *         SELECT *
 *         FROM persons
 *         WHERE ${id ? "ID = $id" : ''}
 *               ${login ? "login = ${login.escape()} " : ""}
 *     delete: |-
 *         DELETE FROM persons
 *         WHERE ${id ? 'ID = ' + id : ''}
 *               ${login ? "login = ${login.escape()}" : ''}
 * com.mysite.Company:
 *     get: SELECT * FROM companies WHERE ID = $id
 *     delete: DELETE FROM companies WHERE ID = $id
 * </pre>
 *
 * @author Aleksey Dobrynin
 */
public class YamlParser implements Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlParser.class);

    @Override
    public List<Entity> parse(InputStream source) {
        LOGGER.trace("start parse yaml source");
        try {
            return new Mapper(source).map();
        } catch (ConstructorException | ClassNotFoundException exc) {
            LOGGER.trace("yaml parse error", exc);
            throw new LurryParseFormatException("yaml format exception", exc);
        } catch (YAMLException exc) {
            LOGGER.trace("yaml parse error", exc);
            throw new LurryPermissionException("yaml parse error", exc);
        }
    }

    private final class Mapper {
        private final InputStream source;

        Mapper(InputStream source) {
            this.source = source;
        }

        @SuppressWarnings("unchecked")
        List<Entity> map() throws ClassNotFoundException {
            Map values = new Yaml().loadAs(source, Map.class);
            List<Entity> entities = new ArrayList<>(values.size());
            for (Map.Entry<String, Map<String, String>> yamlEntity : ((Map<String, Map<String, String>>) values).entrySet()) {
                Entity entity = new Entity(Class.forName(yamlEntity.getKey()), new Query[yamlEntity.getValue().size()]);
                int position = 0;
                for (Map.Entry<String, String> yamlQuery : yamlEntity.getValue().entrySet()) {
                    entity.getQueries()[position++] = new Query(yamlQuery.getKey(), yamlQuery.getValue());
                }
                entities.add(entity);
            }
            return entities;
        }
    }
}
