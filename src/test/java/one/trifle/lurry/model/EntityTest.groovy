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
        'test' | []                               || 3556498
        'test' | null                             || 3556498
        'test' | [new Query("1")]                 || 3556498
        'test' | [new Query("1"), new Query("2")] || 3556498
    }

    @Unroll
    "equals(#obj1, #obj2) == #result"() {
        expect:
        obj1.equals(obj2) == result

        where:
        obj1                                            | obj2                                            || result
        new Entity('test')                              | new Entity('test')                              || true
        new Entity('test', null)                        | new Entity('test', null)                        || true
        new Entity('test', [] as Query[])               | new Entity('test', null)                        || true
        new Entity('test', null)                        | new Entity('test', [] as Query[])               || true
        new Entity('test', [] as Query[])               | new Entity('test', [] as Query[])               || true
        new Entity('test')                              | new Entity('2')                                 || false
        new Entity('test')                              | '2'                                             || false
        new Entity('test')                              | obj1                                            || true
        new Entity('test', [new Query('1')] as Query[]) | new Entity('test', [new Query('1')] as Query[]) || true
        new Entity('test', [new Query('1')] as Query[])                 | new Entity('test', [new Query('2')] as Query[])                 || false
        new Entity('test', [new Query('1'), new Query('2')] as Query[]) | new Entity('test', [new Query('2')] as Query[])                 || false
        new Entity('test', [new Query('1'), new Query('2')] as Query[]) | new Entity('test', [new Query('1'), new Query('2')] as Query[]) || true
        new Entity('test', [new Query('1'), new Query('2')] as Query[]) | new Entity('test', [new Query('2'), new Query('1')] as Query[]) || false
    }
}
