package one.trifle.lurry.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class QueryTest extends Specification {
    @Unroll
    "hashCode(#name) == #code"() {
        expect:
        new Query(name, sql).hashCode() == code

        where:
        name   | sql   || code
        'test' | null  || 3556498
        'test' | ""    || 3556498
        'test' | "sql" || 3556498
    }

    @Unroll
    "equals(#obj1, #obj2) == #result"() {
        expect:
        obj1.equals(obj2) == result

        where:
        obj1                     | obj2                     || result
        new Query('test')        | new Query('test')        || true
        new Query('test', null)  | new Query('test', null)  || true
        new Query('test', "")    | new Query('test', null)  || true
        new Query('test')        | new Query('2')           || false
        new Query('test')        | '2'                      || false
        new Query('test')        | obj1                     || true
        new Query('test', "123") | new Query('test', "321") || true
    }
}
