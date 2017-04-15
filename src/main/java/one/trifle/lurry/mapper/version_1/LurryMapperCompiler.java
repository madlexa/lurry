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
package one.trifle.lurry.mapper.version_1;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import one.trifle.lurry.exception.LurrySqlException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author Aleksey Dobrynin
 */
public class LurryMapperCompiler {
    public static final String MAIN_OBJECT_KEY = "main";

    private String uniqueCode;
    private String mappingCode;
    private String importCode;

    public LurryMapperCompiler(String unique, String mapping, String imports) {
        this.uniqueCode = unique;
        this.mappingCode = mapping;
        this.importCode = imports;
    }

    public ObjectMapper parse() {
        // TODO init unique object for
        CompilerConfiguration conf = new CompilerConfiguration();
        conf.setPluginFactory(new SourceUniquePreProcessor());
        final GroovyShell shell = new GroovyShell(new Binding(), conf);

        return new ObjectMapper() {
            @SuppressWarnings("unchecked")
            private final Map<String, List<String>> unique = (Map<String, List<String>>) shell.evaluate(uniqueCode);
            private final Map<String, Set<String>> uniqueHash = new HashMap<>();
            private final Map<String, Closure<Object>> mapping = new HashMap<>(); // TODO init new GroovyShell().evaluate("[" + mappingCode + "]");
            private final Map<String, Map<String, Object>> objs = new HashMap<>();

            {
//                ImportCustomizer importCustomizer = new ImportCustomizer();
//                importCustomizer.addImport("", "");
//                CompilerConfiguration configuration = new CompilerConfiguration();
//                configuration.addCompilationCustomizers(importCustomizer);
//                new GroovyShell(configuration).

//                new AstBuilder().buildFromSpec()
                for (Map.Entry<String, List<String>> entry : unique.entrySet()) {
                    uniqueHash.put(entry.getKey(), new HashSet<String>());
                    objs.put(entry.getKey(), new HashMap<String, Object>());
                }
            }

            @Override
            public void mapRow(ResultSet rs, int rowNum) {
                Map<String, Object> data = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : unique.entrySet()) { // init all objects from row
                    String hash = getHash(rs, entry.getValue());
                    if (!uniqueHash.get(entry.getKey()).contains(hash)) { // check unique object, if not init, then start init
                        Object obj = mapping.get(entry.getKey()).call(rs, data); // init entity object
                        objs.get(entry.getKey()).put(hash, obj); // cache object
                        data.put(entry.getKey(), obj); // add object for init other objects this row

                        uniqueHash.get(entry.getKey()).add(hash); // cache hash
                    } else {
                        data.put(entry.getKey(), objs.get(entry.getKey()).get(hash)); // add row cache
                    }
                }
            }

            @Override
            public List result() {
                return new ArrayList<>(objs.get(MAIN_OBJECT_KEY).values());
            }

            private String getHash(ResultSet data, List<String> fields) {
                StringBuilder result = new StringBuilder();
                for (String field : fields) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    result.append(getRowField(data, field));
                }
                return result.toString();
            }

            private String getRowField(ResultSet data, String field) {
                try {
                    return String.valueOf(data.getObject(field));
                } catch (SQLException exc) {
                    throw new LurrySqlException("get field = '" + field + "' error", exc);
                }
            }
        };
    }
}
