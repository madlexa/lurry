package com.github.madlexa.lurry

import com.github.madlexa.lurry.model.Query
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic

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
        // TODO groovy.lang.MissingPropertyException
        get(query).make(params).toString()
    }
}
