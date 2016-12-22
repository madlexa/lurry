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
package one.trifle.lurry.mapper.version_1;

import groovy.lang.GroovyShell;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author Aleksey Dobrynin
 */
public class LurryMapperCompiler {
    private String uniqueCode;
    private String mappingCode;

    public LurryMapperCompiler(String unique, String mapping) {
        this.uniqueCode = unique;
        this.mappingCode = mapping;
    }

    @SuppressWarnings("unchecked")
    public ObjectMapper parse() {
        return new ObjectMapper() {
            private final Map<String, String> unique = (Map<String, String>) new GroovyShell().evaluate("[" + uniqueCode + "]");

            @Override
            public void mapRow(ResultSet rs, int rowNum) {

            }

            @Override
            public List result() {
                return null;
            }
        };
    }
}
