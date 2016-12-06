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
import one.trifle.lurry.model.Query

import java.util.concurrent.ConcurrentHashMap

/**
 * TODO
 *
 * @author Aleksey Dobrynin
 */
class QueryProcessor {

    private Map<Query, Template> cache = new ConcurrentHashMap<>()

    String exec(Query query, Map<String, Object> params) {
        Map<String, Object> vals = new HashMap<String, Object>() {
            @Override
            boolean containsKey(Object key) { true }
        }
        vals.putAll(params)
        String result = ""
        use(SafeString) {
            result = get(query).make(vals).toString()
        }
        return result
    }

    private Template get(Query query) {
        Template template = cache[query]
        if (!template) {
            template = new GStringTemplateEngine().createTemplate(query.sql)
            cache[query] = template
        }
        template
    }

    // TODO login from database type
    @Category(String)
    private class SafeString {
        String escape() {
            this.replaceAll("'", "''")
        }

        String quote() {
            "'${this}'"
        }
    }
}
