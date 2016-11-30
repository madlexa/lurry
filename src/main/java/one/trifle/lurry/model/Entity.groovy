package one.trifle.lurry.model

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * TODO
 */
@CompileStatic
@EqualsAndHashCode
class Entity {
    String name
    Query[] queries

    Entity() {}

    Entity(String name, Query[] queries) {
        this.name = name
        this.queries = queries
    }
}
