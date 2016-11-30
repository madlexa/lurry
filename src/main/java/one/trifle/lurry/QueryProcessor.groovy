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
package one.trifle.lurry

import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic
import one.trifle.lurry.model.Query

import java.util.concurrent.ConcurrentHashMap

/**
 * TODO
 */
@CompileStatic
class QueryProcessor {

    private Map<Query, Template> cache = new ConcurrentHashMap<>()

    private Template get(Query query) {
        if (!cache[query]) {
            cache[query] = new GStringTemplateEngine().createTemplate(query.sql)
        }
        cache[query]
    }

    String exec(Query query, Map<String, Object> params) {
        get(query).make(new MissingPropertyMap(params)).toString()
    }

    /**
     * TODO optimize
     */
    private static class MissingPropertyMap<K, V> extends HashMap<K, V> {
        MissingPropertyMap(Map<K, V> params) {
            params.getMetaClass().getMetaMethods().each {
                this.getMetaClass()[it.name] = it
            }
            putAll(params)
        }

        @Override
        boolean containsKey(Object key) { true }
    }
}
