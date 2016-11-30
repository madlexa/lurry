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
