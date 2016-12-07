package one.trifle.lurry.model

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class EntityTest extends Specification {
    @Unroll
    "hashCode(#name) == #code"() {
        expect:
        new Entity(name, queries as Query[]).hashCode() == code

        where:
        name   | queries                          || code
        Person | []                               || 556652879
        Person | null                             || 556652879
        Person | [new Query("1")]                 || 556652879
        Person | [new Query("1"), new Query("2")] || 556652879
    }

    @Unroll
    "equals(#obj1, #obj2) == #result"() {
        expect:
        obj1.equals(obj2) == result

        where:
        obj1                                                            | obj2                                                            || result
        new Entity(Person)                                              | new Entity(Person)                                              || true
        new Entity(Person, null)                                        | new Entity(Person, null)                                        || true
        new Entity(Person, [] as Query[])                               | new Entity(Person, null)                                        || true
        new Entity(Person, null)                                        | new Entity(Person, [] as Query[])                               || true
        new Entity(Person, [] as Query[])                               | new Entity(Person, [] as Query[])                               || true
        new Entity(Person)                                              | new Entity(Person2)                                             || false
        new Entity(Person)                                              | '2'                                                             || false
        new Entity(Person)                                              | obj1                                                            || true
        new Entity(Person, [new Query('1')] as Query[])                 | new Entity(Person, [new Query('1')] as Query[])                 || true
        new Entity(Person, [new Query('1')] as Query[])                 | new Entity(Person, [new Query('2')] as Query[])                 || false
        new Entity(Person, [new Query('1'), new Query('2')] as Query[]) | new Entity(Person, [new Query('2')] as Query[])                 || false
        new Entity(Person, [new Query('1'), new Query('2')] as Query[]) | new Entity(Person, [new Query('1'), new Query('2')] as Query[]) || true
        new Entity(Person, [new Query('1'), new Query('2')] as Query[]) | new Entity(Person, [new Query('2'), new Query('1')] as Query[]) || false
    }

    static class Person {}

    static class Person2 {}
}
