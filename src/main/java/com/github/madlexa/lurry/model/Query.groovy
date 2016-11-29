package com.github.madlexa.lurry.model

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * TODO
 */
@CompileStatic
@EqualsAndHashCode
class Query {
    String name
    String sql

    Query() {}

    Query(String name, String sql) {
        this.name = name
        this.sql = sql
    }
}
