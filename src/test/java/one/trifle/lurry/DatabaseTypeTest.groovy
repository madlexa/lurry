package one.trifle.lurry

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class DatabaseTypeTest extends Specification {
    @Unroll
    "MYSQL escape(#str) == #escape"() {
        expect:
        ((DatabaseType) type).escape(str) == escape

        where:
        type               | str          || escape
        DatabaseType.MYSQL | "test"       || "test"
        DatabaseType.MYSQL | null         || null
        DatabaseType.MYSQL | "\"'test'\"" || "\"''test''\""
        DatabaseType.MYSQL | "\\test\\"   || "\\\\test\\\\"
    }

    @Unroll
    "escape(#str) == #escape"() {
        expect:
        DatabaseType.values().findAll { it != DatabaseType.MYSQL }.each {
            it.escape(str) == escape
        }

        where:
        str          || escape
        "test"       || "test"
        null         || null
        "\"'test'\"" || "\"''test''\""
        "\\test\\"   || "\\test\\"
    }
}