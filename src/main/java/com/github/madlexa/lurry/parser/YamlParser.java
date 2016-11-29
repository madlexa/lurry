package com.github.madlexa.lurry.parser;

import com.github.madlexa.lurry.exception.LurryParseFormatException;
import com.github.madlexa.lurry.exception.LurryPermissionException;
import com.github.madlexa.lurry.model.Entity;
import com.github.madlexa.lurry.model.Query;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public class YamlParser implements Parser {
    @Override
    public List<Entity> parse(InputStream source) {
        try {
            return new Mapper(source).map();
        } catch (ConstructorException exc) {
            throw new LurryParseFormatException("yaml format exception", null);
        } catch (YAMLException exc) {
            throw new LurryPermissionException("yaml parse error", exc);
        }
    }

    private final class Mapper {
        private final InputStream source;

        Mapper(InputStream source) {
            this.source = source;
        }

        List<Entity> map() {
            Map data = new Yaml().loadAs(source, Map.class);
            List<Entity> entities = new ArrayList<>(data.size());
            for (Map.Entry<String, Map<String, String>> yamlEntity : ((Map<String, Map<String, String>>) data).entrySet()) {
                Entity entity = new Entity(yamlEntity.getKey(), new Query[yamlEntity.getValue().size()]);
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
